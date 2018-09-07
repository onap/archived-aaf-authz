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

package org.onap.aaf.cadi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.util.MaskFormatException;
import org.onap.aaf.cadi.util.NetMask;

public class SecurityInfo {
    private static final String SECURITY_ALGO = "RSA";
    private static final String HTTPS_PROTOCOLS = "https.protocols";
    private static final String JDK_TLS_CLIENT_PROTOCOLS = "jdk.tls.client.protocols";

    public static final String HTTPS_PROTOCOLS_DEFAULT = "TLSv1.1,TLSv1.2";
    public static final String REGEX_COMMA = "\\s*,\\s*";
    public static final String SSL_KEY_MANAGER_FACTORY_ALGORITHM;
    
    private SSLSocketFactory socketFactory;
    private X509KeyManager[] x509KeyManager;
    private X509TrustManager[] x509TrustManager;
    public final String defaultAlias;
    private NetMask[] trustMasks;
    private SSLContext context;
    private HostnameVerifier maskHV;
    public final Access access;

    // Change Key Algorithms for IBM's VM.  Could put in others, if needed.
    static {
        if ("IBM Corporation".equalsIgnoreCase(System.getProperty("java.vm.vendor"))) {
            SSL_KEY_MANAGER_FACTORY_ALGORITHM = "IbmX509";
        } else {
            SSL_KEY_MANAGER_FACTORY_ALGORITHM = "SunX509";
        }
    }
    

    public SecurityInfo(final Access access) throws CadiException {
        try {
            this.access = access;
            // reuse DME2 Properties for convenience if specific Properties don't exist
            
            initializeKeyManager();
            
            initializeTrustManager();
            
            defaultAlias = access.getProperty(Config.CADI_ALIAS, null);
            
            initializeTrustMasks();

            String httpsProtocols = Config.logProp(access, Config.CADI_PROTOCOLS,
                        access.getProperty(HTTPS_PROTOCOLS, HTTPS_PROTOCOLS_DEFAULT)
                        );
            System.setProperty(HTTPS_PROTOCOLS, httpsProtocols);
            System.setProperty(JDK_TLS_CLIENT_PROTOCOLS, httpsProtocols);
            if ("1.7".equals(System.getProperty("java.specification.version")) && httpsProtocols.contains("TLSv1.2")) {
                System.setProperty(Config.HTTPS_CIPHER_SUITES, Config.HTTPS_CIPHER_SUITES_DEFAULT);
            }            

            context = SSLContext.getInstance("TLS");
            context.init(x509KeyManager, x509TrustManager, null);
            SSLContext.setDefault(context);
            socketFactory = context.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | UnrecoverableKeyException | IOException e) {
            throw new CadiException(e);
        }
    }

    /**
     * @return the scf
     */
    public SSLSocketFactory getSSLSocketFactory() {
        return socketFactory;
    }

    public SSLContext getSSLContext() {
        return context;
    }

    /**
     * @return the km
     */
    public X509KeyManager[] getKeyManagers() {
        return x509KeyManager;
    }

    public void checkClientTrusted(X509Certificate[] certarr) throws CertificateException {
        for (X509TrustManager xtm : x509TrustManager) {
            xtm.checkClientTrusted(certarr, SECURITY_ALGO);
        }
    }

    public void checkServerTrusted(X509Certificate[] certarr) throws CertificateException {
        for (X509TrustManager xtm : x509TrustManager) {
            xtm.checkServerTrusted(certarr, SECURITY_ALGO);
        }
    }

    public void setSocketFactoryOn(HttpsURLConnection hsuc) {
        hsuc.setSSLSocketFactory(socketFactory);
        if (maskHV != null && !maskHV.equals(hsuc.getHostnameVerifier())) {
            hsuc.setHostnameVerifier(maskHV);
        }
    }
    
