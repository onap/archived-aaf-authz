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
package org.onap.aaf.auth.direct.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.dao.cass.NsSplit;
import org.onap.aaf.auth.dao.cass.PermDAO;
import org.onap.aaf.auth.dao.cass.PermDAO.Data;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.direct.DirectAAFLur;
import org.onap.aaf.auth.direct.DirectAAFLur.PermPermission;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransImpl;
import org.onap.aaf.auth.env.NullTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.lur.LocalPermission;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Trans;

@RunWith(MockitoJUnitRunner.class) 
public class JU_DirectAAFLur {

    @Mock
    AuthzEnv env;
    
    @Mock
    Question question;
    
    @Mock
    Principal bait;
    
    @Mock
    Permission pond;
    
    @Mock
    AuthzTrans trans;
    
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        Mockito.when(env.newTransNoAvg()).thenReturn(trans);
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(trans).error();
    }
    
    public void testFish() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        
        List<PermDAO.Data> rsVal = new ArrayList<PermDAO.Data>();
        Result<List<Data>> rs = new Result<List<Data>>(rsVal,0,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTransNoAvg(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fish(bait, pond);
    }

    @Test
    public void testFishSecondMtd() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        
        List<PermDAO.Data> rsVal = new ArrayList<PermDAO.Data>();
        PermDAO.Data pd = new PermDAO.Data();
        pd.ns = "ns";
        pd.type = "name";
        pd.instance = null;
        rsVal.add(pd);
        pd = new PermDAO.Data();
        pd.ns = "ns";
        pd.type = "name";
        pd.instance = "instance";
        pd.action = null;
        rsVal.add(pd);
        
        pd = new PermDAO.Data();
        pd.ns = "ns";
        pd.type = "name";
        pd.instance = "instance1";
        rsVal.add(pd);
        pd = new PermDAO.Data();
        pd.ns = "ns1";
        pd.type = "name";
        rsVal.add(pd);
        pd = new PermDAO.Data();
        pd.ns = "ns";
        pd.type = "name1";
        rsVal.add(pd);
                
        pd = new PermDAO.Data();
        pd.ns = "ns";
        pd.type = "name";
        pd.instance = "instance";
        pd.action = "action";
        rsVal.add(pd);
        
        pond = new DirectAAFLur.PermPermission("ns", "name", "instance", "action");
        
        Result<List<Data>> rs = new Result<List<Data>>(rsVal,0,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTransNoAvg(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fish(bait, pond);
        
        pond = new AAFPermission("ns", "name", "instance", "action");
        
        Mockito.when(question.getPermsByUser(env.newTransNoAvg(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fish(bait, pond);
        
        rs = new Result<List<Data>>(rsVal,1,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTransNoAvg(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fish(bait, pond);
        rs = new Result<List<Data>>(rsVal,4,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTransNoAvg(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fish(bait, pond);
        rs = new Result<List<Data>>(rsVal,25,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTransNoAvg(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fish(bait, pond);
    }
    
    @Test
    public void testFishAll() {
        Mockito.doReturn(Mockito.mock(LogTarget.class)).when(env).error();
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        
        List<PermDAO.Data> rsVal = new ArrayList<PermDAO.Data>();
        PermDAO.Data pd = new PermDAO.Data();
        pd.ns = "ns";
        pd.type = "name";
        pd.instance = null;
        rsVal.add(pd);
        
        pond = new DirectAAFLur.PermPermission("ns", "name", "instance", "action");
        List<Permission> permissions = new ArrayList<>();
        permissions.add(pond);
        
        Result<List<Data>> rs = new Result<List<Data>>(rsVal,0,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTrans(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fishAll(bait, permissions);
        assertTrue(permissions.size() == 2);
        
        rs = new Result<List<Data>>(rsVal,1,"test",new Object[0]);
        Mockito.when(question.getPermsByUser(env.newTrans(), bait.getName(), false)).thenReturn(rs);
        aafLurObj.fishAll(bait, permissions);
    }
    
    @Test
    public void testDestroy() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        aafLurObj.destroy();
    }
    
    @Test
    public void testHandlesExclusively() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        pond = new DirectAAFLur.PermPermission("ns", "name", "instance", "action");
        assertFalse(aafLurObj.handlesExclusively(pond));
    }
    
    @Test
    public void testToString() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        assertTrue(aafLurObj.toString().contains("DirectAAFLur is enabled"));
    }
    
    @Test
    public void testHandles() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        assertTrue(aafLurObj.handles(null));
    }
    
    @Test
    public void testCreatePerm() {
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        Permission retVal = aafLurObj.createPerm("test");
        assertTrue(retVal instanceof LocalPermission);
        
        NsSplit nss = new NsSplit("test", "test");
        Result<NsSplit> rs = new Result<NsSplit>(nss,0,"test",new Object[0]);
        Mockito.when(question.deriveNsSplit(NullTrans.singleton() , "test")).thenReturn(rs);
        retVal = aafLurObj.createPerm("test|1|2");
        assertTrue(retVal instanceof PermPermission);
        
        rs = new Result<NsSplit>(null,1,"test",new Object[0]);
        Mockito.when(question.deriveNsSplit(NullTrans.singleton() , "test")).thenReturn(rs);
        retVal = aafLurObj.createPerm("test|1|2");
        assertTrue(retVal instanceof LocalPermission);
    }
    
    @Test
    public void testClear() {
        AuthzTransImpl trans =  Mockito.mock(AuthzTransImpl.class);
        Mockito.when(env.newTrans()).thenReturn(trans);
        DirectAAFLur aafLurObj = new DirectAAFLur(env, question);
        StringBuilder sb = new StringBuilder();
        Mockito.when(trans.auditTrail(0, sb)).thenReturn(Mockito.mock(Trans.Metric.class));
        aafLurObj.clear(bait, sb);
    }
    
    @Test
    public void testPermPermission() {
        AuthzTransImpl trans =  Mockito.mock(AuthzTransImpl.class);
        NsSplit nss = new NsSplit("test", "test");
        Result<NsSplit> rs = new Result<NsSplit>(nss,0,"test",new Object[0]);
        Mockito.when(question.deriveNsSplit(trans , "test")).thenReturn(rs);
        PermPermission pp = new PermPermission(trans, question, "test|test|test|test");
        
        assertTrue("test".equalsIgnoreCase(pp.getKey()));
        assertTrue("AAFLUR".equalsIgnoreCase(pp.permType()));
        
        assertFalse(pp.match(null));
        
        pond = new AAFPermission("test.test", "test", "test", "test");
        assertTrue(pp.match(pond));
    }
}
