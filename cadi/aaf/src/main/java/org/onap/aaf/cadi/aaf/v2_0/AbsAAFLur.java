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

package org.onap.aaf.cadi.aaf.v2_0;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CachingLur;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.User;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Split;

public abstract class AbsAAFLur<PERM extends Permission> extends AbsUserCache<PERM> implements CachingLur<PERM> {
    protected static final byte[] BLANK_PASSWORD = new byte[0];
    private String[] debug = null;
    public AAFCon<?> aaf;
    public Lur preemptiveLur=null; // Initial Use is for OAuth2, preemptive Lur
    private String[] supports;
    protected boolean details;

    public AbsAAFLur(AAFCon<?> con) throws APIException {
        super(con.access, con.cleanInterval, con.highCount, con.usageRefreshTriggerCount);
        aaf = con;
        setLur(this);
        supports = con.access.getProperty(Config.AAF_DOMAIN_SUPPORT, Config.AAF_DOMAIN_SUPPORT_DEF).split("\\s*:\\s*");
    }

    public AbsAAFLur(AAFCon<?> con, AbsUserCache<PERM> auc) throws APIException {
        super(auc);
        aaf = con;
        setLur(this);
        supports = con.access.getProperty(Config.AAF_DOMAIN_SUPPORT, Config.AAF_DOMAIN_SUPPORT_DEF).split("\\s*:\\s*");
    }

    @Override
    public void setDebug(String ids) {
        this.debug = ids==null?null:Split.split(',', ids);
    }

    public void details(boolean on) {
        details = on;
    }


    public void setPreemptiveLur(Lur preemptive) {
        this.preemptiveLur = preemptive;
    }

    protected abstract User<PERM> loadUser(Principal bait);

    @Override
    public final boolean handles(Principal principal) {
        if (preemptiveLur!=null) {
            if (preemptiveLur.handles(principal)) {
                return true;
            }
        }
        String userName=principal.getName();
        if (userName!=null) {
            for (String s : supports) {
                if (userName.endsWith(s))
                    return true;
            }
        }
        return false;
    }


    protected abstract boolean isCorrectPermType(Permission pond);

    // This is where you build AAF CLient Code.  Answer the question "Is principal "bait" in the "pond"
    public boolean fish(Principal bait, Permission ... pond) {
        if (preemptiveLur!=null && preemptiveLur.handles(bait)) {
            return preemptiveLur.fish(bait, pond);
        } else {
            if (pond==null) {
                return false;
            }
            if (isDebug(bait)) {
                boolean rv = false;
                StringBuilder sb = new StringBuilder("Log for ");
                sb.append(bait);
                if (handles(bait)) {
                    User<PERM> user = getUser(bait);
                    if (user==null) {
                        sb.append("\n\tUser is not in Cache");
                    } else {
                        if (user.noPerms()) {
                            sb.append("\n\tUser has no Perms");
                        }
                        if (user.permExpired()) {
                            sb.append("\n\tUser's perm expired [");
                            sb.append(new Date(user.permExpires()));
                            sb.append(']');
                        } else {
                            sb.append("\n\tUser's perm expires [");
                            sb.append(new Date(user.permExpires()));
                            sb.append(']');
                        }
                    }
                    if (user==null || user.permsUnloaded() || user.permExpired()) {
                        user = loadUser(bait);
                        sb.append("\n\tloadUser called");
                    }
                    for (Permission p : pond) {
                        if (user==null) {
                            sb.append("\n\tUser was not Loaded");
                            break;
                        } else if (user.contains(p)) {
                            sb.append("\n\tUser contains ");
                            sb.append(p.getKey());
                            rv = true;
                        } else {
                            sb.append("\n\tUser does not contain ");
                            sb.append(p.getKey());
                            List<Permission> perms = new ArrayList<>();
                            user.copyPermsTo(perms);
                            for (Permission perm : perms) {
                                sb.append("\n\t\t");
                                sb.append(perm.getKey());
                            }
                        }
                    }
                } else {
                    sb.append("AAF Lur does not support [");
                    sb.append(bait);
                    sb.append("]");
                }
                aaf.access.log(Level.INFO, sb);
                return rv;
            } else {
                boolean rv = false;
                if (handles(bait)) {
                    User<PERM> user = getUser(bait);
                    if (user==null || user.permsUnloaded() || user.permExpired()) {
                        user = loadUser(bait);
                    }
                    if (user==null) {
                        return false;
                    } else {
                        for (Permission p : pond) {
                            if (rv=user.contains(p)) {
                                break;
                            }
                        }
                    }
                }
                return rv;
            }
        }
    }

