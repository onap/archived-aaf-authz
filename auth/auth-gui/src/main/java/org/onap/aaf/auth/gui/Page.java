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

import static org.onap.aaf.misc.xgen.html.HTMLGen.A;
import static org.onap.aaf.misc.xgen.html.HTMLGen.H1;
import static org.onap.aaf.misc.xgen.html.HTMLGen.LI;
import static org.onap.aaf.misc.xgen.html.HTMLGen.TITLE;
import static org.onap.aaf.misc.xgen.html.HTMLGen.UL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.gui.pages.Home;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.util.Split;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.CacheGen;
import org.onap.aaf.misc.xgen.Code;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLCacheGen;
import org.onap.aaf.misc.xgen.html.HTMLGen;
import org.onap.aaf.misc.xgen.html.Imports;

/**
 * A Base "Mobile First" Page 
 * 
 * @author Jonathan
 *
 */
public class Page extends HTMLCacheGen {
    public static final String AAFURL_TOOLS = "aaf_url.tools";
    public static final String AAF_URL_TOOL_DOT = "aaf_url.tool.";
    public static final String AAF_URL_CUIGUI = "aaf_url.cuigui"; // link to help
    public static final String AAF_URL_GUI_ONBOARD = "aaf_url.gui_onboard";
    public static final String AAF_URL_AAF_HELP = "aaf_url.aaf_help";
    public static final String AAF_URL_CADI_HELP = "aaf_url.cadi_help";
    public static final String PERM_CA_TYPE = "certman";
    public static final String PERM_NS = Define.ROOT_NS();

    public static enum BROWSER {iPhone,html5,ie,ieOld};

    public static final int MAX_LINE = 20;
    protected static final String[] NO_FIELDS = new String[0];
    private static final String BROWSER_TYPE = "BROWSER_TYPE";

    private final String bcName, bcUrl;
    private final String[] fields;

    public final boolean no_cache;

    // Note: Only access is synchronized in "getPerm"
    private final static Map<String,Map<String,Permission>> perms = new HashMap<>();

    public String name() {
        return bcName;
    }
    
    public String url() {
        return bcUrl;
    }
    
    public String[] fields() {
        return fields;
    }
    
    public Page(AuthzEnv env, String name, String url, Enum<?>[] en, final NamedCode ...content) throws APIException, IOException {
        super(CacheGen.PRETTY, new PageCode(env, 1, content));
        fields = new String[en.length];
        int i=-1;
        for (Enum<?> p : en) {
            fields[++i]=p.name();
        }

        bcName = name;
        bcUrl = url;
        // Mark which fields must be "no_cache"
        boolean no_cacheTemp=false;
        for (NamedCode nc : content) {
            if (nc.no_cache()) { 
                no_cacheTemp=true;
                break;
            }
        }
        no_cache=no_cacheTemp;
    }
    public Page(AuthzEnv env, String name, String url, String [] fields, final NamedCode ... content) throws APIException,IOException {
        this(env,name,url,1,fields,content);
    }
    
    public Page(AuthzEnv env, String name, String url, int backdots, String [] fields, final NamedCode ... content) throws APIException,IOException {
        super(CacheGen.PRETTY, new PageCode(env, backdots, content));
        if (fields==null) {
            this.fields = new String[0];
        } else {
            this.fields = fields;
        }
        bcName = name;
        bcUrl = url;
        // Mark which fields must be "no_cache"
        boolean no_cacheTemp=false;
        for (NamedCode nc : content) {
            if (nc.no_cache()) { 
                no_cacheTemp=true;
                break;
            }
        }
        no_cache=no_cacheTemp;
    }
    
    
    private static class PageCode implements Code<HTMLGen> {
            private static final String AAF_GUI_TITLE = "aaf_gui_title";
            
            private final ContentCode[] content;
            private final Slot browserSlot;
            private final int backdots;
            protected AuthzEnv env;
            private StaticSlot sTheme;
        	private static Map<String,List<String>> themes;
        	private static Map<String,Properties> themeProps;

            public PageCode(AuthzEnv env, int backdots, final ContentCode[] content) {
                this.content = content;
                this.backdots = backdots;
                browserSlot = env.slot(BROWSER_TYPE);
                sTheme = env.staticSlot(AAF_GUI.AAF_GUI_THEME);
                this.env = env;
            }

            private static synchronized List<String> getThemeFiles(Env env, String theme) {
            	if(themes==null) {
            		themes = new TreeMap<>();
                    File themeD = new File("theme");
                    if(themeD.exists() && themeD.isDirectory()) {
                    	for (File t : themeD.listFiles()) {
                    		if(t.isDirectory()) {
                    			List<String> la = new ArrayList<>();
                    			for(File f : t.listFiles()) {
                    				if(f.isFile()) {
                    					if(f.getName().endsWith(".props")) {
                    						Properties props;
                    						if(themeProps == null) {
                    							themeProps = new TreeMap<>();
                    							props = null;
                    						} else {
                    							props = themeProps.get(theme);
                    						}
                    						if(props==null) {
                    							props = new Properties();
                    							themeProps.put(theme, props);
                    						}
                    						
                    						try {
	                    						FileInputStream fis = new FileInputStream(f);
	                    						try {
	                    							props.load(fis);
	                    						} finally {
	                    							fis.close();
	                    						}
                    						} catch (IOException e) {
                    							env.error().log(e);
                    						}
                    					} else {
                    						la.add(f.getName());
                    					}
                    				}
                    			}
                				themes.put(t.getName(),la);
                    		}
                    	}
                    }
            	}
            	return themes.get(theme);
            }
            
