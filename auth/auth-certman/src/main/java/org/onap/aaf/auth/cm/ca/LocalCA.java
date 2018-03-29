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
package org.onap.aaf.auth.cm.ca;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.operator.OperatorCreationException;
import org.onap.aaf.auth.cm.cert.BCFactory;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.cert.RDN;
import org.onap.aaf.auth.env.NullTrans;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.cm.CertException;
import org.onap.aaf.cadi.cm.Factory;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

public class LocalCA extends CA {

	// Extensions
	private static final KeyPurposeId[] ASN_WebUsage = new KeyPurposeId[] {
				KeyPurposeId.id_kp_serverAuth, // WebServer
				KeyPurposeId.id_kp_clientAuth};// WebClient
				
	private final PrivateKey caKey;
	private final X500Name issuer;
	private final SecureRandom random = new SecureRandom();
	private byte[] serialish;
	private final X509ChainWithIssuer x509cwi; // "Cert" is CACert

	public LocalCA(Access access, final String name, final String env, final String[][] params) throws IOException, CertException {
		super(access, name, env);
		serialish = new byte[24];
		if(params.length<1 || params[0].length<2) {
			throw new IOException("LocalCA expects cm_ca.<ca name>=org.onap.aaf.auth.cm.ca.LocalCA,<full path to key file>[;<Full Path to Trust Chain, ending with actual CA>]+");
		}
		
		// Read in the Private Key
		String configured;
		File f = new File(params[0][0]);
		if(f.exists() && f.isFile()) {
			String fileName = f.getName();
			if(fileName.endsWith(".key")) {
				caKey = Factory.toPrivateKey(NullTrans.singleton(),f);
				List<FileReader> frs = new ArrayList<FileReader>(params.length-1);
				try {
					String dir = access.getProperty(CM_PUBLIC_DIR, "");
					if(!"".equals(dir) && !dir.endsWith("/")) {
						dir = dir + '/';
					}

					String path;
					for(int i=1; i<params[0].length; ++i) { // first param is Private Key, remainder are TrustChain
						path = !params[0][i].contains("/")?dir+params[0][i]:params[0][i];
						access.printf(Level.INIT, "Loading a TrustChain Member for %s from %s\n",name, path);
						frs.add(new FileReader(path));
					}
					x509cwi = new X509ChainWithIssuer(frs);
				} finally {
					for(FileReader fr : frs) {
						if(fr!=null) {
							fr.close();
						}
					}
				}
				configured = "Configured with " + fileName;
			} else {
				if(params.length<1 || params[0].length<3) {
					throw new CertException("LocalCA parameters must be <keystore [.p12|.pkcs12|.jks|.pkcs11(sun only)]; <alias>; enc:<encrypted Keystore Password>>");
				}
				try {
					Provider p;
					KeyStore keyStore;
					if(fileName.endsWith(".pkcs11")) {
						String ksType;
						p = Factory.getSecurityProvider(ksType="PKCS11",params);
						keyStore = KeyStore.getInstance(ksType,p);
					} else if(fileName.endsWith(".jks")) {
						keyStore = KeyStore.getInstance("JKS");
					} else if(fileName.endsWith(".p12") || fileName.endsWith(".pkcs12")) {
						keyStore = KeyStore.getInstance("PKCS12");
					} else {
						throw new CertException("Unknown Keystore type from filename " + fileName);
					}
					
					FileInputStream fis = new FileInputStream(f);
					KeyStore.ProtectionParameter keyPass;

					try {
						String pass = access.decrypt(params[0][2]/*encrypted passcode*/, true);
						if(pass==null) {
							throw new CertException("Passcode for " + fileName + " cannot be decrypted.");
						}
						char[] ksPass = pass.toCharArray();
						//Assuming Key Pass is same as Keystore Pass
						keyPass = new KeyStore.PasswordProtection(ksPass);

						keyStore.load(fis,ksPass);
					} finally {
						fis.close();
					}
					Entry entry = keyStore.getEntry(params[0][1]/*alias*/, keyPass);
					if(entry==null) {
						throw new CertException("There is no Keystore entry with name '" + params[0][1] +'\'');
					}
					PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry)entry;
					caKey = privateKeyEntry.getPrivateKey();
					
					x509cwi = new X509ChainWithIssuer(privateKeyEntry.getCertificateChain());
					configured =  "keystore \"" + fileName + "\", alias " + params[0][1];
				} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | UnrecoverableEntryException e) {
					throw new CertException("Exception opening Keystore " + fileName, e);
				}
			}
		} else {
			throw new CertException("Private Key, " + f.getPath() + ", does not exist");
		}
		
		X500NameBuilder xnb = new X500NameBuilder();
		for(RDN rnd : RDN.parse(',', x509cwi.getIssuerDN())) {
			xnb.addRDN(rnd.aoi,rnd.value);
		}
		issuer = xnb.build();
		access.printf(Level.INIT, "LocalCA is configured with %s.  The Issuer DN is %s.",
				configured, issuer.toString());
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.auth.cm.service.CA#sign(org.bouncycastle.pkcs.PKCS10CertificationRequest)
	 */
	@Override
	public X509andChain sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException {
		GregorianCalendar gc = new GregorianCalendar();
		Date start = gc.getTime();
		gc.add(GregorianCalendar.MONTH, 2);
		Date end = gc.getTime();
		X509Certificate x509;
		TimeTaken tt = trans.start("Create/Sign Cert",Env.SUB);
		try {
			BigInteger bi;
			synchronized(serialish) {
				random.nextBytes(serialish);
				bi = new BigInteger(serialish);
			}
				
			RSAPublicKey rpk = (RSAPublicKey)csrmeta.keypair(trans).getPublic();
			X509v3CertificateBuilder xcb = new X509v3CertificateBuilder(
					issuer,
					bi, // replace with Serialnumber scheme
					start,
					end,
					csrmeta.x500Name(),
					SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(new RSAKeyParameters(false,rpk.getModulus(),rpk.getPublicExponent()))
//					new SubjectPublicKeyInfo(ASN1Sequence.getInstance(caCert.getPublicKey().getEncoded()))
					);
			List<GeneralName> lsan = new ArrayList<GeneralName>();
			for(String s : csrmeta.sans()) {
				lsan.add(new GeneralName(GeneralName.dNSName,s));
			}
			GeneralName[] sans = new GeneralName[lsan.size()];
			lsan.toArray(sans);

		    JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
		    	xcb.addExtension(Extension.basicConstraints,
                    	false, new BasicConstraints(false))
		            .addExtension(Extension.keyUsage,
		                true, new KeyUsage(KeyUsage.digitalSignature
		                                 | KeyUsage.keyEncipherment))
		            .addExtension(Extension.extendedKeyUsage,
		                          true, new ExtendedKeyUsage(ASN_WebUsage))

                    .addExtension(Extension.authorityKeyIdentifier,
		                          false, extUtils.createAuthorityKeyIdentifier(x509cwi.cert))
		            .addExtension(Extension.subjectKeyIdentifier,
		                          false, extUtils.createSubjectKeyIdentifier(x509cwi.cert.getPublicKey()))
		            .addExtension(Extension.subjectAlternativeName,
		            		false, new GeneralNames(sans))
		                                           ;
	
			x509 = new JcaX509CertificateConverter().getCertificate(
					xcb.build(BCFactory.contentSigner(caKey)));
		} catch (GeneralSecurityException|OperatorCreationException e) {
			throw new CertException(e);
		} finally {
			tt.done();
		}
		
		return new X509ChainWithIssuer(x509cwi,x509);
	}

}
