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
 * Slot.java
 *
 * Created on: Dec 5, 2008
 * Created by: Jonathan
 *
 * (c) 2008 SBC Knowledge Ventures, L.P. All rights reserved.
 ******************************************************************* 
 * RESTRICTED - PROPRIETARY INFORMATION The Information contained 
 * herein is for use only by authorized employees of AT&T Services, 
 * Inc., and authorized Affiliates of AT&T Services, Inc., and is 
 * not for general distribution within or outside the respective 
 * companies. 
 *******************************************************************
 */
package org.onap.aaf.misc.env;

/**
 * Slot's are used to store and retrieve data in the transaction's State object.
 */
public final class Slot {
    
    /*
     * The name of the Slot.
     */
    private final String key;
    
    /*
     * The index of the State's local map associated with this Slot.
     */
    final int slot; 
    
    /**
     * Constructs a new Slot.
     * 
     * @param index
     *             The index of State's local map this Slot is associated with.
     * @param name
     *             The name of the Slot's key.
     */
    Slot(int index, String name) {
        slot = index;
        key = name;
    }
    
    /**
     * Debug method only to print key=slot pairs.
     */
    public String toString() {
        return key + '=' + slot;
    }
    
    /**
     * Returns the name of this Slot's key.
     * 
     * @return
     *             The name of this Slot's key.
     */
    public String getKey() {
        return key;
    }
    
    /**
     * Put an Object into the slot on the State
     * @param state
     * @param obj
     */
    public void put(Object[] state, Object obj) {
        state[slot]=obj;
    }

    /**
     * Get an Object from the slot on the State
     * @param state
     * @param obj
     */
    public Object get(Object[] state) {
        return state[slot];
    }

}
