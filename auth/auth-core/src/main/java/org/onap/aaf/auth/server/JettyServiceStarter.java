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
package org.onap.aaf.auth.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.auth.rserv.RServlet;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Split;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;


public class JettyServiceStarter<ENV extends RosettaEnv, TRANS extends Trans> extends AbsServiceStarter<ENV,TRANS> {
    private boolean secure;

    public JettyServiceStarter(final AbsService<ENV,TRANS> service) throws OrganizationException {
        super(service);
        secure = true;
    }
    
    /**
     * Specifically set this Service starter to Insecure (HTTP) Mode. 
     * @return
     */
    public JettyServiceStarter<ENV,TRANS> insecure() {
        secure = false;
        return this;
    }


    @Override
    public void _propertyAdjustment() {
//        System.setProperty("com.sun.management.jmxremote.port", "8081");
        Properties props = access().getProperties();
        Object httpproto = null;
        // Critical - if no Security Protocols set, then set it.  We'll just get messed up if not
        if ((httpproto=props.get(Config.CADI_PROTOCOLS))==null) {
            if ((httpproto=props.get(Config.HTTPS_PROTOCOLS))==null) {
                props.put(Config.CADI_PROTOCOLS, (httpproto=Config.HTTPS_PROTOCOLS_DEFAULT));
            } else {
                props.put(Config.CADI_PROTOCOLS, httpproto);
            }
        }
    
        if ("1.7".equals(System.getProperty("java.specification.version")) && (httpproto==null || (httpproto instanceof String && ((String)httpproto).contains("TLSv1.2")))) {
            System.setProperty(Config.HTTPS_CIPHER_SUITES, Config.HTTPS_CIPHER_SUITES_DEFAULT);
        }
    }

    @Override
    public void _start(RServlet<TRANS> rserv) throws Exception {
        final int port = Integer.parseInt(access().getProperty("port","0"));
        final String keystore = access().getProperty(Config.CADI_KEYSTORE, null);
        final int IDLE_TIMEOUT = Integer.parseInt(access().getProperty(Config.AAF_CONN_IDLE_TIMEOUT, Config.AAF_CONN_IDLE_TIMEOUT_DEF));
        Server server = new Server();
        
        ServerConnector conn;
        String protocol;
        if (!secure || keystore==null) {
            conn = new ServerConnector(server);
            protocol = "http";
        } else {
            protocol = "https";
            

            String keystorePassword = access().getProperty(Config.CADI_KEYSTORE_PASSWORD, null);
            if (keystorePassword==null) {
                throw new CadiException("No Keystore Password configured for " + keystore);
            }
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(keystore);
            String temp;
            sslContextFactory.setKeyStorePassword(temp=access().decrypt(keystorePassword, true)); // don't allow unencrypted
            sslContextFactory.setKeyManagerPassword(temp);
            temp=null; // don't leave lying around
            
            String truststore = access().getProperty(Config.CADI_TRUSTSTORE, null);
            if (truststore!=null) {
                String truststorePassword = access().getProperty(Config.CADI_TRUSTSTORE_PASSWORD, null);
                if (truststorePassword==null) {
                    throw new CadiException("No Truststore Password configured for " + truststore);
                }
                sslContextFactory.setTrustStorePath(truststore);
                sslContextFactory.setTrustStorePassword(access().decrypt(truststorePassword, false)); 
            }
            // Be able to accept only certain protocols, i.e. TLSv1.1+
            String subprotocols = access().getProperty(Config.CADI_PROTOCOLS, Config.HTTPS_PROTOCOLS_DEFAULT);
            service.setSubprotocol(subprotocols);
            final String[] protocols = Split.splitTrim(',', subprotocols);
            sslContextFactory.setIncludeProtocols(protocols);
            
            // Want to use Client Certificates, if they exist.
            sslContextFactory.setWantClientAuth(true);
            
            // Optional future checks.
            //   sslContextFactory.setValidateCerts(true);
            //     sslContextFactory.setValidatePeerCerts(true);
            //     sslContextFactory.setEnableCRLDP(false);
            //     sslContextFactory.setEnableOCSP(false);
            String certAlias = access().getProperty(Config.CADI_ALIAS, null);
            if (certAlias!=null) {
                sslContextFactory.setCertAlias(certAlias);
            }
            
            HttpConfiguration httpConfig = new HttpConfiguration();
            httpConfig.setSecureScheme(protocol);
            httpConfig.setSecurePort(port);
            httpConfig.addCustomizer(new SecureRequestCustomizer());
            //  httpConfig.setOutputBufferSize(32768);  Not sure why take this setting
            
            conn = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory,HttpVersion.HTTP_1_1.asString()),
                    new HttpConnectionFactory(httpConfig)
                );
        }
        service.setProtocol(protocol);

        
        // Setup JMX 
        // TODO trying to figure out how to set up/log ports
