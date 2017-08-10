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
package com.att.dao.aaf.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.PermDAO;
import com.att.dao.aaf.cass.RoleDAO;
import com.att.dao.aaf.cass.RoleDAO.Data;
import com.att.inno.env.APIException;


public class JU_RoleDAO extends AbsJUCass {

	@Test
	public void test()  throws IOException, APIException {
		RoleDAO rd = new RoleDAO(trans, cluster, AUTHZ);
		try {
			Data data = new RoleDAO.Data();
			data.ns = "com.test.ju_role";
			data.name = "role1";

//	        Bytification
	        ByteBuffer bb = data.bytify();
	        Data bdata = new RoleDAO.Data();
	        bdata.reconstitute(bb);
	        compare(data, bdata);

			// CREATE
			Result<Data> rdc = rd.create(trans, data);
			assertTrue(rdc.isOK());
			Result<List<Data>> rdrr;
			try {
				// READ
				rdrr = rd.read(trans, data);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				Data d = rdrr.value.get(0);
				assertEquals(d.perms.size(),0);
				assertEquals(d.name,data.name);
				assertEquals(d.ns,data.ns);

				PermDAO.Data perm = new PermDAO.Data();
				perm.ns = data.ns;
				perm.type = "Perm";
				perm.instance = "perm1";
				perm.action = "write";
				
				// ADD Perm
				Result<Void> rdar = rd.addPerm(trans, data, perm);
				assertTrue(rdar.isOK());
				rdrr = rd.read(trans, data);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				assertEquals(rdrr.value.get(0).perms.size(),1);
				assertTrue(rdrr.value.get(0).perms.contains(perm.encode()));
				
				// DEL Perm
				rdar = rd.delPerm(trans, data,perm);
				assertTrue(rdar.isOK());
				rdrr = rd.read(trans, data);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				assertEquals(rdrr.value.get(0).perms.size(),0);

				// Add Child
				Data data2 = new Data();
				data2.ns = data.ns;
				data2.name = data.name + ".2";
				
				rdc = rd.create(trans, data2);
				assertTrue(rdc.isOK());
				try {
					rdrr = rd.readChildren(trans, data.ns,data.name);
					assertTrue(rdrr.isOKhasData());
					assertEquals(rdrr.value.size(),1);
					assertEquals(rdrr.value.get(0).name,data.name + ".2");
					
					rdrr = rd.readChildren(trans, data.ns,"*");
					assertTrue(rdrr.isOKhasData());
					assertEquals(rdrr.value.size(),2);

				} finally {
					// Delete Child
					rd.delete(trans, data2, true);
				}
	
			} finally {
				// DELETE
				Result<Void> rddr = rd.delete(trans, data, true);
				assertTrue(rddr.isOK());
				rdrr = rd.read(trans, data);
				assertTrue(rdrr.isOK() && rdrr.isEmpty());
				assertEquals(rdrr.value.size(),0);
			}
		} finally {
			rd.close(trans);
		}
	}

	private void compare(Data a, Data b) {
		assertEquals(a.name,b.name);
		assertEquals(a.description, b.description);
		assertEquals(a.ns,b.ns);
		assertEquals(a.perms(false).size(),b.perms(false).size());
		for(String p : a.perms(false)) {
			assertTrue(b.perms(false).contains(p));
		}
	}

}
