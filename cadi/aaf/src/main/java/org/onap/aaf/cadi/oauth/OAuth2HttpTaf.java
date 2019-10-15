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

package org.onap.aaf.cadi.oauth;

import java.security.NoSuchAlgorithmException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CachedPrincipal.Resp;
import org.onap.aaf.cadi.Taf.LifeForm;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.taf.HttpTaf;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TafResp.RESP;
import org.onap.aaf.misc.env.APIException;

public class OAuth2HttpTaf implements HttpTaf {
    final private Access access;
    final private TokenMgr tmgr;

    public OAuth2HttpTaf(final Access access, final TokenMgr tmgr) {
        this.tmgr = tmgr;
        this.access = access;
    }

    @Override
    public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
        String authz = req.getHeader("Authorization");
        if (authz != null && authz.length()>7 && authz.startsWith("Bearer ")) {
            if (!req.isSecure()) {
                access.log(Level.WARN,"WARNING! OAuth has been used over an insecure channel");
            }
            try {
                String tkn = authz.substring(7);
                Result<OAuth2Principal> rp = tmgr.toPrincipal(tkn,Hash.hashSHA256(tkn.getBytes()));
                if (rp.isOK()) {
                    return new OAuth2HttpTafResp(access,rp.value,rp.value.getName()+" authenticated by Bearer Token",RESP.IS_AUTHENTICATED,resp,false);
                } else {
                    return new OAuth2HttpTafResp(access,null,rp.error,RESP.FAIL,resp,true);
                }
            } catch (APIException | CadiException | LocatorException e) {
                return new OAuth2HttpTafResp(access,null,"Bearer Token invalid",RESP.FAIL,resp,true);
            } catch (NoSuchAlgorithmException e) {
                return new OAuth2HttpTafResp(access,null,"Security Algorithm not available",RESP.FAIL,resp,true);
            }
        }
        return new OAuth2HttpTafResp(access,null,"No OAuth2 ",RESP.TRY_ANOTHER_TAF,resp,true);
    }

    @Override
    public Resp revalidate(CachedPrincipal prin,Object state) {
        //TODO!!!!
        return null;
    }

}
