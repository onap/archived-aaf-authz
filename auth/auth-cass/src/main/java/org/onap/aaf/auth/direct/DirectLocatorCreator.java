/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.direct;

import org.onap.aaf.auth.dao.cass.LocateDAO;
import org.onap.aaf.auth.env.AuthzEnv;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.aaf.v2_0.AbsAAFLocator;

public class DirectLocatorCreator implements AbsAAFLocator.LocatorCreator {
    private final AuthzEnv env;
    private final LocateDAO locateDAO;
    private String myhostname;
    private int myport;

    public DirectLocatorCreator(AuthzEnv env, LocateDAO locateDAO) {
        this.env = env;
        this.locateDAO = locateDAO;
    }

    @Override
    public AbsAAFLocator<?> create(String key, String version) throws LocatorException {
        DirectAAFLocator dal = new DirectAAFLocator(env,locateDAO,key,version);
        if (myhostname!=null) {
            dal.setSelf(myhostname, myport);
        }
        return dal;
    }

    /**
     * Make sure DirectAAFLocator created does not include self.
     * @param hostname
     * @param port
     */
    public void setSelf(String hostname, int port) {
        myhostname = hostname;
        myport = port;
    }

}
