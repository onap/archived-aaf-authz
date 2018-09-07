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

package org.onap.aaf.cadi.aaf.v2_0.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.servlet.http.HttpServletRequest;

import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFTrustChecker;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.cadi.taf.TafResp;
import org.onap.aaf.cadi.taf.TrustNotTafResp;
import org.onap.aaf.cadi.taf.TrustTafResp;
import org.onap.aaf.misc.env.Env;

public class JU_AAFTrustChecker {

    private final static String type = "type";
    private final static String instance = "instance";
    private final static String action = "action";
    private final static String key = type + '|' + instance + '|' + action;
    private final static String name = "name";
    private final static String otherName = "otherName";

    private PropAccess access;

    @Mock private Env envMock;
    @Mock private TafResp trespMock;
    @Mock private HttpServletRequest reqMock;
    @Mock private TaggedPrincipal tpMock;
    @Mock private Lur lurMock;
    @Mock private TaggedPrincipal princMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        access = new PropAccess(new PrintStream(new ByteArrayOutputStream()), new String[0]);
    }

    @Test
    public void test() {
        AAFTrustChecker trustChecker;

        // coverage calls
        trustChecker = new AAFTrustChecker(access);
        trustChecker = new AAFTrustChecker(envMock);

        access.setProperty(Config.CADI_TRUST_PERM, "example");
        when(envMock.getProperty(Config.CADI_TRUST_PERM)).thenReturn("example");
        trustChecker = new AAFTrustChecker(access);
        trustChecker = new AAFTrustChecker(envMock);

        access.setProperty(Config.CADI_TRUST_PERM, key);
        when(envMock.getProperty(Config.CADI_TRUST_PERM)).thenReturn(key);
        trustChecker = new AAFTrustChecker(access);
        trustChecker = new AAFTrustChecker(envMock);

        trustChecker.setLur(lurMock);

        assertThat(trustChecker.mayTrust(trespMock, reqMock), is(trespMock));

        when(reqMock.getHeader(null)).thenReturn("comma,comma,comma");
        assertThat(trustChecker.mayTrust(trespMock, reqMock), is(trespMock));

        when(reqMock.getHeader(null)).thenReturn("colon:colon:colon:colon,comma,comma");
        assertThat(trustChecker.mayTrust(trespMock, reqMock), is(trespMock));

        when(reqMock.getHeader(null)).thenReturn("colon:colon:colon:AS,comma,comma");
        when(trespMock.getPrincipal()).thenReturn(tpMock);
        when(tpMock.getName()).thenReturn(name);
        when(lurMock.fish(princMock, null)).thenReturn(true);
        TafResp tntResp = trustChecker.mayTrust(trespMock, reqMock);

        assertThat(tntResp instanceof TrustNotTafResp, is(true));
        assertThat(tntResp.toString(), is("name requested trust as colon, but does not have Authorization"));

        when(reqMock.getHeader(null)).thenReturn(name + ":colon:colon:AS,comma,comma");
        assertThat(trustChecker.mayTrust(trespMock, reqMock), is(trespMock));

        when(envMock.getProperty(Config.CADI_ALIAS, null)).thenReturn(name);
        when(envMock.getProperty(Config.CADI_TRUST_PERM)).thenReturn(null);
        trustChecker = new AAFTrustChecker(envMock);
        trustChecker.setLur(lurMock);

        when(trespMock.getPrincipal()).thenReturn(princMock);
        when(princMock.getName()).thenReturn(otherName);
        when(lurMock.fish(princMock, null)).thenReturn(true);
        TafResp ttResp = trustChecker.mayTrust(trespMock, reqMock);
        assertThat(ttResp instanceof TrustTafResp, is(true));
        assertThat(ttResp.toString(), is(name + " by trust of   " + name + " validated using colon by colon, null"));

        when(princMock.getName()).thenReturn(name);
        ttResp = trustChecker.mayTrust(trespMock, reqMock);
        assertThat(ttResp instanceof TrustTafResp, is(true));
        assertThat(ttResp.toString(), is(name + " by trust of   " + name + " validated using colon by colon, null"));
    }

}
