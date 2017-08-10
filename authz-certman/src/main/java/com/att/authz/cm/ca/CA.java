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
package com.att.authz.cm.ca;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.cert.X509Certificate;

import com.att.authz.cm.cert.CSRMeta;
import com.att.authz.cm.cert.StandardFields;
import com.att.cadi.cm.CertException;
import com.att.inno.env.Trans;

public abstract class CA {
	private final String name;
	private String[] trustChain;
	private final StandardFields stdFields;
	private MessageDigest messageDigest;
	private final String permType;
	
	protected CA(String name, StandardFields sf, String permType) {
		this.name = name;
		stdFields = sf;
		this.permType = permType;
	}

	/* 
	 * NOTE: These two functions must be called in Protected Constructors during their Construction.
	 */
	protected void setTrustChain(String[] trustChain) {
		this.trustChain = trustChain;
	}

	protected void setMessageDigest(MessageDigest md) {
		messageDigest = md;
	}

	/*
	 * End Required Constructor calls
	 */

	public String getName() {
		return name;
	}

	public String[] getTrustChain() {
		return trustChain;
	}
	
	public String getPermType() {
		return permType;
	}
	
	public StandardFields stdFields() {
		return stdFields;
	}
	
	public abstract X509Certificate sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException;

	public MessageDigest messageDigest() {
		return messageDigest;
	}
}
