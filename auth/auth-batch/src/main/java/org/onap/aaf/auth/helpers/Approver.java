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

package org.onap.aaf.auth.helpers;

import java.util.HashMap;
import java.util.Map;

import org.onap.aaf.auth.actions.Message;
import org.onap.aaf.auth.org.Organization;

public class Approver {
    public String name;
    public Organization org;
    public Map<String, Integer> userRequests;
    
    public Approver(String approver, Organization org) {
        this.name = approver;
        this.org = org;
        userRequests = new HashMap<>();
    }
    
    public void addRequest(String user) {
        if (userRequests.get(user) == null) {
            userRequests.put(user, 1);
        } else {
            Integer curCount = userRequests.remove(user);
            userRequests.put(user, curCount+1);
        }
    }
    
    /**
     * @param sb
     * @return
     */
    public void build(Message msg) {
        msg.clear();
        msg.line("You have %d total pending approvals from the following users:", userRequests.size());
        for (Map.Entry<String, Integer> entry : userRequests.entrySet()) {
            msg.line("  %s (%d)",entry.getKey(),entry.getValue());
        }
    }

}
