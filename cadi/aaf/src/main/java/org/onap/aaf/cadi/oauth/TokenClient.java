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
import java.net.ConnectException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon.GetSetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.persist.Persist.Loader;
import org.onap.aaf.cadi.principal.Kind;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import aafoauth.v2_0.Introspect;
import aafoauth.v2_0.Token;

public class TokenClient {
    private static final String UTF_8 = "UTF-8";

    public enum AUTHN_METHOD {client_credentials,password,payload,basic_auth,certificate,refresh_token, none}

    private final TokenClientFactory factory;
    private final AAFCon<?> tkCon;
    private static RosettaDF<Token> tokenDF;
    protected static RosettaDF<Introspect> introspectDF;


    private int timeout;
    private String client_id, username;
    private byte[] enc_client_secret, enc_password;

    private GetSetter ss;
    private AUTHN_METHOD authn_method;
    private byte[] hash;
    private final char okind;
    private String default_scope;

    // Package on Purpose
    TokenClient(char okind, final TokenClientFactory tcf, final AAFCon<?> tkCon, final int timeout, AUTHN_METHOD am) throws CadiException, APIException {
        this.okind = okind;
        factory = tcf;
        this.tkCon = tkCon;
        this.timeout = timeout;
        ss = null;
        authn_method = am;
        synchronized(tcf) {
            if (introspectDF==null) {
                tokenDF = tkCon.env().newDataFactory(Token.class);
                introspectDF = tkCon.env().newDataFactory(Introspect.class);
            }
        }
 
    }

    public void client_id(String client_id) {
        this.client_id = client_id;
        default_scope = FQI.reverseDomain(client_id);
    }

    public String client_id() {
        return client_id;
    }

    /**
     * This scope based on client_id... the App configured for call
     * @return
     */
    public String defaultScope() {
        return default_scope;
    }

    public void client_creds(Access access) throws CadiException {
        if (okind=='A') {
            String alias = access.getProperty(Config.CADI_ALIAS, null);
            if (alias == null) {
                client_creds(access.getProperty(Config.AAF_APPID, null),access.getProperty(Config.AAF_APPPASS, null));
            } else {
                client_creds(alias,null);
            }
        } else {
            client_creds(access.getProperty(Config.AAF_ALT_CLIENT_ID, null),access.getProperty(Config.AAF_ALT_CLIENT_SECRET, null));
        }
    }

    /**
     * Note: OAuth2 provides for normal Authentication parameters when getting tokens.  Basic Auth is one such valid
     * way to get Credentials.  However, support is up to the OAuth2 Implementation
     *
     * This method is for setting an App's creds (client) to another App.
     *
     * @param client_id
     * @param client_secret
     * @throws IOException
     */
    public void client_creds(final String client_id, final String client_secret) throws CadiException {
        if (client_id==null) {
            throw new CadiException("client_creds:client_id is null");
        }
        this.client_id = client_id;
        default_scope = FQI.reverseDomain(client_id);

        if (client_secret!=null) {
            try {
                if (client_secret.startsWith("enc:")) {
                    final String temp = factory.access.decrypt(client_secret, false); // this is a more powerful, but non-thread-safe encryption
                    hash = Hash.hashSHA256(temp.getBytes());
                    this.enc_client_secret = factory.symm.encode(temp.getBytes());
                    ss = new GetSetter() {
                        @Override
                        public <CLIENT> SecuritySetter<CLIENT> get(AAFCon<CLIENT> con) throws CadiException {
                            return con.basicAuth(client_id, temp);// Base class encrypts password
                        }
                    };
                } else {
                    byte[] temp = client_secret.getBytes();
                    hash = Hash.hashSHA256(temp);
                    this.enc_client_secret = factory.symm.encode(temp);
                    ss = new GetSetter() {
                        @Override
                        public <CLIENT> SecuritySetter<CLIENT> get(AAFCon<CLIENT> con) throws CadiException {
                            return con.basicAuth(client_id, client_secret);// Base class encrypts password
                        }
                    };
                }
                authn_method = AUTHN_METHOD.client_credentials;
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new CadiException(e);
            }
        } else {
            ss = new GetSetter() {
                @Override
                public <CLIENT> SecuritySetter<CLIENT> get(AAFCon<CLIENT> con) throws CadiException {
                    try {
                        return con.x509Alias(client_id);// no password, assume Cert
                    } catch (APIException e) {
                        throw new CadiException(e);
                    } 
                }            
            };
            authn_method = AUTHN_METHOD.client_credentials;
        }
    }

    public void username(String username) {
        this.username = username;
    }

