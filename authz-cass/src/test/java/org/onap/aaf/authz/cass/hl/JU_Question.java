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
package org.onap.aaf.authz.cass.hl;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.layer.Result;
import org.onap.aaf.dao.aaf.cass.NsDAO;
import org.onap.aaf.dao.aaf.cass.PermDAO;
import org.onap.aaf.dao.aaf.cass.RoleDAO;
import org.onap.aaf.dao.aaf.cass.UserRoleDAO;
import org.onap.aaf.dao.aaf.cass.NsDAO.Data;
import org.onap.aaf.dao.aaf.hl.Question;
import org.onap.aaf.dao.aaf.hl.Question.Access;
import org.onap.aaf.dao.aaf.test.AbsJUCass;

import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.TimeTaken;

public class JU_Question extends AbsJUCass {

	private static final int EXPIRES_IN = 60000000;
	private static final String COM_TEST_JU = "com.test.ju_question";
	private static final String JU9999_JU_TEST_COM = "ju9999@ju.test.com";
	private static final String JU9998_JU_TEST_COM = "ju9998@ju.test.com";
	private static final String READ = "read";
	private static final int NFR_1 = 80;
	private static final int NFR_2 = 4000;
	private static final int ROLE_LEVEL1 = 1000;
	private static final int PERM_LEVEL1 = 1000;
//	private static final int PERM_LEVEL2 = 20;
	private static Question q;
	private static NsDAO.Data ndd;

	@BeforeClass
	public static void startupBeforeClass() throws Exception {
		details=false;
		AuthzTrans trans = env.newTransNoAvg();
		q = new Question(trans,cluster,AUTHZ, false);
		ndd = new NsDAO.Data();
		ndd.name=COM_TEST_JU;
		ndd.type=3; // app
		ndd.parent="com.test";
		ndd.description="Temporary Namespace for JU_Question";
		q.nsDAO.create(trans, ndd);
	}
	
