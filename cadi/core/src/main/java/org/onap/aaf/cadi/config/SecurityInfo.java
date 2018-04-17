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
	public static final String SslKeyManagerFactoryAlgorithm;
	
	private SSLSocketFactory scf;
	private X509KeyManager[] km;
	private X509TrustManager[] tm;
	public final String default_alias;
	private NetMask[] trustMasks;
	private SSLContext ctx;
	private HostnameVerifier maskHV;
	public final Access access;

	// Change Key Algorithms for IBM's VM.  Could put in others, if needed.
	static {
		if(System.getProperty("java.vm.vendor").equalsIgnoreCase("IBM Corporation")) {
			SslKeyManagerFactoryAlgorithm = "IbmX509";
		} else {
			SslKeyManagerFactoryAlgorithm = "SunX509";
		}
	}
	

	public SecurityInfo(final Access access) throws CadiException {
		try {
			this.access = access;
			// reuse DME2 Properties for convenience if specific Properties don't exist
			
			initializeKeyManager();
			
			initializeTrustManager();
			
			default_alias = access.getProperty(Config.CADI_ALIAS, null);
			
			initializeTrustMasks();

			String https_protocols = Config.logProp(access, Config.CADI_PROTOCOLS,
						access.getProperty(HTTPS_PROTOCOLS, HTTPS_PROTOCOLS_DEFAULT)
						);
			System.setProperty(HTTPS_PROTOCOLS, https_protocols);
			System.setProperty(JDK_TLS_CLIENT_PROTOCOLS, https_protocols);
			if("1.7".equals(System.getProperty("java.specification.version")) && https_protocols.contains("TLSv1.2")) {
				System.setProperty(Config.HTTPS_CIPHER_SUITES, Config.HTTPS_CIPHER_SUITES_DEFAULT);
			}			

			ctx = SSLContext.getInstance("TLS");
			ctx.init(km, tm, null);
			SSLContext.setDefault(ctx);
			scf = ctx.getSocketFactory();
		} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateException | UnrecoverableKeyException | IOException e) {
			throw new CadiException(e);
		}
	}

	/**
	 * @return the scf
	 */
	public SSLSocketFactory getSSLSocketFactory() {
		return scf;
	}

	public SSLContext getSSLContext() {
		return ctx;
	}

	/**
	 * @return the km
	 */
	public X509KeyManager[] getKeyManagers() {
		return km;
	}

	public void checkClientTrusted(X509Certificate[] certarr) throws CertificateException {
		for(X509TrustManager xtm : tm) {
			xtm.checkClientTrusted(certarr, SECURITY_ALGO);
		}
	}

	public void checkServerTrusted(X509Certificate[] certarr) throws CertificateException {
		for(X509TrustManager xtm : tm) {
			xtm.checkServerTrusted(certarr, SECURITY_ALGO);
		}
	}

	public void setSocketFactoryOn(HttpsURLConnection hsuc) {
		hsuc.setSSLSocketFactory(scf);
		if(maskHV != null && !maskHV.equals(hsuc.getHostnameVerifier())) {
			hsuc.setHostnameVerifier(maskHV);
		}
	}
	
	protected void initializeKeyManager() throws CadiException, IOException, NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException {
		String keyStore = access.getProperty(Config.CADI_KEYSTORE, null);
		if(keyStore != null && !new File(keyStore).exists()) {
			throw new CadiException(keyStore + " does not exist");
		}

		String keyStorePasswd = access.getProperty(Config.CADI_KEYSTORE_PASSWORD, null);
		keyStorePasswd = (keyStorePasswd == null) ? null : access.decrypt(keyStorePasswd, false);

		String keyPasswd = access.getProperty(Config.CADI_KEY_PASSWORD, null);
		keyPasswd = (keyPasswd == null) ? keyStorePasswd : access.decrypt(keyPasswd, false);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance(SslKeyManagerFactoryAlgorithm);
		if(keyStore == null || keyStorePasswd == null) { 
			km = new X509KeyManager[0];
		} else {
			ArrayList<X509KeyManager> kmal = new ArrayList<X509KeyManager>();
			File file;
			for(String ksname : keyStore.split(REGEX_COMMA)) {
				file = new File(ksname);
				String keystoreFormat;
				if(ksname.endsWith(".p12") || ksname.endsWith(".pkcs12")) {
					keystoreFormat = "PKCS12";
				} else {
					keystoreFormat = "JKS";
				}
				if(file.exists()) {
					FileInputStream fis = new FileInputStream(file);
					try {
						KeyStore ks = KeyStore.getInstance(keystoreFormat);
						ks.load(fis, keyStorePasswd.toCharArray());
						kmf.init(ks, keyPasswd.toCharArray());
					} finally {
						fis.close();
					}
				}
			}
			for(KeyManager km : kmf.getKeyManagers()) {
				if(km instanceof X509KeyManager) {
					kmal.add((X509KeyManager)km);
				}
			}
			km = new X509KeyManager[kmal.size()];
			kmal.toArray(km);
		}
	}

	protected void initializeTrustManager() throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException, CadiException {
		String trustStore = access.getProperty(Config.CADI_TRUSTSTORE, null);
		if(trustStore != null && !new File(trustStore).exists()) {
			throw new CadiException(trustStore + " does not exist");
		}

		String trustStorePasswd = access.getProperty(Config.CADI_TRUSTSTORE_PASSWORD, null);
		trustStorePasswd = (trustStorePasswd == null) ? "changeit"/*defacto Java Trust Pass*/ : access.decrypt(trustStorePasswd, false);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance(SslKeyManagerFactoryAlgorithm);
		if(trustStore != null) {
			File file;
			for(String tsname : trustStore.split(REGEX_COMMA)) {
				file = new File(tsname);
				if(file.exists()) {
					FileInputStream fis = new FileInputStream(file);
					try {
						KeyStore ts = KeyStore.getInstance("JKS");
						ts.load(fis, trustStorePasswd.toCharArray());
						tmf.init(ts); 
					} finally {
						fis.close();
					}
				}
			}

			TrustManager tms[] = tmf.getTrustManagers();
			if(tms != null) {
				tm = new X509TrustManager[(tms == null) ? 0 : tms.length];
				for(int i = 0; i < tms.length; ++i) {
					try {
						tm[i] = (X509TrustManager)tms[i];
					} catch (ClassCastException e) {
						access.log(Level.WARN, "Non X509 TrustManager", tm[i].getClass().getName(), "skipped in SecurityInfo");
					}
				}
			}
		}

	}
	
	protected void initializeTrustMasks() throws AccessException {
		String tips = access.getProperty(Config.CADI_TRUST_MASKS, null);
		if(tips != null) {
			access.log(Level.INIT, "Explicitly accepting valid X509s from", tips);
			String[] ipsplit = tips.split(REGEX_COMMA);
			trustMasks = new NetMask[ipsplit.length];
			for(int i = 0; i < ipsplit.length; ++i) {
				try {
					trustMasks[i] = new NetMask(ipsplit[i]);
				} catch (MaskFormatException e) {
					throw new AccessException("Invalid IP Mask in " + Config.CADI_TRUST_MASKS, e);
				}
			}
		}
		
		if(trustMasks != null) {
			final HostnameVerifier origHV = HttpsURLConnection.getDefaultHostnameVerifier();
			HttpsURLConnection.setDefaultHostnameVerifier(maskHV = new HostnameVerifier() {
				@Override
				public boolean verify(final String urlHostName, final SSLSession session) {
					try {
						// This will pick up /etc/host entries as well as DNS
						InetAddress ia = InetAddress.getByName(session.getPeerHost());
						for(NetMask tmask : trustMasks) {
							if(tmask.isInNet(ia.getHostAddress())) {
								return true;
							}
						}
					} catch (UnknownHostException e) {
						// It's ok. do normal Verify
					}
					return origHV.verify(urlHostName, session);
				};
			});
		}
	}
	
}
