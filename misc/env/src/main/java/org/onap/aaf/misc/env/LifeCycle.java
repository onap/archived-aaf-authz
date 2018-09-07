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
 * Created on: Aug 19, 2009
 * Created by: Jonathan
 *
 * (c) 2009 SBC Knowledge Ventures, L.P. All rights reserved.
 ******************************************************************* 
 * RESTRICTED - PROPRIETARY INFORMATION The Information contained 
 * herein is for use only by authorized employees of AT&T Services, 
 * Inc., and authorized Affiliates of AT&T Services, Inc., and is 
 * not for general distribution within or outside the respective 
 * companies. 
 *******************************************************************
 */
package org.onap.aaf.misc.env;

import org.onap.aaf.misc.env.util.RefreshableThreadObject;


/**
 * @author Jonathan
 * 
 */
public interface LifeCycle {
    /**
     * The Service using LifeCycle Elements is required to call this method at
     * the appropriate startup time. This is better for services than a simple
     * static call, because the exact moment of starting can be determined
     * programatically.
     * <p>
     * 
     * An excellent use is to establish security credentials with a backend
     * after appropriate configurations have been read and available as part of
     * the {@link Env} Object.
     * 
     * @param env
     * @throws APIException
     */
    public abstract void servicePrestart(Env env) throws APIException;

    /**
     * Many cases of implementations are not thread safe, and mechanisms must be
     * derived to accomodate them by holding per Thread.
     * <p>
     * 
     * {@link ThreadLocal} is a valuable resource, but start up times within the
     * thread, depending on what it is, can be substantial.
     * <p>
     * 
     * Use ThreadPrestart to do all that is possible before actually performing
     * work, i.e. inside of a client transaction.
     * 
     * @param env
     * @throws APIException
     */
    public abstract void threadPrestart(Env env) throws APIException;

    /**
     * The Service will call this when (service-defined) configurations change.
     * <p>
     * 
     * This mechanism allows the Service to recognize events, such as file
     * changes, and pass on the event to all LifeCycle implementors.
     * <p>
     * 
     * The code should take the opportunity to evaluate configuration and change
     * as necessary.
     * <p>
     * 
     * <h2>IMPORTANT:</h2>
     * The LifeCycle implementor cannot guarantee it will not be in the middle
     * of a transaction, so it would behoove the implementor to construct
     * content that does not affect anything until finished, then apply to an
     * appropriate atomic action (i.e. setting an Object to a field), or even
     * synchronizing.
     * 
     * If you are using Java's "ThreadLocal", consider
     * {@link RefreshableThreadObject}, because it implements LifeCycle, and
     * responds to the refresh command.
     * 
     * @param env
     * @throws APIException
     */
    public abstract void refresh(Env env) throws APIException;

    /**
     * Parallel to threadPrestart, threadDestroy tells the implementor that the
     * service is ending this particular thread, and to take this opportunity to
     * close out any content specific to this thread that can be closed.
     * 
     * @param env
     * @throws APIException
     */
    public abstract void threadDestroy(Env env) throws APIException;

    /**
     * Parallel to servicePrestart, serviceDestroy tells the implementor that
     * the service is ending, and to take this opportunity to close out any
     * content under it's control that can or should be closed explicitly.
     */
    public abstract void serviceDestroy(Env env) throws APIException;
}
