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
package com.att.authz.cm.data;

import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.att.authz.cm.ca.CA;
import com.att.authz.cm.cert.BCFactory;
import com.att.authz.cm.cert.CSRMeta;
import com.att.cadi.cm.CertException;

public class CertReq {
	// These cannot be null
	public CA certAuthority;
	public String mechid;
	public List<String> fqdns;
	// Notify
	public List<String> emails;
	
	
	// These may be null
	public String sponsor;
	public XMLGregorianCalendar start, end;
	
	public CSRMeta getCSRMeta() throws CertException {
		return BCFactory.createCSRMeta(certAuthority, mechid, sponsor,fqdns);
	}
}
