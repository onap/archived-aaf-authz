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
package com.att.dao.aaf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.CredDAO;
import com.att.dao.aaf.cass.CredDAO.Data;
import com.att.inno.env.APIException;

/**
 * UserDAO unit test.
 * User: tp007s
 * Date: 7/19/13
 */
public class JU_CredDAO  extends AbsJUCass {
	@Test
	public void test() throws IOException, NoSuchAlgorithmException, APIException {
		CredDAO udao = new CredDAO(trans,cluster,"authz");
		try {
			// Create
	        CredDAO.Data data = new CredDAO.Data();
	        data.id = "m55555@aaf.att.com";
	        data.type = CredDAO.BASIC_AUTH;
	        data.notes = "temp pass";
	        data.cred      = ByteBuffer.wrap(userPassToBytes("m55555","mypass"));
	        data.other = 12;
	        data.expires = new Date(System.currentTimeMillis() + 60000*60*24*90);
			udao.create(trans,data);
			
//	        Bytification
	        ByteBuffer bb = data.bytify();
	        Data bdata = new CredDAO.Data();
	        bdata.reconstitute(bb);
	        checkData1(data, bdata);

			// Validate Read with key fields in Data
			Result<List<CredDAO.Data>> rlcd = udao.read(trans,data);
			assertTrue(rlcd.isOKhasData());
			for(CredDAO.Data d : rlcd.value) {
				checkData1(data,d);
			}
			
			// Update
			data.cred = ByteBuffer.wrap(userPassToBytes("m55555","mynewpass"));
			udao.update(trans,data);
			rlcd = udao.read(trans,data);
			assertTrue(rlcd.isOKhasData());
			for(CredDAO.Data d : rlcd.value) {
				checkData1(data,d);
			}			
			
			udao.delete(trans,data, true);
		} finally {
			udao.close(trans);
		}

		
	}

