/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.cui;

import java.io.PrintWriter;
import java.security.Principal;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.cadi.config.Config;
import com.att.cadi.http.HTransferSS;
import com.att.cmd.AAFcli;
import com.att.cssa.rserv.HttpCode;

public class CUI extends HttpCode<AuthzTrans, Void> {
	private final AuthGUI gui;
	public CUI(AuthGUI gui) {
		super(null,"Command Line");
		this.gui = gui;
	}

	@Override
	public void handle(AuthzTrans trans, HttpServletRequest req,HttpServletResponse resp) throws Exception {
		ServletInputStream isr = req.getInputStream();
		PrintWriter pw = resp.getWriter();
		int c;
		StringBuilder cmd = new StringBuilder();

		while((c=isr.read())>=0) {
			cmd.append((char)c);
		}

		Principal p = trans.getUserPrincipal();
		trans.env().setProperty(Config.AAF_DEFAULT_REALM, trans.env().getProperty(Config.AAF_DEFAULT_REALM,Config.getDefaultRealm()));
		AAFcli aafcli = new AAFcli(trans.env(), pw, 
				gui.aafCon.hman(), 
				gui.aafCon.securityInfo(), new HTransferSS(p,AuthGUI.app, 
						gui.aafCon.securityInfo()));
	
		aafcli.verbose(false);
		aafcli.gui(true);
		String cmdStr = cmd.toString();
		if (!cmdStr.contains("--help")) {
			cmdStr = cmdStr.replaceAll("help", "--help");
		}
		if (!cmdStr.contains("--version")) {
			cmdStr = cmdStr.replaceAll("version", "--version");
		}
		try {
			aafcli.eval(cmdStr);
			pw.flush();
		} catch (Exception e) {
			pw.flush();
			pw.println(e.getMessage());
		} finally {
			aafcli.close();
		}
		
	}
}
