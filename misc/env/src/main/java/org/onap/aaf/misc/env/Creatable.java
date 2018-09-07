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
 * <h1>Creatable</h1>
 * <b>**Must implement constructor T(ENV env, long currentTimeMillis);**</b><p>
 *
 * This interface exists to cover basic LifeCycle semantics so that Objects
 * can be created dynamically and managed at a basic level (destroy(env)).
 * 
 * @author Jonathan
 *
 * @param <T>
 */
public interface Creatable<T> {
    /**
     * Return the timestamp (Unix long) when this object was created.<p>
     * This can be used to see if the object is out of date in certain
     * circumstances, or perhaps has already been notified in others.
     * 
     * @return long
     */
    public abstract long created();
    
    /**
     * Allow LifeCycle aware process to signal this element as destroyed.
     *  
     * @param env
     */
    public abstract void destroy(Env env);
}
