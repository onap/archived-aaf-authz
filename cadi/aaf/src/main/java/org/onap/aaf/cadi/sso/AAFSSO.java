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

package org.onap.aaf.cadi.sso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.aaf.Defaults;
import org.onap.aaf.cadi.aaf.v2_0.AAFCon;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.configure.ArtifactDir;
import org.onap.aaf.cadi.locator.SingleEndpointLocator;
import org.onap.aaf.cadi.util.MyConsole;
import org.onap.aaf.cadi.util.SubStandardConsole;
import org.onap.aaf.cadi.util.TheConsole;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import locate.v1_1.Configuration;
import locate.v1_1.Configuration.Props;

public class AAFSSO {
    public static final MyConsole  cons = TheConsole.implemented() ? new TheConsole() : new SubStandardConsole();
//    private static final int EIGHT_HOURS = 8 * 60 * 60 * 1000;

    private Properties diskprops;
    private boolean touchDiskprops;
    private File dot_aaf = null;
    private File sso = null; // instantiated, if ever, with diskprops

    boolean removeSSO = false;
    boolean loginOnly = false;
    boolean doExit = true;
    private PropAccess access;
    private StringBuilder err;
    private String user;
    private String encrypted_pass;
    private boolean use_X509;

    private PrintStream os;

    private Method close;
    private final PrintStream stdOutOrig;
    private final PrintStream stdErrOrig;
    private boolean ok;

    public AAFSSO(String[] args) throws IOException, CadiException {
        this(args,new Properties());
    }
    
    public AAFSSO(String[] args, ProcessArgs pa) throws IOException, CadiException {
        this(args,pa.process(args, new Properties()));
    }

