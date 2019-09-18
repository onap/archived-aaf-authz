/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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

package org.onap.aaf.auth.cmd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.cmd.mgmt.Mgmt;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.cmd.perm.Perm;
import org.onap.aaf.auth.cmd.role.Role;
import org.onap.aaf.auth.cmd.user.User;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.aaf.v2_0.AAFConHttp;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.cadi.sso.AAFSSO;
import org.onap.aaf.misc.env.APIException;

import jline.console.ConsoleReader;

public class AAFcli {
    protected static PrintWriter pw;
    protected HMangr hman;
    // Storage for last reused client. We can do this
    // because we're technically "single" threaded calls.
    public Retryable<?> prevCall;

    protected SecuritySetter<HttpURLConnection> ss;
//    protected AuthzEnv env;
    private boolean close;
    private List<Cmd> cmds;

    // Lex State
    private ArrayList<Integer> expect = new ArrayList<>();
    private boolean verbose = true;
    private int delay;
    private SecurityInfoC<HttpURLConnection> si;
    private boolean request = false;
    private String force = null;
    private boolean gui = false;
    // Package on purpose
    Access access;
    AuthzEnv env;

    private static int TIMEOUT = Integer.parseInt(Config.AAF_CONN_TIMEOUT_DEF);
    private static boolean isConsole = false;
    private static boolean isTest = false;
    private static boolean showDetails = false;
    private static boolean ignoreDelay = false;
    private static int globalDelay=0;

    // Create when only have Access
    public AAFcli(Access access, Writer wtr, HMangr hman, SecurityInfoC<HttpURLConnection> si, SecuritySetter<HttpURLConnection> ss) throws APIException, CadiException {
        this(access,new AuthzEnv(access.getProperties()),wtr,hman, si,ss);
    }

    public AAFcli(Access access, AuthzEnv env, Writer wtr, HMangr hman, SecurityInfoC<HttpURLConnection> si, SecuritySetter<HttpURLConnection> ss) throws APIException {
        this.env = env;
        this.access = access;
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

        /*
         * Create Cmd Tree
         */
        cmds = new ArrayList<>();

        Role role = new Role(this);
        cmds.add(new Help(this, cmds));
        cmds.add(new Version(this));
        cmds.add(new Perm(role));
        cmds.add(role);
        cmds.add(new User(this));
        cmds.add(new NS(this));
        cmds.add(new Mgmt(this));
    }

    public AuthzEnv env() {
        return env;
    }

    public static int timeout() {
        return TIMEOUT;
    }

    public void verbose(boolean v) {
        verbose = v;
    }

