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

package org.onap.aaf.auth.batch.helpers;

import com.datastax.driver.core.Row;

public abstract class Creator<T> {
    public abstract T create(Row row);
    public abstract String select();

    public String suffix() {
        return "";
    }

    public String query(String where) {
        StringBuilder sb = new StringBuilder(select());
        if (where!=null) {
            sb.append(" WHERE ");
            int index = where.indexOf(" ALLOW FILTERING");
            if(index< 0 ) {
                sb.append(where);
                sb.append(suffix());
            } else {
                sb.append(where.substring(0, index));
                sb.append(suffix());
                sb.append(" ALLOW FILTERING");
            }
        }
        sb.append(';');
        return sb.toString();
    }


}