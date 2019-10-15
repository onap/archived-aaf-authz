/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/
package org.onap.aaf.org;

import org.onap.aaf.auth.org.EmailWarnings;

public class DefaultOrgWarnings implements EmailWarnings {

    @Override
    public long credEmailInterval()
    {
        return 604800000L; // 7 days in millis 1000 * 86400 * 7
    }

    @Override
    public long roleEmailInterval()
    {
        return 604800000L; // 7 days in millis 1000 * 86400 * 7
    }

    @Override
    public long apprEmailInterval() {
        return 259200000L; // 3 days in millis 1000 * 86400 * 3
    }

    @Override
    public long  credExpirationWarning()
    {
        return( 2592000000L ); // One month, in milliseconds 1000 * 86400 * 30  in milliseconds
    }

    @Override
    public long roleExpirationWarning()
    {
        return( 2592000000L ); // One month, in milliseconds 1000 * 86400 * 30  in milliseconds
    }

    @Override
    public long emailUrgentWarning()
    {
        return( 1209600000L ); // Two weeks, in milliseconds 1000 * 86400 * 14  in milliseconds
    }

}
