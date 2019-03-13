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

package org.onap.aaf.auth.dao.hl;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.dao.cached.CachedCredDAO;
import org.onap.aaf.auth.dao.cached.CachedNSDAO;
import org.onap.aaf.auth.dao.cached.CachedPermDAO;
import org.onap.aaf.auth.dao.cached.CachedRoleDAO;
import org.onap.aaf.auth.dao.cached.CachedUserRoleDAO;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.dao.hl.Question.Access;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.auth.org.Organization.Expiration;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.LogTarget;

import io.netty.util.internal.SystemPropertyUtil;

public class JU_Function {

	@Mock
    AuthzTrans trans;
	@Mock
	PropAccess access;
	
	@Mock
	Question ques;
	
	@Before
	public void setUp() throws APIException, IOException {
		initMocks(this);
	}

	@Test
	public void testCreateNs() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		namespace.owner = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");

		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		Result<Void> retVal = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, true);
		assertTrue(3 == result.status);
	}
	
	@Test
	public void testCreateNsReadSuccess() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		owner.add("test");
		namespace.owner = owner;
		List<String> admin = new ArrayList<String>();
		admin.add("test");
		namespace.admin= admin;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");

		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		Result<Void> retVal = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, true);
		assertTrue(3 == result.status);
	}
	
	@Test
	public void testCreateNsFromApprovaFalse() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		namespace.owner = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
		Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(data,1,"test",new String[0]);
		Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal1.value, Access.write);
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, false);
		assertTrue(1 == result.status);
		
		Mockito.doReturn(retVal2).when(ques).deriveNs(trans, "test");
		funcObj = new Function(trans, ques);
		result = funcObj.createNS(trans, namespace, false);
		assertTrue(1 == result.status);
	}
	
	@Test
	public void testCreateNsownerLoop() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		owner.add("test");
		namespace.owner = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		Mockito.doReturn(org).when(trans).org();
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == Status.ERR_Policy);
		assertTrue(result.details.contains("is not a valid user at"));
		
		Identity iden=Mockito.mock(Identity.class);
		try {
			Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
			Mockito.doReturn("test").when(iden).mayOwn();
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == Status.ERR_Policy);
		assertTrue(result.details.contains("is an invalid Identity"));

		Mockito.doReturn(true).when(iden).isFound();
		result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == Status.ERR_Policy);
		assertTrue(result.details.contains("cannot be the owner of the namespace "));
		
		Mockito.doReturn(true).when(org).isTestEnv();
		try {
			Mockito.doReturn("test").when(org).validate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
			result = funcObj.createNS(trans, namespace, true);
			assertTrue(result.status == Status.ERR_Policy);
			assertTrue(result.details.contains("cannot be the owner of the namespace "));
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateNsownerLoopException() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test";
		List<String> owner = new ArrayList<String>();
		owner.add("test");
		namespace.owner = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		Function funcObj = new Function(trans, ques);
		
		Identity iden=Mockito.mock(Identity.class);
		try {
			Mockito.doThrow(new OrganizationException()).when(org).getIdentity(trans, "test");
			Mockito.doReturn("test").when(iden).mayOwn();
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,1,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
		
		Result<Void> result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == Status.ERR_Security);
		assertTrue(result.details.contains("may not create Root Namespaces"));
		
		Mockito.doReturn(true).when(ques).isGranted(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, null);
		
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		Result<Void> retVal = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		
		result = funcObj.createNS(trans, namespace, true);
		assertTrue(24 == result.status);
		
	}
	
	public void setQuestion(Question ques, CachedNSDAO userRoleDaoObj) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("nsDAO");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(ques, userRoleDaoObj);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setQuestionCredDao(Question ques, CachedCredDAO credDaoObj) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("credDAO");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(ques, credDaoObj);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setQuestionUserRoleDao(Question ques, CachedUserRoleDAO credDaoObj) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("userRoleDAO");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(ques, credDaoObj);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setQuestionCachedRoleDao(Question ques, CachedRoleDAO credDaoObj) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("roleDAO");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(ques, credDaoObj);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setQuestionCachedPermDao(Question ques, CachedPermDAO credDaoObj) {
		Field nsDaoField;
		try {
			nsDaoField = Question.class.getDeclaredField("permDAO");
			
			nsDaoField.setAccessible(true);
	        // remove final modifier from field
	        Field modifiersField = Field.class.getDeclaredField("modifiers");
	        modifiersField.setAccessible(true);
	        modifiersField.setInt(nsDaoField, nsDaoField.getModifiers() & ~Modifier.FINAL);
	        
	        nsDaoField.set(ques, credDaoObj);
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Test
	public void testCreateNsAdminLoop() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		owner.add("test");
		namespace.owner = owner;
		namespace.admin = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
		
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		CachedCredDAO credDAO = Mockito.mock(CachedCredDAO.class);
		Result<Void> retVal = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(retVal).when(nsDaoObj).create(Mockito.any(), Mockito.any());
		List<CredDAO.Data> dataObj = new ArrayList<>();
		CredDAO.Data indData = new CredDAO.Data();
		indData.id = "test";
		indData.notes = "test";
		DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
		try {
			indData.expires = sdf.parse("2090/01/01");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dataObj.add(indData);
		Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(dataObj,0,"test",new String[0]);
		Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		setQuestionCredDao(ques, credDAO);
		
		Identity iden=Mockito.mock(Identity.class);
		try {
			Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
			Mockito.doReturn("test").when(iden).mayOwn();
			Mockito.doReturn(true).when(org).isTestEnv();
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == 1);
		
	}
	
	@Test
	public void testCreateNsAdminLoopCreateSucReadChildrenFailure() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		owner.add("test");
		namespace.owner = owner;
		namespace.admin = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
		
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		CachedCredDAO credDAO = Mockito.mock(CachedCredDAO.class);
		CachedUserRoleDAO userRoleDAO = Mockito.mock(CachedUserRoleDAO.class);
		CachedRoleDAO cachedRoleDAO = Mockito.mock(CachedRoleDAO.class);
		CachedPermDAO cachedPermDAO = Mockito.mock(CachedPermDAO.class);
		Result<Void> retVal = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(retVal).when(nsDaoObj).create(Mockito.any(), Mockito.any());
		List<CredDAO.Data> dataObj = new ArrayList<>();
		CredDAO.Data indData = new CredDAO.Data();
		indData.id = "test";
		indData.notes = "test";
		DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
		try {
			indData.expires = sdf.parse("2090/01/01");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dataObj.add(indData);
		List<RoleDAO.Data> dataObj1 = new ArrayList<>();
		RoleDAO.Data indData1 = new RoleDAO.Data();
		indData1.ns = "test";
		indData1.name = "test";
		Set<String> permsSet = new HashSet<>();
		permsSet.add("test|test");
		indData1.perms = permsSet;
		dataObj1.add(indData1);
		
		List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
		UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
		indData4.ns = "test";
		indData4.rname = "test";
		dataObj4.add(indData4);
		
		List<PermDAO.Data> dataObj5 = new ArrayList<>();
		PermDAO.Data indData5 = new PermDAO.Data();
		indData5.ns = "test";
		indData5.type = "test";
		dataObj5.add(indData5);
		
		Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(dataObj,0,"test",new String[0]);
		Result<List<CredDAO.Data>> retVal6 = new Result<List<CredDAO.Data>>(dataObj,1,"test",new String[0]);
		Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",new String[0]);
		Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",new String[0]);
		Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",new String[0]);
		Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());		
		Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());	
		Mockito.doReturn(retVal2).when(userRoleDAO).create(Mockito.any(), Mockito.any());	
		Mockito.doReturn(retVal6).when(cachedRoleDAO).create(Mockito.any(), Mockito.any());	
		Mockito.doReturn(retVal6).when(cachedRoleDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());	
		Mockito.doReturn(retVal2).when(cachedPermDAO).create(Mockito.any(), Mockito.any());	
		Mockito.doReturn(retVal5).when(cachedPermDAO).readChildren(trans, "test", "test");	
		Mockito.doReturn(retVal5).when(cachedPermDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());	
		Mockito.doReturn(retVal3).when(cachedRoleDAO).readChildren(trans, "test", "test");	
		setQuestion(ques, nsDaoObj);
		setQuestionCredDao(ques, credDAO);
		setQuestionUserRoleDao(ques, userRoleDAO);
		setQuestionCachedRoleDao(ques, cachedRoleDAO);
		setQuestionCachedPermDao(ques, cachedPermDAO);
		
		Identity iden=Mockito.mock(Identity.class);
		try {
			Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
			Mockito.doReturn("test").when(iden).mayOwn();
			Mockito.doReturn(true).when(org).isTestEnv();
			Mockito.doReturn(new GregorianCalendar(2010, 01, 01)).when(org).expiration(null, Expiration.UserInRole);
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == Status.ERR_ActionNotCompleted);
		
	}
	
	@Test
	public void testCreateNsAdminLoopCreateSuc() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Namespace namespace = Mockito.mock(Namespace.class);
		namespace.name = "test.test";
		List<String> owner = new ArrayList<String>();
		owner.add("test");
		namespace.owner = owner;
		namespace.admin = owner;
		
		Organization org = Mockito.mock(Organization.class);
		Mockito.doReturn(org).when(trans).org();
		
		NsDAO.Data data = new NsDAO.Data();
		data.name="test";
		Result<NsDAO.Data> retVal1 = new Result<NsDAO.Data>(data,0,"test",new String[0]);
		Mockito.doReturn(retVal1).when(ques).deriveNs(trans, "test");
		
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		CachedCredDAO credDAO = Mockito.mock(CachedCredDAO.class);
		CachedUserRoleDAO userRoleDAO = Mockito.mock(CachedUserRoleDAO.class);
		CachedRoleDAO cachedRoleDAO = Mockito.mock(CachedRoleDAO.class);
		CachedPermDAO cachedPermDAO = Mockito.mock(CachedPermDAO.class);
		Result<Void> retVal = new Result<Void>(null,0,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());
		Mockito.doReturn(retVal).when(nsDaoObj).create(Mockito.any(), Mockito.any());
		List<CredDAO.Data> dataObj = new ArrayList<>();
		CredDAO.Data indData = new CredDAO.Data();
		indData.id = "test";
		indData.notes = "test";
		DateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
		try {
			indData.expires = sdf.parse("2090/01/01");
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dataObj.add(indData);
		List<RoleDAO.Data> dataObj1 = new ArrayList<>();
		RoleDAO.Data indData1 = new RoleDAO.Data();
		indData1.ns = "test";
		indData1.name = "test";
		Set<String> permsSet = new HashSet<>();
		permsSet.add("test|test|test|test");
		indData1.perms = permsSet;
		dataObj1.add(indData1);
		
		List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
		UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
		indData4.ns = "test";
		indData4.rname = "test";
		dataObj4.add(indData4);
		
		List<PermDAO.Data> dataObj5 = new ArrayList<>();
		PermDAO.Data indData5 = new PermDAO.Data();
		indData5.ns = "test";
		indData5.type = "test";
		Set<String> rolesSet = new HashSet<>();
		rolesSet.add("test|test|test|test");
		indData5.roles = rolesSet;
		dataObj5.add(indData5);
		
		Result<List<CredDAO.Data>> retVal2 = new Result<List<CredDAO.Data>>(dataObj,0,"test",new String[0]);
		Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",new String[0]);
		Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",new String[0]);
		Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(dataObj5,0,"test",new String[0]);
		Mockito.doReturn(retVal2).when(credDAO).readID(Mockito.any(), Mockito.anyString());		
		Mockito.doReturn(retVal4).when(userRoleDAO).readByRole(Mockito.any(), Mockito.anyString());	
		Mockito.doReturn(retVal2).when(userRoleDAO).create(Mockito.any(), Mockito.any());	
		Mockito.doReturn(retVal2).when(cachedRoleDAO).create(Mockito.any(), Mockito.any());	
		Mockito.doReturn(retVal2).when(cachedRoleDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());	
		Mockito.doReturn(retVal2).when(cachedPermDAO).create(Mockito.any(), Mockito.any());	
		Mockito.doReturn(retVal5).when(cachedPermDAO).readChildren(trans, "test", "test");	
		Mockito.doReturn(retVal5).when(cachedPermDAO).delete(Mockito.any(), Mockito.any(), Mockito.anyBoolean());	
		Mockito.doReturn(retVal3).when(cachedRoleDAO).readChildren(trans, "test", "test");	
		setQuestion(ques, nsDaoObj);
		setQuestionCredDao(ques, credDAO);
		setQuestionUserRoleDao(ques, userRoleDAO);
		setQuestionCachedRoleDao(ques, cachedRoleDAO);
		setQuestionCachedPermDao(ques, cachedPermDAO);
		
		Identity iden=Mockito.mock(Identity.class);
		try {
			Mockito.doReturn(iden).when(org).getIdentity(trans, "test");
			Mockito.doReturn("test").when(iden).mayOwn();
			Mockito.doReturn(true).when(org).isTestEnv();
			Mockito.doReturn(new GregorianCalendar(2010, 01, 01)).when(org).expiration(null, Expiration.UserInRole);
		} catch (OrganizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Function funcObj = new Function(trans, ques);
		Result<Void>  result = funcObj.createNS(trans, namespace, true);
		assertTrue(result.status == 0);
		
	}
	
	@Test
	public void test4DeleteNs() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		Result<Void> retVal = new Result<Void>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		
		Function funcObj = new Function(trans, ques);
		Result<Void> result = funcObj.deleteNS(trans, "test");
		
		assertTrue(result.status == Status.ERR_NsNotFound);
	}
	@Test
	public void test4DeleteCanMOveFail() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(true).when(trans).requested(REQD_TYPE.move);
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
		NsDAO.Data dataObj = new NsDAO.Data();
		dataObj.type=1;
		dataAl.add(dataObj);
		Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		
		Mockito.doReturn(false).when(ques).canMove(Mockito.any());
		
		Function funcObj = new Function(trans, ques);
		Result<Void> result = funcObj.deleteNS(trans, "test");
		assertTrue(result.status == Status.ERR_Denied);
		
	}
	@Test
	public void test4DeleteNsReadSuc() {
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
		try {
			Define.set(access);
		} catch (CadiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
		List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
		NsDAO.Data dataObj = new NsDAO.Data();
		dataObj.type=1;
		dataAl.add(dataObj);
		Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",new String[0]);
		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
		setQuestion(ques, nsDaoObj);
		
		Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,1,"test",new String[0]);
		Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
		
		Function funcObj = new Function(trans, ques);
		Result<Void> result = funcObj.deleteNS(trans, "test");
		assertTrue(result.status == 1);
		
	}
