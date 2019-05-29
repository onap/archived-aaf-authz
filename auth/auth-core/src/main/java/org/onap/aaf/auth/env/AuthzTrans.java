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

package org.onap.aaf.auth.env;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TransStore;

public interface AuthzTrans extends TransStore {
    public enum REQD_TYPE {future(1),force(2),move(4),ns(8),detail(16);
        public final int bit;

        REQD_TYPE(int bit) {
            this.bit = bit;
        }
    };
    
    public abstract AuthzTrans set(HttpServletRequest req);

	public abstract HttpServletRequest hreq();

    public abstract String user();

    public abstract void setUser(TaggedPrincipal p);
    
    public abstract TaggedPrincipal getUserPrincipal();

    public abstract String ip();

    public abstract int port();

    public abstract String meth();

    public abstract String path();

    public abstract String agent();
    
    public abstract AuthzEnv env();

    public abstract void setLur(Lur lur);

    public abstract boolean fish(Permission ... p);
    
    public abstract Organization org();

    public abstract boolean requested(REQD_TYPE requested);
    
    public void requested(REQD_TYPE requested, boolean b);
    
    public abstract void logAuditTrail(LogTarget lt);
    
    public abstract Date now();

}