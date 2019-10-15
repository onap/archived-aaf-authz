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

package org.onap.aaf.cadi.oauth.test;

import static org.mockito.Mockito.when;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.oauth.OAuth2Lur;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.oauth.TokenMgr;
import org.onap.aaf.cadi.oauth.TokenPerm;
import org.onap.aaf.cadi.principal.BearerPrincipal;

public class JU_OAuth2Lur {

    private List<AAFPermission> aafPerms;
    private List<Permission> perms;

    @Mock private TokenMgr tmMock;
    @Mock private AAFPermission pondMock;
    @Mock private Principal princMock;
    @Mock private OAuth2Principal oauthPrincMock;
    @Mock private BearerPrincipal bearPrincMock;
    @Mock private TokenPerm tpMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        OAuth2Lur lur = new OAuth2Lur(tmMock);
        lur.createPerm("testPerm");
        lur.createPerm("testPerm1|testPerm2|testPerm3");

        assertThat(lur.fish(princMock, pondMock), is(false));
        assertThat(lur.fish(oauthPrincMock, pondMock), is(false));
    
        when(oauthPrincMock.tokenPerm()).thenReturn(tpMock);
        assertThat(lur.fish(oauthPrincMock, pondMock), is(false));
    
        aafPerms = new ArrayList<>();
        aafPerms.add(pondMock);
        aafPerms.add(pondMock);
        when(tpMock.perms()).thenReturn(aafPerms);
        when(pondMock.match(pondMock)).thenReturn(false).thenReturn(true);
        assertThat(lur.fish(oauthPrincMock, pondMock), is(true));

        perms = new ArrayList<>();
        perms.add(pondMock);
        perms.add(pondMock);
        lur.fishAll(oauthPrincMock, perms);

        when(oauthPrincMock.tokenPerm()).thenReturn(null);
        lur.fishAll(oauthPrincMock, perms);
    
        assertThat(lur.handlesExclusively(pondMock), is(false));
    
        assertThat(lur.handles(null), is(false));
        assertThat(lur.handles(princMock), is(false));
        assertThat(lur.handles(bearPrincMock), is(false));
        when(bearPrincMock.getBearer()).thenReturn("not null :)");
        assertThat(lur.handles(bearPrincMock), is(true));

        lur.destroy();
        lur.clear(null, null);
    }

}
