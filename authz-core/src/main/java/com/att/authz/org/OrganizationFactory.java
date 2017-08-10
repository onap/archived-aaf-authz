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
package com.att.authz.org;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.inno.env.APIException;
import com.att.inno.env.Slot;

/**
 * Organization Plugin Mechanism
 * 
 * Define a NameSpace for the company (i.e. com.att), and put in Properties as 
 * "Organization.[your NS" and assign the supporting Class.  
 * 
 * Example:
 * Organization.com.att=com.att.authz.org.att.ATT
 *
 *
 */
public class OrganizationFactory {
	public static final String ORG_SLOT = "ORG_SLOT";
	private static Organization defaultOrg = null;
	private static Map<String,Organization> orgs = new ConcurrentHashMap<String,Organization>();
	private static Slot orgSlot;
	
	public static void setDefaultOrg(AuthzEnv env, String orgClass) throws APIException {
		orgSlot = env.slot(ORG_SLOT);
		try {
			@SuppressWarnings("unchecked")
			Class<Organization> cls = (Class<Organization>) Class.forName(orgClass);
			Constructor<Organization> cnst = cls.getConstructor(AuthzEnv.class);
			defaultOrg = cnst.newInstance(env);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | 
				InstantiationException | IllegalAccessException | IllegalArgumentException | 
				InvocationTargetException e) {
			throw new APIException(e);
		}
	}
	
	public static Organization obtain(AuthzEnv env,String orgNS) throws OrganizationException {
		int at = orgNS.indexOf('@');
		if(at<0) {
			if(!orgNS.startsWith("com.")) {
				int dot1;
				if((dot1 = orgNS.lastIndexOf('.'))>-1) {
					int dot2;
					StringBuilder sb = new StringBuilder();
					if((dot2 = orgNS.lastIndexOf('.',dot1-1))>-1) {
						sb.append(orgNS,dot1+1,orgNS.length());
						sb.append('.');
						sb.append(orgNS,dot2+1,dot1);
					} else {
						sb.append(orgNS,dot1+1,orgNS.length());
						sb.append('.');
						sb.append(orgNS,at+1,dot1);
					}
					orgNS=sb.toString();
				}
			}
		} else {
			// Only use two places (Enterprise) of domain
			int dot;
			if((dot= orgNS.lastIndexOf('.'))>-1) {
				StringBuilder sb = new StringBuilder();
				int dot2;
				if((dot2 = orgNS.lastIndexOf('.',dot-1))>-1) {
					sb.append(orgNS.substring(dot+1));
					sb.append(orgNS.subSequence(dot2, dot));
					orgNS = sb.toString();
				} else {
					sb.append(orgNS.substring(dot+1));
					sb.append('.');
					sb.append(orgNS.subSequence(at+1, dot));
					orgNS = sb.toString();
				}
			}
		}
		Organization org = orgs.get(orgNS);
		if(org == null) {
			String orgClass = env.getProperty("Organization."+orgNS);
			if(orgClass == null) {
				env.warn().log("There is no Organization." + orgNS + " property");
			} else {
				for(Organization o : orgs.values()) {
					if(orgClass.equals(o.getClass().getName())) {
						org = o;
					}
				}
				if(org==null) {
					try {
						@SuppressWarnings("unchecked")
						Class<Organization> cls = (Class<Organization>) Class.forName(orgClass);
						Constructor<Organization> cnst = cls.getConstructor(AuthzEnv.class);
						org = cnst.newInstance(env);
					} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | 
							InstantiationException | IllegalAccessException | IllegalArgumentException | 
							InvocationTargetException e) {
						throw new OrganizationException(e);
					}
				}
				orgs.put(orgNS, org);
			}
			if(org==null && defaultOrg!=null) {
				org=defaultOrg;
				orgs.put(orgNS, org);
			}
		}
		
		return org;
	}

	public static void set(AuthzTrans trans, String orgNS) throws OrganizationException {
		Organization org = obtain(trans.env(),orgNS);
		trans.put(orgSlot, org);
	}
	
	public static Organization get(AuthzTrans trans) {
		return trans.get(orgSlot,defaultOrg);
	}

}
