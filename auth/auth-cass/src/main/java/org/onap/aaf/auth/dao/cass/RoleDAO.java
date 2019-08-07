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

import org.onap.aaf.auth.dao.Bytification;
import org.onap.aaf.auth.dao.Cached;
import org.onap.aaf.auth.dao.CassAccess;
import org.onap.aaf.auth.dao.CassDAOImpl;
import org.onap.aaf.auth.dao.Loader;
import org.onap.aaf.auth.dao.Streamer;
import org.onap.aaf.auth.dao.hl.Question;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Split;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.DriverException;

public class RoleDAO extends CassDAOImpl<AuthzTrans,RoleDAO.Data> {

    public static final String TABLE = "role";
    public static final int CACHE_SEG = 0x40; // yields segment 0x0-0x3F
    
    private final HistoryDAO historyDAO;
    private final CacheInfoDAO infoDAO;

    private PSInfo psChildren, psNS, psName;

    public RoleDAO(AuthzTrans trans, Cluster cluster, String keyspace) throws APIException, IOException {
        super(trans, RoleDAO.class.getSimpleName(),cluster,keyspace,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        // Set up sub-DAOs
        historyDAO = new HistoryDAO(trans, this);
        infoDAO = new CacheInfoDAO(trans,this);
        init(trans);
    }

    public RoleDAO(AuthzTrans trans, HistoryDAO hDAO, CacheInfoDAO ciDAO) {
        super(trans, RoleDAO.class.getSimpleName(),hDAO,Data.class,TABLE, readConsistency(trans,TABLE), writeConsistency(trans,TABLE));
        historyDAO = hDAO;
        infoDAO = ciDAO;
        init(trans);
    }


    //////////////////////////////////////////
    // Data Definition, matches Cassandra DM
    //////////////////////////////////////////
    private static final int KEYLIMIT = 2;
    /**
     * Data class that matches the Cassandra Table "role"
     * @author Jonathan
     */
    public static class Data extends CacheableData implements Bytification {
        public String        ns;
        public String        name;
        public Set<String>  perms;
        public String        description;

        ////////////////////////////////////////
        // Getters
        public Set<String> perms(boolean mutable) {
            if (perms == null) {
                perms = new HashSet<>();
            } else if (mutable && !(perms instanceof HashSet)) {
                perms = new HashSet<>(perms);
            }
            return perms;
        }
        
        public static Data create(NsDAO.Data ns, String name) {
            NsSplit nss = new NsSplit(ns,name);        
            RoleDAO.Data rv = new Data();
            rv.ns = nss.ns;
            rv.name=nss.name;
            return rv;
        }
        
        public String fullName() {
            StringBuilder sb = new StringBuilder();
            if(ns==null) {
                sb.append('.');
            } else {
                sb.append(ns);
                sb.append(ns.indexOf('@')<0?'.':':'); 
            }
            sb.append(name);
            return sb.toString();
        }
        
        public String encode() {
            return ns + '|' + name;
        }
        
        /**
         * Decode Perm String, including breaking into appropriate Namespace
         * 
         * @param trans
         * @param q
         * @param r
         * @return
         */
        public static Result<Data> decode(AuthzTrans trans, Question q, String r) {
            Data data = new Data();
            if(r.indexOf('@')>=0) {
                int colon = r.indexOf(':');
                if(colon<0) {
                    return Result.err(Result.ERR_BadData, "%s is not a valid Role",r);
                } else {
                    data.ns=r.substring(0, colon);
                    data.name=r.substring(++colon);
                }
            } else {
                String[] ss = Split.splitTrim('|', r,2);
                if (ss[1]==null) { // older 1 part encoding must be evaluated for NS
                    Result<NsSplit> nss = q.deriveNsSplit(trans, ss[0]);
                    if (nss.notOK()) {
                        return Result.err(nss);
                    }
                    data.ns=nss.value.ns;
                    data.name=nss.value.name;
                } else { // new 4 part encoding
                    data.ns=ss[0];
                    data.name=ss[1];
                }
            }
            return Result.ok(data);
        }

        /**
         * Decode from UserRole Data
         * @param urdd
         * @return
         */
        public static RoleDAO.Data decode(UserRoleDAO.Data urdd) {
            RoleDAO.Data rd = new RoleDAO.Data();
            rd.ns = urdd.ns;
            rd.name = urdd.rname;
            return rd;
        }


        /**
         * Decode Perm String, including breaking into appropriate Namespace
         * 
         * @param trans
         * @param q
         * @param p
         * @return
         */
        public static Result<String[]> decodeToArray(AuthzTrans trans, Question q, String p) {
            String[] ss = Split.splitTrim('|', p,2);
            if (ss[1]==null) { // older 1 part encoding must be evaluated for NS
                Result<NsSplit> nss = q.deriveNsSplit(trans, ss[0]);
                if (nss.notOK()) {
                    return Result.err(nss);
                }
                ss[0] = nss.value.ns;
                ss[1] = nss.value.name;
            }
            return Result.ok(ss);
        }
        
        @Override
        public int[] invalidate(Cached<?,?> cache) {
            return new int[] {
                seg(cache,ns,name),
                seg(cache,ns),
                seg(cache,name),
            };
        }

        @Override
        public ByteBuffer bytify() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            RoleLoader.deflt.marshal(this,new DataOutputStream(baos));
            return ByteBuffer.wrap(baos.toByteArray());
        }
        
        @Override
        public void reconstitute(ByteBuffer bb) throws IOException {
            RoleLoader.deflt.unmarshal(this, toDIS(bb));
        }

        @Override
        public String toString() {
            return ns + '.' + name;
        }
    }

