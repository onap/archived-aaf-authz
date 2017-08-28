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
package org.onap.aaf.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.authz.env.AuthzEnv;

import org.onap.aaf.cadi.routing.GreatCircle;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.util.Split;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;

public class CassAccess {
	public static final String KEYSPACE = "authz";
	public static final String CASSANDRA_CLUSTERS = "cassandra.clusters";
	public static final String CASSANDRA_CLUSTERS_PORT = "cassandra.clusters.port";
	public static final String CASSANDRA_CLUSTERS_USER_NAME = "cassandra.clusters.user";
	public static final String CASSANDRA_CLUSTERS_PASSWORD = "cassandra.clusters.password";
	public static final String CASSANDRA_RESET_EXCEPTIONS = "cassandra.reset.exceptions";
	public static final String LATITUDE = "LATITUDE";
	public static final String LONGITUDE = "LONGITUDE";
	private static final List<Resettable> resetExceptions = new ArrayList<Resettable>();
	public static final String ERR_ACCESS_MSG = "Accessing Backend";
	private static Builder cb = null;

	/**
	 * To create DCAwareRoundRobing Policy:
	 * 	 Need Properties
	 * 		LATITUDE (or AFT_LATITUDE)
	 * 		LONGITUDE (or AFT_LONGITUDE)
	 * 		CASSANDRA CLUSTERS with additional information:
	 * 			machine:DC:lat:long,machine:DC:lat:long
	 * @param env
	 * @param prefix
	 * @return
	 * @throws APIException
	 * @throws IOException
	 */

	@SuppressWarnings("deprecation")
	public static synchronized Cluster cluster(Env env, String prefix) throws APIException, IOException {
		if(cb == null) {
			String pre;
			if(prefix==null) {
				pre="";
			} else {
				env.info().log("Cassandra Connection for ",prefix);
				pre = prefix+'.';
			}
			cb = Cluster.builder();
			String str = env.getProperty(pre+CASSANDRA_CLUSTERS_PORT,"9042");
			if(str!=null) {
				env.init().log("Cass Port = ",str );
				cb.withPort(Integer.parseInt(str));
			}
			str = env.getProperty(pre+CASSANDRA_CLUSTERS_USER_NAME,null);
			if(str!=null) {
				env.init().log("Cass User = ",str );
				String epass = env.getProperty(pre + CASSANDRA_CLUSTERS_PASSWORD,null);
				if(epass==null) {
					throw new APIException("No Password configured for " + str);
				}
				//TODO Figure out way to ensure Decryptor setting in AuthzEnv
				if(env instanceof AuthzEnv) {
					cb.withCredentials(str,((AuthzEnv)env).decrypt(epass,true));
				} else {
					cb.withCredentials(str, env.decryptor().decrypt(epass));
				}
			}
	
			str = env.getProperty(pre+CASSANDRA_RESET_EXCEPTIONS,null);
			if(str!=null) {
				env.init().log("Cass ResetExceptions = ",str );
				for(String ex : Split.split(',', str)) {
					resetExceptions.add(new Resettable(env,ex));
				}
			}
	
			str = env.getProperty(LATITUDE,env.getProperty("AFT_LATITUDE",null));
			Double lat = str!=null?Double.parseDouble(str):null;
			str = env.getProperty(LONGITUDE,env.getProperty("AFT_LONGITUDE",null));
			Double lon = str!=null?Double.parseDouble(str):null;
			if(lat == null || lon == null) {
				throw new APIException("LATITUDE(or AFT_LATITUDE) and/or LONGITUDE(or AFT_LATITUDE) are not set");
			}
			
			env.init().printf("Service Latitude,Longitude = %f,%f",lat,lon);
			
			str = env.getProperty(pre+CASSANDRA_CLUSTERS,"localhost");
			env.init().log("Cass Clusters = ",str );
			String[] machs = Split.split(',', str);
			String[] cpoints = new String[machs.length];
			String bestDC = null;
			int numInBestDC = 1;
			double mlat, mlon,temp,distance = -1.0;
			for(int i=0;i<machs.length;++i) {
				String[] minfo = Split.split(':',machs[i]);
				if(minfo.length>0) {
					cpoints[i]=minfo[0];
				}
			
				// Calc closest DC with Great Circle
				if(minfo.length>3) {
					mlat = Double.parseDouble(minfo[2]);
					mlon = Double.parseDouble(minfo[3]);
					if((temp=GreatCircle.calc(lat, lon, mlat, mlon)) > distance) {
						distance = temp;
						if(bestDC!=null && bestDC.equals(minfo[1])) {
							++numInBestDC;
						} else {
							bestDC = minfo[1];
							numInBestDC = 1;
						}
					} else {
						if(bestDC!=null && bestDC.equals(minfo[1])) {
							++numInBestDC;
						}
					}
				}
			}
			
			cb.addContactPoints(cpoints);
			
			if(bestDC!=null) {
				// 8/26/2016 Management has determined that Accuracy is preferred over speed in bad situations
				// Local DC Aware Load Balancing appears to have the highest normal performance, with the best
				// Degraded Accuracy
				cb.withLoadBalancingPolicy(new DCAwareRoundRobinPolicy(
						bestDC, numInBestDC, true /*allow LocalDC to look at other DCs for LOCAL_QUORUM */));
				env.init().printf("Cassandra configured for DCAwareRoundRobinPolicy at %s with emergency remote of up to %d node(s)"
					,bestDC, numInBestDC);
			} else {
				env.init().printf("Cassandra is using Default Policy, which is not DC aware");
			}
		}
		return cb.build();
	}
	
	private static class Resettable {
		private Class<? extends Exception> cls;
		private List<String> messages;
		
		@SuppressWarnings("unchecked")
		public Resettable(Env env, String propData) throws APIException {
			if(propData!=null && propData.length()>1) {
				String[] split = Split.split(':', propData);
				if(split.length>0) {
					try {
						cls = (Class<? extends Exception>)Class.forName(split[0]);
					} catch (ClassNotFoundException e) {
						throw new APIException("Declared Cassandra Reset Exception, " + propData + ", cannot be ClassLoaded");
					}
				}
				if(split.length>1) {
					messages=new ArrayList<String>();
					for(int i=1;i<split.length;++i) {
						String str = split[i];
						int start = str.startsWith("\"")?1:0;
						int end = str.length()-(str.endsWith("\"")?1:0);
						messages.add(split[i].substring(start, end));
					}
				} else {
					messages = null;
				}
			}
		}
		
		public boolean matches(Exception ex) {
			if(ex.getClass().equals(cls)) {
				if(messages!=null) {
					String msg = ex.getMessage();
					for(String m : messages) {
						if(msg.contains(m)) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}
	
	public static final boolean isResetException(Exception e) {
		if(e==null) {
			return true;
		}
		for(Resettable re : resetExceptions) {
			if(re.matches(e)) {
				return true;
			}
		}
		return false;
	}
}
