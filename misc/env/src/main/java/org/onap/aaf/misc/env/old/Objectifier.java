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

/**
 * 
 */
package org.onap.aaf.misc.env.old;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LifeCycle;


/**
 * <h1>Objectifier</h1>
 * <i>Objectifier</i> abstracts the unmarshaling of an Object from a String, and 
 * the creation of an uninitialized object. 
 */
public interface Objectifier<T> extends LifeCycle {
    /**
     * Marshal to Object T from a String, using contents from Env as necessary.<p>
     * 
     * Implementations should use the {@link Env} to call "env.startXMLTime()" to mark
     * XML time, since this is often a costly process.
     *
     * @param env
     * @param input
     * @return T
     * @throws APIException
     */
    public abstract T objectify(Env env, String input) throws APIException;

    /**
     * Create a new object of type T.  This is often more efficiently done with
     * the underlying XML (or other) Library.
     * @return T
     * @throws APIException
     */
    public abstract T newInstance() throws APIException;

    
}