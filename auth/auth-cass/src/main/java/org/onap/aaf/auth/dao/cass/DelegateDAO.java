/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ============================================================================
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
import java.util.List;

import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.dao.Streamer;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

public class DelegateDAO extends CassDAOImpl<AuthzTrans, DelegateDAO.Data> {

    public static final String TABLE = "delegate";
    private PSInfo psByDelegate;
    private static final int KEYLIMIT = 1;

    public DelegateDAO(AuthzTrans trans, Cluster cluster, String keyspace) {
        super(trans, DelegateDAO.class.getSimpleName(),cluster,keyspace,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        init(trans);
    }

    public DelegateDAO(AuthzTrans trans, AbsCassDAO<AuthzTrans,?> aDao) {
        super(trans, DelegateDAO.class.getSimpleName(),aDao,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        init(trans);
    }


    public static class Data implements Bytification {
        public String user;
        public String delegate;
        public Date expires;

        @Override
        public ByteBuffer bytify() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DelegateLoader.dflt.marshal(this,new DataOutputStream(baos));
            return ByteBuffer.wrap(baos.toByteArray());
        }

        @Override
        public void reconstitute(ByteBuffer bb) throws IOException {
            DelegateLoader.dflt.unmarshal(this, toDIS(bb));
        }
    }

    private static class DelegateLoader extends Loader<Data> implements Streamer<Data>{
        public static final int MAGIC=0xD823ACF2;
        public static final int VERSION=1;
        public static final int BUFF_SIZE=48;

        public static final DelegateLoader dflt = new DelegateLoader(KEYLIMIT);

        public DelegateLoader(int keylimit) {
            super(keylimit);
        }

        @Override
        public Data load(Data data, Row row) {
            data.user = row.getString(0);
            data.delegate = row.getString(1);
            data.expires = row.getTimestamp(2);
            return data;
        }

        @Override
        protected void key(Data data, int idx, Object[] obj) {
            obj[idx]=data.user;
        }

        @Override
        protected void body(Data data, int _idx, Object[] obj) {
                int idx = _idx;

            obj[idx]=data.delegate;
            obj[++idx]=data.expires;
        }

        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);
            writeString(os, data.user);
            writeString(os, data.delegate);
            os.writeLong(data.expires.getTime());
        }

        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
            byte[] buff = new byte[BUFF_SIZE];
            data.user = readString(is, buff);
            data.delegate = readString(is,buff);
            data.expires = new Date(is.readLong());
        }
    }

    private void init(AuthzTrans trans) {
        String[] helpers = setCRUD(trans, TABLE, Data.class, DelegateLoader.dflt);
        psByDelegate = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE +
                " WHERE delegate = ?", new DelegateLoader(1),readConsistency);

    }

    public Result<List<DelegateDAO.Data>> readByDelegate(AuthzTrans trans, String delegate) {
        return psByDelegate.read(trans, R_TEXT, new Object[]{delegate});
    }
}