    public AAFSSO(String[] args, Properties dp) throws IOException, CadiException {
        stdOutOrig = System.out;
        stdErrOrig = System.err;
        ok = true;
        List<String> nargs = parseArgs(args);
        diskprops = dp;
        touchDiskprops = false;

        dot_aaf = new File(System.getProperty("user.home") + "/.aaf");
        if (!dot_aaf.exists()) {
            dot_aaf.mkdirs();
        }
        File f = new File(dot_aaf, "sso.out");
        os = new PrintStream(new FileOutputStream(f, true));
        //System.setOut(os);
        System.setErr(os);

        sso = new File(dot_aaf, "sso.props");
        if (sso.exists()) {
            InputStream propStream = new FileInputStream(sso);
            try {
                diskprops.load(propStream);
            } finally {
                propStream.close();
            }
        }
        
        File dot_aaf_kf = new File(dot_aaf, "keyfile");

        if (removeSSO) {
            if (dot_aaf_kf.exists()) {
                dot_aaf_kf.setWritable(true, true);
                dot_aaf_kf.delete();
            }
            if (sso.exists()) {
                Properties temp = new Properties();
                // Keep only these
                for (Entry<Object, Object> es : diskprops.entrySet()) {
                    if (Config.CADI_LATITUDE.equals(es.getKey()) ||
                       Config.CADI_LONGITUDE.equals(es.getKey()) ||
                       Config.AAF_DEFAULT_REALM.equals(es.getKey())) {
                         temp.setProperty(es.getKey().toString(), es.getValue().toString());
                    }
                }
                diskprops = temp;
                touchDiskprops = true;
            }
            String[] naargs = new String[nargs.size()];
            nargs.toArray(naargs);
            access = new PropAccess(os, naargs);
            ok = false;
            setLogDefault();
            System.out.println("AAF SSO information removed");
        } else {
            //    Config.setDefaultRealm(access);
    
            if (!dot_aaf_kf.exists()) {
            	// This will create, as required, or reuse
                ArtifactDir.getSymm(dot_aaf_kf);
            }

            for (Entry<Object, Object> es : diskprops.entrySet()) {
                nargs.add(es.getKey().toString() + '=' + es.getValue().toString());
            }
            String[] naargs = new String[nargs.size()];
            nargs.toArray(naargs);
            access = new PropAccess(os, naargs);
            
            if (loginOnly) {
                for (String tag : new String[] {Config.AAF_APPID, Config.AAF_APPPASS, 
                        Config.CADI_ALIAS, Config.CADI_KEYSTORE,Config.CADI_KEYSTORE_PASSWORD,Config.CADI_KEY_PASSWORD}) {
                    access.getProperties().remove(tag);
                    diskprops.remove(tag);
                }
                touchDiskprops=true;
// TODO Do we want to require reset of Passwords at least every Eight Hours.
//            } else if (sso.lastModified() > (System.currentTimeMillis() - EIGHT_HOURS)) {
//                for (String tag : new String[] {Config.AAF_APPPASS,Config.CADI_KEYSTORE_PASSWORD,Config.CADI_KEY_PASSWORD}) {
//                    access.getProperties().remove(tag);
//                    diskprops.remove(tag);
//                }
//                touchDiskprops=true;
            }
    
            String keyfile = access.getProperty(Config.CADI_KEYFILE); // in case its CertificateMan props
            if (keyfile == null) {
                access.setProperty(Config.CADI_KEYFILE, dot_aaf_kf.getAbsolutePath());
                addProp(Config.CADI_KEYFILE,dot_aaf_kf.getAbsolutePath());
            }
    
    
            String alias, appID;
            alias = access.getProperty(Config.CADI_ALIAS);
            if (alias==null) {
                appID = access.getProperty(Config.AAF_APPID);
                user=appID;
            } else {
                user=alias;
                appID=null;
            }
            
            String aaf_container_ns = "";
            if (appID!=null) {
            	if( access.getProperty(Config.AAF_APPPASS)==null) {
            		appID = user = cons.readLine("Deployer ID [%s]: ", user);
            		access.setProperty(Config.AAF_APPID,appID);
	                char[] password = cons.readPassword("Password for %s: ", user);
	                if(password.length>0) {
		                String app_pass = access.encrypt(new String(password));
		               	access.setProperty(Config.AAF_APPPASS,app_pass);
		               	diskprops.setProperty(Config.AAF_APPPASS,app_pass);
	                }
	                aaf_container_ns = cons.readLine("Container Namespace (blank if none)? [\"\"]: ", aaf_container_ns);
            	}
             	diskprops.setProperty(Config.AAF_APPID,appID);
            }
            
            String keystore=access.getProperty(Config.CADI_KEYSTORE);
            String keystore_pass=access.getProperty(Config.CADI_KEYSTORE_PASSWORD);
            
            if (user==null || (alias!=null && (keystore==null || keystore_pass==null))) {
                String select = null;
                String name;
                for (File tsf : dot_aaf.listFiles()) {
                    name = tsf.getName();
                    if (!name.contains("trust") && (name.endsWith(".jks") || name.endsWith(".p12"))) {
                        setLogDefault();
                        select = cons.readLine("Use %s for Identity? (y/n): ",tsf.getName());
                        if ("y".equalsIgnoreCase(select)) {
                            keystore = tsf.getCanonicalPath();
                            access.setProperty(Config.CADI_KEYSTORE, keystore);
                            addProp(Config.CADI_KEYSTORE, keystore);
                            char[] password = cons.readPassword("Keystore Password: ");
                            encrypted_pass= access.encrypt(new String(password));
                            access.setProperty(Config.CADI_KEYSTORE_PASSWORD, encrypted_pass);
                            addProp(Config.CADI_KEYSTORE_PASSWORD, encrypted_pass);
                            
                            // TODO READ Aliases out of Keystore?
                            user = alias = cons.readLine("Keystore alias: ");
                            access.setProperty(Config.CADI_ALIAS, user);
                            addProp(Config.CADI_ALIAS, user);
                            break;
                        }
                    }
                }
                if (alias==null) {
                    user = appID = cons.readLine(Config.AAF_APPID + ": ");
                    access.setProperty(Config.AAF_APPID, appID);
                    addProp(Config.AAF_APPID, appID);
                    char[] password = cons.readPassword(Config.AAF_APPPASS + ": ");
                    encrypted_pass= access.encrypt(new String(password));
                    access.setProperty(Config.AAF_APPPASS, encrypted_pass);
                    addProp(Config.AAF_APPPASS, encrypted_pass);
                }
            } else {
                encrypted_pass = access.getProperty(Config.CADI_KEYSTORE_PASSWORD);
                if (encrypted_pass == null) {
                    keystore_pass = null;
                    encrypted_pass = access.getProperty(Config.AAF_APPPASS);
                } else {
                    keystore_pass = encrypted_pass;
                }
            }
            
    
            if (alias!=null) {
                use_X509 = true;
            } else {
                use_X509 = false;
                Symm decryptor = ArtifactDir.getSymm(dot_aaf_kf);
                if (user == null) {
                    if (sso.exists()) {
                        String cm_url = access.getProperty(Config.AAF_URL_CM); // SSO might overwrite...
                        FileInputStream fos = new FileInputStream(sso);
                        try {
                            access.load(fos);
                            user = access.getProperty(Config.AAF_APPID);
                            encrypted_pass = access.getProperty(Config.AAF_APPPASS);
                            // decrypt with .aaf, and re-encrypt with regular Keyfile
                            access.setProperty(Config.AAF_APPPASS,
                                    access.encrypt(decryptor.depass(encrypted_pass)));
                            if (cm_url != null) { //Command line CM_URL Overwrites ssofile.
                                access.setProperty(Config.AAF_URL_CM, cm_url);
                            }
                        } finally {
                            fos.close();
                        }
                    } else {
                        diskprops = new Properties();
                        String realm = Config.getDefaultRealm();
                        // Turn on Console Sysout
                        System.setOut(System.out);
                        user = cons.readLine("aaf_id(%s@%s): ", System.getProperty("user.name"), realm);
                        if (user == null) {
                            user = System.getProperty("user.name") + '@' + realm;
                        } else if (user.length() == 0) { //
                            user = System.getProperty("user.name") + '@' + realm;
                        } else if ((user.indexOf('@') < 0) && (realm != null)) {
                            user = user + '@' + realm;
                        }
                        access.setProperty(Config.AAF_APPID, user);
                        diskprops.setProperty(Config.AAF_APPID, user);
                        encrypted_pass = new String(cons.readPassword("aaf_password: "));
                        System.setOut(os);
                        encrypted_pass = Symm.ENC + decryptor.enpass(encrypted_pass);
                        access.setProperty(Config.AAF_APPPASS, encrypted_pass);
                        diskprops.setProperty(Config.AAF_APPPASS, encrypted_pass);
                        diskprops.setProperty(Config.CADI_KEYFILE, access.getProperty(Config.CADI_KEYFILE));
                    }
                }
            }
            if (user == null) {
                err = new StringBuilder("Add -D" + Config.AAF_APPID + "=<id> ");
            }
    
            if (encrypted_pass == null && alias == null) {
                if (err == null) {
                    err = new StringBuilder();
                } else {
                    err.append("and ");
                }
                err.append("-D" + Config.AAF_APPPASS + "=<passwd> ");
            }
            
            String cadiLatitude = access.getProperty(Config.CADI_LATITUDE);
            if (cadiLatitude==null) {
                System.out.println("# If you do not know your Global Coordinates, we suggest bing.com/maps");
                cadiLatitude=AAFSSO.cons.readLine("cadi_latitude[0.000]=");
                if (cadiLatitude==null || cadiLatitude.isEmpty()) {
                    cadiLatitude="0.000";
                }
                access.setProperty(Config.CADI_LATITUDE, cadiLatitude);
                addProp(Config.CADI_LATITUDE, cadiLatitude);
                
            }
            String cadiLongitude = access.getProperty(Config.CADI_LONGITUDE);
            if (cadiLongitude==null) {
                cadiLongitude=AAFSSO.cons.readLine("cadi_longitude[0.000]=");
                if (cadiLongitude==null || cadiLongitude.isEmpty()) {
                    cadiLongitude="0.000";
                }
                access.setProperty(Config.CADI_LONGITUDE, cadiLongitude);
                addProp(Config.CADI_LONGITUDE, cadiLongitude);
            }
    
            String cadi_truststore = access.getProperty(Config.CADI_TRUSTSTORE);
            if (cadi_truststore==null) {
                String name; 
                String select;
                for (File tsf : dot_aaf.listFiles()) {
                    name = tsf.getName();
                    if (name.contains("trust") && 
                            (name.endsWith(".jks") || name.endsWith(".p12"))) {
                        select = cons.readLine("Use %s for TrustStore? (y/n):",tsf.getName());
                        if ("y".equalsIgnoreCase(select)) {
                            cadi_truststore=tsf.getCanonicalPath();
                            access.setProperty(Config.CADI_TRUSTSTORE, cadi_truststore);
                            addProp(Config.CADI_TRUSTSTORE, cadi_truststore);
                            break;
                        }
                    }
                }
            }
            if (cadi_truststore!=null) {
                if (cadi_truststore.indexOf(File.separatorChar)<0) {
                    cadi_truststore=dot_aaf.getPath()+File.separator+cadi_truststore;
                }
                String cadi_truststore_password = access.getProperty(Config.CADI_TRUSTSTORE_PASSWORD);
                if (cadi_truststore_password==null) {
                    cadi_truststore_password=AAFSSO.cons.readLine("cadi_truststore_password[%s]=","changeit");
                    cadi_truststore_password = access.encrypt(cadi_truststore_password);
                    access.setProperty(Config.CADI_TRUSTSTORE_PASSWORD, cadi_truststore_password);
                    addProp(Config.CADI_TRUSTSTORE_PASSWORD, cadi_truststore_password);
                }
            }
            ok = err==null;
        }
        String locateUrl = Config.getAAFLocateUrl(access);
        if (locateUrl==null) {
            locateUrl=AAFSSO.cons.readLine("AAF Locator URL=https://");
            if (locateUrl==null || locateUrl.length()==0) {
                err = new StringBuilder(Config.AAF_LOCATE_URL);
                err.append(" is required.");
                ok = false;
                return;
            } else {
                locateUrl="https://"+locateUrl;
            }
            access.setProperty(Config.AAF_LOCATE_URL, locateUrl);
            addProp(Config.AAF_LOCATE_URL, locateUrl);
            try {
            	if(access.getProperty(Config.AAF_URL)==null) {
            		access.setProperty(Config.AAF_URL, Defaults.AAF_ROOT+".service:"+Defaults.AAF_VERSION);
            	}
				AAFCon<?> aafCon = AAFCon.newInstance(access);
		    	Future<Configuration> acf;
				RosettaDF<Configuration> configDF = new RosettaEnv().newDataFactory(Configuration.class);
				acf = aafCon.client(new SingleEndpointLocator(locateUrl))
				        .read("/configure/"+user+"/aaf", configDF);
		        if (acf.get(aafCon.connTimeout)) {
		        	for(Props p : acf.value.getProps()) {
		        		addProp(p.getTag(),p.getValue());
		        		if(access.getProperty(p.getTag())==null) {
		        			access.setProperty(p.getTag(), p.getValue());
		        		}
		        	}
		        } else {
		        	access.log(Level.INFO,acf.body());
		        }
			} catch (LocatorException | APIException | URISyntaxException e) {
				access.log(e);
			}
        }
        
        final String apiVersion = access.getProperty(Config.AAF_API_VERSION, Config.AAF_DEFAULT_API_VERSION);
        final String aaf_root_ns = access.getProperty(Config.AAF_ROOT_NS);
        String locateRoot;
        if(aaf_root_ns==null) {
        	locateRoot=Defaults.AAF_ROOT;
        } else {
        	locateRoot = Defaults.AAF_LOCATE_CONST + "/%CNS." + aaf_root_ns;
        }
        if(access.getProperty(Config.AAF_URL)==null) {
        	access.setProperty(Config.AAF_URL, locateRoot+".service:"+apiVersion);
        }

        writeFiles();
    }

