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

package org.onap.aaf.auth.oauth.facade;

import org.onap.aaf.auth.dao.cass.OAuthTokenDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.FacadeImpl;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.mapper.MapperIntrospect;
import org.onap.aaf.auth.oauth.service.OAuthService;

public class DirectIntrospectImpl<INTROSPECT> extends FacadeImpl implements DirectIntrospect<INTROSPECT> {
    protected OAuthService service;
    private MapperIntrospect<INTROSPECT> mapper;

    public DirectIntrospectImpl(OAuthService service, MapperIntrospect<INTROSPECT> mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.oauth.facade.OAFacade#mappedIntrospect(org.onap.aaf.auth.env.test.AuthzTrans, java.lang.String)
     */
    @Override
    public Result<INTROSPECT> mappedIntrospect(AuthzTrans trans, String token) {
        Result<INTROSPECT> rti;
         Result<OAuthTokenDAO.Data> rs = service.introspect(trans,token);
        if (rs.notOK()) {
            rti = Result.err(rs);
        } else if (rs.isEmpty()) {
            rti = Result.err(Result.ERR_NotFound,"No Token %s found",token);
        } else {
            rti = mapper.introspect(rs);
        }
        return rti;
    }

}