    /**
     * Note: OAuth2 provides for normal Authentication parameters when getting tokens.  Basic Auth is one such valid
     * way to get Credentials.  However, support is up to the OAuth2 Implementation
     *
     * This method is for setting the End-User's Creds
     *
     * @param client_id
     * @param client_secret
     * @throws IOException
     */
    public void password(final String user, final String password) throws CadiException {
        this.username = user;
        if (password!=null) {
            try {
                if (password.startsWith("enc:")) {
                    final String temp = factory.access.decrypt(password, false); // this is a more powerful, but non-thread-safe encryption
                    hash = Hash.hashSHA256(temp.getBytes());
                    this.enc_password = factory.symm.encode(temp.getBytes());
                    ss = new GetSetter() {
                        @Override
                        public <CLIENT> SecuritySetter<CLIENT> get(AAFCon<CLIENT> con) throws CadiException {
                            return con.basicAuth(user, temp);// Base class encrypts password
                        }
                    };
                } else {
                    byte[] temp = password.getBytes();
                    hash = Hash.hashSHA256(temp);
                    this.enc_password = factory.symm.encode(temp);
                    ss = new GetSetter() {
                        @Override
                        public <CLIENT> SecuritySetter<CLIENT> get(AAFCon<CLIENT> con) throws CadiException {
                            return con.basicAuth(user, password);// Base class encrypts password
                        }
                    };
                }
                authn_method = AUTHN_METHOD.password;
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new CadiException(e);
            }
        }
    }

    public void clearEndUser() {
        username = null;
        enc_password = null;
        if (client_id!=null && enc_client_secret!=null) {
            authn_method = AUTHN_METHOD.client_credentials;
        } else {
            authn_method = AUTHN_METHOD.password;
        }
    }

    public Result<TimedToken> getToken(final String ... scopes) throws LocatorException, CadiException, APIException {
        return getToken(Kind.OAUTH,scopes);
    }

    public void clearToken(final String ... scopes) throws CadiException {
        clearToken(Kind.OAUTH,scopes);
    }

