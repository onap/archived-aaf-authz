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

package org.onap.aaf.auth.locate.facade;

import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.locate.service.LocateService;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;

import locate.v1_0.Endpoints;
import locate.v1_0.MgmtEndpoints;
import locate.v1_1.Configuration;
import locate_local.v1_0.InRequest;
import locate_local.v1_0.Out;
import locate_local.v1_0.Error;


public class LocateFacade_1_1 extends LocateFacadeImpl<InRequest,Out,Endpoints,MgmtEndpoints,Configuration,Error>
{
    public LocateFacade_1_1(AuthzEnv env, LocateService<InRequest,Out,Endpoints,MgmtEndpoints,Configuration,Error> service, Data.TYPE type) throws APIException {
        super(env, service, type);
    }
}
