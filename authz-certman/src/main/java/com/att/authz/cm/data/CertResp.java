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
package com.att.authz.cm.data;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import com.att.authz.cm.cert.CSRMeta;
import com.att.cadi.cm.CertException;
import com.att.cadi.cm.Factory;
import com.att.inno.env.Trans;

public class CertResp {
	public CertResp(Trans trans, X509Certificate x509, CSRMeta csrMeta, String[] notes) throws IOException, GeneralSecurityException, CertException {
		keyPair = csrMeta.keypair(trans);
		privateKey = Factory.toString(trans, keyPair.getPrivate());
		certString = Factory.toString(trans,x509);
		challenge=csrMeta.challenge();
		this.notes = notes;
	}
	private KeyPair keyPair;
	private String challenge;
	
	private String privateKey, certString;
	private String[] notes;
	
	
	public String asCertString() {
		return certString;
	}
	
	public String privateString() throws IOException {
		return privateKey;
	}
	
	public String challenge() {
		return challenge==null?"":challenge;
	}
	
	public String[] notes() {
		return notes;
	}
}