    public void fishAll(Principal bait, List<Permission> perms) {
        if (preemptiveLur!=null && preemptiveLur.handles(bait)) {
            preemptiveLur.fishAll(bait, perms);
        } else {
            if (isDebug(bait)) {
                StringBuilder sb = new StringBuilder("Log for ");
                sb.append(bait);
                if (handles(bait)) {
                    User<PERM> user = getUser(bait);
                    if (user==null) {
                        sb.append("\n\tUser is not in Cache");
                    } else {
                        if (user.noPerms()) {
                            sb.append("\n\tUser has no Perms");
                        }
                        if (user.permExpired()) {
                            sb.append("\n\tUser's perm expired [");
                            sb.append(new Date(user.permExpires()));
                            sb.append(']');
                        } else {
                            sb.append("\n\tUser's perm expires [");
                            sb.append(new Date(user.permExpires()));
                            sb.append(']');
                        }
                    }
                    if (user==null || user.permsUnloaded() || user.permExpired()) {
                        user = loadUser(bait);
                        sb.append("\n\tloadUser called");
                    }
                    if (user==null) {
                        sb.append("\n\tUser was not Loaded");
                    } else {
                        sb.append("\n\tCopying Perms ");
                        user.copyPermsTo(perms);
                        for (Permission p : perms) {
                            sb.append("\n\t\t");
                            sb.append(p.getKey());
                        }
                    }
                } else {
                    sb.append("AAF Lur does not support [");
                    sb.append(bait);
                    sb.append("]");
                }
                aaf.access.log(Level.INFO, sb);
            } else {
                if (handles(bait)) {
                    User<PERM> user = getUser(bait);
                    if (user==null || user.permsUnloaded() || user.permExpired()) {
                        user = loadUser(bait);
                    }
                    if (user!=null) {
                        user.copyPermsTo(perms);
                    }
                }
            }
        }
    }

    @Override
    public void remove(String user) {
        super.remove(user);
    }

    private boolean isDebug(Principal p) {
        if (debug!=null) {
            if (debug.length==1 && "all".equals(debug[0])) {
                return true;
            }
            String name = p.getName();
            for (String s : debug) {
                if (s.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * This special case minimizes loops, avoids multiple Set hits, and calls all the appropriate Actions found.
     * <p>
     * @param bait
     * @param obj
     * @param type
     * @param instance
     * @param actions
     */
    public<A> void fishOneOf(Principal princ, A obj, String type, String instance, List<Action<A>> actions) {
        User<PERM> user = getUser(princ);
        if (user==null || user.permsUnloaded() || user.permExpired()) {
            user = loadUser(princ);
        }
        if (user!=null) {
            ReuseAAFPermission perm = new ReuseAAFPermission(type,instance);
            for (Action<A> action : actions) {
                perm.setAction(action.getName());
                if (user.contains(perm)) {
                    if (action.exec(obj))return;
                }
            }
        }
    }

    public static interface Action<A> {
        public String getName();
        /**
         *  Return false to continue, True to end now
         * @return
         */
        public boolean exec(A a);
    }

    private class ReuseAAFPermission extends AAFPermission {
        public ReuseAAFPermission(String type, String instance) {
            super(type,instance,null,null);
        }

        public void setAction(String s) {
            action = s;
        }
    
        /**
         * This function understands that AAF Keys are hierarchical, :A:B:C, 
         *  Cassandra follows a similar method, so we'll short circuit and do it more efficiently when there isn't a first hit
         * @return
         */
    }
}
