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

package org.onap.aaf.auth.locate.service;

import java.util.List;

import org.onap.aaf.auth.dao.cass.ConfigDAO;
import org.onap.aaf.auth.dao.cass.ConfigDAO.Data;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.mapper.Mapper;
import org.onap.aaf.auth.locate.validation.LocateValidator;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.misc.env.APIException;

import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;
import locate.v1_1.Configuration;
import locate.v1_1.Configuration.Props;

public class LocateServiceImpl<IN,OUT,ERROR> 
      implements LocateService<IN,OUT,Endpoints,MgmtEndpoints,Configuration,ERROR> {
        private Mapper<IN,OUT,Endpoints,MgmtEndpoints,Configuration,ERROR> mapper;
        protected LocateDAO locateDAO;
        private ConfigDAO configDAO;
        private boolean permToRegister;

        public LocateServiceImpl(AuthzTrans trans, AAF_Locate locate, Mapper<IN,OUT,Endpoints,MgmtEndpoints,Configuration,ERROR> mapper){
            this.mapper = mapper;
            this.locateDAO = locate.locateDAO;
            this.configDAO = locate.configDAO;
            permToRegister = false; //TODO Setup a Configuration for this
        }
    
        public Mapper<IN,OUT,Endpoints,MgmtEndpoints,Configuration,ERROR> mapper() {return mapper;}

        @Override
        public Result<Endpoints> getEndPoints(AuthzTrans trans, String service, String version, String other) {
            return mapper.endpoints(locateDAO.readByName(trans, service), version, other);
        }

        /* (non-Javadoc)
         * @see org.onap.aaf.auth.locate.service.GwService#putMgmtEndPoints(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
         */
        @Override
        public Result<Void> putMgmtEndPoints(AuthzTrans trans, MgmtEndpoints meps) {
            LocateValidator v = new LocateValidator().mgmt_endpoints(meps, false);
            if (v.err()) {
                return Result.err(Result.ERR_BadData,v.errs());
            }
            int count = 0;
            StringBuilder denied = null;
            for (MgmtEndpoint me : meps.getMgmtEndpoint()) {
                if (permToRegister) { 
                    int dot = me.getName().lastIndexOf('.'); // Note: Validator checks for NS for getName()
                    AAFPermission p = new AAFPermission(me.getName().substring(0,dot),"locator",me.getHostname(),"write"); 
                    if (!trans.fish(p)) {
                        if(denied==null) {
                            denied = new StringBuilder("May not register service(s):");
                        }
                    
                        denied.append("\n\t");
                        denied.append(p.getKey());
                        denied.append(')');
                        continue;
                    }
                }
                LocateDAO.Data data = mapper.locateData(me);
                locateDAO.update(trans, data, true);
                ++count;
            }
            if (count>0) {
                return Result.ok();
            } else {
                return denied==null?Result.err(Result.ERR_NotFound, "No endpoints found")
                        :Result.err(Result.ERR_Security,denied.toString());
            }
        }

        /* (non-Javadoc)
         * @see org.onap.aaf.auth.locate.service.GwService#removeMgmtEndPoints(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
         */
        @Override
        public Result<Void> removeMgmtEndPoints(AuthzTrans trans, MgmtEndpoints meps) {
            LocateValidator v = new LocateValidator().mgmt_endpoint_key(meps);
            if (v.err()) {
                return Result.err(Result.ERR_BadData,v.errs());
            }
            int count = 0;
            StringBuilder denied = null;
            for (MgmtEndpoint me : meps.getMgmtEndpoint()) {
                 if (permToRegister) { 
                     int dot = me.getName().lastIndexOf('.'); // Note: Validator checks for NS for getName()
                     AAFPermission p = new AAFPermission(me.getName().substring(0,dot),"locator",me.getHostname(),"write"); 
                     if (!trans.fish(p)) {
                         if(denied==null) {
                             denied = new StringBuilder("May not deregister service(s):");
                         }
                     
                         denied.append("\n\t");
                         denied.append(p.getKey());
                         denied.append(')');
                         continue;
                     }
                 }
                 LocateDAO.Data data = mapper.locateData(me);
                 locateDAO.delete(trans, data, true);
                 ++count;
            }
            if (count>0) {
                return Result.ok();
            } else {
                return denied==null?Result.err(Result.ERR_NotFound, "No endpoints found")
                        :Result.err(Result.ERR_Security,denied.toString());
            }
        }

        /////   ADDED v1_1
        /* (non-Javadoc)
         * @see org.onap.aaf.auth.locate.service.LocateService#getConfig(org.onap.aaf.auth.env.AuthzTrans, java.lang.String, java.lang.String)
         *
         * Note: "id" is put in, in case we need to filter, or direct data change in the future by Permission
         */
        @Override
        public Result<Configuration> getConfig(AuthzTrans trans, String id, String type) {
            Result<List<Data>> dr = configDAO.readName(trans, type);
            Configuration c = new Configuration();
            c.setName(type);
            Props p;
        
            if (dr.isOKhasData()) {
                for (ConfigDAO.Data data : dr.value) {
                    p = new Props();
                    p.setTag(data.tag);
                    p.setValue(data.value);
                    c.getProps().add(p);
                }
            }
            return Result.ok(c);
        }


//////////////// APIs ///////////////////
};
