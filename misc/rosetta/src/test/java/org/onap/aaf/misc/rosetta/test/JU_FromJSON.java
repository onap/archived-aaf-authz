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

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.impl.EnvFactory;
import org.onap.aaf.misc.env.jaxb.JAXBmar;
import org.onap.aaf.misc.env.util.IndentPrintWriter;
import org.onap.aaf.misc.env.util.StringBuilderWriter;
import org.onap.aaf.misc.rosetta.InJson;
import org.onap.aaf.misc.rosetta.Out;
import org.onap.aaf.misc.rosetta.OutJson;
import org.onap.aaf.misc.rosetta.OutRaw;
import org.onap.aaf.misc.rosetta.OutXML;

import junit.framework.Assert;
import s.xsd.LargerData;
import s.xsd.SampleData;

public class JU_FromJSON {
    private static int ITERATIONS = 10000;
    static String str = "{\"SampleData\":[" +
                           "{\"id\":\"sd object \\\"1\\\"\",\"date\":1316084944213,\"item\":[\"Item 1.1\",\"Item 1.2\"]}," +
                           "{\"id\":\"sd object \\\"2\\\"\",\"date\":1316084945343,\"item\":[\"Item 2.1\",\"Item 2.2\"]}],\"fluff\":\"MyFluff\"}";
    InJson inJSON = new InJson();

    @Test
    public void rawParse() throws Exception {
        System.out.println("*** PARSE JSON -> RAW Dump ***");
        System.out.println(str);
        StringBuilderWriter sbw = new StringBuilderWriter();
        new OutRaw().extract(new StringReader(str),sbw,inJSON);
        System.out.println(sbw.getBuffer());
    }

    @Test
    public void parseJSON2Dump() throws Exception {
        System.out.println("*** PARSE JSON -> Dump ***");
        System.out.println(str);
        StringBuilderWriter sbw = new StringBuilderWriter(1024);

        new OutDump().extract(new StringReader(str), sbw, inJSON);

        System.out.println(sbw.getBuffer());
    }

    @Test
    public void nonprettyJSON() throws Exception {
        System.out.println("*** JSON -> (Intermediate Stream) -> Non-pretty JSON ***");
        System.out.println(str);
        StringBuilderWriter sbw = new StringBuilderWriter(1024);

        Out jout = new OutJson();
        Trans trans;
        Report report = new Report(ITERATIONS,"JSON");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            Reader sr = new StringReader(str);
            TimeTaken tt = trans.start("Parse JSON", Env.JSON);
            try {
                jout.extract(sr, sbw, inJSON);
            } finally {
                tt.done();
            }
            report.glean(trans,Env.JSON);
        } while (report.go());

