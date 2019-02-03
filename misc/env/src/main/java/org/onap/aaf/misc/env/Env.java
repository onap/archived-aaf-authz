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
 * <H1>Env</H1>
 * <i>Env</i> is the basic representation of what can be obtained from the
 * Environment.  Environments also need the ability to Log and Track Time, so
 * to keep the interfaces clean, Env Interface inherits from Trans.  This does NOT
 * mean that all Environments are Transactions... It only means Environments need 
 * to Log and Track Times. 
 * .<p>
 * 
 * Using this abstraction, Components can be built on a modular basis,
 * and still have the essentials of functioning within the service mechanism.<p>
 * 
 * Thus, for instance, an Module could be made to work in two separate
 * service types, with substantial differences in choices of logging, or auditing,
 * and still have reasonably deep insight, such as the exact time a
 * remote service was invoked.<p>
 * 
 * There is a bit of an assumption corresponding to the reality of the 2000s that
 * XML plays a part in most service work.
 *  
 * @author Jonathan
 *
 */
public interface Env {
    /**
     * Very Severe Error may cause program to abort
     */
    public LogTarget fatal();
    
    /**
     * Severe Error, but program might continue running
     */
    public LogTarget error();

    /**
     * Required Audit statements
     * @return
     */
    public LogTarget audit();

    /**
     * Initialization steps... Allows a Logger to separate startup info
     * @return
     */
    public LogTarget init();

    /**
     * Potentially harmful situations
     * @return
     */
    public LogTarget warn();
    
    /**
     * Course Grained highlights of program progress
     * @return
     */
    public LogTarget info();
    
    /**
     * Fine-grained informational events useful for debugging
     * @return
     */
    public LogTarget debug();
    
    /**
     * Finest grained Informational events... more detailed than Debug
     * @return
     */
    public LogTarget trace();


    /**
     * Basic and Common Audit info... 
     *  
     * Note Apps can define, but should use Integers after 0x1F.  They can combine with "&"
     */
    public static final int REMOTE = 0x01;
    public static final int XML = 0x02;
    public static final int JSON = 0x04;
    public static final int SUB = 0x08;
    public static final int CHECKPOINT = 0x10;
    public static final int ALWAYS = 0x20; // Mark as a line to print, even in WARN+ mode


    
    /**
     * Start a Time Trail with differentiation by flag.  This can be Defined By above flags or combined with
     * app flag definitions
     * 
     * @param string
     * @param flag
     * @return
     */
    public TimeTaken start(String name, int flag, Object ... values);
    
    public String setProperty(String tag, String value);
    public String getProperty(String tag);
    public String getProperty(String tag, String deflt);
    
    /**
     * Passwords should be encrypted on the disk.  Use this method to apply decryption before
     * using.  The Implementation should give ways to decrypt
     * 
     * @param tag
     * @return
     */
    public Decryptor decryptor();
    
    public Encryptor encryptor();

}