    public void setLogDefault() {
        this.setLogDefault(PropAccess.DEFAULT);
        System.setOut(stdOutOrig);
    }

    public void setStdErrDefault() {
        access.setLogLevel(PropAccess.DEFAULT);
        System.setErr(stdErrOrig);
    }

    public void setLogDefault(Level level) {
        if (access!=null) {
            access.setLogLevel(level);
        }
        System.setOut(stdOutOrig);
    }

    public boolean loginOnly() {
        return loginOnly;
    }

    public void addProp(String key, String value) {
        if (key==null || value==null) {
            return;
        }
        touchDiskprops=true;
        diskprops.setProperty(key, value);
    }

    public void writeFiles() throws IOException {
        if (touchDiskprops) {
            // Store Creds, if they work
            if (diskprops != null) {
                if (!dot_aaf.exists()) {
                    dot_aaf.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(sso);
                try {
                    diskprops.store(fos, "AAF Single Signon");
                } finally {
                    fos.close();
                }
            }
            if (sso != null) {
                setReadonly(sso);
                sso.setWritable(true, true);
            }
        }
    }

    public PropAccess access() {
        return access;
    }

    public StringBuilder err() {
        return err;
    }

    public String user() {
        return user;
    }

    public String enc_pass() {
        return encrypted_pass;
    }

    public boolean useX509() {
        return use_X509;
    }

    public void close() {
        if (close != null) {
            try {
                close.invoke(null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // nothing to do here.
            }
            close = null;
        }
    }

    private List<String> parseArgs(String[] args)
    {
        List<String> larg = new ArrayList<>(args.length);

        // Cover for bash's need to escape *.. (\\*)
        // also, remove SSO if required
        for (int i = 0; i < args.length; ++i) {
            if ("\\*".equals(args[i])) {
                args[i] = "*";
            }

            if ("-logout".equalsIgnoreCase(args[i])) {
                removeSSO = true;
            } else if ("-login".equalsIgnoreCase(args[i])) {
                loginOnly = true;
            } else if ("-noexit".equalsIgnoreCase(args[i])) {
                doExit = false;
            } else {
                larg.add(args[i]);
            }
        }
        return larg;
    }
    
    private void setReadonly(File file) {
        file.setExecutable(false, false);
        file.setWritable(false, false);
        file.setReadable(false, false);
        file.setReadable(true, true);
    }

    public boolean ok() {
        return ok;
    }
    
    public static interface ProcessArgs {
        public Properties process(final String[] args, final Properties props);
    }
}