	@AfterClass
	public static void endAfterClass() throws Exception {
		q.nsDAO.delete(trans, ndd,false);
	}
//    @Test
	public void mayUserRead_EmptyPerm() {
		PermDAO.Data pdd = new PermDAO.Data();
		Result<NsDAO.Data> result = q.mayUser(trans,JU9999_JU_TEST_COM,pdd,Access.read);
		assertFalse(result.isOK());
	}

//    @Test
	public void mayUserRead_OnePermNotExist() {
		Result<NsDAO.Data> result = q.mayUser(trans,JU9999_JU_TEST_COM,newPerm(0,0,READ),Access.read);
		assertFalse(result.isOK());
		assertEquals("Denied - ["+ JU9999_JU_TEST_COM +"] may not read Perm [" + COM_TEST_JU + ".myPerm0|myInstance0|read]",result.errorString());
	}
	
//    @Test
	public void mayUserRead_OnePermExistDenied() {
		PermDAO.Data perm = newPerm(0,0,READ);
		q.permDAO.create(trans,perm);
		try {
			Result<NsDAO.Data> result;
			TimeTaken tt = trans.start("q.mayUser...", Env.SUB);
			try {
				result = q.mayUser(trans,JU9999_JU_TEST_COM,perm,Access.read);
			} finally {
				tt.done();
				assertTrue("NFR time < "+ NFR_1 + "ms",tt.millis()<NFR_1);
			}
			assertFalse(result.isOK());
			assertEquals("Denied - ["+ JU9999_JU_TEST_COM +"] may not read Perm ["+COM_TEST_JU + ".myPerm0|myInstance0|read]",result.errorString());
		} finally {
			q.permDAO.delete(trans, perm, false);
		}
	}

//    @Test
	public void mayUserRead_OnePermOneRoleExistOK() {
		PermDAO.Data perm = newPerm(0,0,READ);
		RoleDAO.Data role = newRole(0,perm);
		UserRoleDAO.Data ur = newUserRole(role,JU9999_JU_TEST_COM,EXPIRES_IN);
		try {
			q.permDAO.create(trans,perm);
			q.roleDAO.create(trans,role);
			q.userRoleDAO.create(trans,ur);
			
			Result<NsDAO.Data> result;
			TimeTaken tt = trans.start("q.mayUser...", Env.SUB);
			try {
				result = q.mayUser(trans,JU9999_JU_TEST_COM,perm,Access.read);
			} finally {
				tt.done();
				assertTrue("NFR time < "+ NFR_1 + "ms",tt.millis()<NFR_1);
			}
			assertTrue(result.isOK());
		} finally {
			q.permDAO.delete(trans, perm, false);
			q.roleDAO.delete(trans, role, false);
			q.userRoleDAO.delete(trans, ur, false);
		}
	}

//	@Test
	public void filter_OnePermOneRoleExistOK() {
		PermDAO.Data perm = newPerm(0,0,READ);
		RoleDAO.Data role = newRole(0,perm);
		UserRoleDAO.Data ur1 = newUserRole(role,JU9998_JU_TEST_COM,EXPIRES_IN);
		UserRoleDAO.Data ur2 = newUserRole(role,JU9999_JU_TEST_COM,EXPIRES_IN);
		try {
			q.permDAO.create(trans,perm);
			q.roleDAO.create(trans,role);
			q.userRoleDAO.create(trans,ur1);
			q.userRoleDAO.create(trans,ur2);
			
			Result<List<PermDAO.Data>> pres;
			TimeTaken tt = trans.start("q.getPerms...", Env.SUB);
			try {
				pres = q.getPermsByUserFromRolesFilter(trans, JU9999_JU_TEST_COM, JU9999_JU_TEST_COM);
			} finally {
				tt.done();
				trans.info().log("filter_OnePermOneRleExistOK",tt);
				assertTrue("NFR time < "+ NFR_1 + "ms",tt.millis()<NFR_1);
			}
			assertTrue(pres.isOK());
			
			try {
				pres = q.getPermsByUserFromRolesFilter(trans, JU9999_JU_TEST_COM, JU9998_JU_TEST_COM);
			} finally {
				tt.done();
				trans.info().log("filter_OnePermOneRleExistOK No Value",tt);
				assertTrue("NFR time < "+ NFR_1 + "ms",tt.millis()<NFR_1);
			}
			assertFalse(pres.isOKhasData());

		} finally {
			q.permDAO.delete(trans, perm, false);
			q.roleDAO.delete(trans, role, false);
			q.userRoleDAO.delete(trans, ur1, false);
			q.userRoleDAO.delete(trans, ur2, false);
		}
	}

//    @Test
	public void mayUserRead_OnePermMultiRoleExistOK() {
		PermDAO.Data perm = newPerm(0,0,READ);
		List<RoleDAO.Data> lrole = new ArrayList<RoleDAO.Data>();
		List<UserRoleDAO.Data> lur = new ArrayList<UserRoleDAO.Data>();
		try {
			q.permDAO.create(trans,perm);
			for(int i=0;i<ROLE_LEVEL1;++i) {
				RoleDAO.Data role = newRole(i,perm);
				lrole.add(role);
				q.roleDAO.create(trans,role);
				
				UserRoleDAO.Data ur = newUserRole(role,JU9999_JU_TEST_COM,60000000);
				lur.add(ur);
				q.userRoleDAO.create(trans,ur);
			}
			
			Result<NsDAO.Data> result;
			TimeTaken tt = trans.start("mayUserRead_OnePermMultiRoleExistOK", Env.SUB);
			try {
				result = q.mayUser(trans,JU9999_JU_TEST_COM,perm,Access.read);
			} finally {
				tt.done();
				env.info().log(tt,ROLE_LEVEL1,"iterations");
				assertTrue("NFR time < "+ NFR_2 + "ms",tt.millis()<NFR_2);
			}
			assertTrue(result.isOK());
		} finally {
			q.permDAO.delete(trans, perm, false);
			for(RoleDAO.Data role : lrole) {
				q.roleDAO.delete(trans, role, false);
			}
			for(UserRoleDAO.Data ur : lur) {
				q.userRoleDAO.delete(trans, ur, false);
			}
		}
	}

