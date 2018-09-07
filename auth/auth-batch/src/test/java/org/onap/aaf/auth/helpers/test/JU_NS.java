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
import org.onap.aaf.auth.helpers.NS;
import org.onap.aaf.auth.helpers.NS.NSSplit;

import junit.framework.Assert;

import static org.mockito.Mockito.*;
import org.junit.Test;

public class JU_NS {
    
    NS ns;
    NSSplit nSSplit;
    
    @Before
    public void setUp() {
        ns = new NS("name", "description", "parent", 1, 1);
        nSSplit = new NSSplit("string",1);
    }

    @Test
    public void testToString() {
        Assert.assertEquals("name", ns.toString());
    }
    
    @Test
    public void testHashCode() {
        Assert.assertEquals(3373707, ns.hashCode());
    }
    
    @Test
    public void testEquals() {
        Assert.assertEquals(true, ns.equals("name"));
        Assert.assertEquals(false, ns.equals("name1"));
    }
    
    @Test
    public void testCompareTo() {
        NS nsValid = new NS("name", "description", "parent", 1, 1);
        Assert.assertEquals(0, ns.compareTo(nsValid));
        
        NS nsInvalid = new NS("name1", "description", "parent", 1, 1);
        Assert.assertEquals(-1, ns.compareTo(nsInvalid));
    }
    
    @Test
    public void testDeriveParent() {
        ns.deriveParent("d.ot.te.d");
    }

}
