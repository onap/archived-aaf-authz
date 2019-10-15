/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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
 */

package org.onap.aaf.auth.batch.helpers;

public class CQLBatchLoop {
    private static final int MAX_CHARS = (50 * 1024)/2;

    private final CQLBatch cqlBatch;
    private final int maxBatch;
    private final StringBuilder sb;
    private final boolean dryRun;
    private int i;
    private int total;
    private int batches;
    private final StringBuilder current;
    private boolean showProgress;

    public CQLBatchLoop(CQLBatch cb, int max, boolean dryRun) {
        cqlBatch = cb;
        i=0;
        total = 0;
        maxBatch = max;
        sb = cqlBatch.begin();
        current = new StringBuilder();
        this.dryRun = dryRun;
        showProgress = false;
    }

    public CQLBatchLoop showProgress() {
        showProgress = true;
        return this;
    }
    /**
     * Assume this is another line in the Batch
     * @return
     */
    public StringBuilder inc() {
        if((i>=maxBatch || current.length() + sb.length() > MAX_CHARS) && (i > 0)) {
        
                cqlBatch.execute(dryRun);
                i = -1;
                incBatch();
    }
        if(i < 0) {
            cqlBatch.begin();
            i = 0;
        }
        if(current.length() > MAX_CHARS) {
            cqlBatch.singleExec(current, dryRun);
        } else {
            sb.append(current);
        }
        current.setLength(0);
        ++i;
        ++total;
        return current;
    }

    /**
     * Close up when finished.
     */
    public void flush() {
        if(current.length() + sb.length() > MAX_CHARS) {
            if(i > 0) {
                cqlBatch.execute(dryRun);
                incBatch();
            }
            if(current.length() > 0) {
                cqlBatch.singleExec(current, dryRun);
                current.setLength(0);
                incBatch();
            }
        } else {
            if(i < 0) {
                cqlBatch.begin();
            }
            sb.append(current);
            current.setLength(0);
            cqlBatch.execute(dryRun);
            incBatch();
        }
        i = -1;
    }

    private void incBatch() {
        ++batches;
        if(showProgress) {
            System.out.print('.');
            if(batches%70 == 0) {
                System.out.println();
            } 
        }
    }

    public int total() {
        return total;
    }

    public int batches() {
        return batches;
    }

    public void reset() {
        total = 0;
        batches = 0;
        i = -1;
    }

    public String toString() {
        return cqlBatch.toString();
    }
}