//	@Test
//	public void test4DeleteNsMayUserSuc() {
//		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
//		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
//		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
//		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
//		try {
//			Define.set(access);
//		} catch (CadiException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
//		List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//		NsDAO.Data dataObj = new NsDAO.Data();
//		dataObj.type=1;
//		dataAl.add(dataObj);
//		Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",new String[0]);
//		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
//		setQuestion(ques, nsDaoObj);
//		
//		Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",new String[0]);
//		Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
//		
//		Function funcObj = new Function(trans, ques);
//		Result<Void> result = funcObj.deleteNS(trans, "test");
//		assertTrue(result.status == 1);
//
//		Mockito.doReturn(true).when(ques).isGranted(Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
//
//		CachedCredDAO credDAO = Mockito.mock(CachedCredDAO.class);
//		Mockito.doReturn(retVal2).when(credDAO).readNS(Mockito.any(), Mockito.anyString());		
//		setQuestionCredDao(ques, credDAO);
//
//		CachedPermDAO cachedPermDAO = Mockito.mock(CachedPermDAO.class);
//		Result<List<PermDAO.Data>> retVal5 = new Result<List<PermDAO.Data>>(null,0,"test",new String[0]);
//		Mockito.doReturn(retVal5).when(cachedPermDAO).readNS(trans, "test");
//		setQuestionCachedPermDao(ques, cachedPermDAO);
//		
//		CachedUserRoleDAO cachedUserRoleDAO = Mockito.mock(CachedUserRoleDAO.class);
//		List<UserRoleDAO.Data> dataObj4 = new ArrayList<>();
//		UserRoleDAO.Data indData4 = new UserRoleDAO.Data();
//		indData4.ns = "test";
//		indData4.rname = "test";
//		dataObj4.add(indData4);
//		Result<List<UserRoleDAO.Data>> retVal4 = new Result<List<UserRoleDAO.Data>>(dataObj4,0,"test",new String[0]);
//		Mockito.doReturn(retVal4).when(cachedUserRoleDAO).readByRole(trans, "test");
//		setQuestionUserRoleDao(ques, cachedUserRoleDAO);
//		
//		CachedRoleDAO cachedRoleDAO = Mockito.mock(CachedRoleDAO.class);
//		List<RoleDAO.Data> dataObj1 = new ArrayList<>();
//		RoleDAO.Data indData1 = new RoleDAO.Data();
//		indData1.ns = "test";
//		indData1.name = "test";
//		Set<String> permsSet = new HashSet<>();
//		permsSet.add("test|test");
//		indData1.perms = permsSet;
//		dataObj1.add(indData1);
//		Result<List<RoleDAO.Data>> retVal3 = new Result<List<RoleDAO.Data>>(dataObj1,0,"test",new String[0]);
//		Mockito.doReturn(retVal3).when(cachedRoleDAO).readNS(trans, "test");		
//		setQuestionCachedRoleDao(ques, cachedRoleDAO);
//		
//		funcObj = new Function(trans, ques);
//		result = funcObj.deleteNS(trans, "test");
//		assertTrue(result.status == Status.ERR_DependencyExists);
//
//		Mockito.doReturn(true).when(trans).requested(REQD_TYPE.force);
//		funcObj = new Function(trans, ques);
//		result = funcObj.deleteNS(trans, "test");
//		assertTrue(result.status == 2);
//	}
//	@Test
//	public void test4DeleteNsMayUserSuc() {
//		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
//		Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).debug();
//		Mockito.doReturn(Mockito.mock(Properties.class)).when(access).getProperties();
//		Mockito.doReturn("test.test").when(access).getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
//		try {
//			Define.set(access);
//		} catch (CadiException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		CachedNSDAO nsDaoObj = Mockito.mock(CachedNSDAO.class);
//		List<NsDAO.Data> dataAl = new ArrayList<NsDAO.Data>();
//		NsDAO.Data dataObj = new NsDAO.Data();
//		dataObj.type=1;
//		dataAl.add(dataObj);
//		Result<List<NsDAO.Data>> retVal = new Result<List<NsDAO.Data>>(dataAl,0,"test",new String[0]);
//		Mockito.doReturn(retVal).when(nsDaoObj).read(Mockito.any(), Mockito.anyString());		
//		setQuestion(ques, nsDaoObj);
//		
//		Result<NsDAO.Data> retVal2 = new Result<NsDAO.Data>(null,0,"test",new String[0]);
//		Mockito.doReturn(retVal2).when(ques).mayUser(trans, null,retVal.value.get(0), Access.write);
//		
//		Function funcObj = new Function(trans, ques);
//		Result<Void> result = funcObj.deleteNS(trans, "test");
//		assertTrue(result.status == 1);
//		
//	}
}
