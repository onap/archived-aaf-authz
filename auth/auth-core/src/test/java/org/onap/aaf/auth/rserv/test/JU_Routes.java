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

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
//import org.onap.aaf.auth.rserv.CodeSetter;
import org.onap.aaf.auth.rserv.Route;
import org.onap.aaf.auth.rserv.Routes;
import org.onap.aaf.misc.env.Trans;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_Routes {
    Routes routes;
    @Mock
    HttpServletRequest reqMock;
    //TODO: Gabe [JUnit] Not visible to junit
    //CodeSetter<Trans> codeSetterMock;
    Route<Trans> routeObj;

    @Before
    public void setUp(){
        routes = new Routes();
    }

    @Test
    public void testRouteReport(){
        List listVal = routes.routeReport(); 
        assertNotNull(listVal);
    }

    @Test
    public void testDerive() throws IOException, ServletException{
        routeObj = routes.derive(reqMock, null);
    
    }




}
