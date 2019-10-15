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
package org.onap.aaf.cadi.aaf.v2_0;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.cadi.AbsUserCache;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.User;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.principal.BasicPrincipal;

public class JU_AAFAuthn {

    @Mock
    AAFCon con;

    @Mock
    AbsUserCache<AAFPermission> cache;

    @Mock
    PropAccess propaccess;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void testGetRealm() {
        AAFAuthn authnObj = new AAFAuthn(con);
        String realm = authnObj.getRealm();
        assertNull(realm);
    }

    @Test
    public void testValidateFailure() {
        AAFAuthnImplWithGetUserNull authnObj = new AAFAuthnImplWithGetUserNull(con, cache);
        String realm="";
        try {
            Mockito.doReturn("test").when(propaccess).decrypt("test", false);
            realm = authnObj.validate("test", "test");
            assertNull(realm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            assertNull( e.getLocalizedMessage());
        }
    }

    @Test
    public void testValidate() {
        AAFAuthnImpl authnObj = new AAFAuthnImpl(con);
        String realm="";
        try {
            Mockito.doReturn("test").when(propaccess).decrypt("test", false);
            Rcli rcliObj = Mockito.mock(Rcli.class);
            Mockito.doReturn(rcliObj).when(con).client();
            Mockito.doReturn(rcliObj).when(rcliObj).forUser(null);
            Future<String> futureObj = Mockito.mock(Future.class);
            Mockito.doReturn(futureObj).when(rcliObj).read( "/authn/basicAuth","text/plain");
            realm = authnObj.validate("test", "test","test");
            assertTrue(realm.contains("user/pass combo invalid"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateRevalidate() {
        AAFAuthnImpl authnObj = new AAFAuthnImpl(con);
        String realm="";
        try {
            Mockito.doReturn("test").when(propaccess).decrypt("test", false);
            Rcli rcliObj = Mockito.mock(Rcli.class);
            Mockito.doReturn(rcliObj).when(con).client();
            Mockito.doReturn(rcliObj).when(rcliObj).forUser(null);
            Future<String> futureObj = Mockito.mock(Future.class);
            Mockito.doReturn(futureObj).when(rcliObj).read( "/authn/basicAuth","text/plain");
            Mockito.doReturn(true).when(futureObj).get( 0);
            realm = authnObj.validate("test", "test","test");
            assertNull(realm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateValidUser() {
        AAFAuthnImplWithGetUser authnObj = new AAFAuthnImplWithGetUser(con);
        String realm="";
        try {
            Mockito.doReturn("test").when(propaccess).decrypt("test", false);
            realm = authnObj.validate("test", "test","test");
            assertTrue(realm.contains("User already denied"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testValidateValidUserNull() {
        AAFAuthnImplWithGetUserNull authnObj = new AAFAuthnImplWithGetUserNull(con);
        String realm="";
        try {
            Mockito.doReturn("test").when(propaccess).decrypt("test", false);
            realm = authnObj.validate("test", "test","test");
            assertNull(realm);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    class AAFAuthnImpl extends AAFAuthn{
        AAFAuthnImpl(AAFCon con) {
            super(con);
            this.access = propaccess;
            // TODO Auto-generated constructor stub
        }
    
        AAFAuthnImpl(AAFCon con, AbsUserCache cache) {
            super(con, cache);
            this.access = propaccess;
            // TODO Auto-generated constructor stub
        }
    
    
    }

    class AAFAuthnImplWithGetUser extends AAFAuthn{
        AAFAuthnImplWithGetUser(AAFCon con) {
            super(con);
            this.access = propaccess;
            // TODO Auto-generated constructor stub
        }
    
        AAFAuthnImplWithGetUser(AAFCon con, AbsUserCache cache) {
            super(con, cache);
            this.access = propaccess;
            // TODO Auto-generated constructor stub
        }
    
        @Override
        protected User getUser(String user, byte[] cred) {
            return new User<>("test",new byte[] {});
        }
    }

    class AAFAuthnImplWithGetUserNull extends AAFAuthn{
        AAFAuthnImplWithGetUserNull(AAFCon con) {
            super(con);
            this.access = propaccess;
            // TODO Auto-generated constructor stub
        }
    
        AAFAuthnImplWithGetUserNull(AAFCon con, AbsUserCache cache) {
            super(con, cache);
            this.access = propaccess;
            // TODO Auto-generated constructor stub
        }
    
        @Override
        protected User getUser(String user, byte[] cred) {
            User user1 = null;
            try {
                user1 = new User(new BasicPrincipal("test","test"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return user1;
        }
    }
}
