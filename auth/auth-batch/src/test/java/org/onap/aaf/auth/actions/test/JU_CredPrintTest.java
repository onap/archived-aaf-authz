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

package org.onap.aaf.auth.actions.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.auth.actions.CredPrint;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.CredDAO.Data;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.LogTarget;

public class JU_CredPrintTest {

	@Mock
	private AuthzTrans trans;
	private Data cred;
	@Mock
	LogTarget target;

	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(trans.info()).thenReturn(target);
		cred = new CredDAO.Data();
		cred.type = CredDAO.BASIC_AUTH;
	}

	@Test
	public void testCred() {
		CredPrint print = new CredPrint("text");

		Result<Void> result = print.exec(trans, cred, "text");

		assertEquals(result.status, result.ok().status);
		assertEquals(CredPrint.type(CredDAO.BASIC_AUTH), "OLD");
		assertEquals(CredPrint.type(CredDAO.BASIC_AUTH_SHA256), "U/P");
		assertEquals(CredPrint.type(CredDAO.CERT_SHA256_RSA), "Cert");
		assertEquals(CredPrint.type(0), "Unknown");
	}

}
