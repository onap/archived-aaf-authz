/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018-19 IBM.
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.onap.aaf.auth.dao.AbsCassDAO;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.dao.Streamer;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

/**
 * CredDAO manages credentials.
 * @author Jonathan
 * Date: 6/25/18
 */
public class ConfigDAO extends CassDAOImpl<AuthzTrans,ConfigDAO.Data> {
    public static final String TABLE_NAME = "config";
    public static final int CACHE_SEG = 0x40; // yields segment 0x0-0x3F
    public static final int KEYLIMIT = 2;
    private PSInfo psName;

    public ConfigDAO(AuthzTrans trans, Cluster cluster, String keyspace) throws APIException, IOException {
        super(trans, ConfigDAO.class.getSimpleName(),cluster, keyspace, Data.class,TABLE_NAME, readConsistency(trans,TABLE_NAME), writeConsistency(trans,TABLE_NAME));
        init(trans);
    }

    public ConfigDAO(AuthzTrans trans, AbsCassDAO<AuthzTrans,?> aDao) throws APIException, IOException {
        super(trans, ConfigDAO.class.getSimpleName(),aDao, Data.class,TABLE_NAME, readConsistency(trans,TABLE_NAME), writeConsistency(trans,TABLE_NAME));
        init(trans);
    }

    public static class Data  {
        public String                    name;
        public String                    tag;
        public String                    value;
    }

    private static class ConfigLoader extends Loader<Data> implements Streamer<Data>{
        public static final int MAGIC=2673849;
        public static final int VERSION=1;
        public static final int BUFF_SIZE=48;

        public static final ConfigLoader deflt = new ConfigLoader(KEYLIMIT);
        public ConfigLoader(int keylimit) {
            super(keylimit);
        }

        @Override
        public Data load(Data data, Row row) {
            data.name = row.getString(0);
            data.tag = row.getString(1);
            data.value = row.getString(2);
            return data;
        }

        @Override
        protected void key(Data data, int idx, Object[] obj) {
            obj[idx] = data.name;
            obj[++idx] = data.tag;
        }

        @Override
        protected void body(Data data, int _idx, Object[] obj) {
            obj[_idx] = data.value;
        }

        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);
            writeString(os, data.name);
            writeString(os, data.tag);
            writeString(os, data.value);
        }

        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
            byte[] buff = new byte[BUFF_SIZE];
            data.name = readString(is,buff);
            data.tag = readString(is,buff);
            data.value = readString(is,buff);
        }
    }

    private void init(AuthzTrans trans) throws APIException, IOException {
        String[] helpers = setCRUD(trans, TABLE_NAME, Data.class, ConfigLoader.deflt);

        psName = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE_NAME +
                " WHERE name = ?", ConfigLoader.deflt,readConsistency);
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
        // not an auditable table.
    }

    public Result<List<Data>> readName(AuthzTrans trans, String name) {
        return psName.read(trans, R_TEXT, new Object[]{name});
    }


}
