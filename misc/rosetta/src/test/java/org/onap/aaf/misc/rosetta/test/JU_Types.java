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

import java.io.StringWriter;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.junit.Test;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.env.Trans.Metric;
import org.onap.aaf.misc.env.jaxb.JAXBmar;
import org.onap.aaf.misc.env.jaxb.JAXBumar;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.env.util.StringBuilderWriter;
import org.onap.aaf.misc.rosetta.OutJson;
import org.onap.aaf.misc.rosetta.OutRaw;
import org.onap.aaf.misc.rosetta.OutXML;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;
import org.onap.aaf.misc.rosetta.marshal.DocMarshal;
import org.onap.aaf.misc.rosetta.test.obj.MultiMarshal;
import org.onap.aaf.misc.rosetta.test.obj.SingleMarshal;

import types.xsd.Multi;
import types.xsd.Multi.Single;

public class JU_Types {

    @Test
    public void single() throws Exception {
        Single single = setSData();
        SingleMarshal psingle = new SingleMarshal();
    
        OutRaw raw = new OutRaw();
        OutJson json = new OutJson();
        OutXML xml = new OutXML("Single","xmlns=urn:types:xsd");
    
    
        System.out.println("===== RAW =====");
        raw.extract(single, System.out, psingle);

        System.out.println("\n===== JSON =====");
        json.extract(single, System.out, psingle);
    
        System.out.println("\n\n===== Pretty JSON =====");
        json.extract(single, System.out, psingle, true);

        System.out.println("\n\n===== XML =====");
        xml.extract(single, System.out, psingle,false);

        System.out.println("\n\n===== Pretty XML =====");
        xml.extract(single, System.out, psingle, true);

        RosettaEnv env = new RosettaEnv();
        StringWriter sw = new StringWriter();
        xml.extract(single, sw, psingle, true);
        JAXBumar jumar = new JAXBumar(single.getClass());
        JAXBmar jmar = new JAXBmar(new QName("Single","urn.types.xsd"),single.getClass());
        jmar.pretty(true);
        sw = new StringWriter();
        jmar.marshal(env.info(), single, sw);
        System.out.println(sw);
        Single news = jumar.unmarshal(env.info(), sw.toString());
//        System.out.println(news.getDatetime());
//        sw = new StringWriter();
//        news.setDatetime(Chrono.timeStamp());
//        xml.extract(single, sw, psingle, true);
        news = jumar.unmarshal(env.info(), sw.toString());
        System.out.println(sw.toString());
    
        String sample = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "\n<ns2:urn.types.xsd xmlns:ns2=\"Single\" xmlns=\"urn:types:xsd\">"
                + "\n<str>MyString</str>"
                + "\n<int>2147483647</int>"
                + "\n<long>9223372036854775807</long>"
                + "\n<date>2015-05-27-05:00</date>"
                + "\n<datetime>2015-05-27T07:05:04.234-05:00</datetime>"
                + "\n<binary>FF00FF0E082507807F</binary>"
                + "\n<array>String 1</array>"
                + "\n<array>String 2</array>"
                + "\n</ns2:urn.types.xsd>";
        System.out.println(sample);
        news = jumar.unmarshal(env.info(), sample);

        System.out.println(news.getDatetime());

    }

    @Test
    public void multi() throws Exception {
        OutRaw raw = new OutRaw();
        OutJson json = new OutJson();
        OutXML xml = new OutXML("Multi","xmlns=urn:types:xsd");

        Multi multi = new Multi();
        MultiMarshal pmulti = new MultiMarshal();

        for (int i=0;i<10;++i) {
            System.out.println("===== Multi Iteration " + i + " =====");
            if (i>0) {
                multi.getSingle().add(setSData());
            }
            System.out.println("  ===== RAW =====");
            raw.extract(multi, System.out, pmulti);
        
            System.out.println("\n  ===== JSON =====");
            json.extract(multi, System.out, pmulti);
        
            System.out.println("\n\n  ===== Pretty JSON =====");
            json.extract(multi, System.out, pmulti, true);

            System.out.println("\n\n  ===== XML =====");
            xml.extract(multi, System.out, pmulti,false);

            System.out.println("\n\n  ===== Pretty XML =====");
            xml.extract(multi, System.out, pmulti, true);
        }
    }