        String result = sbw.toString();
        System.out.println(result);
        Assert.assertEquals(result, str);
        report.report(sbw);
        System.out.println(sbw.toString());
    }

    @Test
    public void parseJSON2JSON() throws Exception {
        System.out.println("*** JSON -> (Intermediate Stream) -> Pretty JSON ***");
        System.out.println(str);

        StringBuilderWriter sbw = new StringBuilderWriter(1024);

        Out jout = new OutJson();
        Trans trans;
        Report report = new Report(ITERATIONS,"JSON");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            Reader sr = new StringReader(str);
            TimeTaken tt = trans.start("Parse JSON", Env.JSON);
            try {
                jout.extract(sr, sbw, inJSON,true);
            } finally {
                tt.done();
            }
            report.glean(trans,Env.JSON);
        } while (report.go());

        report.report(sbw);
        System.out.println(sbw.toString());
    }

    @Test
    public void parseJSON2XML() throws Exception {
        System.out.println("*** PARSE JSON -> XML ***");
        System.out.println(str);

        StringBuilderWriter sbw = new StringBuilderWriter(1024);

        Out xout = new OutXML("LargerData","xmlns=urn:s:xsd");
        Trans trans;
        Report report = new Report(ITERATIONS,"JSON");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            Reader sr = new StringReader(str);
            TimeTaken tt = trans.start("Parse JSON", Env.JSON);
            try {
                xout.extract(sr, sbw, inJSON);
            } finally {
                tt.done();
            }
            report.glean(trans,Env.JSON);
        } while (report.go());

        report.report(sbw);
        System.out.println(sbw.toString());
    }

    @Test
    public void parseJSON2PrettyXML() throws Exception {
        System.out.println("*** PARSE JSON -> Pretty XML ***");
        System.out.println(str);

        StringBuilderWriter sbw = new StringBuilderWriter(1024);
        IndentPrintWriter ipw = new IndentPrintWriter(sbw);

        Out xout = new OutXML("LargerData","xmlns=urn:s:xsd");
        Trans trans;
        Report report = new Report(ITERATIONS,"JSON");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            Reader sr = new StringReader(str);
            TimeTaken tt = trans.start("Parse JSON", Env.JSON);
            try {
                xout.extract(sr, ipw, inJSON);
            } finally {
                tt.done();
            }
            report.glean(trans,Env.JSON);
        } while (report.go());

        report.report(sbw);
        System.out.println(sbw.toString());
    }


    @Test
    public void jaxbObj2XML() throws Exception {
        System.out.println("*** JAXB Object -> XML ***");

        LargerData ld = new LargerData();
        SampleData sd = new SampleData();
        sd.setDate(System.currentTimeMillis());
        sd.setId("sd object \"1\"");
        sd.getItem().add("Item 1.1");
        sd.getItem().add("Item 1.2");
        ld.getSampleData().add(sd);
        sd = new SampleData();
        sd.setDate(System.currentTimeMillis());
        sd.setId("sd object \"2\"");
        sd.getItem().add("Item 2.1");
        sd.getItem().add("Item 2.2");
        ld.getSampleData().add(sd);
        ld.setFluff("MyFluff");

        JAXBmar jaxBmar = new JAXBmar(LargerData.class);
        //jaxBmar.asFragment(true);
        //jaxBmar.pretty(true);
        StringBuilderWriter sbw = new StringBuilderWriter(1024);

        Trans trans;
        Report report = new Report(ITERATIONS,"XML");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            TimeTaken tt = trans.start("JAXB", Env.XML);
            try {
                jaxBmar.marshal(LogTarget.NULL, ld, sbw);
            } finally {
                tt.done();
            }
            report.glean(trans,Env.XML);
        } while (report.go());

        report.report(sbw);
        System.out.println(sbw.toString());
    }

    @Test
    public void jaxbObj2PrettyXML() throws Exception {
        System.out.println("*** JAXB Object -> Pretty XML ***");

        LargerData ld = new LargerData();
        SampleData sd = new SampleData();
        sd.setDate(System.currentTimeMillis());
        sd.setId("sd object \"1\"");
        sd.getItem().add("Item 1.1");
        sd.getItem().add("Item 1.2");
        ld.getSampleData().add(sd);
        sd = new SampleData();
        sd.setDate(System.currentTimeMillis());
        sd.setId("sd object \"2\"");
        sd.getItem().add("Item 2.1");
        sd.getItem().add("Item 2.2");
        ld.getSampleData().add(sd);
        ld.setFluff("MyFluff");

        JAXBmar jaxBmar = new JAXBmar(LargerData.class);
        //jaxBmar.asFragment(true);
        jaxBmar.pretty(true);
        StringBuilderWriter sbw = new StringBuilderWriter(1024);

        Trans trans;
        Report report = new Report(ITERATIONS,"XML");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            TimeTaken tt = trans.start("JAXB", Env.XML);
            try {
                jaxBmar.marshal(LogTarget.NULL, ld, sbw);
            } finally {
                tt.done();
            }
            report.glean(trans,Env.XML);
        } while (report.go());

        report.report(sbw);
        System.out.println(sbw.toString());
    }
}
