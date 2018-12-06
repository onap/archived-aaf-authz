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

import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.util.Chrono;

public class CredPrint implements Action<CredDAO.Data,Void,String> {
    private String info;

    public CredPrint(String text) {
        this.info = text;
    }

    @Override
    public Result<Void> exec(AuthzTrans trans, CredDAO.Data cred, String text) {
        trans.info().log(info,cred.id,text, type(cred.type),Chrono.dateOnlyStamp(cred.expires));
        return Result.ok();
    }
    
    
    public static String type(int type) {
        switch(type) {
            case CredDAO.BASIC_AUTH: // 1
                    return "OLD";
            case CredDAO.BASIC_AUTH_SHA256: // 2 
                    return "U/P"; 
            case CredDAO.CERT_SHA256_RSA: // 200
                    return "Cert"; 
            default: 
                return "Unknown";
        }
    }

}