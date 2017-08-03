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
package com.att.authz.cm.facade;

import static org.junit.Assert.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.att.authz.cm.mapper.Mapper;
import com.att.authz.cm.service.CMService;
import com.att.authz.cm.service.CertManAPI;
import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.cadi.aaf.AAFPermission;
import com.att.authz.layer.Result;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;
import com.att.inno.env.LogTarget;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaData;


@RunWith(MockitoJUnitRunner.class)
public class JU_FacadeImpl<REQ,CERT,ARTIFACTS,ERROR> {
	
	private static AuthzTrans trans;
	private static HttpServletResponse resp;
	private static CertManAPI certman;
	private static FacadeImpl hImpl;
	private static CMService service;
	private Mapper<REQ,CERT,ARTIFACTS,ERROR> mapper;
	private Data.TYPE dataType;
	private static AuthzEnv env;
	
	private static FacadeImpl fImpl;
	private static HttpServletRequest req;
	
	@Before
	public void setUp() throws APIException, IOException {
		fImpl = mock(FacadeImpl.class);
		env = mock(AuthzEnv.class);
		resp = mock(HttpServletResponse.class);
		req = mock(HttpServletRequest.class);
		hImpl = mock(FacadeImpl.class, CALLS_REAL_METHODS);
		Result<Void> rvd = (Result) mock(Result.class);
		trans = mock(AuthzTrans.class);
		when(trans.error()).thenReturn(new LogTarget() {
			
			@Override
			public void printf(String fmt, Object... vars) {}
			
			@Override
			public void log(Throwable e, Object... msgs) {
				e.getMessage();
				e.printStackTrace();
				msgs.toString();
				
			}
			
			@Override
			public void log(Object... msgs) {
			}
			
			@Override
			public boolean isLoggable() {
				
				return false;
			}
		});
		when(trans.start(Mockito.anyString(), Mockito.anyInt())).thenReturn(new TimeTaken("Now", 1) {
			
			@Override
			public void output(StringBuilder sb) {
				
			}
		});
		when(fImpl.check(Mockito.any(AuthzTrans.class), Mockito.any(HttpServletResponse.class), Mockito.anyString())).thenReturn(rvd);
		when(resp.getOutputStream()).thenReturn(new ServletOutputStream() {
			
			@Override
			public void write(int b) throws IOException {
				
				
			}
		});
		
	}
	
	@Test
	public void check() throws IOException {
		AAFPermission ap = new AAFPermission("str1","str3","str2");
		String perms = ap.getInstance();
		assertNotNull(hImpl.check(trans, resp, perms));
	}
	
	@Test
	public void checkNull() throws IOException {
		AAFPermission ap = new AAFPermission(null,"Str3","str2");
		String perms = ap.getInstance();
		assertNotNull(hImpl.check(trans, resp, perms));
	}
	
	@Test
	public void checkTwoNull() throws IOException {
		AAFPermission ap = new AAFPermission(null,null,"str2");
		String perms = ap.getInstance();
		assertNotNull(fImpl.check(trans, resp, perms));
	}
	
	@Test
	public void checkAllNull() throws IOException {
		AAFPermission ap = new AAFPermission(null,null,null);
		String perms = ap.getInstance();
		assertNotNull(fImpl.check(trans, resp, perms));
	}
	
	@Test
	public void checkTrans_null() throws IOException {
		AAFPermission ap = new AAFPermission("str1","str3","str2");
		String perms = ap.getInstance();
		assertNotNull(hImpl.check(null, resp, perms));
	}
	
	@Test
	public void checkRespNull() throws IOException {
		AAFPermission ap = new AAFPermission("str1","str3","str2");
		String perms = ap.getInstance();
		assertNotNull(hImpl.check(trans, null, perms));
	}
	
	@Test
	public void requestCert() {		
		assertNotNull(hImpl.requestCert(trans, req, resp, true));
	}
	
	@Test
	public void renewCert() {		
		assertNotNull(hImpl.renewCert(trans, req, resp, true));
	}
	
	@Test
	public void dropCert() {		
		assertNotNull(hImpl.renewCert(trans, req, resp, true));
	}
	
	@Test
	public void createArtifacts() {		
		assertNotNull(hImpl.createArtifacts(trans, req, resp));
	}
	
	@Test
	public void readArtifacts() {		
		assertNotNull(hImpl.readArtifacts(trans, req, resp));
	}
}
