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
package org.onap.aaf.dao.aaf.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.junit.Test;
import org.onap.aaf.authz.layer.Result;
import org.onap.aaf.dao.aaf.cass.CertDAO;
import org.onap.aaf.dao.aaf.cass.CertDAO.Data;

import org.onap.aaf.inno.env.APIException;

/**
 * UserDAO unit test.
 * User: tp007s
 * Date: 7/19/13
 */
public class JU_CertDAO  extends AbsJUCass {
	@Test
	public void test() throws IOException, NoSuchAlgorithmException, APIException {
		CertDAO cdao = new CertDAO(trans,cluster,"authz");
		try {
			// Create
	        CertDAO.Data data = new CertDAO.Data();
	        data.serial=new BigInteger("11839383");
	        data.id = "m55555@tguard.att.com";
	        data.x500="CN=ju_cert.dao.att.com, OU=AAF, O=\"ATT Services, Inc.\", L=Southfield, ST=Michigan, C=US";
	        data.x509="I'm a cert";
	        data.ca = "aaf";
			cdao.create(trans,data);

//	        Bytification
	        ByteBuffer bb = data.bytify();
	        Data bdata = new CertDAO.Data();
	        bdata.reconstitute(bb);
	        checkData1(data, bdata);

			// Validate Read with key fields in Data
			Result<List<CertDAO.Data>> rlcd = cdao.read(trans,data);
			assertTrue(rlcd.isOKhasData());
			for(CertDAO.Data d : rlcd.value) {
				checkData1(data,d);
			}

			// Validate Read with key fields in Data
			rlcd = cdao.read(trans,data.ca,data.serial);
			assertTrue(rlcd.isOKhasData());
			for(CertDAO.Data d : rlcd.value) {
				checkData1(data,d);
			}

			// Update
			data.id = "m66666.tguard.att.com";
			cdao.update(trans,data);
			rlcd = cdao.read(trans,data);
			assertTrue(rlcd.isOKhasData());
			for(CertDAO.Data d : rlcd.value) {
				checkData1(data,d);
			}			
			
			cdao.delete(trans,data, true);
		} finally {
			cdao.close(trans);
		}

		
	}

	private void checkData1(Data data, Data d) {
		assertEquals(data.ca,d.ca);
		assertEquals(data.serial,d.serial);
		assertEquals(data.id,d.id);
		assertEquals(data.x500,d.x500);
		assertEquals(data.x509,d.x509);
	}

}
