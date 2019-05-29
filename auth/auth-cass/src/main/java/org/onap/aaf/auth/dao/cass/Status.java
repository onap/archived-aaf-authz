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

import org.onap.aaf.auth.layer.Result;




/**
 * Add additional Behavior for Specific Applications for Results
 * 
 * In this case, we add additional BitField information accessible by
 * method (
 * @author Jonathan
 *
 * @param <RV>
 */
public class Status<RV> extends Result<RV> {
    
    // Jonathan 10/1/2013:  Initially, I used enum, but it's not extensible.
    public final static int ERR_NsNotFound = Result.ERR_General+1,
                            ERR_RoleNotFound = Result.ERR_General+2,
                            ERR_PermissionNotFound = Result.ERR_General+3, 
                            ERR_UserNotFound = Result.ERR_General+4,
                            ERR_UserRoleNotFound = Result.ERR_General+5,
                            ERR_DelegateNotFound = Result.ERR_General+6,
                            ERR_InvalidDelegate = Result.ERR_General+7,
                            ERR_DependencyExists = Result.ERR_General+8,
                            ERR_NoApprovals = Result.ERR_General+9,
                            ACC_Now = Result.ERR_General+10,
                            ACC_Future = Result.ERR_General+11,
                            ERR_ChoiceNeeded = Result.ERR_General+12,
                            ERR_FutureNotRequested = Result.ERR_General+13;
  
    /**
     * Constructor for Result set. 
     * @param data
     * @param status
     */
    private Status(RV value, int status, String details, String[] variables ) {
        super(value,status,details,(Object[])variables);
    }

    public static String name(int status) {
        switch(status) {
            case OK: return "OK";
            case ERR_NsNotFound: return "ERR_NsNotFound";
            case ERR_RoleNotFound: return "ERR_RoleNotFound";
            case ERR_PermissionNotFound: return "ERR_PermissionNotFound"; 
            case ERR_UserNotFound: return "ERR_UserNotFound";
            case ERR_UserRoleNotFound: return "ERR_UserRoleNotFound";
            case ERR_DelegateNotFound: return "ERR_DelegateNotFound";
            case ERR_InvalidDelegate: return "ERR_InvalidDelegate";
            case ERR_ConflictAlreadyExists: return "ERR_ConflictAlreadyExists";
            case ERR_DependencyExists: return "ERR_DependencyExists";
            case ERR_ActionNotCompleted: return "ERR_ActionNotCompleted";
            case ERR_Denied: return "ERR_Denied";
            case ERR_Policy: return "ERR_Policy";
            case ERR_BadData: return "ERR_BadData";
            case ERR_NotImplemented: return "ERR_NotImplemented";
            case ERR_NotFound: return "ERR_NotFound";
            case ERR_ChoiceNeeded: return "ERR_ChoiceNeeded";
        }
        //case ERR_General:   or unknown... 
        return "ERR_General";
    }
    
}
