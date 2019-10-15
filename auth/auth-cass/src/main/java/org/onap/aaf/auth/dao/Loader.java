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

package org.onap.aaf.auth.dao;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.datastax.driver.core.Row;

public abstract class Loader<DATA> {
    private int keylimit;
    public Loader(int keylimit) {
        this.keylimit = keylimit;
    }

    public int keylimit() {
        return keylimit;
    }

    protected abstract DATA load(DATA data, Row row);
    protected abstract void key(DATA data, int idx, Object[] obj);
    protected abstract void body(DATA data, int idx, Object[] obj);

    public final Object[] extract(DATA data, int size, CassDAOImpl.CRUD type) {
        Object[] rv=null;
        switch(type) {
            case delete:
                rv = new Object[keylimit()];
                key(data,0,rv);
                break;
            case update:
                rv = new Object[size];
                body(data,0,rv);
                int body = size-keylimit();
                if (body>0) {
                    key(data,body,rv);
                }
                break;
            default:
                rv = new Object[size];
                key(data,0,rv);
                if (size>keylimit()) {
                    body(data,keylimit(),rv);
                }
                break;
        }
        return rv;
    }

    public static void writeString(DataOutputStream os, String s) throws IOException {
        if (s==null) {
            os.writeInt(-1);
        } else {
            switch(s.length()) {
                case 0:
                    os.writeInt(0);
                    break;
                default:
                    byte[] bytes = s.getBytes();
                    os.writeInt(bytes.length);
                    os.write(bytes);
            }
        }
    }


    /**
     * We use bytes here to set a Maximum
     * <p>
     * @param is
     * @param MAX
     * @return
     * @throws IOException
     */
    public static String readString(DataInputStream is, byte[] _buff) throws IOException {
        int l = is.readInt();
        byte[] buff = _buff;
        switch(l) {
            case -1: return null;
            case  0: return "";
            default:
                // Cover case where there is a large string, without always allocating a large buffer.
                if (l>buff.length) {
                    buff = new byte[l];
                }
                is.read(buff,0,l);
                return new String(buff,0,l);
        }
    }

    /**
     * Write a set with proper sizing
     * <p>
     * Note: at the moment, this is just String.  Probably can develop system where types
     * are supported too... but not now.
     * <p>
     * @param os
     * @param set
     * @throws IOException
     */
    public static void writeStringSet(DataOutputStream os, Collection<String> set) throws IOException {
        if (set==null) {
            os.writeInt(-1);
        } else {
            os.writeInt(set.size());
            for (String s : set) {
                writeString(os, s);
            }
        }

    }

    public static Set<String> readStringSet(DataInputStream is, byte[] buff) throws IOException {
        int l = is.readInt();
        if (l<0) {
            return null;
        }
        Set<String> set = new HashSet<>(l);
        for (int i=0;i<l;++i) {
            set.add(readString(is,buff));
        }
        return set;
    }

    public static List<String> readStringList(DataInputStream is, byte[] buff) throws IOException {
        int l = is.readInt();
        if (l<0) {
            return null;
        }
        List<String> list = new ArrayList<>(l);
        for (int i=0;i<l;++i) {
            list.add(Loader.readString(is,buff));
        }
        return list;
    }

    /** 
     * Write a map
     * @param os
     * @param map
     * @throws IOException
     */
    public static void writeStringMap(DataOutputStream os, Map<String,String> map) throws IOException {
        if (map==null) {
            os.writeInt(-1);
        } else {
            Set<Entry<String, String>> es = map.entrySet();
            os.writeInt(es.size());
            for (Entry<String,String> e : es) {
                writeString(os, e.getKey());
                writeString(os, e.getValue());
            }
        }

    }

    public static Map<String,String> readStringMap(DataInputStream is, byte[] buff) throws IOException {
        int l = is.readInt();
        if (l<0) {
            return null;
        }
        Map<String,String> map = new HashMap<>(l);
        for (int i=0;i<l;++i) {
            String key = readString(is,buff);
            map.put(key,readString(is,buff));
        }
        return map;
    }
    public static void writeHeader(DataOutputStream os, int magic, int version) throws IOException {
        os.writeInt(magic);
        os.writeInt(version);
    }

    public static int readHeader(DataInputStream is, final int magic, final int version) throws IOException {
        if (is.readInt()!=magic) {
            throw new IOException("Corrupted Data Stream");
        }
        int v = is.readInt();
        if (version<0 || v>version) {
            throw new IOException("Unsupported Data Version: " + v);
        }
        return v;
    }

}

