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

package org.onap.aaf.misc.rosetta.env;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.rosetta.Out;
import org.onap.aaf.misc.rosetta.Parse;
import org.onap.aaf.misc.rosetta.Saved;
import org.owasp.encoder.Encode;

public class RosettaData<T> implements Data<T>{
    private Env trans;
    private RosettaDF<T> df;
    private Saved saved;
    private TYPE inType, outType;
    // Note: This is an array of boolean in order to pass into other methods
    private boolean options[] = new boolean[] {false, false};
    // Temp Storage of XML.  Only when we must use JAXB to read in Objects
    private String xml,json;

    // package on purpose
    RosettaData(Env env, RosettaDF<T> rosettaDF) {
        df = rosettaDF;
        saved = new Saved(); // Note: Saved constructs storage as needed...
        trans = env;
        inType = df.getInType();
        outType = df.getOutType(); // take defaults
    }

    //Added for junits
    void setSaved(Saved saved) {
        this.saved = saved;
    }

//    // @Override
    public RosettaData<T> in(TYPE rosettaType) {
        inType = rosettaType;
        return this;
    }

//    // @Override
    public RosettaData<T> out(TYPE rosettaType) {
        outType = rosettaType;
        return this;
    }

//    // @Override
    public RosettaData<T> load(Reader rdr) throws APIException {
        Parse<Reader,?> in = df.getIn(inType);
        TimeTaken tt = in.start(trans);
        try {
            saved.extract(rdr, (Writer)null, in);
            xml=json=null;
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
        return this;
    }

    // @Override
    public RosettaData<T> load(InputStream is) throws APIException {
        Parse<Reader,?> in = df.getIn(inType);
        TimeTaken tt = in.start(trans);
        try {
            saved.extract(new InputStreamReader(is), (Writer)null, in);
            xml=json=null;
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
        return this;
    }

    // @Override
    public RosettaData<T> load(String str) throws APIException {
        Parse<Reader,?> in = df.getIn(inType);
        TimeTaken tt = in.start(trans);
        try {
            saved.extract(new StringReader(str), (Writer)null, in);
            switch(inType) {
                case XML:
                    xml = str;
                    break;
                case JSON:
                    json = str;
                    break;
                default:

                }
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
        return this;
    }

    // @Override
    public RosettaData<T> load(T t) throws APIException {
        Parse<?,?> in = df.getIn(inType);
        TimeTaken tt = in.start(trans);
        try {
            if (df.marshal==null) { // Unknown marshaller... do working XML marshal/extraction
                StringWriter sw = new StringWriter();
                df.jaxMar.marshal(trans.debug(), t, sw, options);
                saved.extract(new StringReader(xml = sw.toString()), (Writer)null, df.inXML);
            } else {
                saved.extract(t, (Writer)null, df.marshal);
            }
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
        return this;
    }

    public Saved getEvents() {
        return saved;
    }

    // @Override
    public T asObject() throws APIException {
        Out out = df.getOut(TYPE.XML);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            //TODO Replace JAXB with Direct Object method!!!
            StringWriter sw = new StringWriter();
            out.extract(null, sw, saved);
            return df.jaxUmar.unmarshal(trans.debug(), sw.toString());
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    // @Override
    public String asString() throws APIException {
        Out out = df.getOut(outType);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            if (outType==TYPE.XML) {
                if (xml==null) {
                    StringWriter sw = new StringWriter();
                    out.extract(null, sw, saved, options);
                    xml = sw.toString();
                }
                return xml;
            } else {  // is JSON
                if (json==null) {
                    StringWriter sw = new StringWriter();
                    out.extract(null, sw, saved, options);
                    json = sw.toString();
                }
                return json;
            }
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }


    // @Override
    public RosettaData<T> to(OutputStream os) throws APIException, IOException {
        Out out = df.getOut(outType);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            if (outType==TYPE.XML && xml!=null) {
                os.write(Encode.forJava(xml).getBytes());
            } else if (outType==TYPE.JSON && json!=null) {
            	os.write(Encode.forJava(json).getBytes());
            } else {
                out.extract(null, os, saved, options);
            }
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
        return this;
    }

    // @Override
    public RosettaData<T> to(Writer writer) throws APIException, IOException {
        Out out = df.getOut(outType);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            if (outType==TYPE.XML && xml!=null) {
                writer.append(xml);
            } else if (outType==TYPE.JSON && json!=null) {
                writer.append(json);
            } else {
                out.extract(null, writer, saved, options);
            }
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
        return this;
    }

    // @Override
    public Class<T> getTypeClass() {
        return df.getTypeClass();
    }

    private static final boolean[] emptyOption = new boolean[0];

    public void direct(InputStream is, OutputStream os) throws APIException, IOException {
        direct(is,os,emptyOption);
    }

    public void direct(Reader reader, Writer writer, boolean ... options) throws APIException, IOException {
        Parse<Reader,?> in = df.getIn(inType);
        Out out = df.getOut(outType);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            out.extract(reader, writer, in,options);
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    public void direct(T t, Writer writer, boolean ... options) throws APIException, IOException {
        Out out = df.getOut(outType);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            if (df.marshal==null) { // Unknown marshaller... do working XML marshal/extraction
                StringWriter sw = new StringWriter();
                df.jaxMar.marshal(trans.debug(), t, sw, options);
                out.extract(new StringReader(xml = sw.toString()), writer, df.inXML,options);
            } else {
                out.extract(t, writer, df.marshal,options);
            }
        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }

    public void direct(T t, OutputStream os, boolean ... options) throws APIException, IOException {
        Out out = df.getOut(outType);
        TimeTaken tt = trans.start(out.logName(),df.logType(outType)); // determine from Out.. without dependency on Env?
        try {
            if (df.marshal==null) { // Unknown marshaller... do working XML marshal/extraction
                if (outType.equals(TYPE.XML)) {
                    df.jaxMar.marshal(trans.debug(), t, os, options);
                } else {
                    StringWriter sw = new StringWriter();
                    df.jaxMar.marshal(trans.debug(), t, sw, options);
                    out.extract(new StringReader(xml = sw.toString()), new OutputStreamWriter(os), df.inXML,options);
                }
            } else {
                out.extract(t, new OutputStreamWriter(os), df.marshal,options);
            }

        } catch (Exception e) {
            throw new APIException(e);
        } finally {
            tt.done();
        }
    }


    public void direct(InputStream is, OutputStream os, boolean ... options) throws APIException, IOException {
        direct(new InputStreamReader(is),new OutputStreamWriter(os), options);
    }

    // // @Override
    public RosettaData<T> option(int option) {
        options[0] = (option&Data.PRETTY)==Data.PRETTY;
        options[1] = (option&Data.FRAGMENT)==Data.FRAGMENT;
        return this;
    }

}
