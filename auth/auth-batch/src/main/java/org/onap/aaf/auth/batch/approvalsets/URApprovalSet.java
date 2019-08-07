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

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.onap.aaf.auth.batch.helpers.Approval;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO.Data;
import org.onap.aaf.auth.dao.hl.Function.FUTURE_OP;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.misc.env.util.Chrono;

public class URApprovalSet extends ApprovalSet {
    private static final String FMT_SUFFIX = "%s] - Expires %s";
    private static final String EXTEND_ACCESS_FMT = Approval.RE_APPROVAL_IN_ROLE + "%s] to Role [" + FMT_SUFFIX;
    private static final String REVALIDATE_AS_ADMIN_FMT = Approval.RE_VALIDATE_ADMIN + FMT_SUFFIX;
    private static final String REVALIDATE_AS_OWNER_FMT = Approval.RE_VALIDATE_OWNER + FMT_SUFFIX;

    public URApprovalSet(final AuthzTrans trans, final GregorianCalendar start, final DataView dv, final Loader<UserRoleDAO.Data> lurdd) throws IOException, CadiException {
        super(start, "user_role", dv);
        Organization org = trans.org();
        UserRoleDAO.Data urdd = lurdd.load();
        setConstruct(urdd.bytify());
        setMemo(getMemo(urdd));
        GregorianCalendar expires = org.expiration(null, Organization.Expiration.UserInRole);
        if(urdd.expires.before(expires.getTime())) {
            expires.setTime(urdd.expires);
        }
        setExpires(expires);
        setTargetKey(urdd.user+'|'+urdd.role);
        setTargetDate(urdd.expires);
        
        Result<RoleDAO.Data> r = dv.roleByName(trans, urdd.role);
        if(r.notOKorIsEmpty()) {
            throw new CadiException(r.errorString());
        }
        Result<NsDAO.Data> n = dv.ns(trans, urdd.ns);
        if(n.notOKorIsEmpty()) {
            throw new CadiException(n.errorString());
        }
        UserRoleDAO.Data found = null;
        Result<List<Data>> lur = dv.ursByRole(trans, urdd.role);
        if(lur.isOK()) {
            for(UserRoleDAO.Data ur : lur.value) {
                if(urdd.user.equals(ur.user)) {
                    found = ur;
                    break;
                }
            }
        }
        if(found==null) {
            throw new CadiException(String.format("User '%s' in Role '%s' does not exist", urdd.user,urdd.role));
        }
        
        // Primarily, Owners are responsible, unless it's owned by self
        boolean isOwner = false;
        Result<List<UserRoleDAO.Data>> owners = dv.ursByRole(trans, urdd.ns+".owner");
        if(owners.isOK()) {
            for(UserRoleDAO.Data owner : owners.value) {
                if(urdd.user.equals(owner.user)) {
                    isOwner = true;
                } else {
                    ApprovalDAO.Data add = newApproval(urdd);
                    add.approver = owner.user;
                    add.type="owner";
                    ladd.add(add);
                }
            }
        }

        if(isOwner) {
            try {
                List<Identity> apprs = org.getApprovers(trans, urdd.user);
                if(apprs!=null) {
                    for(Identity i : apprs) {
                        ApprovalDAO.Data add = newApproval(urdd);
                        add.approver = i.fullID();
                        add.type = org.getApproverType();
                        ladd.add(add);
                    }
                }
            } catch (OrganizationException e) {
                throw new CadiException(e);
            }
        }
    }
    
    private void setTargetDate(Date expires) {
        fdd.target_date = expires;
    }

    private void setTargetKey(String key) {
        fdd.target_key = key;
    }

    private ApprovalDAO.Data newApproval(UserRoleDAO.Data urdd) {
        ApprovalDAO.Data add = new ApprovalDAO.Data();
        add.id = Chrono.dateToUUID(System.currentTimeMillis());
        add.ticket = fdd.id;
        add.user = urdd.user;
        add.operation = FUTURE_OP.A.name();
        add.status = ApprovalDAO.PENDING;
        add.memo = getMemo(urdd);
        return add;
    }

    private String getMemo(Data urdd) {
        switch(urdd.rname) {
        case "owner":
            return String.format(REVALIDATE_AS_OWNER_FMT,urdd.ns,Chrono.dateOnlyStamp(urdd.expires));
        case "admin":
            return String.format(REVALIDATE_AS_ADMIN_FMT,urdd.ns,Chrono.dateOnlyStamp(urdd.expires));
        default:
            return String.format(EXTEND_ACCESS_FMT,
                       urdd.user,
                       urdd.role,
                       Chrono.dateOnlyStamp(urdd.expires));
        }
    }

}
