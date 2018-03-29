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
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.Provider;
import java.security.Security;
import java.security.PrivateKey;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import sun.security.pkcs11.SunPKCS11;

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
			throw new IOException("LocalCA expects cm_ca.<ca name>=org.onap.aaf.auth.cm.ca.LocalCA,<full path to pkcs11 config file>[;<Full Path to Trust Chain, ending with actual CA>]+");
		}

		// add pkcs11 provider
		Provider p = new SunPKCS11(params[0][0]);
		if (p==null) {
			throw new RuntimeException("could not get security provider");
		}
		Security.addProvider(p);
		// Load the key store
		char[] pin = "123456789".toCharArray();
		KeyStore keyStore = KeyStore.getInstance("PKCS11", p);
		keyStore.load(null, pin);
		// get the Private Key
		PrivateKeyEntry privateKeyEntry = (PrivateKeyEntry) keyStore.getEntry("0x2222", null);
		caKey = privateKeyEntry.getPrivateKey();

		String dir = access.getProperty(CM_PUBLIC_DIR, "");
		if(!"".equals(dir) && !dir.endsWith("/")) {
			dir = dir + '/';
		}
		List<FileReader> frs = new ArrayList<FileReader>(params.length-1);
		try {
			String path;
			for(int i=1; i<params[0].length; ++i) { // first param is Private Key, remainder are TrustChain
				path = !params[0][i].contains("/")?dir+params[0][i]:params[0][i];
				access.printf(Level.INIT, "Loading a TrustChain Member for %s from %s\n",name, path);
				frs.add(new FileReader(path));
			}
			x509cwi = new X509ChainWithIssuer(frs);
			X500NameBuilder xnb = new X500NameBuilder();
			for(RDN rnd : RDN.parse(',', x509cwi.getIssuerDN())) {
				xnb.addRDN(rnd.aoi,rnd.value);
			}
			issuer = xnb.build();
		} finally {
			for(FileReader fr : frs) {
				if(fr!=null) {
					fr.close();
				}
			}
		}
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
