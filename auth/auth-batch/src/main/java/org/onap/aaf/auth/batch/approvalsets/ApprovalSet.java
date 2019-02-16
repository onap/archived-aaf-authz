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
package org.onap.aaf.auth.batch.approvalsets;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.misc.env.util.Chrono;

public class ApprovalSet {
	private DataView dataview;
	protected FutureDAO.Data fdd;
	protected List<ApprovalDAO.Data> ladd;
	
	public ApprovalSet(final GregorianCalendar start, final String target, final DataView dv) throws CadiException {
		dataview = dv;
		fdd = new FutureDAO.Data();
        try {
			fdd.id = newID(target);
		} catch (NoSuchAlgorithmException e) {
			throw new CadiException(e);
		} 
		fdd.target = target;
		fdd.start = start.getTime();
		ladd = new ArrayList<>();
	}
	
	protected UUID newID(String target) throws NoSuchAlgorithmException {
		StringBuilder sb = new StringBuilder(new String(SecureRandom.getInstanceStrong().generateSeed(10)));
        sb.append(target);
        sb.append(System.currentTimeMillis());
        return Chrono.dateToUUID(System.currentTimeMillis());
	}

	protected void setConstruct(final ByteBuffer bytes) {
		fdd.construct = bytes;
	}

	protected void setMemo(final String memo) {
		fdd.memo = memo;
	}
	
	protected void setExpires(final GregorianCalendar expires) {
		fdd.expires = expires.getTime();
	}
	
	public Result<Void> write(AuthzTrans trans) {
		StringBuilder errs = null;
		Result<FutureDAO.Data> rf = dataview.insert(trans, fdd);
		if(rf.notOK()) {
			errs = new StringBuilder();
			errs.append(rf.errorString());
		} else {
			for(ApprovalDAO.Data add : ladd) {
				Result<ApprovalDAO.Data> af = dataview.insert(trans, add);
				if(af.notOK()) {
					if(errs==null) {
						errs = new StringBuilder();
					} else {
						errs.append('\n');
					}
					errs.append(af.errorString());
				}
			}
		}
		return errs==null?Result.ok():Result.err(Result.ERR_Backend,errs.toString());
	}
}