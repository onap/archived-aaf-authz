/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * <p>
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * <p>
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.cadi.taf.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.*;

import java.io.IOException;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.taf.TafResp.RESP;

import org.onap.aaf.cadi.taf.PuntTafResp;


public class JU_PuntTafResp {

    @Test
    public void test() throws IOException {
        String name = "name";
        String explanation = "example explanation";

        PuntTafResp punt = new PuntTafResp(name, explanation);

        assertFalse(punt.isValid());
        assertThat(punt.isAuthenticated(), is(RESP.TRY_ANOTHER_TAF));
        assertThat(punt.desc(), is("Not processing this transaction: " + explanation));
        assertThat(punt.taf(), is(name));
        assertThat(punt.authenticate(), is(RESP.TRY_ANOTHER_TAF));
        assertThat(punt.getPrincipal(), is(nullValue()));
        assertThat(punt.getAccess(), is(Access.NULL));
        assertFalse(punt.isFailedAttempt());
    }

}
