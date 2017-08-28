/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import java.io.IOException;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Slot;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.html.HTMLGen;

public class LoginLandingAction extends Page {
	public LoginLandingAction(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,"Login",LoginLanding.HREF, LoginLanding.fields,
			new BreadCrumbs(breadcrumbs),
			new NamedCode(true,"content") {
				final Slot sID = gui.env.slot(LoginLanding.NAME+'.'+LoginLanding.fields[0]);
//				final Slot sPassword = gui.env.slot(LoginLanding.NAME+'.'+LoginLanding.fields[1]);
				
				@Override
				public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
					cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI, AuthzTrans>() {
						@Override
						public void code(final AuthGUI gui, final AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
							String username = trans.get(sID,null);
//							String password = trans.get(sPassword,null);

							hgen.p("User: "+username);
							hgen.p("Pass: ********");
							
							// TODO: clarification from JG
							// put in request header?
							// then pass through authn/basicAuth call?
							
						}
					});
				}
		});
	}
}
