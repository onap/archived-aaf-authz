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

package org.onap.aaf.misc.env.util;

import java.util.regex.Pattern;

public class IPValidator {
    private static final Pattern ipv4_p = Pattern.compile(
            "^((\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])\\.){3}\\2$"
            );

    private static final Pattern ipv6_p = Pattern.compile(
            "^(([0-9a-fA-F]{0,4})([:|.])){2,7}([0-9a-fA-F]{0,4})$"
            );
    
    private static final Pattern doubleColon = Pattern.compile(
            ".*::.*::.*"
            );

    private static final Pattern tooManyColon = Pattern.compile(
            "(.*:){1,7}"
            );

    
    public static boolean ipv4(String str) {
        return ipv4_p.matcher(str).matches();
    }
    
    public static boolean ipv6(String str) {
        return ipv6_p.matcher(str).matches() &&
               !doubleColon.matcher(str).matches() &&
               !tooManyColon.matcher(str).matches();
    }
    
    public static boolean ip (String str) {
        return ipv4_p.matcher(str).matches() || ipv6(str);
    }
}
