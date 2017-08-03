package com.att.authz.gui.pages;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.att.authz.env.AuthzTrans;
import com.att.authz.gui.AuthGUI;
import com.att.authz.gui.BreadCrumbs;
import com.att.authz.gui.NamedCode;
import com.att.authz.gui.Page;
import com.att.authz.gui.Table;
import com.att.authz.gui.Table.Cells;
import com.att.authz.gui.table.AbsCell;
import com.att.authz.gui.table.TextCell;
import com.att.cadi.CadiException;
import com.att.cadi.Symm;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.client.Retryable;
import com.att.cssa.rserv.HttpMethods;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.xgen.Cache;
import com.att.xgen.html.HTMLGen;

import aaf.v2_0.Api;
import aaf.v2_0.Api.Route;

public class ApiDocs extends Page {
	// Package on purpose
	private static final String HREF = "/gui/api";
	private static final String NAME = "AAF RESTful API";
	private static final String fields[] = {};
	private static final String ERROR_LINK = "<a href=\"./example/"
			+ "YXBwbGljYXRpb24vRXJyb3IranNvbg=="
//			+ Symm.base64noSplit().encode("application/Error+json") 
			+ "\">JSON</a> "
			+ "<a href=\"./example/"
			+ "YXBwbGljYXRpb24vRXJyb3IreG1s"
//			+ Symm.base64noSplit().encode("application/Error+xml") 
			+ "\">XML</a> ";

	
	public ApiDocs(final AuthGUI gui, final Page ... breadcrumbs) throws APIException, IOException {
		super(gui.env,NAME,HREF, fields,
			new BreadCrumbs(breadcrumbs),
			new Preamble(),
			new Table<AuthGUI,AuthzTrans>("AAF API Reference",gui.env.newTransNoAvg(),new Model(), "class=std")
			);
	}
	
	private static class Preamble extends NamedCode {

		private static final String I = "i";

		public Preamble() {
			super(false, "preamble");
		}

