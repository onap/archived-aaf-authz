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

package org.onap.aaf.cadi.aaf.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import org.junit.*;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;

public class JU_AAFPermission {
    private final static String ns = "ns";
    private final static String type = "type";
    private final static String instance = "instance";
    private final static String action = "action";
    private final static String key = ns + '|' + type + '|' + instance + '|' + action;
    private final static String role = "role";

    private static List<String> roles;

    @Before
    public void setup() {
        roles = new ArrayList<>();
        roles.add(role);
    }

    @Test
    public void constructor1Test() {
        AAFPermission perm = new AAFPermission(ns, type, instance, action);
        assertThat(perm.getNS(), is(ns));
        assertThat(perm.getType(), is(type));
        assertThat(perm.getInstance(), is(instance));
        assertThat(perm.getAction(), is(action));
        assertThat(perm.getKey(), is(key));
        assertThat(perm.permType(), is("AAF"));
        assertThat(perm.roles().size(), is(0));
        assertThat(perm.toString(), is("AAFPermission:" +
                                        "\n\tNS: " + ns +
                                        "\n\tType: " + type +
                                        "\n\tInstance: " + instance +
                                        "\n\tAction: " + action +
                                        "\n\tKey: " + key));
    }

    @Test
    public void constructor2Test() {
        AAFPermission perm;

        perm = new AAFPermission(ns, type, instance, action, null);
        assertThat(perm.getNS(), is(ns));
        assertThat(perm.getType(), is(type));
        assertThat(perm.getInstance(), is(instance));
        assertThat(perm.getAction(), is(action));
        assertThat(perm.getKey(), is(key));
        assertThat(perm.permType(), is("AAF"));
        assertThat(perm.roles().size(), is(0));
        assertThat(perm.toString(), is("AAFPermission:" +
                                        "\n\tNS: " + ns +
                                        "\n\tType: " + type +
                                        "\n\tInstance: " + instance +
                                        "\n\tAction: " + action +
                                        "\n\tKey: " + key));

        perm = new AAFPermission(ns, type, instance, action, roles);
        assertThat(perm.getNS(), is(ns));
        assertThat(perm.getType(), is(type));
        assertThat(perm.getInstance(), is(instance));
        assertThat(perm.getAction(), is(action));
        assertThat(perm.getKey(), is(key));
        assertThat(perm.permType(), is("AAF"));
        assertThat(perm.roles().size(), is(1));
        assertThat(perm.roles().get(0), is(role));
        assertThat(perm.toString(), is("AAFPermission:" +
                "\n\tNS: " + ns +
                "\n\tType: " + type +
                "\n\tInstance: " + instance +
                "\n\tAction: " + action +
                "\n\tKey: " + key));
    }

    @Test
    public void matchTest() {
        final AAFPermission controlPermission = new AAFPermission(ns,type, instance, action);
        PermissionStub perm;
        AAFPermission aafperm;

        aafperm = new AAFPermission(ns, type, instance, action);
        assertThat(controlPermission.match(aafperm), is(true));

        perm = new PermissionStub(key);
        assertThat(controlPermission.match(perm), is(true));

        // Coverage tests
        perm = new PermissionStub("not a valid key");
        assertThat(controlPermission.match(perm), is(false));
        perm = new PermissionStub("type");
        assertThat(controlPermission.match(perm), is(false));
        perm = new PermissionStub("type|instance|badAction");
        assertThat(controlPermission.match(perm), is(false));
    }

    @Test
    public void coverageTest() {
        AAFPermissionStub aafps = new AAFPermissionStub();
        assertThat(aafps.getNS(), is(nullValue()));
        assertThat(aafps.getType(), is(nullValue()));
        assertThat(aafps.getInstance(), is(nullValue()));
        assertThat(aafps.getAction(), is(nullValue()));
        assertThat(aafps.getKey(), is(nullValue()));
        assertThat(aafps.permType(), is("AAF"));
        assertThat(aafps.roles().size(), is(0));
    }

    private class PermissionStub implements Permission {
        private String key;

        public PermissionStub(String key) { this.key = key; }
        @Override public String permType() { return null; }
        @Override public String getKey() { return key; }
        @Override public boolean match(Permission p) { return false; }
    }

    private class AAFPermissionStub extends AAFPermission {

    }
}
