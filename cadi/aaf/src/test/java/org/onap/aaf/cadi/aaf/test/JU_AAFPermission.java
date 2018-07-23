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

import org.junit.Before;
import org.junit.Test;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.aaf.AAFPermission;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JU_AAFPermission {
    private static final List<String> NO_ROLES = new ArrayList<>();
    private static final String NS = "ns";
    private static final String TYPE = "type";
    private static final String INSTANCE = "instance";
    private static final String ACTION = "action";
    private static final String KEY = NS + '|' + TYPE + '|' + INSTANCE + '|' + ACTION;
    private static final String ROLE = "role";

    private static List<String> roles;

    @Before
    public void setup() {
        roles = new ArrayList<>();
        roles.add(ROLE);
    }

    @Test
    public void shouldCreatePermissionWithCorrectKeyComponents() {
        AAFPermission perm = new AAFPermission(NS, TYPE, INSTANCE, ACTION);

        assertAAFPermissionKeyComponenets(perm);
    }

    @Test
    public void shouldCreatePermissionWithCorrectKey() {
        AAFPermission perm;

        perm = new AAFPermission(NS, TYPE, INSTANCE, ACTION);
        assertThat(perm.getKey(), is(KEY));

        perm = new AAFPermission(null, TYPE, INSTANCE, ACTION);
        assertThat(perm.getKey(), is(TYPE + '|' + INSTANCE + '|' + ACTION));
    }

    @Test
    public void shouldCreatePermissionWithCorrectRoles() {
        AAFPermission perm;

        perm = new AAFPermission(NS, TYPE, INSTANCE, ACTION);
        assertThat(perm.roles(), is(NO_ROLES));

        perm = new AAFPermission(NS, TYPE, INSTANCE, ACTION, roles);
        assertThat(perm.roles(), is(roles));
    }

    @Test
    public void shouldCorrectlyMatchBasedOnParsedKey() {
        final AAFPermission controlPermission = new AAFPermission(NS, TYPE, INSTANCE, ACTION);

        assertThat(controlPermission.match(newPermission("ns|type|instance|action")), is(true));
        assertThat(controlPermission.match(newPermission("not a valid KEY")), is(false));
        assertThat(controlPermission.match(newPermission("ns")), is(false));
        assertThat(controlPermission.match(newPermission("ns|type")), is(false));
        assertThat(controlPermission.match(newPermission("ns|type|instance")), is(false));
        assertThat(controlPermission.match(newPermission("ns|type|instance|badAction")), is(false));
    }

    @Test
    public void shouldCorrectlyMatchWithNotDefinedNs() {
        AAFPermission controlPermission;

        controlPermission = new AAFPermission(null, TYPE, INSTANCE, ACTION);
        assertThat(controlPermission.match(new AAFPermission(null, TYPE, INSTANCE, ACTION)), is(true));
        assertThat(controlPermission.match(new AAFPermission(NS, TYPE, INSTANCE, ACTION)), is(false));

        controlPermission = new AAFPermission(null, NS + '.' + TYPE, INSTANCE, ACTION);
        assertThat(controlPermission.match(new AAFPermission(NS, TYPE, INSTANCE, ACTION)), is(true));
    }

    @Test
    public void shouldCorrectlyMatchWithNotDefinedNsInReferencePermission() {
        final AAFPermission controlPermission = new AAFPermission(NS, TYPE, INSTANCE, ACTION);

        assertThat(controlPermission.match(new AAFPermission(null, NS + '.' + TYPE, INSTANCE, ACTION)), is(true));
        assertThat(controlPermission.match(new AAFPermission(null, TYPE, INSTANCE, ACTION)), is(false));
    }

    private PermissionStub newPermission(String s) {
        return new PermissionStub(s);
    }

    private void assertAAFPermissionKeyComponenets(AAFPermission perm) {
        assertThat(perm.getNS(), is(NS));
        assertThat(perm.getType(), is(TYPE));
        assertThat(perm.getInstance(), is(INSTANCE));
        assertThat(perm.getAction(), is(ACTION));
        assertThat(perm.permType(), is("AAF"));
        assertThat(perm.toString(), is("AAFPermission:" +
                "\n\tNS: " + NS +
                "\n\tType: " + TYPE +
                "\n\tInstance: " + INSTANCE +
                "\n\tAction: " + ACTION +
                "\n\tKey: " + KEY));
    }

    private class PermissionStub implements Permission {

        private String key;

        private PermissionStub(String key) {
            this.key = key;
        }

        @Override
        public String permType() {
            return null;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public boolean match(Permission p) {
            return false;
        }
    }

}
