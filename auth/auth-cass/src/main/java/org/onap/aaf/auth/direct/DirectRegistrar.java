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

package org.onap.aaf.auth.direct;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.util.Split;

public class DirectRegistrar implements Registrant<AuthzEnv> {
    private Data locate;
    private LocateDAO ldao;
    public DirectRegistrar(Access access, LocateDAO ldao, String name, String version, int port) throws CadiException {
        this.ldao = ldao;
        locate = new LocateDAO.Data();
        locate.name = name;
        locate.port = port;
        
        try {
            String latitude = access.getProperty(Config.CADI_LATITUDE, null);
            if (latitude==null) {
                latitude = access.getProperty("AFT_LATITUDE", null);
            }
            String longitude = access.getProperty(Config.CADI_LONGITUDE, null);
            if (longitude==null) {
                longitude = access.getProperty("AFT_LONGITUDE", null);
            }
            if (latitude==null || longitude==null) {
                throw new CadiException(Config.CADI_LATITUDE + " and " + Config.CADI_LONGITUDE + " is required");
            } else {
                locate.latitude = Float.parseFloat(latitude);
                locate.longitude = Float.parseFloat(longitude);
            }
            String split[] = Split.splitTrim('.', version);
            locate.pkg = split.length>3?Integer.parseInt(split[3]):0;
            locate.patch = split.length>2?Integer.parseInt(split[2]):0;
            locate.minor = split.length>1?Integer.parseInt(split[1]):0;
            locate.major = split.length>0?Integer.parseInt(split[0]):0;
            locate.hostname = access.getProperty(Config.AAF_REGISTER_AS, null);
            if (locate.hostname==null) {
                locate.hostname = access.getProperty(Config.HOSTNAME, null);
            }
            if (locate.hostname==null) {
                locate.hostname = Inet4Address.getLocalHost().getHostName();
            }
            String subprotocols = access.getProperty(Config.CADI_PROTOCOLS, null);
            if (subprotocols==null) {
                locate.protocol="http";
            } else {
                locate.protocol="https";
                for (String s : Split.split(',', subprotocols)) {
                    locate.subprotocol(true).add(s);
                }
            }
        } catch (NumberFormatException | UnknownHostException e) {
            throw new CadiException("Error extracting Data from Properties for Registrar",e);
        }
    }
    
    @Override
    public Result<Void> update(AuthzEnv env) {
        org.onap.aaf.auth.layer.Result<Void> dr = ldao.update(env.newTransNoAvg(), locate);
        if (dr.isOK()) {
            return Result.ok(200, null);
        } else {
            return Result.err(503, dr.errorString());
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.server.Registrant#cancel(org.onap.aaf.auth.env.test.AuthzEnv)
     */
    @Override
    public Result<Void> cancel(AuthzEnv env) {
        org.onap.aaf.auth.layer.Result<Void> dr = ldao.delete(env.newTransNoAvg(), locate, false);
        if (dr.isOK()) {
            return Result.ok(200, null);
        } else {
            return Result.err(503, dr.errorString());
        }

    }

}
