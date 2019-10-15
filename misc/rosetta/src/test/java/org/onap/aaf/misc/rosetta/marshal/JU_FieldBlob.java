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
package org.onap.aaf.misc.rosetta.marshal;

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.aaf.misc.env.jaxb.JAXBumar;

public class JU_FieldBlob {

    class FieldBlobImpl extends FieldBlob{

        public FieldBlobImpl(String name) {
            super(name);
            // TODO Auto-generated constructor stub
        }

        @Override
        protected byte[] data(Object t) {
            // TODO Auto-generated method stub
            return null;
        }

    }
    @Test
    public void testData() {
        FieldBlob<JAXBumar> obj = new FieldBlobImpl("test");
        obj.data(Mockito.mock(JAXBumar.class), new StringBuilder());
    }

}
