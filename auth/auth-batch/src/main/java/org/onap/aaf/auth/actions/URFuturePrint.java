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

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.helpers.UserRole;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.misc.env.util.Chrono;


public class URFuturePrint implements  Action<UserRole,String,String> {
    private String info;

    public URFuturePrint(String text) {
        this.info = text;
    }

    @Override
    public Result<String> exec(AuthzTrans trans, UserRole ur, String text) {
        trans.info().log(info,text,ur.user(),"to",ur.role(),"on",Chrono.dateOnlyStamp(ur.expires()));
        return Result.ok(info);
    }}