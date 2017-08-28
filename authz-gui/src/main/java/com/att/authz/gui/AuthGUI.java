/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import static com.att.cssa.rserv.HttpMethods.GET;
import static com.att.cssa.rserv.HttpMethods.POST;
import static com.att.cssa.rserv.HttpMethods.PUT;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.api.DME2Server;
import com.att.aft.dme2.api.DME2ServerProperties;
import com.att.aft.dme2.api.DME2ServiceHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder;
import com.att.aft.dme2.api.util.DME2FilterHolder.RequestDispatcherType;
import com.att.aft.dme2.api.util.DME2ServletHolder;
import com.att.authz.common.Define;
import com.att.authz.cui.CUI;
import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.env.AuthzTransFilter;
import com.att.authz.env.AuthzTransOnlyFilter;
import com.att.authz.gui.pages.ApiDocs;
import com.att.authz.gui.pages.ApiExample;
import com.att.authz.gui.pages.ApprovalAction;
import com.att.authz.gui.pages.ApprovalForm;
import com.att.authz.gui.pages.Home;
import com.att.authz.gui.pages.LoginLanding;
import com.att.authz.gui.pages.LoginLandingAction;
import com.att.authz.gui.pages.NsDetail;
import com.att.authz.gui.pages.NsHistory;
import com.att.authz.gui.pages.NsInfoAction;
import com.att.authz.gui.pages.NsInfoForm;
import com.att.authz.gui.pages.NssShow;
import com.att.authz.gui.pages.PassChangeAction;
import com.att.authz.gui.pages.PassChangeForm;
import com.att.authz.gui.pages.PendingRequestsShow;
import com.att.authz.gui.pages.PermDetail;
import com.att.authz.gui.pages.PermGrantAction;
import com.att.authz.gui.pages.PermGrantForm;
import com.att.authz.gui.pages.PermHistory;
import com.att.authz.gui.pages.PermsShow;
import com.att.authz.gui.pages.RequestDetail;
import com.att.authz.gui.pages.RoleDetail;
import com.att.authz.gui.pages.RoleHistory;
import com.att.authz.gui.pages.RolesShow;
import com.att.authz.gui.pages.UserRoleExtend;
import com.att.authz.gui.pages.UserRoleRemove;
import com.att.authz.gui.pages.WebCommand;
import com.att.authz.org.OrganizationFactory;
import com.att.authz.server.AbsServer;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.v2_0.AAFTrustChecker;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import com.att.cssa.rserv.CachingFileAccess;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.Slot;
import org.onap.aaf.rosetta.env.RosettaDF;
import com.att.xgen.html.HTMLGen;
import com.att.xgen.html.State;

import aaf.v2_0.Api;
import aaf.v2_0.Approvals;
import aaf.v2_0.CredRequest;
import aaf.v2_0.Error;
import aaf.v2_0.History;
import aaf.v2_0.Nss;
import aaf.v2_0.Perms;
import aaf.v2_0.RolePermRequest;
import aaf.v2_0.Roles;
import aaf.v2_0.UserRoles;
import aaf.v2_0.Users;

public class AuthGUI extends AbsServer implements State<Env>{
	public static final int TIMEOUT = 60000;
	public static final String app = "AAF GUI";
	
	public RosettaDF<Perms> permsDF;
	public RosettaDF<Roles> rolesDF;
	public RosettaDF<Users> usersDF;
	public RosettaDF<UserRoles> userrolesDF;
	public RosettaDF<CredRequest> credReqDF;
	public RosettaDF<RolePermRequest> rolePermReqDF;
	public RosettaDF<Approvals> approvalsDF;
	public RosettaDF<Nss> nssDF;
	public RosettaDF<Api> apiDF;
	public RosettaDF<Error> errDF;
	public RosettaDF<History> historyDF;

	public final AuthzEnv env;
	public final Slot slot_httpServletRequest;

	public AuthGUI(final AuthzEnv env) throws CadiException, GeneralSecurityException, IOException, APIException {
		super(env,app);
		this.env = env;
		
		env.setLog4JNames("log4j.properties","authz","gui","audit","init","trace ");
		OrganizationFactory.setDefaultOrg(env, "com.att.authz.org.att.ATT");


		slot_httpServletRequest = env.slot("HTTP_SERVLET_REQUEST");
		
		permsDF = env.newDataFactory(Perms.class);
		rolesDF = env.newDataFactory(Roles.class);
//			credsDF = env.newDataFactory(Cred.class);
		usersDF = env.newDataFactory(Users.class);
		userrolesDF = env.newDataFactory(UserRoles.class);
		credReqDF = env.newDataFactory(CredRequest.class);
		rolePermReqDF = env.newDataFactory(RolePermRequest.class);
		approvalsDF = env.newDataFactory(Approvals.class);
		nssDF = env.newDataFactory(Nss.class);
		apiDF = env.newDataFactory(Api.class);
		errDF   = env.newDataFactory(Error.class);
		historyDF = env.newDataFactory(History.class);

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
							new Display(this, GET, new RoleHistory(this,start,myRoles,roleDetail));
							
		// MyNameSpace
		final Page myNamespaces = new Display(this, GET, new NssShow(this, start)).page();
		Page nsDetail = new Display(this, GET, new NsDetail(this, start, myNamespaces)).page();
			   		  	new Display(this, GET, new NsHistory(this, start,myNamespaces,nsDetail));
							 
		// Password Change Screens
		final Page pwc = new Display(this, GET, new PassChangeForm(this, start)).page();
						 new Display(this, POST, new PassChangeAction(this, start, pwc));

		// Validation Change Screens
		final Page validate = new Display(this, GET, new ApprovalForm(this, start)).page();
							  new Display(this, POST, new ApprovalAction(this, start, validate));
							
		// Onboard, Detailed Edit  Screens
		final Page onb = new Display(this, GET, new NsInfoForm(this, start)).page();
						 new Display(this, POST, new NsInfoAction(this, start, onb));

		// Web Command Screens
		/* final Page webCommand =*/ new Display(this, GET, new WebCommand(this, start)).page();
		
		// API Docs
		final Page apidocs = new Display(this, GET, new ApiDocs(this, start)).page();
							 new Display(this, GET, new ApiExample(this,start, apidocs)).page();
		
		// Permission Grant Page
		final Page permGrant = 	new Display(this, GET, new PermGrantForm(this, start)).page();
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
		
		///////////////////////  
		// WebContent Handler
		///////////////////////
		route(env,GET,"/theme/:key", new CachingFileAccess<AuthzTrans>(env,
				CachingFileAccess.CFA_WEB_DIR,"theme"));
		///////////////////////
	}
	