    @Test
	public void mayUserRead_MultiPermOneRoleExistOK() {
		RoleDAO.Data role = newRole(0);
		UserRoleDAO.Data ur = newUserRole(role,JU9999_JU_TEST_COM,EXPIRES_IN);
		List<PermDAO.Data> lperm = new ArrayList<PermDAO.Data>();
		try {
			for(int i=0;i<PERM_LEVEL1;++i) {
				lperm.add(newPerm(i,i,READ,role));
			}
			q.roleDAO.create(trans, role);
			q.userRoleDAO.create(trans, ur);
			
			Result<NsDAO.Data> result;
			TimeTaken tt = trans.start("mayUserRead_MultiPermOneRoleExistOK", Env.SUB);
			try {
				result = q.mayUser(trans,JU9999_JU_TEST_COM,lperm.get(PERM_LEVEL1-1),Access.read);
			} finally {
				tt.done();
				env.info().log(tt,PERM_LEVEL1,"iterations");
				assertTrue("NFR time < "+ NFR_2 + "ms",tt.millis()<NFR_2);
			}
			assertTrue(result.isOK());
		} finally {
			for(PermDAO.Data perm : lperm) {
				q.permDAO.delete(trans, perm, false);
			}
			q.roleDAO.delete(trans, role, false);
			q.userRoleDAO.delete(trans, ur, false);
		}
	}

////	@Test
//	public void mayUserRead_MultiPermMultiRoleExistOK() {
//		List<PermDAO.Data> lperm = new ArrayList<PermDAO.Data>();
//		List<RoleDAO.Data> lrole = new ArrayList<RoleDAO.Data>();
//		List<UserRoleDAO.Data> lur = new ArrayList<UserRoleDAO.Data>();
//
//		try {
//			RoleDAO.Data role;
//			UserRoleDAO.Data ur;
//			for(int i=0;i<ROLE_LEVEL1;++i) {
//				lrole.add(role=newRole(i));
//				q.roleDAO.create(trans, role);
//				lur.add(ur=newUserRole(role, JU9999_JU_TEST_COM, EXPIRES_IN));
//				q.userRoleDAO.create(trans, ur);
//				for(int j=0;j<PERM_LEVEL2;++j) {
//					lperm.add(newPerm(i,j,READ,role));
//				}
//			}
//			
//			Result<NsDAO.Data> result;
//			TimeTaken tt = trans.start("mayUserRead_MultiPermMultiRoleExistOK", Env.SUB);
//			try {
//				result = q.mayUser(trans,JU9999_JU_TEST_COM,lperm.get(ROLE_LEVEL1*PERM_LEVEL2-1),Access.read);
//			} finally {
//				tt.done();
//				env.info().log(tt,lperm.size(),"perms",", ",lrole.size(),"role");
//				assertTrue("NFR time < "+ NFR_2 + "ms",tt.millis()<NFR_2);
//			}
//			assertTrue(result.isOK());
//		} finally {
//			for(PermDAO.Data perm : lperm) {
//				q.permDAO.delete(trans, perm, false);
//			}
//			for(RoleDAO.Data role : lrole) {
//				q.roleDAO.delete(trans, role, false);
//			}
//			for(UserRoleDAO.Data ur : lur) {
//				q.userRoleDAO.delete(trans, ur, false);
//			}
//		}
//	}

