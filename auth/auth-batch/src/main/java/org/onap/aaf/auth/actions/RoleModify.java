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
import org.onap.aaf.auth.dao.cass.RoleDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.Perm;
import org.onap.aaf.auth.helpers.Role;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class RoleModify extends ActionDAO<Role,RoleDAO.Data,RoleModify.Modify> {
    public RoleModify(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster, dryRun);
    }
    
    public RoleModify(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<RoleDAO.Data> exec(final AuthzTrans trans, final Role r,final RoleModify.Modify modify) {
        Result<List<Data>> rr = q.roleDAO.read(trans, r.ns,r.name);
        if(dryRun) {
            if(rr.isOKhasData()) {
                return Result.ok(rr.value.get(0));
            } else {
                return Result.err(Result.ERR_NotFound, "Data not Found " + r.toString());
            }
        } else {
            Result<Data> rv = null;
            if(rr.isOKhasData()) {
                for(final Data d : rr.value) {
                    modify.change(d);
                    if(d.ns.equals(r.ns) && d.name.equals(r.name)) {
                        // update for fields
                        // In either case, adjust Roles
                        for(String p : d.perms) {
                            if(!r.perms.contains(p)) {
                                Result<PermDAO.Data> rpdd = PermDAO.Data.decode(trans, q, p);
                                if(rpdd.isOKhasData()) {
                                    q.roleDAO.dao().addPerm(trans, d, rpdd.value);
                                }
                            }
                        }
                        for(String p : r.perms) {
                            if(!d.perms.contains(p)) {
                                Result<PermDAO.Data> rpdd = PermDAO.Data.decode(trans, q, p);
                                if(rpdd.isOKhasData()) {
                                    q.roleDAO.dao().delPerm(trans, d, rpdd.value);
                                }
                            }
                        }
                        rv = Result.ok(d);
                    } else {                
                        for(String p : d.perms) {
                            Perm perm = Perm.keys.get(p);
                            if(perm!=null) {
                                if(perm.roles.contains(r.encode())) {
                                    modify.permModify().exec(trans, perm, new PermModify.Modify() {
                                        @Override
                                        public RoleModify roleModify() {
                                            return RoleModify.this;
                                        }
                                        
                                        @Override
                                        public void change(PermDAO.Data pdd) {
                                            pdd.roles.remove(r.encode());
                                            pdd.roles.add(d.encode());
                                        }
                                    });
                                }
                            }
                        }
                        Result<List<Data>> preexist = q.roleDAO.read(trans, d);
                        if(preexist.isOKhasData()) {
                            Data rdd = preexist.value.get(0);
                            for(String p : d.perms) {
                                Result<PermDAO.Data> perm = PermDAO.Data.decode(trans, q, p);
                                if(perm.isOKhasData()) {
                                    q.roleDAO.dao().addPerm(trans,rdd, perm.value);
                                }
                            }
                            rv = Result.ok(rdd);
                        } else {
                            rv = q.roleDAO.create(trans, d);
                        }
                        if(rv.isOK()) {
                            trans.info().printf("Updating %s|%s to %s|%s", r.ns, r.name, d.ns, d.name);
                            RoleDAO.Data rmme = new RoleDAO.Data();
                            rmme.ns=r.ns;
                            rmme.name=r.name;
                            q.roleDAO.delete(trans, rmme, false);
                            
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
        void change(RoleDAO.Data ur);
        PermModify permModify();
    }
    
    public Result<Void> delete(AuthzTrans trans, Role r) {
        if(dryRun) {
            return Result.ok();
        } else {
            RoleDAO.Data data = new RoleDAO.Data();
            data.ns=r.ns;
            data.name = r.name;
            return q.roleDAO.delete(trans,data,false);
        }
    }
}