    @Test
    public void doc() throws Exception {
        OutRaw raw = new OutRaw();
        OutJson json = new OutJson();
        OutXML xml = new OutXML("Multi","xmlns=urn:types:xsd");

        Multi multi = new Multi();
        DocMarshal<Multi> doc = DocMarshal.root(new MultiMarshal());

        for (int i=0;i<3;++i) {
            System.out.println("===== Multi Iteration " + i + " =====");
            if (i>0) {
                multi.getSingle().add(setSData());
            }
            System.out.println("  ===== RAW =====");
            raw.extract(multi, System.out, doc);
        
            System.out.println("\n  ===== JSON =====");
            json.extract(multi, System.out, doc);
        
            System.out.println("\n\n  ===== Pretty JSON =====");
            json.extract(multi, System.out, doc, true);

            System.out.println("\n\n  ===== XML =====");
            xml.extract(multi, System.out, doc,false);

            System.out.println("\n\n  ===== Pretty XML =====");
            xml.extract(multi, System.out, doc, true);
        }
    }


//    @Test
//    public void saved() throws Exception {
//        Saved saved = new Saved();
//        saved.extract(in, ignore, parser, options);
//    }

    @Test
    public void df() throws Exception {
        RosettaEnv env = new RosettaEnv();
        RosettaDF<Multi> df = env.newDataFactory(Multi.class);
        df.out(TYPE.JSON).option(Data.PRETTY);
    
        Multi multi = new Multi();
        multi.getSingle().add(setSData());
    

        System.out.println("========== Original loading");
        Trans trans = env.newTrans();
        RosettaData<Multi> data = df.newData(trans);
        // Prime pump
        for (int i=0;i<100;++i) {
            data.load(multi);
        }
        trans = env.newTrans();
        data = df.newData(trans);
    
        int iters = 10000;
        for (int i=0;i<iters;++i) {
            data.load(multi);
        }
        Metric metrics = trans.auditTrail(0, null,Env.JSON,Env.XML);
        System.out.println(data.asString());
        System.out.println(metrics.total/iters + "ms avg");

        System.out.println("========== New loading");
        // With new
        df.rootMarshal(DocMarshal.root(new MultiMarshal()));
        trans = env.newTrans();
        data = df.newData(trans);

        // Prime pump
        for (int i=0;i<100;++i) {
            data.load(multi);
        }
        trans = env.newTrans();
        data = df.newData(trans);
    
        for (int i=0;i<iters;++i) {
            data.load(multi);
        }
        metrics = trans.auditTrail(0, null,Env.JSON,Env.XML);
        System.out.println(data.asString());
        System.out.println(metrics.total/iters + "ms avg");
    
        // Assert.assertEquals(first, second);

        System.out.println("========== Direct Object to JSON String");
        trans = env.newTrans();
        data = df.newData(trans);
        StringBuilderWriter sbw = new StringBuilderWriter(256);
        // Prime pump
        for (int i=0;i<100;++i) {
            sbw.reset();
            data.direct(multi, sbw, true);
        }
        trans = env.newTrans();
        data = df.newData(trans);

        for (int i=0;i<iters;++i) {
            sbw.reset();
            data.direct(multi, sbw, true);
        }
    
        metrics = trans.auditTrail(0, null,Env.JSON,Env.XML);
        System.out.println(sbw.toString());
        System.out.println(metrics.total/iters + "ms avg");
    
    }

    private Single setSData() {
        Single s = new Single();
        s.setStr("MyString");
        s.setInt(Integer.MAX_VALUE);
        s.setLong(Long.MAX_VALUE);
        XMLGregorianCalendar ts = Chrono.timeStamp();
        s.setDate(ts);
        s.setDatetime(ts);
        byte[] bytes= new byte[] {-1,0,(byte)0XFF,0xE,0x8,0x25,0x7,Byte.MIN_VALUE,Byte.MAX_VALUE};
        s.setBinary(bytes);
        s.getArray().add("String 1");
        s.getArray().add("String 2");
        return s;
    }

//    @Test
//    public void jsonInOut() throws IOException, ParseException {
//        Parse<?> jin = new InJson();
//        Out jout = new OutJson();
//
////        go(jin, jout, "{\"id\":\"Me, Myself\",\"date\":1353094689100}");
//        
//    }


    /*
    private void go(Parse<Reader,?> in, Out out, String str) throws IOException, ParseException {
    
        System.out.println(str);
        StringWriter sw = new StringWriter(1024);
        out.extract(new StringReader(str), sw, in);
        System.out.println(sw);
        String result = sw.toString();
    
        if (!result.equals(str)) {
            sw.getBuffer().setLength(0);
            new OutRaw().extract(new StringReader(str), sw, in);
            System.out.println(sw);
        }

        Assert.assertEquals(str,result);
        System.out.println();

    }
    */
}
