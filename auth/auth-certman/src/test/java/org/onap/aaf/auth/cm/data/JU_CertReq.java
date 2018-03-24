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
 * *
 ******************************************************************************/
package org.onap.aaf.auth.cm.data;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.cm.ca.CA;
import org.onap.aaf.auth.cm.ca.X509andChain;
import org.onap.aaf.auth.cm.cert.BCFactory;
import org.onap.aaf.auth.cm.cert.CSRMeta;
import org.onap.aaf.auth.cm.data.CertReq;
import org.onap.aaf.cadi.cm.CertException;
import org.onap.aaf.misc.env.Trans;

@RunWith(MockitoJUnitRunner.class)
public class JU_CertReq {
	
	private static BCFactory bcFact;
	
	private static CSRMeta value;
	
	private static CertReq req;
	
	@BeforeClass
	public static void setUp() {
		bcFact = mock(BCFactory.class);
		value = mock(CSRMeta.class);
		req = mock(CertReq.class);
		
	}
	
//	@Test
//	public void getCSRMeta() throws CertException, IOException {
//		//req = new CertReq();
//		req.mechid = "1213";
//		List<String> fqdnsas = new ArrayList<String>();
//		fqdnsas.add("String1");
//		List<String> emails = new ArrayList<String>();
//		emails.add("pupleti@hotmail.com");
//		req.emails = emails;
//		req.fqdns = fqdnsas;
//		req.certAuthority = new CA(null, "testName", "ALL") {
//			//TODO: Gabe [JUnit] REREVIEW
//			@Override
//			public X509andChain sign(Trans trans, CSRMeta csrmeta) throws IOException, CertException {
//	
//				return null;
//			}
//		};
//		req.sponsor = "asa@df.co";
//		assertNull(req.getCSRMeta());
//	}
	
	@Test						//TODO: Temporary fix AAF-111
	public void netYetTested() {
		fail("Tests not yet implemented");
	}
}
