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

package org.onap.aaf.cadi.aaf.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;

import org.onap.aaf.cadi.aaf.PermEval;

public class JU_PermEval {

    @Test
    public void instanceNullTest() {
        assertThat(PermEval.evalInstance(null, null), is(false));
        assertThat(PermEval.evalInstance(null, "test"), is(false));
        assertThat(PermEval.evalInstance("test", null), is(false));
    }

    @Test
    public void instanceEmptyTest() {
        assertThat(PermEval.evalInstance("", ""), is(false));
        assertThat(PermEval.evalInstance("", "test"), is(false));
        assertThat(PermEval.evalInstance("test", ""), is(false));
    }

    @Test
    public void instanceAsterixTest() {
        assertThat(PermEval.evalInstance("*", "*"), is(true));
        assertTrue(PermEval.evalInstance("*","fred"));
    }

    @Test
    public void instanceRegexTest() {
        assertThat(PermEval.evalInstance("test", "!test"), is(true));
        assertThat(PermEval.evalInstance(",", "!"), is(true));
        assertThat(PermEval.evalInstance("test,test", "!test"), is(true));

        assertThat(PermEval.evalInstance("test", "!"), is(false));
        assertThat(PermEval.evalInstance("test", "!mismatch"), is(false));
        assertThat(PermEval.evalInstance("test,mismatch", "!mismatch"), is(false));
    }

    @Test
    public void instanceKeyTest() {
        // Reject non-keys
        assertThat(PermEval.evalInstance("fred", ":fred"), is(false));

        // Reject differing number of keys
        assertThat(PermEval.evalInstance(":fred:barney", ":fred"), is(false));
        assertThat(PermEval.evalInstance(":fred", ":fred:barney"), is(false));

        // Accept all wildcard keys
        assertThat(PermEval.evalInstance(":*", ":fred"), is(true));

        // Accept matching empty keys
        assertThat(PermEval.evalInstance(":", ":"), is(true));
        assertThat(PermEval.evalInstance("/", "/"), is(true));
        assertThat(PermEval.evalInstance("/something/", "/something/"), is(true));

        // Reject non-matching empty keys
        assertThat(PermEval.evalInstance(":fred", ":"), is(false));

        // Accept matches starting with a wildcard
        assertThat(PermEval.evalInstance(":!.*ed", ":fred"), is(true));

        // Reject non-matches starting with a wildcard
        assertThat(PermEval.evalInstance(":!.*arney", ":fred"), is(false));

        // Accept matches ending with a wildcard
        assertThat(PermEval.evalInstance(":fr*", ":fred"), is(true));

        // Reject non-matches ending with a wildcard
        assertThat(PermEval.evalInstance(":bar*", ":fred"), is(false));

        // Accept exact keys
        assertThat(PermEval.evalInstance(":fred", ":fred"), is(true));

        // Reject mismatched keys
        assertThat(PermEval.evalInstance(":fred", ":barney"), is(false));

        // Check using alt-start character
        assertThat(PermEval.evalInstance("/fred", "/fred"), is(true));
        assertThat(PermEval.evalInstance("/barney", "/fred"), is(false));
    }

    @Test
    public void instanceDirectTest() {
        assertThat(PermEval.evalInstance("fred","fred"), is(true));
        assertThat(PermEval.evalInstance("fred,wilma","fred"), is(true));
        assertThat(PermEval.evalInstance("barney,betty,fred,wilma","fred"), is(true));
        assertThat(PermEval.evalInstance("barney,betty,wilma","fred"), is(false));

        assertThat(PermEval.evalInstance("fr*","fred"), is(true));
        assertThat(PermEval.evalInstance("freddy*","fred"), is(false));
        assertThat(PermEval.evalInstance("ba*","fred"), is(false));
    }

    @Test
    public void actionTest() {
        // Accept server *
        assertThat(PermEval.evalAction("*", ""), is(true));
        assertThat(PermEval.evalAction("*", "literally anything"), is(true));

        // Reject empty actions
        assertThat(PermEval.evalAction("literally anything", ""), is(false));

        // Accept match as regex
        assertThat(PermEval.evalAction("action", "!action"), is(true));

        // Reject non-match as regex
        assertThat(PermEval.evalAction("action", "!nonaction"), is(false));

        // Accept exact match
        assertThat(PermEval.evalAction("action", "action"), is(true));

        // Reject non-match
        assertThat(PermEval.evalAction("action", "nonaction"), is(false));
    }

