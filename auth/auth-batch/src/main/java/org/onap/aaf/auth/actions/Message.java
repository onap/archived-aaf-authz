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

package org.onap.aaf.auth.actions;

import java.util.ArrayList;
import java.util.List;

public class Message {
    public final List<String> lines;
        
    public Message() {
        lines = new ArrayList<>();
    }

    public void clear() {
        lines.clear();
    }
    
    public String line(String format, Object ... args) {
        String rv=String.format(format, args);
        lines.add(rv);
        return rv;
    }

    public void msg(StringBuilder sb, String lineIndent) {
        if(!lines.isEmpty()) {
            for(String line : lines) {
                sb.append(lineIndent);
                sb.append(line);
                sb.append('\n');
            }
        }
    }
}