	private void checkData1(Data data, Data d) {
		assertEquals(data.id,d.id);
		assertEquals(data.type,d.type);
		assertEquals(data.ns,d.ns);
		assertEquals(data.notes,d.notes);
		assertEquals(data.cred,d.cred);
		assertEquals(data.other,d.other);
		assertEquals(data.expires,d.expires);
	}

//    private String                          CONST_myName = "MyName";
//    public static final java.nio.ByteBuffer CONST_MY_CRED = get_CONST_MY_CRED();
//    public static final int                 CONST_CRED_TYPE = 11;
//
//    public static final Date                CONST_UPDATE_DATE = new Date(System.currentTimeMillis()+60000*24);
//    @Test
//    public void test() {
//        UserDAO ud = new UserDAO(trans, cluster,"authz");
//        try {
//            UserDAO.Data data = createPrototypeUserData();
//            ud.create(trans, data);
//
//            // Validate Read with key fields in Data
//            for(UserDAO.Data d : ud.read(trans, data)) {
//                checkData1(data,d);
//            }
//
//            // Validate readByName
//            for(UserDAO.Data d : ud.read(trans, CONST_myName)) {
//                checkData1(data,d);
//            }
//
//            ud.delete(trans, data);
//            List<UserDAO.Data> d_2 = ud.read(trans, CONST_myName);
//
//            // Validate that data was deleted
//            assertEquals("User should not be found after deleted", 0, d_2.size() );
//
//            data = new UserDAO.Data();
//            data.name = CONST_myName;
//            data.cred = CONST_MY_CRED;
//            data.cred_type= CONST_CRED_TYPE;
//            data.expires = new Date(System.currentTimeMillis()+60000*24);
//            final Result<UserDAO.Data> user = ud.r_create(trans, data);
//            assertEquals("ud.createUser should work", Result.Status.OK, user.status);
//
//            checkDataIgnoreDateDiff(data, user.value);
//
//            // finally leave system in consistent state by deleting user again
//            ud.delete(trans,data);
//
//        } catch (DAOException e) {
//            e.printStackTrace();
//            fail("Fail due to Exception");
//        } finally {
//            ud.close(trans);
//        }
//    }
//
//    private UserDAO.Data createPrototypeUserData() {
//        UserDAO.Data data = new UserDAO.Data();
//        data.name = CONST_myName;
//
//        data.cred_type = CONST_CRED_TYPE;
//        data.cred      = CONST_MY_CRED;
//        data.expires = CONST_UPDATE_DATE;
//        return data;
//    }
//
//    //    @Test
//    //    public void testReadByUser() throws Exception {
//    //           // this test was done above in our super test, since it uses the same setup
//    //    }
//
//    @Test
//    public void testFunctionCreateUser() throws Exception {
//        String name = "roger_rabbit";
//        Integer credType = CONST_CRED_TYPE;
//        java.nio.ByteBuffer cred = CONST_MY_CRED;
//        final UserDAO ud = new UserDAO(trans, cluster,"authz");
//        final UserDAO.Data data = createPrototypeUserData();
//        Result<UserDAO.Data> ret = ud.r_create(trans, data);
//        Result<List<Data>> byUserNameLookup = ud.r_read(trans, name);
//        
//        assertEquals("sanity test w/ different username (different than other test cases) failed", name, byUserNameLookup.value.get(0).name);
//        assertEquals("delete roger_rabbit failed", true, ud.delete(trans, byUserNameLookup.value.get(0)));
//    }
//
//    @Test
//    public void testLowLevelCassandraCreateData_Given_UserAlreadyPresent_ShouldPass() throws Exception {
//        UserDAO ud = new UserDAO(trans, cluster,"authz");
//
//        final UserDAO.Data data = createPrototypeUserData();
//        final UserDAO.Data data1 = ud.create(trans, data);
//        final UserDAO.Data data2 = ud.create(trans, data);
//
//        assertNotNull(data1);
//        assertNotNull(data2);
//
//        assertEquals(CONST_myName, data1.name);
//        assertEquals(CONST_myName, data2.name);
//    }
//
//    @Test
//    public void testCreateUser_Given_UserAlreadyPresent_ShouldFail() throws Exception {
//        UserDAO ud = new UserDAO(trans, cluster,"authz");
//
//        final UserDAO.Data data = createPrototypeUserData();
//
//        // make sure that some prev test did not leave the user in the DB
//        ud.delete(trans, data);
//
//        // attempt to create same user twice !!!
//        
//        final Result<UserDAO.Data> data1 = ud.r_create(trans, data);
//        final Result<UserDAO.Data> data2 = ud.r_create(trans, data);
//
//        assertNotNull(data1);
//        assertNotNull(data2);
//
//        assertEquals(true,   Result.Status.OK == data1.status);
//        assertEquals(false,  Result.Status.OK == data2.status);
//    }
//
//    private void checkData1(UserDAO.Data data, UserDAO.Data d) {
//        data.name = CONST_myName;
//
//        data.cred_type = CONST_CRED_TYPE;
//        data.cred      = CONST_MY_CRED;
//        data.expires   = CONST_UPDATE_DATE;
//
//        assertEquals(data.name, d.name);
//        assertEquals(data.cred_type, d.cred_type);
//        assertEquals(data.cred, d.cred);
//        assertEquals(data.expires, d.expires);
//
//    }
//
//    private void checkDataIgnoreDateDiff(UserDAO.Data data, UserDAO.Data d) {
//        data.name = CONST_myName;
//
//        data.cred_type = CONST_CRED_TYPE;
//        data.cred      = CONST_MY_CRED;
//        data.expires   = CONST_UPDATE_DATE;
//
//        assertEquals(data.name, d.name);
//        assertEquals(data.cred_type, d.cred_type);
//        assertEquals(data.cred, d.cred);
//         // we allow dates to be different, e.g. high level calls e.g. createUser sets the date itself.
//        //assertEquals(data.updated, d.updated);
//
//    }
//
//    /**
//     * Get a CONST_MY_CRED ByteBuffer, which is the java type for a cass blob.
//     * @return
//     */
//    private static java.nio.ByteBuffer get_CONST_MY_CRED() {
//     return ByteBuffer.wrap("Hello".getBytes());
//    }
//
}