//        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
//        MBeanContainer mbContainer=new MBeanContainer(mbeanServer);
//        server.addEventListener(mbContainer);
//        server.addBean(mbContainer);
        
        // Add loggers MBean to server (will be picked up by MBeanContainer above)
//        server.addBean(Log.getLog());
    
        conn.setHost(hostname);
        conn.setPort(port);
        conn.setIdleTimeout(IDLE_TIMEOUT);
        server.addConnector(conn);
        
        server.setHandler(new AbstractHandler() {
                private FilterChain fc = buildFilterChain(service,new FilterChain() {
                    @Override
                    public void doFilter(ServletRequest req, ServletResponse resp) throws IOException, ServletException {
                        rserv.service(req, resp);
                    }
                });
                
                @Override
                public void handle(String target, Request baseRequest, HttpServletRequest hreq, HttpServletResponse hresp) throws IOException, ServletException {
                    try {
                        fc.doFilter(hreq,hresp);
                    } catch (Exception e) {
                        service.access.log(e, "Error Processing " + target);
                        hresp.setStatus(500 /* Service Error */);
                    }
                    baseRequest.setHandled(true);
                }
            }
        );
        
        try {
            access().printf(Level.INIT, "Starting service on %s:%d (%s)",hostname,port,InetAddress.getByName(hostname).getHostAddress());
            server.start();
            access().log(Level.INIT,server.dump());
        } catch (Exception e) {
            access().log(e,"Error starting " + hostname + ':' + port + ' ' + InetAddress.getLocalHost().getHostAddress());
            String doExit = access().getProperty("cadi_exitOnFailure", "true");
            if (doExit == "true") {
                System.exit(1);
            } else {
                throw e;
            }
        }
        try {
        	String no_register = env().getProperty("aaf_no_register",null);
        	if(no_register==null) {
        		register(service.registrants(port));
        	} else {
        		access().printf(Level.INIT,"'aaf_no_register' is set.  %s will not be registered with Locator", service.app_name);
        	}
            access().printf(Level.INIT, "Starting Jetty Service for %s, version %s, on %s://%s:%d", service.app_name,service.app_version,protocol,hostname,port);
            
            rserv.postStartup(hostname, port);
        } catch (Exception e) {
            access().log(e,"Error registering " + service.app_name);
            String doExit = access().getProperty("cadi_exitOnFailure", "true");
            if (doExit == "true") {
                System.exit(1);
            } else {
                throw e;
            }
        }
    }

    private FilterChain buildFilterChain(final AbsService<?,?> as, final FilterChain doLast) throws CadiException, LocatorException {
        Filter[] filters = as.filters();
        FilterChain fc = doLast;
        for (int i=filters.length-1;i>=0;--i) {
            fc = new FCImpl(filters[i],fc);
        }
        return fc;
    }
    
    private class FCImpl implements FilterChain {
        private Filter f;
        private FilterChain next;
        
        public FCImpl(final Filter f, final FilterChain fc) {
            this.f=f;
            next = fc;
            
        }
        @Override
        public void doFilter(ServletRequest req, ServletResponse resp) throws IOException, ServletException {
            f.doFilter(req,resp, next);
        }
    }
}
