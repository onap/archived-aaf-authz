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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.configure.Agent;
import org.onap.aaf.cadi.locator.PropertyLocator;
import org.onap.aaf.cadi.locator.SingleEndpointLocator;
import org.onap.aaf.cadi.oauth.TokenClient.AUTHN_METHOD;
import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.cadi.principal.Kind;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import aafoauth.v2_0.Token;

public class TokenClientFactory extends Persist<Token,TimedToken> {
    private static TokenClientFactory instance;
    private Map<String,AAFConHttp> aafcons = new ConcurrentHashMap<>();
    private SecurityInfoC<HttpURLConnection> hsi;
    // Package on purpose
    final Symm symm;

    private TokenClientFactory(Access pa) throws APIException, GeneralSecurityException, IOException, CadiException {
        super(pa, new RosettaEnv(pa.getProperties()),Token.class,"outgoing");
        Map<String, String> aaf_urls = Agent.loadURLs(pa);
        if (access.getProperty(Config.AAF_OAUTH2_TOKEN_URL,null)==null) {
            access.getProperties().put(Config.AAF_OAUTH2_TOKEN_URL, aaf_urls.get(Config.AAF_OAUTH2_TOKEN_URL)); // Default to AAF
        }
        if (access.getProperty(Config.AAF_OAUTH2_INTROSPECT_URL,null)==null) {
            access.getProperties().put(Config.AAF_OAUTH2_INTROSPECT_URL, aaf_urls.get(Config.AAF_OAUTH2_INTROSPECT_URL)); // Default to AAF);
        }

        symm = Symm.encrypt.obtain();
        hsi = SecurityInfoC.instance(access, HttpURLConnection.class);
    }
    
    public synchronized static final TokenClientFactory instance(Access access) throws APIException, GeneralSecurityException, IOException, CadiException {
        if (instance==null) {
            instance = new TokenClientFactory(access);
        }
        return instance;
    }
    
    /**
     * Pickup Timeout from Properties
     * 
     * @param tagOrURL
     * @return
     * @throws CadiException
     * @throws LocatorException
     * @throws APIException
     */
    public<INTR> TokenClient newClient(final String tagOrURL) throws CadiException, LocatorException, APIException {
        return newClient(tagOrURL,Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF)));
    }
    
    public<INTR> TokenClient newClient(final String tagOrURL, final int timeout) throws CadiException, LocatorException, APIException {
        AAFConHttp ach;
        if (tagOrURL==null) {
            throw new CadiException("parameter tagOrURL cannot be null.");
        } else {
            ach = aafcons.get(tagOrURL);
            if (ach==null) {
                aafcons.put(tagOrURL, ach=new AAFConHttp(access,tagOrURL));
            }
        }
        char okind;
        if ( Config.AAF_OAUTH2_TOKEN_URL.equals(tagOrURL) ||
            Config.AAF_OAUTH2_INTROSPECT_URL.equals(tagOrURL) ||
            tagOrURL.equals(access.getProperty(Config.AAF_OAUTH2_TOKEN_URL, null)) ||
            tagOrURL.equals(access.getProperty(Config.AAF_OAUTH2_INTROSPECT_URL, null))
            ) {
                okind = Kind.AAF_OAUTH;
            } else {
                okind = Kind.OAUTH;
            }
        TokenClient tci = new TokenClient(
                okind,
                this,
                ach,
                timeout,
                AUTHN_METHOD.none);
        tci.client_creds(access);
        return tci;
    }
    
    public TzClient newTzClient(final String locatorURL) throws CadiException, LocatorException {
        try {
            return new TzHClient(access,hsi,bestLocator(locatorURL));
        } catch (URISyntaxException e) {
            throw new LocatorException(e);
        }
    }

    static String getKey(char tokenSource,String client_id, String username, byte[] hash, String scope) throws CadiException {
        try {
            StringBuilder sb = new StringBuilder(client_id);
            sb.append('_');
            if (username!=null) {
                sb.append(username);
            }
            sb.append('_');
            sb.append(tokenSource);
            if (scope!=null) {
                byte[] tohash=scope.getBytes();
                if (hash!=null && hash.length>0) {
                    byte temp[] = new byte[hash.length+tohash.length];
                    System.arraycopy(tohash, 0, temp, 0, tohash.length);
                    System.arraycopy(hash, 0, temp, tohash.length, hash.length);
                    tohash = temp;
                }
                if (scope.length()>0) {
                    sb.append(Hash.toHexNo0x(Hash.hashSHA256(tohash)));
                }
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CadiException(e);
        }
    }

    @Override
    protected TimedToken newCacheable(Token t, long expires, byte[] hash, Path path) throws IOException {
        return new TimedToken(this,t,expires,hash,path);
    }

    public TimedToken putTimedToken(String key, Token token, byte[] hash) throws IOException, CadiException {
        TimedToken tt = new TimedToken(this,token,token.getExpiresIn()+(System.currentTimeMillis()/1000),hash,getPath(key));
        put(key,tt);
        return tt;
    }
    
    private static final Pattern locatePattern = Pattern.compile("https://.*/locate/.*");
    public Locator<URI> bestLocator(final String locatorURL ) throws LocatorException, URISyntaxException {
        if (locatorURL==null) {
            throw new LocatorException("Cannot have a null locatorURL in bestLocator");
        }
        if (locatorURL.startsWith("https://AAF_LOCATE_URL/") || locatePattern.matcher(locatorURL).matches()) {
            return new AAFLocator(hsi,new URI(locatorURL));
        } else if (locatorURL.indexOf(',')>0) { // multiple URLs is a Property Locator
            return new PropertyLocator(locatorURL);
        } else {
            return new SingleEndpointLocator(locatorURL);
        }
        // Note: Removed DME2Locator... If DME2 client is needed, use DME2Clients
    }
}
