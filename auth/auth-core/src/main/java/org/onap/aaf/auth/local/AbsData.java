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
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.local.DataFile.Token;
import org.onap.aaf.auth.local.DataFile.Token.Field;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

public abstract class AbsData implements Iterable<String> {
    protected DataFile data;
    protected TextIndex ti;
    private File dataf,idxf,lockf;
    private String name;
    private char delim;
    private int maxLineSize;
    private int fieldOffset;
    private int skipLines;

    public AbsData(File dataf,char sepChar, int maxLineSize, int fieldOffset) {
        File dir = dataf.getParentFile();
        int dot = dataf.getName().lastIndexOf('.');
        name = dataf.getName().substring(0,dot);

        this.dataf=dataf;
        this.delim = sepChar;
        this.maxLineSize = maxLineSize;
        this.fieldOffset = fieldOffset;
        idxf = new File(dir,name.concat(".idx"));
        lockf = new File(dir,name.concat(".lock"));


        data = new DataFile(dataf,"r");
        ti = new TextIndex(idxf);
        skipLines=0;
    }

    public void skipLines(int lines) {
        skipLines=lines;
    }

    public String name() {
        return name;
    }

    public void open(AuthzTrans trans, long timeout) throws IOException {
        TimeTaken tt = trans.start("Open Data File", Env.SUB);
        boolean first = true;
        try {
                if (!dataf.exists()) {
                    throw new FileNotFoundException("Data File Missing:" + dataf.getCanonicalPath());
                }
                long begin = System.currentTimeMillis();
                long end = begin+timeout;
                boolean exists;
                while ((exists=lockf.exists()) && begin<end) {
                    if (first) {
                        trans.warn().log("Waiting for",lockf.getCanonicalPath(),"to close");
                        first = false;
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    begin = System.currentTimeMillis();
                }
                if (exists) {
                    throw new IOException(lockf.getCanonicalPath() + "exists.  May not open Datafile");
                }
                data.open();
                try {
                    ensureIdxGood(trans);
                } catch (IOException e) {
                    data.close();
                    throw e;
                }
                ti.open();

        } finally {
            tt.done();
        }
    }

    private synchronized void ensureIdxGood(AuthzTrans trans) throws IOException {
        if (!idxf.exists() || idxf.length()==0 || dataf.lastModified()>idxf.lastModified()) {
            trans.warn().log(idxf.getAbsolutePath(),"is missing, empty or out of date, creating");
            RandomAccessFile raf = new RandomAccessFile(lockf, "rw");
            try {
                ti.create(trans, data, maxLineSize, delim, fieldOffset, skipLines);
                if (!idxf.exists() || (idxf.length()==0 && dataf.length()!=0)) {
                    throw new IOException("Data Index File did not create correctly");
                }
            } finally {
                raf.close();
                lockf.delete();
            }
        }
    }

    public void close(AuthzTrans trans) throws IOException {
        ti.close();
        data.close();
    }

    public class Reuse {
        public Token tokenData;
        private Field fieldData;

        private Reuse(int size,char delim) {
            tokenData = data.new Token(size);
            fieldData = tokenData.new Field(delim);
        }

        public void reset() {
            getFieldData().reset();
        }

        public void pos(int rec) {
            getFieldData().reset();
            tokenData.pos(rec);
        }

        public String next() {
            return getFieldData().next();
        }

        public String at(int field) {
            return getFieldData().at(field);
        }

        public String atToEnd(int field) {
            return getFieldData().atToEnd(field);
        }

        public Field getFieldData() {
            return fieldData;
        }
    }

    public Reuse reuse() {
        return new Reuse(maxLineSize,delim);
    }

    public Iter iterator() {
        return new Iter();
    }

    public class Iter implements Iterator<String> {
        private Reuse reuse;
        private org.onap.aaf.auth.local.TextIndex.Iter tii;

        public Iter() {
            reuse = reuse();
            tii = ti.new Iter();
        }

        @Override
        public boolean hasNext() {
            return tii.hasNext();
        }

        @Override
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            reuse.reset();
            int rec = tii.next();
            reuse.pos(rec);
            return reuse.at(0);
        }

        @Override
        public void remove() {
            // read only
        }
    }
}
