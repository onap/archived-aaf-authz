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

/**
 * Defines the Type Codes in the NS Table.
 * @author Jonathan
 *
 */
public enum NsType {
        UNKNOWN (-1),
        DOT (0),
        ROOT (1),
        COMPANY (2),
        APP (3),
        STACKED_APP (10),
        STACK (11);

        public final int type;
        private NsType(int t) {
            type = t;
        }
        /**
         * This is not the Ordinal, but the Type that is stored in NS Tables
         *
         * @param t
         * @return
         */
        public static NsType fromType(int t) {
            for (NsType nst : values()) {
                if (t==nst.type) {
                    return nst;
                }
            }
            return UNKNOWN;
        }

        /**
         * Use this one rather than "valueOf" to avoid Exception
         * @param s
         * @return
         */
        public static NsType fromString(String s) {
            if (s!=null) {
                for (NsType nst : values()) {
                    if (nst.name().equals(s)) {
                        return nst;
                    }
                }
            }
            return UNKNOWN;
        }


}
