/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.cmd;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.att.aft.dme2.api.DME2Manager;
import com.att.authz.env.AuthzEnv;
import com.att.cadi.Access.Level;
import com.att.cadi.CadiException;
import com.att.cadi.Locator;
import com.att.cadi.SecuritySetter;
import com.att.cadi.client.PropertyLocator;
import com.att.cadi.client.Retryable;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfo;
import com.att.cadi.config.SecurityInfoC;
import com.att.cadi.dme2.DME2Locator;
import com.att.cadi.filter.AccessGetter;
import com.att.cadi.http.HBasicAuthSS;
import com.att.cadi.http.HMangr;
import com.att.cmd.mgmt.Mgmt;
import com.att.cmd.ns.NS;
import com.att.cmd.perm.Perm;
import com.att.cmd.role.Role;
import com.att.cmd.user.User;
import com.att.inno.env.APIException;
import com.att.inno.env.Env;
import com.att.inno.env.impl.Log4JLogTarget;
import com.att.inno.env.util.Split;

import jline.console.ConsoleReader;

public class AAFcli {

	public static final String AAF_DEFAULT_REALM = "aaf_default_realm";
	protected static PrintWriter pw;
	protected HMangr hman;
	// Storage for last reused client. We can do this
	// because we're technically "single" threaded calls.
	public Retryable<?> prevCall;

	protected SecuritySetter<HttpURLConnection> ss;
	protected AuthzEnv env;
	private boolean close;
	private List<Cmd> cmds;

	// Lex State
	private ArrayList<Integer> expect = new ArrayList<Integer>();
	private boolean verbose = true;
	private int delay;
	private SecurityInfo si;
	private boolean request = false;
	private String force = null;
	private boolean gui = false;

	private static int TIMEOUT = Integer.parseInt(Config.AAF_CONN_TIMEOUT_DEF);
	private static boolean isConsole = false;
	private static boolean isTest = false;
	private static boolean showDetails = false;
	private static boolean ignoreDelay = false;
	private static int globalDelay=0;
	
	public static int timeout() {
		return TIMEOUT;
	}

	public AAFcli(AuthzEnv env, Writer wtr, HMangr hman, SecurityInfo si, SecuritySetter<HttpURLConnection> ss) throws APIException {
		this.env = env;
		this.ss = ss;
		this.hman = hman;
		this.si = si;
		if (wtr instanceof PrintWriter) {
			pw = (PrintWriter) wtr;
			close = false;
		} else {
			pw = new PrintWriter(wtr);
			close = true;
		}


		// client = new DRcli(new URI(aafurl), new
		// BasicAuth(user,toPass(pass,true)))
		// .apiVersion("2.0")
		// .timeout(TIMEOUT);

		/*
		 * Create Cmd Tree
		 */
		cmds = new ArrayList<Cmd>();

		Role role = new Role(this);
		cmds.add(new Help(this, cmds));
		cmds.add(new Version(this));
		cmds.add(new Perm(role));
		cmds.add(role);
		cmds.add(new User(this));
		cmds.add(new NS(this));
		cmds.add(new Mgmt(this));
	}

	public void verbose(boolean v) {
		verbose = v;
	}

	public void close() {
		if (hman != null) {
			hman.close();
			hman = null;
		}
		if (close) {
			pw.close();
		}
	}

