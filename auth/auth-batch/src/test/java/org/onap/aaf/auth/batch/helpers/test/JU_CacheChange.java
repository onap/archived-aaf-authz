/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.batch.helpers.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.helpers.CacheChange;

import junit.framework.Assert;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;

public class JU_CacheChange {

    CacheChange cc;


    @Before
    public void setUp() {
        cc = new CacheChange();
    }

    @Test
    public void testDelayedDelete() {
        cc.delayedDelete(null);
    }

    @Test
    public void testGetRemoved() {
        List list = cc.getRemoved();
        Assert.assertNotNull(list);
    }

    @Test
    public void testResetLocalData() {
        cc.resetLocalData();
    }

    @Test
    public void testCacheSize() {
        int size;
        size = cc.cacheSize();
        Assert.assertEquals(0, size);
    }

    @Test
    public void testContains() {
        boolean containsBools;
        containsBools = cc.contains(null);
        Assert.assertEquals(false, containsBools);
    }

}
