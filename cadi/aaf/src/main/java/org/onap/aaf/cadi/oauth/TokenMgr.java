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

package org.onap.aaf.cadi.oauth;

import java.io.IOException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import aaf.v2_0.Perms;
import aafoauth.v2_0.Introspect;

public class TokenMgr extends Persist<Introspect, TokenPerm> {
    protected static Map<String,TokenPerm> tpmap = new ConcurrentHashMap<>();
    protected static Map<String,TokenMgr> tmmap = new HashMap<>(); // synchronized in getInstance
    protected static Map<String,String> currentToken = new HashMap<>(); // synchronized in getTP
    public static RosettaDF<Perms> permsDF;
    public static RosettaDF<Introspect> introspectDF;

    private final TokenPermLoader tpLoader;

    private TokenMgr(PropAccess access, String tokenURL, String introspectURL) throws APIException, CadiException {
        super(access,new RosettaEnv(access.getProperties()),Introspect.class,"introspect");
        synchronized(access) {
            if (permsDF==null) {
                permsDF = env.newDataFactory(Perms.class);
                introspectDF = env.newDataFactory(Introspect.class);
            }
        }
        if ("dbToken".equals(tokenURL) && "dbIntrospect".equals(introspectURL)) {
            tpLoader = new TokenPermLoader() { // null Loader
                @Override
                public Result<TokenPerm> load(String accessToken, byte[] cred)
                        throws APIException, CadiException, LocatorException {
                    return Result.err(404, "DBLoader");
                }
            };
        } else {
            RemoteTokenPermLoader rtpl = new RemoteTokenPermLoader(tokenURL, introspectURL); // default is remote
            String i = access.getProperty(Config.AAF_APPID,null);
            String p = access.getProperty(Config.AAF_APPPASS, null);
            if (i==null || p==null) {
                throw new CadiException(Config.AAF_APPID + " and " + Config.AAF_APPPASS + " must be set to initialize TokenMgr");
            }
            rtpl.introCL.client_creds(i,p);
            tpLoader = rtpl;
        }
    }

    private TokenMgr(PropAccess access, TokenPermLoader tpl) throws APIException, CadiException {
        super(access,new RosettaEnv(access.getProperties()),Introspect.class,"incoming");
        synchronized(access) {
            if (permsDF==null) {
                permsDF = env.newDataFactory(Perms.class);
                introspectDF = env.newDataFactory(Introspect.class);
            }
        }
        tpLoader = tpl;
    }

    public static synchronized TokenMgr getInstance(final PropAccess access, final String tokenURL, final String introspectURL) throws APIException, CadiException {
        String key;
        TokenMgr tm = tmmap.get(key=tokenURL+'/'+introspectURL);
        if (tm==null) {
            tmmap.put(key, tm=new TokenMgr(access,tokenURL,introspectURL));
        }
        return tm;
    }

    public Result<OAuth2Principal> toPrincipal(final String accessToken, final byte[] hash) throws APIException, CadiException, LocatorException {
        Result<TokenPerm> tp = get(accessToken, hash, new Loader<TokenPerm>() {
            @Override
            public Result<TokenPerm> load(String key) throws APIException, CadiException, LocatorException {
                try {
                    return tpLoader.load(accessToken,hash);
                } catch (APIException | LocatorException e) {
                    throw new CadiException(e);
                }
            }
        });
        if (tp.isOK()) {
            return Result.ok(200, new OAuth2Principal(tp.value,hash));
        } else {
            return Result.err(tp);
        }
    }

    public Result<TokenPerm> get(final String accessToken, final byte[] hash) throws APIException, CadiException, LocatorException {
        return get(accessToken,hash,new Loader<TokenPerm>() {
            @Override
            public Result<TokenPerm> load(String key) throws APIException, CadiException, LocatorException {
                return tpLoader.load(key,hash);
            }

        });
//        return tpLoader.load(accessToken,hash);
    }

    public interface TokenPermLoader{
        public Result<TokenPerm> load(final String accessToken, final byte[] cred) throws APIException, CadiException, LocatorException;
    }

    private class RemoteTokenPermLoader implements TokenPermLoader {
        private TokenClientFactory tcf;
        private TokenClient tokenCL, introCL;

        public RemoteTokenPermLoader(final String tokenURL, final String introspectURL) throws APIException, CadiException {
            try {
                tcf = TokenClientFactory.instance(access);
                int timeout = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
                tokenCL = tcf.newClient(tokenURL,
                                        timeout);
                if (introspectURL.equals(tokenURL)) {
                    introCL = tokenCL;
                } else {
                    introCL = tcf.newClient(introspectURL,
                            timeout);
                }

            } catch (GeneralSecurityException | IOException | NumberFormatException | LocatorException e) {
                throw new CadiException(e);
            }
        }

        public Result<TokenPerm> load(final String accessToken, final byte[] cred) throws APIException, CadiException, LocatorException {
            long start = System.currentTimeMillis();
            try {
                Result<Introspect> ri = introCL.introspect(accessToken);
                if (ri.isOK()) {
                    return Result.ok(ri.code, new TokenPerm(TokenMgr.this,permsDF,ri.value,cred,getPath(accessToken)));
                } else {
                    return Result.err(ri);
                }
            } finally {
                access.printf(Level.INFO, "Token loaded in %d ms",System.currentTimeMillis()-start);
            }
        }
    }

    public void clear(Principal p, StringBuilder report) {
        TokenPerm tp = tpmap.remove(p.getName());
        if (tp==null) {
            report.append("Nothing to clear");
        } else {
            report.append("Cleared ");
            report.append(p.getName());
        }
    }

    @Override
    protected TokenPerm newCacheable(Introspect i, long expires, byte[] hash, Path path) throws APIException {
        // Note: Introspect drives the Expiration... ignoring expires.
        return new TokenPerm(this,permsDF,i,hash,path);
    }

    public TokenPerm putIntrospect(Introspect intro, byte[] cred) throws APIException {
        return newCacheable(intro, intro.getExp(), cred, getPath(intro.getAccessToken()));
    }

}