            protected Imports getImports(Env env, Holder<String> theme, String defaultTheme, int backdots, BROWSER browser) {
            	List<String> ls = getThemeFiles(env,theme.get());
            	Imports imp = new Imports(backdots);
            	if(ls==null) {
            		theme.set(defaultTheme);
            	}
        		String prefix = "theme/" + theme.get() + '/';
        		for(String f : ls) {
            		if(f.endsWith(".js")) {
            			imp.js(prefix + f);
            		} else if(f.endsWith(".css")) {
            			if(f.endsWith("iPhone.css")) {
            				if(BROWSER.iPhone.equals(browser)) {
            					imp.css(prefix + f);
            				}
            			} else if (f.endsWith("Desktop.css")){
            				if(!BROWSER.iPhone.equals(browser)) {
            					imp.css(prefix + f);
            				}
            			// Make Console specific to Console page
            			} else if (!"console.js".equals(f)) {
            				imp.css(prefix + f);
            			}
            		}
            	}
            	return imp;
            }
            
            @Override
            public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                // Note: I found that App Storage saves everything about the page, or not.  Thus, if you declare the page uncacheable, none of the 
                // Artifacts, like JPGs are stored, which makes this feature useless for Server driven elements
                cache.dynamic(hgen,  new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                    @Override
                    public void code(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
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
                final String title = env.getProperty(AAF_GUI_TITLE,"Authentication/Authorization Framework");
                final String defaultTheme = env.get(sTheme,"onap"); 
                final Holder<String> hTheme = new Holder<>(defaultTheme);
              
                Mark head = hgen.head();
                    hgen.leaf(TITLE).text(title).end();
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                        @Override
                        public void code(AAF_GUI state, AuthzTrans trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                        	BROWSER browser = browser(trans,browserSlot);  
                        	Cookie[] cookies = trans.hreq().getCookies();
                        	if(cookies!=null) {
                        		for(Cookie c : cookies) {
                        			if("aaf_theme".equals(c.getName())) {
                        				hTheme.set(c.getValue());
                        			}
                        		}
                        	}
                            hgen.imports(getImports(env,hTheme,defaultTheme,backdots,browser));
                            switch(browser) {
                                case ie:
                                case ieOld:
                                    hgen.js().text("document.createElement('header');")
                                            .text("document.createElement('nav');")
                                            .done();
                                    break;
                                default:
                            }
                            
                        }
                    });
                    hgen.end(head);
                    
