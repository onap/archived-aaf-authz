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

import inherit.DerivedA;
import inherit.Root;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.DataFactory;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.impl.BasicEnv;
import org.onap.aaf.misc.rosetta.InJson;
import org.onap.aaf.misc.rosetta.InXML;
import org.onap.aaf.misc.rosetta.Out;
import org.onap.aaf.misc.rosetta.OutJson;
import org.onap.aaf.misc.rosetta.OutRaw;
import org.onap.aaf.misc.rosetta.OutXML;
import org.onap.aaf.misc.rosetta.Parse;
import org.onap.aaf.misc.rosetta.ParseException;

public class JU_Stream2Obj {

    /*
    <?xml version="1.0" encoding=Config.UTF-8 standalone="yes"?>
    <root xmlns="urn:inherit">
      <base xsi:type="derivedA" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <name>myDerivedA_1</name>
        <num>1432</num>
        <shortName>mda_1</shortName>
        <value>value_1</value>
        <value>value_2</value>
      </base>
    </root>

    {"base":[{"__extension":"derivedA","name":"myDerivedA_1","num":1432,"shortName":"mda_1","value":["value_1","value_2"]}]}
    */

    @Test
    public void json2Obj() throws APIException, SecurityException, NoSuchFieldException, ClassNotFoundException, ParseException, IOException {
        DerivedA da = new DerivedA();
        da.setName("myDerivedA_1");
        da.setNum((short)1432);
        da.setShortName("mda_1");
        da.getValue().add("value_1");
        da.getValue().add("value_2");
    
        Root root = new Root();
        root.getBase().add(da);

        da = new DerivedA();
        da.setName("myDerivedA_2");
        da.setNum((short)1432);
        da.setShortName("mda_2");
        da.getValue().add("value_2.1");
        da.getValue().add("value_2.2");
        root.getBase().add(da);
    
        EnvJAXB env = new BasicEnv();
        DataFactory<Root> rootDF = env.newDataFactory(Root.class);
    
        String xml = rootDF.newData(env).out(Data.TYPE.XML).load(root).option(Data.PRETTY).asString();
        System.out.println(xml);

        InXML inXML;
        Parse<Reader,?> in = inXML = new InXML(Root.class);
        Out out = new OutRaw();

        StringWriter sw = new StringWriter();
        out.extract(new StringReader(xml), sw, in);
        System.out.println(sw.toString());

    
        out = new OutJson();

        sw = new StringWriter();
        out.extract(new StringReader(xml), sw, in);
        String json;
        System.out.println(json = sw.toString());
    
        in = new InJson();
        out = new OutRaw();

        sw = new StringWriter();
        out.extract(new StringReader(json), sw, in);
        System.out.println(sw.toString());
    
        out = new OutXML(inXML);

        sw = new StringWriter();
        out.extract(new StringReader(json), sw, in, true);
        System.out.println(sw.toString());

        System.out.flush();

    }

}
