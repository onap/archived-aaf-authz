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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.oauth.TokenPerm;
import org.onap.aaf.cadi.oauth.TokenPerm.LoadPermissions;
import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import aaf.v2_0.Perms;
import aafoauth.v2_0.Introspect;

public class JU_TokenPerm {

    private static final byte[] hash = "hashstring".getBytes();

    private static final String clientId = "clientId";
    private static final String username = "username";
    private static final String token = "token";
    private static final String scopes = "scopes";
    private static final String content = "content";

    private static final long expires = 10000L;

    private static Path path;

    @Mock private Persist<Introspect, ?> persistMock;
    @Mock private RosettaDF<Perms> dfMock;
    @Mock private Introspect introspectMock;

    @Before
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);

        when(introspectMock.getExp()).thenReturn(expires);
        when(introspectMock.getClientId()).thenReturn(clientId);
        when(introspectMock.getUsername()).thenReturn(username);
        when(introspectMock.getAccessToken()).thenReturn(token);
        when(introspectMock.getScope()).thenReturn(scopes);
        when(introspectMock.getExp()).thenReturn(expires);

        path = Files.createTempFile("fake", ".txt");
    }

    @Test
    public void tokenTest() throws APIException {
        TokenPerm tokenPerm = new TokenPerm(persistMock, dfMock, introspectMock, hash, path);
        assertThat(tokenPerm.perms().size(), is(0));
        assertThat(tokenPerm.getClientId(), is(clientId));
        assertThat(tokenPerm.getUsername(), is(username));
        assertThat(tokenPerm.getToken(), is(token));
        assertThat(tokenPerm.getScopes(), is(scopes));
        assertThat(tokenPerm.getIntrospect(), is(introspectMock));

        when(introspectMock.getContent()).thenReturn(content);
        tokenPerm = new TokenPerm(persistMock, dfMock, introspectMock, hash, path);
    }

    @Test
    public void test() throws ParseException {
        String json;
        LoadPermissions lp;
        Permission p;

        json = "{\"perm\":[" +
            "  {\"ns\":\"com\",\"type\":\"access\",\"instance\":\"*\",\"action\":\"read,approve\"}," +
            "]}";

        lp = new LoadPermissions(new StringReader(json));
        assertThat(lp.perms.size(), is(1));

        p = lp.perms.get(0);
        assertThat(p.getKey(), is("com|access|*|read,approve"));
        assertThat(p.permType(), is("AAF"));

        // Extra closing braces for coverage
        json = "{\"perm\":[" +
            "  {\"ns\":\"com\",\"type\":\"access\",\"instance\":\"*\",\"action\":\"read,approve\"}}," +
            "]]}";

        lp = new LoadPermissions(new StringReader(json));
        assertThat(lp.perms.size(), is(1));

        p = lp.perms.get(0);
        assertThat(p.getKey(), is("com|access|*|read,approve"));
        assertThat(p.permType(), is("AAF"));

        // Test without a type
        json = "{\"perm\":[" +
            "  {\"instance\":\"*\",\"action\":\"read,approve\"}," +
            "]}";

        lp = new LoadPermissions(new StringReader(json));
        assertThat(lp.perms.size(), is(0));

        // Test without an instance
        json = "{\"perm\":[" +
            "  {\"type\":\"com.access\",\"action\":\"read,approve\"}," +
            "]}";

        lp = new LoadPermissions(new StringReader(json));
        assertThat(lp.perms.size(), is(0));

        // Test without an action
        json = "{\"perm\":[" +
            "  {\"type\":\"com.access\",\"instance\":\"*\"}," +
            "]}";

        lp = new LoadPermissions(new StringReader(json));
        assertThat(lp.perms.size(), is(0));
    }

    @Test
    public void redundancyTest() {
        String json = "{\"perm\":[" +
                "  {\"type\":\"com.access\",\"instance\":\"*\",\"action\":\"read,approve\"}," +
                "  {\"type\":\"org.osaaf.aaf.access\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.aaf.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.aaf.attrib\",\"instance\":\":com.att.*:swm\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.aaf.bogus\",\"instance\":\"sample\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.aaf.ca\",\"instance\":\"aaf\",\"action\":\"ip\"}," +
                "  {\"type\":\"org.osaaf.aaf.ca\",\"instance\":\"local\",\"action\":\"domain\"}," +
                "  {\"type\":\"org.osaaf.aaf.cache\",\"instance\":\"*\",\"action\":\"clear\"}," +
                "  {\"type\":\"org.osaaf.aaf.cass\",\"instance\":\":mithril\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.aaf.certman\",\"instance\":\"local\",\"action\":\"read,request,showpass\"}," +
                "  {\"type\":\"org.osaaf.aaf.db\",\"instance\":\"pool\",\"action\":\"clear\"}," +
                "  {\"type\":\"org.osaaf.aaf.deny\",\"instance\":\"com.att\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.aaf.jenkins\",\"instance\":\"mithrilcsp.sbc.com\",\"action\":\"admin\"}," +
                "  {\"type\":\"org.osaaf.aaf.log\",\"instance\":\"com.att\",\"action\":\"id\"}," +
                "  {\"type\":\"org.osaaf.aaf.myPerm\",\"instance\":\"myInstance\",\"action\":\"myAction\"}," +
                "  {\"type\":\"org.osaaf.aaf.ns\",\"instance\":\":com.att.*:ns\",\"action\":\"write\"}," +
                "  {\"type\":\"org.osaaf.aaf.ns\",\"instance\":\":com.att:ns\",\"action\":\"write\"}," +
                "  {\"type\":\"org.osaaf.aaf.password\",\"instance\":\"com.att\",\"action\":\"extend\"}," +
                "  {\"type\":\"org.osaaf.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.authz.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.authz.dev.access\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.authz.swm.star\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.cadi.access\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.chris.access\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.csid.lab.swm.node\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.myapp.access\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.myapp.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.sample.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.sample.swm.myPerm\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.temp.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"org.osaaf.test.access\",\"instance\":\"*\",\"action\":\"*\"}," +
                "  {\"type\":\"org.osaaf.test.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"com.test.access\",\"instance\":\"*\",\"action\":\"read\"}," +
                "  {\"type\":\"com.test.access\",\"instance\":\"*\",\"action\":\"read\"}" +
                "]}";
        try {
            LoadPermissions lp = new LoadPermissions(new StringReader(json));
            assertThat(lp.perms.size(), is(34));
        } catch (ParseException e) {
            fail(e.getMessage());
        }
    }

}
