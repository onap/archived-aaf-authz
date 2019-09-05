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

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HClient;
import org.onap.aaf.cadi.locator.DNSLocator;
import org.onap.aaf.cadi.locator.SingleEndpointLocator;
import org.onap.aaf.cadi.locator.SizedLocator;
import org.onap.aaf.cadi.util.FixURIinfo;
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
    private HClient client;
    private HClient lclient;
    private RosettaDF<Endpoints> epsDF;
    private SizedLocator<URI> locatorLocator;
    private Item locatorItem;


    public AAFLocator(SecurityInfoC<HttpURLConnection> si, URI locatorURI) throws LocatorException {
        super(si.access, nameFromLocatorURI(locatorURI), 10000L /* Wait at least 10 seconds between refreshes */);
        synchronized(sr) {
            if (env==null) {
                env = new RosettaEnv(access.getProperties());
            }
        }
        
        int connectTimeout = Integer.parseInt(si.access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
        try {
            String[] path = Split.split('/',locatorURI.getPath());
            FixURIinfo fui = new FixURIinfo(locatorURI);
            if ("AAF_LOCATE_URL".equals(fui.getHost())) {
                client = createClient(si.defSS, locatorURI, connectTimeout);
            } else if (path.length>1 && "locate".equals(path[1])) {
                StringBuilder sb = new StringBuilder();
                for (int i=3;i<path.length;++i) {
                    sb.append('/');
                    sb.append(path[i]);
                }
                setPathInfo(sb.toString());
                client = createClient(si.defSS, locatorURI, connectTimeout);
            } else {
                client = new HClient(si.defSS, locatorURI, connectTimeout);
            }
            epsDF = env.newDataFactory(Endpoints.class);
            
        } catch (APIException /*| URISyntaxException*/ e) {
            throw new LocatorException(e);
        }
        lclient = new HClient(si.defSS, locatorURI, connectTimeout);
        
        if(si.access.willLog(Access.Level.DEBUG)) {
            si.access.log(Access.Level.DEBUG, "Root URI:",client.getURI());
        }
        
        String dnsString;
        if(locatorURI.getPort()<0) {
        	dnsString=locatorURI.getScheme() + "://" + locatorURI.getHost();
        } else {
        	dnsString=locatorURI.getScheme() + "://" +locatorURI.getHost()+':'+locatorURI.getPort();
        }
        if(dnsString.contains("null")) { // for Testing Purposes, mostly.
        	locatorLocator = null;
        } else {
	        locatorLocator = new DNSLocator(access, dnsString);
	        if(locatorLocator.hasItems()) {
	        	locatorItem = locatorLocator.best();
	        } else {
	        	// For when DNS doesn't work, including some K8s Installations
				locatorLocator = new SingleEndpointLocator(dnsString);
	        }
        }
    }

    private URI locatorFail(URI uri) throws LocatorException, URISyntaxException {
        locatorLocator.invalidate(locatorItem);
        locatorItem = locatorLocator.best();
        URI newURI = locatorLocator.get(locatorItem);
        return new URI(uri.getScheme(),
                       uri.getUserInfo(),
                       newURI.getHost(),
                       newURI.getPort(),
                       uri.getPath(),
                       uri.getQuery(),
                       uri.getFragment());
    }

    protected final int maxIters() {
    	
        return locatorLocator.size();
    }


    @Override
    public boolean refresh() {
        try {
            int max = locatorLocator.size();
            for(int i=0;i<max;) {
                ++i;
                try {
                    lclient.setMethod("GET");
                    lclient.send();
                    break;
                } catch (APIException connectE) {
                    Throwable ce = connectE.getCause();
                    if(ce!=null && ce instanceof java.net.ConnectException && i< maxIters()) {
                        try {
                            URI old = client.getURI();
                            lclient.setURI(locatorFail(old));
                            access.printf(Level.INFO, "AAF Locator changed from %s to %s",old, lclient.getURI());
                            continue;
                        } catch (LocatorException e) {
                            throw connectE;
                        }
                    }
                    // last one, just throw
                    throw connectE;
                }
            }
            Future<Endpoints> fr = lclient.futureRead(epsDF, TYPE.JSON);
            if (fr.get(lclient.timeout())) {
                List<EP> epl = new LinkedList<>();
                for (Endpoint endpoint : fr.value.getEndpoint()) {
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
    
    protected HClient createClient(SecuritySetter<HttpURLConnection> ss, URI uri, int connectTimeout) throws LocatorException {
        return new HClient(ss, uri, connectTimeout);
    }
    
}
