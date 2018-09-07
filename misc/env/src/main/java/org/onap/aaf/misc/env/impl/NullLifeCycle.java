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
package org.onap.aaf.misc.env.impl;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.LifeCycle;



/**
 * <h1>NullLifeCycle</h1>
 * 
 * This is a convenience class for those Objects which should
 * implement LifeCycle, but don't have anything to do in any of the 
 * LifeCycle methods defined. Extending
 * NullLifeCycle reduces the required methods for the class by 5.  
 * Any one or two of them can be overloaded.<p>
 * 
 * If more are overloaded, it is
 * recommended just to implement LifeCycle.
 * <p>
 * 
 * This only works, though, if the Object doesn't need to extend something
 * else, due to Java's Single Extension policy.  In other cases, just
 * implement LifeCycle, and leave them empty.
 * 
 * @author Jonathan
 *
 */
public class NullLifeCycle implements LifeCycle {
    public void servicePrestart(Env env) throws APIException {}
    public void threadPrestart(Env env) throws APIException {}
    public void refresh(Env env) throws APIException {}
    public void threadDestroy(Env env) throws APIException {}
    public void serviceDestroy(Env env) throws APIException {}
}