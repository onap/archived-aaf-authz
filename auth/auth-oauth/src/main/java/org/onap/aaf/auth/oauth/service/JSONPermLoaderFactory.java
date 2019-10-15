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

package org.onap.aaf.auth.oauth.service;

import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

public class JSONPermLoaderFactory {
    /**
     * Load JSON Perms from AAF Service (Remotely)
     * @param aafcon
     * @param timeout
     * @return
     */
    public static JSONPermLoader remote(final AAFCon<?> aafcon, final int timeout) {
        return new JSONPermLoader() {
            public Result<String> loadJSONPerms(AuthzTrans trans, String user, Set<String> scopes) throws APIException, CadiException {
                Rcli<?> c = aafcon.clientAs(Config.AAF_DEFAULT_API_VERSION,trans.getUserPrincipal());
                StringBuilder pathinfo = new StringBuilder("/authz/perms/user/");
                pathinfo.append(user);
                pathinfo.append("?scopes=");
                boolean first = true;
                for (String s : scopes) {
                    if (first) {
                        first = false;
                    } else {
                        pathinfo.append(':');
                    }
                    pathinfo.append(s);
                }
                TimeTaken tt = trans.start("Call AAF Service", Env.REMOTE);
                try {
                    Future<String> fs = c.read(pathinfo.toString(), "application/Perms+json;charset=utf-8;version=2.0");
                    if (fs.get(timeout)) {
                        return Result.ok(fs.body());
                    } else if (fs.code()==404) {
                        return Result.err(Result.ERR_NotFound,fs.body());
                    } else {
                        return Result.err(Result.ERR_Backend,"Error accessing AAF %s: %s",Integer.toString(fs.code()),fs.body());
                    }
                } finally {
                    tt.done();
                }
            }
        };
    }
    public static JSONPermLoader direct(final Question question) {
        return new JSONPermLoader() {
            public Result<String> loadJSONPerms(AuthzTrans trans, String user, Set<String> scopes) throws APIException, CadiException {
                TimeTaken tt = trans.start("Cached DB Perm lookup", Env.SUB);
                Result<List<PermDAO.Data>> pd;
                try {
                    pd = question.getPermsByUser(trans, user, false);
                } finally {
                    tt.done();
                }
                if (pd.notOK()) {
                    return Result.err(pd);
                }
                // Since we know it is 
                StringBuilder sb = new StringBuilder("{\"perm\":[");
                boolean first = true;
                for (PermDAO.Data d : pd.value) {
                    if (scopes.contains(d.ns)) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(',');
                        }
                        sb.append("{\"ns\":\"");
                        sb.append(d.ns);
                        sb.append("\",\"type\":\"");
                        sb.append(d.type);
                        sb.append("\",\"instance\":\"");
                        sb.append(d.instance);
                        sb.append("\",\"action\":\"");
                        sb.append(d.action);
                        sb.append("\"}");
                    }
                }
                sb.append("]}");
                return Result.ok(sb.toString());
            }
        };
    }

}
