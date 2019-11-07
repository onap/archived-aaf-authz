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

package org.onap.aaf.auth.rserv;


/**
 * Analyze and hold Version information for Code
 *
 * @author Jonathan
 *
 */
public class Version {
    private Object[] parts;

    public Version(String v) {
        String sparts[] = v.split("\\.");
        parts = new Object[sparts.length];
        System.arraycopy(sparts, 0, parts, 0, sparts.length);
        if (parts.length>1) { // has at least a minor
          try {
              parts[1]=Integer.decode(sparts[1]); // minor elements need to be converted to Integer for comparison
          } catch (NumberFormatException e) {
              // it's ok, leave it as a string
              parts[1]=sparts[1]; // This useless piece of code forced by Sonar which calls empty Exceptions "Blockers".
          }
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Version) {
            Version ver = (Version)obj;
            int length = Math.min(parts.length, ver.parts.length);
            for (int i=0;i<length;++i) { // match on declared parts
                if ((i==1) && (parts[1] instanceof Integer && ver.parts[1] instanceof Integer)) {
                        // Match on Minor version if this Version is less than Version to be checked
                        if (((Integer)parts[1])<((Integer)ver.parts[1])) {
                            return false;
                        }
                        continue; // don't match next line
                }
                if (!parts[i].equals(ver.parts[i])) {
                    return false; // other spots exact match
                }
            }
            return true;
        }
        return false;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Object obj : parts) {
            if (first) {
                first = false;
            } else {
                sb.append('.');
            }
            sb.append(obj.toString());
        }
        return sb.toString();
    }
}