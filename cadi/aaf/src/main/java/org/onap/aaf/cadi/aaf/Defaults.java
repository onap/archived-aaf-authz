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
    public final static String AAF_VERSION = "2.1";
    public final static String AAF_NS = "AAF_NS";
    public final static String AAF_LOCATE_CONST="https://AAF_LOCATE_URL";
    public final static String AAF_ROOT =   AAF_LOCATE_CONST + "/%CNS.%" + AAF_NS;

}
