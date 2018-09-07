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
package org.onap.aaf.cadi.aaf;

public interface Defaults {
    public static String AAF_VERSION = "2.1";
    public static String AAF_NS = "AAF_NS";
    public static String AAF_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".service:" + AAF_VERSION;
    public static String GUI_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".gui:" + AAF_VERSION;
    public static String CM_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".cm:" + AAF_VERSION;
    public static String FS_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".fs:" + AAF_VERSION;
    public static String HELLO_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".hello:" + AAF_VERSION;
    public static String OAUTH2_TOKEN_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".token:" + AAF_VERSION;
    public static String OAUTH2_INTROSPECT_URL = "https://AAF_LOCATE_URL/" + AAF_NS + ".introspect:" + AAF_VERSION;
}