                Mark body = hgen.body();
                    Mark header = hgen.header();
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                        @Override
                        public void code(AAF_GUI state, AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen xgen)
                                throws APIException, IOException {
                            // Obtain Server Info, and print
                            // AT&T Only
                            String env = trans.getProperty(Config.AAF_ENV,"N/A");
                            xgen.leaf(H1).text(title + " on " + env).end();
                            xgen.leaf("p","id=version").text("AAF Version: " + state.deployedVersion).end();
                            
                            // Obtain User Info, and print
                            TaggedPrincipal p = trans.getUserPrincipal();
                            String user,secured;
                            if (p==null) {
                                user = "please choose a Login Authority";
                                secured = "NOT Secure!";
                            } else {
                                user = p.personalName();
                                secured = p.tag();
                            }
                            xgen.leaf("p","id=welcome").text("Welcome, ")
                                .text(user)
                                .text("<sup>")
                                .text(secured)
                                .text("</sup>").end();
                            
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
                    ContentCode nc;
                    // If BreadCrumbs, put here
                    if (content.length>0 && content[0] instanceof BreadCrumbs) {
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
                        for (int i=cIdx;i<content.length;++i) {
                            nc = content[i];
                            Mark ctnt = hgen.divID(nc.idattrs());
                            nc.code(cache, hgen);
                            hgen.end(ctnt);
                        }

                    hgen.end(inner);    
                    
                    // Navigation - Using older Nav to work with decrepit   IE versions
                    
                    Mark nav = hgen.divID("nav");
                    cache.dynamic(hgen, new DynamicCode<HTMLGen,AAF_GUI,AuthzTrans>() {
                        @Override
                        public void code(AAF_GUI state, AuthzTrans trans,Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
                        	Properties props = themeProps==null?null:themeProps.get(hTheme.get());
                        	if(props!=null && "TRUE".equalsIgnoreCase(props.getProperty("main_menu_in_nav"))) {
                                xgen.incr("h2").text("Navigation").end();
                                Mark mark = new Mark();
                            	boolean selected = isSelected(trans.path(),Home.HREF);
                            			//trans.path().endsWith("home");
                                xgen.incr(mark,HTMLGen.UL)
                            		.incr(HTMLGen.LI,selected?"class=selected":"")
                            		.incr(HTMLGen.A, "href=home")
                            		.text("Home")
                            		.end(2);
                                boolean noSelection = !selected;
                                for(String[] mi : Home.MENU_ITEMS) {
                                	//selected = trans.path().endsWith(mi[0]);
                                	if(noSelection) {
                                		selected = isSelected(trans.path(),mi[2]);
                                		noSelection = !selected;
                                	} else {
                                		selected = false;
                                	}
                                	xgen.incr(HTMLGen.LI,selected?"class=selected":"")
                                	    .incr(HTMLGen.A, "href="+mi[0])
                                	    .text(mi[1])
                                	    .end(2);
                                }
                                xgen.end(mark);
                        	}
                        }

						private boolean isSelected(String path, String item) {
							if(item.equals(path)) {
								return true;
							} else {
								for(ContentCode c : content) {
									if(c instanceof BreadCrumbs) {
										Page[] bc = ((BreadCrumbs)c).breadcrumbs;
										if(bc!=null) {
											for(int i = bc.length-1;i>0;--i) {
												if(bc[i].url().equals(item)) {
													return true;
												}
											}
											return false;
										}
									}
								}
							}
							return false;
						}
                    });
                    hgen.incr("h2").text("Related Links").end();
                    hgen.incr(UL);
                    String aaf_help = env.getProperty(AAF_URL_AAF_HELP,null);
                    if (aaf_help!=null) {
                        hgen.leaf(LI).leaf(A,"href="+env.getProperty(AAF_URL_AAF_HELP),"target=_blank").text("AAF WIKI").end(2);
                        String sub = env.getProperty(AAF_URL_AAF_HELP+".sub");
                        if (sub!=null) {
                            hgen.incr(UL,"style=margin-left:5%");
                            for (String s : Split.splitTrim(',', sub)) {
                                hgen.leaf(LI).leaf(A,"href="+env.getProperty(AAF_URL_AAF_HELP+".sub."+s),"target=_blank").text(s.replace('+', ' ')).end(2);
                            }
                            hgen.end();
                        }
                    }
                    aaf_help = env.getProperty(AAF_URL_CADI_HELP,null);
                    if (aaf_help!=null) {
                        hgen.leaf(LI).leaf(A,"href="+aaf_help,"target=_blank").text("CADI WIKI").end(2);
                    }
                    String tools = env.getProperty(AAFURL_TOOLS);
                    if (tools!=null) {
                        hgen.hr()
                            .incr(HTMLGen.UL,"style=margin-left:5%")
                             .leaf(HTMLGen.H3).text("Related Tools").end();

                        for (String tool : Split.splitTrim(',',tools)) {
                            hgen.leaf(LI).leaf(A,"href="+env.getProperty(AAF_URL_TOOL_DOT+tool),"target=_blank").text(tool.replace('+', ' ')).end(2);
                        }
                        hgen.end();
                    }
                    hgen.end();
                    
                    hgen.hr();
                    
                    hgen.end(nav);
                    // Footer - Using older Footer to work with decrepit IE versions
                    Mark footer = hgen.divID("footer");
                        hgen.textCR(1, env.getProperty(AAF_GUI.AAF_GUI_COPYRIGHT))
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
     *         Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; 
     *         .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)
     * Official IE 8
     *         Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; 
     *         .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; ATT)
     * 
     * IE 11 Compatibility
     *         Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; SLCC2; .NET CLR 2.0.50727; 
     *         .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET CLR 1.1.4322; .NET4.0C; .NET4.0E; InfoPath.3; HVD; ATT)
     * 
     * IE 11 (not Compatiblity)
     *         Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; 
     *         .NET CLR 3.5.30729; .NET CLR 3.0.30729;    Media Center PC 6.0; .NET CLR 1.1.4322; .NET4.0C; .NET4.0E; InfoPath.3; HVD; ATT)
     * 
     * @param trans
     * @return
     */
    public static BROWSER browser(AuthzTrans trans, Slot slot) {
        BROWSER br = trans.get(slot, null);
        if (br==null) {
            String agent = trans.agent();
            int msie; 
            if (agent.contains("iPhone") /* other phones? */) {
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
    
    /*
     * Get, rather than create each time, permissions for validations
     */
    protected static synchronized Permission getPerm(String instance, String action) {
        Map<String,Permission> msp = perms.get(instance);
        Permission p;
        if (msp==null) {
            msp = new HashMap<>();
            perms.put(instance, msp);
            p=null;
        } else {
            p = msp.get(instance);
        }
        if (p==null) {
            p=new AAFPermission(PERM_NS, PERM_CA_TYPE,instance,action);
            msp.put(action, p);
        }
        return p;
     }

    protected static String getSingleParam(HttpServletRequest req, String tag) {
        String values[] = req.getParameterValues(tag);
        return values.length<1?null:values[0];
    }

}

