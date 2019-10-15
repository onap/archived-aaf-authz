/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.misc.rosetta.test;

import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.impl.EnvFactory;
import org.onap.aaf.misc.env.jaxb.JAXBmar;
import org.onap.aaf.misc.env.util.StringBuilderWriter;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

import s.xsd.LargerData;
import s.xsd.Multi;
import s.xsd.SampleData;

public class JU_RosettaDF {
    public static int ITERATIONS = 1;

    @Test
    public void testCached() throws Exception {
        RosettaEnv env = new RosettaEnv();
        RosettaDF<LargerData> df = env.newDataFactory(LargerData.class);
        JAXBmar jmar = new JAXBmar(LargerData.class);

        StringBuilderWriter sbw = new StringBuilderWriter(1024);
        Trans trans = EnvFactory.newTrans();

        Report report = new Report(ITERATIONS,"Load JSON","Extract JAXB", "JAXB Marshal", "Cached to XML", "Cached to JSON");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            Data<LargerData> data;
            TimeTaken tt = trans.start("Load JSON", 1);
            try {
                data = df.newData(trans).out(Data.TYPE.JSON).in(Data.TYPE.JSON).load(JU_FromJSON.str);
            } finally {
                tt.done();
            }
            LargerData ld;
            tt = trans.start("Extract JAXB", 2);
            try {
                ld = data.asObject();
            } finally {
                tt.done();
            }

            tt = trans.start("JAXB marshal", 3);
            try {
                jmar.marshal(trans.debug(), ld, sbw);
            } finally {
                tt.done();
            }
            sbw.append('\n');
        
            tt = trans.start("To XML from Cache",4);
            try {
                data.out(Data.TYPE.XML).to(sbw);
            } finally {
                tt.done();
            }
        
            sbw.append('\n');
        
            tt = trans.start("To JSON from Cache",5);
            try {
                data.out(Data.TYPE.JSON).to(sbw);
            } finally {
                tt.done();
            }
            report.glean(trans, 1,2,3,4,5);
        } while (report.go());
    
        report.report(sbw);
        System.out.println(sbw);
    
    }

    @Test
    public void testDirect() throws Exception {
        RosettaEnv env = new RosettaEnv();
        RosettaDF<LargerData> df = env.newDataFactory(LargerData.class);

        StringBuilderWriter sbw = new StringBuilderWriter(1024);
        Trans trans = EnvFactory.newTrans();

        Report report = new Report(ITERATIONS);
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            RosettaData<?> data = df.newData(trans).in(Data.TYPE.JSON).out(Data.TYPE.XML);
            data.direct(new StringReader(JU_FromJSON.str), sbw);
            report.glean(trans);
        } while (report.go());
    
        report.report(sbw);
        System.out.println(sbw);
    
    }

    @Test
    public void testMulti() throws Exception {
        RosettaEnv env = new RosettaEnv();
        RosettaDF<Multi> df = env.newDataFactory(Multi.class);

//        StringBuilderWriter sbw = new StringBuilderWriter(1024);
//        Trans trans = EnvFactory.newTrans();

        Multi m = new Multi();
        m.getF1().add("String1");
        m.getF2().add("String2");
    
        System.out.println(df.newData().load(m).out(TYPE.RAW).asString());
        System.out.println(df.newData().load(m).out(TYPE.JSON).asString());
    
    }

    @Test
    public void testQuotes() throws Exception {
        RosettaEnv env = new RosettaEnv();
        RosettaDF<SampleData> df = env.newDataFactory(SampleData.class);

        SampleData sd = new SampleData();
        sd.setId("\"AT&T Services, Inc.\"");
        System.out.println(sd.getId());
        String out =df.newData().load(sd).out(TYPE.JSON).asString();
        System.out.println(out);
        Assert.assertEquals(
                "{\"id\":\"\\\"AT&T Services, Inc.\\\"\",\"date\":0}",
                out);
    
        SampleData sd2 = df.newData().in(TYPE.JSON).load(out).asObject();
        System.out.println(sd2.getId());
        Assert.assertEquals(sd.getId(),sd2.getId());
    }
}
