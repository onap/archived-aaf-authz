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

package org.onap.aaf.auth.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class DataFile {
    private RandomAccessFile rafile;
    private FileChannel channel;
    public MappedByteBuffer mapBuff;
    private final File file;
    private final String access;

    public DataFile(File file, String access)  {
        this.file = file;
        this.access = access;
    }
    public void open() throws IOException {
        if (!file.exists()) throw new FileNotFoundException();
        rafile = new RandomAccessFile(file,access);
        channel = rafile.getChannel();
        mapBuff = channel.map("r".equals(access)?MapMode.READ_ONLY:MapMode.READ_WRITE,0,channel.size());
    }
    public boolean isOpened() {
        return mapBuff!=null;
    }
    public void close() throws IOException {
        if (channel!=null){
            channel.close();
        }
        if (rafile!=null) {
            rafile.close();
        }
        mapBuff = null;
    }

    public long size() throws IOException {
        return channel==null?0:channel.size();
    }

    private synchronized int load(Token t) {
        int len = Math.min(mapBuff.limit()-t.next,t.buff.length);
        if (len>0) {
            mapBuff.position(t.next);
            mapBuff.get(t.buff,0,len);
        }
        return len<0?0:len;
    }

    public class Token {
        private byte[] buff;
        int pos, next, end;

        public Token(int size) {
            buff = new byte[size];
            pos = next = end = 0;
        }

        public boolean pos(int to) {
            pos = next = to;
            return (end=load(this))>0;
        }

        public boolean nextLine() {
            end = load(this);
            pos = next;
            for (int i=0;i<end;++i) {
                if (buff[i]=='\n') {
                    end = i;
                    next += i+1;
                    return true;
                }
            }
            return false;
        }

        public IntBuffer getIntBuffer() {
            return ByteBuffer.wrap(buff).asIntBuffer();
        }

        public String toString() {
            return new String(buff,0,end);
        }

        public class Field {
            char delim;
            int idx;
            ByteBuffer bb;

            public Field(char delimiter) {
                delim = delimiter;
                idx = 0;
                bb = null;
            }

            public Field reset() {
                idx = 0;
                return this;
            }

            public String next() {
                if (idx>=end)return null;
                int start = idx;
                byte c=0;
                int endStr = -1;
                while (idx<end && idx<buff.length && (c=buff[idx])!=delim && c!='\n') { // for DOS
                    if (c=='\r')endStr=idx;
                    ++idx;
                }

                if (endStr<0) {
                    endStr=idx-start;
                } else {
                    endStr=endStr-start;
                }
                ++idx;
                return new String(buff,start,endStr);
            }

            public String at(int fieldOffset) {
                int start;
                byte c=0;
                for (int count = idx = start = 0; idx<end && idx<buff.length; ++idx) {
                    if ((c=buff[idx])==delim || c=='\n') {
                        if (count++ == fieldOffset) {
                            break;
                        }
                        start = idx+1;
                    }
                }
                return new String(buff,start,(idx-start-(c=='\r'?1:0)));
            }

            public String atToEnd(int fieldOffset) {
                int start;
                byte c=0;
                for (int count = idx = start = 0; idx<end && idx<buff.length; ++idx) {
                    if ((c=buff[idx])==delim || c=='\n') {
                        if (count++ == fieldOffset) {
                            break;
                        }
                        start = idx+1;
                    }
                }

                for (; idx<end && idx<buff.length && (c=buff[idx])!='\n'; ++idx) {
                    ++idx;
                }
                return new String(buff,start,(idx-start-((c=='\r' || idx>=end)?1:0)));
            }

        }

        public int pos() {
            return pos;
        }
    }

    public File file() {
        return file;
    }

}
