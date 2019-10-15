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

package org.onap.aaf.cadi.configure;

import java.io.PrintStream;

import org.onap.aaf.misc.env.Trans;

import certman.v1_0.Artifacts.Artifact;
import certman.v1_0.CertInfo;

public class PlaceArtifactOnStream implements PlaceArtifact {
    private PrintStream out;

    public PlaceArtifactOnStream(PrintStream printStream) {
        out = printStream;
    }

    @Override
    public boolean place(Trans trans, CertInfo capi, Artifact a, String machine) {
        String lineSeparator = System.lineSeparator();

        if (capi.getNotes()!=null && capi.getNotes().length()>0) {
            trans.info().printf("Warning:    %s" + lineSeparator, capi.getNotes());
        }
        out.printf("Challenge:  %s" + lineSeparator, capi.getChallenge());
        out.printf("PrivateKey:" + lineSeparator + "%s" + lineSeparator, capi.getPrivatekey());
        out.println("Certificate Chain:");
        for (String c : capi.getCerts()) {
            out.println(c);
        }
        return true;
    }
}
