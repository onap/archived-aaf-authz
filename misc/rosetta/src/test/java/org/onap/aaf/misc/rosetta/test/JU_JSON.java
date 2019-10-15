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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;
import org.onap.aaf.misc.rosetta.InJson;
import org.onap.aaf.misc.rosetta.Out;
import org.onap.aaf.misc.rosetta.OutJson;
import org.onap.aaf.misc.rosetta.OutRaw;
import org.onap.aaf.misc.rosetta.Parse;
import org.onap.aaf.misc.rosetta.ParseException;

public class JU_JSON {

    @Test
    public void test() throws IOException, ParseException {
        InJson jin = new InJson();
        Out jout = new OutJson();

        go(jin, jout, "{\"id\":\"Me, Myself\",\"date\":1353094689100}");

        go(jin, jout, "{\"id\":\"My ID 1\",\"desc\":\"My Description 1\",\"comment\":[\"My Comment 1\"],\"utc\":1360418381310}");
        go(jin, jout, "{\"id\":\"My ID 1\",\"desc\":\"My Description 1\",\"comment\":[\"My Comment 1\",\"My Comment 2\"],\"utc\":1360418381310}");

        go(jin, jout, "{\"SampleData\":[" +
                   "{\"id\":\"sd object \\\"1\\\"\",\"date\":1316084944213,\"item\":[\"Item 1.1\",\"Item 1.2\"]}," +
                   "{\"id\":\"sd object \\\"2\\\"\",\"date\":1316084945343,\"item\":[\"Item 2.1\",\"Item 2.2\"]}],\"fluff\":\"MyFluff\"}"
                   );

        go(jin, jout, "{\"SampleData\":[{\"date\":1316084945343}],\"fluff\":\"MyFluff\"}");

        go(jin, jout, "{\"id\":\"Me,[}[eg[)(:x,\\\" Myself\",\"date\":1353094689100}");

        // TODO: Clean out AT&T specific data
        go(jin,jout, "{\"userid\":\"xk3233\",\"timestamp\":1353097388531,\"item\":[{\"tag\":\"color\",\"value\":\"Mauve\"},{\"tag\":\"shirtsize\",\"value\":\"Xtra Large\"}]}");
        //go()
        //"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><vote xmlns=\"urn:poll.att.com\"><userid>xk3233</userid><timestamp>1353082669667</timestamp></vote>");

        // 3/11/2015 Jonathan found a case with missing comma
        go(jin,jout, "{\"start\":\"2015-03-11T18:18:05.580-05:00\",\"end\":\"2015-09-11-05:00\",\"force\":\"false\",\"perm\":{\"type\":\"org.osaaf.myns.mytype\",\"instance\":\"myInstance\",\"action\":\"myAction\"}"
                + ",\"role\":\"org.osaaf.myns.myrole\"}");

        // 3/12/2015 Jonathan Kurt Schurenberg noticed an issue of object names in an array.  This is valid code.
        go(jin,jout, "{\"role\":[{\"name\":\"org.osaaf.myns.myrole\",\"perms\":[{\"type\":\"org.osaaf.myns.mytype\",\"instance\":\"myAction\"},{\"type\":\"org.osaaf.myns.mytype\",\"instance\":\"myOtherAction\"}]}"
                + ",{\"name\":\"org.osaaf.myns.myOtherRole\",\"perms\":[{\"type\":\"org.osaaf.myns.myOtherType\",\"instance\":\"myAction\"},{\"type\":\"org.osaaf.myns.myOthertype\",\"instance\":\"myOtherAction\"}]}]}");

        // 3/13/2015 - discovered with complex Response
        go(jin,jout, "{\"meth\":\"GET\",\"path\":\"/authz/perms/:type\",\"desc\":\"GetPermsByType\",\"comments\":[\"List All Permissions that match :type listed\"],"
                + "\"contentType\":[\"application/Permissions+json;q=1.0;charset=utf-8;version=1.1,application/json;q=1.0;version=1.1\""
                + ",\"application/Perms+xml;q=1.0;charset=utf-8;version=2.0,text/xml;q=1.0;version=2.0\",\"application/Perms+json;q=1.0;charset=utf-8;version=2.0,application/json;q=1.0;version=2.0,*/*;q=1.0\""
                + ",\"application/Permissions+xml;q=1.0;charset=utf-8;version=1.1,text/xml;q=1.0;version=1.1\"]}");


        // Test a Windoze "Pretty Print", validate skipping of Windoze characters as well as other odd control characters listed
        // in json.org
        StringWriter sw = new StringWriter();
        jout.extract(new StringReader(
                "{\b\f\n\r\t \"id\""
                + ":\"Me, \b\f\n\r\tMyself\",\"date\":1353094689100"
                + "\b\f\n\r\t }"
                ),sw,jin);
        Assert.assertEquals("{\"id\":\"Me, \b\f\n\r\tMyself\",\"date\":1353094689100}",sw.toString());
        System.out.println(sw.toString());

        // 10/01/2015 Jonathan AAF-703 Ron Gallagher, this response is ok
        go(jin,jout, "{\"perm\":[{\"type\":\"org.osaaf.myns.myPerm\",\"action\":\"myAction\",\"description\":\"something\"}]}");
        // but when description:"" causes extra comma at end
        go(jin,jout, "{\"perm\":[{\"type\":\"org.osaaf.myns.myPerm\",\"action\":\"myAction\",\"description\":\"\"}]}","{\"perm\":[{\"type\":\"org.osaaf.myns.myPerm\",\"action\":\"myAction\"}]}");
        // Test other empty string scenarios
         go(jin,jout, "{\"perm\":[{\"type\":\"\",\"action\":\"\",\"description\":\"\"}]}","{\"perm\":[{}]}");
         go(jin,jout, "{\"perm\":[{\"type\":\"\",\"action\":\"\",\"description\":\"hi\"}]}","{\"perm\":[{\"description\":\"hi\"}]}");
        go(jin,jout, "{\"perm\":[{\"type\":\"\",\"action\":\"myAction\",\"description\":\"\"}]}","{\"perm\":[{\"action\":\"myAction\"}]}");


        go(jin,jout, "{\"perm\":[{\"type\":\"org.osaaf.myns.myPerm\",\"action\":,\"description\":\"something\"}]}","{\"perm\":[{\"type\":\"org.osaaf.myns.myPerm\",\"description\":\"something\"}]}");

        go(jin, jout, "{\"name\":\"\\\"hello\\\"\"}");

        go(jin, jout, "{\"name\":\"\\\\\"}");

        go(jin, jout, "{\"role\":\"org.osaaf.scamper.UserStory0152 7_IT-00323-a-admin\",\"perm\":{\"type\":\"org.osaaf.scamper.application\",\"instance\":\"_()`!@#\\\\$%^=+][{}<>/.-valid.app.name-is_good\",\"action\":\"Administrator\"}}");


    }


    private void go(Parse<Reader,?> in, Out out, String str) throws IOException, ParseException {
        go(in,out,str,str);
    }


    private void go(Parse<Reader, ?> in, Out out, String str, String cmp) throws IOException, ParseException {

        System.out.println(str);
        StringWriter sw = new StringWriter(1024);
        out.extract(new StringReader(str), sw, in);
        System.out.println(sw);
        String result = sw.toString();

        if (!result.equals(cmp)) {
            sw.getBuffer().setLength(0);
            new OutRaw().extract(new StringReader(str), sw, in);
            System.out.println(sw);
        }

        Assert.assertEquals(cmp,result);
        System.out.println();

    }
}