    public void close() {
//        if (hman != null) {
//            hman.close();
//            hman = null;
//        }
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
                    String p = access.getProperty(largs[idx].substring(v + 2, e),null);
                    if (p==null) {
                        p = System.getProperty(largs[idx].substring(v+2,e));
                    }
                    ++e;
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
                if ("as".equalsIgnoreCase(largs[idx])) {
                    if (largs.length > ++idx) {
                        // get Password from Props with ID as Key
                        String user = largs[idx++];
                        int colon = user.indexOf(':');
                        String pass;
                        if (colon > 0) {
                            pass = user.substring(colon + 1);
                            user = user.substring(0, colon);
                        } else {
                            pass = access.getProperty(user, null);
                        }
                        if (pass != null) {
                            pass = access.decrypt(pass, false);
                            access.getProperties().put(user, pass);
                            ss=new HBasicAuthSS(si, user, pass);
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
                    // Sonar insists we do something with the string, though it's only a pause.  Not very helpful...
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    String sonar;
                    try {
                        sonar = br.readLine();
                    } finally {
                        br.close();
                    }
                    sonar=""; // this useless code brought to you by Sonar.
                    pw.print(sonar);
                    continue;
                } else if ("exit".equalsIgnoreCase(largs[idx])) {
                    pw.println("Exiting...");
                    return false;
                } else if ("set".equalsIgnoreCase(largs[idx])) {
                    while (largs.length > ++idx) {
                        int equals = largs[idx].indexOf('=');
                        String tag, value;
                        if (equals < 0) {
                            tag = largs[idx];
                            value = access.getProperty(Config.AAF_APPPASS,null);
                            if (value==null) {
                                break;
                            } else {
                                value = access.decrypt(value, false);
                                if (value==null) {
                                    break;
                                }
                                access.getProperties().put(tag, value);
                                pw.println("set " + tag + " <encrypted>");
                            }
                        } else {
                            tag = largs[idx].substring(0, equals);
                            value = largs[idx].substring(++equals);
                            pw.println("set " + tag + ' ' + value);
                        }
                        boolean isTrue = "TRUE".equalsIgnoreCase(value);
                        if ("FORCE".equalsIgnoreCase(tag)) {
                            force = value;
                        } else if ("REQUEST".equalsIgnoreCase(tag)) {
                            request = isTrue;
                        } else if ("DETAILS".equalsIgnoreCase(tag)) {
                            showDetails = isTrue;
                        } else {
                            access.getProperties().put(tag, value);
                        }
                    }
                    continue;
                    // Allow Script to indicate if Failure is what is expected
                }

            } 
            
            if ("REQUEST".equalsIgnoreCase(largs[idx])) {
                request=true;
                ++idx;
            } else if ("FORCE".equalsIgnoreCase(largs[idx])) {
                force="true";
                ++idx;
            } else if ("DETAILS".equalsIgnoreCase(largs[idx])) {
                showDetails=true;
                ++idx;
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
                            Thread.sleep((long)(delay+globalDelay));
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
                    rv = expect.isEmpty() || expect.contains(ret);
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
        ArrayList<String> arr = new ArrayList<>();
        boolean start = true;
        char quote = 0;
        char last = 0;
        for (int i = 0; i < line.length(); ++i) {
            char ch;
            ch = line.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (start || last==',') {
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
            } else if (ch=='|' && quote==0) {
                arr.add(sb.toString());
                sb.setLength(0);
                start = true;
            } else {
                start = false;
                sb.append(ch);
                last = ch;
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
        
        try {
            AAFSSO aafsso = new AAFSSO(args);
            String noexit = aafsso.access().getProperty("no_exit");
            try {
                PropAccess access = aafsso.access();

                if (aafsso.ok()) {
                    Define.set(access);
                    AuthzEnv env = new AuthzEnv(access);
                    
                    Reader rdr = null;
                    boolean exitOnFailure = true;
                    /*
                     * Check for "-" options anywhere in command line
                     */
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < args.length; ++i) {
                        if ("-i".equalsIgnoreCase(args[i])) {
                            rdr = new InputStreamReader(System.in);
                            // } else if ("-o".equalsIgnoreCase(args[i])) {
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
                            access.setProperty(Cmd.STARTDATE, args[++i]);
                        } else if ("-e".equalsIgnoreCase(args[i]) && args.length > i + 1) {
                            access.setProperty(Cmd.ENDDATE, args[++i]);
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
                    
                    AAFConHttp aafcon = new AAFConHttp(access);
//                    
//                    SecurityInfoC<?> si = aafcon.securityInfo();
//                    Locator<URI> loc;
                    
                    aafsso.setLogDefault();
                    aafsso.setStdErrDefault();
    
                    // Note, with AAF Locator, this may not longer be necessary 3/2018 Jonathan
                    if (!aafsso.loginOnly()) {
//                        try {
//                            loc = new AAFLocator(si,new URI(access.getProperty(Config.AAF_URL)));
//                        } catch (Throwable t) {
//                            aafsso.setStdErrDefault();
//                            throw t;
//                        } finally {
//                            // Other Access is done writing to StdOut and StdErr, reset Std out
//                            aafsso.setLogDefault();
//                        }
    
                        TIMEOUT = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
//                        HMangr hman = new HMangr(access, loc).readTimeout(TIMEOUT).apiVersion(Config.AAF_DEFAULT_API_VERSION);
                        
                        if (access.getProperty(Config.AAF_DEFAULT_REALM)==null) {
                            access.setProperty(Config.AAF_DEFAULT_REALM, "people.osaaf.org");
                            aafsso.addProp(Config.AAF_DEFAULT_REALM, "people.osaaf.org");
                        }
            
                        AAFcli aafcli = new AAFcli(access,env, new OutputStreamWriter(System.out),  
                                aafcon.hman(), aafcon.securityInfo(), aafcon.securityInfo().defSS);
//                            new HBasicAuthSS(si,aafsso.user(), access.decrypt(aafsso.enc_pass(),false)));
//                        }
                        if (!ignoreDelay) {
                            File delay = new File("aafcli.delay");
                            if (delay.exists()) {
                                BufferedReader br = new BufferedReader(new FileReader(delay));
                                try {
                                    globalDelay = Integer.parseInt(br.readLine());
                                } catch (Exception e) {
                                    access.log(Level.DEBUG,e);
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
                                        showDetails = (line.contains("-d"));
                
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
                                try {
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        if (!aafcli.eval(line) && exitOnFailure) {
                                            rv = 1;
                                            break;
                                        }
                                    }
                                } finally {
                                    br.close();
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
                    }
                }
            } finally {
                aafsso.close();
                StringBuilder err = aafsso.err();
                if (err != null) {
                    err.append("to continue...");
                    System.err.println(err);
                }
            }
            if (noexit==null) {
                return;
            }


        } catch (MessageException e) {
            System.out.println("MessageException caught");

            System.err.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        System.exit(rv);
    }

    public boolean isTest() {
        return AAFcli.isTest;
    }
    
    public boolean isDetailed() {
        return AAFcli.showDetails;
    }

    public String typeString(Class<?> cls, boolean json) {
        return "application/" + cls.getSimpleName() + "+" + (json ? "json" : "xml");//+ ";version=" + hman.apiVersion();
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
