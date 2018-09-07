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

import java.util.Map;

public interface EnvProps extends Env {
    public interface EnvProperty {
        public String getProperty(String input);
    };

    /**
     * Obtain a Property (String) based on a Key.  Implementor decides how
     * that works, i.e. from a complex set of Configurations, or just 
     * "System" (Java standard)
     * 
     * @param key
     * @return APIException
     */
    public String getProperty(String key);

    /**
     * Obtain a Property (String) based on a Key.  Implementor decides how
     * that works, i.e. from a complex set of Configurations, or just 
     * "System" (Java standard)
     * 
     * If Property Value is null, then default will be used.
     * @param key
     * @return APIException
     */
    public String getProperty(String tag, String defaultValue);

    /**
     * Set a Property (String) based on a Key accessible to all in Env.  Implementor decides how
     * that works, i.e. from a complex set of Configurations, or just 
     * "System" (Java standard)
     * 
     * @param key
     * @return APIException
     */
    public String setProperty(String key, String value);
    
    /**
     * Get the SubProperties based on key.
     * 
     * use "false" to remove prefix, "true" to leave prefix in.
     * 
     * @param key
     * @return APIException
     * Given a known property set (or in this case, properties starting with key), 
     * return map of all properties with appropriate key names
     */
    public Map<String, String> getSubProperties(String key, boolean includePrefix);

    /**
     * Get all of the properties in the Environment
     * @return
     */
    public Map<String, String> getProperties();

}
