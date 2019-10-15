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
package org.onap.aaf.auth.locate.api;

import static org.junit.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.onap.aaf.auth.locate.AAF_Locate;
import org.onap.aaf.auth.locate.facade.LocateFacade;

public class JU_API_AAFTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AAF_Locate gwAPI;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private LocateFacade facade;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void testAPI_AAFAccess() throws Exception {
        try {
            API_AAFAccess.init(gwAPI, facade);
        } catch (Exception e) {
            fail("There should be no exception as Mocks are used");
        }
    }

    @Test
    public void testAPI_Find() throws Exception {
        try {
            API_Find.init(gwAPI, facade);
        } catch (Exception e) {
            fail("There should be no exception as Mocks are used");
        }
    }

    @Test
    public void testAPI_API() throws Exception {
        try {
            API_Api.init(gwAPI, facade);
        } catch (Exception e) {
            fail("There should be no exception as Mocks are used");
        }
    }
}
