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

package org.onap.aaf.misc.rosetta.test;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import s.xsd.LargerData;
import s.xsd.SampleData;

public class JU_Nulls {

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        RosettaEnv env = new RosettaEnv();
        try {
            RosettaDF<LargerData> df = env.newDataFactory(LargerData.class);
            df.out(Data.TYPE.JSON);
            LargerData urr = new LargerData();
            SampleData sd = new SampleData();
            sd.setDate(1444125487798L);
            sd.setId(null);
            urr.getSampleData().add(sd);
            urr.setFluff(null);
            RosettaData<LargerData> data = df.newData();
//            StringWriter sw = new StringWriter();
//            df.direct(trans, urr, sw);
//            System.out.println(sw.toString());
            data.load(urr);
            System.out.println(data.asString());
            Assert.assertEquals("{\"SampleData\":[{\"date\":1444125487798}]}", data.asString());

            System.out.println(data.out(Data.TYPE.RAW).asString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