	@Test
	public void mayUserRead_MultiPermMultiRoleExist_10x10() {
		env.info().log("Original Filter Method 10x10");
		mayUserRead_MultiPermMultiRoleExist(10,10);
		env.info().log("New Filter Method 10x10");
		mayUserRead_MultiPermMultiRoleExist_NewOK(10,10);
	}

//	@Test
	public void mayUserRead_MultiPermMultiRoleExist_20x10() {
		env.info().log("mayUserRead_MultiPermMultiRoleExist_20x10");
		mayUserRead_MultiPermMultiRoleExist_NewOK(20,10);
	}

//	@Test
	public void mayUserRead_MultiPermMultiRoleExist_100x10() {
		env.info().log("mayUserRead_MultiPermMultiRoleExist_100x10");
		mayUserRead_MultiPermMultiRoleExist_NewOK(100,10);
	}

//	@Test
	public void mayUserRead_MultiPermMultiRoleExist_100x20() {
		env.info().log("mayUserRead_MultiPermMultiRoleExist_100x20");
		mayUserRead_MultiPermMultiRoleExist_NewOK(100,20);
	}

//	@Test
	public void mayUserRead_MultiPermMultiRoleExist_1000x20() {
		env.info().log("mayUserRead_MultiPermMultiRoleExist_1000x20");
		mayUserRead_MultiPermMultiRoleExist_NewOK(1000,20);
	}

	private void mayUserRead_MultiPermMultiRoleExist(int roleLevel, int permLevel) {
		List<PermDAO.Data> lperm = new ArrayList<PermDAO.Data>();
		List<RoleDAO.Data> lrole = new ArrayList<RoleDAO.Data>();
		List<UserRoleDAO.Data> lur = new ArrayList<UserRoleDAO.Data>();
		load(roleLevel, permLevel, lperm,lrole,lur);


		Result<List<PermDAO.Data>> pres;
		trans.setUser(new Principal() {
			@Override
			public String getName() {
				return JU9999_JU_TEST_COM;
			}
		});

		try {
			TimeTaken group = trans.start("  Original Security Method (1st time)", Env.SUB);
			try {
				TimeTaken tt = trans.start("    Get User Perms for "+JU9998_JU_TEST_COM, Env.SUB);
				try {
					pres = q.getPermsByUser(trans,JU9998_JU_TEST_COM,true);
				} finally {
					tt.done();
					env.info().log(tt,"  Looked up (full) getPermsByUser for",JU9998_JU_TEST_COM);
				}
				assertTrue(pres.isOK());
				tt = trans.start("    q.mayUser", Env.SUB);
				List<PermDAO.Data> reduced = new ArrayList<PermDAO.Data>();
				
				try {
					for(PermDAO.Data p : pres.value) {
						Result<Data> r = q.mayUser(trans,JU9999_JU_TEST_COM,p,Access.read);
						if(r.isOK()) {
							reduced.add(p);
						}
					}
				} finally {
					tt.done();
					env.info().log(tt," reduced" + pres.value.size(),"perms","to",reduced.size());
	//				assertTrue("NFR time < "+ NFR_2 + "ms",tt.millis()<NFR_2);
				}
	//			assertFalse(result.isOK());
			} finally {
				group.done();
				env.info().log(group,"  Original Validation Method (1st pass)");
			}
			

		} finally {
			unload(lperm, lrole, lur);
		}
	}

	private void mayUserRead_MultiPermMultiRoleExist_NewOK(int roleLevel, int permLevel) {
		List<PermDAO.Data> lperm = new ArrayList<PermDAO.Data>();
		List<RoleDAO.Data> lrole = new ArrayList<RoleDAO.Data>();
		List<UserRoleDAO.Data> lur = new ArrayList<UserRoleDAO.Data>();
		load(roleLevel, permLevel, lperm,lrole,lur);

		try {

			Result<List<PermDAO.Data>> pres;
			TimeTaken tt = trans.start("  mayUserRead_MultiPermMultiRoleExist_New New Filter", Env.SUB);
			try {
				pres = q.getPermsByUserFromRolesFilter(trans, JU9999_JU_TEST_COM, JU9998_JU_TEST_COM);
			} finally {
				tt.done();
				env.info().log(tt,lperm.size(),"perms",", ",lrole.size(),"role", lur.size(), "UserRoles");
//				assertTrue("NFR time < "+ NFR_2 + "ms",tt.millis()<NFR_2);
			}
//			assertTrue(pres.isOKhasData());

			tt = trans.start("  mayUserRead_MultiPermMultiRoleExist_New New Filter (2nd time)", Env.SUB);
			try {
				pres = q.getPermsByUserFromRolesFilter(trans, JU9999_JU_TEST_COM, JU9998_JU_TEST_COM);
			} finally {
				tt.done();
				env.info().log(tt,lperm.size(),"perms",", ",lrole.size(),"role", lur.size(), "UserRoles");
				assertTrue("NFR time < "+ NFR_2 + "ms",tt.millis()<NFR_2);
			}
//			assertTrue(pres.isOKhasData());

		} finally {
			unload(lperm, lrole, lur);
		}
	}


