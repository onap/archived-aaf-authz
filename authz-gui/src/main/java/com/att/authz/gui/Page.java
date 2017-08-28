/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import static com.att.xgen.html.HTMLGen.A;
import static com.att.xgen.html.HTMLGen.H1;
import static com.att.xgen.html.HTMLGen.LI;
import static com.att.xgen.html.HTMLGen.TITLE;
import static com.att.xgen.html.HTMLGen.UL;

import java.io.IOException;
import java.security.Principal;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Slot;
import org.onap.aaf.inno.env.util.Split;
import com.att.xgen.Cache;
import com.att.xgen.CacheGen;
import com.att.xgen.Code;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLCacheGen;
import com.att.xgen.html.HTMLGen;
import com.att.xgen.html.Imports;

/**
 * A Base "Mobile First" Page 
 * 
 *
 */
public class Page extends HTMLCacheGen {
	public static enum BROWSER {iPhone,html5,ie,ieOld};
	
	public static final int MAX_LINE=20;

	protected static final String[] NO_FIELDS = new String[0];

	private static final String ENV_CONTEXT = "envContext";
	private static final String DME_SERVICE_NAME = "DMEServiceName";
	private static final String ROUTE_OFFER = "routeOffer";
	private static final String BROWSER_TYPE = "BROWSER_TYPE";

	private final String bcName, bcUrl;
	private final String[] fields;

	public final boolean no_cache;

	public String name() {
		return bcName;
	}
	
	public String url() {
		return bcUrl;
	}
	
	public String[] fields() {
		return fields;
	}
	
	public Page(AuthzEnv env, String name, String url, String [] fields, final NamedCode ... content) throws APIException,IOException {
		this(env,name,url,1,fields,content);
	}
	
	public Page(AuthzEnv env, String name, String url, int backdots, String [] fields, final NamedCode ... content) throws APIException,IOException {
		super(CacheGen.PRETTY, new PageCode(env, backdots, content));
		bcName = name;
		bcUrl = url;
		this.fields = fields;
		// Mark which fields must be "no_cache"
		boolean no_cacheTemp=false;
		for(NamedCode nc : content) {
			if(nc.no_cache) { 
				no_cacheTemp=true;
				break;
			}
		}
		no_cache=no_cacheTemp;
	}
	
	private static class PageCode implements Code<HTMLGen> {
			private final NamedCode[] content;
			private final Slot browserSlot;
			private final int backdots;
			protected AuthzEnv env;

			public PageCode(AuthzEnv env, int backdots, final NamedCode[] content) {
				this.content = content;
				this.backdots = backdots;
				browserSlot = env.slot(BROWSER_TYPE);
				this.env = env;
			}
			
			@Override
			public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
				// Note: I found that App Storage saves everything about the page, or not.  Thus, if you declare the page uncacheable, none of the 
				// Artifacts, like JPGs are stored, which makes this feature useless for Server driven elements
				//hgen.html("manifest=../theme/aaf.appcache");
				cache.dynamic(hgen,  new DynamicCode<HTMLGen,AuthGUI,AuthzTrans>() {
					@Override
					public void code(AuthGUI state, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
						switch(browser(trans,browserSlot)) {
							case ieOld:
							case ie:
								hgen.directive("!DOCTYPE html");
								hgen.directive("meta", "http-equiv=X-UA-Compatible","content=IE=11");
							default:
						}
					}
				});
				hgen.html();
				Mark head = hgen.head();
					hgen.leaf(TITLE).text("AT&amp;T Authentication/Authorization Tool").end();
					hgen.imports(new Imports(backdots).css("theme/aaf5.css")
								 			   	.js("theme/comm.js")
												.js("theme/console.js")
												.js("theme/common.js"));
					cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI,AuthzTrans>() {
						@Override
						public void code(AuthGUI state, AuthzTrans trans, Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
							switch(browser(trans,browserSlot)) {
								case iPhone:
									hgen.imports(new Imports(backdots).css("theme/aaf5iPhone.css"));
									break;
								case ie:
								case ieOld:
									hgen.js().text("document.createElement('header');")
											.text("document.createElement('nav');")
											.done();
								case html5:
									hgen.imports(new Imports(backdots).css("theme/aaf5Desktop.css"));
									break;
							}
						}
					});
					hgen.end(head);
					
