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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.att.authz.layer.Result;
import com.att.dao.aaf.cass.NsDAO;
import com.att.dao.aaf.cass.NsDAO.Data;
import com.att.dao.aaf.cass.NsType;
import com.att.inno.env.APIException;


public class JU_NsDAO extends AbsJUCass {
	private static final String CRM = "ju_crm";
	private static final String SWM = "ju_swm";

	@Test
	public void test() throws APIException, IOException  {
		NsDAO nsd = new NsDAO(trans, cluster, AUTHZ);
		try {
			final String nsparent = "com.test";
			final String ns1 = nsparent +".ju_ns";
			final String ns2 = nsparent + ".ju_ns2";
			
			Map<String,String> oAttribs = new HashMap<String,String>();
			oAttribs.put(SWM, "swm_data");
			oAttribs.put(CRM, "crm_data");
			Data data = new NsDAO.Data();
			data.name = ns1;
			data.type = NsType.APP.type;
			data.attrib(true).putAll(oAttribs);
			

			Result<List<Data>> rdrr;

			// CREATE
			Result<Data> rdc = nsd.create(trans, data);
			assertTrue(rdc.isOK());
			
			try {
//		        Bytification
		        ByteBuffer bb = data.bytify();
		        Data bdata = new NsDAO.Data();
		        bdata.reconstitute(bb);
		        compare(data, bdata);

				// Test READ by Object
				rdrr = nsd.read(trans, data);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				Data d = rdrr.value.get(0);
				assertEquals(d.name,data.name);
				assertEquals(d.type,data.type);
				attribsEqual(d.attrib(false),data.attrib(false));
				attribsEqual(oAttribs,data.attrib(false));
				
				// Test Read by Key
				rdrr = nsd.read(trans, data.name);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				d = rdrr.value.get(0);
				assertEquals(d.name,data.name);
				assertEquals(d.type,data.type);
				attribsEqual(d.attrib(false),data.attrib(false));
				attribsEqual(oAttribs,data.attrib(false));
				
				// Read NS by Type
				Result<Set<String>> rtypes = nsd.readNsByAttrib(trans, SWM);
				Set<String> types;
				if(rtypes.notOK()) {
					throw new IOException(rtypes.errorString());
				} else {
					types = rtypes.value;
				}
				assertEquals(1,types.size());
				assertEquals(true,types.contains(ns1));
				
				// Add second NS to test list of data returned
				Data data2 = new NsDAO.Data();
				data2.name = ns2;
				data2.type = 3; // app
				Result<Data> rdc2 = nsd.create(trans, data2);
				assertTrue(rdc2.isOK());
				
					// Interrupt - test PARENT
					Result<List<Data>> rdchildren = nsd.getChildren(trans, "com.test");
					assertTrue(rdchildren.isOKhasData());
					boolean child1 = false;
					boolean child2 = false;
					for(Data dchild : rdchildren.value) {
						if(ns1.equals(dchild.name))child1=true;
						if(ns2.equals(dchild.name))child2=true;
					}
					assertTrue(child1);
					assertTrue(child2);

				// FINISH DATA 2 by deleting
				Result<Void> rddr = nsd.delete(trans, data2, true);
				assertTrue(rddr.isOK());

				// ADD DESCRIPTION
				String description = "This is my test Namespace";
				assertFalse(description.equalsIgnoreCase(data.description));
				
				Result<Void> addDesc = nsd.addDescription(trans, data.name, description);
				assertTrue(addDesc.isOK());
				rdrr = nsd.read(trans, data);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				assertEquals(rdrr.value.get(0).description,description);
				
				// UPDATE
				String newDescription = "zz1234 Owns This Namespace Now";
				oAttribs.put("mso", "mso_data");
				data.attrib(true).put("mso", "mso_data");
				data.description = newDescription;
				Result<Void> update = nsd.update(trans, data);
				assertTrue(update.isOK());
				rdrr = nsd.read(trans, data);
				assertTrue(rdrr.isOKhasData());
				assertEquals(rdrr.value.size(),1);
				assertEquals(rdrr.value.get(0).description,newDescription);
				attribsEqual(oAttribs, rdrr.value.get(0).attrib);
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				// DELETE
				Result<Void> rddr = nsd.delete(trans, data, true);
				assertTrue(rddr.isOK());
				rdrr = nsd.read(trans, data);
				assertTrue(rdrr.isOK() && rdrr.isEmpty());
				assertEquals(rdrr.value.size(),0);
			}
		} finally {
			nsd.close(trans);
		}
	}

	private void compare(NsDAO.Data d, NsDAO.Data data) {
		assertEquals(d.name,data.name);
		assertEquals(d.type,data.type);
		attribsEqual(d.attrib(false),data.attrib(false));
		attribsEqual(d.attrib(false),data.attrib(false));
	}
	
	private void attribsEqual(Map<String,String> aa, Map<String,String> ba) {
		assertEquals(aa.size(),ba.size());
		for(Entry<String, String> es : aa.entrySet()) {
			assertEquals(es.getValue(),ba.get(es.getKey()));
		}
	}
}
