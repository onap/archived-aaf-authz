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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.helpers.History;

import junit.framework.Assert;

import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Test;

public class JU_History {
    
    History history;
    History history1;
    
    @Before
    public void setUp() {
        UUID id = new UUID(0, 0);
        history = new History(id, "action", "memo", "subject", "target", "user", 5);
        history1 = new History(id, "action", "memo", "reconstruct", "subject", "target", "user", 5);
    }

    @Test
    public void testToString() {
        String result = "00000000-0000-0000-0000-000000000000 5 user, target, action, subject, memo";
        Assert.assertEquals(result, history.toString());
    }
    
    @Test
    public void testHashCode() {
        Assert.assertEquals(0, history.hashCode());
    }
    
    @Test
    public void testEquals() {
        Assert.assertFalse(history.equals(history1));
    }
    
}
