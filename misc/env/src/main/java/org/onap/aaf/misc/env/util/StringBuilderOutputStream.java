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

public class StringBuilderOutputStream extends OutputStream {
    private StringBuilder buf;


    /**
     * Create a new string writer using the default initial string-buffer
     * size.
     */
    public StringBuilderOutputStream() {
    buf = new StringBuilder();
    }

    /**
     * Create a new string writer using a passed in StringBuilder
     * size.
     */
    public StringBuilderOutputStream(StringBuilder sb) {
    buf = sb;
    }

    /**
     * Create a new string writer using the specified initial string-buffer
     * size.
     *
     * @param initialSize
     *        The number of <tt>byte</tt> values that will fit into this buffer
     *        before it is automatically expanded
     *
     * @throws IllegalArgumentException
     *         If <tt>initialSize</tt> is negative
     */
    public StringBuilderOutputStream(int initialSize) {
    if (initialSize < 0) {
        throw new IllegalArgumentException("Negative buffer size");
    }
    buf = new StringBuilder(initialSize);
    }

    /**
     * Write a single character.
     */
    public void write(int c) {
    buf.append((byte) c);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param  bbuf  Array of characters
     * @param  off   Offset from which to start writing characters
     * @param  len   Number of characters to write
     */
    
    public void write(byte bbuf[], int off, int len) {
        if ((off < 0) || (off > bbuf.length) || (len < 0) ||
            ((off + len) > bbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buf.append(new String(bbuf, off, len));
    }

    @Override
    public void write(byte[] b) throws IOException {
        buf.append(new String(b));
    }

    /**
     * Write a string.
     */
    public void write(String str) {
        buf.append(str);
    }

    /**
     * Write a portion of a string.
     *
     * @param  str  String to be written
     * @param  off  Offset from which to start writing characters
     * @param  len  Number of characters to write
     */
    public void write(String str, int off, int len)  {
        buf.append(str,off,len);
    }

    public StringBuilderOutputStream append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            for(int i = 0;i<csq.length();++i) {
                buf.append(csq.charAt(i));
            }
        }
        return this;
    }

    public StringBuilderOutputStream append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        return append(cs.subSequence(start, end));
    }

    /**
     * Appends the specified character to this writer. 
     *
     * <p> An invocation of this method of the form <tt>out.append(c)</tt>
     * behaves in exactly the same way as the invocation
     *
     * <pre>
     *     out.write(c) </pre>
     *
     * @param  c
     *         The 16-bit character to append
     *
     * @return  This writer
     *
     * @since 1.5
     */
    public StringBuilderOutputStream append(byte c) {
        buf.append(c);
        return this;
    }

    /**
     * Return the buffer's current value as a string.
     */
    public String toString() {
        return buf.toString();
    }

    /**
     * Return the string buffer itself.
     *
     * @return StringBuffer holding the current buffer value.
     */
    public StringBuilder getBuffer() {
    return buf;
    }
    
    public void reset() {
        buf.setLength(0);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

}
