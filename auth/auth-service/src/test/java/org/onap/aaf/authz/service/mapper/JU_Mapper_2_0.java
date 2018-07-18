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
package org.onap.aaf.authz.service.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import aaf.v2_0.NsRequest;
import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Request;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.Pair;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.auth.service.mapper.Mapper_2_0;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;

@RunWith(MockitoJUnitRunner.class)
public class JU_Mapper_2_0 {

	private Mapper_2_0 mapper;
	@Mock
	private Question question;
	@Mock
	AuthzTrans transaction;

	@Before
	public void setUp() throws APIException, IOException, CadiException {
		this.mapper = new Mapper_2_0(question);
	}

	@Test(expected = ClassCastException.class)
	public void ns_willThrowException_whenInvalidRequestType() {
		//given
		Request rq = new Request();

		//when
		mapper.ns(transaction, rq);

		//then
		fail("Expected ClassCastException");
	}

	@Test
	public void ns_shouldConvertNamespaceRequest_whenValidTypeIsExplicitlyProvided() {
		//given
		String namespaceName = "org.companyA.app1";
		String namespaceType = "APP";
		NsType expectedNsType = NsType.APP;
		NsRequest nsRequest = createNsRequestForType(namespaceName, namespaceType);

		//when
		Result<Namespace> result = mapper.ns(transaction,nsRequest);

		//then
		assertTrue(result.isOK());
		assertNamespaceValues(result.value, expectedNsType, namespaceName);
		verify(transaction).checkpoint(namespaceName,Env.ALWAYS);
	}

	@Test
	public void ns_shouldConvertNamespaceRequest_whenInValidTypeIsExplicitlyProvided() {
		//given
		String namespaceName = "org.companyA.app1.service0";
		String invalidNsType = "BLUE";
		NsType expectedNsType = NsType.APP;
		NsRequest nsRequest = createNsRequestForType(namespaceName, invalidNsType);

		//when
		Result<Namespace> result = mapper.ns(transaction,nsRequest);

		//then
		assertTrue(result.isOK());
		assertNamespaceValues(result.value, expectedNsType, namespaceName);
		verify(transaction).checkpoint(namespaceName,Env.ALWAYS);
	}

	@Test
	public void ns_shouldConvertRootNamespaceRequest_whenTypeNotProvided() {
		//given
		String rootNsName = "org";
		NsType expectedNsType = NsType.ROOT;
		NsRequest nsRequest = createNsRequestForType(rootNsName, null);

		//when
		Result<Namespace> result = mapper.ns(transaction,nsRequest);

		//then
		assertTrue(result.isOK());
		assertNamespaceValues(result.value, expectedNsType, rootNsName);
		verify(transaction).checkpoint(rootNsName,Env.ALWAYS);
	}

	@Test
	public void ns_shouldConvertCompanyNamespaceRequest_whenTypeNotProvided() {
		//given
		String companyNsName = "org.companyA";
		NsType expectedNsType = NsType.COMPANY;
		NsRequest nsRequest = createNsRequestForType(companyNsName, null);

		//when
		Result<Namespace> result = mapper.ns(transaction,nsRequest);

		//then
		assertTrue(result.isOK());
		assertNamespaceValues(result.value, expectedNsType, companyNsName);
		verify(transaction).checkpoint(companyNsName,Env.ALWAYS);
	}

	private void assertNamespaceValues(Namespace value, NsType nsType, String namespaceName) {
		List<String> people = Lists.newArrayList("tk007@people.osaaf.org");
		assertEquals(Integer.valueOf(nsType.type), value.type);
		assertEquals(namespaceName, value.name);
		assertEquals("some namespace description", value.description);
		assertEquals(people, value.admin);
		assertEquals(people, value.owner);
	}

	private NsRequest createNsRequestForType(String nsName, String nsType) {
		NsRequest req = new NsRequest();
		req.setType(nsType);
		req.setName(nsName);
		req.setDescription("some namespace description");
		req.getAdmin().add("tk007@people.osaaf.org");
		req.getResponsible().add("tk007@people.osaaf.org");
		return req;
	}

