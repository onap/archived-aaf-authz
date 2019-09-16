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

package org.onap.aaf.cadi.aaf.v2_0;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.CadiWrap;
import org.onap.aaf.cadi.Connector;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.aaf.marshal.CertsMarshal;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.RegistrationPropHolder;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.lur.EpiLur;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.util.FQI;
import org.onap.aaf.cadi.util.Vars;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import aaf.v2_0.Certs;
import aaf.v2_0.CredRequest;
import aaf.v2_0.Error;
import aaf.v2_0.Perms;
import aaf.v2_0.Users;

public abstract class AAFCon<CLIENT> implements Connector {
    final public Access access;
    // Package access
    final public int timeout, cleanInterval, connTimeout;
    final public int highCount, userExpires, usageRefreshTriggerCount;
    private Map<String,Rcli<CLIENT>> clients = new ConcurrentHashMap<>();
    final public RosettaDF<Perms> permsDF;
    final public RosettaDF<Certs> certsDF;
    final public RosettaDF<Users> usersDF;
    final public RosettaDF<CredRequest> credReqDF;
    final public RosettaDF<Error> errDF;
    private String realm;
    public final String app;
    protected final String apiVersion;
    protected SecurityInfoC<CLIENT> si;

    private AAFLurPerm lur;

    final public RosettaEnv env;
    protected AAFCon(AAFCon<CLIENT> copy) {
        access = copy.access;
        apiVersion = access.getProperty(Config.AAF_API_VERSION, Config.AAF_DEFAULT_API_VERSION);
        timeout = copy.timeout;
        cleanInterval = copy.cleanInterval;
        connTimeout = copy.connTimeout;
        highCount = copy.highCount;
        userExpires = copy.userExpires;
        usageRefreshTriggerCount = copy.usageRefreshTriggerCount;
        permsDF = copy.permsDF;
        certsDF = copy.certsDF;
        usersDF = copy.usersDF;
        credReqDF = copy.credReqDF;
        errDF = copy.errDF;
        app = copy.app;
        si = copy.si;
        env = copy.env;
        realm = copy.realm;
    }
    protected AAFCon(Access access, String tag, SecurityInfoC<CLIENT> si) throws CadiException{
        apiVersion = access.getProperty(Config.AAF_API_VERSION, Config.AAF_DEFAULT_API_VERSION);
        if (tag==null) {
            throw new CadiException("AAFCon cannot be constructed without a property tag or URL");
        } else {
            String str = access.getProperty(tag,null);
            if (str==null) {
                if (tag.contains("://")) { // assume a URL
                    str = tag;
                } else {
                    throw new CadiException("A URL or " + tag + " property is required.");
                }
            }
            try {
                RegistrationPropHolder rph = new RegistrationPropHolder(access, 0);
                str = rph.replacements("AAFCon",str, null,null);
            } catch (UnknownHostException e) {
                throw new CadiException(e);
            }
            access.printf(Level.INFO, "AAFCon has URL of %s",str);
            setInitURI(str);
        }
        try {
            this.access = access;
            this.si = si;
            if (si.defSS.getID().equals(SecurityInfoC.DEF_ID)) { // it's the Preliminary SS, try to get a better one
                String mechid = access.getProperty(Config.AAF_APPID, null);
                if (mechid==null) {
                    mechid=access.getProperty(Config.OAUTH_CLIENT_ID,null);
                }
                String alias = access.getProperty(Config.CADI_ALIAS, null);
                if(alias != null) {
                    si.defSS=x509Alias(alias);
                    set(si.defSS);
                } else {
    
                    String encpass = access.getProperty(Config.AAF_APPPASS, null);
                    if (encpass==null) {
                        encpass = access.getProperty(Config.OAUTH_CLIENT_SECRET,null);
                    }
                    
                    if (encpass==null) {
                        if (alias==null) {
                            access.printf(Access.Level.WARN,"%s, %s or %s required before use.", Config.CADI_ALIAS, Config.AAF_APPID, Config.OAUTH_CLIENT_ID);
                            set(si.defSS);
                        }
                    } else {
                        if (mechid!=null) {
                            si.defSS=basicAuth(mechid, encpass);
                            set(si.defSS);
                        } else {
                            si.defSS=new SecuritySetter<CLIENT>() {
        
                                @Override
                                public String getID() {
                                    return "";
                                }
        
                                @Override
                                public void setSecurity(CLIENT client) throws CadiException {
                                    throw new CadiException("AAFCon has not been initialized with Credentials (SecuritySetter)");
                                }
        
                                @Override
                                public int setLastResponse(int respCode) {
                                    return 0;
                                }
                            };
                            set(si.defSS);
                        }
                    }
                }
            }
            
            timeout = Integer.parseInt(access.getProperty(Config.AAF_CALL_TIMEOUT, Config.AAF_CALL_TIMEOUT_DEF));
            cleanInterval = Integer.parseInt(access.getProperty(Config.AAF_CLEAN_INTERVAL, Config.AAF_CLEAN_INTERVAL_DEF));
            highCount = Integer.parseInt(access.getProperty(Config.AAF_HIGH_COUNT, Config.AAF_HIGH_COUNT_DEF).trim());
            connTimeout = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF).trim());
            userExpires = Integer.parseInt(access.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF).trim());
            usageRefreshTriggerCount = Integer.parseInt(access.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF).trim())-1; // zero based
    
            app=FQI.reverseDomain(si.defSS.getID());
            //TODO Get Realm from AAF
            realm="people.osaaf.org";
    
            env = new RosettaEnv();
            permsDF = env.newDataFactory(Perms.class);
            usersDF = env.newDataFactory(Users.class);
            certsDF = env.newDataFactory(Certs.class);
            certsDF.rootMarshal(new CertsMarshal()); // Speedier Marshaling
            credReqDF = env.newDataFactory(CredRequest.class);
            errDF = env.newDataFactory(Error.class);
        } catch (APIException e) {
            throw new CadiException("AAFCon cannot be configured",e);
        }
    }
    protected abstract URI initURI();
    protected abstract void setInitURI(String uriString) throws CadiException;

    public final String aafVersion() {
        return apiVersion;
    }
    
    /**
     * Use this call to get the appropriate client based on configuration (HTTP, future)
     * using default AAF API Version
     * 
     * @param apiVersion
     * @return
     * @throws CadiException
     */
    public Rcli<CLIENT> client() throws CadiException {
        return client(apiVersion);
    }        

    /**
     * Use this call to get the appropriate client based on configuration (HTTP, future)
     * 
     * @param apiVersion
     * @return
     * @throws CadiException
     */
    public Rcli<CLIENT> client(final String apiVersion) throws CadiException {
        Rcli<CLIENT> client = clients.get(apiVersion);
        if (client==null) {
            client = rclient(initURI(),si.defSS);
            client.apiVersion(apiVersion)
                  .readTimeout(connTimeout);
            clients.put(apiVersion, client);
        } 
        return client;
    }

    public Rcli<CLIENT> client(URI uri) throws CadiException {
        return rclient(uri,si.defSS).readTimeout(connTimeout);
    }
    
    /**
     * Use this API when you have permission to have your call act as the end client's ID.
     * 
     *  Your calls will get 403 errors if you do not have this permission.  it is a special setup, rarely given.
     * 
     * @param apiVersion
     * @param req
     * @return
     * @throws CadiException
     */
    public Rcli<CLIENT> clientAs(TaggedPrincipal p) throws CadiException {
       return clientAs(apiVersion,p);
    }
    
    /**
     * Use this API when you have permission to have your call act as the end client's ID.
     * 
     *  Your calls will get 403 errors if you do not have this permission.  it is a special setup, rarely given.
     * 
     * @param apiVersion
     * @param req
     * @return
     * @throws CadiException
     */
    public Rcli<CLIENT> clientAs(String apiVersion, TaggedPrincipal p) throws CadiException {
        Rcli<CLIENT> cl = client(apiVersion);
        return cl.forUser(transferSS(p));
    }

    
    public RosettaEnv env() {
        return env;
    }
    
    /**
     * Return the backing AAFCon, if there is a Lur Setup that is AAF.
     * 
     * If there is no AAFLur setup, it will return "null"
     * @param servletRequest
     * @return
     */
    public static final AAFCon<?> obtain(Object servletRequest) {
        if (servletRequest instanceof CadiWrap) {
            Lur lur = ((CadiWrap)servletRequest).getLur();
            if (lur != null) {
                if (lur instanceof EpiLur) {
                    AbsAAFLur<?> aal = (AbsAAFLur<?>) ((EpiLur)lur).subLur(AbsAAFLur.class);
                    if (aal!=null) {
                        return aal.aaf;
                    }
                } else {
                    if (lur instanceof AbsAAFLur) {
                        return ((AbsAAFLur<?>)lur).aaf;
                    }
                }
            }
        }
        return null;
    }
    
    public abstract AAFCon<CLIENT> clone(String url) throws CadiException, LocatorException;
    
    public AAFAuthn<CLIENT> newAuthn() throws APIException {
        try {
            return new AAFAuthn<>(this);
        } catch (Exception e) {
            throw new APIException(e);
        }
    }

    public AAFAuthn<CLIENT> newAuthn(AbsUserCache<AAFPermission> c) {
        return new AAFAuthn<>(this, c);
    }

    public AAFLurPerm newLur() throws CadiException {
        try {
            if (lur==null) {
                lur = new AAFLurPerm(this);
                return lur;
            } else {
                return new AAFLurPerm(this,lur);
            }
        } catch (CadiException e) {
            throw e;
        } catch (Exception e) {
            throw new CadiException(e);
        }
    }
    
    public AAFLurPerm newLur(AbsUserCache<AAFPermission> c) throws APIException {
        try {
            return new AAFLurPerm(this,c);
        } catch (APIException e) {
            throw e;
        } catch (Exception e) {
            throw new APIException(e);
        }
    }

    protected abstract Rcli<CLIENT> rclient(URI uri, SecuritySetter<CLIENT> ss) throws CadiException;
    
    public abstract Rcli<CLIENT> rclient(Locator<URI> loc, SecuritySetter<CLIENT> ss) throws CadiException;

    public Rcli<CLIENT> client(Locator<URI> locator) throws CadiException {
        return rclient(locator,si.defSS);
    }
    
    public abstract<RET> RET best(Retryable<RET> retryable) throws LocatorException, CadiException, APIException;

    public abstract<RET> RET bestForUser(GetSetter get, Retryable<RET> retryable) throws LocatorException, CadiException, APIException;

    public abstract SecuritySetter<CLIENT> basicAuth(String user, String password) throws CadiException;
    
    public abstract SecuritySetter<CLIENT> transferSS(TaggedPrincipal principal) throws CadiException;
    
    public abstract SecuritySetter<CLIENT> basicAuthSS(BasicPrincipal principal) throws CadiException;
    
    public abstract SecuritySetter<CLIENT> tokenSS(final String client_id, final String accessToken) throws CadiException;
    
    public abstract SecuritySetter<CLIENT> x509Alias(String alias) throws APIException, CadiException;
    

    public String getRealm() {
        return realm;

    }
    
    /**
     * This interface allows the AAFCon, even though generic, to pass in correctly typed values based on the above SS commands.
     * @author Jonathan
     *
     */
    public interface GetSetter {
        public<CLIENT> SecuritySetter<CLIENT> get(AAFCon<CLIENT> con) throws CadiException;
    }

    public SecuritySetter<CLIENT> set(final SecuritySetter<CLIENT> ss) {
        si.set(ss);
        for (Rcli<CLIENT> client : clients.values()) {
            client.setSecuritySetter(ss);
        }
        return ss;
    }
    
    public SecurityInfoC<CLIENT> securityInfo() {
        return si;
    }

    public String defID() {
        if (si!=null) {
            return si.defSS.getID();
        }
        return "unknown";
    }
    
    public void invalidate() throws CadiException {
        for (Rcli<CLIENT> client : clients.values()) {
            client.invalidate();
        }
        clients.clear();
    }

    public String readableErrMsg(Future<?> f) {
        String text = f.body();
        if (text==null || text.length()==0) {
            text = f.code() + ": **No Message**";
        } else if (text.contains("%")) {
            try {
                Error err = errDF.newData().in(TYPE.JSON).load(f.body()).asObject();
                return Vars.convert(err.getText(),err.getVariables());
            } catch (APIException e){
                access.log(e);
            }
        }
        return text;
    }
    
    public static AAFCon<?> newInstance(PropAccess pa) throws CadiException, LocatorException {
        // Potentially add plugin for other kinds of Access
        return new AAFConHttp(pa);
    }
}
