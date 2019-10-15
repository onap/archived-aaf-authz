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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.dao.Streamer;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

/**
 * CredDAO manages credentials.
 * @author Jonathan
 * Date: 7/19/13
 */
public class OAuthTokenDAO extends CassDAOImpl<AuthzTrans,OAuthTokenDAO.Data> {
    public static final String TABLE = "oauth_token";
    private AbsCassDAO<AuthzTrans, Data>.PSInfo psByUser;

    public OAuthTokenDAO(AuthzTrans trans, Cluster cluster, String keyspace) {
        super(trans, OAuthTokenDAO.class.getSimpleName(),cluster, keyspace, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        init(trans);
    }

    public OAuthTokenDAO(AuthzTrans trans, AbsCassDAO<AuthzTrans,?> aDao) {
            super(trans, OAuthTokenDAO.class.getSimpleName(),aDao, Data.class, TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
            init(trans);
    }


    public static final int KEYLIMIT = 1;
    public static class Data implements Bytification {
        public String                       id;
        public String                    client_id;
        public String                    user;
        public boolean                    active;
        public int                        type;
        public String                    refresh;
        public Date                      expires;
        public long                        exp_sec;
        public String                     content;
        public Set<String>                  scopes;
        public String                    state;
        public String                    req_ip; // requesting

        public Set<String> scopes(boolean mutable) {
            if (scopes == null) {
                scopes = new HashSet<>();
            } else if (mutable && !(scopes instanceof HashSet)) {
                scopes = new HashSet<>(scopes);
            }
            return scopes;
        }

        @Override
        public ByteBuffer bytify() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OAuthLoader.deflt.marshal(this,new DataOutputStream(baos));
            return ByteBuffer.wrap(baos.toByteArray());
        }

        @Override
        public void reconstitute(ByteBuffer bb) throws IOException {
            OAuthLoader.deflt.unmarshal(this, toDIS(bb));
        }

        public String toString() {
            return user.toString() + ' ' + id.toString() + ' ' + Chrono.dateTime(expires) + (active?"":"in") + "active";
        }
    }

    private static class OAuthLoader extends Loader<Data> implements Streamer<Data>{
        public static final int MAGIC=235677843;
            public static final int VERSION=1;
            public static final int BUFF_SIZE=96; // Note: only used when

            public static final OAuthLoader deflt = new OAuthLoader(KEYLIMIT);
            public OAuthLoader(int keylimit) {
                super(keylimit);
            }

            @Override
        public Data load(Data data, Row row) {
            data.id = row.getString(0);
            data.client_id = row.getString(1);
            data.user = row.getString(2);
            data.active = row.getBool(3);
            data.type = row.getInt(4);
            data.refresh = row.getString(5);
            data.expires = row.getTimestamp(6);
            data.exp_sec = row.getLong(7);
            data.content = row.getString(8);
            data.scopes = row.getSet(9,String.class);
            data.state = row.getString(10);
            data.req_ip = row.getString(11);
            return data;
        }

        @Override
        protected void key(final Data data, final int idx, Object[] obj) {
            obj[idx] = data.id;
        }

        @Override
        protected void body(final Data data, final int idx, Object[] obj) {
            int i;
            obj[i=idx] = data.client_id;
            obj[++i] = data.user;
            obj[++i] = data.active;
            obj[++i] = data.type;
            obj[++i] = data.refresh;
            obj[++i] = data.expires;
            obj[++i] = data.exp_sec;
            obj[++i] = data.content;
            obj[++i] = data.scopes;
            obj[++i] = data.state;
            obj[++i] = data.req_ip;
        }

        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);
            writeString(os, data.id);
            writeString(os, data.client_id);
            writeString(os, data.user);
            os.writeBoolean(data.active);
            os.writeInt(data.type);
            writeString(os, data.refresh);
            os.writeLong(data.expires==null?-1:data.expires.getTime());
            os.writeLong(data.exp_sec);
            writeString(os, data.content);
            writeStringSet(os,data.scopes);
            writeString(os, data.state);
            writeString(os, data.req_ip);
        }


        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
            byte[] buff = new byte[BUFF_SIZE]; // used only if fits
            data.id = readString(is,buff);
            data.client_id = readString(is,buff);
            data.user = readString(is,buff);
            data.active = is.readBoolean();
            data.type = is.readInt();
            data.refresh = readString(is,buff);
            long l = is.readLong();
            data.expires = l<0?null:new Date(l);
            data.exp_sec = is.readLong();
            data.content = readString(is,buff); // note, large strings still ok with small buffer
            data.scopes = readStringSet(is,buff);
            data.state = readString(is,buff);
            data.req_ip = readString(is,buff);
        }
    }

    private void init(AuthzTrans trans) {
        String[] helpers = setCRUD(trans, TABLE, Data.class, OAuthLoader.deflt);
        psByUser = new PSInfo(trans, "SELECT " + helpers[0] + " from " + TABLE + " WHERE user=?",OAuthLoader.deflt,readConsistency);
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

    public Result<List<Data>> readByUser(AuthzTrans trans, String user) {
        return psByUser.read(trans, "Read By User", new Object[]{user});
    }
}
