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

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is; 
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aaf.cadi.oauth.OAuth2Principal;
import org.onap.aaf.cadi.oauth.TokenPerm;

public class JU_OAuth2Principal {

    @Mock TokenPerm tpMock;


    private static final String username = "username";

    private static final byte[] hash = "hashstring".getBytes();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    
        when(tpMock.getUsername()).thenReturn(username);
    }

    @Test
    public void test() {
        OAuth2Principal princ = new OAuth2Principal(tpMock, hash);
        assertThat(princ.getName(), is(username));
        assertThat(princ.tokenPerm(), is(tpMock));
        assertThat(princ.tag(), is("OAuth"));
        assertThat(princ.personalName(), is(username));
    }

}
