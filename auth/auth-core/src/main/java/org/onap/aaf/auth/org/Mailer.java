/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.org;

import java.util.List;

import org.onap.aaf.auth.env.AuthzTrans;

public interface Mailer {
    public boolean sendEmail(
            AuthzTrans trans,
            String test,
            List<String> toList, 
            List<String> ccList, 
            String subject, 
            String body,
            Boolean urgent) throws OrganizationException;

    public String mailFrom();

    public int count();

}
