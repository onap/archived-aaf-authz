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

package org.onap.aaf.auth.locate;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.locate.facade.LocateFacade;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.aaf.v2_0.AAFAuthn;
import org.onap.aaf.cadi.principal.BasicPrincipal;
import org.onap.aaf.cadi.principal.X509Principal;

public class BasicAuthCode extends LocateCode {
    private AAFAuthn<?> authn;

    public BasicAuthCode(AAFAuthn<?> authn, LocateFacade facade) {
        super(facade, "AAF Basic Auth",true);
        this.authn = authn;
    }

    @Override
    public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Principal p = trans.getUserPrincipal();
        if (p == null) {
            trans.error().log("Transaction not Authenticated... no Principal");
        } else if (p instanceof BasicPrincipal) {
            // the idea is that if call is made with this credential, and it's a BasicPrincipal, it's ok
            // otherwise, it wouldn't have gotten here.
            resp.setStatus(HttpStatus.OK_200);
            return;
        } else if (p instanceof X509Principal) {
            // Since X509Principal has priority, BasicAuth Info might be there, but not validated.
            String ba;
            if ((ba=req.getHeader("Authorization"))!=null && ba.startsWith("Basic ")) {
                ba = Symm.base64noSplit.decode(ba.substring(6));
                int colon = ba.indexOf(':');
                if (colon>=0) {
                    String err;
                    if ((err=authn.validate(ba.substring(0, colon), ba.substring(colon+1),trans))==null) {
                        resp.setStatus(HttpStatus.OK_200);
                    } else {
                        trans.audit().log(ba.substring(0,colon),": ",err);
                        resp.setStatus(HttpStatus.UNAUTHORIZED_401);
                    }
                    return;
                }
            }
        }
        trans.checkpoint("Basic Auth Check Failed: This wasn't a Basic Auth Trans");
        // For Auth Security questions, we don't give any info to client on why failed
        resp.setStatus(HttpStatus.FORBIDDEN_403);
    }
}
