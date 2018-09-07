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
 * A Trans is like an Env, however, it's purpose it to track the Transient 
 * Data associated with Transactions, or other short term elements.
 * 
 * Any Object implementing Trans should expect to go in an out of scope quickly
 * 
 * Implementations should also overload the concepts of "Start", etc and build up
 * and Audit Log, so it can implement "metric" below
 * 
 * All Transactions (i.e. a call to a service) will need these items.
 * 
 * @author Jonathan
 *
 */
public interface Trans extends Env {
    /**
     * Add a completed entry in the Audit Trail for tracking purposes.
     * 
     * @param text
     */
    public void checkpoint(String text);

    /**
     * Add a completed entry in the Audit Trail for tracking purposes, and combine flag with "CHECKPOINT" 
     * 
     * @param text
     */
    public void checkpoint(String text, int additionalFlag);

    /**
     * Output an Audit Trail onto the StringBuilder
     *
     * Load metrics into an array of floats from passed in Flags
     * 
     * @param flag
     * @param sb
     * @return     
     */
    public Metric auditTrail(LogTarget lt, int indent, StringBuilder sb, int ... flag);

    public Metric auditTrail(int indent, StringBuilder sb, int ... flag);

    public class Metric {
        public float[] buckets;
        public float   total;
        public int     entries;
    }
}