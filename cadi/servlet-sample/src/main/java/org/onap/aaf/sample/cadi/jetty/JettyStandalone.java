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

package org.onap.aaf.sample.cadi.jetty;

import org.eclipse.jetty.server.Server;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.sample.cadi.MyServlet;
import org.onap.aaf.cadi.PropAccess;




public class JettyStandalone {
    public static void main(String[] args) {
        PropAccess access = new PropAccess(args);
        try {
            Server server = JettyServletServer.run(access, "/caditest", MyServlet.class, 3456);
            server.join();
        } catch (Exception e) {
            access.log(Level.ERROR, e);
        } finally {
            access.log(Level.INFO,"Stopping Service");
        }
    
    }

}
