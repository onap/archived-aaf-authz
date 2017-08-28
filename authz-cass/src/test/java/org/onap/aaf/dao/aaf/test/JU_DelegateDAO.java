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

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.onap.aaf.authz.layer.Result;
import org.onap.aaf.dao.aaf.cass.DelegateDAO;
import org.onap.aaf.dao.aaf.cass.DelegateDAO.Data;


public class JU_DelegateDAO  extends AbsJUCass {
	@Test
	public void testCRUD() throws Exception {
		DelegateDAO dao = new DelegateDAO(trans, cluster, AUTHZ);
		DelegateDAO.Data data = new DelegateDAO.Data();
		data.user = "myname";
		data.delegate = "yourname";
		data.expires = new Date();
		
//        Bytification
        ByteBuffer bb = data.bytify();
        Data bdata = new DelegateDAO.Data();
        bdata.reconstitute(bb);
        compare(data, bdata);

		try {
			// Test create
			Result<Data> ddcr = dao.create(trans,data);
			assertTrue(ddcr.isOK());
			
			
			// Read by User
			Result<List<DelegateDAO.Data>> records = dao.read(trans,data.user);
			assertTrue(records.isOKhasData());
			for(DelegateDAO.Data rdata : records.value) 
				compare(data,rdata);

			// Read by Delegate
			records = dao.readByDelegate(trans,data.delegate);
			assertTrue(records.isOKhasData());
			for(DelegateDAO.Data rdata : records.value) 
				compare(data,rdata);
			
			// Update
			data.delegate = "hisname";
			data.expires = new Date();
			assertTrue(dao.update(trans, data).isOK());

			// Read by User
			records = dao.read(trans,data.user);
			assertTrue(records.isOKhasData());
			for(DelegateDAO.Data rdata : records.value) 
				compare(data,rdata);

			// Read by Delegate
			records = dao.readByDelegate(trans,data.delegate);
			assertTrue(records.isOKhasData());
			for(DelegateDAO.Data rdata : records.value) 
				compare(data,rdata);

			// Test delete
			dao.delete(trans,data, true);
			records = dao.read(trans,data.user);
			assertTrue(records.isEmpty());
			
			
		} finally {
			dao.close(trans);
		}
	}
	
	private void compare(Data d1, Data d2) {
		assertEquals(d1.user, d2.user);
		assertEquals(d1.delegate, d2.delegate);
		assertEquals(d1.expires,d2.expires);
	}


}
