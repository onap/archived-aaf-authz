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

package org.onap.aaf.auth.batch.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.helpers.NS;
import org.onap.aaf.auth.batch.helpers.UserRole;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class MassMail extends Batch {

	private Map<String, CSV.Writer> writerList;
	private final String from;
	private String subject = "AAF Information";
	private boolean bOwners = true;
	private boolean bAdmins = false;
	private boolean bAppIDOnly = false;
	private int escalate = 0;
	private final File subdir;
	private final String head;
	private final String content;
	private final String tail;

	public MassMail(AuthzTrans trans) throws APIException, IOException, OrganizationException {
		super(trans.env());
		from = trans.getProperty("MAIL_FROM");
		if (from == null) {
			throw new APIException("No MAIL_FROM property set");
		}
		String subdirName = null;
		// Flags
		for (String arg : args()) {
			switch (arg) {
			case "-owners":
				bOwners = true;
				break;
			case "-admins":
				bAdmins = true;
				break;
			case "-appid_only":
				bAppIDOnly = true;
				break;
			default:
				if (arg.length() > 10 && arg.startsWith("-escalate=")) {
					escalate = Integer.parseInt(arg.substring(10));
				} else if (subdirName == null) {
					subdirName = arg;
				}
			}
		}

		subdir = new File(logDir(), subdirName == null ? "mailing" : subdirName);
		if (subdir.exists()) {
			if (!subdir.isDirectory()) {
				throw new APIException(subdirName + " is not a directory");
			}
		} else {
			subdir.mkdirs();
		}

		subject = readIn(subdir, "subject");
		head = readIn(subdir, "html.head");
		content = readIn(subdir,"html.content");
		tail = readIn(subdir,"html.tail");
		
		trans.info().log("Starting Connection Process");
		TimeTaken tt0 = trans.start("Cassandra Initialization", Env.SUB);
		try {
			TimeTaken tt = trans.start("Connect to Cluster", Env.REMOTE);
			try {
				session = cluster.connect();
			} finally {
				tt.done();
			}

			// Create Intermediate Output
			writerList = new HashMap<>();

			NS.load(trans, session, NS.v2_0_11);
			UserRole.load(trans, session, UserRole.v2_0_11);
//            now = new Date();
//            String sdate = Chrono.dateOnlyStamp(now);
		} finally {
			tt0.done();
		}
	}

	private String readIn(File subdir, String name) throws IOException {
		File file = new File(subdir, name);
		byte[] bytes = new byte[(int) file.length()];
		FileInputStream fis = new FileInputStream(file);
		try {
			fis.read(bytes);
			return new String(bytes);
		} finally {
			fis.close();
		}
	}

	@Override
	protected void run(AuthzTrans trans) {
//        try {
		trans.info().log("Create a Mass Mailing");

		final AuthzTrans transNoAvg = trans.env().newTransNoAvg();
		final Organization org = trans.org();
		if (org != null) {
			StringBuilder to = new StringBuilder();
			StringBuilder cc = new StringBuilder();
			StringBuilder greet = new StringBuilder();
			for (NS ns : NS.data.values()) {
				if (bAppIDOnly) {
					ResultSet results;
					Statement stmt = new SimpleStatement(
							String.format("SELECT count(*) FROM authz.cred WHERE ns='%s';", ns.ndd.name));
					results = session.execute(stmt);
					long count = results.one().getLong(0);
					if (count <= 0) {
						continue;
					}
				}

				to.setLength(0);
				cc.setLength(0);
				greet.setLength(0);
				if (bOwners) {
					StringBuilder o = to;
					List<UserRole> owners = UserRole.getByRole().get(ns.ndd.name + ".owner");
					if (owners.isEmpty()) {
						trans.error().log(ns.ndd.name, "has no owners!");
					} else {
						for (UserRole owner : owners) {
							try {
								Identity identity = org.getIdentity(transNoAvg, owner.user());
								if (identity.isPerson()) {
									if (o.length() > 0) {
										o.append(',');
										greet.append(',');
									}
									o.append(identity.email());
									greet.append(identity.firstName());
									for (int i = 0; i < escalate; ++i) {
										identity = identity.responsibleTo();
										if (identity == null) {
											break;
										}
										if (cc.length() > 0) {
											cc.append(',');
										}
										cc.append(identity.email());
									}
								}
							} catch (OrganizationException e) {
								trans.error().log(e, "Error Reading Organization");
							}
						}
					}
				}

				if (bAdmins) {
					StringBuilder a;
					if (bOwners) {
						a = cc;
					} else {
						a = to;
					}
					List<UserRole> admins = UserRole.getByRole().get(ns.ndd.name + ".admin");
					if (admins.isEmpty()) {
						trans.warn().log(ns.ndd.name, "has no admins!");
					} else {
						for (UserRole admin : admins) {
							try {
								Identity identity = org.getIdentity(transNoAvg, admin.user());
								if (identity.isPerson()) {
									if (a.length() > 0) {
										a.append(',');
									}
									a.append(identity.email());
									if (!bOwners) {
										if (greet.length() > 0) {
											greet.append(',');
										}
										greet.append(identity.firstName());
									}
								}
							} catch (OrganizationException e) {
								trans.error().log(e, "Error Reading Organization");
							}
						}
					}
				}

				try {
					PrintStream ps = new PrintStream(new FileOutputStream(
							subdir.getPath() + File.separatorChar + "email_" + ns.ndd.name + ".hdr"));
					try {
						ps.print("TO: ");
						ps.println(to);
						ps.print("CC: ");
						ps.println(cc);
						ps.print("FROM: ");
						ps.println(from);
						ps.print("SUBJECT: ");
						ps.println(subject);
					} finally {
						ps.close();
					}
					
					ps = new PrintStream(new FileOutputStream(
							subdir.getPath() + File.separatorChar + "email_" + ns.ndd.name + ".html"));
					try {
						ps.println(head.replaceAll("%FIRST_NAMES%", greet.toString()));
						ps.println(content.replaceAll("%NS%", ns.ndd.name));
						ps.println(tail);
					} finally {
						ps.close();
					}
				} catch (IOException e) {
					trans.error().log(e);
				}

			}
//            	UserRole.load(transNoAvg, session, UserRole.v2_0_11, ur -> {
//	            	if("owner".equals(ur.rname())) {
//	            		try {
//							Identity identity = org.getIdentity(transNoAvg, ur.user());
//							if(identity.isPerson()) {
//								System.out.println("Emailing " + identity.email());
//							}
//						} catch (OrganizationException e) {
//		                    trans.error().log(e, "Error Reading Organization");
//						}
//	            	}
//                    Organization org = trans.org();
//                        Identity identity = org.getIdentity(trans, ur.row);
//
//                    if(!check(transNoAvg, checked, ur.user())) {
//                        ur.row(whichWriter(transNoAvg,ur.user()),UserRole.UR);
//                    }
//                } catch (OrganizationException e) {
//                    trans.error().log(e, "Error Decrypting X509");
//                }
//            	});
		}

//        } catch (OrganizationException e) {
//            trans.info().log(e);

	}

	@Override
	protected void _close(AuthzTrans trans) {
		session.close();
		for (CSV.Writer cw : writerList.values()) {
			cw.close();
		}
	}

}
