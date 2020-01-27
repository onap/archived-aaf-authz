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

package org.onap.aaf.auth.gui;

import static org.onap.aaf.auth.rserv.HttpMethods.GET;
import static org.onap.aaf.auth.rserv.HttpMethods.POST;
import static org.onap.aaf.auth.rserv.HttpMethods.PUT;

import javax.servlet.Filter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cui.CUI;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.gui.pages.ApiDocs;
import org.onap.aaf.auth.gui.pages.ApiExample;
import org.onap.aaf.auth.gui.pages.ApprovalAction;
import org.onap.aaf.auth.gui.pages.ApprovalForm;
import org.onap.aaf.auth.gui.pages.CMArtiChangeAction;
import org.onap.aaf.auth.gui.pages.CMArtiChangeForm;
import org.onap.aaf.auth.gui.pages.CMArtifactShow;
import org.onap.aaf.auth.gui.pages.CredDetail;
import org.onap.aaf.auth.gui.pages.CredHistory;
import org.onap.aaf.auth.gui.pages.Home;
import org.onap.aaf.auth.gui.pages.LoginLanding;
import org.onap.aaf.auth.gui.pages.LoginLandingAction;
import org.onap.aaf.auth.gui.pages.NsDetail;
import org.onap.aaf.auth.gui.pages.NsHistory;
import org.onap.aaf.auth.gui.pages.NsInfoAction;
import org.onap.aaf.auth.gui.pages.NsInfoForm;
import org.onap.aaf.auth.gui.pages.NssShow;
import org.onap.aaf.auth.gui.pages.PassChangeAction;
import org.onap.aaf.auth.gui.pages.PassChangeForm;
import org.onap.aaf.auth.gui.pages.PassDeleteAction;
import org.onap.aaf.auth.gui.pages.PendingRequestsShow;
import org.onap.aaf.auth.gui.pages.PermDetail;
import org.onap.aaf.auth.gui.pages.PermGrantAction;
import org.onap.aaf.auth.gui.pages.PermGrantForm;
import org.onap.aaf.auth.gui.pages.PermHistory;
import org.onap.aaf.auth.gui.pages.PermsShow;
import org.onap.aaf.auth.gui.pages.RequestDetail;
import org.onap.aaf.auth.gui.pages.RoleDetail;
import org.onap.aaf.auth.gui.pages.RoleDetailAction;
import org.onap.aaf.auth.gui.pages.RoleHistory;
import org.onap.aaf.auth.gui.pages.RolesShow;
import org.onap.aaf.auth.gui.pages.UserRoleExtend;
import org.onap.aaf.auth.gui.pages.UserRoleRemove;
import org.onap.aaf.auth.gui.pages.WebCommand;
import org.onap.aaf.auth.rserv.CachingFileAccess;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.server.AbsService;
import org.onap.aaf.auth.server.JettyServiceStarter;
import org.onap.aaf.auth.server.Log4JLogIt;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.aaf.v2_0.AAFLurPerm;
import org.onap.aaf.cadi.aaf.v2_0.AAFTrustChecker;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.http.HTransferSS;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.register.Registrant;
import org.onap.aaf.cadi.register.RemoteRegistrant;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.xgen.html.HTMLGen;
import org.onap.aaf.misc.xgen.html.State;

import certman.v1_0.Artifacts;
import certman.v1_0.CertInfo;

public class AAF_GUI extends AbsService<AuthzEnv, AuthzTrans> implements State<Env>{
    public static final String AAF_GUI_THEME = "aaf_gui_theme";
    public static final String AAF_GUI_COPYRIGHT = "aaf_gui_copyright";
    public static final String HTTP_SERVLET_REQUEST = "HTTP_SERVLET_REQUEST";
    public static final int TIMEOUT = 60000;
    public static final String APP = "AAF GUI";

    // AAF API

    // Certificate manager API
    public RosettaDF<Artifacts> artifactsDF;
    public RosettaDF<CertInfo>  certInfoDF;

