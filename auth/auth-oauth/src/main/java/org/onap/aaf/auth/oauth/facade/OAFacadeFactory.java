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

package org.onap.aaf.auth.oauth.facade;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.oauth.AAF_OAuth;
import org.onap.aaf.auth.oauth.mapper.Mapper1_0;
import org.onap.aaf.auth.oauth.mapper.MapperIntrospect1_0;
import org.onap.aaf.auth.oauth.service.OAuthService;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;

import aafoauth.v2_0.Introspect;


public class OAFacadeFactory {
    public static OAFacade1_0 v1_0(AAF_OAuth certman, AuthzTrans trans, OAuthService service, Data.TYPE type) throws APIException {
        return new OAFacade1_0(
                certman,
                service,
                new Mapper1_0(),
                type);  
    }
   
    public static DirectIntrospect<Introspect> directV1_0(OAuthService service) {
        return new DirectIntrospectImpl<Introspect>(service, new MapperIntrospect1_0());
    }
}
