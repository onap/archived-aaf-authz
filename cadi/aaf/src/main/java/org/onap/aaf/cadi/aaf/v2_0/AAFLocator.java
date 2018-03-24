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

package org.onap.aaf.cadi.aaf.v2_0;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.util.Split;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.impl.BasicTrans;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import locate.v1_0.Endpoint;
import locate.v1_0.Endpoints;

public class AAFLocator extends AbsAAFLocator<BasicTrans>  {
	private static RosettaEnv env;
	HClient client;
	private RosettaDF<Endpoints> epsDF;

	public AAFLocator(SecurityInfoC<HttpURLConnection> si, URI locatorURI) throws LocatorException {
		super(si.access, nameFromLocatorURI(locatorURI), 10000L /* Wait at least 10 seconds between refreshes */);
		SecuritySetter<HttpURLConnection> ss;
		try {
			ss=AAFConHttp.bestSS(si);
		} catch (APIException | CadiException e1) {
			throw new LocatorException(e1);
		}
		synchronized(sr) {
			if(env==null) {
				env = new RosettaEnv(access.getProperties());
			}
		}
		
		int connectTimeout = Integer.parseInt(si.access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
		try {
			String[] path = Split.split('/',locatorURI.getPath());
			if(path.length>2 && "locate".equals(path[1])) {
				StringBuilder sb = new StringBuilder();
				for(int i=3;i<path.length;++i) {
					sb.append('/');
					sb.append(path[i]);
				}
				setPathInfo(sb.toString());
				String host = locatorURI.getHost();
				if(aaf_locator_host!=null && (host==null || "AAF_LOCATOR_URL".equals(host))) {
					int slash = aaf_locator_host.lastIndexOf("//");
					host = aaf_locator_host.substring(slash+2);
				}
				client = new HClient(ss, new URI(
											locatorURI.getScheme(),
											locatorURI.getUserInfo(),
											host,
											locatorURI.getPort(),
											"/locate/"+name + '/' + version,
											null,
											null
											), connectTimeout);
			} else {
				client = new HClient(ss, locatorURI, connectTimeout);
			}
			epsDF = env.newDataFactory(Endpoints.class);
			refresh();
		} catch (APIException | URISyntaxException e) {
			throw new LocatorException(e);
		}
	}

	@Override
	public boolean refresh() {
		try {
			client.setMethod("GET");
			client.send();
			Future<Endpoints> fr = client.futureRead(epsDF, TYPE.JSON);
			if(fr.get(client.timeout())) {
				List<EP> epl = new LinkedList<EP>();
				for(Endpoint endpoint : fr.value.getEndpoint()) {
					epl.add(new EP(endpoint,latitude,longitude));
				}
				
				Collections.sort(epl);
				replace(epl);
				return true;
			} else {
				env.error().printf("Error reading location information from %s: %d %s\n",client.getURI().toString(),fr.code(),fr.body());
			}
		} catch (CadiException | URISyntaxException | APIException e) {
			env.error().log(e,"Error connecting " + client.getURI() + " for location.");
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator#getURI()
	 */
	@Override
	protected URI getURI() {
		return client.getURI();
	}
}