		@Override
		public void code(Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
			xgen.leaf(HTMLGen.H1).text("AAF 2.0 RESTful interface").end()
				.hr();
			xgen.leaf(HTMLGen.H2).text("Accessing RESTful").end();
			xgen.incr(HTMLGen.UL)
					.leaf(HTMLGen.LI).text("AAF RESTful service is secured by the following:").end()
					.incr(HTMLGen.UL)
						.leaf(HTMLGen.LI).text("The Client must utilize HTTP/S. Non Secure HTTP is not acceptable").end()
						.leaf(HTMLGen.LI).text("The Client MUST supply an Identity validated by one of the following mechanisms").end()
						.incr(HTMLGen.UL)
							.leaf(HTMLGen.LI).text("(Near Future) Application level Certificate").end()
						.end()
					.end()
					.leaf(HTMLGen.LI).text("Responses").end()
					.incr(HTMLGen.UL)
						.leaf(HTMLGen.LI).text("Each API Entity listed shows what structure will be accepted by service (ContentType) "
								+ "or responded with by service (Accept). Therefore, use these in making your call. Critical for PUT/POST.").end()
						.leaf(HTMLGen.LI).text("Each API call may respond with JSON or XML.  Choose the ContentType/Accept that has "
								+ "+json after the type for JSON or +xml after the Type for XML").end()
						.leaf(HTMLGen.LI).text("XSDs for Versions").end()
						.incr(HTMLGen.UL)
							.leaf(HTMLGen.LI).leaf(HTMLGen.A,"href=../theme/aaf_2_0.xsd").text("API 2.0").end().end()
						.end()
						.leaf(HTMLGen.LI).text("AAF can support multiple Versions of the API.  Choose the ContentType/Accept that has "
								+ "the appropriate version=?.?").end()
						.leaf(HTMLGen.LI).text("All Errors coming from AAF return AT&T Standard Error Message as a String: " + ERROR_LINK 
								+ " (does not apply to errors from Container)").end()
					.end()
					.leaf(HTMLGen.LI).text("Character Restrictions").end()
					.incr(HTMLGen.UL)
						.leaf(HTMLGen.LI).text("Character Restrictions must depend on the Enforcement Point used").end()
						.leaf(HTMLGen.LI).text("Most AAF usage will be AAF Enforcement Point Characters for Instance and Action are:")
							.br().br().leaf(I).text("a-zA-Z0-9,.()_-=%").end()
							.br().br().text("For Instance, you may declare a multi-dimensional key with : (colon) separator, example:").end()
							.br().leaf(I).text(":myCluster:myKeyspace").end()
							.br().br().text("The * (asterix) may be used as a wild-card by itself or within the multi-dimensional key, example:")
							.br().leaf(I).text(":myCluster:*").end()
							.br().br().text("The % (percent) character can be used as an Escape Character. Applications can use % followed by 2 hexadecimal "
									+ "digits to cover odd keys.  It is their code, however, which must translate.")
							.br().br().text("The = (equals) is allowed so that Applications can pass Base64 encodations of binary keys").end()
						.leaf(HTMLGen.LI).text("Ask for a Consultation on how these are typically used, or, if your tool is the only Enforcement Point, if set may be expanded").end()
					.end()
				.end();
			/*
			
			The Content is defined in the AAF XSD - TODO Add aaf.xsdâ€�;
			Character Restrictions

			URLs impose restrictions on characters which have specific meanings. This means you cannot have these characters in the Field Content you send
			â€œ#â€� is a â€œFragment URLâ€�, or anchor. Content after this Character is not sent. AAF cannot do anything about thisâ€¦ donâ€™t use it.
			â€œ?=&â€�. These are used to delineate Parameters.
			â€œ/â€œ is used to separate fields
			*/
		}
		
	};
	/**
	 * Implement the Table Content for Permissions by User
	 * 
	 *
	 */
	private static class Model implements Table.Data<AuthGUI,AuthzTrans> {
		public static final String[] HEADERS = new String[] {"Entity","Method","Path Info","Description"};
		private static final TextCell BLANK = new TextCell("");
	
