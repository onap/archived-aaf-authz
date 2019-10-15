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

package org.onap.aaf.auth.rserv.test;

import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.mockito.Matchers;
import org.onap.aaf.auth.rserv.Route;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.auth.rserv.*;

public class JU_Route {
    Route route;
    HttpCode httpCode;
    HttpMethods httpMethod;
    Trans trans;

    @Before
    public void setUp() {        //TODO: AAF-111 complete when actual input is provided
        //httpMethod = Matchers.any(HttpMethods.class);
        //when(httpMethod.name()).thenReturn("test");
    //    route = new Route(null,"path/to/place");
    }


    @Test
    public void testAdd() {
    //    route.add(httpCode, "path/to/place");
    }

    @Test
    public void testStart() {
    //    trans = mock(Trans.class);
    //    route.start(trans, "test", httpCode, "test");
    }

}
