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

package org.onap.aaf.auth.actions;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.hl.Function;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Approval;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization.Expiration;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;

public class URFutureApprove extends ActionDAO<UserRole, String,String> implements Action<UserRole,String,String>, Key<UserRole> {
    private final Date start;
    private final Date expires;

    public URFutureApprove(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans,cluster, dryRun);
        GregorianCalendar gc = new GregorianCalendar();
        start = gc.getTime();
        expires = trans.org().expiration(gc, Expiration.Future).getTime();
    }
    
    public URFutureApprove(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
        GregorianCalendar gc = new GregorianCalendar();
        start = gc.getTime();
        expires = trans.org().expiration(gc, Expiration.Future).getTime();
    }

    @Override
    public Result<String> exec(AuthzTrans trans, UserRole ur,String text) {
        if (dryRun) {
            return Result.ok(text);
        } else {
            Result<NsDAO.Data> rns = q.deriveNs(trans, ur.ns());
            if (rns.isOK()) {
                
                FutureDAO.Data data = new FutureDAO.Data();
                data.id=null; // let Create function assign UUID
                data.target=Function.FOP_USER_ROLE;
                
                data.memo = key(ur);
                data.start = start;
                data.expires = ur.expires();
                try {
                    data.construct = ur.urdd().bytify();
                } catch (IOException e) {
                    return Result.err(e);
                }
                Result<String> rfuture = f.createFuture(trans, data, Function.FOP_USER_ROLE, ur.user(), rns.value, FUTURE_OP.A);
                if (rfuture.isOK()) {
                    trans.info().log(rfuture.value, text, ur.user(), data.memo);
                } else {
                    trans.error().log(rfuture.details, text);
                }
                return rfuture;
            } else {
                return Result.err(rns);
            }
        }
    }
    
    @Override
    public String key(UserRole ur) {
        String expire;
        if (expires.before(start)) {
            expire = "' - EXPIRED ";
        } else {
            expire = "' - expiring ";
        }
        
        if (Question.OWNER.equals(ur.rname())) {
            return Approval.RE_VALIDATE_OWNER + ur.ns() + expire + Chrono.dateOnlyStamp(ur.expires());
        } else if (Question.ADMIN.equals(ur.rname())) {
            return Approval.RE_VALIDATE_ADMIN + ur.ns() + expire + Chrono.dateOnlyStamp(ur.expires());
        } else {
            return Approval.RE_APPROVAL_IN_ROLE + ur.role() + expire + Chrono.dateOnlyStamp(ur.expires());
        }
    }

}