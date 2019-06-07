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

import java.io.IOException;

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.CachedPrincipal;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.User;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.lur.ConfigPrincipal;

public class AAFAuthn<CLIENT> extends AbsUserCache<AAFPermission> {
    private AAFCon<CLIENT> con;
    private String realm;
    
    /**
     * Configure with Standard AAF properties, Stand alone
     * @param con
     * @throws Exception ..
     */
    // Package on purpose
    AAFAuthn(AAFCon<CLIENT> con) {
        super(con.access,con.cleanInterval,con.highCount,con.usageRefreshTriggerCount);
        this.con = con;
    }

    /**
     * Configure with Standard AAF properties, but share the Cache (with AAF Lur)
     * @param con
     * @throws Exception 
     */
    // Package on purpose
    AAFAuthn(AAFCon<CLIENT> con, AbsUserCache<AAFPermission> cache) {
        super(cache);
        this.con = con;
    }
    
    /**
     * Return Native Realm of AAF Instance.
     * 
     * @return
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Returns null if ok, or an Error String;
     * 
     * Convenience function.  Passes "null" for State object
     */
    public String validate(String user, String password) throws IOException {
        return validate(user,password,null);
    }
    
    /**
     * Returns null if ok, or an Error String;
     * 
     * For State Object, you may put in HTTPServletRequest or AuthzTrans, if available.  Otherwise,
     * leave null
     * 
     * @param user
     * @param password
     * @return
     * @throws IOException 
     * @throws CadiException 
     * @throws Exception
     */
    public String validate(String user, String password, Object state) throws IOException {
        password = access.decrypt(password, false);
        byte[] bytes = password.getBytes();
        User<AAFPermission> usr = getUser(user,bytes);

        if (usr != null && !usr.permExpired()) {
            if (usr.principal==null) {
                return "User already denied";
            } else {
                return null; // good
            }
        }

        AAFCachedPrincipal cp = new AAFCachedPrincipal(user, bytes, con.cleanInterval);
        // Since I've relocated the Validation piece in the Principal, just revalidate, then do Switch
        // Statement
        switch(cp.revalidate(state)) {
            case REVALIDATED:
                if (usr!=null) {
                    usr.principal = cp;
                } else {
                    addUser(new User<AAFPermission>(cp,con.timeout));
                }
                return null;
            case INACCESSIBLE:
                return "AAF Inaccessible";
            case UNVALIDATED:
                addUser(new User<AAFPermission>(user,bytes,con.timeout));
                return "user/pass combo invalid for " + user;
            case DENIED:
                return "AAF denies API for " + user;
            default: 
                return "AAFAuthn doesn't handle Principal " + user;
        }
    }
    
    private class AAFCachedPrincipal extends ConfigPrincipal implements CachedPrincipal {
        private long expires;
        private long timeToLive;

        private AAFCachedPrincipal(String name, byte[] pass, int timeToLive) {
            super(name,pass);
            this.timeToLive = timeToLive;
            expires = timeToLive + System.currentTimeMillis();
        }

        public Resp revalidate(Object state) {
            try {
                Miss missed = missed(getName(),getCred());
                if (missed==null || missed.mayContinue()) {
                    Rcli<CLIENT> client = con.client().forUser(con.basicAuth(getName(), new String(getCred())));
                    Future<String> fp = client.read(
                            "/authn/basicAuth",
                            "text/plain"
                            );
                    if (fp.get(con.timeout)) {
                        expires = System.currentTimeMillis() + timeToLive;
                        addUser(new User<AAFPermission>(this, expires));
                        return Resp.REVALIDATED;
                    } else {
                        addMiss(getName(), getCred());
                        return Resp.UNVALIDATED;
                    }
                } else {
                    return Resp.UNVALIDATED;
                }
            } catch (Exception e) {
                con.access.log(e);
                return Resp.INACCESSIBLE;
            }
        }

        public long expires() {
            return expires;
        }
    }

}
