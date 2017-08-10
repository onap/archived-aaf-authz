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
package com.att.authz.cm.service;

import com.att.authz.cm.facade.Facade1_0;
import com.att.authz.env.AuthzTrans;
import com.att.cssa.rserv.HttpCode;

public abstract class Code extends HttpCode<AuthzTrans,Facade1_0> implements Cloneable {

	public Code(CertManAPI cma, String description, String ... roles) {
		super(cma.facade1_0, description, roles);
		// Note, the first "Code" will be created with default Facade, "JSON".
		// use clone for another Code with XML
	}
	

	public <D extends Code> D clone(Facade1_0 facade) throws Exception {
		@SuppressWarnings("unchecked")
		D d = (D)clone();
		d.context = facade;
		return d;
	}

}
