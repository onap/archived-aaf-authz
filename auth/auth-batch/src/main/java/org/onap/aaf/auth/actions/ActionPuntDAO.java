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
import java.util.Date;
import java.util.GregorianCalendar;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.misc.env.APIException;

import com.datastax.driver.core.Cluster;

public abstract class ActionPuntDAO<D, RV, T> extends ActionDAO<D, RV, T> {
    private int months;
    protected static final Date now = new Date();

    public ActionPuntDAO(AuthzTrans trans, Cluster cluster, int months, int range, boolean dryRun) throws APIException, IOException {
        super(trans, cluster,dryRun);
        this.months = months;
    }

    public ActionPuntDAO(AuthzTrans trans, ActionDAO<?, ?,?> predecessor, int months, int range) {
        super(trans, predecessor);
        this.months = months;
    }
    

    protected Date puntDate(Date current) {
        GregorianCalendar temp = new GregorianCalendar();
        temp.setTime(current);
        temp.add(GregorianCalendar.MONTH, months);
        return temp.getTime();
    }

}