    private final AAFConHttp cmCon;
    public final AAFConHttp aafCon;
    public final AAFLurPerm lur;

    public final Slot slotHttpServletRequest;
    protected final String deployedVersion;
    private StaticSlot sThemeWebPath;
    private StaticSlot sDefaultTheme;


    public AAF_GUI(final AuthzEnv env) throws Exception {
        super(env.access(), env);
        sDefaultTheme = env.staticSlot(AAF_GUI_THEME);
        String defTheme = env.getProperty(AAF_GUI_THEME,"onap");
        env.put(sDefaultTheme, defTheme);

        sThemeWebPath = env.staticSlot(CachingFileAccess.CFA_WEB_PATH);
        if(env.get(sThemeWebPath)==null) {
            env.put(sThemeWebPath,"theme");
        }

        slotHttpServletRequest = env.slot(HTTP_SERVLET_REQUEST);
        deployedVersion = app_version;

        // Certificate Manager
        String aafUrlCm = env.getProperty(Config.AAF_URL_CM,Config.AAF_URL_CM_DEF);
        cmCon =  new AAFConHttp(env.access(),aafUrlCm);
        artifactsDF = env.newDataFactory(Artifacts.class);
        certInfoDF  = env.newDataFactory(CertInfo.class);


        /////////////////////////
        // Screens
        /////////////////////////
        // Start Screen
        final Page start = new Display(this, GET, new Home(this)).page();

        // MyPerms Screens
        final Page myPerms = new Display(this, GET, new PermsShow(this, start)).page();
        Page permDetail = new Display(this, GET, new PermDetail(this, start, myPerms)).page();
                            new Display(this, GET, new PermHistory(this,start,myPerms,permDetail));

        // MyRoles Screens
        final Page myRoles = new Display(this, GET, new RolesShow(this, start)).page();
        Page roleDetail = new Display(this, GET, new RoleDetail(this, start, myRoles)).page();
                          new Display(this, POST, new RoleDetailAction(this,start,myRoles,roleDetail));
                          new Display(this, GET, new RoleHistory(this,start,myRoles,roleDetail));

        // MyNameSpace
        final Page myNamespaces = new Display(this, GET, new NssShow(this, start)).page();
        Page nsDetail  = new Display(this, GET, new NsDetail(this, start, myNamespaces)).page();
                         new Display(this, GET, new NsHistory(this, start,myNamespaces,nsDetail));
        Page crdDetail = new Display(this, GET, new CredDetail(this, start, myNamespaces, nsDetail)).page();
                         new Display(this, GET, new CredHistory(this,start,myNamespaces,nsDetail,crdDetail));
        Page artiShow  = new Display(this, GET, new CMArtifactShow(this, start, myNamespaces, nsDetail, crdDetail)).page();
        Page artiCForm = new Display(this, GET, new CMArtiChangeForm(this, start, myNamespaces, nsDetail, crdDetail,artiShow)).page();
                         new Display(this, POST, new CMArtiChangeAction(this, start,artiShow,artiCForm));

        // Password Change Screens
        final Page pwc = new Display(this, GET, new PassChangeForm(this, start,crdDetail)).page();
                         new Display(this, POST, new PassChangeAction(this, start, pwc));

        // Password Delete Screen
                         new Display(this, GET, new PassDeleteAction(this, start,crdDetail));

        // Validation Change Screens
        final Page validate = new Display(this, GET, new ApprovalForm(this, start)).page();
                              new Display(this, POST, new ApprovalAction(this, start, validate));

        // Onboard, Detailed Edit Screens
        final Page onb = new Display(this, GET, new NsInfoForm(this, start)).page();
                         new Display(this, POST, new NsInfoAction(this, start, onb));

        // Web Command Screens
        /* final Page webCommand =*/ new Display(this, GET, new WebCommand(this, start)).page();

        // API Docs
        final Page apidocs = new Display(this, GET, new ApiDocs(this, start)).page();
                             new Display(this, GET, new ApiExample(this,start, apidocs)).page();

        // Permission Grant Page
        final Page permGrant =     new Display(this, GET, new PermGrantForm(this, start)).page();
                                 new Display(this, POST, new PermGrantAction(this, start, permGrant)).page();

        // Login Landing if no credentials detected
        final Page loginLanding = new Display(this, GET, new LoginLanding(this, start)).page();
                                  new Display(this, POST, new LoginLandingAction(this, start, loginLanding));

        // User Role Request Extend and Remove
        new Display(this, GET, new UserRoleExtend(this, start,myRoles)).page();
        new Display(this, GET, new UserRoleRemove(this, start,myRoles)).page();

        // See my Pending Requests
        final Page requestsShow = new Display(this, GET, new PendingRequestsShow(this, start)).page();
                                  new Display(this, GET, new RequestDetail(this, start, requestsShow));

        // Command line Mechanism
        route(env, PUT, "/gui/cui", new CUI(this),"text/plain;charset=utf-8","*/*");

        route(env, GET, "/gui/clear", new HttpCode<AuthzTrans, Void>(null, "Clear"){
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                trans.clearCache();
                Cookie cookies[] = req.getCookies();
                if(cookies!=null) {
                    for(Cookie c : cookies) {
                        if(c.getName().startsWith("aaf.gui.")) {
                            c.setMaxAge(0);
                            resp.addCookie(c);
                        }
                    }
                }
                resp.sendRedirect("/gui/home");
            }
        }, "text/plain;charset=utf-8","*/*");

