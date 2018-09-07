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

package org.onap.aaf.auth.actions;

import java.io.IOException;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.NsAttrib;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class NSACreate extends ActionDAO<NsAttrib,Void,String> {
    public NSACreate(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public NSACreate(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<Void> exec(AuthzTrans trans, NsAttrib nsa, String text) {
        if(dryRun) {
            trans.info().printf("Would Create %s Attrib '%s=%s' in %s",text,nsa.key,nsa.value,nsa.ns);
            return Result.ok();
        } else {
            Result<Void> rv = q.nsDAO.dao().attribAdd(trans, nsa.ns, nsa.key, nsa.value);
            if(rv.isOK()) {
                trans.info().printf("%s - Created Attrib '%s=%s' in %s",text,nsa.key,nsa.value,nsa.ns);
            } else {
                trans.error().printf("Error Creating Attrib '%s=%s' in %s - %s",nsa.key,nsa.value,nsa.ns,rv.details);
            }
            return rv;
        }
    }
    
}