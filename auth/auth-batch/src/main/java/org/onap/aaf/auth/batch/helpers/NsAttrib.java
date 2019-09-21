/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2018 IBM.
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

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

public class NsAttrib {

    public final String ns;
    public final String key;
    public final String value;
    
    public NsAttrib(String ns, String key, String value) {
        this.ns = ns;
        this.key = key;
        this.value = value;
    }
    
   public static void load(Trans trans, Session session, Creator<NsAttrib> creator, Visitor<NsAttrib> visitor) {
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
                visitor.visit(creator.create(row));
            }
        } finally {
            tt.done();
            trans.info().log("Found",count,"NS Attributes");
        }
    }
}

