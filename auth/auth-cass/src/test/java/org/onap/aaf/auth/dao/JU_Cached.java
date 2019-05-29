/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.dao;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.*;
import org.mockito.*;
// import org.junit.runner.RunWith;
// import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;

import org.onap.aaf.auth.cache.Cache;
import org.onap.aaf.auth.cache.Cache.Dated;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.Cached;
import org.onap.aaf.auth.dao.Cached.Getter;
import org.onap.aaf.auth.dao.JU_Cached.DataStub;
import org.onap.aaf.auth.dao.cass.CacheableData;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.Trans;

// @RunWith(PowerMockRunner.class)
public class JU_Cached {

    @Mock
    CIDAO<Trans> ciDaoMock;

    @Mock
    AuthzEnv authzEnvMock;

    @Mock
    CIDAO<AuthzTrans> cidaoATMock;
    
    String name = "nameString";

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testCachedIdx(){
        Cached<Trans, DataStub> cached = new Cached<Trans, DataStub>(ciDaoMock, name, 1, 30000L);
        assertThat(cached.cacheIdx("1234567890"), is(0));
    }
    
    @Test
    public void testInvalidate(){
        Cached<Trans, DataStub> cached = new Cached<Trans, DataStub>(ciDaoMock, name, 5, 30000L);
        cached.add("test", new ArrayList<>());
        cached.invalidate("test");
        cached.invalidate("test1");
    }

/*
 * DO NOT ATTEMPT TO TEST Timer Threads in JUNIT!!!!!
    @SuppressWarnings("static-access")
    @Test
    public void testStopTimer(){
        Cached<Trans, DataStub> cached = new Cached<Trans, DataStub>(ciDaoMock, name, 1, 30000L);
        cached.stopTimer();
        assertTrue(true);
    }

    @SuppressWarnings("static-access")
    @Test
    public void testStartRefresh(){
        Cached<Trans, DataStub> cached = new Cached<Trans, DataStub>(ciDaoMock, name, 1, 30000L);
        cached.startRefresh(authzEnvMock, cidaoATMock);
        assertTrue(true);
    }
*/
//    @Mock
//    Trans transMock;
//    @Mock
//    Getter<DAO> getterMock;
//    
//    @Test
//    public void testGet(){
//        cached.get(transMock, name, getterMock);
//        fail("not implemented");
//    }
//    
//    @SuppressWarnings("unchecked")
//    public Result<List<DATA>> get(TRANS trans, String key, Getter<DATA> getter) {
//        List<DATA> ld = null;
//        Result<List<DATA>> rld = null;
//        
//        int cacheIdx = cacheIdx(key);
//        Map<String, Dated> map = ((Map<String,Dated>)cache[cacheIdx]);
//        
//        // Check for saved element in cache
//        Dated cached = map.get(key);
//        // Note: These Segment Timestamps are kept up to date with DB
//        Date dbStamp = info.get(trans, name,cacheIdx);
//        
//        // Check for cache Entry and whether it is still good (a good Cache Entry is same or after DBEntry, so we use "before" syntax)
//        if (cached!=null && dbStamp.before(cached.timestamp)) {
//            ld = (List<DATA>)cached.data;
//            rld = Result.ok(ld);
//        } else {
//            rld = getter.get();
//            if (rld.isOK()) { // only store valid lists
//                map.put(key, new Dated(rld.value));  // successful item found gets put in cache
////            } else if (rld.status == Result.ERR_Backend){
////                map.remove(key);
//            }
//        }
//        return rld;
//    }

    class DataStub extends CacheableData {
        @Override public int[] invalidate(Cached<?, ?> cache) { return null; }
    }
}
