/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.batch.update;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.BatchPrincipal;
import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.batch.helpers.CQLBatch;
import org.onap.aaf.auth.batch.helpers.CQLBatchLoop;
import org.onap.aaf.auth.batch.helpers.Cred;
import org.onap.aaf.auth.batch.helpers.Future;
import org.onap.aaf.auth.batch.helpers.LastNotified;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.batch.helpers.X509;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

public class Remove extends Batch {
	private final AuthzTrans noAvg;
	private HistoryDAO historyDAO;
	private CQLBatch cqlBatch;

	public Remove(AuthzTrans trans) throws APIException, IOException, OrganizationException {
		super(trans.env());
		trans.info().log("Starting Connection Process");

		noAvg = env.newTransNoAvg();
		noAvg.setUser(new BatchPrincipal("Remove"));

		TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
		try {
			historyDAO = new HistoryDAO(trans, cluster, CassAccess.KEYSPACE);
			TimeTaken tt2 = trans.start("Connect to Cluster", Env.REMOTE);
			try {
				session = historyDAO.getSession(trans);
			} finally {
				tt2.done();
			}
			cqlBatch = new CQLBatch(noAvg.info(),session); 


		} finally {
			tt0.done();
		}
	}

	@Override
	protected void run(AuthzTrans trans) {

		// Create Intermediate Output 
		File logDir = logDir();

		List<File> remove = new ArrayList<>();
		if(args().length>0) {
			for(int i=0;i<args().length;++i) {
				remove.add(new File(logDir, args()[i]));
			}
		} else {
			remove.add(new File(logDir,"Delete"+Chrono.dateOnlyStamp()+".csv"));
		}

		for(File f : remove) {
			trans.init().log("Processing File:",f.getAbsolutePath());
		}

		final Holder<Boolean> ur = new Holder<>(false);
		final Holder<Boolean> cred = new Holder<>(false);
		final Holder<Boolean> x509 = new Holder<>(false);
		final Holder<String> memoFmt = new Holder<String>("");
		final HistoryDAO.Data hdd = new HistoryDAO.Data();
		final String orgName = trans.org().getName();

		hdd.action="delete";
		hdd.reconstruct = ByteBuffer.allocate(0);
		hdd.user = noAvg.user();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
		hdd.yr_mon = Integer.parseInt(sdf.format(new Date()));

		try { 
			final CQLBatchLoop cbl = new CQLBatchLoop(cqlBatch,50,dryRun);
			for(File f : remove) {
				trans.info().log("Processing ",f.getAbsolutePath(),"for Deletions");
				if(f.exists()) {
					CSV removeCSV = new CSV(env.access(),f);
					try {
						removeCSV.visit( row -> {
							switch(row.get(0)) {
								case "info":
									switch(row.get(1)) {
										case "Delete":
											memoFmt.set("%s expired from %s on %s");
											break;
										case "NotInOrgDelete":
											memoFmt.set("Identity %s was removed from %s on %s");
											break;
									}
									break;
								case "ur":
									if(!ur.get()) {
										ur.set(true);
									}
									//TODO If deleted because Role is no longer there, double check...
									
									UserRole.batchDelete(cbl.inc(),row);
									hdd.target=UserRoleDAO.TABLE; 
									hdd.subject=UserRole.histSubject(row);
									hdd.memo=UserRole.histMemo(memoFmt.get(), row);
									historyDAO.createBatch(cbl.inc(), hdd);
									break;
								case "cred":
									if(!cred.get()) {
										cred.set(true);
									}
									Cred.batchDelete(cbl.inc(),row);
									hdd.target=CredDAO.TABLE; 
									hdd.subject=Cred.histSubject(row);
									hdd.memo=Cred.histMemo(memoFmt.get(), orgName,row);
									historyDAO.createBatch(cbl.inc(), hdd);
									break;
								case "x509":
									if(!x509.get()) {
										x509.set(true);
									}
									X509.batchDelete(cbl.inc(),row);
									hdd.target="x509"; 
									hdd.subject=X509.histSubject(row);
									hdd.memo=X509.histMemo(memoFmt.get(),row);
									historyDAO.createBatch(cbl.inc(), hdd);
									break;
								case "future":
									// Not cached
									Future.deleteByIDBatch(cbl.inc(),row.get(1));
									break;
								case "approval":
									// Not cached
									Approval.deleteByIDBatch(cbl.inc(),row.get(1));
									break;
								case "notified":
									LastNotified.delete(cbl.inc(),row);
									break;
							}
						});
						cbl.flush();
					} catch (IOException | CadiException e) {
						e.printStackTrace();
					}
				} else {
					trans.error().log("File",f.getAbsolutePath(),"does not exist.");
				}
			}
		} finally {
			TimeTaken tt = trans.start("Touch UR,Cred and Cert Caches",Trans.REMOTE);
			try {
				if(ur.get()) {
					cqlBatch.touch(UserRoleDAO.TABLE, 0, UserRoleDAO.CACHE_SEG, dryRun);
				}
				if(cred.get()) {
					cqlBatch.touch(CredDAO.TABLE, 0, CredDAO.CACHE_SEG, dryRun);
				}
				if(x509.get()) {
					cqlBatch.touch(CertDAO.TABLE, 0, CertDAO.CACHE_SEG, dryRun);
				}
			} finally {
				tt.done();
			}
		}
	}

	@Override
	protected void _close(AuthzTrans trans) {
		session.close();
	}

}
