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

package org.onap.aaf.misc.env.util;

import java.io.IOException;
import java.io.OutputStream;

public class DoubleOutputStream extends OutputStream {
    private OutputStream[] oss;
    private boolean[] close;

    /**
     * Create a Double Stream Writer
     * Some Streams should not be closed by this object (i.e. System.out), therefore, mark them with booleans
     */
    public DoubleOutputStream(OutputStream a, boolean closeA, OutputStream b, boolean closeB) {
        oss = new OutputStream[] {a,b};
        close = new boolean[] {closeA,closeB};
    }

    /**
     * Write a single character.
     * @throws IOException 
     */
    @Override
    public void write(int c) throws IOException {
        for(OutputStream os : oss) {
            os.write(c);
        }
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  bbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     * @throws IOException 
     */
    @Override
    public void write(byte bbuf[], int off, int len) throws IOException {
        for(OutputStream os : oss) {
            os.write(bbuf,off,len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        for(OutputStream os : oss) {
            os.write(b);
        }
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        for(int i=0;i<oss.length;++i) {
            if(close[i]) {
                oss[i].close();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.io.OutputStream#flush()
     */
    @Override
    public void flush() throws IOException {
        for(OutputStream os : oss) {
            os.flush();
        }
    }



}
