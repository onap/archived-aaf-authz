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

package org.onap.aaf.cadi.enduser.test;

import java.net.URISyntaxException;
import java.security.Principal;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.enduser.ClientFactory;
import org.onap.aaf.cadi.enduser.SimpleRESTClient;
import org.onap.aaf.misc.env.APIException;


public class SimpleRestClientExample {
	public final static void main(final String args[]) throws URISyntaxException, LocatorException {
		try {
			// Note: Expect ClientFactory to be long-lived... do NOT create more than once.
			ClientFactory cf = new ClientFactory(args);
			
	
			String urlString = cf.getAccess().getProperty("myurl", null);
			if(urlString==null) {
				System.out.println("Note: In your startup, add \"myurl=https://<aaf hello machine>:8130\" to command line\n\t"
						+ "OR\n\t" 
						+ " add -Dmyurl=https://<aaf hello machine>:8130 to VM Args\n\t"
						+ "where \"aaf hello machine\" is an aaf Installation you know about.");
			} else {
				SimpleRESTClient restClient = cf.simpleRESTClient(urlString,"org.osaaf.aaf");
				
				// Make some calls
				
				// Call with no Queries
				String rv = restClient.get("resthello");
				System.out.println(rv);
				
				// Call with Queries
				rv = restClient.get("resthello?perm=org.osaaf.people|*|read");
				System.out.println(rv);
				
				// Call setting ID from principal coming from Trans
				// Pretend Transaction
				HRequest req = new HRequest("demo@people.osaaf.org"); // Pretend Trans has Jonathan as Identity
				
				rv = restClient.as(req.userPrincipal()).get("resthello?perm=org.osaaf.people|*|read");
				System.out.println(rv);
			}			
		} catch (CadiException | APIException e) {
			e.printStackTrace();
		}
	}
	
	private static class HRequest { 
		
		public HRequest(String fqi) {
			name = fqi;
		}
		protected final String name;

	// fake out HttpServletRequest, only for get Principal
		public Principal userPrincipal() {
			return new Principal() {

				@Override
				public String getName() {
					return name;
				}
				
			};
		}
	}
}
