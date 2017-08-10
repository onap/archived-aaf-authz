/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
package com.att.authz.cm.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import com.att.cadi.cm.CertException;
import com.att.cadi.cm.Factory;
import com.att.inno.env.Trans;

public class CSRMeta {
	private String environment;
	private String cn;
	private String mechID;
	private String email;
	private String o;
	private String l;
	private String st;
	private String c;
	private String challenge;
	
	private ArrayList<String> sanList = new ArrayList<String>();

	private KeyPair keyPair;
	private X500Name name = null;
	private SecureRandom random = new SecureRandom();

	public X500Name x500Name() throws IOException {
		if(name==null) {
			X500NameBuilder xnb = new X500NameBuilder();
			xnb.addRDN(BCStyle.CN,cn);
			xnb.addRDN(BCStyle.E,email);
			if(environment==null) {
				xnb.addRDN(BCStyle.OU,mechID);
			} else {
				xnb.addRDN(BCStyle.OU,mechID+':'+environment);
			}
			xnb.addRDN(BCStyle.O,o);
			xnb.addRDN(BCStyle.L,l);
			xnb.addRDN(BCStyle.ST,st);
			xnb.addRDN(BCStyle.C,c);
			name = xnb.build();
		}
		return name;
	}
	
	
	public PKCS10CertificationRequest  generateCSR(Trans trans) throws IOException, CertException {
		PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(x500Name(),keypair(trans).getPublic());
		if(challenge!=null) {
			DERPrintableString password = new DERPrintableString(challenge);
			builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_challengePassword, password);
		}
		
		if(sanList.size()>0) {
			GeneralName[] gna = new GeneralName[sanList.size()];
			int i=-1;
			for(String s : sanList) {
				gna[++i]=new GeneralName(GeneralName.dNSName,s);
			}
			
			builder.addAttribute(
					PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
					new Extensions(new Extension[] {
							new Extension(Extension.subjectAlternativeName,false,new GeneralNames(gna).getEncoded())
					})
			);
		}
//		builder.addAttribute(Extension.basicConstraints,new BasicConstraints(false))
//      .addAttribute(Extension.keyUsage, new KeyUsage(KeyUsage.digitalSignature
//                           | KeyUsage.keyEncipherment));
		try {
			return builder.build(BCFactory.contentSigner(keypair(trans).getPrivate()));
		} catch (OperatorCreationException e) {
			throw new CertException(e);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void dump(PKCS10CertificationRequest csr) {
		 Attribute[] certAttributes = csr.getAttributes();
		 for (Attribute attribute : certAttributes) {
		     if (attribute.getAttrType().equals(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest)) {
		         Extensions extensions = Extensions.getInstance(attribute.getAttrValues().getObjectAt(0));
//		         Extension ext = extensions.getExtension(Extension.subjectAlternativeName);
		         GeneralNames gns = GeneralNames.fromExtensions(extensions,Extension.subjectAlternativeName);
		         GeneralName[] names = gns.getNames();
		         for(int k=0; k < names.length; k++) {
		             String title = "";
		             if(names[k].getTagNo() == GeneralName.dNSName) {
		                 title = "dNSName";
		             }
		             else if(names[k].getTagNo() == GeneralName.iPAddress) {
		                 title = "iPAddress";
		                 // Deprecated, but I don't see anything better to use.
		                 names[k].toASN1Object();
		             }
		             else if(names[k].getTagNo() == GeneralName.otherName) {
		                 title = "otherName";
		             }
		             System.out.println(title + ": "+ names[k].getName());
		         } 
		     }
		 }
	}
	
	public X509Certificate initialConversationCert(Trans trans) throws IOException, CertificateException, OperatorCreationException {
		GregorianCalendar gc = new GregorianCalendar();
		Date start = gc.getTime();
		gc.add(GregorianCalendar.DAY_OF_MONTH,2);
		Date end = gc.getTime();
		X509v3CertificateBuilder xcb = new X509v3CertificateBuilder(
				x500Name(),
				new BigInteger(12,random), // replace with Serialnumber scheme
				start,
				end,
				x500Name(),
//				SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(caCert.getPublicKey().getEn)
				new SubjectPublicKeyInfo(ASN1Sequence.getInstance(keypair(trans).getPublic().getEncoded()))
				);
		return new JcaX509CertificateConverter().getCertificate(
				xcb.build(BCFactory.contentSigner(keypair(trans).getPrivate())));
	}

	public CSRMeta san(String v) {
		sanList.add(v);
		return this;
	}

	public List<String> sans() {
		return sanList;
	}


	public KeyPair keypair(Trans trans) {
		if(keyPair == null) {
			keyPair = Factory.generateKeyPair(trans);
		}
		return keyPair;
	}

	/**
	 * @return the cn
	 */
	public String cn() {
		return cn;
	}


	/**
	 * @param cn the cn to set
	 */
	public void cn(String cn) {
		this.cn = cn;
	}

	/**
	 * Environment of Service MechID is good for
	 */
	public void environment(String env) {
		environment = env;
	}
	
	/**
	 * 
	 * @return
	 */
	public String environment() {
		return environment;
	}
	
	/**
	 * @return the mechID
	 */
	public String mechID() {
		return mechID;
	}


	/**
	 * @param mechID the mechID to set
	 */
	public void mechID(String mechID) {
		this.mechID = mechID;
	}


	/**
	 * @return the email
	 */
	public String email() {
		return email;
	}


	/**
	 * @param email the email to set
	 */
	public void email(String email) {
		this.email = email;
	}


	/**
	 * @return the o
	 */
	public String o() {
		return o;
	}


	/**
	 * @param o the o to set
	 */
	public void o(String o) {
		this.o = o;
	}

	/**
	 * 
	 * @return the l
	 */
	public String l() {
		return l;
	}
	
	/**
	 * @param l the l to set
	 */
	public void l(String l) {
		this.l=l;
	}

	/**
	 * @return the st
	 */
	public String st() {
		return st;
	}


	/**
	 * @param st the st to set
	 */
	public void st(String st) {
		this.st = st;
	}


	/**
	 * @return the c
	 */
	public String c() {
		return c;
	}


	/**
	 * @param c the c to set
	 */
	public void c(String c) {
		this.c = c;
	}


	/**
	 * @return the challenge
	 */
	public String challenge() {
		return challenge;
	}


	/**
	 * @param challenge the challenge to set
	 */
	public void challenge(String challenge) {
		this.challenge = challenge;
	}
	
}
