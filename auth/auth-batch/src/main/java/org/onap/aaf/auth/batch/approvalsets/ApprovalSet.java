/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.util.Chrono;

public class ApprovalSet {
    private DataView dataview;
    protected FutureDAO.Data fdd;
    protected List<ApprovalDAO.Data> ladd;

    public ApprovalSet(final GregorianCalendar start, final String target, final DataView dv) {
        dataview = dv;
        fdd = new FutureDAO.Data();
        fdd.id = Chrono.dateToUUID(System.currentTimeMillis());
        fdd.target = target;
        fdd.start = start.getTime();
        ladd = new ArrayList<>();
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
        if(ladd == null || ladd.isEmpty()) {
            errs = new StringBuilder("No Approvers for ");
            errs .append(fdd.memo);
        } else {
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
        }
        return errs==null?Result.ok():Result.err(Result.ERR_Backend,errs.toString());
    }

    public boolean hasApprovals() {
        return !ladd.isEmpty();
    }

    public Set<String> approvers() {
        Set<String> rv = new HashSet<>();
        for(ApprovalDAO.Data app : ladd) {
            rv.add(app.approver);
        }
        return rv;
    }
}