    private static class RoleLoader extends Loader<Data> implements Streamer<Data> {
        public static final int MAGIC=923577343;
        public static final int VERSION=1;
        public static final int BUFF_SIZE=96;

        public static final RoleLoader deflt = new RoleLoader(KEYLIMIT);
        
        public RoleLoader(int keylimit) {
            super(keylimit);
        }
        
        @Override
        public Data load(Data data, Row row) {
            // Int more efficient
            data.ns = row.getString(0);
            data.name = row.getString(1);
            data.perms = row.getSet(2,String.class);
            data.description = row.getString(3);
            return data;
        }

        @Override
        protected void key(Data data, int _idx, Object[] obj) {
                int idx = _idx;
            obj[idx]=data.ns;
            obj[++idx]=data.name;
        }

        @Override
        protected void body(Data data, int _idx, Object[] obj) {
                int idx = _idx;
            obj[idx]=data.perms;
            obj[++idx]=data.description;
        }

        @Override
        public void marshal(Data data, DataOutputStream os) throws IOException {
            writeHeader(os,MAGIC,VERSION);
            writeString(os, data.ns);
            writeString(os, data.name);
            writeStringSet(os,data.perms);
            writeString(os, data.description);
        }

        @Override
        public void unmarshal(Data data, DataInputStream is) throws IOException {
            /*int version = */readHeader(is,MAGIC,VERSION);
            // If Version Changes between Production runs, you'll need to do a switch Statement, and adequately read in fields
            byte[] buff = new byte[BUFF_SIZE];
            data.ns = readString(is, buff);
            data.name = readString(is,buff);
            data.perms = readStringSet(is,buff);
            data.description = readString(is,buff);
        }
    };

    private void init(AuthzTrans trans) {
        String[] helpers = setCRUD(trans, TABLE, Data.class, RoleLoader.deflt);
        
        psNS = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE +
                " WHERE ns = ?", new RoleLoader(1),readConsistency);

        psName = new PSInfo(trans, SELECT_SP + helpers[FIELD_COMMAS] + " FROM " + TABLE +
                " WHERE name = ?", new RoleLoader(1),readConsistency);

