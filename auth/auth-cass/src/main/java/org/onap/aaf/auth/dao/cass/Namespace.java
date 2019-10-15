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

package org.onap.aaf.auth.dao.cass;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.rserv.Pair;


public class Namespace implements Bytification {
    public static final int MAGIC=250935515;
    public static final int VERSION=1;
    public static final int BUFF_SIZE=48;

    public String name;
    public List<String> owner;
    public List<String> admin;
    public List<Pair<String,String>> attrib;
    public String description;
    public Integer type;
    public String parent;
    public Namespace() {}

    public Namespace(NsDAO.Data ndd) {
        name = ndd.name;
        description = ndd.description;
        type = ndd.type;
        parent = ndd.parent;
        if (ndd.attrib!=null && !ndd.attrib.isEmpty()) {
            attrib = new ArrayList<>();
            for ( Entry<String, String> entry : ndd.attrib.entrySet()) {
                attrib.add(new Pair<String,String>(entry.getKey(),entry.getValue()));
            }
        }
    }

    public Namespace(NsDAO.Data ndd,List<String> owner, List<String> admin) {
        name = ndd.name;
        this.owner = owner;
        this.admin = admin;
        description = ndd.description;
        type = ndd.type;
        parent = ndd.parent;
        if (ndd.attrib!=null && !ndd.attrib.isEmpty()) {
            attrib = new ArrayList<>();
            for ( Entry<String, String> entry : ndd.attrib.entrySet()) {
                attrib.add(new Pair<String,String>(entry.getKey(),entry.getValue()));
            }
        }
    }

    public NsDAO.Data data() {
        NsDAO.Data ndd = new NsDAO.Data();
        ndd.name = name;
        ndd.description = description;
        ndd.parent = parent;
        ndd.type = type;
        return ndd;
    }

    @Override
    public ByteBuffer bytify() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(baos);

        Loader.writeHeader(os,MAGIC,VERSION);
        Loader.writeString(os, name);
        os.writeInt(type);
        Loader.writeStringSet(os,admin);
        Loader.writeStringSet(os,owner);
        Loader.writeString(os,description);
        Loader.writeString(os,parent);

        return ByteBuffer.wrap(baos.toByteArray());
    }

    @Override
    public void reconstitute(ByteBuffer bb) throws IOException {
        DataInputStream is = CassDAOImpl.toDIS(bb);
        /*int version = */Loader.readHeader(is,MAGIC,VERSION);
        // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
    
        byte[] buff = new byte[BUFF_SIZE];
        name = Loader.readString(is, buff);
        type = is.readInt();
        admin = Loader.readStringList(is,buff);
        owner = Loader.readStringList(is,buff);
        description = Loader.readString(is,buff);
        parent = Loader.readString(is,buff);
    
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name.toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0==null || !(arg0 instanceof Namespace)) {
            return false;
        }
        return name.equals(((Namespace)arg0).name);
    }

}
