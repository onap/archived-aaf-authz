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

import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.Trans.Metric;

public class Report {
    float total;
    float buckets[];
    String[] names;
    private int iterations;
    private int count;

    public Report(int iters, String ... names) {
        iterations = iters;
        buckets = new float[names.length];
        this.names = names;
        total=0;
        count = 0;
    }

    public void glean(Trans trans, int ... type) {
        Metric m = trans.auditTrail(0, null, type);
        total+=m.total;
        int min = Math.min(buckets.length, m.buckets.length);
        for (int b=0;b<min;++b) {
            buckets[b]+=m.buckets[b];
        }
    }

    public boolean go() {
        return ++count<iterations;
    }


    public void report(Writer sbw) throws IOException {
        sbw.append("\n"+count + " entries, Total Time: " + total + "ms, Avg Time: " + total/count + "ms\n");
        int min = Math.min(buckets.length, names.length);
        for (int i=0;i<min;++i) {
            sbw.append("  Time: " + names[i] + ' ' + buckets[i] + "ms, Avg Time: " + buckets[i]/count + "ms\n");
        }

    }
}
