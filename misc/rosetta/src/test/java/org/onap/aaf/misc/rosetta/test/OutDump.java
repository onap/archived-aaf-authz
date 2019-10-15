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

import java.io.IOException;
import java.io.Writer;

import org.onap.aaf.misc.env.util.IndentPrintWriter;
import org.onap.aaf.misc.rosetta.Out;
import org.onap.aaf.misc.rosetta.Parse;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.Parsed;

public class OutDump extends Out{

    @Override
    public<IN, S> void extract(IN in, Writer writer, Parse<IN,S> prs, boolean ... options) throws IOException, ParseException {
        IndentPrintWriter ipw = writer instanceof IndentPrintWriter?(IndentPrintWriter)writer:new IndentPrintWriter(writer);

        Parsed<S> p = prs.newParsed();
    
        while ((p = prs.parse(in,p.reuse())).valid()) {
            switch(p.event) {
                case Parse.START_OBJ:
                    ipw.append("Start Object ");
                    ipw.append(p.name);
                    ipw.inc();
                    break;
                case Parse.END_OBJ: 
                    printData(ipw,p);
                    ipw.dec();
                    ipw.append("End Object ");
                    ipw.append(p.name);
                    break;
                case Parse.START_ARRAY:
                    ipw.inc();
                    ipw.append("Start Array ");
                    ipw.append(p.name);
                    ipw.append('\n');
                    break;
                case Parse.END_ARRAY: 
                    printData(ipw,p);
                    ipw.dec();
                    ipw.append("End Array ");
                    ipw.append('\n');
                    break;
                case Parse.NEXT:
                    printData(ipw,p);
                    break;
            }
        }
    }

    private void printData(IndentPrintWriter ipw, Parsed<?> parsed) {
        if (parsed.hasData()) {
            ipw.append("Data:[");
            if (parsed.hasName()) {
                ipw.append(parsed.name);
                ipw.append(" : "); 
            }
            ipw.append(parsed.sb);
            ipw.append("]");
            ipw.append('\n');
        }
    }

    @Override
    public String logName() {
        return "Rosetta OutDump";
    }

}
