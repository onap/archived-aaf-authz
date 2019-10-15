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
import java.util.Date;
import java.util.List;

import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.Cached;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.DAOException;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.dao.Streamer;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;

public class UserRoleDAO extends CassDAOImpl<AuthzTrans,UserRoleDAO.Data> {
    public static final String TABLE = "user_role";

    public static final int CACHE_SEG = 0x40; // yields segment 0x0-0x3F

    private static final String TRANS_UR_SLOT = "_TRANS_UR_SLOT_";
    public Slot transURSlot;

    private final HistoryDAO historyDAO;
    private final CacheInfoDAO infoDAO;

    private PSInfo psByUser, psByRole, psUserInRole;



    public UserRoleDAO(AuthzTrans trans, Cluster cluster, String keyspace) throws APIException, IOException {
        super(trans, UserRoleDAO.class.getSimpleName(),cluster,keyspace,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        transURSlot = trans.slot(TRANS_UR_SLOT);
        init(trans);

        // Set up sub-DAOs
        historyDAO = new HistoryDAO(trans, this);
        infoDAO = new CacheInfoDAO(trans,this);
    }

    public UserRoleDAO(AuthzTrans trans, HistoryDAO hDAO, CacheInfoDAO ciDAO) {
        super(trans, UserRoleDAO.class.getSimpleName(),hDAO,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        transURSlot = trans.slot(TRANS_UR_SLOT);
        historyDAO = hDAO;
        infoDAO = ciDAO;
        init(trans);
    }

    private static final int KEYLIMIT = 2;
    public static class Data extends CacheableData implements Bytification {
        public String  user;
        public String  role;
        public String  ns; 
        public String  rname; 
        public Date   expires;
    
        @Override
        public int[] invalidate(Cached<?,?> cache) {
            // Note: I'm not worried about Name collisions, because the formats are different:
            // Jonathan... etc versus
            // com. ...
            // The "dot" makes the difference.
            return new int[] {
                seg(cache,user,role),
                seg(cache,user),
                seg(cache,role)
            };
        }

        @Override
        public ByteBuffer bytify() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            URLoader.deflt.marshal(this,new DataOutputStream(baos));
            return ByteBuffer.wrap(baos.toByteArray());
        }
    
        @Override
        public void reconstitute(ByteBuffer bb) throws IOException {
            URLoader.deflt.unmarshal(this, toDIS(bb));
        }

        public void role(String ns, String rname) {
            this.ns = ns;
            this.rname = rname;
            this.role = ns + '.' + rname;
        }
    
        public void role(RoleDAO.Data rdd) {
            ns = rdd.ns;
            rname = rdd.name;
            role = rdd.fullName();
        }

    
        public boolean role(AuthzTrans trans, Question ques, String role) {
            this.role = role;
            Result<NsSplit> rnss = ques.deriveNsSplit(trans, role);
            if (rnss.isOKhasData()) {
                ns = rnss.value.ns;
                rname = rnss.value.name;
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return user + '|' + ns + '|' +  rname + '|' + Chrono.dateStamp(expires);
        }
    }

    private static class URLoader extends Loader<Data> implements Streamer<Data> {
        public static final int MAGIC=738469903;
        public static final int VERSION=1;
        public static final int BUFF_SIZE=48;
    
        public static final URLoader deflt = new URLoader(KEYLIMIT);

        public URLoader(int keylimit) {
            super(keylimit);
        }

        @Override
        public Data load(Data data, Row row) {
            data.user = row.getString(0);
            data.role = row.getString(1);
            data.ns = row.getString(2);
            data.rname = row.getString(3);
            data.expires = row.getTimestamp(4);
            return data;
        }

        @Override
        protected void key(Data data, int _idx, Object[] obj) {
                int idx = _idx;
            obj[idx]=data.user;
            obj[++idx]=data.role;
        }

        @Override
        protected void body(Data data, int _idx, Object[] obj) {
                int idx = _idx;
            obj[idx]=data.ns;
            obj[++idx]=data.rname;
            obj[++idx]=data.expires;
        }
    
        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);

            writeString(os, data.user);
            writeString(os, data.role);
            writeString(os, data.ns);
            writeString(os, data.rname);
            os.writeLong(data.expires==null?-1:data.expires.getTime());
        }

        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
        
            byte[] buff = new byte[BUFF_SIZE];
            data.user = readString(is,buff);
            data.role = readString(is,buff);
            data.ns = readString(is,buff);
            data.rname = readString(is,buff);
            long l = is.readLong();
            data.expires = l<0?null:new Date(l);
        }

    };

    private void init(AuthzTrans trans) {
        String[] helper = setCRUD(trans, TABLE, Data.class, URLoader.deflt);
    
        psByUser = new PSInfo(trans, SELECT_SP + helper[FIELD_COMMAS] + " FROM user_role WHERE user = ?", 
            new URLoader(1) {
                @Override
                protected void key(Data data, int idx, Object[] obj) {
                    obj[idx]=data.user;
                }
            },readConsistency);
    
        // Note: We understand this call may have poor performance, so only should be used in Management (Delete) func
        psByRole = new PSInfo(trans, SELECT_SP + helper[FIELD_COMMAS] + " FROM user_role WHERE role = ? ALLOW FILTERING", 
                new URLoader(1) {
                    @Override
                    protected void key(Data data, int idx, Object[] obj) {
                        obj[idx]=data.role;
                    }
                },readConsistency);
    
        psUserInRole = new PSInfo(trans,SELECT_SP + helper[FIELD_COMMAS] + " FROM user_role WHERE user = ? AND role = ?",
                URLoader.deflt,readConsistency);
    }

    public Result<List<Data>> readByUser(AuthzTrans trans, String user) {
        return psByUser.read(trans, R_TEXT + " by User " + user, new Object[]{user});
    }

    /**
     * Note: Use Sparingly. Cassandra's forced key structure means this will perform fairly poorly
     * @param trans
     * @param role
     * @return
     * @throws DAOException
     */
    public Result<List<Data>> readByRole(AuthzTrans trans, String role) {
        return psByRole.read(trans, R_TEXT + " by Role " + role, new Object[]{role});
    }

    /**
     * Direct Lookup of User Role
     * Don't forget to check for Expiration
     */
    public Result<List<Data>> readByUserRole(AuthzTrans trans, String user, String role) {
        return psUserInRole.read(trans, R_TEXT + " by User " + user + " and Role " + role, new Object[]{user,role});
    }


    /**
     * Log Modification statements to History
     * @param modified           which CRUD action was done
     * @param data               entity data that needs a log entry
     * @param overrideMessage    if this is specified, we use it rather than crafting a history message based on data
     */
    @Override
    protected void wasModified(AuthzTrans trans, CRUD modified, Data data, String ... override) {
        boolean memo = override.length>0 && override[0]!=null;
        boolean subject = override.length>1 && override[1]!=null;

        HistoryDAO.Data hd = HistoryDAO.newInitedData();
        HistoryDAO.Data hdRole = HistoryDAO.newInitedData();
    
        hd.user = hdRole.user = trans.user();
        hd.action = modified.name();
        // Modifying User/Role is an Update to Role, not a Create.  Jonathan, 07-14-2015
        hdRole.action = CRUD.update.name();
        hd.target = TABLE;
        hdRole.target = RoleDAO.TABLE;
        hd.subject = subject?override[1] : (data.user + '|'+data.role);
        hdRole.subject = data.role;
        switch(modified) {
            case create: 
                hd.memo = hdRole.memo = memo
                    ? String.format("%s by %s", override[0], hd.user)
                    : String.format("%s added to %s",data.user,data.role);
                break;
            case update: 
                hd.memo = hdRole.memo = memo
                    ? String.format("%s by %s", override[0], hd.user)
                    : String.format("%s - %s was updated",data.user,data.role);
                break;
            case delete: 
                hd.memo = hdRole.memo = memo
                    ? String.format("%s by %s", override[0], hd.user)
                    : String.format("%s removed from %s",data.user,data.role);
                try {
                    hd.reconstruct = hdRole.reconstruct = data.bytify();
                } catch (IOException e) {
                    trans.warn().log(e,"Deleted UserRole could not be serialized");
                }
                break;
            default:
                hd.memo = hdRole.memo = memo
                ? String.format("%s by %s", override[0], hd.user)
                : "n/a";
        }

        if (historyDAO.create(trans, hd).status!=Status.OK) {
            trans.error().log("Cannot log to History");
        }
    
        if (historyDAO.create(trans, hdRole).status!=Status.OK) {
            trans.error().log("Cannot log to History");
        }
        // uses User as Segment
        if (infoDAO.touch(trans, TABLE,data.invalidate(cache)).notOK()) {
            trans.error().log("Cannot touch CacheInfo");
        }
    }
}
