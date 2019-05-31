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

package org.onap.aaf.cadi.register.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.junit.Test;
import org.mockito.internal.configuration.DefaultInjectionEngine;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.register.RegistrationCreator;

import junit.framework.Assert;
import locate.v1_0.MgmtEndpoint;

public class JU_RegistrationCreator {

	private static final String DOT_OOM = ".oom";

	@Test
	public void test() {
		PropAccess pa = new PropAccess();
		pa.setProperty(Config.CADI_LATITUDE, "32.7");
		pa.setProperty(Config.CADI_LONGITUDE, "-72.0");
		pa.setProperty(Config.AAF_LOCATOR_NAME, "");
		
		try {
			String hostname = Inet4Address.getLocalHost().getHostName();
			String entry = "";
			RegistrationCreator rc = new RegistrationCreator(pa);
			int port = 999;
			for(MgmtEndpoint me : rc.create(port).getMgmtEndpoint()) {
				assertEquals(hostname,me.getHostname());
				assertEquals(port,me.getPort());
				assertEquals(pa.getProperty(Config.CADI_LATITUDE),Float.toString(me.getLatitude()));
				assertEquals(pa.getProperty(Config.CADI_LONGITUDE),Float.toString(me.getLongitude()));
				assertEquals(2,me.getMajor());
				assertEquals(1,me.getMinor());
				assertEquals(0,me.getPatch());
				assertEquals(0,me.getPkg());
				assertEquals(entry,me.getName());
				assertEquals("https",me.getProtocol());
				assertEquals(0,me.getSpecialPorts().size());
			}

			String protocol = "http";
			pa.setProperty(Config.AAF_LOCATOR_PROTOCOL, protocol);
			rc = new RegistrationCreator(pa);
			for(MgmtEndpoint me : rc.create(port).getMgmtEndpoint()) {
				assertEquals(hostname,me.getHostname());
				assertEquals(port,me.getPort());
				assertEquals(pa.getProperty(Config.CADI_LATITUDE),Float.toString(me.getLatitude()));
				assertEquals(pa.getProperty(Config.CADI_LONGITUDE),Float.toString(me.getLongitude()));
				assertEquals(2,me.getMajor());
				assertEquals(1,me.getMinor());
				assertEquals(0,me.getPatch());
				assertEquals(0,me.getPkg());
				assertEquals("",me.getName());
				assertEquals(protocol,me.getProtocol());
				assertEquals(0,me.getSpecialPorts().size());
			}
			
			pa.setProperty(Config.AAF_LOCATOR_ENTRIES, "service");
			rc = new RegistrationCreator(pa);
			for(MgmtEndpoint me : rc.create(port).getMgmtEndpoint()) {
				switch(me.getName()) {
					case "":
						assertEquals(hostname,me.getHostname());
						assertEquals(port,me.getPort());
						break;
					case "service":
						assertEquals(hostname,me.getHostname());
						assertEquals(port,me.getPort());
						break;
					default:
						fail("unknown Locator Entry");
				}
				assertEquals(pa.getProperty(Config.CADI_LATITUDE),Float.toString(me.getLatitude()));
				assertEquals(pa.getProperty(Config.CADI_LONGITUDE),Float.toString(me.getLongitude()));
				assertEquals(2,me.getMajor());
				assertEquals(1,me.getMinor());
				assertEquals(0,me.getPatch());
				assertEquals(0,me.getPkg());
				assertEquals(protocol,me.getProtocol());
				assertEquals(0,me.getSpecialPorts().size());
			}

			entry = "service";
			pa.setProperty(Config.AAF_LOCATOR_ENTRIES, entry);
			rc = new RegistrationCreator(pa);
			for(MgmtEndpoint me : rc.create(port).getMgmtEndpoint()) {
				switch(me.getName()) {
					case "":
						assertEquals(hostname,me.getHostname());
						assertEquals(port,me.getPort());
						break;
					case "service":
						assertEquals(hostname,me.getHostname());
						assertEquals(port,me.getPort());
						break;
					default:
						fail("unknown Locator Entry");
				}
				assertEquals(pa.getProperty(Config.CADI_LATITUDE),Float.toString(me.getLatitude()));
				assertEquals(pa.getProperty(Config.CADI_LONGITUDE),Float.toString(me.getLongitude()));
				assertEquals(2,me.getMajor());
				assertEquals(1,me.getMinor());
				assertEquals(0,me.getPatch());
				assertEquals(0,me.getPkg());
				assertEquals(protocol,me.getProtocol());
				assertEquals(0,me.getSpecialPorts().size());
			}

			pa.setProperty(Config.AAF_LOCATOR_CONTAINER, "oom");
			pa.setProperty(Config.AAF_ROOT_NS, Config.AAF_ROOT_NS_DEF);
			pa.setProperty(Config.AAF_LOCATOR_NAME,"%NS.%N");
			pa.setProperty(Config.AAF_LOCATOR_NAME+DOT_OOM,"%CNS.%NS.%N");
			pa.setProperty(Config.AAF_LOCATOR_CONTAINER_NS+DOT_OOM, "onap");
			String k8s_public_hostname="k8s.public.com";
			int public_port = 30001;
			
			pa.setProperty(Config.AAF_LOCATOR_PUBLIC_FQDN,k8s_public_hostname);
			pa.setProperty(Config.AAF_LOCATOR_PUBLIC_PORT+DOT_OOM,Integer.toString(public_port));
			pa.setProperty(Config.AAF_LOCATOR_APP_NS, Config.AAF_ROOT_NS_DEF);
			rc = new RegistrationCreator(pa);
			for(MgmtEndpoint me : rc.create(port).getMgmtEndpoint()) {
				switch(me.getName()) {
					case "org.osaaf.aaf.service":
						assertEquals(k8s_public_hostname,me.getHostname());
						assertEquals(public_port,me.getPort());
						break;
					case "onap.org.osaaf.aaf.service":
						assertEquals(hostname,me.getHostname());
						assertEquals(port,me.getPort());
						break;
					default:
						fail("unknown Entry Name, " + me.getName());
				}
				assertEquals(pa.getProperty(Config.CADI_LATITUDE),Float.toString(me.getLatitude()));
				assertEquals(pa.getProperty(Config.CADI_LONGITUDE),Float.toString(me.getLongitude()));
				assertEquals(2,me.getMajor());
				assertEquals(1,me.getMinor());
				assertEquals(0,me.getPatch());
				assertEquals(0,me.getPkg());
				assertEquals(protocol,me.getProtocol());
				assertEquals(0,me.getSpecialPorts().size());
			}


		} catch (CadiException | UnknownHostException e) {
			Assert.fail(e.getMessage());
		}
		

	}

}
