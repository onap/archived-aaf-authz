/*
 * ============LICENSE_START==========================================
 * ===================================================================
 * Copyright © 2018 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 */

package org.onap.aaf.auth.cmd.test.ns;

import static org.junit.Assert.*;

import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;

import org.onap.aaf.auth.cmd.ns.List;
import org.onap.aaf.auth.cmd.ns.NS;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.config.SecurityInfoC;
import org.onap.aaf.cadi.http.HMangr;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Nss;
import aaf.v2_0.Roles;
import aaf.v2_0.Users.User;
import junit.framework.Assert;

import org.onap.aaf.auth.cmd.AAFcli;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import org.junit.Test;

public class JU_List {
    
    List list;
    AAFcli aafcli;
    User user;
    
    private class NssStub extends Nss {
        public void addNs(Nss.Ns ns) {    
            if (this.ns == null) {
                this.ns = new ArrayList<>();
            }
            this.ns.add(ns);
        }
        
        private class NsStub extends Ns{
            public void addAttrib(Nss.Ns.Attrib attrib) {
                if ( this.attrib == null) {
                    this.attrib = new ArrayList<>();
                }
                this.attrib.add(attrib);
            }
            
            public void addResponsible(String str) {
                if (this.responsible == null) {
                    this.responsible = new ArrayList<>();
                }
                this.responsible.add(str);
            }
            
            public void addAdmin(String str) {
                if (this.admin == null) {
                    this.admin = new ArrayList<>();
                }
                this.admin.add(str);
            }
        }
        
        
        
        
    }
    

    @Before
    public void setUp() throws APIException, LocatorException, CadiException {
        PropAccess prop = new PropAccess();
        AuthzEnv aEnv = new AuthzEnv();
        Writer wtr = mock(Writer.class);
        Locator loc = mock(Locator.class);
        HMangr hman = new HMangr(aEnv, loc);        
        aafcli = new AAFcli(prop, aEnv, wtr, hman, null, null);
        user = new User();
        NS ns = new NS(aafcli);
        
        list = new List(ns);
    }
    
    @Test
    public void testReport() throws Exception {
        Future<Nss> fu = mock(Future.class);
        NssStub nssStub = new NssStub();
        NssStub.NsStub nsStub = nssStub.new NsStub();
        Nss.Ns.Attrib attrib = mock(Nss.Ns.Attrib.class);
        when(attrib.getKey()).thenReturn("key");
        when(attrib.getValue()).thenReturn("value");
        nsStub.addAttrib(attrib);
        nsStub.addResponsible("test");
        nsStub.addAdmin("admin");
        nssStub.addNs(nsStub);
        fu.value = nssStub;
        aafcli.eval("DETAILS @[ 123");
        
        list.report(fu, "test");
    }
    
    @Test
    public void testGetType() {
        Assert.assertEquals("n/a", list.getType(user));
        user.setType(1);
        Assert.assertEquals("U/P", list.getType(user));
        user.setType(2);
        Assert.assertEquals("U/P2", list.getType(user));
        user.setType(10);
        Assert.assertEquals("FQI", list.getType(user));
        user.setType(200);
        Assert.assertEquals("x509", list.getType(user));
    }
    
}
