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


package org.onap.aaf.auth.batch.helpers.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.batch.helpers.Creator;

import com.datastax.driver.core.Row;

import junit.framework.Assert;

import static org.mockito.Mockito.*;
import org.junit.Test;

public class JU_Creator {

    CreatorStub creatorStub;

    private class CreatorStub extends Creator{

        @Override
        public Object create(Row row) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String select() {
            // TODO Auto-generated method stub
            return "Select";                    //Changed from null to Select
        }
    
    }

    @Before
    public void setUp() {
        creatorStub = new CreatorStub();
    }

    @Test
    public void testQuery() {
        creatorStub.select();
        Assert.assertEquals("Select WHERE test;", creatorStub.query("test"));
        Assert.assertEquals("Select;", creatorStub.query(null));
    }

}