	private void load(int roleLevel, int permLevel,	List<PermDAO.Data> lperm , List<RoleDAO.Data> lrole, List<UserRoleDAO.Data> lur) {
		RoleDAO.Data role;
		UserRoleDAO.Data ur;
		PermDAO.Data perm;
		
		int onethirdR=roleLevel/3;
		int twothirdR=onethirdR*2;
		int onethirdP=permLevel/3;
		int twothirdP=onethirdP*2;

		for(int i=0;i<roleLevel;++i) {
			lrole.add(role=newRole(i));
			if(i<onethirdR) { // one has
				lur.add(ur=newUserRole(role, JU9998_JU_TEST_COM, EXPIRES_IN));
				q.userRoleDAO.create(trans, ur);
				for(int j=0;j<onethirdP;++j) {
					lperm.add(perm=newPerm(i,j,READ,role));
					q.permDAO.create(trans, perm);
				}
			} else if(i<twothirdR) { // both have
				lur.add(ur=newUserRole(role, JU9998_JU_TEST_COM, EXPIRES_IN));
				q.userRoleDAO.create(trans, ur);
				lur.add(ur=newUserRole(role, JU9999_JU_TEST_COM, EXPIRES_IN));
				q.userRoleDAO.create(trans, ur);
				for(int j=onethirdP;j<twothirdP;++j) {
					lperm.add(perm=newPerm(i,j,READ,role));
					q.permDAO.create(trans, perm);
				}
			} else { // other has
				lur.add(ur=newUserRole(role, JU9999_JU_TEST_COM, EXPIRES_IN));
				q.userRoleDAO.create(trans, ur);
				for(int j=twothirdP;j<permLevel;++j) {
					lperm.add(perm=newPerm(i,j,READ,role));
					q.permDAO.create(trans, perm);
				}
			}
			q.roleDAO.create(trans, role);
		}

	}
	
	private void unload(List<PermDAO.Data> lperm , List<RoleDAO.Data> lrole, List<UserRoleDAO.Data> lur) {
		for(PermDAO.Data perm : lperm) {
			q.permDAO.delete(trans, perm, false);
		}
		for(RoleDAO.Data role : lrole) {
			q.roleDAO.delete(trans, role, false);
		}
		for(UserRoleDAO.Data ur : lur) {
			q.userRoleDAO.delete(trans, ur, false);
		}

	}
	private PermDAO.Data newPerm(int permNum, int instNum, String action, RoleDAO.Data ... grant) {
		PermDAO.Data pdd = new PermDAO.Data();
		pdd.ns=COM_TEST_JU;
		pdd.type="myPerm"+permNum;
		pdd.instance="myInstance"+instNum;
		pdd.action=action;
		for(RoleDAO.Data r : grant) {
			pdd.roles(true).add(r.fullName());
			r.perms(true).add(pdd.encode());
		}
		return pdd;
	}

	private RoleDAO.Data newRole(int roleNum, PermDAO.Data ... grant) {
		RoleDAO.Data rdd = new RoleDAO.Data();
		rdd.ns = COM_TEST_JU+roleNum;
		rdd.name = "myRole"+roleNum;
		for(PermDAO.Data p : grant) {
			rdd.perms(true).add(p.encode());
			p.roles(true).add(rdd.fullName());
		}
		return rdd;
	}

	private UserRoleDAO.Data newUserRole(RoleDAO.Data role,String user, long offset) {
		UserRoleDAO.Data urd = new UserRoleDAO.Data();
		urd.user=user;
		urd.role(role);
		urd.expires=new Date(System.currentTimeMillis()+offset);
		return urd;
	}


}
