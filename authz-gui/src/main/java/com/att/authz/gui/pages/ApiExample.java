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
import com.att.cadi.Symm;
import com.att.cadi.client.Future;
import com.att.inno.env.APIException;
import com.att.inno.env.Data.TYPE;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.Error;

/**
 * Detail Page for Permissions
 * 
 *
 */
public class ApiExample extends Page {
	public static final String HREF = "/gui/example/:tc";
	public static final String NAME = "APIExample";

	public ApiExample(final AuthGUI gui, Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env, NAME, HREF, 2/*backdots*/, new String[] {"API Code Example"},
				new BreadCrumbs(breadcrumbs),
				new Model()
				);
	}
	
	private static class Model extends NamedCode {
		private static final String WITH_OPTIONAL_PARAMETERS = "\n\n////////////\n  Data with Optional Parameters \n////////////\n\n";

		public Model() {
			super(false);
		}

		@Override
		public void code(Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
			Mark inner = xgen.divID("inner");
			xgen.divID("example","class=std");
			cache.dynamic(xgen, new DynamicCode<HTMLGen,AuthGUI,AuthzTrans>() {
				@Override
				public void code(final AuthGUI gui, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
					TimeTaken tt = trans.start("Code Example",Env.REMOTE);
					try {
						final String typecode;
						int prefix = trans.path().lastIndexOf('/')+1;
						String encoded = trans.path().substring(prefix);
						typecode = Symm.base64noSplit.decode(encoded);
						Future<String> fp = gui.client().read("/api/example/" + encoded,
								"application/Void+json"
								);
						Future<String> fs2;
						if(typecode.contains("Request+")) {
							fs2 = gui.client().read("/api/example/" + typecode+"?optional=true",
									"application/Void+json"
									);
						} else {
							fs2=null;
						}
						
						
						if(fp.get(5000)) {
								xgen.incr(HTMLGen.H1).text("Sample Code").end()
								.incr(HTMLGen.H5).text(typecode).end();
								xgen.incr("pre");
								if(typecode.contains("+xml")) {
									xgen.xml(fp.body());
									if(fs2!=null && fs2.get(5000)) {
										xgen.text(WITH_OPTIONAL_PARAMETERS);
										xgen.xml(fs2.body());
									}
								} else {
									xgen.text(fp.body());
									if(fs2!=null && fs2.get(5000)) {
										xgen.text(WITH_OPTIONAL_PARAMETERS);
										xgen.text(fs2.body());
									}
								}
								xgen.end();
						} else {
							Error err = gui.errDF.newData().in(TYPE.JSON).load(fp.body()).asObject();
							xgen.incr(HTMLGen.H3)
								.textCR(2,"Error from AAF Service")
								.end();
							
							xgen.p("Error Code: ",err.getMessageId())
								.p(err.getText())
								.end();
								
						}

					} catch (APIException e) {
						throw e;
					} catch (IOException e) {
						throw e;
					} catch (Exception e) {
						throw new APIException(e);
					}finally {
						tt.done();
					}
				}
					
			});
			xgen.end(inner);
		}
	}

}		
		