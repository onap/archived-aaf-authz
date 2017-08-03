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
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.Client;
import org.jscep.client.ClientException;
import org.jscep.client.EnrollmentResponse;
import org.jscep.client.verification.CertificateVerifier;
import org.jscep.transaction.TransactionException;

import com.att.authz.cm.cert.BCFactory;
import com.att.authz.cm.cert.CSRMeta;
import com.att.authz.cm.cert.StandardFields;
import com.att.authz.common.Define;
import com.att.cadi.cm.CertException;
import com.att.cadi.cm.Factory;
import com.att.cadi.config.Config;
import com.att.cadi.routing.GreatCircle;
import com.att.inno.env.Env;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;
import com.att.inno.env.util.Split;

public class AppCA extends CA {
	public static final String CA_PERM_TYPE = Define.ROOT_NS+".ca"; // Permission Type for validation
	private static final String AAF_DATA_DIR = "aaf_data_dir";
	private static final String CA_PREFIX = "http://";
	private static final String CA_POSTFIX="/certsrv/mscep_admin/mscep.dll";

	private final static String MS_PROFILE="1";
	private static final String CM_TRUST_CAS = "cm_trust_cas";
	private Clients clients;

	private static class AAFStdFields implements StandardFields {
		private final String env;
		public AAFStdFields(Trans trans) throws CertException {
	 		env = trans.getProperty(Config.AAF_ENV);
			if(env==null) {
				throw new CertException(Config.AAF_ENV + " must be set to create Certificates");
			}
		}
		@Override
		public void set(CSRMeta csr) {
			// Environment
			csr.environment(env);
			// Standard Fields
			csr.o("ATT Services,Inc.");
			csr.l("St Louis");
			csr.st("Missouri");
			csr.c("US");
		}
	}

	public AppCA(final Trans trans, final String name, final String urlstr, final String id, final String pw) throws IOException, CertificateException, CertException {
 		super(name,new AAFStdFields(trans), CA_PERM_TYPE);
		
		clients = new Clients(trans,urlstr);
		
 		
		// Set this for NTLM password Microsoft
		Authenticator.setDefault(new Authenticator() {
			  public PasswordAuthentication getPasswordAuthentication () {
		            return new PasswordAuthentication (
		            		id,
		             		trans.decryptor().decrypt(pw).toCharArray());
		        }
		});



		try {
			StringBuilder sb = new StringBuilder("CA Reported Trusted Certificates");
			List<X509Certificate> trustCerts = new ArrayList<X509Certificate>();
			for(Client client : clients) {
				CertStore cs = client.getCaCertificate(MS_PROFILE);
				
				Collection<? extends Certificate> cc = cs.getCertificates(null);
				for(Certificate c : cc) {
					X509Certificate xc = (X509Certificate)c;
					// Avoid duplicate Certificates from multiple servers
					X509Certificate match = null;
					for(X509Certificate t : trustCerts) {
						if(t.getSerialNumber().equals(xc.getSerialNumber())) {
							match = xc;
							break;
						}
					}
					if(match==null && xc.getSubjectDN().getName().startsWith("CN=ATT ")) {
						sb.append("\n\t");
						sb.append(xc.getSubjectDN());
						sb.append("\n\t\tSerial Number: ");
						String bi = xc.getSerialNumber().toString(16);
						for(int i=0;i<bi.length();++i) {
							if(i>1 && i%2==0) {
								sb.append(':');
							}
							sb.append(bi.charAt(i));
						}
						sb.append("\n\t\tIssuer:        ");
						sb.append(xc.getIssuerDN());
						sb.append("\n\t\tNot Before:    ");
						sb.append(xc.getNotBefore());
						sb.append("\n\t\tNot After:     ");
						sb.append(xc.getNotAfter());
						sb.append("\n\t\tSigAlgorithm:  ");
						sb.append(xc.getSigAlgName());
						sb.append("\n\t\tType:          ");
						sb.append(xc.getType());
						sb.append("\n\t\tVersion:       ");
						sb.append(xc.getVersion());

						trustCerts.add(xc);
					}
				}
			}
			trans.init().log(sb);
			// Add Additional ones from Property
			String data_dir = trans.getProperty(AAF_DATA_DIR);
			if(data_dir!=null) {
				File data = new File(data_dir);
				if(data.exists()) {
					String trust_cas = trans.getProperty(CM_TRUST_CAS);
					byte[] bytes;
					if(trust_cas!=null) {
						for(String fname : Split.split(';', trust_cas)) {
							File crt = new File(data,fname);
							if(crt.exists()) {
								bytes = Factory.decode(crt);
								try {
									Collection<? extends Certificate> cc = Factory.toX509Certificate(bytes);
									for(Certificate c : cc) {
										trustCerts.add((X509Certificate)c);
									}
								} catch (CertificateException e) {
									throw new CertException(e);
								}
							}
						}
					}
				}
			}
			
			String[] trustChain = new String[trustCerts.size()];
			int i=-1;
			for( Certificate cert : trustCerts) {
				trustChain[++i]=BCFactory.toString(trans,cert);
			}
			
			setTrustChain(trustChain);
		} catch (ClientException | CertStoreException e) {
			// Note:  Cannot validly start without all Clients, because we need to read all Issuing Certificates
			// This is acceptable risk for most things, as we're not real time in general
			throw new CertException(e);
		}
	}


