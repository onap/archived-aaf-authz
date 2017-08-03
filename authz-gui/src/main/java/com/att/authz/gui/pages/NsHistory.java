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
import com.att.cadi.CadiException;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.Slot;
import com.att.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.History;
import aaf.v2_0.History.Item;

public class NsHistory extends Page {
	static final String NAME="NsHistory";
	static final String HREF = "/gui/nsHistory";
	static final String FIELDS[] = {"name","dates"};
	static final String WEBPHONE = "http://webphone.att.com/cgi-bin/webphones.pl?id=";
	static enum Month { JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, 
							AUGUST, SEPTEMBER, OCTOBER, NOVEMBER, DECEMBER };
	
	public NsHistory(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME,HREF, FIELDS,
			new BreadCrumbs(breadcrumbs),
			new Table<AuthGUI,AuthzTrans>("History", gui.env.newTransNoAvg(),new Model(gui.env()),"class=std"),
			new NamedCode(true, "content") {
				@Override
				public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
					final Slot name = gui.env.slot(NAME+".name");
					cache.dynamic(hgen, new DynamicCode<HTMLGen, AuthGUI, AuthzTrans>() {
						@Override
						public void code(AuthGUI gui, AuthzTrans trans,	Cache<HTMLGen> cache, HTMLGen hgen)	throws APIException, IOException {
							String obName = trans.get(name, null);
							
							// Use Javascript to make the table title more descriptive
							hgen.js()
							.text("var caption = document.querySelector(\".title\");")
							.text("caption.innerHTML='History for Namespace [ " + obName + " ]';")						
							.done();
							
							// Use Javascript to change Link Target to our last visited Detail page
							String lastPage = NsDetail.HREF + "?name=" + obName;
							hgen.js()
								.text("alterLink('nsdetail', '"+lastPage + "');")							
								.done();
							
							hgen.br();
							hgen.leaf("a","href=#advanced_search","onclick=divVisibility('advanced_search');").text("Advanced Search").end()
								.divID("advanced_search", "style=display:none");
							hgen.incr("table");
								
							addDateRow(hgen,"Start Date");
							addDateRow(hgen,"End Date");
							hgen.incr("tr").incr("td");
							hgen.tagOnly("input", "type=button","value=Get History",
									"onclick=datesURL('"+HREF+"?name=" + obName+"');");
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
		private Slot name;
		private Slot dates;
		
		public Model(AuthzEnv env) {
			name = env.slot(NAME+".name");
			dates = env.slot(NAME+".dates");
		}
		
		@Override
		public String[] headers() {
			return headers;
		}
		
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			final String oName = trans.get(name,null);
			final String oDates = trans.get(dates,null);
			
			if(oName==null) {
				return Cells.EMPTY;
			}
			
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			String msg = null;
			final TimeTaken tt = trans.start("AAF Get History for Namespace ["+oName+"]",Env.REMOTE);
			try {
				gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						if (oDates != null) {
							client.setQueryParams("yyyymm="+oDates);
						}
						Future<History> fh = client.read("/authz/hist/ns/"+oName,gui.historyDF);
						if (fh.get(AuthGUI.TIMEOUT)) {
							tt.done();
							TimeTaken tt2 = trans.start("Load History Data", Env.SUB);
							try {
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
							} finally {
								tt2.done();
							}
						} else {
							if (fh.code()==403) {
								rv.add(new AbsCell[] {new TextCell("You may not view History of Namespace [" + oName + "]", "colspan = 3", "class=center")});
							} else {
								rv.add(new AbsCell[] {new TextCell("*** Data Unavailable ***", "colspan = 3", "class=center")});
							}
						}
						return null;
					}
				});
			} catch (Exception e) {
				trans.error().log(e);
			} finally {
				tt.done();
			}
		return new Cells(rv,msg);
		}
	}

}
