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
import java.util.List;

import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.PermDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Perm;
import org.onap.aaf.auth.helpers.Role;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class PermModify extends ActionDAO<Perm,PermDAO.Data,PermModify.Modify> {
    public PermModify(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public PermModify(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<PermDAO.Data> exec(AuthzTrans trans, final Perm p, final Modify modify) {
        Result<List<PermDAO.Data>> rr = q.permDAO.read(trans, p.ns,p.type,p.instance,p.action);
        if(dryRun) {
            if(rr.isOKhasData()) {
                return Result.ok(rr.value.get(0));
            } else {
                return Result.err(Result.ERR_NotFound, "Data not Found " + p.toString());
            }
        } else {
            Result<PermDAO.Data> rv = null;
            if(rr.isOKhasData()) {
                for(final Data d : rr.value) {
                    modify.change(d);
                    if(d.ns.equals(p.ns) && d.type.equals(p.type) && d.instance.equals(p.instance) && d.action.equals(p.action)) {
                        // update for fields
                        // In either case, adjust Permissions
                        for(String r : d.roles) {
                            if(!p.roles.contains(r)) {
                                q.permDAO.dao().addRole(trans, d, r);
                            }
                        }
                        for(String r : p.roles) {
                            if(!d.roles.contains(r)) {
                                q.permDAO.dao().delRole(trans, d, r);
                            }
                        }
                        rv = Result.ok(d);
                    } else {
                        for(String r : d.roles) {
                            Role role = Role.keys.get(r);
                            if(role.perms.contains(p.encode())) {
                                modify.roleModify().exec(trans, role, new RoleModify.Modify() {
                                    @Override
                                    public PermModify permModify() {
                                        return PermModify.this;
                                    }
                                    
                                    @Override
                                    public void change(RoleDAO.Data rdd) {
                                        rdd.perms.remove(p.encode());
                                        rdd.perms.add(d.encode());
                                    }
                                });
                            }
                        }
        
                        rv = q.permDAO.create(trans, d);
                        if(rv.isOK()) {
                            PermDAO.Data pdd = new PermDAO.Data();
                            pdd.ns = p.ns;
                            pdd.type = p.type;
                            pdd.instance = p.instance;
                            pdd.action = p.action;
                            q.permDAO.delete(trans, pdd, false);
                            trans.info().printf("Updated %s|%s|%s|%s to %s|%s|%s|%s\n", 
                                p.ns, p.type, p.instance, p.action, 
                                d.ns, d.type, d.instance, d.action);
                        } else {
                            trans.info().log(rv.errorString());
                        }
                    }
                    
                }
            } else {
                rv = Result.err(rr);
            }
            if(rv==null) {
                rv = Result.err(Status.ERR_General,"Never get to this code");
            }
    
            return rv;
        }
    }
    
    public static interface Modify {
        void change(PermDAO.Data ur);
        RoleModify roleModify();
    }

    public Result<Void> delete(AuthzTrans trans, Perm p) {
        if(dryRun) {
            return Result.ok();
        } else {
            PermDAO.Data data = new PermDAO.Data();
            data.ns=p.ns;
            data.type = p.type;
            data.instance = p.instance;
            data.action = p.action;
            return q.permDAO.delete(trans,data,false);
        }
    }
    
}