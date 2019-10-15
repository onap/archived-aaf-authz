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

package org.onap.aaf.auth.service.facade;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.RServlet;

/**
 * AuthzFacade
 *   This layer is responsible for covering the Incoming Messages, be they XML, JSON or just entries on the URL,
 *   and converting them to data that can be called on the Service Layer.
 *   
 *   Upon response, this layer, because it knew the incoming Data Formats (i.e. XML/JSON), the HTTP call types
 *   are set on "ContentType" on Response.
 *   
 *   Finally, we wrap the call in Time Stamps with explanation of what is happing for Audit trails.
 *   
 * @author Jonathan
 *
 */
public interface AuthzFacade {
    public static final int PERM_DEPEND_424 = -1000;
    public static final int ROLE_DEPEND_424 = -1001;

    /*
     * Namespaces
     */
    public abstract Result<Void> requestNS(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, NsType type);

    public abstract Result<Void> getNSsByName(AuthzTrans trans, HttpServletResponse resp, String ns, boolean full);

    public abstract Result<Void> getNSsByAdmin(AuthzTrans trans, HttpServletResponse resp, String user, boolean full);

    public abstract Result<Void> getNSsByResponsible(AuthzTrans trans, HttpServletResponse resp, String user, boolean full);

    public abstract Result<Void> getNSsByEither(AuthzTrans trans, HttpServletResponse resp, String user, boolean full);

    public abstract Result<Void> getNSsChildren(AuthzTrans trans, HttpServletResponse resp, String pathParam);

