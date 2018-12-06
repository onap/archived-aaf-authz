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

package org.onap.aaf.auth.batch.actions;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

import com.datastax.driver.core.Cluster;

public class CredPunt extends ActionPuntDAO<CredDAO.Data,Void,String> {
    
    public CredPunt(AuthzTrans trans, Cluster cluster, int months, int range, boolean dryRun) throws IOException, APIException {
        super(trans,cluster,months,range,dryRun);
    }

    public CredPunt(AuthzTrans trans, ActionDAO<?,?,?> adao, int months, int range) throws IOException {
        super(trans, adao, months,range);
    }

    public Result<Void> exec(AuthzTrans trans, CredDAO.Data cdd,String text) {
        Result<Void> rv = null;
        Result<List<CredDAO.Data>> read = q.credDAO.read(trans, cdd);
        if (read.isOKhasData()) {
            for (CredDAO.Data data : read.value) {
                Date from = data.expires;
                data.expires = puntDate(from);
                if (data.expires.compareTo(from)<=0) {
                    trans.debug().printf("Error: %s is before %s", Chrono.dateOnlyStamp(data.expires), Chrono.dateOnlyStamp(from));
                } else {
                    if (dryRun) {
                        trans.info().log("Would Update Cred",cdd.id, CredPrint.type(cdd.type), "from",Chrono.dateOnlyStamp(from),"to",Chrono.dateOnlyStamp(data.expires));
                    } else {
                        trans.info().log("Updated Cred",cdd.id, CredPrint.type(cdd.type), "from",Chrono.dateOnlyStamp(from),"to",Chrono.dateOnlyStamp(data.expires));
                        rv = q.credDAO.update(trans, data);
                    }
                }
            }
        }
        if (rv==null) {
            rv=Result.err(read);
        }
        return rv;
    }
}