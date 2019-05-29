/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ================================================================================
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
package org.onap.aaf.auth.cm.ca;

import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.Client;
import org.jscep.client.ClientException;
import org.jscep.client.EnrollmentResponse;
import org.onap.aaf.auth.cm.cert.BCFactory;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Locator.Item;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.configure.CertException;
import org.onap.aaf.cadi.locator.HotPeerLocator;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Split;

public class JscepCA extends CA {
    static final String CA_PREFIX = "http://";
    static final String CA_POSTFIX="/certsrv/mscep_admin/mscep.dll";

    private static final String MS_PROFILE="1";
    private static final int MAX_RETRY=3;
    public static final long INVALIDATE_TIME = 1000*60*10L; // 10 mins

    // package on purpose
    private Map<String,X509ChainWithIssuer> mxcwiS;
    private Map<Client,X509ChainWithIssuer> mxcwiC;


    private JscepClientLocator clients;

    public JscepCA(final Access access, final String name, final String env, String [][] params) throws IOException, CertException, LocatorException {
         super(access, name, env);
         mxcwiS = new ConcurrentHashMap<>();
         mxcwiC = new ConcurrentHashMap<>();
         
         if (params.length<2) {
             throw new CertException("No Trust Chain parameters are included");
         } 
         if (params[0].length<2) {
             throw new CertException("User/Password required for JSCEP");
         }
         final String id = params[0][0];
         final String pw = params[0][1]; 
        
        // Set this for NTLM password Microsoft
        Authenticator.setDefault(new Authenticator() {
              public PasswordAuthentication getPasswordAuthentication () {
                    try {
                        return new PasswordAuthentication (id,access.decrypt(pw,true).toCharArray());
                    } catch (IOException e) {
                        access.log(e);
                    }
                    return null;
              }
        });
        
        StringBuilder urlstr = new StringBuilder();

        for (int i=1;i<params.length;++i) { // skip first section, which is user/pass
            // Work 
            if (i>1) {
                urlstr.append(','); // delimiter
            }
            urlstr.append(params[i][0]);
            
            String dir = access.getProperty(CM_PUBLIC_DIR, "");
            if (!"".equals(dir) && !dir.endsWith("/")) {
                dir = dir + '/';
            }
            String path;
            List<FileReader> frs = new ArrayList<>(params.length-1);
            try {
                for (int j=1; j<params[i].length; ++j) { // first 3 taken up, see above
                    path = !params[i][j].contains("/")?dir+params[i][j]:params[i][j];
                    access.printf(Level.INIT, "Loading a TrustChain Member for %s from %s",name, path);
                    frs.add(new FileReader(path));
                }
                X509ChainWithIssuer xcwi = new X509ChainWithIssuer(frs);
                addCaIssuerDN(xcwi.getIssuerDN());
                mxcwiS.put(params[i][0],xcwi);
            } finally {
                for (FileReader fr : frs) {
                    if (fr!=null) {
                        fr.close();
                    }
                }
            }
        }        
        clients = new JscepClientLocator(access,urlstr.toString());
    }

    // package on purpose
    
    @Override
    public X509ChainWithIssuer sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException {
        TimeTaken tt = trans.start("Generating CSR and Keys for New Certificate", Env.SUB);
        PKCS10CertificationRequest csr;
        try {
            csr = csrmeta.generateCSR(trans);
            if (trans.info().isLoggable()) {
                trans.info().log(BCFactory.toString(csr));
            } 
            if (trans.info().isLoggable()) {
                trans.info().log(csr);
            }
        } finally {
            tt.done();
        }
        
        tt = trans.start("Enroll CSR", Env.SUB);
        Client client = null;
        Item item = null;
        for (int i=0; i<MAX_RETRY;++i) {
            try {
                item = clients.best();
                client = clients.get(item);
                
                EnrollmentResponse er = client.enrol(
                        csrmeta.initialConversationCert(trans),
                        csrmeta.keypair(trans).getPrivate(),
                        csr,
                        MS_PROFILE /* profile... MS can't deal with blanks*/);
                
                while (true) {
                    if (er.isSuccess()) {
                        trans.checkpoint("Cert from " + clients.info(item));
                        X509Certificate x509 = null;
                        for ( Certificate cert : er.getCertStore().getCertificates(null)) {
                            if (x509==null) {
                                x509 = (X509Certificate)cert;
                                break;
                            }
                        }
                        X509ChainWithIssuer mxcwi = mxcwiC.get(client);
                        return new X509ChainWithIssuer(mxcwi,x509);

                    } else if (er.isPending()) {
                        trans.checkpoint("Polling, waiting on CA to complete");
                        Thread.sleep(3000);
                    } else if (er.isFailure()) {
                        throw new CertException(clients.info(item)+':'+er.getFailInfo().toString());
                    }
                }
            } catch (LocatorException e) {
                trans.error().log(e);
                i=MAX_RETRY;
            } catch (ClientException e) {
                trans.error().log(e,"SCEP Client Error, Temporarily Invalidating Client: " + clients.info(item));
                try  { 
                    clients.invalidate(client);
                    if (!clients.hasItems()) {
                        clients.refresh();
                    }
                } catch (LocatorException e1) {
                    trans.error().log(e,clients.info(item));
                    i=MAX_RETRY;  // can't go any further
                }
            } catch (Exception e) {
                trans.error().log(e);
                i=MAX_RETRY;
            } finally {
                tt.done();
            }
        }
        
        return null;
    }
    
    /**
     * Locator specifically for Jscep Clients.
     * 
     * Class based client for access to common Map
     */
    private class JscepClientLocator extends HotPeerLocator<Client> {

        protected JscepClientLocator(Access access, String urlstr)throws LocatorException {
            super(access, urlstr, JscepCA.INVALIDATE_TIME,
                 access.getProperty("cadi_latitude","39.833333"), //Note: Defaulting to GEO center of US
                 access.getProperty("cadi_longitude","-98.583333")
                 );
        }

        @Override
        protected Client _newClient(String urlinfo) throws LocatorException {
            try {
                String[] info = Split.split('/', urlinfo);
                Client c = new Client(new URL(JscepCA.CA_PREFIX + info[0] + JscepCA.CA_POSTFIX),
                        cert -> {
                            //TODO checkIssuer
                            return true;
                        }
                );
                // Map URL to Client, because Client doesn't expose Connection
                mxcwiC.put(c, mxcwiS.get(urlinfo));
                return c;
            } catch (MalformedURLException e) {
                throw new LocatorException(e);
            }
        }

        @Override
        protected Client _invalidate(Client client) {
            return null;
        }

        @Override
        protected void _destroy(Client client) {
            mxcwiC.remove(client);
        }
        
        
    }
}
