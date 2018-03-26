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

package com.att.dao.aaf.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.dao.cass.LocateDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

/**
 * Test the LocateDAO
 * 
 * Utilize AbsJUCass to initialize and pre-load Cass
 * 
 * @author Jonathan
 *
 */
public class JU_LocateDAO extends AbsJUCass{

	@Test
	public void test() throws APIException, IOException {
		LocateDAO pd = new LocateDAO(trans,cluster,CassAccess.KEYSPACE);
		try {
			LocateDAO.Data data = new LocateDAO.Data();
			data.name="org.osaaf.aaf.locateTester";
			data.hostname="mithrilcsp.sbc.com";
			data.port=19999;
			data.latitude=32.780140f;
			data.longitude=-96.800451f;
			data.major=2;
			data.minor=0;
			data.patch=19;
			data.pkg=23;
			data.protocol="https";
			Set<String> sp = data.subprotocol(true);
			sp.add("TLS1.1");
			sp.add("TLS1.2");
			


			// CREATE
			Result<Data> rpdc = pd.create(trans,data);
			assertTrue(rpdc.isOK());

			Result<List<LocateDAO.Data>> rlpd;
			try {
//		        Bytification
		        ByteBuffer bb = data.bytify();
		        Data bdata = new LocateDAO.Data();
		        bdata.reconstitute(bb);
		        compare(data, bdata);

				// Validate Read with key fields in Data
		        rlpd = pd.read(trans,data);
		        assertTrue(rlpd.isOK());
		        if(rlpd.isOK()) {
					for(LocateDAO.Data d : rlpd.value) {
						compare(data,d);
					}
		        }

		        // Validate Read by Name
		        rlpd = pd.readByName(trans,data.name);
		        assertTrue(rlpd.isOK());
		        if(rlpd.isOK()) {
					for(LocateDAO.Data d : rlpd.value) {
						compare(data,d);
					}
		        }

				// Modify
				data.latitude = -31.0000f;
				
				Result<Void> rupd = pd.update(trans, data);
				assertTrue(rupd.isOK());
		        rlpd = pd.read(trans,data);
		        assertTrue(rlpd.isOK());
		        if(rlpd.isOK()) {
					for(LocateDAO.Data d : rlpd.value) {
						compare(data,d);
					}
		        }

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// DELETE
				Result<Void> rpdd = pd.delete(trans,data,true);
				assertTrue(rpdd.isOK());
				rlpd = pd.read(trans, data);
				assertTrue(rlpd.isOK() && rlpd.isEmpty());
				assertEquals(rlpd.value.size(),0);
			}
		} finally {
			pd.close(trans);
		}
	}

	private void compare(Data a, Data b) {
		assertEquals(a.name,b.name);
		assertEquals(a.hostname,b.hostname);
		assertEquals(a.port,b.port);
		assertEquals(a.major,b.major);
		assertEquals(a.minor,b.minor);
		assertEquals(a.patch,b.patch);
		assertEquals(a.pkg,b.pkg);
		assertEquals(a.latitude,b.latitude);
		assertEquals(a.longitude,b.longitude);
		assertEquals(a.protocol,b.protocol);
		Set<String> spa = a.subprotocol(false);
		Set<String> spb = b.subprotocol(false);
		assertEquals(spa.size(),spb.size());
		for(String s : spa) {
			assertTrue(spb.contains(s));
		}
	}
}
