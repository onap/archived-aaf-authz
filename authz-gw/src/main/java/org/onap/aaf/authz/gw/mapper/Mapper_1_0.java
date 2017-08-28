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
package org.onap.aaf.authz.gw.mapper;

import org.onap.aaf.cadi.util.Vars;

import gw.v1_0.Error;
import gw.v1_0.InRequest;
import gw.v1_0.Out;

public class Mapper_1_0 implements Mapper<InRequest,Out,Error> {
	
	@Override
	public Class<?> getClass(API api) {
		switch(api) {
			case IN_REQ: return InRequest.class;
			case OUT: return Out.class;
			case ERROR: return Error.class;
			case VOID: return Void.class;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <A> A newInstance(API api) {
		switch(api) {
			case IN_REQ: return (A) new InRequest();
			case OUT: return (A) new Out();
			case ERROR: return (A)new Error();
			case VOID: return null;
		}
		return null;
	}

	//////////////  Mapping Functions /////////////
	@Override
	public gw.v1_0.Error errorFromMessage(StringBuilder holder, String msgID, String text,String... var) {
		Error err = new Error();
		err.setMessageId(msgID);
		// AT&T Restful Error Format requires numbers "%" placements
		err.setText(Vars.convert(holder, text, var));
		for(String s : var) {
			err.getVariables().add(s);
		}
		return err;
	}

}
