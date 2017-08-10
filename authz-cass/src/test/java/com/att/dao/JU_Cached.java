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
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.dao;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import com.att.authz.env.AuthzEnv;
import com.att.authz.env.AuthzTrans;
import com.att.authz.layer.Result;
import com.att.cache.Cache;
import com.att.cache.Cache.Dated;
import com.att.dao.Cached.Getter;
//import com.att.dao.Cached.Refresh;
import com.att.inno.env.Trans;

@RunWith(PowerMockRunner.class)
public class JU_Cached {
	Cached cached;
	@Mock
	CIDAO<Trans> ciDaoMock;
	@Mock
	AuthzEnv authzEnvMock;
	@Mock
	CIDAO<AuthzTrans> cidaoATMock;
	
	String name = "nameString";
	
	@Before
	public void setUp(){
		cached = new Cached(ciDaoMock, name, 0);
	}
	
	@Test(expected=ArithmeticException.class)
	public void testCachedIdx(){
		int Result = cached.cacheIdx("1234567890");		
	}
	
	@Test(expected=ArithmeticException.class)
	public void testInvalidate(){
		int Res = cached.invalidate(name);
	}
	
	@SuppressWarnings("static-access")
	@Test
	public void testStopTimer(){
		cached.stopTimer();
		assertTrue(true);
	}

	@SuppressWarnings("static-access")
	@Test
	public void testStartRefresh(){
		cached.startRefresh(authzEnvMock, cidaoATMock);
		assertTrue(true);
	}
//	@Mock
//	Trans transMock;
//	@Mock
//	Getter<DAO> getterMock;
//	
//	@Test
//	public void testGet(){
//		cached.get(transMock, name, getterMock);
//		fail("not implemented");
//	}
//	
//	@SuppressWarnings("unchecked")
//	public Result<List<DATA>> get(TRANS trans, String key, Getter<DATA> getter) {
//		List<DATA> ld = null;
//		Result<List<DATA>> rld = null;
//		
//		int cacheIdx = cacheIdx(key);
//		Map<String, Dated> map = ((Map<String,Dated>)cache[cacheIdx]);
//		
//		// Check for saved element in cache
//		Dated cached = map.get(key);
//		// Note: These Segment Timestamps are kept up to date with DB
//		Date dbStamp = info.get(trans, name,cacheIdx);
//		
//		// Check for cache Entry and whether it is still good (a good Cache Entry is same or after DBEntry, so we use "before" syntax)
//		if(cached!=null && dbStamp.before(cached.timestamp)) {
//			ld = (List<DATA>)cached.data;
//			rld = Result.ok(ld);
//		} else {
//			rld = getter.get();
//			if(rld.isOK()) { // only store valid lists
//				map.put(key, new Dated(rld.value));  // successful item found gets put in cache
////			} else if(rld.status == Result.ERR_Backend){
////				map.remove(key);
//			}
//		}
//		return rld;
//	}
}