	public static void main(String[] args) {
		setup(AuthGUI.class, "authGUI.props");
	}

	/**
	 * Start up AuthzAPI as DME2 Service
	 * @param env
	 * @param props
	 * @throws DME2Exception
	 * @throws CadiException 
	 */
	public void startDME2(Properties props) throws DME2Exception, CadiException {
		
		DME2Manager dme2 = new DME2Manager("AAF GUI DME2Manager", props);
        DME2ServiceHolder svcHolder;
        List<DME2ServletHolder> slist = new ArrayList<DME2ServletHolder>();
        svcHolder = new DME2ServiceHolder();
        String serviceName = env.getProperty("DMEServiceName",null);
    	if(serviceName!=null) {
	    	svcHolder.setServiceURI(serviceName);
	        svcHolder.setManager(dme2);
	        svcHolder.setContext("/");
	        
	        
	        DME2ServletHolder srvHolder = new DME2ServletHolder(this, new String[]{"/gui"});
	        srvHolder.setContextPath("/*");
	        slist.add(srvHolder);
	        
	        EnumSet<RequestDispatcherType> edlist = EnumSet.of(
	        		RequestDispatcherType.REQUEST,
	        		RequestDispatcherType.FORWARD,
	        		RequestDispatcherType.ASYNC
	        		);

	        ///////////////////////
	        // Apply Filters
	        ///////////////////////
	        List<DME2FilterHolder> flist = new ArrayList<DME2FilterHolder>();
	        
	        // Secure all GUI interactions with AuthzTransFilter
	        flist.add(new DME2FilterHolder(new AuthzTransFilter(env, aafCon, new AAFTrustChecker(
	        		env.getProperty(Config.CADI_TRUST_PROP, Config.CADI_USER_CHAIN),
	        		Define.ROOT_NS + ".mechid|"+Define.ROOT_COMPANY+"|trust"
	        	)),"/gui/*", edlist));
	        
	        // Don't need security for display Artifacts or login page
	        AuthzTransOnlyFilter atof;
	        flist.add(new DME2FilterHolder(atof =new AuthzTransOnlyFilter(env),"/theme/*", edlist));
	        flist.add(new DME2FilterHolder(atof,"/js/*", edlist));
	        flist.add(new DME2FilterHolder(atof,"/login/*", edlist));

	        svcHolder.setFilters(flist);
	        svcHolder.setServletHolders(slist);
	        
	        DME2Server dme2svr = dme2.getServer();
//	        dme2svr.setGracefulShutdownTimeMs(1000);
	
	        env.init().log("Starting AAF GUI with Jetty/DME2 server...");
	        dme2svr.start();
	        DME2ServerProperties dsprops = dme2svr.getServerProperties();
	        try {
//	        	if(env.getProperty("NO_REGISTER",null)!=null)
	        	dme2.bindService(svcHolder);
	        	env.init().log("DME2 is available as HTTP"+(dsprops.isSslEnable()?"/S":""),"on port:",dsprops.getPort());

	            while(true) { // Per DME2 Examples...
	            	Thread.sleep(5000);
	            }
	        } catch(InterruptedException e) {
	            env.init().log("AAF Jetty Server interrupted!");
	        } catch(Exception e) { // Error binding service doesn't seem to stop DME2 or Process
	            env.init().log(e,"DME2 Initialization Error");
	        	dme2svr.stop();
	        	System.exit(1);
	        }
    	} else {
    		env.init().log("Properties must contain DMEServiceName");
    	}
	}


	public AuthzEnv env() {
		return env;
	}

	/**
	 * Derive API Error Class from AAF Response (future)
	 */
	public Error getError(AuthzTrans trans, Future<?> fp) {
//		try {
			String text = fp.body();
			Error err = new Error();
			err.setMessageId(Integer.toString(fp.code()));
			if(text==null || text.length()==0) {
				err.setText("**No Message**");
			} else {
				err.setText(fp.body());
			}
			return err;
//		} catch (APIException e) {
//			Error err = new Error();
//			err.setMessageId(Integer.toString(fp.code()));
//			err.setText("Could not obtain response from AAF Message: " + e.getMessage());
//			return err;
//		}
	}

	public void writeError(AuthzTrans trans, Future<?> fp, HTMLGen hgen) {
		Error err = getError(trans,fp);

		String messageBody = err.getText();
		List<String> vars = err.getVariables();
		for (int varCounter=0;varCounter<vars.size();) {
			String var = vars.get(varCounter++);
			if (messageBody.indexOf("%" + varCounter) >= 0) {
				messageBody = messageBody.replace("%" + varCounter, var);
			}
		}

		String msg = "[" + err.getMessageId() + "] " + messageBody;
		if(hgen!=null) {
			hgen.text(msg);
		}
		trans.checkpoint("AAF Error: " + msg);
	}

}