    public abstract Result<Void> addAdminToNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id);

    public abstract Result<Void> delAdminFromNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id);

    public abstract Result<Void> addResponsibilityForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id);

    public abstract Result<Void> delResponsibilityForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String id);

    public abstract Result<Void> updateNsDescription(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> deleteNS(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, String ns);

    // NS Attribs
    public abstract Result<Void> createAttribForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String key, String value);

    public abstract Result<Void> readNsByAttrib(AuthzTrans trans, HttpServletResponse resp, String key);

    public abstract Result<Void> updAttribForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String key, String value);

    public abstract Result<Void> delAttribForNS(AuthzTrans trans, HttpServletResponse resp, String ns, String key);

    /*
     * Permissions
     */
    public abstract Result<Void> createPerm(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> getPermsByName(AuthzTrans trans, HttpServletResponse resp, 
            String type, String instance, String action);

    public abstract Result<Void> getPermsByUser(AuthzTrans trans, HttpServletResponse response, String user);

    public abstract Result<Void> getPermsByUserScope(AuthzTrans trans, HttpServletResponse resp, String user, String[] scopes);

    public abstract Result<Void> getPermsByUserWithAAFQuery(AuthzTrans trans, HttpServletRequest request, HttpServletResponse response, String user);

    public abstract Result<Void> getPermsByType(AuthzTrans trans, HttpServletResponse resp, String type);

    public abstract Result<Void> getPermsForRole(AuthzTrans trans, HttpServletResponse response, String roleName);

    public abstract Result<Void> getPermsByNS(AuthzTrans trans, HttpServletResponse response, String ns);

    public abstract Result<Void> renamePerm(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp,
            String type, String instance, String action);

    public abstract Result<Void> updatePermDescription(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> resetPermRoles(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> deletePerm(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> deletePerm(AuthzTrans trans,    HttpServletResponse resp, 
            String perm, String type, String action);

    /*
     * Roles
     */
    public abstract Result<Void> createRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse response);

    public abstract Result<Void> getRolesByName(AuthzTrans trans,HttpServletResponse resp, String name);

    public abstract Result<Void> getRolesByNS(AuthzTrans trans, HttpServletResponse resp, String ns);

    public abstract Result<Void> getRolesByNameOnly(AuthzTrans trans, HttpServletResponse resp, String nameOnly);

    public abstract Result<Void> getRolesByUser(AuthzTrans trans, HttpServletResponse resp, String user);

    public abstract Result<Void> getRolesByPerm(AuthzTrans trans, HttpServletResponse resp, String type, String instance, String action);

    public abstract Result<Void> updateRoleDescription(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> addPermToRole(AuthzTrans trans,HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> delPermFromRole(AuthzTrans trans,HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> delPermFromRole(AuthzTrans trans, HttpServletResponse resp, 
            String role, String type, String instance, String action);

    public abstract Result<Void> deleteRole(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> deleteRole(AuthzTrans trans, HttpServletResponse resp, String role);

    /*
     * Users
     */

    public abstract Result<Void> getUsersByRole(AuthzTrans trans, HttpServletResponse resp, String role);

    public abstract Result<Void> getUsersByPermission(AuthzTrans trans, HttpServletResponse resp, 
            String type, String instance, String action);



    /*
     * Delegates
     */
    public abstract Result<Void> createDelegate(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> updateDelegate(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> deleteDelegate(AuthzTrans trans,  HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> deleteDelegate(AuthzTrans trans,  String user);

    public abstract Result<Void> getDelegatesByUser(AuthzTrans trans, String userName, HttpServletResponse resp);

    public abstract Result<Void> getDelegatesByDelegate(AuthzTrans trans, String userName, HttpServletResponse resp);

    /*
     * Credentials
     */
    public abstract Result<Void> createUserCred(AuthzTrans trans, HttpServletRequest req);

    public abstract Result<Void> changeUserCred(AuthzTrans trans, HttpServletRequest req);

    public abstract Result<Void> extendUserCred(AuthzTrans trans, HttpServletRequest req, String days);

    public abstract Result<Void> getCredsByNS(AuthzTrans trans,    HttpServletResponse resp, String ns);

    public abstract Result<Void> getCredsByID(AuthzTrans trans, HttpServletResponse resp, String id);

    public abstract Result<Void> deleteUserCred(AuthzTrans trans, HttpServletRequest req);

    public abstract Result<Void> validBasicAuth(AuthzTrans trans, HttpServletResponse resp, String basicAuth);

    public abstract Result<Date> doesCredentialMatch(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    /*
     * Miscellaneous
     */
    /**
     * Place Standard Messages based on HTTP Code onto Error Data Structure, and write to OutputStream
     * Log message
     */
    public abstract void error(AuthzTrans trans, HttpServletResponse response, Result<?> result);

    /*
     * UserRole
     */
    public abstract Result<Void> requestUserRole(AuthzTrans trans,HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> getUserInRole(AuthzTrans trans, HttpServletResponse resp, String user, String role);

    public abstract Result<Void> getUserRolesByRole(AuthzTrans trans, HttpServletResponse resp, String role);

    public abstract Result<Void> getUserRolesByUser(AuthzTrans trans, HttpServletResponse resp, String user);

    public abstract Result<Void> deleteUserRole(AuthzTrans trans, HttpServletResponse resp, String user, String role);

    /*
     * resetUsersForRoles and resetRolesForUsers is too dangerous and not helpful.
     */

    public abstract Result<Void> extendUserRoleExpiration(AuthzTrans trans, HttpServletResponse resp, String user,
    String role);

    /*
     * Approval 
     */
    public abstract Result<Void> updateApproval(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp);

    public abstract Result<Void> getApprovalsByUser(AuthzTrans trans, HttpServletResponse resp, String user);

    public abstract Result<Void> getApprovalsByTicket(AuthzTrans trans, HttpServletResponse resp, String ticket);

    public abstract Result<Void> getApprovalsByApprover(AuthzTrans trans, HttpServletResponse resp, String approver);


    /*
     * History
     */
    public abstract Result<Void> getHistoryByUser(AuthzTrans trans,    HttpServletResponse resp, String user, int[] yyyymm, final int sort);

    public abstract Result<Void> getHistoryByRole(AuthzTrans trans,    HttpServletResponse resp, String role, int[] yyyymm, final int sort);

    public abstract Result<Void> getHistoryByPerm(AuthzTrans trans,    HttpServletResponse resp, String perm, int[] yyyymm, final int sort);

    public abstract Result<Void> getHistoryByNS(AuthzTrans trans,    HttpServletResponse resp, String ns, int[] yyyymm, final int sort);

    public abstract Result<Void> getHistoryBySubject(AuthzTrans trans, HttpServletResponse resp, String type, String subject, int[] yyyymm, int sort);

    /*
     * Cache 
     */
    public abstract Result<Void> cacheClear(AuthzTrans trans, String pathParam);

    public abstract Result<Void> cacheClear(AuthzTrans trans, String string,String segments);

    public abstract void dbReset(AuthzTrans trans);



    /*
     * API
     */
    public Result<Void> getAPI(AuthzTrans trans, HttpServletResponse resp, RServlet<AuthzTrans> rservlet);

    public abstract Result<Void> getAPIExample(AuthzTrans trans, HttpServletResponse resp, String typeCode, boolean optional);

    public abstract Result<Void> getCertInfoByID(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp, String id);






}