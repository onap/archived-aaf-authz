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

package org.onap.aaf.sample.cadi.tomcate;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.log4j.chainsaw.Main;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.PropAccess;

/** 
 * @author JonathanGathman
 *
 */
public class TomcatEmbedded {

    public static void main(String[] args) throws Exception {
        System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
        Tomcat tomcat = new Tomcat();
        
        Service service = tomcat.getService();
        service.addConnector(getSslConnector(new PropAccess(args), 8081));
        
        tomcat.addWebapp("/caditest", getRootFolder().getAbsolutePath());
        
        tomcat.start();
        tomcat.getServer().await();

    }
    
    private static Connector getSslConnector(PropAccess access, int port) throws IOException {
        Connector connector = new Connector();
        connector.setPort(port);
        connector.setSecure(true);
        connector.setScheme("https");
        setAttr(connector,access,"keyAlias","cadi_alias");
        setAttr(connector,access,"keystoreFile","cadi_keystore");
        connector.setAttribute("keystoreType", "PKCS12");
        setAttr(connector,access,"keystorePass","cadi_keystore_password");
        setAttr(connector,access,"truststoreFile","cadi_truststore");
        connector.setAttribute("truststoreType", "JKS");
        setAttr(connector,access,"truststorePass","cadi_truststore_password");
        connector.setAttribute("clientAuth", "want");
        connector.setAttribute("protocol", "HTTP/1.1");
        connector.setAttribute("sslProtocol", "TLS");
        connector.setAttribute("maxThreads", "200");
        connector.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");
        connector.setAttribute("SSLEnabled", true);
        return connector;
     }
    
    private static void setAttr(Connector connector, Access access, String ctag, String atag) throws IOException {
        String value = access.getProperty(atag, null);
        if(value==null) {
            access.log(Level.ERROR, atag, "is null");
        } else {
            if(value.startsWith("enc:")) {
                access.log(Level.INIT,atag,"=enc:************");
                value = access.decrypt(value, false);
            } else {
                access.log(Level.INIT,atag,"=",value);
            }
            connector.setAttribute(ctag, value);
        }
    }

    private static File getRootFolder() {
        try {
            File root;
            String runningJarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("\\\\", "/");
            int lastIndexOf = runningJarPath.lastIndexOf("/target/");
            if (lastIndexOf < 0) {
                root = new File("");
            } else {
                root = new File(runningJarPath.substring(0, lastIndexOf));
            }
            System.out.println("application resolved root folder: " + root.getAbsolutePath());
            return root;
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
