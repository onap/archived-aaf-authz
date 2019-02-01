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
 */

package org.onap.aaf.cadi.register;

import java.net.UnknownHostException;
import java.util.List;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.Defaults;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.config.RegistrationPropHolder;
import org.onap.aaf.cadi.util.Split;

import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;

public class RegistrationCreator {
	private Access access;
    
    public RegistrationCreator(Access access) {
    	this.access = access;
    }
    
    public MgmtEndpoints create(final int port) throws CadiException {
    	MgmtEndpoints me = new MgmtEndpoints();
    	List<MgmtEndpoint> lme = me.getMgmtEndpoint();
    	MgmtEndpoint defData = null;
    	MgmtEndpoint locate = null;

    	try {
    		String dot_le;
    		String version=null;
    		
    		RegistrationPropHolder ph = new RegistrationPropHolder(access, port);
    		
    		// Now, loop through by Container
    		for(String le : Split.splitTrim(',', ph.lcontainer)) {
    			if(le.isEmpty()) {
    				dot_le = le;
    			} else {
    				dot_le = "."+le;
    			}

    			for(String entry : Split.splitTrim(',', ph.lentries)) {
    				if(defData==null) {
    					defData = locate = new MgmtEndpoint();

    					version = access.getProperty(Config.AAF_LOCATOR_VERSION, Defaults.AAF_VERSION);
    					locate.setProtocol(access.getProperty(Config.AAF_LOCATOR_PROTOCOL,null));
    					List<String> ls = locate.getSubprotocol();
    					for(String sp : Split.splitTrim(',', access.getProperty(Config.AAF_LOCATOR_SUBPROTOCOL,""))) {
    						ls.add(sp);	
    					}
    					locate.setLatitude(ph.latitude);
    					locate.setLongitude(ph.longitude);

    				} else {
    					locate = copy(defData);
    				}
    				
    				locate.setName(ph.getEntryName(entry,dot_le));
    				locate.setHostname(ph.getEntryFQDN(entry,dot_le));
    				locate.setPort(ph.getEntryPort(dot_le));
    				
    				String specificVersion = access.getProperty(Config.AAF_LOCATOR_VERSION + dot_le,null);
    				if(specificVersion == null && locate == defData) {
    					specificVersion = version;
    				}
    				if(specificVersion!=null) {
    					String split[] = Split.splitTrim('.', specificVersion);
    					locate.setPkg(split.length>3?Integer.parseInt(split[3]):0);
    					locate.setPatch(split.length>2?Integer.parseInt(split[2]):0);
    					locate.setMinor(split.length>1?Integer.parseInt(split[1]):0);
    					locate.setMajor(split.length>0?Integer.parseInt(split[0]):0);
    				}

    				String protocol = access.getProperty(Config.AAF_LOCATOR_PROTOCOL + dot_le, null);
    				if (protocol!=null) {
    					locate.setProtocol(protocol);
    					String subprotocols = access.getProperty(Config.AAF_LOCATOR_SUBPROTOCOL + dot_le, null);
    					if(subprotocols!=null) {
    						List<String> ls = locate.getSubprotocol();
    						for (String s : Split.split(',', subprotocols)) {
    							ls.add(s);
    						}
    					}
    				}
    				lme.add(locate);
    			}
    		}
    	} catch (NumberFormatException | UnknownHostException e) {
    		throw new CadiException("Error extracting Data from Properties for Registrar",e);
    	}

    	return me;
    }
	
    private MgmtEndpoint copy(MgmtEndpoint mep) {
		MgmtEndpoint out = new MgmtEndpoint();
		out.setName(mep.getName());
		out.setHostname(mep.getHostname());
		out.setLatitude(mep.getLatitude());
		out.setLongitude(mep.getLongitude());
		out.setMajor(mep.getMajor());
		out.setMinor(mep.getMinor());
		out.setPkg(mep.getPkg());
		out.setPatch(mep.getPatch());
		out.setPort(mep.getPort());
		out.setProtocol(mep.getProtocol());
		out.getSpecialPorts().addAll(mep.getSpecialPorts());
		out.getSubprotocol().addAll(mep.getSubprotocol());
		return out;
	}
}