    public void clearToken(final char kind, final String ... scopes) throws CadiException {
        final String scope = addScope(scopes);
        char c;
        if (kind==Kind.OAUTH) {
            c = okind;
        } else {
            c = kind;
        }
        final String key = TokenClientFactory.getKey(c,client_id,username,hash,scope);
        factory.delete(key);
    }
    /**
     * Get AuthToken
     * @throws APIException 
     * @throws CadiException 
     * @throws LocatorException 
     */
    public Result<TimedToken> getToken(final char kind, final String ... scopes) throws LocatorException, CadiException, APIException {
        final String scope = addScope(scopes);
        char c;
        if (kind==Kind.OAUTH) {
            c = okind;
        } else {
            c = kind;
        }
        final String key = TokenClientFactory.getKey(c,client_id,username,hash,scope);
        if (ss==null) {
            throw new APIException("client_creds(...) must be set before obtaining Access Tokens");
        }
    
        Result<TimedToken> rtt = factory.get(key,hash,new Loader<TimedToken>() {
            @Override
            public Result<TimedToken> load(final String key) throws APIException, CadiException, LocatorException {
                final List<String> params = new ArrayList<>();
                params.add(scope);
                addSecurity(params,authn_method);
        
                final String paramsa[] = new String[params.size()];
                params.toArray(paramsa);
                Result<Token> rt = tkCon.best(new Retryable<Result<Token>>() {
                    @Override
                    public Result<Token> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                        // /token?grant_type=client_credential&scope=com.att.aaf+com.att.test
                        Future<Token> f = client.postForm(null,tokenDF,paramsa);
                        if (f.get(timeout)) {
                            return Result.ok(f.code(),f.value);
                        } else {
                            return Result.err(f.code(), f.body());
                        }
                    }
                });
            
                if (rt.isOK()) {
                    try {
                        return Result.ok(rt.code,factory.putTimedToken(key,rt.value, hash));
                    } catch (IOException e) {
                        // TODO What to do here?
                        e.printStackTrace();
                        return Result.err(999,e.getMessage());
                    }
                } else {
                    return Result.err(rt);
                }
            }
        });
        if (rtt.isOK()) { // not validated for Expired
            TimedToken tt = rtt.value;
            if (tt.expired()) {
                rtt = refreshToken(tt);
                if (rtt.isOK()) {
                    tkCon.access.printf(Level.INFO, "Refreshed token %s to %s",tt.getAccessToken(),rtt.value.getAccessToken());
                    return Result.ok(200,rtt.value);
                } else {
                    tkCon.access.printf(Level.INFO, "Expired token %s cannot be renewed %d %s",tt.getAccessToken(),rtt.code,rtt.error);
                    factory.delete(key);
                    tt=null;
                }
            } else {
                return Result.ok(200,tt);
            }
        } else {
            Result.err(rtt);
        }
        return Result.err(404,"Not Found");
    }

    public Result<TimedToken> refreshToken(Token token) throws APIException, LocatorException, CadiException {
        if (ss==null) {
            throw new APIException("client_creds(...) must be set before obtaining Access Tokens");
        }
        final List<String> params = new ArrayList<>();
        params.add("refresh_token="+token.getRefreshToken());
        addSecurity(params,AUTHN_METHOD.refresh_token);
        final String scope="scope="+token.getScope().replace(' ', '+');
        params.add(scope);

        final String paramsa[] = new String[params.size()];
        params.toArray(paramsa);
        Result<Token> rt = tkCon.best(new Retryable<Result<Token>>() {
            @Override
            public Result<Token> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                // /token?grant_type=client_credential&scope=com.att.aaf+com.att.test
                Future<Token> f = client.postForm(null,tokenDF,paramsa);
                if (f.get(timeout)) {
                    return Result.ok(f.code(),f.value);
                } else {
                    return Result.err(f.code(), f.body());
                }
            }
        });
        String key =  TokenClientFactory.getKey(okind,client_id, username, hash, scope);
        if (rt.isOK()) {
            try {
                return Result.ok(200,factory.putTimedToken(key, rt.value, hash));
            } catch (IOException e) {
                //TODO what to do here?
                return Result.err(999, e.getMessage());
            }
        } else if (rt.code==404) {
            factory.deleteFromDisk(key);
        }
        return Result.err(rt);
    }

    public Result<Introspect> introspect(final String token) throws APIException, LocatorException, CadiException {
        if (ss==null) {
            throw new APIException("client_creds(...) must be set before introspecting Access Tokens");
        }

        return tkCon.best(new Retryable<Result<Introspect>>() {
                @Override
                public Result<Introspect> code(Rcli<?> client) throws CadiException, ConnectException, APIException {
                    final List<String> params = new ArrayList<>();
                    params.add("token="+token);
                    addSecurity(params,AUTHN_METHOD.client_credentials);
                    final String paramsa[] = new String[params.size()];
                    params.toArray(paramsa);
                    // /token?grant_type=client_credential&scope=com.att.aaf+com.att.test
                    Future<Introspect> f = client.postForm(null,introspectDF,paramsa);
                    if (f.get(timeout)) {
                        return Result.ok(f.code(),f.value);
                    } else {
                        return Result.err(f.code(), f.body());
                    }
                }
            }
        );
    }

    private String addScope(String[] scopes) {
        String rv = null;
        StringBuilder scope=null;
        boolean first = true;
        for (String s : scopes) {
            if (first) {
                scope = new StringBuilder();
                scope.append("scope=");
                first=false;
            } else {
                scope.append('+');
            }
            scope.append(s);
        }
        if (scope!=null) {
            rv=scope.toString();
        }
        return rv;
    }

    private void addSecurity(List<String> params, AUTHN_METHOD authn) throws APIException {
        // Set GrantType... different than Credentials
        switch(authn) {
            case client_credentials:
                params.add("grant_type=client_credentials");
                break;
            case password:
                params.add("grant_type=password");
                break;
            case refresh_token:
                params.add("grant_type=refresh_token");
                break;
            case none:
                break;
            default:
                // Nothing to do
                break;
        }
    
        // Set Credentials appropriate 
        switch(authn_method) {
            case client_credentials:
                if (client_id!=null) {
                    params.add("client_id="+client_id);
                }
    
                if (enc_client_secret!=null) {
                    try {
                        params.add("client_secret="+URLEncoder.encode(new String(factory.symm.decode(enc_client_secret)),UTF_8));
                    } catch (IOException e) {
                        throw new APIException("Error Decrypting Password",e);
                    }
                }
            
                if (username!=null) {
                    params.add("username="+username);
                }

                break;
            case refresh_token:
                if (client_id!=null) {
                    params.add("client_id="+client_id);
                }
    
                if (enc_client_secret!=null) {
                    try {
                        params.add("client_secret="+URLEncoder.encode(new String(factory.symm.decode(enc_client_secret)),UTF_8));
                    } catch (IOException e) {
                        throw new APIException("Error Decrypting Password",e);
                    }
                }
                break;

            case password:
                if (client_id!=null) {
                    params.add("client_id="+client_id);
                }
    
                if (enc_client_secret!=null) {
                    try {
                        params.add("client_secret="+ URLEncoder.encode(new String(factory.symm.decode(enc_client_secret)),UTF_8));
                    } catch (IOException e) {
                        throw new APIException("Error Decrypting Password",e);
                    }
                }
                if (username!=null) {
                    params.add("username="+username);
                }
    
                if (enc_password!=null) {
                    try {
                        params.add("password="+ URLEncoder.encode(new String(factory.symm.decode(enc_password)),UTF_8));
                    } catch (IOException e) {
                        throw new APIException("Error Decrypting Password",e);
                    }
                }

                break;
            default:
                // Nothing to do
                break;
        }
    }
}