    @Test
    public void redundancyTest() {
        // TRUE
        assertTrue(PermEval.evalInstance(":fred:fred",":fred:fred"));
        assertTrue(PermEval.evalInstance(":fred:fred,wilma",":fred:fred"));
        assertTrue(PermEval.evalInstance(":fred:barney,betty,fred,wilma",":fred:fred"));
        assertTrue(PermEval.evalInstance(":*:fred",":fred:fred"));
        assertTrue(PermEval.evalInstance(":fred:*",":fred:fred"));
        assertTrue(PermEval.evalInstance(":!f.*:fred",":fred:fred"));
        assertTrue(PermEval.evalInstance(":fred:!f.*",":fred:fred"));

        // FALSE
        assertFalse(PermEval.evalInstance("fred","wilma"));
        assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
        assertFalse(PermEval.evalInstance(":fred:fred",":fred:wilma"));
        assertFalse(PermEval.evalInstance(":fred:fred",":wilma:fred"));
        assertFalse(PermEval.evalInstance(":wilma:!f.*",":fred:fred"));
        assertFalse(PermEval.evalInstance(":!f.*:wilma",":fred:fred"));
        assertFalse(PermEval.evalInstance(":!w.*:!f.*",":fred:fred"));
        assertFalse(PermEval.evalInstance(":!f.*:!w.*",":fred:fred"));

        assertFalse(PermEval.evalInstance(":fred:!x.*",":fred:fred"));

        // MSO Tests 12/3/2015
        assertFalse(PermEval.evalInstance("/v1/services/features/*","/v1/services/features"));
        assertFalse(PermEval.evalInstance(":v1:services:features:*",":v1:services:features"));
        assertTrue(PermEval.evalInstance("/v1/services/features/*","/v1/services/features/api1"));
        assertTrue(PermEval.evalInstance(":v1:services:features:*",":v1:services:features:api2"));
        // MSO - Xue Gao
        assertTrue(PermEval.evalInstance(":v1:requests:*",":v1:requests:test0-service"));



        // Same tests, with Slashes
        assertTrue(PermEval.evalInstance("/fred/fred","/fred/fred"));
        assertTrue(PermEval.evalInstance("/fred/fred,wilma","/fred/fred"));
        assertTrue(PermEval.evalInstance("/fred/barney,betty,fred,wilma","/fred/fred"));
        assertTrue(PermEval.evalInstance("*","fred"));
        assertTrue(PermEval.evalInstance("/*/fred","/fred/fred"));
        assertTrue(PermEval.evalInstance("/fred/*","/fred/fred"));
        assertTrue(PermEval.evalInstance("/!f.*/fred","/fred/fred"));
        assertTrue(PermEval.evalInstance("/fred/!f.*","/fred/fred"));

        // FALSE
        assertFalse(PermEval.evalInstance("fred","wilma"));
        assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
        assertFalse(PermEval.evalInstance("/fred/fred","/fred/wilma"));
        assertFalse(PermEval.evalInstance("/fred/fred","/wilma/fred"));
        assertFalse(PermEval.evalInstance("/wilma/!f.*","/fred/fred"));
        assertFalse(PermEval.evalInstance("/!f.*/wilma","/fred/fred"));
        assertFalse(PermEval.evalInstance("/!w.*/!f.*","/fred/fred"));
        assertFalse(PermEval.evalInstance("/!f.*/!w.*","/fred/fred"));

        assertFalse(PermEval.evalInstance("/fred/!x.*","/fred/fred"));

        assertTrue(PermEval.evalInstance(":!com.att.*:role:write",":com.att.temp:role:write"));

        // CPFSF-431 Group needed help with Wild Card
        // They tried
        assertTrue(PermEval.evalInstance(
                ":topic.org.onap.sample_test.crm.pre*",
                ":topic.org.onap.sample_test.crm.predemo100"
                ));

        // Also can be
        assertTrue(PermEval.evalInstance(
                ":!topic.org.onap.sample_test.crm.pre.*",
                ":topic.org.onap.sample_test.crm.predemo100"
                ));

        // coverage
        @SuppressWarnings("unused")
        PermEval pe = new PermEval();
    }

    @Test
    public void pathTest() {
        assertTrue(PermEval.evalInstance("/","/"));
        assertFalse(PermEval.evalInstance("/","/hello"));
        assertTrue(PermEval.evalInstance("/","/"));
        assertTrue(PermEval.evalInstance("/onap/so/infra/*/*/*","/onap/so/infra/a/b/c"));
        assertFalse(PermEval.evalInstance("/onap/so/infra/*","/onap/so/infra"));
        assertTrue(PermEval.evalInstance("/onap/so/infra/*","/onap/so/infra/a/b/c"));
        assertTrue(PermEval.evalInstance("/onap/so/infra*","/onap/so/infra"));
        assertFalse(PermEval.evalInstance("/onap/so/infra*/hello","/onap/so/infra"));
        assertFalse(PermEval.evalInstance("/onap/so/infra*/hello","/onap/so/infra23"));
        assertTrue(PermEval.evalInstance("/onap/so/infra*/hello","/onap/so/infra23/hello"));
        assertFalse(PermEval.evalInstance("/onap/so/*/hello","/onap/so/infra23"));
        assertFalse(PermEval.evalInstance("/onap/so/*/","/onap/so/infra23"));
        assertTrue(PermEval.evalInstance("/onap/so/*/","/onap/so/infra23/"));
    }



}
