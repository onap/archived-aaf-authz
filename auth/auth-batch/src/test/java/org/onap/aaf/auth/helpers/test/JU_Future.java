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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.helpers.Future;

import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

public class JU_Future {
    
    Future future;
    Date start;
    Date expires;
    ByteBuffer bBuff;
    
    @Before
    public void setUp() {
        UUID id = new UUID(0, 0);
        start = new Date();
        expires = new Date();
        future = new Future(id, "Re-Validate Ownership for AAF Namespace '\'test\'test","target",start, expires, bBuff);
    }

    @Test
    public void testId() {
        Assert.assertTrue(future.id() instanceof UUID);
    }
    
    @Test
    public void testMemo() {
        Assert.assertEquals("Re-Validate Ownership for AAF Namespace '\'test\'test", future.memo());
    }
    
    @Test
    public void testStart() {
        Assert.assertTrue(future.start() instanceof Date);
    }
    
    @Test
    public void testExpires() {
        Assert.assertTrue(future.expires() instanceof Date);
    }
    
    @Test
    public void testTarget() {
        Assert.assertEquals("target",future.target());
    }
    
    @Test
    public void testExpunge() {
        future.expunge();
    }
    
    @Test
    public void testCompareTo() {
        future.compareTo(null);
        future.compareTo(future);
    }
    
    @Test
    public void testResetLocalData() {
        future.resetLocalData();
    }
    
    @Test
    public void testSizeForDeletion() {
        Assert.assertEquals(0, future.sizeForDeletion());
    }
    
    @Test
    public void testPendingDelete() {
        Assert.assertEquals(false, future.pendingDelete(future));
    }
    

}
