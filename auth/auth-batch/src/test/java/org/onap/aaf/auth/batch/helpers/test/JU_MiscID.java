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
import org.onap.aaf.auth.batch.BatchException;
import org.onap.aaf.auth.batch.helpers.MiscID;

import com.datastax.driver.core.Row;

import junit.framework.Assert;

import static org.mockito.Mockito.*;
import org.junit.Test;

public class JU_MiscID {
    
    MiscID miscId;
    
    @Before
    public void setUp() {
        miscId = new MiscID();
    }
    
    @Test
    public void testRowSet() {
        Row row = mock(Row.class);
        miscId.set(row);
    }
    
    @Test
    public void testStringSet() throws BatchException {
        String[] strArr = {"id", "sponsor", "created", "renewal"};
        miscId.set(strArr);
    }
    
    @Test
    public void testHashcode() throws BatchException {
        String[] strArr = {"id", "sponsor", "created", "renewal"};
        miscId.set(strArr);
        Assert.assertEquals(3355, miscId.hashCode());
    }
    
    @Test
    public void testEquals() throws BatchException {
        String[] strArr = {"id", "sponsor", "created", "renewal"};
        miscId.set(strArr);
        Assert.assertFalse(miscId.equals("id"));
        Assert.assertTrue(miscId.equals(miscId));
    }
    
    @Test
    public void testInsertStmt() throws IllegalArgumentException, IllegalAccessException {
        String expected = "INSERT INTO authz.miscid (id,created,sponsor,renewal) VALUES ('null','null','null','null')";
        String result = miscId.insertStmt().toString();
        Assert.assertEquals(expected, result);
    }
    
    @Test
    public void testUpdateStmt() throws IllegalArgumentException, IllegalAccessException, BatchException {
        String expected = "UPDATE authz.miscid SET sponser='sponsor1',created='created1',renewal='renewal1' WHERE id='id'";
        String[] strArr = {"id", "sponsor", "created", "renewal"};
        miscId.set(strArr);
        MiscID miscId1 = new MiscID();
        String[] strArr1 = {"id", "sponsor1", "created1", "renewal1"};
        miscId1.set(strArr1);        
        StringBuilder result = miscId.updateStmt(miscId1);

        Assert.assertEquals(expected, result.toString());
    }


}
