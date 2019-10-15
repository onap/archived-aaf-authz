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
import java.io.IOException;
import java.io.PrintStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.util.Chmod;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public abstract class ArtifactDir implements PlaceArtifact {

    protected static final String C_R = "\n";
    protected File dir;

    // This checks for multiple passes of Dir on the same objects.  Run clear after done.
    protected final static Map<String,Object> processed = new HashMap<>();
    private static final Map<String, Symm> symms = new HashMap<>();

    /**
     * Note:  Derived Classes should ALWAYS call "super.place(cert,arti)" first, and
     * then "placeProperties(arti)" just after they implement
     */
    @Override
    public final boolean place(Trans trans, CertInfo certInfo, Artifact arti, String machine) throws CadiException {
        validate(arti);

        try {
            PropHolder cred = PropHolder.get(arti,"cred.props");

            // Obtain/setup directory as required
            dir = new File(arti.getDir());
            if (processed.get("dir")==null) {
                if (!dir.exists()) {
                    Chmod.to755.chmod(dir);
                    if (!dir.mkdirs()) {
                        throw new CadiException("Could not create " + dir);
                    }
                }

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
                cred.add(Config.CADI_X509_ISSUERS,issuers.toString());

                cred.addEnc("Challenge", certInfo.getChallenge());
            }

            _place(trans, certInfo,arti);

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

    public static void write(File f, Chmod c, String ... data) throws IOException {
        System.out.println("Writing file " + f.getCanonicalPath());
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
        System.out.println("Writing file " + f.getCanonicalPath());
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
        System.out.println("Writing file " + f.getCanonicalPath());
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

    // Get the Symm associated with specific File (there can be several active at once)
    public synchronized static final Symm getSymm(File f) throws IOException {
        Symm symm = symms.get(f.getCanonicalPath());
        if(symm==null) {
            if (!f.exists()) {
                write(f,Chmod.to400,Symm.keygen());
//            } else {
//                System.out.println("Encryptor using " + f.getCanonicalPath());
            }
            symm = Symm.obtain(f);
            symms.put(f.getCanonicalPath(),symm);
        }
        return symm;
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

    public static void clear() {
        processed.clear();
    }

}
