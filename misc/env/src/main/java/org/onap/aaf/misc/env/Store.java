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

import java.util.List;

public interface Store {
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
     * Returns the existing Slot associated with the supplied name, or null if it doesn't exist.
     * 
     * @param name
     *             The name of the Slot to get.
     * @return
     *             The Slot assigned to the supplied name, or null if it doesn't exist.
     *             
     */
    public abstract Slot existingSlot(String name);

    /**
     * Returns the names used while creating Slots in a List
     * 
     * @return
     */
    public abstract List<String> existingSlotNames();

    /**
     * Returns the StaticSlot assigned to the supplied name.
     * 
     * @param name
     *             The name of the StaticSlot to acquire.
     * @return
     *             The StaticSlot associated with the supplied name.
     */
    public abstract StaticSlot staticSlot(String name);

    /**
     * Returns the names used while creating Static Slots in a List
     * 
     * @return
     */
    public abstract List<String> existingStaticSlotNames();
    
    /**
     * Store the supplied value in the StaticSlot of the Organizer's static state.
     * 
     * @param slot
     *             The StaticSlot used to store the object.
     * @param value
     *             The object to store.
     */
    public abstract void put(StaticSlot slot, Object value);

    /**
     * Returns an Object from the Organizer's static state, or the Default if null
     * 
     * @param slot
     *             The StaticSlot to retrieve the data from.
     * @return
     *             The Object located in the supplied StaticSlot of the Organizer's static state.
     */
    public abstract<T> T get(StaticSlot slot, T dflt);

    /**
     * Returns an Object from the Organizer's static state 
     * 
     * @param slot
     *             The StaticSlot to retrieve the data from.
     * @return
     *             The Object located in the supplied StaticSlot of the Organizer's static state.
     */
    public abstract<T> T get(StaticSlot slot);

//    /** 
//     * Transfer (targeted) Args to Slots
//     * 
//     * Transfer Strings with format "tag=value" into Static Slots
//     */
//    public abstract void transfer(String args[], String ... tagss);
}