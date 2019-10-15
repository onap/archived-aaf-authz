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

package org.onap.aaf.auth.batch.helpers.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.auth.batch.helpers.Role;

import junit.framework.Assert;

public class JU_Role {

    Role shortRole;
    Role longRole;
    Set set;

    @Before
    public void setUp() {
        set = new HashSet();
        shortRole = new Role("full");
        longRole = new Role("ns", "name", "description", set);
    }

    @Test
    public void testEncode() {
        Assert.assertEquals("ns|name", longRole.encode());
    }

    @Test
    public void testFullName() {
        Assert.assertEquals("ns.name", longRole.fullName());
        Assert.assertEquals("full", shortRole.fullName());
    
        longRole.fullName("test");
    }

    @Test
    public void testToString() {
        Assert.assertEquals("ns|name", longRole.toString());
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(-2043567518, longRole.hashCode());
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(false, longRole.equals(longRole));
    }

    @Test
    public void testCompareTo() {
        Assert.assertEquals(-14, longRole.compareTo(shortRole));
        Assert.assertEquals(14, shortRole.compareTo(longRole));
    }

    @Test
    public void testStageRemove() {
        longRole.stageRemove(shortRole);
    }

}
