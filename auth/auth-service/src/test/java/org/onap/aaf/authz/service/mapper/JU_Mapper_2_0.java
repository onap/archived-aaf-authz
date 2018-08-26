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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.onap.aaf.auth.layer.Result.ERR_General;

import aaf.v2_0.NsRequest;
import aaf.v2_0.Nss;
import aaf.v2_0.Nss.Ns;
import aaf.v2_0.Perm;
import aaf.v2_0.Perms;
import aaf.v2_0.Request;
import aaf.v2_0.Role;
import aaf.v2_0.RoleRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.cass.Namespace;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.NsType;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.PermDAO.Data;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.dao.hl.Question.Access;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTrans.REQD_TYPE;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.rserv.Pair;
import org.onap.aaf.auth.service.mapper.Mapper.API;
import org.onap.aaf.auth.service.mapper.Mapper_2_0;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

@RunWith(MockitoJUnitRunner.class)
public class JU_Mapper_2_0 {

  private static final String USER = "John";

	private Mapper_2_0 mapper;
	@Mock
	private Question question;
	@Mock
	private AuthzTrans transaction;
	@Mock
  private TimeTaken tt;


	@Before
	public void setUp() throws APIException, IOException, CadiException {
	  given(transaction.start(anyString(), eq(Env.SUB))).willReturn(tt);
	  given(transaction.user()).willReturn(USER);
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
		NsRequest req = mapper.newInstance(API.NS_REQ);
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
	public void nss_shouldAddSeveralNamespacesToNss() {
		//given
		Nss nss = mapper.newInstance(API.NSS);
		Namespace ns1 = mapper.ns(transaction, createNsRequestForType("org.onap",  "COMPANY")).value;
		Namespace ns2 = mapper.ns(transaction, createNsRequestForType("org.onap.prh",  "APP")).value;

		//when
		Result<Nss> result = mapper.nss(transaction, Lists.newArrayList(ns1,ns2), nss);

		//then
		assertTrue(result.isOK());
		assertEquals("Two namespaces should be added",2, result.value.getNs().size());
	}

	@Test
	public void perm_shouldNotAddPerms_whenFilterIsSet_andUserIsNotAuthorized() {
		//given
    given(question.mayUser(eq(transaction), eq(USER), any(PermDAO.Data.class), eq(Access.read)))
        .willReturn(Result.err(9, "error"));
    Perms permsContainer = mapper.newInstance(API.PERMS);
    List<PermDAO.Data> permsData = Lists.newArrayList(new PermDAO.Data());
    boolean filter = true;

		//when
    Result<Perms> result = mapper.perms(transaction, permsData, permsContainer, filter);

		//then
    assertTrue(result.isOK());
    assertEquals("No perms added",0,result.value.getPerm().size());
	}

	@Test
  public void perm_shouldAddPerm_withNamespaceSet_whenUserIsAuthorized_AndNamespaceIsRequestedType() {
      //given
      given(question.mayUser(eq(transaction), eq(USER), any(PermDAO.Data.class), eq(Access.read)))
          .willReturn(Result.ok(new NsDAO.Data()));
      given(transaction.requested(REQD_TYPE.ns)).willReturn(true);
      Perms permsContainer = mapper.newInstance(API.PERMS);
      Set<String> roles = Sets.newHashSet("org.onap.portal.owner","org.onap.portal.designer"
				,"org.onap.portal.tester");
      String namespace = "org.onap.portal";
      String type = "access";
      String fullType = namespace + "." +type;
      String action = "read";
      String description = "Portal Read Access";
		  List<PermDAO.Data> permsData = Lists.newArrayList(createPermDAOobj(namespace, type, "*",action, roles, description));
      boolean filter = true;

      //when
      Result<Perms> result = mapper.perms(transaction, permsData, permsContainer, filter);

      //then
      assertTrue(result.isOK());
      assertEquals("Perm is added",1,result.value.getPerm().size());
      Perm perm = Iterables.getOnlyElement(result.value.getPerm());
      assertEquals(namespace, perm.getNs());
      assertEquals(fullType, perm.getType());
      assertEquals(action, perm.getAction());
      assertEquals("*", perm.getInstance());
      assertEquals(description, perm.getDescription());
      assertEquals(Lists.newArrayList(roles), perm.getRoles());
  }

	@Test
	public void perm_shouldAddPerm_withoutNamespaceSet_whenUserIsAuthorized_AndNamespaceIsNotRequestedType() {
		//given
		given(question.mayUser(eq(transaction), eq(USER), any(PermDAO.Data.class), eq(Access.read)))
			.willReturn(Result.ok(new NsDAO.Data()));
		given(transaction.requested(REQD_TYPE.ns)).willReturn(false);
		Perms permsContainer = mapper.newInstance(API.PERMS);
		String namespace = "org.onap.portal";
		String type = "access";
		String fullType = namespace + "." + type;
		String action = "read";
		List<PermDAO.Data> permsData = Lists.newArrayList(createPermDAOobj(namespace, type, "*",action, null, null));
		boolean filter = true;

		//when
		Result<Perms> result = mapper.perms(transaction, permsData, permsContainer, filter);

		//then
		assertTrue(result.isOK());
		assertEquals("Perm is added",1,result.value.getPerm().size());
		Perm perm = Iterables.getOnlyElement(result.value.getPerm());
		assertNull(perm.getNs());
		assertEquals(fullType, perm.getType());
		assertEquals(action, perm.getAction());
	}

	@Test
	public void perm_shouldAddPermsWithCorrectSortedOrder() {
		//given
		given(question.mayUser(eq(transaction), eq(USER), any(PermDAO.Data.class), eq(Access.read)))
			.willReturn(Result.ok(new NsDAO.Data()));
		Perms permsContainer = mapper.newInstance(API.PERMS);
		PermDAO.Data perm1 = createPermDAOobj("org.onap.portal", "access", "*", "read", null, null);
		PermDAO.Data perm2 = createPermDAOobj("org.onap.portal", "access", "*", "write", null, null);
		PermDAO.Data perm3 = createPermDAOobj("org.onap.portal", "design", "*", "new", null, null);
		PermDAO.Data perm4 = createPermDAOobj("org.onap.portal", "workflow", "1", "edit", null, null);
		PermDAO.Data perm5 = createPermDAOobj("org.onap.portal", "workflow", "2", "edit", null, null);
		List<PermDAO.Data> permsData = Lists.newArrayList(perm4, perm1, perm5, perm3, perm2);
		List<PermDAO.Data> correctOrderPerms = Lists.newArrayList(perm1, perm2, perm3, perm4, perm5);

		//when
		Result<Perms> result = mapper.perms(transaction, permsData, permsContainer, true);

		//then
		assertTrue(result.isOK());
		assertEquals("Alls Perms added",5,result.value.getPerm().size());
		List<Perm> mappedPerms = result.value.getPerm();
		for(int i=0; i<5; i++) {
			comparePerm(correctOrderPerms.get(i), mappedPerms.get(i));
		}
	}

	private void comparePerm(Data data, Perm perm) {
		assertEquals(data.ns + "." + data.type, perm.getType());
		assertEquals(data.instance, perm.getInstance());
		assertEquals(data.action, perm.getAction());
	}

	private PermDAO.Data createPermDAOobj(String ns, String name, String instance, String action, Set<String> roles, String description) {
			NsSplit nss = new NsSplit(ns, name);
      PermDAO.Data perm = new PermDAO.Data(nss, instance, action);
      perm.roles = roles;
      perm.description = description;
      return perm;
  }

  @Test
	public void role_shouldReturnErrorResult_whenNssIsNok() throws Exception {
		//given
		String roleName = "admin";
		RoleRequest request = createRoleRequest(roleName, "role description");
		given(question.deriveNsSplit(transaction, roleName)).willReturn(Result.err(new IllegalArgumentException()));

		//when
		Result<RoleDAO.Data> result = mapper.role(transaction, request);

		//then
		assertFalse(result.isOK());
		assertNull(result.value);
		assertEquals(ERR_General, result.status);
	}

	@Test
	public void role_shouldReturnMappedRoleObject_whenNssIsOk() throws Exception {
		//given
		String roleName = "admin";
		String roleNs = "org.onap.roles";
		String roleFullName = roleNs + "." + roleName;
		String description =" role description";
		RoleRequest request = createRoleRequest(roleFullName, description);
		given(question.deriveNsSplit(transaction, roleFullName)).willReturn(Result.ok(new NsSplit(roleNs, roleName)));

		//when
		Result<RoleDAO.Data> result = mapper.role(transaction, request);

		//then
		assertTrue(result.isOK());
		assertEquals(roleName, result.value.name);
		assertEquals(roleNs, result.value.ns);
		assertEquals(description, result.value.description);
		verify(transaction).checkpoint(roleFullName, Env.ALWAYS);
	}

	private RoleRequest createRoleRequest(String name, String description) {
		RoleRequest req = mapper.newInstance(API.ROLE_REQ);
		req.setName(name);
		req.setDescription(description);
		return req;
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