    protected void initializeKeyManager() throws CadiException, IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException {
        String keyStore = access.getProperty(Config.CADI_KEYSTORE, null);
        if (keyStore != null && !new File(keyStore).exists()) {
            throw new CadiException(keyStore + " does not exist");
        }

        String keyStorePasswd = access.getProperty(Config.CADI_KEYSTORE_PASSWORD, null);
        keyStorePasswd = (keyStorePasswd == null) ? null : access.decrypt(keyStorePasswd, false);
        if (keyStore == null || keyStorePasswd == null) { 
            x509KeyManager = new X509KeyManager[0];
            return;
        }

        String keyPasswd = access.getProperty(Config.CADI_KEY_PASSWORD, null);
        keyPasswd = (keyPasswd == null) ? keyStorePasswd : access.decrypt(keyPasswd, false);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(SSL_KEY_MANAGER_FACTORY_ALGORITHM);

        ArrayList<X509KeyManager> keyManagers = new ArrayList<>();
        File file;
        for (String ksname : keyStore.split(REGEX_COMMA)) {
            String keystoreFormat;
            if (ksname.endsWith(".p12") || ksname.endsWith(".pkcs12")) {
                keystoreFormat = "PKCS12";
            } else {
                keystoreFormat = "JKS";
            }

            file = new File(ksname);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    KeyStore ks = KeyStore.getInstance(keystoreFormat);
                    ks.load(fis, keyStorePasswd.toCharArray());
                    keyManagerFactory.init(ks, keyPasswd.toCharArray());
                } finally {
                    fis.close();
                }
            }
        }
        for (KeyManager keyManager : keyManagerFactory.getKeyManagers()) {
            if (keyManager instanceof X509KeyManager) {
                keyManagers.add((X509KeyManager)keyManager);
            }
        }
        x509KeyManager = new X509KeyManager[keyManagers.size()];
        keyManagers.toArray(x509KeyManager);
    }

    protected void initializeTrustManager() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, CadiException {
        String trustStore = access.getProperty(Config.CADI_TRUSTSTORE, null);
        if (trustStore != null && !new File(trustStore).exists()) {
            throw new CadiException(trustStore + " does not exist");
        }

        if (trustStore == null) {
            return;
        }

        String trustStorePasswd = access.getProperty(Config.CADI_TRUSTSTORE_PASSWORD, null);
        trustStorePasswd = (trustStorePasswd == null) ? "changeit"/*defacto Java Trust Pass*/ : access.decrypt(trustStorePasswd, false);

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(SSL_KEY_MANAGER_FACTORY_ALGORITHM);
        File file;
        for (String trustStoreName : trustStore.split(REGEX_COMMA)) {
            file = new File(trustStoreName);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                try {
                    KeyStore ts = KeyStore.getInstance("JKS");
                    ts.load(fis, trustStorePasswd.toCharArray());
                    trustManagerFactory.init(ts); 
                } finally {
                    fis.close();
                }
            }
        }

        TrustManager trustManagers[] = trustManagerFactory.getTrustManagers();
        if (trustManagers == null || trustManagers.length == 0) {
            return;
        }

        x509TrustManager = new X509TrustManager[trustManagers.length];
        for (int i = 0; i < trustManagers.length; ++i) {
            try {
                x509TrustManager[i] = (X509TrustManager)trustManagers[i];
            } catch (ClassCastException e) {
                access.log(Level.WARN, "Non X509 TrustManager", x509TrustManager[i].getClass().getName(), "skipped in SecurityInfo");
            }
        }
    }
    
    protected void initializeTrustMasks() throws AccessException {
        String tips = access.getProperty(Config.CADI_TRUST_MASKS, null);
        if (tips == null) {
            return;
        }

        access.log(Level.INIT, "Explicitly accepting valid X509s from", tips);
        String[] ipsplit = tips.split(REGEX_COMMA);
        trustMasks = new NetMask[ipsplit.length];
        for (int i = 0; i < ipsplit.length; ++i) {
            try {
                trustMasks[i] = new NetMask(ipsplit[i]);
            } catch (MaskFormatException e) {
                throw new AccessException("Invalid IP Mask in " + Config.CADI_TRUST_MASKS, e);
            }
        }
    
        final HostnameVerifier origHV = HttpsURLConnection.getDefaultHostnameVerifier();
        maskHV = new HostnameVerifier() {
            @Override
            public boolean verify(final String urlHostName, final SSLSession session) {
                try {
                    // This will pick up /etc/host entries as well as DNS
                    InetAddress ia = InetAddress.getByName(session.getPeerHost());
                    for (NetMask tmask : trustMasks) {
                        if (tmask.isInNet(ia.getHostAddress())) {
                            return true;
                        }
                    }
                } catch (UnknownHostException e) {
                    // It's ok. do normal Verify
                }
                return origHV.verify(urlHostName, session);
            };
        };
        HttpsURLConnection.setDefaultHostnameVerifier(maskHV);
    }
    
}
