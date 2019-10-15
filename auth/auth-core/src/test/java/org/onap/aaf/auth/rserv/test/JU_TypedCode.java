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

package org.onap.aaf.auth.rserv.test;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
//import org.onap.aaf.auth.rserv.Acceptor;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.auth.rserv.RouteReport;
import org.onap.aaf.auth.rserv.TypedCode;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class JU_TypedCode {
    TypedCode typedCode;
    @Mock
    RouteReport routeReportMock;

    @Before
    public void setUp(){
        typedCode = new TypedCode();
    }

    @Test
    public void testFirst(){
        String returnVal = typedCode.first();
        assertNull(returnVal);
    }

    @Test
    public void testAdd() {
        HttpCode<?, ?> code = mock(HttpCode.class);
        typedCode.add(code , "test", "test1", "test2");
    }

    @Test
    public void testPrep() throws IOException, ServletException, ClassNotFoundException {
        Trans trans = mock(Trans.class);
        TimeTaken time = new TimeTaken("yell", 2) {
            @Override
            public void output(StringBuilder sb) {
                // TODO Auto-generated method stub
            }
        };
        when(trans.start(";na=me;,prop", 8)).thenReturn(time);
        HttpCode<?, ?> code = mock(HttpCode.class);
        code.pathParam(null, null);
        code.isAuthorized(null); //Testing httpcode, currently not working
        code.no_cache();
        code.toString();
    
        typedCode.add(code , "");
        typedCode.prep(null , "q");
    
        typedCode.add(code , "t");
        typedCode.prep(trans , null);
    
        typedCode.add(code , "t");
        typedCode.prep(trans , "");
    
        typedCode.add(code, "POST /authn/validate application/CredRequest+json;charset=utf-8;version=2.0,application/json;version=2.0,*/*");
        //typedCode.prep(trans , "POST /authn/validate application/CredRequest+json;charset=utf-8;version=2.0,application/json;version=2.0,*/*");    
    }

    @Test
    public void testRelatedTo() {
        HttpCode<?, ?> code = mock(HttpCode.class);
        StringBuilder sb = new StringBuilder();
        typedCode.relatedTo(code, sb);
    }

}
