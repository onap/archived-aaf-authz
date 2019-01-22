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

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.aaf.Defaults;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.util.Split;

import locate.v1_0.MgmtEndpoint;
import locate.v1_0.MgmtEndpoints;

public class RegistrationCreator {
    private static final String MUST_BE_DEFINED = " must be defined\n";
	private Access access;
    
    public RegistrationCreator(Access access) {
    	this.access = access;
    }
    
    public MgmtEndpoints create(final int port) throws CadiException {
    	MgmtEndpoints me = new MgmtEndpoints();
    	List<MgmtEndpoint> lme = me.getMgmtEndpoint();
    	MgmtEndpoint defData = null;
    	MgmtEndpoint locate = null;


    	StringBuilder errs = new StringBuilder();
    	try {
    		String hostname = access.getProperty(Config.HOSTNAME, null);
    		if (hostname==null) {
    			hostname = Inet4Address.getLocalHost().getHostName();
    		}
    		if (hostname==null) {
    			errs.append(Config.HOSTNAME);
    			errs.append(MUST_BE_DEFINED);
    		}

    		Float latitude=null;
    		String slatitude = access.getProperty(Config.CADI_LATITUDE, null);
    		if(slatitude == null) {
    			errs.append(Config.CADI_LATITUDE);
    			errs.append(MUST_BE_DEFINED);
    		} else {
    			latitude = Float.parseFloat(slatitude);
    		}

    		Float longitude=null;
    		String slongitude = access.getProperty(Config.CADI_LONGITUDE, null);
    		if(slongitude == null) {
    			errs.append(Config.CADI_LONGITUDE);
    			errs.append(MUST_BE_DEFINED);
    		} else {
    			longitude = Float.parseFloat(slongitude);
    		}

    		if(errs.length()>0) {
    			throw new CadiException(errs.toString());
    		}

    		String dot_le;
    		String ns;
    		String version=null;
    		String lentries = access.getProperty(Config.AAF_LOCATOR_CONTAINER, null);
    		if(lentries==null) {
    			lentries="";
    		} else {
    			lentries=',' + lentries; // "" makes a blank default Public Entry
    		}

    		String defaultName = null;
    		String str;
    		int public_port = port;
    		// Note: only one of the ports can be public...  Therefore, only the la
    		for(String le : Split.splitTrim(',', lentries)) {
    			dot_le = le.isEmpty()?"":"."+le;
				str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT+dot_le, null);
				if(str!=null) { // Get Public Port
					public_port = Integer.decode(str);
				}
    		}
    		
    		String public_hostname = hostname;
    		for(String le : Split.splitTrim(',', lentries)) {
    			dot_le = le.isEmpty()?"":"."+le;
				String ph = access.getProperty(Config.AAF_LOCATOR_PUBLIC_HOSTNAME+dot_le,null);
				if( ph != null) {
					public_hostname=ph;
				}
    		}
    		
    		String default_fqdn = access.getProperty(Config.AAF_LOCATOR_FQDN, public_hostname);
    		

    		// Now, loop through by Container
    		for(String le : Split.splitTrim(',', lentries)) {
    			// Add variable entries
    			String names;
    			if(le.length()>0) {
    				dot_le = '.' + le;
    				names = access.getProperty(Config.AAF_LOCATOR_NAMES+dot_le,null);
    				if(names==null) {
    					// Go for Default
    					names = access.getProperty(Config.AAF_LOCATOR_NAMES,"");
    				}
    			} else {
    				dot_le = "";
    				names=access.getProperty(Config.AAF_LOCATOR_NAMES,dot_le);
    			}
    			
    			for(String name : Split.splitTrim(',', names)) {
    				if(defData==null) {
    					defData = locate = new MgmtEndpoint();

    					defaultName = name;
    					version = access.getProperty(Config.AAF_LOCATOR_VERSION, Defaults.AAF_VERSION);
    					locate.setProtocol(access.getProperty(Config.AAF_LOCATOR_PROTOCOL,null));
    					List<String> ls = locate.getSubprotocol();
    					for(String sp : Split.splitTrim(',', access.getProperty(Config.AAF_LOCATOR_SUBPROTOCOL,""))) {
    						ls.add(sp);	
    					}
    					locate.setLatitude(latitude);
    					locate.setLongitude(longitude);

    				} else {
    					locate = copy(defData);
    				}
    				
    				str = access.getProperty(Config.HOSTNAME+dot_le, null);
    				if(str==null) {
    					str = access.getProperty(Config.HOSTNAME, hostname);
    				}
    				locate.setHostname(hostname);
    				
    				ns = access.getProperty(Config.AAF_LOCATOR_NS+dot_le,null);
    				if(ns==null) {
    					ns = access.getProperty(Config.AAF_LOCATOR_NS,"");
    				}
    				switch(ns) {
	    				case Defaults.AAF_NS:
	    					ns = access.getProperty(Config.AAF_ROOT_NS, "");
	    					// Fallthrough on purpose.
    				}

    				String ns_dot;
    				if(ns.isEmpty()) {
    					ns_dot = ns;
    				} else {
    					ns_dot = ns + '.';
    				}

    				String container_id = access.getProperty(Config.AAF_LOCATOR_CONTAINER_ID+dot_le, "");
    				if(!container_id.isEmpty()) {
    					ns_dot = container_id + '.' + ns_dot;
    				}

    				if(!le.isEmpty()) {
    					ns_dot = le + '.' + ns_dot;
    				}

    				if(name.isEmpty()) {
   						locate.setName(ns_dot + defaultName);
    				} else {
    					locate.setName(ns_dot + name);
    				}

    				if(dot_le.isEmpty()) {
    					locate.setHostname(access.getProperty(Config.AAF_LOCATOR_FQDN, default_fqdn));
    				} else {
	    				str =  access.getProperty(Config.AAF_LOCATOR_FQDN+dot_le, null);
	    				if(str==null) {
	    					locate.setHostname(default_fqdn);
	    				} else {
	        				String container_ns = access.getProperty(Config.AAF_LOCATOR_CONTAINER_NS+dot_le, "");
	    					str = str.replace("%CNS", container_ns);
	        				String container = access.getProperty(Config.AAF_LOCATOR_CONTAINER+dot_le, "");
    						str = str.replace("%C", container);
	    					str = str.replace("%NS", ns);
	    					str = str.replace("%N", name);
	    					str = str.replace("%DF", default_fqdn);
	    					str = str.replace("%PH", public_hostname);
	    					locate.setHostname(str);
	    				}
    				}
    				
    				if(le.isEmpty()) {
    					locate.setPort(public_port);
    				} else {
    					locate.setPort(port);
    				}

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
