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

package org.onap.aaf.auth.oauth.mapper;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.dao.cass.OAuthTokenDAO;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.service.OCreds;
import org.onap.aaf.auth.oauth.service.OAuthService.GRANT_TYPE;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.oauth.OAuth2Principal;

public interface Mapper<TOKEN_REQ,TOKEN,INTROSPECT,ERROR> extends MapperIntrospect<INTROSPECT>
{
    public enum API{TOKEN_REQ, TOKEN,INTROSPECT, ERROR,VOID};

    public Class<?> getClass(API api);
    public<A> A newInstance(API api);

    public ERROR errorFromMessage(StringBuilder holder, String msgID, String text, Object ... detail);
    public TOKEN_REQ tokenReqFromParams(HttpServletRequest req);
    public OCreds credsFromReq(TOKEN_REQ tokReq);

    public OAuthTokenDAO.Data clientTokenReq(TOKEN_REQ tokReq, Holder<GRANT_TYPE> hgt);
    public Result<TOKEN> tokenFromData(Result<OAuthTokenDAO.Data> rs);
    public INTROSPECT fromPrincipal(OAuth2Principal p);
}
