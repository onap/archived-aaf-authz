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

package org.onap.aaf.auth.service.mapper;

import java.util.Collection;
import java.util.List;

import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.DelegateDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.HistoryDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.MayChange;
import org.onap.aaf.misc.rosetta.Marshal;

public interface Mapper<
    NSS,
    PERMS,
    PERMKEY,
    ROLES,
    USERS,
    USERROLES,
    DELGS,
    CERTS,
    KEYS,
    REQUEST,
    HISTORY,
    ERROR,
    APPROVALS>
{
    enum API{NSS,NS_REQ,
             PERMS,PERM_KEY,PERM_REQ,
             ROLES,ROLE,ROLE_REQ,ROLE_PERM_REQ,
             USERS,USER_ROLE_REQ,USER_ROLES,
             CRED_REQ,CERTS,
             APPROVALS,
             DELGS,DELG_REQ,
             KEYS,
             HISTORY,
             ERROR,
             API,
             VOID};
    public Class<?> getClass(API api);
    public<A> Marshal<A> getMarshal(API api);
    public<A> A newInstance(API api);

    public Result<PermDAO.Data> permkey(AuthzTrans trans, PERMKEY from);
    public Result<PermDAO.Data> perm(AuthzTrans trans, REQUEST from);
    public Result<RoleDAO.Data> role(AuthzTrans trans, REQUEST from);
    public Result<Namespace> ns(AuthzTrans trans, REQUEST from);
    public Result<CredDAO.Data> cred(AuthzTrans trans, REQUEST from, boolean requiresPass);
    public Result<USERS> cred(List<CredDAO.Data> lcred, USERS to);
    public Result<CERTS> cert(List<CertDAO.Data> lcert, CERTS to);
    public Result<DelegateDAO.Data> delegate(AuthzTrans trans, REQUEST from);
    public Result<DELGS> delegate(List<DelegateDAO.Data> lDelg);
    public Result<APPROVALS> approvals(List<ApprovalDAO.Data> lAppr);
    public Result<List<ApprovalDAO.Data>> approvals(APPROVALS apprs);
    public Result<List<PermDAO.Data>> perms(AuthzTrans trans, PERMS perms);

    public Result<UserRoleDAO.Data> userRole(AuthzTrans trans, REQUEST from);
    public Result<PermDAO.Data> permFromRPRequest(AuthzTrans trans, REQUEST from);
    public REQUEST ungrantRequest(AuthzTrans trans, String role, String type, String instance, String action);
    public Result<RoleDAO.Data> roleFromRPRequest(AuthzTrans trans, REQUEST from);

    /*
     * Check Requests of varying sorts for Future fields set
     */
    public Result<FutureDAO.Data> future(AuthzTrans trans, String table, REQUEST from, Bytification content, boolean enableApproval, Memo memo, MayChange mc);

    public Result<NSS> nss(AuthzTrans trans, Namespace from, NSS to);

    // Note: Prevalidate if NS given is allowed to be seen before calling
    public Result<NSS> nss(AuthzTrans trans, Collection<Namespace> from, NSS to);
//    public Result<NSS> ns_attrib(AuthzTrans trans, Set<String> from, NSS to);
    public Result<PERMS> perms(AuthzTrans trans, List<PermDAO.Data> from, PERMS to, boolean filter);
    public Result<PERMS> perms(AuthzTrans trans, List<PermDAO.Data> from, PERMS to, String[] scopes, boolean filter);
    public Result<ROLES> roles(AuthzTrans trans, List<RoleDAO.Data> from, ROLES roles, boolean filter);
    // Note: Prevalidate if NS given is allowed to be seen before calling
    public Result<USERS> users(AuthzTrans trans, Collection<UserRoleDAO.Data> from, USERS to);
    public Result<USERROLES> userRoles(AuthzTrans trans, Collection<UserRoleDAO.Data> from, USERROLES to);
    public Result<KEYS> keys(Collection<String> from);

    public Result<HISTORY> history(AuthzTrans trans, List<HistoryDAO.Data> history, final int sort);

    public ERROR errorFromMessage(StringBuilder holder, String msgID, String text, String... detail);

    /*
     * A Memo Creator... Use to avoid creating superfluous Strings until needed.
     */
    public static interface Memo {
        public String get();
    }



}
