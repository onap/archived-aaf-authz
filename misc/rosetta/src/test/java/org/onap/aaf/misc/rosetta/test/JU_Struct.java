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

import org.junit.Test;
import org.onap.aaf.misc.rosetta.JaxInfo;

import s.xsd.LargerData;

public class JU_Struct {
    public final static String XML ="<LargerData xmlns=\"urn:s:xsd\">\n" +
                                        "<SampleData>\n" +
                                            "<id>sd object 1</id>\n" +
                                            "<date>1346439215932</date>\n" +
                                            "<item>Item 1.1</item>\n" +
                                            "<item>Item 1.2</item>\n" +
                                        "</SampleData>\n" +
                                        "<SampleData>\n" +
                                            "<id>sd object 2</id>\n" +
                                            "<date>1346439215932</date>\n" +
                                            "<item>Item 2.1</item>\n" +
                                            "<item>Item 2.2</item>\n" +
                                        "</SampleData>\n" +
                                        "<fluff>MyFluff</fluff>\n" +
                                    "</LargerData>\n";

//    @Test
//    public void test2() throws Exception  {
//
//        SampleData sd = new SampleData();
//        sd.setDate(new Date().getTime());
//        sd.setId("myId");
//        sd.getItem().add("Item 1.1");
//    
//        InObj<SampleData> inObj = new InObj<SampleData>(SampleData.class);
//
//        JaxSet<SampleData> jaxSet = JaxSet.get(SampleData.class);
//         Setter<SampleData> setter = jaxSet.setter("id");
//         setter.set(sd, "Your ID");
//     
//         for (Entry<String, Getter<SampleData>> es : jaxSet.getters()) {
//             System.out.print(es.getKey());
//             System.out.print(' ');
//             System.out.println(es.getValue().get(sd));
//         }
//    }

    @Test
    public void test() throws Exception  {
        JaxInfo ji = JaxInfo.build(LargerData.class);
        System.out.println(ji);
    }

}
