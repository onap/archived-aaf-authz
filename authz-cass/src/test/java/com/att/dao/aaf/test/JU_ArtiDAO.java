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
import com.att.dao.aaf.cass.ArtiDAO;
import com.att.dao.aaf.cass.ArtiDAO.Data;

/**
 * UserDAO unit test.
 * User: tp007s
 * Date: 7/19/13
 */
public class JU_ArtiDAO  extends AbsJUCass {
	@Test
	public void test() throws IOException, NoSuchAlgorithmException {
		ArtiDAO adao = new ArtiDAO(trans,cluster,"authz");
		try {
			// Create
	        ArtiDAO.Data data = new ArtiDAO.Data();
	        data.mechid="m55555@perturbed.att.com";
	        data.machine="perturbed1232.att.com";
	        data.type(false).add("file");
	        data.type(false).add("jks");
	        data.sponsor="Fred Flintstone";
	        data.ca="devl";
	        data.dir="/opt/app/aft/keys";
	        data.appName="kumquat";
	        data.os_user="aft";
	        data.notify="email:myname@bogus.email.com";
	        data.expires=new Date();
	        
//	        Bytification
	        ByteBuffer bb = data.bytify();
	        Data bdata = new ArtiDAO.Data();
	        bdata.reconstitute(bb);
	        checkData1(data, bdata);
	        
	        
//	        DB work
			adao.create(trans,data);
			try {
				// Validate Read with key fields in Data
				Result<List<ArtiDAO.Data>> rlcd = adao.read(trans,data);
				assertTrue(rlcd.isOKhasData());
				for(ArtiDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}
	
				// Validate Read with key fields in Data
				rlcd = adao.read(trans,data.mechid, data.machine);
				assertTrue(rlcd.isOKhasData());
				for(ArtiDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}
	
				// By Machine
				rlcd = adao.readByMachine(trans,data.machine);
				assertTrue(rlcd.isOKhasData());
				for(ArtiDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}
				
				// By MechID
				rlcd = adao.readByMechID(trans,data.mechid);
				assertTrue(rlcd.isOKhasData());
				for(ArtiDAO.Data d : rlcd.value) {
					checkData1(data,d);
				}
	
				// Update
				data.sponsor = "Wilma Flintstone";
				adao.update(trans,data);
				rlcd = adao.read(trans,data);
				assertTrue(rlcd.isOKhasData());
				for(ArtiDAO.Data d : rlcd.value) {
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
		assertEquals(data.mechid,d.mechid);
		assertEquals(data.machine,d.machine);
		assertEquals(data.type(false).size(),d.type(false).size());
		for(String s: data.type(false)) {
			assertTrue(d.type(false).contains(s));
		}
		assertEquals(data.sponsor,d.sponsor);
		assertEquals(data.ca,d.ca);
		assertEquals(data.dir,d.dir);
		assertEquals(data.appName,d.appName);
		assertEquals(data.os_user,d.os_user);
		assertEquals(data.notify,d.notify);
		assertEquals(data.expires,d.expires);
	}

}
