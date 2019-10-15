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

package org.onap.aaf.misc.rosetta;


public class Parsed<S> {
    public static final String EXTENSION_TAG="extension";

    public boolean isString;

    public StringBuilder sb;
    public char event;
    public String name;
    public S state;

    public Parsed() {
        this(null);
    }

    // Package on purpose
    Parsed(S theState) {
        sb = new StringBuilder();
        isString = false;
        event = Parse.NONE;
        name = "";
        state = theState;
    }

    public boolean valid() {
        return event!=Parse.NONE;
    }

    public Parsed<S> reuse() {
        isString=false;
        sb.setLength(0);
        event = Parse.NONE;
        name = "";
        // don't touch T...
        return this;
    }

    public void dataIsName() {
        name = sb.toString();
        sb.setLength(0);
    }

    public boolean hasName() {
        return name.length()>0;
    }

    public boolean hasData() {
        return sb.length()>0;
    }

    public String toString() {
        StringBuilder sb2 = new StringBuilder();
        if (event<40)sb2.append((int)event);
        else sb2.append(event);
        sb2.append(" - ");
        sb2.append(name);
        if (sb.length()>0) {
            sb2.append(" : ");
            if (isString)sb2.append('"');
            sb2.append(sb);
            if (isString)sb2.append('"');
        }
        return sb2.toString();
    }

}
