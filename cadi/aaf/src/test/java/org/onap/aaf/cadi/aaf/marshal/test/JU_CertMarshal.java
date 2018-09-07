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

package org.onap.aaf.cadi.aaf.marshal.test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.*;

import org.onap.aaf.cadi.aaf.marshal.CertMarshal;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.rosetta.OutRaw;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.marshal.DataWriter;

import aaf.v2_0.Certs.Cert;

public class JU_CertMarshal {

    private static final String fingerprint = "fingerprint";
    private static final String id = "id";
    private static final String x500 = "x500";

    private String fingerprintAsString;

    private XMLGregorianCalendar expires;

    private ByteArrayOutputStream outStream;

    @Before
    public void setup() {
        expires = Chrono.timeStamp();
        outStream = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();
        DataWriter.HEX_BINARY.write(fingerprint.getBytes(), sb);
        fingerprintAsString = sb.toString();
    }

    @Test
    public void test() throws ParseException, IOException {
        Cert cert = setupCert();
        CertMarshal cm = new CertMarshal();
        OutRaw raw = new OutRaw();

        raw.extract(cert, new PrintStream(outStream), cm);

        String[] output = outStream.toString().split("\n");

        String[] expected = new String[] {
                "{ - ",
                ", - fingerprint : \"" + fingerprintAsString + "\"",
                ", - id : \"" + id + "\"",
                ", - x500 : \"" + x500 + "\"",
                ", - expires : \"" + Chrono.dateTime(expires) + "\"",
                "} - ",
        };

        assertThat(output.length, is(expected.length));

        for (int i = 0; i < output.length; i++) {
            assertThat(output[i], is(expected[i]));
        }
    }

    private Cert setupCert() {
        Cert cert = new Cert();
        cert.setId(id);
        cert.setX500(x500);
        cert.setExpires(expires);
        cert.setFingerprint(fingerprint.getBytes());
        return cert;
    }

}
