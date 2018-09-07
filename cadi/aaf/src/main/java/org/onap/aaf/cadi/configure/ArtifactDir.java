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

package org.onap.aaf.cadi.configure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.util.Chmod;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public abstract class ArtifactDir implements PlaceArtifact {

    protected static final String C_R = "\n";
    protected File dir;
    private List<String> encodeds = new ArrayList<>();
    
    private Symm symm;
    // This checks for multiple passes of Dir on the same objects.  Run clear after done.
    protected static Map<String,Object> processed = new HashMap<>();


    /**
     * Note:  Derived Classes should ALWAYS call "super.place(cert,arti)" first, and 
     * then "placeProperties(arti)" just after they implement
     */
    @Override
    public final boolean place(Trans trans, CertInfo certInfo, Artifact arti, String machine) throws CadiException {
        validate(arti);
        
        try {
            // Obtain/setup directory as required
            dir = new File(arti.getDir());
            if (processed.get("dir")==null) {
                if (!dir.exists()) {
                    Chmod.to755.chmod(dir);
                    if (!dir.mkdirs()) {
                        throw new CadiException("Could not create " + dir);
                    }
                }
                
                // Also place cm_url and Host Name
                addProperty(Config.CM_URL,trans.getProperty(Config.CM_URL));
//                addProperty(Config.HOSTNAME,machine);
//                addProperty(Config.AAF_ENV,certInfo.getEnv());
                // Obtain Issuers
                boolean first = true;
                StringBuilder issuers = new StringBuilder();
                for (String dn : certInfo.getCaIssuerDNs()) {
                    if (first) {
                        first=false;
                    } else {
                        issuers.append(':');
                    }
                    issuers.append(dn);
                }
                addProperty(Config.CADI_X509_ISSUERS,issuers.toString());
            }
            symm = (Symm)processed.get("symm");
            if (symm==null) {
                // CADI Key Gen
                File f = new File(dir,arti.getNs() + ".keyfile");
                if (!f.exists()) {
                    write(f,Chmod.to400,Symm.keygen());
                }
                symm = Symm.obtain(f); 

                addEncProperty("ChallengePassword", certInfo.getChallenge());
                
                processed.put("symm",symm);
            }

            _place(trans, certInfo,arti);
            
            placeProperties(arti);
            
            processed.put("dir",dir);

        } catch (Exception e) {
            throw new CadiException(e);
        }
        return true;
    }

    /**
     * Derived Classes implement this instead, so Dir can process first, and write any Properties last
     * @param cert
     * @param arti
     * @return
     * @throws CadiException
     */
    protected abstract boolean _place(Trans trans, CertInfo certInfo, Artifact arti) throws CadiException;

    protected void addProperty(String tag, String value) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(tag);
        sb.append('=');
        sb.append(value);
        encodeds.add(sb.toString());
    }

    protected void addEncProperty(String tag, String value) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(tag);
        sb.append('=');
        sb.append("enc:");
        sb.append(symm.enpass(value));
        encodeds.add(sb.toString());
    }

    public static void write(File f, Chmod c, String ... data) throws IOException {
        f.setWritable(true,true);
        
        FileOutputStream fos = new FileOutputStream(f);
        PrintStream ps = new PrintStream(fos);
        try {
            for (String s : data) {
                ps.print(s);
            }
        } finally {
            ps.close();
            c.chmod(f);
        }
    }

    public static void write(File f, Chmod c, byte[] bytes) throws IOException {
        f.setWritable(true,true);
        
        FileOutputStream fos = new FileOutputStream(f);
        try {
            fos.write(bytes);
        } finally {
            fos.close();
            c.chmod(f);
        }
    }
    
    public static void write(File f, Chmod c, KeyStore ks, char[] pass ) throws IOException, CadiException {
        f.setWritable(true,true);
        
        FileOutputStream fos = new FileOutputStream(f);
        try {
            ks.store(fos, pass);
        } catch (Exception e) {
            throw new CadiException(e);
        } finally {
            fos.close();
            c.chmod(f);
        }
    }


    private void validate(Artifact a) throws CadiException {
        StringBuilder sb = new StringBuilder();
        if (a.getDir()==null) {
            sb.append("File Artifacts require a path");
        }

        if (a.getNs()==null) {
            if (sb.length()>0) {
                sb.append('\n');
            }
            sb.append("File Artifacts require an AAF Namespace");
        }
        
        if (sb.length()>0) {
            throw new CadiException(sb.toString());
        }
    }

    private boolean placeProperties(Artifact arti) throws CadiException {
        if (encodeds.size()==0) {
            return true;
        }
        boolean first=processed.get("dir")==null;
        try {
            File f = new File(dir,arti.getNs()+".cred.props");
            if (f.exists()) {
                if (first) {
                    File backup = File.createTempFile(f.getName()+'.', ".backup",dir);
                    f.renameTo(backup);
                } else {
                    f.setWritable(true);
                }
            }
            
            // Append if not first
            PrintWriter pw = new PrintWriter(new FileWriter(f,!first));
            try {
                // Write a Header
                if (first) {
                    for (int i=0;i<60;++i) {
                        pw.print('#');
                    }
                    pw.println();
                    pw.println("# Properties Generated by AT&T Certificate Manager");
                    pw.print("#   by ");
                    pw.println(System.getProperty("user.name"));
                    pw.print("#   on ");
                    pw.println(Chrono.dateStamp());
                    pw.println("# @copyright 2016, AT&T");
                    for (int i=0;i<60;++i) {
                        pw.print('#');
                    }
                    pw.println();
                    for (String prop : encodeds) {
                        if (    prop.startsWith("cm_") 
                            || prop.startsWith(Config.HOSTNAME)
                            || prop.startsWith(Config.AAF_ENV)) {
                            pw.println(prop);
                        }
                    }
                }
            
                for (String prop : encodeds) {
                    if (prop.startsWith("cadi")) {
                        pw.println(prop);
                    }
                }
            } finally {
                pw.close();
            }
            Chmod.to644.chmod(f);
            
            if (first) {
                // Challenge
                f = new File(dir,arti.getNs()+".chal");
                if (f.exists()) {
                    f.delete();
                }
                pw = new PrintWriter(new FileWriter(f));
                try {
                    for (String prop : encodeds) {
                        if (prop.startsWith("Challenge")) {
                            pw.println(prop);
                        }
                    }
                } finally {
                    pw.close();
                }
                Chmod.to400.chmod(f);
            }
        } catch (Exception e) {
            throw new CadiException(e);
        }
        return true;
    }
    
    public static void clear() {
        processed.clear();
    }

}
