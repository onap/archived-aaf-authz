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

package org.onap.aaf.misc.rosetta;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

public class Nulls {
    public static final Parse<Reader, ?> IN = new Parse<Reader, Void>() {

        // @Override
        public Parsed<Void> parse(Reader r, Parsed<Void> parsed)throws ParseException {
            parsed.event = Parse.END_DOC;
            return parsed;
        }

        // @Override
        public Parsed<Void> newParsed() {
            Parsed<Void> parsed = new Parsed<Void>();
            parsed.event = Parse.END_DOC;
            return parsed;
        }

        // @Override
        public TimeTaken start(Env env) {
            return env.start("IN", Env.SUB);
        }
    
    };

    public static final Out OUT = new Out() {

        // @Override
        public <IN,S> void extract(IN in, Writer writer, Parse<IN, S> parse, boolean ... options)throws IOException, ParseException {
        }
        @Override
        public String logName() {
            return "Rosetta NULL";
        }


    };
}
