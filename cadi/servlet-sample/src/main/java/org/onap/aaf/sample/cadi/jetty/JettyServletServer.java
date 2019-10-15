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

package org.onap.aaf.sample.cadi.jetty;

import java.net.Inet4Address;
import java.util.concurrent.ArrayBlockingQueue;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfo;
import org.onap.aaf.cadi.filter.CadiFilter;

public abstract class JettyServletServer implements Servlet {

    public static Server run(PropAccess access, String context, Class<? extends Servlet> servletCls, int port, String ...args) throws Exception {
        // Defaults:
        int blockingQueueSize = 10;
        int corePoolSize = 10;
        int maxPoolSize = 10;
        int keepAliveTime  = 3000;
        String hostname = access.getProperty(Config.HOSTNAME, null);
        if (hostname==null) {
            hostname = Inet4Address.getLocalHost().getHostName();
        }

        // Add your own Properties to override defaults

        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(blockingQueueSize);
        QueuedThreadPool pool = new QueuedThreadPool(maxPoolSize,corePoolSize,keepAliveTime,queue);
        Server server = new Server(pool);

        String protocol;
        if (access.getProperty(Config.CADI_KEYSTORE_PASSWORD,null)==null) {
            ServerConnector conn = new ServerConnector(server);
            conn.setHost(hostname);
            conn.setPort(port);
            server.addConnector(conn);
            protocol = "http";
        } else {
            // Setup Security
            SecurityInfo securityInfo = new SecurityInfo(access);
            SslContextFactory scf = new SslContextFactory();
            scf.setSslContext(securityInfo.getSSLContext());
            scf.setWantClientAuth(true);
            ServerConnector sslConnector = new ServerConnector(server,scf);
            sslConnector.setHost(hostname);
            sslConnector.setPort(port);
            server.addConnector(sslConnector);
            protocol = "https";
        }

        // Setup Sample Servlet
        CadiFilter cf = new CadiFilter(true,access);
        FilterHolder cfh = new FilterHolder(cf);

        ServletHandler shand = new ServletHandler();
        shand.addFilterWithMapping(cfh, "/*", FilterMapping.ALL);
        // To use normal Servlets, just add the class here... Actually, bug in Jetty... need to add with ServletHolder
        ServletHolder sh = new ServletHolder();
        sh.setServlet(servletCls.newInstance());
        shand.addServletWithMapping(sh,"/*");

        // To use JASPI Authorization Style to protect the servlet, wrap the Servlet
        // with the "MiniJSAPIWrap class, as shown here.  Then add "@RolesAllowed" on your
        // servlet (see sample).  Use Pipe delimited Permissions, not AAF Roles in the line
        // shand.addServletWithMapping(new MiniJASPIWrap(MyServlet.class),"/*");
        // call initialize after start
        ContextHandler ch = new ServletContextHandler();
        ch.setContextPath(context);
        ch.setHandler(shand);
        server.setHandler(ch);
        // Startup the Server
        server.setStopAtShutdown(true);
        server.start();

        access.log(Level.INFO,"TestServlet is running at " + protocol + "://"+hostname+':'+port+context);
        return server;
    }

}
