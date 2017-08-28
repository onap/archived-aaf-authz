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
package org.onap.aaf.authz.service.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.authz.cadi.DirectAAFLur.PermPermission;
import org.onap.aaf.authz.env.AuthzTrans;
import org.onap.aaf.authz.layer.Result;
import org.onap.aaf.authz.org.Organization;
import org.onap.aaf.authz.service.validation.Validator;
import org.onap.aaf.dao.aaf.cass.CredDAO;
import org.onap.aaf.dao.aaf.cass.DelegateDAO;
import org.onap.aaf.dao.aaf.cass.Namespace;
import org.onap.aaf.dao.aaf.cass.PermDAO;
import org.onap.aaf.dao.aaf.cass.RoleDAO;
import org.onap.aaf.dao.aaf.cass.UserRoleDAO;

public class JU_Validator {
	
	@Before
	public void setUp(){
		Validator validator = new Validator();
	}


	@Test
	public void test() {
		assertTrue(Validator.ACTION_CHARS.matcher("HowdyDoody").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("Howd?yDoody").matches());
		assertTrue(Validator.ACTION_CHARS.matcher("_HowdyDoody").matches());
		assertTrue(Validator.INST_CHARS.matcher("HowdyDoody").matches());
		assertFalse(Validator.INST_CHARS.matcher("Howd?yDoody").matches());
		assertTrue(Validator.INST_CHARS.matcher("_HowdyDoody").matches());

		//		
		assertTrue(Validator.ACTION_CHARS.matcher("*").matches());
		assertTrue(Validator.INST_CHARS.matcher("*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":*:*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":*:*").matches());
		
		assertFalse(Validator.ACTION_CHARS.matcher(":hello").matches());
		assertTrue(Validator.INST_CHARS.matcher(":hello").matches());
		assertFalse(Validator.INST_CHARS.matcher("hello:").matches());
		assertFalse(Validator.INST_CHARS.matcher("hello:d").matches());

		assertFalse(Validator.ACTION_CHARS.matcher(":hello:*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":hello:*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":hello:d*:*").matches());
		assertFalse(Validator.INST_CHARS.matcher(":hello:d*d:*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":hello:d*:*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("HowdyDoody*").matches());
		assertFalse(Validator.INST_CHARS.matcher("Howdy*Doody").matches());
		assertTrue(Validator.INST_CHARS.matcher("HowdyDoody*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("*HowdyDoody").matches());
		assertFalse(Validator.INST_CHARS.matcher("*HowdyDoody").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":h*").matches());
		assertFalse(Validator.INST_CHARS.matcher(":h*h*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":h*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":h:h*:*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":h:h*:*").matches());
		assertFalse(Validator.INST_CHARS.matcher(":h:h*h:*").matches());
		assertFalse(Validator.INST_CHARS.matcher(":h:h*h*:*").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":h:*:*h").matches());
		assertFalse(Validator.INST_CHARS.matcher(":h:*:*h").matches());
		assertTrue(Validator.INST_CHARS.matcher(":com.test.*:ns:*").matches());

		
		assertFalse(Validator.ACTION_CHARS.matcher("1234+235gd").matches());
		assertTrue(Validator.ACTION_CHARS.matcher("1234-235gd").matches());
		assertTrue(Validator.ACTION_CHARS.matcher("1234-23_5gd").matches());
		assertTrue(Validator.ACTION_CHARS.matcher("1234-235g,d").matches());
		assertTrue(Validator.ACTION_CHARS.matcher("1234-235gd(Version12)").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("123#4-23@5g:d").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("123#4-23@5g:d").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("1234-23 5gd").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("1234-235gd ").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(" 1234-235gd").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(" ").matches());

		// Allow % and =   (Needed for Escaping & Base64 usages) jg 
		assertTrue(Validator.ACTION_CHARS.matcher("1234%235g=d").matches());
		assertFalse(Validator.ACTION_CHARS.matcher(":1234%235g=d").matches());
		assertTrue(Validator.INST_CHARS.matcher("1234%235g=d").matches());
		assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d").matches());
		assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:%20==").matches());
		assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:==%20:=%23").matches());
		assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:*:=%23").matches());
		assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:==%20:*").matches());
		assertTrue(Validator.INST_CHARS.matcher(":*:==%20:*").matches());

		// Allow / instead of :  (more natural instance expression) jg 
		assertFalse(Validator.INST_CHARS.matcher("1234/a").matches());
		assertTrue(Validator.INST_CHARS.matcher("/1234/a").matches());
		assertTrue(Validator.INST_CHARS.matcher("/1234/*/a/").matches());
		assertTrue(Validator.INST_CHARS.matcher("/1234//a").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("1234/a").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("/1234/*/a/").matches());
		assertFalse(Validator.ACTION_CHARS.matcher("1234//a").matches());


		assertFalse(Validator.INST_CHARS.matcher("1234+235gd").matches());
		assertTrue(Validator.INST_CHARS.matcher("1234-235gd").matches());
		assertTrue(Validator.INST_CHARS.matcher("1234-23_5gd").matches());
		assertTrue(Validator.INST_CHARS.matcher("1234-235g,d").matches());
		assertTrue(Validator.INST_CHARS.matcher("m1234@shb.dd.com").matches());
		assertTrue(Validator.INST_CHARS.matcher("1234-235gd(Version12)").matches());
		assertFalse(Validator.INST_CHARS.matcher("123#4-23@5g:d").matches());
		assertFalse(Validator.INST_CHARS.matcher("123#4-23@5g:d").matches());
		assertFalse(Validator.INST_CHARS.matcher("").matches());

		
		for( char c=0x20;c<0x7F;++c) {
			boolean b;
			switch(c) {
				case '?':
				case '|':
				case '*':
					continue; // test separately
				case '~':
				case ',':
					b = false;
					break;
				default:
					b=true;
			}
		}
		
		assertFalse(Validator.ID_CHARS.matcher("abc").matches());
		assertFalse(Validator.ID_CHARS.matcher("").matches());
		assertTrue(Validator.ID_CHARS.matcher("abc@att.com").matches());
		assertTrue(Validator.ID_CHARS.matcher("ab-me@att.com").matches());
		assertTrue(Validator.ID_CHARS.matcher("ab-me_.x@att._-com").matches());
		
		assertFalse(Validator.NAME_CHARS.matcher("ab-me_.x@att._-com").matches());
		assertTrue(Validator.NAME_CHARS.matcher("ab-me").matches());
		assertTrue(Validator.NAME_CHARS.matcher("ab-me_.xatt._-com").matches());

		
		// 7/22/2016
		assertTrue(Validator.INST_CHARS.matcher(
				"/!com.att.*/role/write").matches());
		assertTrue(Validator.INST_CHARS.matcher(
				":!com.att.*:role:write").matches());

	}

}