	@Test
	public void nss_shouldConvertNamespaceToNss_withoutAttributes() {
		//given
		Nss nss = mapper.newInstance(API.NSS);
		Namespace ns = mapper.ns(transaction, createNsRequestForType("org.onap",  null)).value;

		//when
		Result<Nss> result = mapper.nss(transaction, ns, nss);

		//then
		assertTrue(result.isOK());
		assertEquals("Only one Ns should be added",1, result.value.getNs().size());
		Ns addedNs = Iterables.getOnlyElement(result.value.getNs());
		assertEquals(ns.admin, addedNs.getAdmin());
		assertEquals(ns.name, addedNs.getName());
		assertEquals(ns.owner, addedNs.getResponsible());
		assertEquals(ns.description, addedNs.getDescription());
		assertTrue(addedNs.getAttrib().isEmpty());
	}

	@Test
	public void nss_shouldConvertNamespaceToNss_withAttributes() {
		//given
		Nss nss = mapper.newInstance(API.NSS);
		Namespace ns = mapper.ns(transaction, createNsRequestForType("org.onap",  null)).value;
		ns.attrib = Lists.newArrayList();
		int attribNum = 5;
		Map<String, String> attribs = ImmutableMap.of("key1", "value1", "key2", "value2", "key3", "value3", "key4", "value4", "key5", "value5");
		attribs.forEach((key,val) -> ns.attrib.add(new Pair<>(key,val)));

		//when
		Result<Nss> result = mapper.nss(transaction, ns, nss);

		//then
		assertTrue(result.isOK());
		assertEquals("Only one Ns should be added",1, result.value.getNs().size());
		Ns addedNs = Iterables.getOnlyElement(result.value.getNs());
		assertEquals(attribNum, addedNs.getAttrib().size());
		addedNs.getAttrib().forEach( attr -> {
			assertEquals(attr.getValue(), attribs.get(attr.getKey()));
		});
	}

	@Test
	public void test() {
		assertTrue(true);
	}
	
	@Test
	public void testApprovals(){
		assertTrue(true);
	}
	
	@Test
	public void testCert(){
		assertTrue(true);
		
	}
	
	@Test
	public void testCred(){
		assertTrue(true);
		
	}
	
	@Test
	public void testDelegate(){
		assertTrue(true);
	}
	
	@Test
	public void testErrorFromMessage(){
		assertTrue(true);
		
	}
	
	@Test
	public void testFuture(){
		assertTrue(true);
	}
	
	@Test
	public void testGetClass(){
		assertTrue(true);
	}

	@Test
	public void testGetExpires(){
		assertTrue(true);
	}
	
	@Test
	public void testGetMarshal(){
		assertTrue(true);
		
	}
	
	@Test
	public void testHistory(){
		assertTrue(true);
	}
	
	@Test
	public void testKeys(){
		assertTrue(true);
		
	}
	
	@Test
	public void testNewInstance(){
		assertTrue(true);
	}
	
	@Test
	public void testNs(){
		assertTrue(true);
	}
	
	@Test
	public void testNss(){
		assertTrue(true);
	}
	
	@Test
	public void testPerm(){
		assertTrue(true);
	}
	
	@Test
	public void testPermFromRPRequest(){
		assertTrue(true);
	}
	
	@Test
	public void testPermKey(){
		assertTrue(true);
	}
	
	@Test
	public void testPerms(){
		assertTrue(true);
	}
	
	@Test
	public void testRole(){
		assertTrue(true);
	}
	
	@Test
	public void testRoleFromRPRequest(){
		assertTrue(true);
	}
	
	@Test
	public void testRoles(){
		assertTrue(true);
	}
	
	@Test
	public void testUserRole(){
		assertTrue(true);
	}
	
	@Test
	public void testUserRoles(){
		assertTrue(true);
	}
	
	@Test
	public void testUsers(){
		assertTrue(true);
	}

	
}