	@Override
	public X509Certificate sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException {
		TimeTaken tt = trans.start("Generating CSR and Keys for New Certificate", Env.SUB);
		PKCS10CertificationRequest csr;
		try {
			csr = csrmeta.generateCSR(trans);
			if(trans.info().isLoggable()) {
				trans.info().log(BCFactory.toString(trans, csr));
			} 
			if(trans.info().isLoggable()) {
				trans.info().log(csr);
			}
		} finally {
			tt.done();
		}
		
		tt = trans.start("Enroll CSR", Env.SUB);
		Client client = null;
		try {
			client = clients.best();
			EnrollmentResponse er = client.enrol(
					csrmeta.initialConversationCert(trans),
					csrmeta.keypair(trans).getPrivate(),
					csr,
					MS_PROFILE /* profile... MS can't deal with blanks*/);
			while(true) {
				if(er.isSuccess()) {
					for( Certificate cert : er.getCertStore().getCertificates(null)) {
						return (X509Certificate)cert;
					}
					break;
				} else if (er.isPending()) {
					trans.checkpoint("Polling, waiting on CA to complete");
					Thread.sleep(3000);
				} else if (er.isFailure()) {
					throw new CertException(er.getFailInfo().toString());
				}
			}
		} catch (ClientException e) {
			trans.error().log(e,"SCEP Client Error, Temporarily Invalidating Client");
			if(client!=null) {
				clients.invalidate(client);
			}
		} catch (InterruptedException|TransactionException|CertificateException|OperatorCreationException | CertStoreException e) {
			trans.error().log(e);
		} finally {
			tt.done();
		}
		
		return null;
	}


	private class Clients implements Iterable<Client>{
		/**
		 * CSO Servers are in Dallas and St Louis
		 * GEO_LOCATION   LATITUDE    LONGITUDE    ZIPCODE   TIMEZONE
		 * ------------   --------    ---------    -------   --------
		 * DLLSTXCF       32.779295   -96.800014   75202     America/Chicago
		 * STLSMORC       38.627345   -90.193774   63101     America/Chicago
		 * 
		 * The online production issuing CA servers are:
		 * 	AAF - CADI Issuing CA 01	135.41.45.152	MOSTLS1AAFXXA02
		 * 	AAF - CADI Issuing CA 02	135.31.72.154	TXDLLS2AAFXXA02
		 */
		
		private final Client[] client;
		private final Date[] failure;
		private int preferred;

		public Clients(Trans trans, String urlstr) throws MalformedURLException { 
	 		String[] urlstrs = Split.split(',', urlstr);
	 		client = new Client[urlstrs.length];
	 		failure = new Date[urlstrs.length];
	 		double distance = Double.MAX_VALUE;
	 		String localLat = trans.getProperty("AFT_LATITUDE","39.833333"); //Note: Defaulting to GEO center of US
	 		String localLong = trans.getProperty("AFT_LONGITUDE","-98.583333");
	 		for(int i=0;i<urlstrs.length;++i) {
	 			String[] info = Split.split('/', urlstrs[i]);
	 			if(info.length<3) {
	 				throw new MalformedURLException("Configuration needs LAT and LONG, i.e. ip:port/lat/long");
	 			}
	 			client[i] = new Client(new URL(CA_PREFIX + info[0] + CA_POSTFIX), 
		 			new CertificateVerifier() {
		 				@Override
		 				public boolean verify(X509Certificate cert) {
		 					return true;
		 				}
		 			}
	 			);
	 			double d = GreatCircle.calc(info[1],info[2],localLat,localLong);
	 			if(d<distance) {
	 				preferred = i;
	 				distance=d;
	 			}
	 		}
	 		trans.init().printf("Preferred Certificate Authority is %s",urlstrs[preferred]);
	 		for(int i=0;i<urlstrs.length;++i) {
	 			if(i!=preferred) {
	 				trans.init().printf("Alternate Certificate Authority is %s",urlstrs[i]);
	 			}
	 		}
		}
		private Client best() throws ClientException {
			if(failure[preferred]==null) {
				return client[preferred];
			} else {
				Client c=null;
				// See if Alternate available
				for(int i=0;i<failure.length;++i) {
					if(failure[i]==null) {
						c=client[i];
					}
				}
				
				// If not, see if any expirations can be cleared
				Date now = new Date();
				for(int i=0;i<failure.length;++i) {
					if(now.after(failure[i])) {
						failure[i]=null;
						if(c==null) {
							c=client[i];
						}
					}
				}
				
				// if still nothing found, then throw.
				if(c==null) {
					throw new ClientException("No available machines to call");
				} 
				return c;
			}
		}
		
		public void invalidate(Client clt) {
		   for(int i=0;i<client.length;++i) {
			   if(client[i].equals(clt)) {
				   failure[i]=new Date(System.currentTimeMillis()+180000 /* 3 mins */);
			   }
		   }
		}
		
		@Override
		public Iterator<Client> iterator() {
			return new Iterator<Client>() {
				private int iter = 0;
				@Override
				public boolean hasNext() {
					return iter < Clients.this.client.length;
				}

				@Override
				public Client next() {
					return Clients.this.client[iter++];
				}
				
			};
		}
	}
}
