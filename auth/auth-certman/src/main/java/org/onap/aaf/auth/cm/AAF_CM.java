/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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


package org.onap.aaf.auth.cm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.Filter;

import org.onap.aaf.auth.cache.Cache;
import org.onap.aaf.auth.cache.Cache.Dated;
import org.onap.aaf.auth.cm.api.API_Artifact;
import org.onap.aaf.auth.cm.api.API_Cert;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.facade.Facade1_0;
import org.onap.aaf.auth.cm.facade.FacadeFactory;
import org.onap.aaf.auth.cm.mapper.Mapper.API;
import org.onap.aaf.auth.cm.service.CMService;
import org.onap.aaf.auth.cm.service.Code;
import org.onap.aaf.auth.cm.validation.CertmanValidator;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.direct.DirectLocatorCreator;
import org.onap.aaf.auth.direct.DirectRegistrar;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.auth.server.JettyServiceStarter;
import org.onap.aaf.auth.server.Log4JLogIt;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.aaf.v2_0.AAFTrustChecker;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.util.Split;

import com.datastax.driver.core.Cluster;

public class AAF_CM extends AbsService<AuthzEnv, AuthzTrans> {

    private static final String USER_PERMS = "userPerms";
    private static final String CM_ALLOW_TMP = "cm_allow_tmp";
    private static final Map<String,CA> certAuths = new TreeMap<>();
    public static  Facade1_0 facade1_0; // this is the default Facade
    public static  Facade1_0 facade1_0_XML; // this is the XML Facade
    public static  Map<String, Dated> cacheUser;
    public static  AAFAuthn<?> aafAuthn;
    public static  AAFLurPerm aafLurPerm;
    public final  Cluster cluster;
    public final LocateDAO locateDAO;
    public static AuthzEnv envLog;
    CMService service;

