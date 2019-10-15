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
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.util.Chmod;
import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class PlaceArtifactInKeystore extends ArtifactDir {
    private String kst;

    public PlaceArtifactInKeystore(String kst) {
        this.kst = kst;
    }

    @Override
    public boolean _place(Trans trans, CertInfo certInfo, Artifact arti) throws CadiException {
        final String ext = (kst==Agent.PKCS12?"p12":kst);
        File fks = new File(dir,arti.getNs()+'.'+ext);
        try {
            KeyStore jks = KeyStore.getInstance(kst);
            if (fks.exists()) {
                File backup = File.createTempFile(fks.getName()+'.', ".backup",dir);
                fks.renameTo(backup);
            }

            // Get the Cert(s)... Might include Trust store
            Collection<? extends Certificate> certColl = Factory.toX509Certificate(certInfo.getCerts());
            // find where the trusts end in 1.0 API
    
            X509Certificate x509;
            List<X509Certificate> chainList = new ArrayList<>();
            Set<X509Certificate> caSet = new HashSet<>();
            X509Certificate curr = null;
            for (Certificate c : certColl) {
                x509 = (X509Certificate)c;
                // Is a Root (self-signed, anyway)
                if (x509.getSubjectDN().equals(x509.getIssuerDN())) {
                    caSet.add(x509);
                } else {
                    // Expect Certs in Trust Chain Order. 
                    if(curr==null) {
                        chainList.add(x509);
                        curr=x509;
                    } else {
                        // Only Add Cert next on the list
                        if(curr.getIssuerDN().equals(x509.getSubjectDN())) {
                            chainList.add(x509);
                            curr=x509;
                        }
                    }
                }
            }

            // Properties, etc
            // Add CADI Keyfile Entry to Properties
            File keyfile = new File(arti.getDir()+'/'+arti.getNs() + ".keyfile");
            PropHolder props = PropHolder.get(arti, "cred.props");
            props.add(Config.CADI_KEYFILE,keyfile.getAbsolutePath());

            // Set Keystore Password
            props.add(Config.CADI_KEYSTORE,fks.getAbsolutePath());
            String keystorePass = Symm.randomGen(Agent.PASS_SIZE);
            String encP = props.addEnc(Config.CADI_KEYSTORE_PASSWORD,keystorePass);
            // Since there are now more than one Keystore type, the keystore password property might
            // be overwritten, making the store useless without key. So we write it specifically
            // as well.
            props.add(Config.CADI_KEYSTORE_PASSWORD+'_'+ext,encP);
            char[] keystorePassArray = keystorePass.toCharArray();
            jks.load(null,keystorePassArray); // load in
        
            // Add Private Key/Cert Entry for App
            // Note: Java SSL security classes, while having a separate key from keystore,
            // is documented to not actually work. 
            // java.security.UnrecoverableKeyException: Cannot recover key
            // You can create a custom Key Manager to make it work, but Practicality  
            // dictates that you live with the default, meaning, they are the same
            String keyPass = keystorePass; //Symm.randomGen(CmAgent.PASS_SIZE);
            PrivateKey pk = Factory.toPrivateKey(trans, certInfo.getPrivatekey());
            props.addEnc(Config.CADI_KEY_PASSWORD, keyPass);
            props.add(Config.CADI_ALIAS, arti.getMechid());
//            Set<Attribute> attribs = new HashSet<>();
//            if (kst.equals("pkcs12")) {
//                // Friendly Name
//                attribs.add(new PKCS12Attribute("1.2.840.113549.1.9.20", arti.getNs()));
//            } 
//        
            KeyStore.ProtectionParameter protParam = 
                    new KeyStore.PasswordProtection(keyPass.toCharArray());
        
            Certificate[] trustChain = new Certificate[chainList.size()];
            chainList.toArray(trustChain);
            KeyStore.PrivateKeyEntry pkEntry = 
                new KeyStore.PrivateKeyEntry(pk, trustChain);
            jks.setEntry(arti.getMechid(), 
                    pkEntry, protParam);

            // Write out
            write(fks,Chmod.to644,jks,keystorePassArray);
        
            // Change out to TrustStore
            // NOTE: PKCS12 does NOT support Trusted Entries.  Put in JKS Always
            fks = new File(dir,arti.getNs()+".trust.jks");
            if (fks.exists()) {
                File backup = File.createTempFile(fks.getName()+'.', ".backup",dir);
                fks.renameTo(backup);
            }

            jks = KeyStore.getInstance(Agent.JKS);
        
            // Set Truststore Password
            props.add(Config.CADI_TRUSTSTORE,fks.getAbsolutePath());
            String trustStorePass = Symm.randomGen(Agent.PASS_SIZE);
            props.addEnc(Config.CADI_TRUSTSTORE_PASSWORD,trustStorePass);
            char[] truststorePassArray = trustStorePass.toCharArray();
            jks.load(null,truststorePassArray); // load in
        
            // Add Trusted Certificates, but PKCS12 doesn't support
            Certificate[] trustCAs = new Certificate[caSet.size()];
            caSet.toArray(trustCAs);
            for (int i=0; i<trustCAs.length;++i) {
                jks.setCertificateEntry("ca_" + arti.getCa() + '_' + i, trustCAs[i]);
            }
            // Write out
            write(fks,Chmod.to644,jks,truststorePassArray);
            return true;
        } catch (Exception e) {
            throw new CadiException(e);
        }
    }

}
