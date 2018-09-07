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

import java.io.InputStream;
import java.io.Reader;

public interface IOObjectifier<T> extends Objectifier<T> {
    /**
     * Marshal to Object T from a Reader, using contents from Env as necessary.<p>
     * 
     * Implementations should use the {@link Env} to call "env.startXMLTime()" to mark
     * XML time, since this is often a costly process.
     *
     * @param env
     * @param input
     * @return T
     * @throws APIException
     */
    public abstract T objectify(Env env, Reader rdr) throws APIException;
    
    /**
     * Marshal to Object T from an InputStream, using contents from Env as necessary.<p>
     * 
     * Implementations should use the {@link Env} to call "env.startXMLTime()" to mark
     * XML time, since this is often a costly process.
     *
     * @param env
     * @param input
     * @return T
     * @throws APIException
     */
    public abstract T objectify(Env env, InputStream is) throws APIException;

}
