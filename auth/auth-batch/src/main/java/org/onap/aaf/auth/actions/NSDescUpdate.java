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
import org.onap.aaf.auth.helpers.NS;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public class NSDescUpdate extends ActionDAO<NS,Void,String> {
    public NSDescUpdate(AuthzTrans trans, Cluster cluster, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
    }
    
    public NSDescUpdate(AuthzTrans trans, ActionDAO<?,?,?> adao) {
        super(trans, adao);
    }

    @Override
    public Result<Void> exec(AuthzTrans trans, NS ns, String desc) {
        if(dryRun) {
            trans.info().printf("Would Update '%s' Description to '%s'",ns,desc);
            return Result.ok();
        } else {
            Result<Void> rv = q.nsDAO.dao().addDescription(trans, ns.name, desc);
            if(rv.isOK()) {
                trans.info().printf("Updated '%s' Description to '%s'",ns,desc);
            } else {
                trans.error().printf("Error Updating '%s' Description to '%s' - %s",ns,desc,rv.details);
            }
            return rv;
        }
    }
    
}