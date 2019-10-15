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

import org.onap.aaf.auth.oauth.AAF_OAuth;
import org.onap.aaf.auth.oauth.mapper.Mapper;
import org.onap.aaf.auth.oauth.service.OAuthService;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;

import aaf.v2_0.Error;
import aafoauth.v2_0.Introspect;
import aafoauth.v2_0.Token;
import aafoauth.v2_0.TokenRequest;

/**
 * @author Jonathan
 *
 */
public class OAFacade1_0 extends OAFacadeImpl<TokenRequest,Token,Introspect,Error> {
    public OAFacade1_0(AAF_OAuth api,
                     OAuthService service,
                     Mapper<TokenRequest,Token,Introspect,Error> mapper,
                     Data.TYPE type) throws APIException {
        super(api, service, mapper, type);
    }

}
