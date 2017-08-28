/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui.pages;


import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.authz.gui.Table;
import com.att.authz.gui.Table.Cells;
import com.att.authz.gui.table.AbsCell;
import com.att.authz.gui.table.RefCell;
import com.att.authz.gui.table.TextCell;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.Slot;
import org.onap.aaf.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.History;
import aaf.v2_0.History.Item;


public class PermHistory extends Page {
	static final String NAME="PermHistory";
	static final String HREF = "/gui/permHistory";
	static final String FIELDS[] = {"type","instance","action","dates"};
	static final String WEBPHONE = "http://webphone.att.com/cgi-bin/webphones.pl?id=";
	static enum Month { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, 
		AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER };
	
	public PermHistory(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME,HREF, FIELDS,
			new BreadCrumbs(breadcrumbs),
			new Table<AuthGUI,AuthzTrans>("History", gui.env.newTransNoAvg(),new Model(gui.env()),"class=std"),
			new NamedCode(true, "content") {
				@Override
				public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
					final Slot sType = gui.env.slot(NAME+".type");
					final Slot sInstance = gui.env.slot(NAME+".instance");
					final Slot sAction = gui.env.slot(NAME+".action");
					cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
						@Override
						public void code(AuthGUI gui, AuthzTrans trans,	Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {
							String type = trans.get(sType, null);
							String instance = trans.get(sInstance,null);
							String action = trans.get(sAction,null);
							
							// Use Javascript to make the table title more descriptive
							hgen.js()
							.text("var caption = document.querySelector(\".title\");")
							.text("caption.innerHTML='History for Permission [ " + type + " ]';")						
							.done();
							
							// Use Javascript to change Link Target to our last visited Detail page
							String lastPage = PermDetail.HREF + "?type=" + type
									+ "&instance=" + instance
									+ "&action=" + action;
							hgen.js()
								.text("alterLink('permdetail', '"+lastPage + "');")							
								.done();
							
							hgen.br();
							hgen.leaf("a", "href=#advanced_search", "onclick=divVisibility('advanced_search');").text("Advanced Search").end()
								.divID("advanced_search", "style=display:none");
							hgen.incr("table");
								
							addDateRow(hgen,"Start Date");
							addDateRow(hgen,"End Date");
							hgen.incr("tr").incr("td");
							hgen.tagOnly("input", "type=button","value=Get History",
									"onclick=datesURL('"+HREF+"?type=" + type
									+ "&instance=" + instance
									+ "&action=" + action+"');");
							hgen.end().end();
							hgen.end();
							hgen.end();
						}
					});
				}
			}

			);
		
	}
	
	private static void addDateRow(HTMLGen hgen, String s) {
		hgen
			.incr("tr")
			.incr("td")
			.incr("label", "for=month", "required").text(s+"*").end()
			.end()
			.incr("td")
			.incr("select", "name=month"+s.substring(0, s.indexOf(' ')), "id=month"+s.substring(0, s.indexOf(' ')), "required")
			.incr("option", "value=").text("Month").end();
		for (Month m : Month.values()) {
			if (Calendar.getInstance().get(Calendar.MONTH) == m.ordinal()) {
				hgen.incr("option", "selected", "value="+(m.ordinal()+1)).text(m.name()).end();
			} else {
				hgen.incr("option", "value="+(m.ordinal()+1)).text(m.name()).end();
			}
		}
		hgen.end()
			.end()
			.incr("td")
			.tagOnly("input","type=number","id=year"+s.substring(0, s.indexOf(' ')),"required",
					"value="+Calendar.getInstance().get(Calendar.YEAR), "min=1900", 
					"max="+Calendar.getInstance().get(Calendar.YEAR),
					"placeholder=Year").end()
			.end();
	}
	
	/**
	 * Implement the Table Content for History
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		private static final String CSP_ATT_COM = "@csp.att.com";
		private static final String[] headers = new String[] {"Date","User","Memo"};
		private Slot sType;
		private Slot sDates;
		
		public Model(AuthzEnv env) {
			sType = env.slot(NAME+".type");
			sDates = env.slot(NAME+".dates");
		}
		
		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			final String oName = trans.get(sType,null);
			final String oDates = trans.get(sDates,null);
			
			if(oName==null) {
				return Cells.EMPTY;
			}
			
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			String msg = null;
			try {
				gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						TimeTaken tt = trans.start("AAF Get History for Permission ["+oName+"]",Env.REMOTE);
						try {
							if (oDates != null) {
								client.setQueryParams("yyyymm="+oDates);
							}
							Future<History> fh = client.read(
								"/authz/hist/perm/"+oName,
								gui.historyDF
								);
							
							
							if (fh.get(AuthGUI.TIMEOUT)) {
								tt.done();
								tt = trans.start("Load History Data", Env.SUB);
								List<Item> histItems = fh.value.getItem();
								
								java.util.Collections.sort(histItems, new Comparator<Item>() {
									@Override
									public int compare(Item o1, Item o2) {
										return o2.getTimestamp().compare(o1.getTimestamp());
									}
								});
								
								for (Item i : histItems) {
									String user = i.getUser();
									AbsCell userCell = (user.endsWith(CSP_ATT_COM)?
											new RefCell(user,WEBPHONE + user.substring(0,user.indexOf('@'))):new TextCell(user));
									
									rv.add(new AbsCell[] {
											new TextCell(i.getTimestamp().toGregorianCalendar().getTime().toString()),
											userCell,
											new TextCell(i.getMemo())
									});
								}
								
							} else {
								if (fh.code()==403) {
									rv.add(new AbsCell[] {new TextCell("You may not view History of Permission [" + oName + "]", "colspan = 3", "class=center")});
								} else {
									rv.add(new AbsCell[] {new TextCell("*** Data Unavailable ***", "colspan = 3", "class=center")});
								}
							}
						} finally {
							tt.done();
						}

						return null;
					}
				});
				
			} catch (Exception e) {
				trans.error().log(e);
			}
		return new Cells(rv,msg);
		}
	}

}
