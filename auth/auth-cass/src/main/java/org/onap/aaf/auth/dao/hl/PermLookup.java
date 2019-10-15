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

package org.onap.aaf.auth.dao.hl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

/**
 * PermLookup is a Storage class for the various pieces of looking up Permission
 * during Transactions to avoid duplicate processing
 *
 * @author Jonathan
 *
 */
// Package on purpose
public class PermLookup {
    private AuthzTrans trans;
    private String user;
    private Question q;
    private Result<List<UserRoleDAO.Data>> userRoles = null;
    private Result<List<RoleDAO.Data>> roles = null;
    private Result<Set<String>> permNames = null;
    private Result<List<PermDAO.Data>> perms = null;

    private PermLookup() {}

    public static PermLookup get(AuthzTrans trans, Question q, String user) {
        PermLookup lp=null;
        Map<String, PermLookup> permMap = trans.get(Question.PERMS, null);
        if (permMap == null) {
            trans.put(Question.PERMS, permMap = new HashMap<>());
        } else {
            lp = permMap.get(user);
        }

        if (lp == null) {
            lp = new PermLookup();
            lp.trans = trans;
            lp.user = user;
            lp.q = q;
            permMap.put(user, lp);
        }
        return lp;
    }

    public Result<List<UserRoleDAO.Data>> getUserRoles() {
        if (userRoles==null) {
            userRoles = q.userRoleDAO().readByUser(trans,user);
            if (userRoles.isOKhasData()) {
                List<UserRoleDAO.Data> lurdd = new ArrayList<>();
                Date now = new Date();
                for (UserRoleDAO.Data urdd : userRoles.value) {
                    if (urdd.expires.after(now)) { // Remove Expired
                        lurdd.add(urdd);
                    }
                }
                if (lurdd.size()==0) {
                    return userRoles = Result.err(Status.ERR_UserNotFound,
                                "%s not found or not associated with any Roles: ",
                                user);
                } else {
                    return userRoles = Result.ok(lurdd);
                }
            } else {
                return userRoles;
            }
        } else {
            return userRoles;
        }
    }

    public Result<List<RoleDAO.Data>> getRoles() {
        if (roles==null) {
            Result<List<UserRoleDAO.Data>> rur = getUserRoles();
            if (rur.isOK()) {
                List<RoleDAO.Data> lrdd = new ArrayList<>();
                for (UserRoleDAO.Data urdata : rur.value) {
                    // Gather all permissions from all Roles
                        if (urdata.ns==null || urdata.rname==null) {
                            return Result.err(Status.ERR_BadData,"DB Content Error: nulls in User Role %s %s", urdata.user,urdata.role);
                        } else {
                            Result<List<RoleDAO.Data>> rlrd = q.roleDAO().read(
                                    trans, urdata.ns, urdata.rname);
                            if (rlrd.isOK()) {
                                lrdd.addAll(rlrd.value);
                            }
                        }
                    }
                return roles = Result.ok(lrdd);
            } else {
                return roles = Result.err(rur);
            }
        } else {
            return roles;
        }
    }

    public Result<Set<String>> getPermNames() {
        if (permNames==null) {
            Result<List<RoleDAO.Data>> rlrd = getRoles();
            if (rlrd.isOK()) {
                Set<String> pns = new TreeSet<>();
                for (RoleDAO.Data rdata : rlrd.value) {
                    pns.addAll(rdata.perms(false));
                }
                return permNames = Result.ok(pns);
            } else {
                return permNames = Result.err(rlrd);
            }
        } else {
            return permNames;
        }
    }

    public Result<List<PermDAO.Data>> getPerms(boolean lookup) {
        if (perms==null) {
            // Note: It should be ok for a Valid user to have no permissions -
            // Jonathan 8/12/2013
            Result<Set<String>> rss = getPermNames();
            if (rss.isOK()) {
                List<PermDAO.Data> lpdd = new ArrayList<>();
                for (String perm : rss.value) {
                    if (lookup) {
                        Map<String,PermDAO.Data> mspdd = new TreeMap<>();
                        Result<String[]> ap = PermDAO.Data.decodeToArray(trans, q, perm);
                        if (ap.isOK()) {

                            Result<List<PermDAO.Data>> rlpd = q.permDAO().read(perm,trans,ap.value);
                            if (rlpd.isOKhasData()) {
                                for (PermDAO.Data pData : rlpd.value) {
                                    // ONLY add perms/roles which are related to this lookup
                                    for(String pdr : pData.roles(false)) {
                                        for(RoleDAO.Data r : roles.value) {
                                            if(pdr.equals(r.encode())) {
                                                PermDAO.Data pdd = mspdd.get(pData.fullPerm());
                                                if(pdd==null) {
                                                    pdd = new PermDAO.Data();
                                                    pdd.ns = pData.ns;
                                                    pdd.type = pData.type;
                                                    pdd.instance = pData.instance;
                                                    pdd.action = pData.action;
                                                    pdd.description = pData.description;
                                                    lpdd.add(pdd);
                                                }
                                                pdd.roles(true).add(pdr);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            trans.error().log("In getPermsByUser, for", user, perm);
                        }
                    } else {
                        Result<PermDAO.Data> pr = PermDAO.Data.decode(trans, q, perm);
                        if (pr.notOK()) {
                            trans.error().log("In getPermsByUser, for", user, pr.errorString());
                        } else {
                            lpdd.add(pr.value);
                        }
                    }

                }
                return perms = Result.ok(lpdd);
            } else {
                return perms = Result.err(rss);
            }
        } else {
            return perms;
        }
    }
}
