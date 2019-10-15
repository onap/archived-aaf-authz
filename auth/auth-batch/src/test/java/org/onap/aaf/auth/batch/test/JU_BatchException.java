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


package org.onap.aaf.auth.batch.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.BatchException;

import static org.mockito.Mockito.*;
import org.junit.Test;

public class JU_BatchException {

    BatchException bExcept1;
    BatchException bExcept2;
    BatchException bExcept3;
    BatchException bExcept4;
    BatchException bExcept5;
    Throwable throwable;

    @Before
    public void setUp() {
        throwable = new Throwable();
    }

    @Test
    public void testBatchException() {
        bExcept1 = new BatchException();
        bExcept2 = new BatchException("test");
        bExcept3 = new BatchException(throwable);
        bExcept4 = new BatchException("test", throwable);
        bExcept5 = new BatchException("test", throwable,true,true);
    }

}