	public boolean eval(String line) throws Exception {
		if (line.length() == 0) {
			return true;
		} else if (line.startsWith("#")) {
			pw.println(line);
			return true;
		}

		String[] largs = argEval(line);
		int idx = 0;

		// Variable replacement
		StringBuilder sb = null;
		while (idx < largs.length) {
			int e = 0;
			for (int v = largs[idx].indexOf("@["); v >= 0; v = largs[idx].indexOf("@[", v + 1)) {
				if (sb == null) {
					sb = new StringBuilder();
				}
				sb.append(largs[idx], e, v);
				if ((e = largs[idx].indexOf(']', v)) >= 0) {
					String p = env.getProperty(largs[idx].substring(v + 2, e++));
					if (p != null) {
						sb.append(p);
					}
				}
			}
			if (sb != null && sb.length() > 0) {
				sb.append(largs[idx], e, largs[idx].length());
				largs[idx] = sb.toString();
				sb.setLength(0);
			}
			++idx;
		}

		idx = 0;
		boolean rv = true;
		while (rv && idx < largs.length) {
			// Allow Script to change Credential
			if (!gui) {
				if("as".equalsIgnoreCase(largs[idx])) {
					if (largs.length > ++idx) {
						// get Password from Props with ID as Key
						String user = largs[idx++];
						int colon = user.indexOf(':');
						String pass;
						if (colon > 0) {
							pass = user.substring(colon + 1);
							user = user.substring(0, colon);
						} else {
							pass = env.getProperty(user);
						}
						
						if (pass != null) {
							pass = env.decrypt(pass, false);
							env.setProperty(user, pass);
							ss = new HBasicAuthSS(user, pass,(SecurityInfoC<HttpURLConnection>) si);
							pw.println("as " + user);
						} else { // get Pass from System Properties, under name of
							// Tag
							pw.println("ERROR: No password set for " + user);
							rv = false;
						}
						continue;
					}
				} else if ("expect".equalsIgnoreCase(largs[idx])) {
					expect.clear();
					if (largs.length > idx++) {
						if (!"nothing".equals(largs[idx])) {
							for (String str : largs[idx].split(",")) {
								try {
									if ("Exception".equalsIgnoreCase(str)) {
										expect.add(-1);
									} else {
										expect.add(Integer.parseInt(str));
									}
								} catch (NumberFormatException e) {
									throw new CadiException("\"expect\" should be followed by Number");
								}
							}
						++idx;
						}
					}
					continue;
					// Sleep, typically for reports, to allow DB to update
					// Milliseconds
					
				} else if ("sleep".equalsIgnoreCase(largs[idx])) {
					Integer t = Integer.parseInt(largs[++idx]);
					pw.println("sleep " + t);
					Thread.sleep(t);
					++idx;
					continue;
				} else if ("delay".equalsIgnoreCase(largs[idx])) {
					delay = Integer.parseInt(largs[++idx]);
					pw.println("delay " + delay);
					++idx;
					continue;
				} else if ("pause".equalsIgnoreCase(largs[idx])) {
					pw.println("Press <Return> to continue...");
					++idx;
					new BufferedReader(new InputStreamReader(System.in)).readLine();
					continue;
				} else if ("exit".equalsIgnoreCase(largs[idx])) {
					pw.println("Exiting...");
					return false;
				}

			} 
			
			if("REQUEST".equalsIgnoreCase(largs[idx])) {
				request=true;
				++idx;
			} else if("FORCE".equalsIgnoreCase(largs[idx])) {
				force="true";
				++idx;
			} else if ("set".equalsIgnoreCase(largs[idx])) {
				while (largs.length > ++idx) {
					int equals = largs[idx].indexOf('=');
					if (equals < 0) {
						break;
					}
					String tag = largs[idx].substring(0, equals);
					String value = largs[idx].substring(++equals);
					pw.println("set " + tag + ' ' + value);
					boolean isTrue = "TRUE".equalsIgnoreCase(value);
					if("FORCE".equalsIgnoreCase(tag)) {
						force = value;
					} else if("REQUEST".equalsIgnoreCase(tag)) {
						request = isTrue;
					} else if("DETAILS".equalsIgnoreCase(tag)) {
						showDetails = isTrue;
					} else {
						env.setProperty(tag, value);
					}
				}
				continue;
				// Allow Script to indicate if Failure is what is expected
			}

			int ret = 0;
			for (Cmd c : cmds) {
				if (largs[idx].equalsIgnoreCase(c.getName())) {
					if (verbose) {
						pw.println(line);
						if (expect.size() > 0) {
							pw.print("** Expect ");
							boolean first = true;
							for (Integer i : expect) {
								if (first) {
									first = false;
								} else {
									pw.print(',');
								}
								pw.print(i);
							}
							pw.println(" **");
						}
					}
					try {
						ret = c.exec(++idx, largs);
						if (delay+globalDelay > 0) {
							Thread.sleep(delay+globalDelay);
						}
					} catch (Exception e) {
						if (expect.contains(-1)) {
							pw.println(e.getMessage());
							ret = -1;
						} else {
							throw e;
						}
					} finally {
						clearSingleLineProperties();
					}
					rv = expect.isEmpty() ? true : expect.contains(ret);
					if (verbose) {
						if (rv) {
							pw.println();
						} else {
							pw.print("!!! Unexpected Return Code: ");
							pw.print(ret);
							pw.println(", VALIDATE OUTPUT!!!");
						}
					}
					return rv;
				}
			}
			pw.write("Unknown Instruction \"");
			pw.write(largs[idx]);
			pw.write("\"\n");
			idx = largs.length;// always end after one command
		}
		return rv;
	}

