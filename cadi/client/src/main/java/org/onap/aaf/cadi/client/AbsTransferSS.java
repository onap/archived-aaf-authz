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

package org.onap.aaf.cadi.client;

import java.security.Principal;

import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

/**
 * This client represents the ability to Transfer the Identity of the caller to the authenticated
 * user being transferred to.  This ability is critical for App-to-App communication to ensure that
 * Authorization can happen on the End-Users' credentials when appropriate, even though Authentication
 * to App1 by App2 must be by App2's credentials.
 *
 * @author Jonathan
 *
 * @param <CLIENT>
 */
public abstract class AbsTransferSS<CLIENT> implements SecuritySetter<CLIENT> {
    protected String value;
    protected SecurityInfoC<CLIENT> securityInfo;
    protected SecuritySetter<CLIENT> defSS;
    private Principal principal;

    //Format:<ID>:<APP>:<protocol>[:AS][,<ID>:<APP>:<protocol>]*
    public AbsTransferSS(TaggedPrincipal principal, String app) {
        init(principal, app);
    }

    public AbsTransferSS(TaggedPrincipal principal, String app, SecurityInfoC<CLIENT> si) {
        init(principal,app);
        securityInfo = si;
        this.defSS = si.defSS;
    }

    private void init(TaggedPrincipal principal, String app)  {
        this.principal=principal;
        if (principal==null) {
            return;
        } else  {
            value = principal.getName() + ':' +
                    app + ':' +
                    principal.tag() + ':' +
                    "AS";
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.SecuritySetter#getID()
     */
    @Override
    public String getID() {
        return principal==null?"":principal.getName();
    }
}