		@Override
		public String[] headers() {
			return HEADERS;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public Cells get(final AuthGUI gui, final AuthzTrans trans) {
			ArrayList<AbsCell[]> ns = new ArrayList<AbsCell[]>();
			ArrayList<AbsCell[]> perms = new ArrayList<AbsCell[]>();
			ArrayList<AbsCell[]> roles = new ArrayList<AbsCell[]>();
			ArrayList<AbsCell[]> user = new ArrayList<AbsCell[]>();
			ArrayList<AbsCell[]> aafOnly = new ArrayList<AbsCell[]>();
			ArrayList<AbsCell[]> rv = new ArrayList<AbsCell[]>();
			
	
			TimeTaken tt = trans.start("AAF APIs",Env.REMOTE);
			try {
				gui.clientAsUser(trans.getUserPrincipal(), new Retryable<Void>() {
					@Override
					public Void code(Rcli<?> client) throws CadiException, ConnectException, APIException {
						Future<Api> fa = client.read("/api",gui.apiDF);
						if(fa.get(5000)) {
							tt.done();
							TimeTaken tt2 = trans.start("Load Data", Env.SUB);
							try {
								if(fa.value!=null)for(Route r : fa.value.getRoute()) {
									String path = r.getPath();
									// Build info
									StringBuilder desc = new StringBuilder();
			
									desc.append("<p class=double>");
									desc.append(r.getDesc());
									
									if(r.getComments().size()>0) {
										for(String ct : r.getComments()) {
											desc.append("</p><p class=api_comment>");
											desc.append(ct);
										}
									}
			
									if(r.getParam().size()>0) {
										desc.append("<hr><p class=api_label>Parameters</p>");
										
										for(String params : r.getParam()) {
											String param[] = params.split("\\s*\\|\\s*");
											desc.append("</p><p class=api_contentType>");
											desc.append(param[0]);
											desc.append(" : ");
											desc.append(param[1]);
											if("true".equalsIgnoreCase(param[2])) {
												desc.append(" (Required)");
											}
										}
									}
			
			
									if(r.getExpected()!=0) {
										desc.append("</p><p class=api_label>Expected HTTP Code</p><p class=api_comment>");
										desc.append(r.getExpected());
									} 
			
									if(r.getExplicitErr().size()!=0) {
										desc.append("</p><p class=api_label>Explicit HTTP Error Codes</p><p class=api_comment>");
										boolean first = true;
										for(int ee : r.getExplicitErr()) {
											if(first) {
												first = false;
											} else {
												desc.append(", ");
											}
											desc.append(ee);
										}
									}
			
									desc.append("</p><p class=api_label>");
									desc.append("GET".equals(r.getMeth())?"Accept:":"ContentType:");
									Collections.sort(r.getContentType());
									if(r.getPath().startsWith("/authn/basicAuth")) {
										desc.append("</p><p class=api_contentType>text/plain");
									}
									for(String ct : r.getContentType()) {
										if(ct.contains("version=2")) {
											desc.append("</p><p class=api_contentType><a href=\"./example/");
											try {
												desc.append(Symm.base64noSplit.encode(ct));
											} catch (IOException e) {
												throw new CadiException(e);
											}
											desc.append("\"/>");
											desc.append(ct);
											desc.append("</a>");
										}
									}
									desc.append("</p>");
									
									
									AbsCell[] sa = new AbsCell[] {
										null,
										new TextCell(r.getMeth(),"class=right"),
										new TextCell(r.getPath()),
										new TextCell(desc.toString()),
									};
			
									if(path.startsWith("/authz/perm")) {
										sa[0] = perms.size()==0?new TextCell("PERMISSION"):BLANK;
										perms.add(sa);
									} else if(path.startsWith("/authz/role") || path.startsWith("/authz/userRole")) {
										sa[0] = roles.size()==0?new TextCell("ROLE"):BLANK;
										roles.add(sa);
									} else if(path.startsWith("/authz/ns")) {
										sa[0] = ns.size()==0?new TextCell("NAMESPACE"):BLANK;
										ns.add(sa);
									} else if(path.startsWith("/authn/basicAuth") 
										|| path.startsWith("/authn/validate")
										|| path.startsWith("/authz/user")) {
										sa[0] = user.size()==0?new TextCell("USER"):BLANK;
										user.add(sa);
									} else {
										sa[0] = aafOnly.size()==0?new TextCell("AAF ONLY"):BLANK;
										aafOnly.add(sa);
									}
								}
								//TODO if(trans.fish(p))
								prepare(rv, perms,roles,ns,user);
							} finally {
								tt2.done();
							}
						} else {
							gui.writeError(trans, fa, null);
						}
						return null;
					}
				});
			} catch (Exception e) {
				trans.error().log(e.getMessage());
			} finally {
				tt.done();
			}
			
			return new Cells(rv,null);
		}

		@SuppressWarnings("unchecked")
		private void prepare(ArrayList<AbsCell[]> rv, ArrayList<AbsCell[]> ... all) {
			AbsCell lead;
			AbsCell[] row;
			for(ArrayList<AbsCell[]> al : all) {
				if(al.size()>1) {
					row = al.get(0);
					lead = row[0];
					row[0]=BLANK;
					al.get(0).clone()[0]=BLANK;
					Collections.sort(al, new Comparator<AbsCell[]>() {
						@Override
						public int compare(AbsCell[] ca1, AbsCell[] ca2) {
							int meth = ((TextCell)ca1[2]).name.compareTo(
									   ((TextCell)ca2[2]).name);
							if(meth == 0) {
								return (HttpMethods.valueOf(((TextCell)ca1[1]).name).compareTo(
										HttpMethods.valueOf(((TextCell)ca2[1]).name)));
							} else { 
								return meth;
							}
						}
					});
					// set new first row
					al.get(0)[0]=lead;

					rv.addAll(al);
				}
			}
		}
	}
}
