/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ============================================================================
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

package org.onap.aaf.auth.cui;

import java.io.PrintWriter;
import java.util.regex.Pattern;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.AAF_GUI;
import org.onap.aaf.auth.gui.Page;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.http.HTransferSS;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;


public class CUI extends HttpCode<AuthzTrans, Void> {
    private final AAF_GUI gui;
    private final static Pattern userPerm = Pattern.compile("perm (create|delete).*@.*:id.*aaf.gui.*");


    public CUI(AAF_GUI gui) {
        super(null,"Command Line");
        this.gui = gui;
    }

    @Override
    public void handle(AuthzTrans trans, HttpServletRequest req,HttpServletResponse resp) throws Exception {
        ServletInputStream isr = req.getInputStream();
        PrintWriter pw = resp.getWriter();
        int c;
        StringBuilder cmd = new StringBuilder();

        while ((c=isr.read())>=0) {
            cmd.append((char)c);
        }

        TimeTaken tt = trans.start("Execute AAFCLI", Env.REMOTE);
        try {
            TaggedPrincipal p = trans.getUserPrincipal();
            // Access needs to be set after overall construction.  Thus, the lazy create.
            AAFcli aafcli;
            AAFConHttp aafcon = gui.aafCon();
            aafcli= new AAFcli(gui.access,gui.env, pw,
                    aafcon.hman(),
                    aafcon.securityInfo(),
                    new HTransferSS(p,AAF_GUI.app,
                            aafcon.securityInfo()));
            aafcli.verbose(false);
            aafcli.gui(true);

            String cmdStr = cmd.toString();
            if (cmdStr.contains("--help")) {
                cmdStr = cmdStr.replaceAll("--help", "help");
            }
            if (cmdStr.contains("--version")) {
                cmdStr = cmdStr.replaceAll("--version", "version");
            }
            try {
                aafcli.eval(cmdStr);
                if(userPerm.matcher(cmdStr).matches()) {
                    trans.clearCache();
                    Cookie cookie = new Cookie(Page.AAF_THEME,trans.getProperty(Page.AAF_THEME));
                    cookie.setMaxAge(-1);
                    cookie.setComment("Remove AAF GUI Theme");
                    trans.hresp().addCookie(cookie);
                }
                pw.flush();
            } catch (Exception e) {
                pw.flush();
                trans.error().log("Error", e.getMessage());
            } finally {
                aafcli.close();
            }
        } finally {
            tt.done();
        }

    }
}
