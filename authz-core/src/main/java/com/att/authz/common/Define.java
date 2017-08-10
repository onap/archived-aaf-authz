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
package com.att.authz.common;

import com.att.cadi.CadiException;
import com.att.cadi.config.Config;
import com.att.inno.env.Env;

public class Define {
	public static String ROOT_NS="NS.Not.Set";
	public static String ROOT_COMPANY=ROOT_NS;

	public static void set(Env env) throws CadiException {
		ROOT_NS = env.getProperty(Config.AAF_ROOT_NS);
		if(ROOT_NS==null) {
			throw new CadiException(Config.AAF_ROOT_NS + " property is required.");
		}
		ROOT_COMPANY = env.getProperty(Config.AAF_ROOT_COMPANY);
		if(ROOT_COMPANY==null) {
			int last = ROOT_NS.lastIndexOf('.');
			if(last>=0) {
				ROOT_COMPANY = ROOT_NS.substring(0, last);
			} else {
				throw new CadiException(Config.AAF_ROOT_COMPANY + " or " + Config.AAF_ROOT_NS + " property with 3 positions is required.");
			}
		}
		env.init().log("AAF Root NS is " + ROOT_NS + ", and AAF Root Company is " +ROOT_COMPANY);
	}
	
}
