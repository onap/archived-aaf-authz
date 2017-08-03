/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aai
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * Copyright © 2017 Amdocs
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.authz.cm.ca;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.security.SecureRandom;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.operator.OperatorCreationException;

import com.att.authz.cm.cert.BCFactory;
import com.att.authz.cm.cert.CSRMeta;
import com.att.authz.cm.cert.StandardFields;
import com.att.authz.common.Define;
import com.att.cadi.cm.CertException;
import com.att.cadi.cm.Factory;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;

public class DevlCA extends CA {
	
	// Extensions
	private static final KeyPurposeId[] ASN_WebUsage = new KeyPurposeId[] {
				KeyPurposeId.id_kp_serverAuth, // WebServer
				KeyPurposeId.id_kp_clientAuth};// WebClient
				
	private X509Certificate caCert;
	private final RSAPrivateKey caKey;
	private final X500Name issuer;
	private final SecureRandom random = new SecureRandom();
	private byte[] serialish = new byte[24];

	public DevlCA(Trans trans, String name, String dirString) throws IOException, CertException {
		super(name, new StandardFields() {
			@Override
			public void set(CSRMeta csr) {
				// Standard Fields
				csr.o("ATT Services, Inc.");
				csr.l("St Louis");
				csr.st("Missouri");
				csr.c("US");
			}
		}, Define.ROOT_NS+".ca" // Permission Type for validation
		);
		File dir = new File(dirString);
		if(!dir.exists()) {
			throw new CertException(dirString + " does not exist");
		}
		
		File ca = new File(dir,"ca.crt");
		if(ca.exists()) {
			byte[] bytes = Factory.decode(ca);
			Collection<? extends Certificate> certs;
			try {
				certs = Factory.toX509Certificate(bytes);
			} catch (CertificateException e) {
				throw new CertException(e);
			}
			List<String> lTrust = new ArrayList<String>();
			caCert=null;
			for(Certificate c : certs) {
				if(caCert==null) {
					caCert = (X509Certificate)c;
				} else {
					lTrust.add(Factory.toString(trans,c));
				}
				break;
			}
		}
		
		this.setTrustChain(new String[]{Factory.toString(trans,caCert)});
				
			/*
			 * Private key needs to be converted to "DER" format, with no password.  
			 * 	Use chmod 400 on key
			 * 
			 *  openssl pkcs8 -topk8 -outform DER -nocrypt -in ca.key -out ca.der
			 *
			 */
			ca = new File(dir,"ca.der");
			if(ca.exists()) {
				byte[] bytes = Factory.binary(ca);
				
//					EncryptedPrivateKeyInfo ekey=new EncryptedPrivateKeyInfo(bytes);
//				    Cipher cip=Cipher.getInstance(ekey.getAlgName());
//				    PBEKeySpec pspec=new PBEKeySpec("password".toCharArray());
//				    SecretKeyFactory skfac=SecretKeyFactory.getInstance(ekey.getAlgName());
//				    Key pbeKey=skfac.generateSecret(pspec);
//				    AlgorithmParameters algParams=ekey.getAlgParameters();
//				    cip.init(Cipher.DECRYPT_MODE,pbeKey,algParams);
					
				KeyFactory keyFactory;
				try {
					keyFactory = KeyFactory.getInstance("RSA");
					PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(bytes);
						
		            caKey = (RSAPrivateKey) keyFactory.generatePrivate(privSpec);
				} catch (GeneralSecurityException e) {
					throw new CertException(e);
				}
				
				X500NameBuilder xnb = new X500NameBuilder();
				xnb.addRDN(BCStyle.C,"US");
				xnb.addRDN(BCStyle.ST,"Missouri");
				xnb.addRDN(BCStyle.L,"Arnold");
				xnb.addRDN(BCStyle.O,"ATT Services, Inc.");
				xnb.addRDN(BCStyle.OU,"AAF");
				xnb.addRDN(BCStyle.CN,"aaf.att.com");
				xnb.addRDN(BCStyle.EmailAddress,"DL-aaf-support@att.com");
				issuer = xnb.build();
		} else {
			throw new CertException(ca.getPath() + " does not exist");
		}
	}

	/* (non-Javadoc)
	 * @see com.att.authz.cm.service.CA#sign(org.bouncycastle.pkcs.PKCS10CertificationRequest)
	 */
	@Override
	public X509Certificate sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException {
		GregorianCalendar gc = new GregorianCalendar();
		Date start = gc.getTime();
		gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
		Date end = gc.getTime();
		X509Certificate x509;
		TimeTaken tt = trans.start("Create/Sign Cert",Env.SUB);
		try {
			BigInteger bi;
			synchronized(serialish) {
				random.nextBytes(serialish);
				bi = new BigInteger(serialish);
			}
				
			X509v3CertificateBuilder xcb = new X509v3CertificateBuilder(
					issuer,
					bi, // replace with Serialnumber scheme
					start,
					end,
					csrmeta.x500Name(),
//					SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(caCert.getPublicKey().getEn)
					new SubjectPublicKeyInfo(ASN1Sequence.getInstance(caCert.getPublicKey().getEncoded()))
					);
			List<GeneralName> lsan = new ArrayList<GeneralName>();
			for(String s : csrmeta.sans()) {
				lsan.add(new GeneralName(GeneralName.dNSName,s));
			}
			GeneralName[] sans = new GeneralName[lsan.size()];
			lsan.toArray(sans);

		    JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
		    xcb		.addExtension(Extension.basicConstraints,
                    	false, new BasicConstraints(false))
		            .addExtension(Extension.keyUsage,
		                true, new KeyUsage(KeyUsage.digitalSignature
		                                 | KeyUsage.keyEncipherment))
		            .addExtension(Extension.extendedKeyUsage,
		                          true, new ExtendedKeyUsage(ASN_WebUsage))

                    .addExtension(Extension.authorityKeyIdentifier,
		                          false, extUtils.createAuthorityKeyIdentifier(caCert))
		            .addExtension(Extension.subjectKeyIdentifier,
		                          false, extUtils.createSubjectKeyIdentifier(caCert.getPublicKey()))
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
		return x509;
	}

}