				Mark body = hgen.body();
					Mark header = hgen.header();
					cache.dynamic(hgen, new DynamicCode<HTMLGen,AuthGUI,AuthzTrans>() {
						@Override
						public void code(AuthGUI state, AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen xgen)
								throws APIException, IOException {
							// Obtain Server Info, and print
							String DMEServiceName = trans.getProperty(DME_SERVICE_NAME);
							String env = DMEServiceName.substring(
									DMEServiceName.indexOf(ENV_CONTEXT),
									DMEServiceName.indexOf(ROUTE_OFFER) -1).split("=")[1];
							
							xgen.leaf(H1).text("AT&amp;T Auth Tool on " + env).end();
							xgen.leaf("p","id=version").text("AAF Version: " + trans.getProperty(Config.AAF_DEPLOYED_VERSION, "N/A")).end();
							
							// Obtain User Info, and print
							Principal p = trans.getUserPrincipal();
							String user;
							if(p==null) {
								user = "please choose a Login Authority";
							} else {
								user = p.getName();
							}
							xgen.leaf("p","id=welcome").text("Welcome, " + user).end();
							
							switch(browser(trans,browserSlot)) {
								case ieOld:
								case ie:
									xgen.incr("h5").text("This app is Mobile First HTML5.  Internet Explorer " 
											+ " does not support all HTML5 standards. Old, non TSS-Standard versions may not function correctly.").br()
											.text("  For best results, use a highly compliant HTML5 browser like Firefox.")
										.end();
									break;
								default:
							}
						}
					});
					
					hgen.hr();
					
					int cIdx;
					NamedCode nc;
					// If BreadCrumbs, put here
					if(content.length>0 && content[0] instanceof BreadCrumbs) {
						nc = content[0];
						Mark ctnt = hgen.divID(nc.idattrs());
						nc.code(cache, hgen);
						hgen.end(ctnt);
						cIdx = 1;
					} else {
						cIdx = 0;
					}
					
					hgen.end(header);
					
					Mark inner = hgen.divID("inner");
						// Content
						for(int i=cIdx;i<content.length;++i) {
							nc = content[i];
							Mark ctnt = hgen.divID(nc.idattrs());
							nc.code(cache, hgen);
							hgen.end(ctnt);
						}

					hgen.end(inner);	
					
					// Navigation - Using older Nav to work with decrepit  IE versions
					
					Mark nav = hgen.divID("nav");
					hgen.incr("h2").text("Related Links").end();
					hgen.incr(UL)
						 .leaf(LI).leaf(A,"href="+env.getProperty("aaf_url.aaf_help")).text("AAF WIKI").end(2)
						 .leaf(LI).leaf(A,"href="+env.getProperty("aaf_url.cadi_help")).text("CADI WIKI").end(2);
						String tools = env.getProperty("aaf_tools");
						if(tools!=null) {
							hgen.hr()
								.incr(HTMLGen.UL,"style=margin-left:5%")
							 	.leaf(HTMLGen.H3).text("Related Tools").end();

							for(String tool : Split.splitTrim(',',tools)) {
								hgen.leaf(LI).leaf(A,"href="+env.getProperty("aaf_url.tool."+tool)).text(tool.toUpperCase() + " Help").end(2);
							}
							hgen.end();
						}
						 hgen.end();
					
					hgen.hr();
					
					hgen.end(nav);
					// Footer - Using older Footer to work with decrepit IE versions
					Mark footer = hgen.divID("footer");
						hgen.textCR(1, "(c) 2014-6 AT&amp;T Inc. All Rights Reserved")
						.end(footer);
						
					hgen.end(body);
				hgen.endAll();
		}
	}

	public static String getBrowserType() {
		return BROWSER_TYPE;
	}
	
	/**
	 * It's IE if int >=0
	 * 
	 * Use int found in "ieVersion"
	 * 
	 * Official IE 7
	 * 		Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; 
	 * 		.NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)
	 * Official IE 8
	 * 		Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; 
	 * 		.NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; ATT)
	 * 
	 * IE 11 Compatibility
	 * 		Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; 
	 * 		.NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET CLR 1.1.4322; .NET4.0C; .NET4.0E; InfoPath.3; HVD; ATT)
	 * 
	 * IE 11 (not Compatiblity)
	 * 		Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; 
	 * 		.NET CLR 3.5.30729; .NET CLR 3.0.30729;	Media Center PC 6.0; .NET CLR 1.1.4322; .NET4.0C; .NET4.0E; InfoPath.3; HVD; ATT)
	 * 
	 * @param trans
	 * @return
	 */
	public static BROWSER browser(AuthzTrans trans, Slot slot) {
		BROWSER br = trans.get(slot, null);
		if(br==null) {
			String agent = trans.agent();
			int msie; 
			if(agent.contains("iPhone") /* other phones? */) {
				br=BROWSER.iPhone;
			} else if ((msie = agent.indexOf("MSIE"))>=0) {
				msie+=5;
				int end = agent.indexOf(";",msie);
				float ver;
				try {
					ver = Float.valueOf(agent.substring(msie,end));
					br = ver<8f?BROWSER.ieOld:BROWSER.ie;
				} catch (Exception e) {
					br = BROWSER.ie;
				}
			} else {
				br = BROWSER.html5;
			}
			trans.put(slot,br);
		}
		return br;
	}
}

