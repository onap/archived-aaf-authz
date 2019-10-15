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

package org.onap.aaf.cadi.olur;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.cadi.lur.LocalPermission;
import org.onap.aaf.cadi.oauth.AbsOTafLur;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.oauth.TimedToken;
import org.onap.aaf.cadi.oauth.TokenClient;
import org.onap.aaf.cadi.oauth.TokenPerm;
import org.onap.aaf.cadi.principal.Kind;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Pool.Pooled;
import org.onap.aaf.misc.env.util.Split;

public class OLur extends AbsOTafLur implements Lur {
    public OLur(PropAccess access, final String token_url, final String introspect_url) throws APIException, CadiException {
        super(access, token_url, introspect_url);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#fish(java.security.Principal, org.onap.aaf.cadi.Permission)
     */
    @Override
    public boolean fish(Principal bait, Permission ... pond) {
        TokenPerm tp;
        if (bait instanceof OAuth2Principal) {
            OAuth2Principal oa2p = (OAuth2Principal)bait;
            tp = oa2p.tokenPerm();
        } else {
            tp=null;
        }
        if (tp==null) {
            // if no Token Perm preset, get
            try {
                Pooled<TokenClient> tcp = tokenClientPool.get();
                try {
                    TokenClient tc = tcp.content;
                    tc.username(bait.getName());
                    Set<String> scopeSet = new HashSet<>();
                    scopeSet.add(tc.defaultScope());
                    AAFPermission ap;
                    for (Permission p : pond) {
                        if (p instanceof AAFPermission) {
                            ap = (AAFPermission)p;
                            scopeSet.add(ap.getNS());
                        }
                    }
                    String[] scopes = new String[scopeSet.size()];
                    scopeSet.toArray(scopes);

                    Result<TimedToken> rtt = tc.getToken(Kind.getKind(bait),scopes);
                    if (rtt.isOK()) {
                        Result<TokenPerm> rtp = tkMgr.get(rtt.value.getAccessToken(), bait.getName().getBytes());
                        if (rtp.isOK()) {
                            tp = rtp.value;
                        }
                    }
                } finally {
                    tcp.done();
                }
            } catch (APIException | LocatorException | CadiException e) {
                access.log(e, "Unable to Get a Token");
            }
        }

        boolean rv = false;
        if (tp!=null) {
            if (tkMgr.access.willLog(Level.DEBUG)) {
                StringBuilder sb = new StringBuilder("AAF Permissions for user ");
                sb.append(bait.getName());
                sb.append(", from token ");
                sb.append(tp.get().getAccessToken());
                for (AAFPermission p : tp.perms()) {
                    sb.append("\n\t[");
                    sb.append(p.getNS());
                    sb.append(']');
                    sb.append(p.getType());
                    sb.append('|');
                    sb.append(p.getInstance());
                    sb.append('|');
                    sb.append(p.getAction());
                }
                sb.append('\n');
                access.log(Level.DEBUG, sb);
            }
            for (Permission p : pond) {
                if (rv) {
                    break;
                }
                for (AAFPermission perm : tp.perms()) {
                    if (rv=perm.match(p)) {
                        break;
                    }
                }
            }
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#fishAll(java.security.Principal, java.util.List)
     */
    @Override
    public void fishAll(Principal bait, List<Permission> permissions) {
        if (bait instanceof OAuth2Principal) {
            for (AAFPermission p : ((OAuth2Principal)bait).tokenPerm().perms()) {
                permissions.add(p);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#handlesExclusively(org.onap.aaf.cadi.Permission)
     */
    @Override
    public boolean handlesExclusively(Permission ... pond) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#handles(java.security.Principal)
     */
    @Override
    public boolean handles(Principal principal) {
        return principal instanceof OAuth2Principal;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Lur#createPerm(java.lang.String)
     */
    @Override
    public Permission createPerm(final String p) {
        String[] s = Split.split('|',p);
        switch(s.length) {
            case 3:
                return new AAFPermission(null, s[0],s[1],s[2]);
            case 4:
                return new AAFPermission(s[0],s[1],s[2],s[3]);
            default:
                return new LocalPermission(p);
        }
    }

}