        psChildren = new PSInfo(trans, SELECT_SP +  helpers[FIELD_COMMAS] +  " FROM " + TABLE + 
                " WHERE ns=? AND name > ? AND name < ?", 
                new RoleLoader(3) {
            @Override
            protected void key(Data data, int _idx, Object[] obj) {
                    int idx = _idx;
                obj[idx] = data.ns;
                obj[++idx]=data.name + DOT;
                obj[++idx]=data.name + DOT_PLUS_ONE;
            }
        },readConsistency);
        
    }

    public Result<List<Data>> readNS(AuthzTrans trans, String ns) {
        return psNS.read(trans, R_TEXT + " NS " + ns, new Object[]{ns});
    }

    public Result<List<Data>> readName(AuthzTrans trans, String name) {
        return psName.read(trans, R_TEXT + name, new Object[]{name});
    }

    public Result<List<Data>> readChildren(AuthzTrans trans, String ns, String role) {
        if (role.length()==0 || "*".equals(role)) {
            return psChildren.read(trans, R_TEXT, new Object[]{ns, FIRST_CHAR, LAST_CHAR}); 
        } else {
            return psChildren.read(trans, R_TEXT, new Object[]{ns, role+DOT, role+DOT_PLUS_ONE});
        }
    }

    /**
     * Add a single Permission to the Role's Permission Collection
     * 
     * @param trans
     * @param role
     * @param perm
     * @param type
     * @param action
     * @return
     */
    public Result<Void> addPerm(AuthzTrans trans, RoleDAO.Data role, PermDAO.Data perm) {
        // Note: Prepared Statements for Collection updates aren't supported
        String pencode = perm.encode();
        try {
            getSession(trans).execute(UPDATE_SP + TABLE + " SET perms = perms + {'" + 
                pencode + "'} WHERE " +
                "ns = '" + role.ns + "' AND name = '" + role.name + "';");
        } catch (DriverException | APIException | IOException e) {
            reportPerhapsReset(trans,e);
            return Result.err(Result.ERR_Backend, CassAccess.ERR_ACCESS_MSG);
        }

        wasModified(trans, CRUD.update, role, "Added permission " + pencode + " to role " + role.fullName());
        return Result.ok();
    }

    /**
     * Remove a single Permission from the Role's Permission Collection
     * @param trans
     * @param role
     * @param perm
     * @param type
     * @param action
     * @return
     */
    public Result<Void> delPerm(AuthzTrans trans, RoleDAO.Data role, PermDAO.Data perm) {
        // Note: Prepared Statements for Collection updates aren't supported

        String pencode = perm.encode();
        
        //ResultSet rv =
        try {
            getSession(trans).execute(UPDATE_SP + TABLE + " SET perms = perms - {'" + 
                pencode    + "'} WHERE " +
                "ns = '" + role.ns + "' AND name = '" + role.name + "';");
        } catch (DriverException | APIException | IOException e) {
            reportPerhapsReset(trans,e);
            return Result.err(Result.ERR_Backend, CassAccess.ERR_ACCESS_MSG);
        }

        //TODO how can we tell when it doesn't?
        wasModified(trans, CRUD.update, role, "Removed permission " + pencode + " from role " + role.fullName() );
        return Result.ok();
    }
    
    /**
     * Add description to role
     * 
     * @param trans
     * @param ns
     * @param name
     * @param description
     * @return
     */
    public Result<Void> addDescription(AuthzTrans trans, String ns, String name, String description) {
        try {
            getSession(trans).execute(UPDATE_SP + TABLE + " SET description = '" 
                + description + "' WHERE ns = '" + ns + "' AND name = '" + name + "';");
        } catch (DriverException | APIException | IOException e) {
            reportPerhapsReset(trans,e);
            return Result.err(Result.ERR_Backend, CassAccess.ERR_ACCESS_MSG);
        }

        Data data = new Data();
        data.ns=ns;
        data.name=name;
        wasModified(trans, CRUD.update, data, "Added description " + description + " to role " + data.fullName(), null );
        return Result.ok();
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
        hd.user = trans.user();
        hd.action = modified.name();
        hd.target = TABLE;
        hd.subject = subject ? override[1] : data.fullName();
        hd.memo = memo ? override[0] : (data.fullName() + " was "  + modified.name() + 'd' );
        if (modified==CRUD.delete) {
            try {
                hd.reconstruct = data.bytify();
            } catch (IOException e) {
                trans.error().log(e,"Could not serialize RoleDAO.Data");
            }
        }

        if (historyDAO.create(trans, hd).status!=Status.OK) {
            trans.error().log("Cannot log to History");
        }
        if (infoDAO.touch(trans, TABLE,data.invalidate(cache)).notOK()) {
            trans.error().log("Cannot touch CacheInfo for Role");
        }
    }

    
}