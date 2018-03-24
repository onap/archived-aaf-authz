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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO;
import org.onap.aaf.auth.dao.cass.OAuthTokenDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.oauth.AAFToken;

/**
 * UserDAO unit test.
 * Date: 7/19/13
 */
public class JU_OAuthTokenDAO  extends AbsJUCass {
	@Test
	public void test() throws IOException, NoSuchAlgorithmException {
		OAuthTokenDAO adao = new OAuthTokenDAO(trans,cluster,CassAccess.KEYSPACE);
		UUID uuid = UUID.randomUUID();
		try {
			// Create
	        Data data = new OAuthTokenDAO.Data();
	        data.id=AAFToken.toToken(uuid);
	        data.client_id="zClient";
	        data.user = "xy1255@csp.att.com";
	        data.active = true;
	        data.type=1;
	        data.refresh = AAFToken.toToken(UUID.randomUUID());
	        data.expires=new Date();
	        data.scopes(false).add("org.osaaf.aaf");
	        data.scopes(false).add("org.osaaf.grid");
	        data.content="{darth:\"I am your content\"}";
	        data.req_ip="::1";
	        
//	        Bytification
	        ByteBuffer bb = data.bytify();
	        Data bdata = new OAuthTokenDAO.Data();
//	        System.out.println(new String(Symm.base64noSplit.encode(bb.array())));
	        bdata.reconstitute(bb);
	        checkData1(data, bdata);
	        
//	        DB work
			adao.create(trans,data);
			try {
				// Validate Read with Data Object
				Result<List<OAuthTokenDAO.Data>> rlcd = adao.read(trans,data);
				assertTrue(rlcd.isOKhasData());
				for(OAuthTokenDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}
				// Validate Read with key fields in Data
				rlcd = adao.read(trans,data.id);
				assertTrue(rlcd.isOKhasData());
				for(OAuthTokenDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}
				
				// Validate Read by User
				rlcd = adao.readByUser(trans,data.user);
				assertTrue(rlcd.isOKhasData());
				for(OAuthTokenDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}

				// Update
				data.content = "{darth:\"I am your content\", luke:\"Noooooooo!\"}";
				data.active = false;
				adao.update(trans,data);
				rlcd = adao.read(trans,data);
				assertTrue(rlcd.isOKhasData());
				for(OAuthTokenDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}			

			} finally {
				// Always delete data, even if failure.
				adao.delete(trans,data, true);
			}
		} finally {
			adao.close(trans);
		}

	}

	private void checkData1(Data data, Data d) {
		assertEquals(data.id,d.id);
		assertEquals(data.client_id,d.client_id);
		assertEquals(data.user,d.user);
		assertEquals(data.active,d.active);
		assertEquals(data.type,d.type);
		assertEquals(data.refresh,d.refresh);
		assertEquals(data.expires,d.expires);
		for(String s: data.scopes(false)) {
			assertTrue(d.scopes(false).contains(s));
		}
		for(String s: d.scopes(false)) {
			assertTrue(data.scopes(false).contains(s));
		}
		assertEquals(data.content,d.content);
		assertEquals(data.state,d.state);
		assertEquals(data.req_ip,d.req_ip);
	}

}
