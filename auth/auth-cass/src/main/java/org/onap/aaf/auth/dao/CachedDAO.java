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

import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.Trans;

/**
 * CachedDAO
 * <p>
 * Cache the response of "get" of any DAO.  
 * <p>
 * For simplicity's sake, at this time, we only do this for single Object keys  
 * <p>
 * @author Jonathan
 *
 * @param <DATA>
 */
public class CachedDAO<TRANS extends Trans,D extends DAO<TRANS,DATA>,DATA extends Cacheable> 
        extends Cached<TRANS,DATA> implements DAO_RO<TRANS,DATA>{
//    private final String dirty_str; 

    private final D dao;

    public CachedDAO(D dao, CIDAO<TRANS> info, int segsize, long expireIn) {
        super(info, dao.table(), segsize, expireIn);
    
        // Instantiate a new Cache per DAO name (so separate instances use the same cache) 
        this.dao = dao;
        //read_str = "Cached READ for " + dao.table();
//        dirty_str = "Cache DIRTY on " + dao.table();
        if (dao instanceof CassDAOImpl) {
            ((CassDAOImpl<?,?>)dao).cache = this;
        }
    }

    public static<T extends Trans, DA extends DAO<T,DT>, DT extends Cacheable> 
            CachedDAO<T,DA,DT> create(DA dao, CIDAO<T> info, int segsize, long expireIn) {
        return new CachedDAO<T,DA,DT>(dao,info, segsize, expireIn);
    }

    public void add(DATA data)  {
        String key = keyFromObjs(dao.keyFrom(data));
        List<DATA> list = new ArrayList<>();
        list.add(data);
        super.add(key,list);
    }

//    public void invalidate(TRANS trans, Object ... objs)  {
//        TimeTaken tt = trans.start(dirty_str, Env.SUB);
//        try {
//            super.invalidate(keyFromObjs(objs));
//        } finally {
//            tt.done();
//        }
//    }

    public static String keyFromObjs(Object ... objs) {
        String key;
        if (objs.length==1 && objs[0] instanceof String) {
            key = (String)objs[0];
        } else {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : objs) {
                if (o!=null) {
                    if (first) {
                        first =false;
                    } else {
                        sb.append('|');
                    }
                    sb.append(o.toString());
                }
            }
            key = sb.toString();
        }
        return key;
    }

    public Result<DATA> create(TRANS trans, DATA data) {
        Result<DATA> d = dao.create(trans,data);
        if (d.status==Status.OK) {
            add(d.value);
        } else {
            trans.error().log(d.errorString());
        }
        // dao.create already modifies cache. Do not invalidate again. invalidate(trans,data);
        return d;
    }

    protected class DAOGetter implements Getter<DATA> {
        protected TRANS trans;
        protected Object objs[];
        protected D dao;
        public Result<List<DATA>> result;

        public DAOGetter(TRANS trans, D dao, Object ... objs) {
            this.trans = trans;
            this.dao = dao;
            this.objs = objs;
        }
    
        /**
         * Separated into single call for easy overloading
         * @return
         */
        public Result<List<DATA>> call() {
            return dao.read(trans, objs);
        }
    
        @Override
        public final Result<List<DATA>> get() {
            return call();
//            if (result.isOKhasData()) { // Note, given above logic, could exist, but stale
//                return result.value;
//            } else {
//                return null;
//            }
        }
    }

    @Override
    public Result<List<DATA>> read(final TRANS trans, final Object ... objs) {
        DAOGetter getter = new DAOGetter(trans,dao,objs); 
        return get(trans, keyFromObjs(objs),getter);
//        if (ld!=null) {
//            return Result.ok(ld);//.emptyList(ld.isEmpty());
//        }
//        // Result Result if exists
//        if (getter.result==null) {
//            return Result.err(Status.ERR_NotFound, "No Cache or Lookup found on [%s]",dao.table());
//        }
//        return getter.result;
    }

    // Slight Improved performance available when String and Obj versions are known. 
    public Result<List<DATA>> read(final String key, final TRANS trans, final Object[] objs) {
        DAOGetter getter = new DAOGetter(trans,dao,objs); 
        return get(trans, key, getter);
//        if (ld!=null) {
//            return Result.ok(ld);//.emptyList(ld.isEmpty());
//        }
//        // Result Result if exists
//        if (getter.result==null) {
//            return Result.err(Status.ERR_NotFound, "No Cache or Lookup found on [%s]",dao.table());
//        }
//        return getter.result;
    }

    @Override
    public Result<List<DATA>> read(TRANS trans, DATA data) {
        return read(trans,dao.keyFrom(data));
    }
    public Result<Void> update(TRANS trans, DATA data) {
        Result<Void> d = dao.update(trans, data);
        if (d.status==Status.OK) {
            add(data);
        } else {
            trans.error().log(d.errorString());
        }
        return d;
    }

    public Result<Void> delete(TRANS trans, DATA data, boolean reread) {
        if (reread) { // If reread, get from Cache, if possible, not DB exclusively
            Result<List<DATA>> rd = read(trans,data);
            if (rd.notOK()) {
                return Result.err(rd);
//            } else {
//                trans.error().log(rd.errorString());
            }
            if (rd.isEmpty()) {
                data.invalidate(this);
                return Result.err(Status.ERR_NotFound,"Not Found");
            }
            data = rd.value.get(0);
        }
        Result<Void> rv=dao.delete(trans, data, false);
        data.invalidate(this);
        return rv;
    }

    @Override
    public void close(TRANS trans) {
        if (dao!=null) {
            dao.close(trans);
        }
    }


    @Override
    public String table() {
        return dao.table();
    }

    public D dao() {
        return dao;
    }

    public void invalidate(TRANS trans, DATA data) {
        if (info.touch(trans, dao.table(),data.invalidate(this)).notOK()) {
        trans.error().log("Cannot touch CacheInfo for Role");
    }
    }
}
