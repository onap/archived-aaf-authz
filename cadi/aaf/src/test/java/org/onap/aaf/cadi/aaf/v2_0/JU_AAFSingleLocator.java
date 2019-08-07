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
package org.onap.aaf.cadi.aaf.v2_0;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.Locator;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.AAFPermission;


public class JU_AAFSingleLocator {

    @Mock
    AAFCon con;
    
    @Mock
    AbsUserCache<AAFPermission> cache;
    
    @Mock
    PropAccess propaccess;
    

    AAFSingleLocator authnObj;
    
    @Before
    public void setUp() {
        initMocks(this);
        try {
            authnObj = new AAFSingleLocator("http://www.google.com");
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Test
    public void testGetRealm() {
        try {
            URI retVal = authnObj.get(Mockito.mock( Locator.Item.class));
            assertEquals("www.google.com",retVal.getHost());
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testHasItem() {
            boolean retVal = authnObj.hasItems();
            assertTrue(retVal);
    }
    
    @Test
    public void testInvalidate() {
        try {
             authnObj.invalidate(Mockito.mock( Locator.Item.class));
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testBest() {
        try {
            Locator.Item retVal = authnObj.best();
            assertTrue(retVal.toString().contains("org.onap.aaf.cadi.aaf.v2_0.AAFSingleLocator$SingleItem"));
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testFirst() {
        try {
            Locator.Item retVal = authnObj.first();
            assertTrue(retVal.toString().contains("org.onap.aaf.cadi.aaf.v2_0.AAFSingleLocator$SingleItem"));
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Test
    public void testNext() {
        try {
            Locator.Item retVal = authnObj.next(Mockito.mock( Locator.Item.class));
            assertNull(retVal);
        } catch (LocatorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testRefres() {
            boolean retVal = authnObj.refresh();
            assertFalse(retVal);
    }
    
    @Test
    public void testdestroy() {
        authnObj.destroy();
    }
    
    
}
