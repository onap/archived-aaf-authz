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

package org.onap.aaf.auth.rserv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.misc.env.Trans;


public class Routes<TRANS extends Trans> {
    // Since this must be very, very fast, and only needs one creation, we'll use just an array.
    private Route<TRANS>[] routes;
    private int end;


    @SuppressWarnings("unchecked")
    public Routes() {
        routes = new Route[10];
        end = 0;
    }

    // This method for setup of Routes only...
    // Package on purpose
    synchronized Route<TRANS> findOrCreate(HttpMethods  meth, String path) {
        Route<TRANS> rv = null;
        for (int i=0;i<end;++i) {
            if (routes[i].resolvesTo(meth,path))rv = routes[i];
        }
    
        if (rv==null) {
            if (end>=routes.length) {
                @SuppressWarnings("unchecked")
                Route<TRANS>[] temp = new Route[end+10];
                System.arraycopy(routes, 0, temp, 0, routes.length);
                routes = temp;
            }
        
            routes[end++]=rv=new Route<TRANS>(meth,path);
        }
        return rv;
    }

    public Route<TRANS> derive(HttpServletRequest req, CodeSetter<TRANS> codeSetter)  throws IOException, ServletException {
        Route<TRANS> rv = null;
        String path = req.getPathInfo();
        String meth = req.getMethod();
        //TODO a TREE would be better
        for (int i=0;rv==null && i<end; ++i) {
            rv = routes[i].matches(meth,path);
            if (rv!=null && !codeSetter.matches(rv)) { // potential match, check if has Code 
                rv = null; // not quite, keep going
            }
        }
        return rv;
    }

    public List<RouteReport> routeReport() {
        ArrayList<RouteReport> ltr = new ArrayList<>();
        for (int i=0;i<end;++i) {
            ltr.add(routes[i].api());
        }
        return ltr;
    }
}
