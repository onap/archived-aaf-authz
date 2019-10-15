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

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.impl.EnvFactory;
import org.onap.aaf.misc.env.util.StringBuilderWriter;
import org.onap.aaf.misc.rosetta.InJson;
import org.onap.aaf.misc.rosetta.JaxInfo;
import org.onap.aaf.misc.rosetta.OutJson;
import org.onap.aaf.misc.rosetta.OutXML;
import org.onap.aaf.misc.rosetta.Saved;

import s.xsd.LargerData;

public class JU_Saved<b> {
    private static int ITERATIONS = 100000;

    @Test
    public void test() throws Exception {
        InJson inJSON = new InJson();
        OutDump dump = new OutDump();
        JaxInfo ji = JaxInfo.build(LargerData.class);
        OutXML xml = new OutXML(ji);;
        OutJson json = new OutJson();
    
        Saved saved = new Saved();
    
        StringBuilderWriter sbw = new StringBuilderWriter(1024);
    
        Trans trans;
        Report report = new Report(ITERATIONS,"Save","Dump","XML ","JSON");
        do {
            sbw.reset();
            trans = EnvFactory.newTrans();
            Reader sr = new StringReader(JU_FromJSON.str);
            TimeTaken tt = trans.start("Parse Text, and Save", 1);
            try {
                saved.load(sr, inJSON);
            } finally {
                tt.done();
            }

//            sbw.append("==== Start Direct Raw =====\n");
//            new OutRaw().extract(new StringReader(JU_FromJSON.str), sbw, inJSON);
//        
//            sbw.append("==== Start Raw from Saved =====\n");
//            new OutRaw().extract(null,sbw,saved);

            sbw.append("==== Start Dump from Saved =====\n");
            tt = trans.start("Dump", 2);
            try {
                dump.extract(null,sbw,saved);
            } finally {
                tt.done();
            }
        
            sbw.append("\n==== Start XML =====\n");
            tt = trans.start("XML", 3);
            try {
                xml.extract(null,sbw,saved);
            } finally {
                tt.done();
            }
        
            sbw.append("\n==== Start JSON =====\n");
            tt = trans.start("JSON", 4);
            try {
                json.extract(null,sbw,saved);
            } finally {
                tt.done();
            }
            report.glean(trans,1,2,3,4);
        } while (report.go());
    
        report.report(sbw);
        System.out.println(sbw.toString());

    }
}
