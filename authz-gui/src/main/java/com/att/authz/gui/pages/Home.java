/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;

import static com.att.xgen.html.HTMLGen.A;
import static com.att.xgen.html.HTMLGen.H3;

import java.io.IOException;

import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.inno.env.APIException;
import com.att.xgen.Cache;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;


public class Home extends Page {
	public static final String HREF = "/gui/home";
	public Home(final AuthGUI gui) throws APIException, IOException {
		super(gui.env,"Home",HREF, NO_FIELDS, new NamedCode(false,"content") {
			@Override
			public void code(final Cache<HTMLGen> cache, final HTMLGen xgen) throws APIException, IOException {
//				// TEMP
//				JSGen jsg = xgen.js();
//				jsg.function("httpPost","sURL","sParam")
//					.text("var oURL = new java.net.URL(sURL)")
//					.text("var oConn = oURL.openConnection();")
//					.text("oConn.setDoInput(true);")
//					.text("oConn.setDoOutpu(true);")
//					.text("oConn.setUseCaches(false);")
//					.text("oConn.setRequestProperty(\"Content-Type\",\"application/x-www-form-urlencoded\");")
//					.text(text)
//				jsg.done();
				// TEMP
				final Mark pages = xgen.divID("Pages");
				xgen.leaf(H3).text("Choose from the following:").end()
					.leaf(A,"href=myperms").text("My Permissions").end()
					.leaf(A,"href=myroles").text("My Roles").end()
				//	TODO: uncomment when on cassandra 2.1.2 for MyNamespace GUI page
					.leaf(A,"href=mynamespaces").text("My Namespaces").end()
					.leaf(A,"href=approve").text("My Approvals").end()
					.leaf(A, "href=myrequests").text("My Pending Requests").end()
					// Enable later
//					.leaf(A, "href=onboard").text("Onboarding").end()
				// Password Change.  If logged in as CSP/GSO, go to their page
					.leaf(A,"href=passwd").text("Password Management").end()
					.leaf(A,"href=cui").text("Command Prompt").end()
					.leaf(A,"href=api").text("AAF API").end()
					;
				
				xgen.end(pages);
			}
		});
	}

}
