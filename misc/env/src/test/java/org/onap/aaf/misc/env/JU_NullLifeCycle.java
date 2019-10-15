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

package org.onap.aaf.misc.env;

import org.junit.Test;
import org.onap.aaf.misc.env.impl.NullLifeCycle;

public class JU_NullLifeCycle {

    @Test
    public void testServicePrestart() {
        NullLifeCycle lifeCycleObj = new NullLifeCycle();
        try {
            lifeCycleObj.servicePrestart(null);
            lifeCycleObj.serviceDestroy(null);
            lifeCycleObj.threadDestroy(null);
            lifeCycleObj.threadPrestart(null);
            lifeCycleObj.refresh(null);
        }catch(APIException a) {
        
        }
    }
   
}