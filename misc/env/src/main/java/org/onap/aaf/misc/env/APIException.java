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
 * An Exception with the ability to hold a payload.<p>
 * 
 * This is important, because sometimes, the output of a Framework
 * may be a descriptive object which doesn't inherit from Throwable
 * and thus cannot be attached in "initCause".<p>
 * 
 * Examples may be a SOAP Fault.
 * 
 * @author Jonathan
 *
 */
public class APIException extends Exception {
    
    private Object payload = null;
    
    /**
     * @param t
     */
    public APIException(Throwable t) {
        super(t);
    }
    
    /**
     * @param string
     */
    public APIException(String string) {
        super(string);
    }

    /**
     * @param errorMessage
     * @param t
     */
    public APIException(String errorMessage, Throwable t) {
        super(errorMessage,t);
    }

    /**
     * Return payload, or null if none was set.  Type is up to the calling
     * System.
     * 
     * @return Object
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * Set a specific payload into this Exception, which doesn't necessarily
     * inherit from Throwable.
     * 
     * @param payload
     * @return APIException
     */
    public APIException setPayload(Object payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Java expected serial ID
     */
    private static final long serialVersionUID = 3505343458251445169L;
}
