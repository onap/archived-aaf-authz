/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modification Copyright (c) 2019 IBM
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

import org.onap.aaf.auth.batch.helpers.ExpireRange;
import org.onap.aaf.cadi.Access;

public class OneMonthNotifyCredBody extends NotifyCredBody {
    public OneMonthNotifyCredBody(Access access) {
        super(access, ExpireRange.ONE_MONTH);
    }

    @Override
    public String subject() {
        return String.format("AAF One Month Credential Notification (ENV: %s)",env);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.batch.reports.bodies.NotifyCredBody#dynamic()
     */
    @Override
    protected String dynamic() {
        return "This is your <b>one month</b> notification. " + super.dynamic();
    }
}
