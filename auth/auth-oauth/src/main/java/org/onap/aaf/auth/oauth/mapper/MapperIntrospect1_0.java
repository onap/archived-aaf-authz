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

import java.util.Set;

import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.oauth.service.OAuthService.CLIENT_TYPE;

import aafoauth.v2_0.Introspect;

public class MapperIntrospect1_0 implements MapperIntrospect<Introspect> {

    public Result<Introspect> introspect(Result<Data> rs) {
        if (rs.isOKhasData()) {
            Data data = rs.value;
            Introspect ti = new Introspect();
            ti.setAccessToken(data.id);
            ti.setActive(data.active);
            ti.setClientId(data.client_id);
            for (CLIENT_TYPE ct : CLIENT_TYPE.values()) {
                if (data.type==ct.ordinal()) {
                    ti.setClientType(ct.name());
                    break;
                }
            }
            if (ti.getClientType()==null) {
                ti.setClientType(CLIENT_TYPE.unknown.name());
            }
            ti.setActive(data.active);
            ti.setScope(getScopes(data.scopes(false)));
            ti.setContent(data.content);
            ti.setUsername(data.user);
            ti.setExp(data.exp_sec); // want seconds from Jan 1, 1970
            return Result.ok(ti);
        }
        return Result.err(rs);
    }

    protected static String getScopes(Set<String> scopes) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : scopes) {
            if (start) {
                start = false;
            } else {
                sb.append(' ');
            }
            sb.append(s);
        }
        return sb.toString();
    }

}
