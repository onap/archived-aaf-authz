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

import javax.xml.datatype.XMLGregorianCalendar;

public abstract class FieldDateTime<T> extends FieldMarshal<T> {
    public FieldDateTime(String name) {
        super(name);
    }

    @Override
    final protected boolean data(T t, StringBuilder sb) {
        return DataWriter.DATE_TIME.write(data(t), sb);
    }

    protected abstract XMLGregorianCalendar data(T t);
}
