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

public class JU_FastCalling extends AbsJUCass {

	@Test
	public void test() throws IOException, NoSuchAlgorithmException, APIException {
		trans.setProperty("cassandra.writeConsistency.cred","ONE");
		
		CredDAO udao = new CredDAO(env.newTransNoAvg(),cluster,"authz");
		System.out.println("Starting calls");
		for(iterations=0;iterations<8;++iterations) {
			try {
				// Create
		        CredDAO.Data data = new CredDAO.Data();
		        data.id = "m55555@aaf.att.com";
		        data.type = CredDAO.BASIC_AUTH;
		        data.cred      = ByteBuffer.wrap(userPassToBytes("m55555","mypass"));
		        data.expires = new Date(System.currentTimeMillis() + 60000*60*24*90);
				udao.create(trans,data);
				
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
				updateTotals();
				newTrans();
			}
		}

	}

	private void checkData1(Data data, Data d) {
		assertEquals(data.id,d.id);
		assertEquals(data.type,d.type);
		assertEquals(data.cred,d.cred);
		assertEquals(data.expires,d.expires);
	}

}
