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
package org.onap.aaf.authz.service;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.authz.cadi.DirectAAFUserPass;
import org.onap.aaf.authz.env.AuthzEnv;
import org.onap.aaf.authz.facade.AuthzFacade_2_0;
import org.onap.aaf.authz.service.AuthAPI;
import org.onap.aaf.dao.aaf.hl.Question;

public class JU_AuthAPI {
	
	public AuthAPI authAPI;
	AuthzEnv env;
	private static final String ORGANIZATION = "Organization.";
	private static final String DOMAIN = "openecomp.org";

    public Question question;
    private AuthzFacade_2_0 facade;
    private AuthzFacade_2_0 facade_XML;
    private DirectAAFUserPass directAAFUserPass;
    public Properties props;
	@Before
	public void setUp(){
		try {
			authAPI = new AuthAPI(env);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testStartDME2(Properties props){
		try {
			authAPI.startDME2(props);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//assertTrue(true);
		
	}


	

}
