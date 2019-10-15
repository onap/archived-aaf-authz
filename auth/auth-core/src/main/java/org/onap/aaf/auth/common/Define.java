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
package org.onap.aaf.auth.common;

import java.util.Map.Entry;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.config.Config;

public class Define {
    private static String ROOT_NS = null;
    private static String ROOT_COMPANY = null;
    private static boolean initialized = false;

    private final static String MSG = ".set(Access access) must be called before use";
    public static final CharSequence ROOT_NS_TAG = "AAF_NS"; // use for certain Replacements in Location
    private static final int ROOT_NS_TAG_LEN=ROOT_NS_TAG.length();
    private static final String ROOT_NS_TAG_DOT = ROOT_NS_TAG +".";

    public static String ROOT_NS() {
        if (ROOT_NS==null) {
            throw new RuntimeException(Define.class.getName() + MSG);
        }
        return ROOT_NS;
    }

    public static String ROOT_COMPANY() {
        if (ROOT_NS==null) {
            throw new RuntimeException(Define.class.getName() + MSG);
        }
        return ROOT_COMPANY;
    }

    public static void set(Access access) throws CadiException {
        ROOT_NS = access.getProperty(Config.AAF_ROOT_NS,"org.osaaf.aaf");
        ROOT_COMPANY = access.getProperty(Config.AAF_ROOT_COMPANY,null);
        if (ROOT_COMPANY==null) {
            int last = ROOT_NS.lastIndexOf('.');
            if (last>=0) {
                ROOT_COMPANY = ROOT_NS.substring(0, last);
            } else {
                throw new CadiException(Config.AAF_ROOT_COMPANY + " or " + Config.AAF_ROOT_NS + " property with 3 positions is required.");
            }
        }

        for ( Entry<Object, Object> es : access.getProperties().entrySet()) {
            if (es.getKey().toString().startsWith(ROOT_NS_TAG_DOT)) {
                access.getProperties().setProperty(es.getKey().toString(),varReplace(es.getValue().toString()));
            }
        }

        initialized = true;
        access.printf(Level.INIT,"AAF Root NS is %s, and AAF Company Root is %s",ROOT_NS,ROOT_COMPANY);
    }

    public static String varReplace(final String potential) {
        int idx = potential.indexOf(ROOT_NS_TAG_DOT);
        if(idx<0) {
            return potential;
        } else if(idx==0) {
            return ROOT_NS + potential.substring(ROOT_NS_TAG_LEN);
        } else if('.'==potential.charAt(idx)) {
            return potential.replace(ROOT_NS_TAG, ROOT_NS);
        } else {
            return potential;
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static String getCredType(int type) {
        switch(type) {
            case 0:      return "NoCrd";
            case 1:   return "U/P";
            case 2:   return "U/P2";
            case 10:  return "FQI";
            case 200: return "x509";
            default:
                return "n/a";
        }
    }

}