	private String[] argEval(String line) {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> arr = new ArrayList<String>();
		boolean start = true;
		char quote = 0;
		for (int i = 0; i < line.length(); ++i) {
			char ch;
			if (Character.isWhitespace(ch = line.charAt(i))) {
				if (start) {
					continue; // trim
				} else if (quote != 0) {
					sb.append(ch);
				} else {
					arr.add(sb.toString());
					sb.setLength(0);
					start = true;
				}
			} else if (ch == '\'' || ch == '"') { // toggle
				if (quote == ch) {
					quote = 0;
				} else {
					quote = ch;
				}
			} else {
				start = false;
				sb.append(ch);
			}
		}
		if (sb.length() > 0) {
			arr.add(sb.toString());
		}

		String[] rv = new String[arr.size()];
		arr.toArray(rv);
		return rv;
	}

	public static void keyboardHelp() {
		System.out.println("'C-' means hold the ctrl key down while pressing the next key.");
		System.out.println("'M-' means hold the alt key down while pressing the next key.");
		System.out.println("For instance, C-b means hold ctrl key and press b, M-b means hold alt and press b\n");

		System.out.println("Basic Keybindings:");
		System.out.println("\tC-l - clear screen");        
		System.out.println("\tC-a - beginning of line");
		System.out.println("\tC-e - end of line");
		System.out.println("\tC-b - backward character (left arrow also works)");
		System.out.println("\tM-b - backward word");
		System.out.println("\tC-f - forward character (right arrow also works)");
		System.out.println("\tM-f - forward word");
		System.out.println("\tC-d - delete character under cursor");
		System.out.println("\tM-d - delete word forward");
		System.out.println("\tM-backspace - delete word backward");
		System.out.println("\tC-k - delete from cursor to end of line");
		System.out.println("\tC-u - delete entire line, regardless of cursor position\n");

		System.out.println("Command History:");
		System.out.println("\tC-r - search backward in history (repeating C-r continues the search)");
		System.out.println("\tC-p - move backwards through history (up arrow also works)");
		System.out.println("\tC-n - move forwards through history (down arrow also works)\n");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int rv = 0;
		// Cover for bash's need to escape *... (\\*)
		for (int i = 0; i < args.length; ++i) {
			if ("\\*".equals(args[i])) {
				args[i] = "*";
			}
		}
		
		System.setProperty("java.util.logging.config.file", "etc/logging.props");
		final AuthzEnv env = new AuthzEnv(System.getProperties());
		
		// Stop the (exceedingly annoying) DME2/other logs from printing console
		InputStream is;

		// Load Log4j too... sigh
		is = ClassLoader.getSystemResourceAsStream("log4j.properties");
		if(is==null) {
			env.log(Level.WARN, "Cannot find 'log4j.properties' in Classpath.  Best option: add 'etc' directory to classpath");
		} else {
			try {
				Properties props = new Properties();
				props.load(is);
				PropertyConfigurator.configure(props);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					env.debug().log(e); // only logging to avoid Sonar False positives.
				}
			}
		}

