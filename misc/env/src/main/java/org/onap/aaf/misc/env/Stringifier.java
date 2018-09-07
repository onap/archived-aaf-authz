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

package org.onap.aaf.misc.env;



/**
 * <h1>Stringifier</h1>
 * <i>Stringifier</i> abstracts the marshaling of a String to an Object
 */
public interface Stringifier<T> extends LifeCycle {
    
    /**
     * Marshal from a String to an Object T, using contents from Env as necessary.<p>
     * 
     * Implementations should use the {@link Env} to call "env.startXMLTime()" to mark
     * XML time, since this is often a costly process.
     *
     * @param env
     * @param input
     * @return String
     * @throws APIException
     */
    public abstract String stringify(Env env, T input, boolean ... options) throws APIException;
    
}