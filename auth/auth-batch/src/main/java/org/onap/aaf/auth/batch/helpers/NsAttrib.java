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

package org.onap.aaf.auth.batch.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class NsAttrib  {
    public static final List<NsAttrib> data = new ArrayList<>();
    public static final SortedMap<String,List<NsAttrib>> byKey = new TreeMap<>();
    public static final SortedMap<String,List<NsAttrib>> byNS = new TreeMap<>();

    public final String ns;
    public final String key;
    public final String value;
    public static Creator<NsAttrib> v2_0_11 = new Creator<NsAttrib>() {
        @Override
        public NsAttrib create(Row row) {
            return new NsAttrib(row.getString(0), row.getString(1), row.getString(2));
        }

        @Override
        public String select() {
            return "select ns,key,value from authz.ns_attrib";
        }
    };
    
    public NsAttrib(String ns, String key, String value) {
        this.ns = ns;
        this.key = key;
        this.value = value;
    }
    
    public static void load(Trans trans, Session session, Creator<NsAttrib> creator ) {
        trans.info().log( "query: " + creator.select() );
        ResultSet results;
        TimeTaken tt = trans.start("Load NsAttributes", Env.REMOTE);
        try {
            Statement stmt = new SimpleStatement(creator.select());
            results = session.execute(stmt);
        } finally {
            tt.done();
        }
        int count = 0;
        tt = trans.start("Process NsAttributes", Env.SUB);

        try {
            for (Row row : results.all()) {
                ++count;
                NsAttrib ur = creator.create(row);
                data.add(ur);
                
                List<NsAttrib> lna = byKey.get(ur.key);
                if (lna==null) {
                    lna = new ArrayList<>();
                    byKey.put(ur.key, lna);
                }
                lna.add(ur);
                
                lna = byNS.get(ur.ns);
                if (lna==null) {
                    lna = new ArrayList<>();
                    byNS.put(ur.ns, lna);
                }
                lna.add(ur);
            }
        } finally {
            tt.done();
            trans.info().log("Found",count,"NS Attributes");
        }
    }

    public String toString() {
        return '"' + ns + "\",\"" + key + "\",\""  + value +'"';
    }

}