		env.loadFromSystemPropsStartsWith("AFT", "DME2", "aaf", "keyfile");
		try {
			Log4JLogTarget.setLog4JEnv("aaf", env);
			GetProp gp = new GetProp(env);
			String user = gp.get(false,Config.AAF_MECHID,"fully qualified id");
			String pass = gp.get(true, Config.AAF_MECHPASS, "password is hidden");
			if(env.getProperty(Config.AAF_URL)==null) {
				String p = env.getProperty("DMEServiceName");
				if(p!=null) {
					boolean https = "true".equalsIgnoreCase(env.getProperty("AFT_DME2_SSL_ENABLE"));
					env.setProperty(Config.AAF_URL, "http"+(https?"s":"")+"://DME2RESOLVE/"+p);
				}
			}
			String aafUrl = gp.get(false, Config.AAF_URL, "https://DME2RESOLVE or Direct URL:port");

			if(aafUrl!=null && aafUrl.contains("//DME2")) {
				//gp.set(Config.AFT_LATITUDE,"Lookup from a Map App or table");
				//gp.set(Config.AFT_LONGITUDE,"Lookup from a Map App or table");
				//gp.set(Config.AFT_ENVIRONMENT,"Check DME2 Installations");
			}

			if (gp.err() != null) {
				gp.err().append("to continue...");
				System.err.println(gp.err());
				System.exit(1);
			}
			

			Reader rdr = null;
			boolean exitOnFailure = true;
			/*
			 * Check for "-" options anywhere in command line
			 */
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < args.length; ++i) {
				if ("-i".equalsIgnoreCase(args[i])) {
					rdr = new InputStreamReader(System.in);
					// } else if("-o".equalsIgnoreCase(args[i])) {
					// // shall we do something different? Output stream is
					// already done...
				} else if ("-f".equalsIgnoreCase(args[i])) {
					if (args.length > i + 1) {
						rdr = new FileReader(args[++i]);
					}
				} else if ("-a".equalsIgnoreCase(args[i])) {
					exitOnFailure = false;
				} else if ("-c".equalsIgnoreCase(args[i])) {
					isConsole = true;
				} else if ("-s".equalsIgnoreCase(args[i]) && args.length > i + 1) {
					env.setProperty(Cmd.STARTDATE, args[++i]);
				} else if ("-e".equalsIgnoreCase(args[i]) && args.length > i + 1) {
					env.setProperty(Cmd.ENDDATE, args[++i]);
				} else if ("-t".equalsIgnoreCase(args[i])) {
					isTest = true;
				} else if ("-d".equalsIgnoreCase(args[i])) {
					showDetails = true;
				} else if ("-n".equalsIgnoreCase(args[i])) {
					ignoreDelay = true;
				} else {
					if (sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(args[i]);
				}
			}

			SecurityInfo si = new SecurityInfo(env);
			env.loadToSystemPropsStartsWith("AAF", "DME2");
			Locator loc;
			if(aafUrl.contains("//DME2RESOLVE")) {
				DME2Manager dm = new DME2Manager("AAFcli DME2Manager", System.getProperties());
				loc = new DME2Locator(env, dm, aafUrl);
			} else {
				loc = new PropertyLocator(aafUrl);
			}

			//Config.configPropFiles(new AccessGetter(env), env);
			
			TIMEOUT = Integer.parseInt(env.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
			HMangr hman = new HMangr(env, loc).readTimeout(TIMEOUT).apiVersion("2.0");
			
			//TODO: Consider requiring a default in properties
			env.setProperty(Config.AAF_DEFAULT_REALM, System.getProperty(Config.AAF_DEFAULT_REALM,Config.getDefaultRealm()));

			AAFcli aafcli = new AAFcli(env, new OutputStreamWriter(System.out), hman, si, 
				new HBasicAuthSS(user, env.decrypt(pass,false), (SecurityInfoC<HttpURLConnection>) si));
			if(!ignoreDelay) {
				File delay = new File("aafcli.delay");
				if(delay.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(delay));
					try {
						globalDelay = Integer.parseInt(br.readLine());
					} catch(Exception e) {
						env.debug().log(e);
					} finally {
						br.close();
					}
				}
			}
			try {
				if (isConsole) {
					System.out.println("Type 'help' for short help or 'help -d' for detailed help with aafcli commands");
					System.out.println("Type '?' for help with command line editing");
					System.out.println("Type 'q', 'quit', or 'exit' to quit aafcli\n");

					ConsoleReader reader = new ConsoleReader();
					try {
						reader.setPrompt("aafcli > ");
	
						String line;
						while ((line = reader.readLine()) != null) {
							showDetails = (line.contains("-d"))?true:false;
	
							if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("q") || line.equalsIgnoreCase("exit")) {
								break;
							} else if (line.equalsIgnoreCase("--help -d") || line.equalsIgnoreCase("help -d") 
									|| line.equalsIgnoreCase("help")) {
								line = "--help";
							} else if (line.equalsIgnoreCase("cls")) {
								reader.clearScreen();
								continue;
							} else if (line.equalsIgnoreCase("?")) {
								keyboardHelp();
								continue;
							}
							try {
								aafcli.eval(line);
								pw.flush();
							} catch (Exception e) {
								pw.println(e.getMessage());
								pw.flush();
							}
						}
					} finally {
						reader.close();
					}
				} else if (rdr != null) {
					BufferedReader br = new BufferedReader(rdr);
					String line;
					while ((line = br.readLine()) != null) {
						if (!aafcli.eval(line) && exitOnFailure) {
							rv = 1;
							break;
						}
					}
				} else { // just run the command line
					aafcli.verbose(false);
					if (sb.length() == 0) {
						sb.append("--help");
					}
					rv = aafcli.eval(sb.toString()) ? 0 : 1;
				}
			} finally {
				aafcli.close();

				// Don't close if No Reader, or it's a Reader of Standard In
				if (rdr != null && !(rdr instanceof InputStreamReader)) {
					rdr.close();
				}
			}
		} catch (MessageException e) {
			System.out.println("MessageException caught");

			System.err.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		System.exit(rv);

	}

	private static class GetProp {
		private Console cons = System.console();
		private StringBuilder err = null;
		private AuthzEnv env;
		
		public GetProp(AuthzEnv env) {
			this.env = env;
		}

		public String get(final boolean pass, final String tag, final String other)  {
			String data = env.getProperty(tag,null);
			if (data == null) {
				if(cons!=null) {
					if(pass) {
						char[] cp = System.console().readPassword("%s: ",tag);
						if(cp!=null) {
							data=String.valueOf(cp);
						}
					} else {
						cons.writer().format("%s: ", tag);
						cons.flush();
						data = cons.readLine();
					}
				}
				if(data==null) {
					if(err == null) {
						err  = new StringBuilder("Add -D");
					} else {
						err.append(", -D");
					}
					err.append(tag);
					if(other!=null) {
						err.append("=<");
						err.append(other);
						err.append('>');
					}
				}
			}
			return data;
		}
		
		public void set(final String tag, final String other)  {
			String data = env.getProperty(tag,null);
			if (data == null) {
				if(cons!=null) {
					cons.writer().format("%s: ", tag);
					cons.flush();
					data = cons.readLine();
				}
				if(data==null) {
					if(err == null) {
						err  = new StringBuilder("Add -D");
					} else {
						err.append(", -D");
					}
					err.append(tag);
					if(other!=null) {
						err.append("=<");
						err.append(other);
						err.append('>');
					}
				}
			}
			if(data!=null) {
				System.setProperty(tag, data);
			}
		}

		public StringBuilder err() {
			return err;
		}
	}

	public boolean isTest() {
		return AAFcli.isTest;
	}
	
	public boolean isDetailed() {
		return AAFcli.showDetails;
	}

	public String typeString(Class<?> cls, boolean json) {
		return "application/" + cls.getSimpleName() + "+" + (json ? "json" : "xml") + ";version=" + hman.apiVersion();
	}

	public String forceString() {
		return force;
	}

	public boolean addRequest() {
		return request;
	}

	public void clearSingleLineProperties() {
		force  = null;
		request = false;
		showDetails = false;
	}

	public void gui(boolean b) {
		gui  = b;
	}

}
