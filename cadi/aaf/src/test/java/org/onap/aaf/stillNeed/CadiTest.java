/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.stillNeed;

import java.net.HttpURLConnection;
import java.net.URI;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HBasicAuthSS;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.http.HX509SS;

public class CadiTest {
	public static void main(String args[]) {
		Access access = new PropAccess();
		try {
			SecurityInfoC<HttpURLConnection> si = SecurityInfoC.instance(access, HttpURLConnection.class);
			SecuritySetter<HttpURLConnection> ss;
			if(access.getProperty(Config.CADI_ALIAS,null)!=null) {
				ss = new HX509SS(si);
			} else {
				ss = new HBasicAuthSS(si);
			}
			HClient hclient = new HClient(ss,new URI("https://zlp08851.vci.att.com:8095"),3000);
			hclient.setMethod("OPTIONS");
			hclient.setPathInfo("/cadi/log/set/WARN");
			hclient.send();
			Future<String> future = hclient.futureReadString();
			if(future.get(5000)) {
				System.out.printf("Success %s",future.value);
			} else {
				System.out.printf("Error: %d-%s", future.code(),future.body());
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