    //Added for junits
    public CMService getService() {
        return null;
    }
    /**
     * Construct AuthzAPI with all the Context Supporting Routes that Authz needs
     * <p>
     * @param env
     * @param si 
     * @param dm 
     * @param decryptor 
     * @throws APIException 
     */
    public AAF_CM(AuthzEnv env) throws Exception {
        super(env.access(),env);
        aafLurPerm = aafCon().newLur();
        // Note: If you need both Authn and Authz construct the following:
        aafAuthn = aafCon().newAuthn(aafLurPerm);

        String aafEnv = env.getProperty(Config.AAF_ENV);
        if (aafEnv==null) {
            throw new APIException("aaf_env needs to be set");
        }
    
        // Check for allowing /tmp in Properties
        String allowTmp = env.getProperty(CM_ALLOW_TMP);
        if("true".equalsIgnoreCase(allowTmp)) {
            CertmanValidator.allowTmp();
        }


        // Initialize Facade for all uses
        AuthzTrans trans = env.newTrans();

        cluster = org.onap.aaf.auth.dao.CassAccess.cluster(env,null);
        locateDAO = new LocateDAO(trans,cluster,CassAccess.KEYSPACE);

        // Have AAFLocator object Create DirectLocators for Location needs
        AbsAAFLocator.setCreator(new DirectLocatorCreator(env, locateDAO));

        // Load Supported Certificate Authorities by property
        // Note: Some will be dynamic Properties, so we need to look through all
        for (Entry<Object, Object> es : env.access().getProperties().entrySet()) {
            String key = es.getKey().toString();
            if (key.startsWith(CA.CM_CA_PREFIX)) {
                int idx = key.indexOf('.');
                if (idx==key.lastIndexOf('.')) { // else it's a regular property 
                    env.log(Level.INIT, "Loading Certificate Authority Module: " + key.substring(idx+1));
                    String[] segs = Split.split(',', env.getProperty(key));
                    if (segs.length>0) {
                        String[][] multiParams = new String[segs.length-1][];
                        for (int i=0;i<multiParams.length;++i) {
                            multiParams[i]=Split.split(';',segs[1+i]);
                        }
                        @SuppressWarnings("unchecked")
                        Class<CA> cac = (Class<CA>)Class.forName(segs[0]);
                        Constructor<CA> cons = cac.getConstructor(new Class<?>[] {
                            Access.class,String.class,String.class,String[][].class
                        });
                        Object pinst[] = new Object[4];
                        pinst[0]=env;
                        pinst[1]= key.substring(idx+1);
                        pinst[2]= aafEnv;
                        pinst[3] = multiParams; 
                        try {
                            CA ca = cons.newInstance(pinst);
                            certAuths.put(ca.getName(),ca);
                        } catch (InvocationTargetException e) {
                            access.log(e, "Loading", segs[0]);
                        }
                    }
                }
            }
        }
        if (certAuths.size()==0) {
            throw new APIException("No Certificate Authorities have been configured in CertMan");
        }

        service = getService();
        if(service == null) {
            service = new CMService(trans, this);
        }
        // note: Service knows how to shutdown Cluster on Shutdown, etc.  See Constructor
        facade1_0 = FacadeFactory.v1_0(this,trans, service,Data.TYPE.JSON);   // Default Facade
        facade1_0_XML = FacadeFactory.v1_0(this,trans,service,Data.TYPE.XML); 


        synchronized(env) {
            if (cacheUser == null) {
                cacheUser = Cache.obtain(USER_PERMS);
                Cache.startCleansing(env, USER_PERMS);
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // APIs
        ////////////////////////////////////////////////////////////////////////
        API_Cert.init(this);
        API_Artifact.init(this);

        StringBuilder sb = new StringBuilder();
        trans.auditTrail(2, sb);
        trans.init().log(sb);
    }

    public CA getCA(String key) {
        return certAuths.get(key);
    }


    /**
     * Setup XML and JSON implementations for each supported Version type
     * <p>
     * We do this by taking the Code passed in and creating clones of these with the appropriate Facades and properties
     * to do Versions and Content switches
     * <p>
     */
    public void route(HttpMethods meth, String path, API api, Code code) throws Exception {
        String version = "1.0";
        // Get Correct API Class from Mapper
        Class<?> respCls = facade1_0.mapper().getClass(api); 
        if (respCls==null) throw new Exception("Unknown class associated with " + api.getClass().getName() + ' ' + api.name());
        // setup Application API HTML ContentTypes for JSON and Route
        String application = applicationJSON(respCls, version);
        route(env,meth,path,code,application,"application/json;version="+version,"*/*");

        // setup Application API HTML ContentTypes for XML and Route
        application = applicationXML(respCls, version);
        route(env,meth,path,code.clone(facade1_0_XML),application,"application/xml;version="+version);

        // Add other Supported APIs here as created
    }

    public void routeAll(HttpMethods meth, String path, API api, Code code) {
        route(env,meth,path,code,""); // this will always match
    }

    @Override
    public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
        try {
            return new Filter[] {
                    new AuthzTransFilter(env,aafCon(),
                        new AAFTrustChecker((Env)env),
                        additionalTafLurs)
                };
        } catch (NumberFormatException e) {
            throw new CadiException("Invalid Property information", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Registrant<AuthzEnv>[] registrants(final int port) throws CadiException, LocatorException {
        return new Registrant[] {
            new DirectRegistrar(access,locateDAO,port)
        };
    }

    public void destroy() {
        Cache.stopTimer();
        locateDAO.close(env.newTransNoAvg());
        cluster.close();
    }

    public static void main(final String[] args) {
        try {
            Log4JLogIt logIt = new Log4JLogIt(args, "cm");
            PropAccess propAccess = new PropAccess(logIt,args);
            try {
                new JettyServiceStarter<AuthzEnv,AuthzTrans>(
                    new AAF_CM(new AuthzEnv(propAccess)),true)
                        .start();
            } catch (Exception e) {
                propAccess.log(e);
            }
        } catch (APIException e) {
            e.printStackTrace(System.err);
        }
    }
}
