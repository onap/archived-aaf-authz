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

package org.onap.aaf.auth.rserv;

/**
 * A pair of generic Objects.  
 * <p>
 * @author Jonathan
 *
 * @param <X>
 * @param <Y>
 */
public class Pair<X,Y> {
    public X x;
    public Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return "X: " + x.toString() + "-->" + y.toString();
    }
}
