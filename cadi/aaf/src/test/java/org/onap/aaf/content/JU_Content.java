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

package org.onap.aaf.content;

import java.io.StringReader;

import org.junit.*;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import aaf.v2_0.Error;

// TODO: This test doesn't really do anything. It should be deleted as soon as coverage is above 50%

public class JU_Content {

    @Test
    public void parseErrorJSON() throws Exception {
        final String msg = "{\"messageId\":\"SVC2000\",\"text\":\"Select which cred to delete (or 0 to delete all):" +
            "1) %1" +
            "2) %2" +
            "3) %3" +
            "4) %4" +
            "Run same command again with chosen entry as last parameter\"," +
            "\"variables\":[" +
            "\"m55555@jr583u.cred.test.com 1 Wed Oct 08 11:48:08 CDT 2014\"," +
            "\"m55555@jr583u.cred.test.com 1 Thu Oct 09 12:54:46 CDT 2014\"," +
            "\"m55555@jr583u.cred.test.com 1 Tue Jan 06 05:00:00 CST 2015\"," +
            "\"m55555@jr583u.cred.test.com 1 Wed Jan 07 05:00:00 CST 2015\"]}";
    
        Error err = new Error();
        err.setText("Hello");
        err.getVariables().add("I'm a teapot");
        err.setMessageId("12");
    
    
//        System.out.println(msg);
        RosettaEnv env = new RosettaEnv();
        RosettaDF<aaf.v2_0.Error> errDF = env.newDataFactory(aaf.v2_0.Error.class);
        errDF.in(RosettaData.TYPE.JSON);
        errDF.out(RosettaData.TYPE.JSON);
        RosettaData<Error> data = errDF.newData();
        data.load(err);
    
        @SuppressWarnings("unused")
        String output = data.asString();
//        System.out.println(output);
    
        data.load(new StringReader(msg));
        err = data.asObject();
        output = err.getText();
//        System.out.println(output);
    }
    

}
