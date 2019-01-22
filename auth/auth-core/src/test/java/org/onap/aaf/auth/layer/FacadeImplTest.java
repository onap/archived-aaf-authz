/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2019 IBM Intellectual Property. All rights reserved.
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

package org.onap.aaf.auth.layer;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.misc.env.Data.TYPE;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FacadeImplTest {

    FacadeImpl facade;
    HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        facade = new FacadeImpl() {
        };
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void setContentType() {
        TYPE type = TYPE.JSON;
        facade.setContentType(response, type);
        verify(response).setContentType("application/json");

        type = TYPE.XML;
        facade.setContentType(response, type);
        verify(response).setContentType("text.xml");
    }

    @Test
    public void setCacheControlOff() {
        facade.setCacheControlOff(response);
        verify(response).setHeader("Cache-Control", "no-store");
        verify(response).setHeader("Pragma", "no-cache");
    }
}