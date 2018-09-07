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

package org.onap.aaf.auth.helpers.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.helpers.Perm;

import junit.framework.Assert;

import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class JU_Perm {
    
    Perm perm;
    Set set;
    
    @Before
    public void setUp() {
        set = new HashSet();
        perm = new Perm("ns","type", "instance", "action","description", set);
    }

    @Test
    public void testFullType() {
        Assert.assertEquals("ns.type", perm.fullType());
    }
    
    @Test
    public void testFullPerm() {
        Assert.assertEquals("ns.type|instance|action", perm.fullPerm());
    }
    
    @Test
    public void testEncode() {
        Assert.assertEquals("ns|type|instance|action", perm.encode());
    }
    
    @Test
    public void testHashCode() {
        Assert.assertEquals(850667666, perm.hashCode());
    }
    
    @Test
    public void testToString() {
        Assert.assertEquals("ns|type|instance|action", perm.toString());
    }
    
    @Test
    public void testEquals() {
        Perm perm1 = new Perm("ns","type", "instance", "action","description", set);
        Assert.assertEquals(false, perm.equals(perm1));
    }
    
    @Test
    public void testCompareTo() {
        Perm perm1 = new Perm("ns","type", "instance", "action","description", set);
        Perm perm2 = new Perm("ns1","type", "instance", "action","description", set);
        
        Assert.assertEquals(0, perm.compareTo(perm1));
        Assert.assertEquals(75, perm.compareTo(perm2));
    }
    
    @Test
    public void testStageRemove() {
        Perm perm1 = new Perm("ns","type", "instance", "action","description", set);
        perm.stageRemove(perm1);
    }

}
