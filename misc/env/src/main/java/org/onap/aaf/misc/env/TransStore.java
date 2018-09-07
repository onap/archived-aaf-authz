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

public interface TransStore extends Trans {
    /**
     * Returns the Slot assigned to the supplied name.
     * 
     * @param name
     *             The name of the Slot to acquire.
     * @return
     *             The Slot associated with the supplied name.
     */
    public abstract Slot slot(String name);
    
    /**
     * Put data into the right slot 
     */
    public void put(Slot slot, Object value);

    /**
     *  Get data from the right slot
     *  
     *  This will do a cast to the expected type derived from Default
     */
    public<T> T get(Slot slot, T deflt);

    /**
     * Returns an Object from the Organizer's static state, or the Default if null
     * 
     * @param slot
     *             The StaticSlot to retrieve the data from.
     * @return
     *             The Object located in the supplied StaticSlot of the Organizer's static state.
     */
    public abstract<T> T get(StaticSlot slot, T dflt);
    
}
