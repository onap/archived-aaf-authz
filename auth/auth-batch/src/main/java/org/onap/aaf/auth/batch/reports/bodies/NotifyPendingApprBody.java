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
package org.onap.aaf.auth.batch.reports.bodies;

import java.util.List;

import org.onap.aaf.auth.batch.helpers.LastNotified;
import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;

public class NotifyPendingApprBody extends NotifyBody {

    public NotifyPendingApprBody(Access access) {
        super(access,"appr","PendingApproval");
    }

    @Override
    public boolean body(AuthzTrans trans, StringBuilder sb, int indent, Notify n, String id) {
        boolean rv = false;
        for(List<String> row : rows.get(id)) {
            String qty = row.get(2);
            if("1".equals(qty)) {
                printf(sb,indent,"You have an Approval in the AAF %s Environment awaiting your decision.\n",row.get(3));
            } else {
                printf(sb,indent,"You have %s Approvals in the AAF %s Environment awaiting your decision.\n",qty,row.get(3));
            }
            printf(sb,indent,"<br><br><b>ACTION:</b> <i>Click on</i> <a href=\"%s/approve\">AAF Approval Page</a>",n.guiURL);
            rv = true;
            break; // only one
        }

        return rv;
    }

    @Override
    public String user(List<String> row) {
        if( (row != null) && row.size()>1) {
            return row.get(1);
        }
        return null;
    }

    @Override
    public String subject() {
        return String.format("AAF Pending Approval Notification (ENV: %s)",env);
    }


    @Override
    public void record(AuthzTrans trans, StringBuilder query, String id, List<String> notified, LastNotified lastN) {
        for(String n : notified) {
            // No special key for Pending Requests.
            lastN.update(query,n,"pending","");
        }
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.batch.reports.bodies.NotifyBody#store(java.util.List)
     */
    @Override
    public void store(List<String> row) {
        // Notify Pending is setup for 1 Notification at a time
        super.rows.clear();
        super.store(row);
    }

}
