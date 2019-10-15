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

package org.onap.aaf.auth.dao.cass;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.dao.Streamer;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

/**
 * LocateDAO manages credentials. 
 * @author Jonathan
 * Date: 10/11/17
 */
public class LocateDAO extends CassDAOImpl<AuthzTrans,LocateDAO.Data> {
    public static final String TABLE = "locate";
    private AbsCassDAO<AuthzTrans, Data>.PSInfo psName;

    public LocateDAO(AuthzTrans trans, Cluster cluster, String keyspace) throws APIException, IOException {
        super(trans, LocateDAO.class.getSimpleName(),cluster, keyspace, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        init(trans);
    }

    public LocateDAO(AuthzTrans trans, AbsCassDAO<AuthzTrans,?> adao) throws APIException, IOException {
        super(trans, LocateDAO.class.getSimpleName(), adao, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        init(trans);
    }

    public static final int KEYLIMIT = 3;
    public static class Data implements Bytification {
    
        public String                    name;
        public String                    hostname;
        public int                        port;
        public int                        major;
        public int                        minor;
        public int                        patch;
        public int                        pkg;
        public float                        latitude;
        public float                        longitude;
        public String                    protocol;
        private Set<String>                subprotocol;
        public UUID                        port_key; // Note: Keep Port_key LAST at all times, because we shorten the UPDATE to leave Port_key Alone during reregistration.

      // Getters
        public Set<String> subprotocol(boolean mutable) {
            if (subprotocol == null) {
                subprotocol = new HashSet<>();
            } else if (mutable && !(subprotocol instanceof HashSet)) {
                subprotocol = new HashSet<>(subprotocol);
            }
            return subprotocol;
        }
    
        @Override
        public ByteBuffer bytify() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            LocateLoader.deflt.marshal(this,new DataOutputStream(baos));
            return ByteBuffer.wrap(baos.toByteArray());
        }
    
        @Override
        public void reconstitute(ByteBuffer bb) throws IOException {
            LocateLoader.deflt.unmarshal(this, toDIS(bb));
        }

        public Data copy() {
            Data out = new Data();
            out.name = name;
            out.hostname = hostname;
            out.port = port;
            out.major = major;
            out.minor = minor;
            out.patch = patch;
            out.pkg = pkg;
            out.latitude = latitude;
            out.longitude = longitude;
            out.protocol = protocol;
            out.subprotocol = new HashSet<>();
            out.subprotocol.addAll(subprotocol);
            out.port_key = port_key;
            return out;
        }
    }

    private static class LocateLoader extends Loader<Data> implements Streamer<Data>{
        public static final int MAGIC=85102934;
            public static final int VERSION=1;
            public static final int BUFF_SIZE=48; // Note: 

            public static final LocateLoader deflt = new LocateLoader(KEYLIMIT);
            public LocateLoader(int keylimit) {
            super(keylimit);
        }

        @Override
        public Data load(Data data, Row row) {
                data.name = row.getString(0);
                data.hostname = row.getString(1);
                data.port = row.getInt(2);
                data.major = row.getInt(3);
                data.minor = row.getInt(4);
                data.patch = row.getInt(5);
                data.pkg = row.getInt(6);
                data.latitude = row.getFloat(7);
                data.longitude = row.getFloat(8);
                data.protocol = row.getString(9);
                data.subprotocol = row.getSet(10,String.class);
                data.port_key = row.getUUID(11);
            return data;
        }

        @Override
        protected void key(Data data, int idx, Object[] obj) {
            obj[idx] = data.name;
            obj[++idx] = data.hostname;
            obj[++idx] = data.port;
        }

        @Override
        protected void body(final Data data, final int _idx, final Object[] obj) {
                int idx = _idx;
            obj[idx] = data.major;
            obj[++idx] = data.minor;
            obj[++idx] = data.patch;
            obj[++idx] = data.pkg;
            obj[++idx] = data.latitude;
            obj[++idx] = data.longitude;
            obj[++idx] = data.protocol;
            obj[++idx] = data.subprotocol;
            obj[++idx] = data.port_key;
        }

        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);
            writeString(os, data.name);
            writeString(os, data.hostname);
            os.writeInt(data.port);
            os.writeInt(data.major);
            os.writeInt(data.minor);
            os.writeInt(data.patch);
            os.writeInt(data.pkg);
            os.writeFloat(data.latitude);
            os.writeFloat(data.longitude);
            writeString(os, data.protocol);
            if (data.subprotocol==null) {
                os.writeInt(0);
            } else {
                os.writeInt(data.subprotocol.size());
                for (String s: data.subprotocol) {
                    writeString(os,s);
                }
            }
        
            writeString(os,data.port_key==null?"":data.port_key.toString());
        }

        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
            byte[] buff = new byte[BUFF_SIZE];
            data.name = readString(is,buff);
            data.hostname = readString(is,buff);
            data.port = is.readInt();
            data.major = is.readInt();
            data.minor = is.readInt();
            data.patch = is.readInt();
            data.pkg = is.readInt();
            data.latitude = is.readFloat();
            data.longitude = is.readFloat();
            data.protocol = readString(is,buff);
        
            int size = is.readInt();
            data.subprotocol = new HashSet<>(size);
            for (int i=0;i<size;++i) {
                data.subprotocol.add(readString(is,buff));
            }
            String port_key = readString(is,buff);
            if (port_key.length()>0) {
                data.port_key=UUID.fromString(port_key);
            } else {
                data.port_key = null;
            }
        }
    }

    public Result<List<LocateDAO.Data>> readByName(AuthzTrans trans, String service) {
            return psName.read(trans, "Read By Name", new Object[] {service});
    }

    private void init(AuthzTrans trans) throws APIException, IOException {
        // Set up sub-DAOs
        String[] helpers = setCRUD(trans, TABLE, Data.class, LocateLoader.deflt);
//        int lastComma = helpers[ASSIGNMENT_COMMAS].lastIndexOf(',');
//        replace(CRUD.update,new PSInfo(trans,"UPDATE LOCATE SET " + helpers[ASSIGNMENT_COMMAS].substring(0, lastComma) +
//                " WHERE name=? AND hostname=? AND port=?;", new LocateLoader(3),writeConsistency));
        psName = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE +
                " WHERE name = ?", new LocateLoader(1),readConsistency);
    }

    /**
     * Log Modification statements to History
     *
     * @param modified        which CRUD action was done
     * @param data            entity data that needs a log entry
     * @param overrideMessage if this is specified, we use it rather than crafting a history message based on data
     */
    @Override
    protected void wasModified(AuthzTrans trans, CRUD modified, Data data, String ... override) {
    }
}