        ///////////////////////
        // WebContent Handler
        ///////////////////////
        CachingFileAccess<AuthzTrans> cfa = new CachingFileAccess<AuthzTrans>(env);
        route(env,GET,"/theme/:key*", cfa);
        ///////////////////////
        aafCon = aafCon();
        lur = aafCon.newLur();
    }

    public<T> RosettaDF<T> getDF(Class<T> cls) throws APIException {
        return Cmd.getDF(env,cls);
    }

    public void writeError(AuthzTrans trans, Future<?> fp, HTMLGen hgen, int indent) {
        if (hgen!=null) {
            String msg = aafCon.readableErrMsg(fp);
            hgen.incr(HTMLGen.P,"style=text-indent:"+indent*10+"px")
                .text("<font color=\"red\"><i>Error</i>:</font> ")
                .text(msg)
                .end();
            trans.checkpoint(msg);
        }
    }

    public<RET> RET cmClientAsUser(TaggedPrincipal p,Retryable<RET> retryable) throws APIException, LocatorException, CadiException  {
            return cmCon.hman().best(new HTransferSS(p, APP, aafCon.securityInfo()), retryable);
    }

    @Override
    public Filter[] _filters(Object ... additionalTafLurs) throws CadiException, LocatorException {
        try {
            return new Filter[] {
                    new XFrameFilter(XFrameFilter.TYPE.none),
                    new AuthzTransFilter(env,aafCon(),
                            new AAFTrustChecker((Env)env),
                            additionalTafLurs),
                    new OrgLookupFilter()
                };
        } catch (NumberFormatException e) {
            throw new CadiException("Invalid Property information", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Registrant<AuthzEnv>[] registrants(final int port) throws CadiException, LocatorException {
        return new Registrant[] {
            new RemoteRegistrant<AuthzEnv>(aafCon(),port)
        };
    }

    public static void main(final String[] args) {
        try {
            Log4JLogIt logIt = new Log4JLogIt(args, "gui");
            PropAccess propAccess = new PropAccess(logIt,args);

            try {
                new JettyServiceStarter<AuthzEnv,AuthzTrans>(
                    new AAF_GUI(new AuthzEnv(propAccess)),true)
                        .start();
            } catch (Exception e) {
                propAccess.log(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
