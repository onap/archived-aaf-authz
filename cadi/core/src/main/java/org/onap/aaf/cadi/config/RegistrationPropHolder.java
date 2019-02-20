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

package org.onap.aaf.cadi.config;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.util.Split;

public class RegistrationPropHolder {
	private final String PUBLIC_NAME="%NS.%N";
	private final Access access;
	public String hostname;
	private int port;
	public String public_hostname;
	private Integer public_port;
	public Float latitude;
	public Float longitude;
	public final String default_fqdn;
	public final String default_container_ns;
	public final String default_name;
	public final String lentries;
	public final String lcontainer;
	public final String default_container;

	public RegistrationPropHolder(final Access access, final int port) throws UnknownHostException, CadiException {
		this.access = access;
		StringBuilder errs = new StringBuilder();
		String str;
		this.port = port;

		lentries=access.getProperty(Config.AAF_LOCATOR_ENTRIES,"");
		
		default_container = access.getProperty(Config.AAF_LOCATOR_CONTAINER, "");
		if(!default_container.isEmpty()) {
			lcontainer=',' + default_container; // "" makes a blank default Public Entry
			str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT+'.'+default_container, null);
			if(str==null) {
				str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT, null);
			}
		} else {
			lcontainer=default_container;
			str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_PORT, null);
		}
		if(str!=null) {
			public_port=Integer.decode(str);
		}
		
		hostname = access.getProperty(Config.HOSTNAME, null);
		if (hostname==null) {
			hostname = Inet4Address.getLocalHost().getHostName();
		}
		if (hostname==null) {
			mustBeDefined(errs,Config.HOSTNAME);
		}

		public_hostname = access.getProperty(Config.AAF_LOCATOR_PUBLIC_HOSTNAME, hostname);
				
		default_name = access.getProperty(Config.AAF_LOCATOR_NAME, "%CNS.%NS.%N");
		
		latitude=null;
		String slatitude = access.getProperty(Config.CADI_LATITUDE, null);
		if(slatitude == null) {
			mustBeDefined(errs,Config.CADI_LATITUDE);
		} else {
			latitude = Float.parseFloat(slatitude);
		}

		longitude=null;
		String slongitude = access.getProperty(Config.CADI_LONGITUDE, null);
		if(slongitude == null) {
			mustBeDefined(errs,Config.CADI_LONGITUDE);
		} else {
			longitude = Float.parseFloat(slongitude);
		}

		String dot_le;
		// Note: only one of the ports can be public...  Therefore, only the last
		for(String le : Split.splitTrim(',', lcontainer)) {
			dot_le = le.isEmpty()?"":"."+le;
			str = access.getProperty(Config.AAF_LOCATOR_PUBLIC_HOSTNAME+dot_le,null);
			if( str != null) {
				public_hostname=str;
			}
		}
		
		default_fqdn = access.getProperty(Config.AAF_LOCATOR_FQDN, hostname);
		default_container_ns = access.getProperty(Config.AAF_LOCATOR_CONTAINER_NS,"");
		
		if(errs.length()>0) {
			throw new CadiException(errs.toString());
		}
	}

	private void mustBeDefined(StringBuilder errs, String propname) {
		errs.append('\n');
		errs.append(propname);
		errs.append(" must be defined.");
		
	}

	public String getEntryFQDN(final String entry, final String dot_le) {
		String str;
		if(public_hostname!=null && dot_le.isEmpty()) {
			str = public_hostname;
		} else {
			str = access.getProperty(Config.AAF_LOCATOR_FQDN+dot_le, default_fqdn);
		}
		return replacements(str,entry,dot_le);
	}
	
	public String getEntryName(final String entry, final String dot_le) {
		String str;
		if(dot_le.isEmpty()) {
			str = PUBLIC_NAME;
		} else {
			str = access.getProperty(Config.AAF_LOCATOR_NAME+dot_le, default_name);
		}
		return replacements(str,entry,dot_le);
	}
	
	
	private String getNS(String dot_le) {
		String ns;
		ns = access.getProperty(Config.AAF_LOCATOR_NS+dot_le,null);
		if(ns==null) {
			ns = access.getProperty(Config.AAF_ROOT_NS, "");
		}
		return ns;
	}

	
	public String replacements(String source, final String name, final String _dot_le) {
		if(source == null) {
			return "";
		} else if(source.isEmpty()) {
			return source;
		}
		
		String dot_le;
		if(_dot_le==null) {
			dot_le = default_container.isEmpty()?"":'.'+default_container;
		} else {
			dot_le = _dot_le;
		}

        String aaf_locator_host = access.getProperty(Config.AAF_LOCATE_URL+dot_le,null);
        if(aaf_locator_host==null) {
        	aaf_locator_host = access.getProperty(Config.AAF_LOCATE_URL,null);
        }

        String str;
        if(aaf_locator_host!=null) {
			if("https://AAF_LOCATE_URL".equals(source)) {
				source = aaf_locator_host;
			} else {
		        str = aaf_locator_host;
				if(source.indexOf(Config.AAF_LOCATE_URL_TAG)>=0) {
					if(!str.endsWith("/")) {
						str+='/';
					}
					if(!str.endsWith("/locate/")) {
						str+="locate/";
					}
					source = source.replace("https://AAF_LOCATE_URL/", str);
				}
			}
        }

		int atC = source.indexOf("%C"); 
		if(atC>=0) {
			// aaf_locator_container_ns
			str = access.getProperty(Config.AAF_LOCATOR_CONTAINER_NS+dot_le, default_container_ns);
			if(str.isEmpty()) {
				source = source.replace("%CNS"+'.', str);
			}
			source = source.replace("%CNS", str);
			
			str = access.getProperty(Config.AAF_LOCATOR_CONTAINER+dot_le,default_container);
			if(str.isEmpty()) {
				source = source.replace("%C"+'.', str);
			}
			source = source.replace("%C", str);
		}
		
		if(source.indexOf("%NS")>=0) {
			str = getNS(dot_le);
			if(str==null || str.isEmpty()) {
				source = source.replace("%NS"+'.', str);
			}
			source = source.replace("%NS", str);
		}

		// aaf_root_ns
		if(source.indexOf("AAF_NS")>=0) {
			str = access.getProperty(Config.AAF_ROOT_NS, Config.AAF_ROOT_NS_DEF);
			String temp = source.replace("%AAF_NS", str);
			if(temp.equals(source)) { // intended
				source = source.replace("AAF_NS", str); // Backward Compatibility
			} else {
				source = temp;
			}
		}

		
		if(source.indexOf('%')>=0) {
            // These shouldn't be expected to have dot elements
            if(name!=null) {
              source = source.replace("%N", name);
            }
            if(default_fqdn!=null) {
              source = source.replace("%DF", default_fqdn);
            }
            if(public_hostname!=null) {
              source = source.replace("%PH", public_hostname);
            }
		}
		return source;
	}
	
	public int getEntryPort(final String dot_le) {
		return public_port!=null && dot_le.isEmpty()?
				public_port:
				port;
	}

	public Access access() {
		return access;
	}
}