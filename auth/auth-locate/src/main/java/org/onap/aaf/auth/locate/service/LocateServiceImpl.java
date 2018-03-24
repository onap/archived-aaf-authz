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

import java.util.UUID;

import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.locate.mapper.Mapper;
import org.onap.aaf.auth.locate.validation.LocateValidator;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.misc.env.APIException;

import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;

public class LocateServiceImpl<IN,OUT,ERROR> 
	  implements LocateService<IN,OUT,Endpoints,MgmtEndpoints,ERROR> {
		private Mapper<IN,OUT,Endpoints,MgmtEndpoints,ERROR> mapper;
		private LocateDAO locateDAO;
		private boolean permToRegister;
	
		public LocateServiceImpl(AuthzTrans trans, LocateDAO locateDAO, Mapper<IN,OUT,Endpoints,MgmtEndpoints,ERROR> mapper) throws APIException {
			this.mapper = mapper;
			this.locateDAO = locateDAO; 
			permToRegister = false; //TODO Setup a Configuration for this
		}
		
		public Mapper<IN,OUT,Endpoints,MgmtEndpoints,ERROR> mapper() {return mapper;}

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
			if(v.err()) {
				return Result.err(Result.ERR_BadData,v.errs());
			}
			int count = 0;
			for(MgmtEndpoint me : meps.getMgmtEndpoint()) {
				if(permToRegister) { 
					int dot = me.getName().lastIndexOf('.'); // Note: Validator checks for NS for getName()
					AAFPermission p = new AAFPermission(me.getName().substring(0,dot)+".locator",me.getName(),"write"); 
					if(trans.fish(p)) {
						LocateDAO.Data data = mapper.locateData(me);
						locateDAO.update(trans, data, true);
						++count;
					} else {
						return Result.err(Result.ERR_Denied,"May not register service (needs " + p.getKey() + ')');
					}
				} else { //TODO if(MechID is part of Namespace) { 
					LocateDAO.Data data = mapper.locateData(me);
					locateDAO.update(trans, data, true);
					++count;
				}
			}
			if(count>0) {
				return Result.ok();
			} else {
				return Result.err(Result.ERR_NotFound, "No endpoints found");
			}
		}

		/* (non-Javadoc)
		 * @see org.onap.aaf.auth.locate.service.GwService#removeMgmtEndPoints(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.Object)
		 */
		@Override
		public Result<Void> removeMgmtEndPoints(AuthzTrans trans, MgmtEndpoints meps) {
			LocateValidator v = new LocateValidator().mgmt_endpoint_key(meps);
			if(v.err()) {
				return Result.err(Result.ERR_BadData,v.errs());
			}
			int count = 0;
			for(MgmtEndpoint me : meps.getMgmtEndpoint()) {
				int dot = me.getName().lastIndexOf('.'); // Note: Validator checks for NS for getName()
				AAFPermission p = new AAFPermission(me.getName().substring(0,dot)+".locator",me.getHostname(),"write"); 
				if(trans.fish(p)) {
					LocateDAO.Data data = mapper.locateData(me);
					data.port_key = UUID.randomUUID();
					locateDAO.delete(trans, data, false);
					++count;
				} else {
					return Result.err(Result.ERR_Denied,"May not register service (needs " + p.getKey() + ')');
				}
			}
			if(count>0) {
				return Result.ok();
			} else {
				return Result.err(Result.ERR_NotFound, "No endpoints found");
			}
		}


//////////////// APIs ///////////////////
};
