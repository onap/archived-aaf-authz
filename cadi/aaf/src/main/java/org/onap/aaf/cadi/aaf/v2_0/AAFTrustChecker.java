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

import javax.servlet.http.HttpServletRequest ;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.TrustChecker;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.principal.TrustPrincipal;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TrustNotTafResp;
import org.onap.aaf.cadi.taf.TrustTafResp;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.util.Split;

public class AAFTrustChecker implements TrustChecker {
    private final String tag, id;
    private final AAFPermission perm;
    private Lur lur;

    /**
     *
     * Instance will be replaced by Identity
     * @param lur
     *
     * @param tag
     * @param perm
     */
    public AAFTrustChecker(final Env env) {
        tag = env.getProperty(Config.CADI_USER_CHAIN_TAG, Config.CADI_USER_CHAIN);
        id = env.getProperty(Config.CADI_ALIAS,env.getProperty(Config.AAF_APPID)); // share between components
        String str = env.getProperty(Config.CADI_TRUST_PERM);
        AAFPermission temp=null;
        if (str!=null) {
            String[] sp = Split.splitTrim('|', str);
            switch(sp.length) {
                case 3:
                    temp = new AAFPermission(null,sp[0],sp[1],sp[2]);
                    break;
                case 4:
                    temp = new AAFPermission(sp[0],sp[1],sp[2],sp[3]);
                    break;
            }
        }
        perm=temp;
    }

    public AAFTrustChecker(final Access access) {
        tag = access.getProperty(Config.CADI_USER_CHAIN_TAG, Config.CADI_USER_CHAIN);
        id = access.getProperty(Config.CADI_ALIAS,access.getProperty(Config.AAF_APPID,null)); // share between components
        String str = access.getProperty(Config.CADI_TRUST_PERM,null);
        AAFPermission temp=null;
        if (str!=null) {
            String[] sp = Split.splitTrim('|', str);
            switch(sp.length) {
                case 3:
                    temp = new AAFPermission(null,sp[0],sp[1],sp[2]);
                    break;
                case 4:
                    temp = new AAFPermission(sp[0],sp[1],sp[2],sp[3]);
                    break;
            }
        }
        perm=temp;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.TrustChecker#setLur(org.onap.aaf.cadi.Lur)
     */
    @Override
    public void setLur(Lur lur) {
        this.lur = lur;
    }

    @Override
    public TafResp mayTrust(TafResp tresp, HttpServletRequest req) {
        String user_info = req.getHeader(tag);
        if (user_info == null) {
            return tresp;
        }
    
        tresp.getAccess().log(Level.DEBUG, user_info);

        String[] info = Split.split(',', user_info);
        String[] flds = Split.splitTrim(':', info[0]);
        if (flds.length < 4) {
            return tresp;
        }
        if (!("AS".equals(flds[3]))) { // is it set for "AS"
            return tresp;
        }

        String principalName = tresp.getPrincipal().getName();
        if (principalName.equals(id)  // We do trust our own App Components: if a trust entry is made with self, always accept
                || lur.fish(tresp.getPrincipal(), perm)) { // Have Perm set by Config.CADI_TRUST_PERM
            String desc = "  " + flds[0] + " validated using " + flds[2] + " by " + flds[1] + ',';
            return new TrustTafResp(tresp, new TrustPrincipal(tresp.getPrincipal(), flds[0]), desc);
        } else if (principalName.equals(flds[0])) { // Ignore if same identity
            return tresp;
        } else {
            String desc = tresp.getPrincipal().getName() + " requested trust as " + flds[0] + ", but does not have Authorization";
            return new TrustNotTafResp(tresp, desc);
        }
    }

}