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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.CIDAO;
import org.onap.aaf.auth.dao.Cached;
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
 * Date: 7/19/13
 */
public class CertDAO extends CassDAOImpl<AuthzTrans,CertDAO.Data> {
    public static final String TABLE = "x509";
    public static final int CACHE_SEG = 0x40; // yields segment 0x0-0x3F

    private HistoryDAO historyDAO;
    private CIDAO<AuthzTrans> infoDAO;
    private PSInfo psX500,psID;

    public CertDAO(AuthzTrans trans, Cluster cluster, String keyspace) throws APIException, IOException {
        super(trans, CertDAO.class.getSimpleName(),cluster, keyspace, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        init(trans);
    }

    public CertDAO(AuthzTrans trans, HistoryDAO hDao, CacheInfoDAO ciDao) throws APIException, IOException {
        super(trans, CertDAO.class.getSimpleName(),hDao, Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        historyDAO = hDao;
        infoDAO = ciDao;
        init(trans);
    }

    public static final int KEYLIMIT = 2;
    public static class Data extends CacheableData implements Bytification {
    
        public String                    ca;
        public BigInteger                 serial;
        public String                      id;
        public String                    x500;
        public String                    x509;

        @Override
        public int[] invalidate(Cached<?,?> cache) {
            return new int[] {
                seg(cache,ca,serial)
            };
        }
    
        @Override
        public ByteBuffer bytify() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CertLoader.deflt.marshal(this,new DataOutputStream(baos));
            return ByteBuffer.wrap(baos.toByteArray());
        }
    
        @Override
        public void reconstitute(ByteBuffer bb) throws IOException {
            CertLoader.deflt.unmarshal(this, toDIS(bb));
        }
    }

    private static class CertLoader extends Loader<Data> implements Streamer<Data>{
        public static final int MAGIC=85102934;
        public static final int VERSION=1;
        public static final int BUFF_SIZE=48; // Note: 

        public static final CertLoader deflt = new CertLoader(KEYLIMIT);
        public CertLoader(int keylimit) {
            super(keylimit);
        }

        @Override
        public Data load(Data data, Row row) {
            data.ca = row.getString(0);
            ByteBuffer bb = row.getBytesUnsafe(1);
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            data.serial = new BigInteger(bytes);
            data.id = row.getString(2);
            data.x500 = row.getString(3);
            data.x509 = row.getString(4);
            return data;
        }

        @Override
        protected void key(Data data, int idx, Object[] obj) {
            obj[idx] = data.ca;
            obj[++idx] = ByteBuffer.wrap(data.serial.toByteArray());
        }

        @Override
        protected void body(Data data, int _idx, Object[] obj) {
            int idx = _idx;

            obj[idx] = data.id;
            obj[++idx] = data.x500;
            obj[++idx] = data.x509;

        
        }

        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);
            writeString(os, data.id);
            writeString(os, data.x500);
            writeString(os, data.x509);
            writeString(os, data.ca);
            if (data.serial==null) {
                os.writeInt(-1);
            } else {
                byte[] dsba = data.serial.toByteArray();
                int l = dsba.length;
                os.writeInt(l);
                os.write(dsba,0,l);
            }
        }

        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
            byte[] buff = new byte[BUFF_SIZE];
            data.id = readString(is,buff);
            data.x500 = readString(is,buff);
            data.x509 = readString(is,buff);
            data.ca = readString(is,buff);
            int i = is.readInt();
            data.serial=null;
            if (i>=0) {
                byte[] bytes = new byte[i]; // a bit dangerous, but lessened because of all the previous sized data reads
                if (is.read(bytes)>0) {
                    data.serial = new BigInteger(bytes);
                }
            }
        }
    }

    public Result<List<CertDAO.Data>> read(AuthzTrans trans, Object ... key) {
        // Translate BigInteger to Byte array for lookup
        return super.read(trans, key[0],ByteBuffer.wrap(((BigInteger)key[1]).toByteArray()));
    }

    private void init(AuthzTrans trans) throws APIException, IOException {
        // Set up sub-DAOs
        if (historyDAO==null) {
            historyDAO = new HistoryDAO(trans,this);
        }
        if (infoDAO==null) {
            infoDAO = new CacheInfoDAO(trans,this);
        }

        String[] helpers = setCRUD(trans, TABLE, Data.class, CertLoader.deflt);

        psID = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE +
                " WHERE id = ?", CertLoader.deflt,readConsistency);

        psX500 = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE +
                " WHERE x500 = ?", CertLoader.deflt,readConsistency);
    
    }

    public Result<List<Data>> readX500(AuthzTrans trans, String x500) {
        return psX500.read(trans, R_TEXT, new Object[]{x500});
    }

    public Result<List<Data>> readID(AuthzTrans trans, String id) {
        return psID.read(trans, R_TEXT, new Object[]{id});
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
        boolean memo = override.length>0 && override[0]!=null;
        boolean subject = override.length>1 && override[1]!=null;

        HistoryDAO.Data hd = HistoryDAO.newInitedData();
        hd.user = trans.user();
        hd.action = modified.name();
        hd.target = TABLE;
        hd.subject = subject?override[1]: data.id;
        hd.memo = memo
                ? String.format("%s by %s", override[0], hd.user)
                : (modified.name() + "d certificate info for " + data.id);
        // Detail?
           if (modified==CRUD.delete) {
                    try {
                        hd.reconstruct = data.bytify();
                    } catch (IOException e) {
                        trans.error().log(e,"Could not serialize CertDAO.Data");
                    }
                }

        if (historyDAO.create(trans, hd).status!=Status.OK) {
            trans.error().log("Cannot log to History");
        }
        if (infoDAO.touch(trans, TABLE,data.invalidate(cache)).status!=Status.OK) {
            trans.error().log("Cannot touch Cert");
        }
    }
}
