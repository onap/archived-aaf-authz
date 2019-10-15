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

package org.onap.aaf.auth.direct;

import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.register.RegistrationCreator;

import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;

public class DirectRegistrar implements Registrant<AuthzEnv> {

    private LocateDAO ldao;
    private List<LocateDAO.Data> ldd; 
    public DirectRegistrar(Access access, LocateDAO ldao, int port) throws CadiException {
        this.ldao = ldao;
        ldd = new ArrayList<>();
        RegistrationCreator rc = new RegistrationCreator(access);
        MgmtEndpoints mes = rc.create(port);
        for(MgmtEndpoint me : mes.getMgmtEndpoint()) {
            ldd.add(convert(me));
        }
    }

    private LocateDAO.Data convert(MgmtEndpoint me) {
        LocateDAO.Data out = new LocateDAO.Data();
        out.name=me.getName();
        out.hostname=me.getHostname();
        out.latitude=me.getLatitude();
        out.longitude=me.getLongitude();
        out.major=me.getMajor();
        out.minor=me.getMinor();
        out.pkg=me.getPkg();
        out.patch=me.getPatch();
        out.port=me.getPort();
        out.protocol=me.getProtocol();
        out.subprotocol(true).addAll(me.getSubprotocol());
//        out.port_key = UUID.randomUUID();
        return out;
    }

    @Override

    public Result<Void> update(AuthzEnv env) {
        AuthzTrans trans = env.newTransNoAvg(); 
        StringBuilder sb = null;
        for(LocateDAO.Data ld : ldd) {
            org.onap.aaf.auth.layer.Result<Void> dr = ldao.update(trans, ld);
            if (dr.notOK()) {
                if(sb == null) {
                    sb = new StringBuilder(dr.errorString());
                } else {
                    sb.append(';');
                    sb.append(dr.errorString());
                }
            }
        }
    
        if(sb==null) {
            return Result.ok(200, null);
        } else {
            return Result.err(503, sb.toString());
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.server.Registrant#cancel(org.onap.aaf.auth.env.test.AuthzEnv)
     */
    @Override
    public Result<Void> cancel(AuthzEnv env) {
        AuthzTrans trans = env.newTransNoAvg(); 
        StringBuilder sb = null;
        for(LocateDAO.Data ld : ldd) {
            org.onap.aaf.auth.layer.Result<Void> dr = ldao.delete(trans, ld, false);
            if (dr.notOK()) {
                if(sb == null) {
                    sb = new StringBuilder(dr.errorString());
                } else {
                    sb.append(';');
                    sb.append(dr.errorString());
                }
            }
        }
    
        if(sb==null) {
            return Result.ok(200, null);
        } else {
            return Result.err(503, sb.toString());
        }
    }

}
