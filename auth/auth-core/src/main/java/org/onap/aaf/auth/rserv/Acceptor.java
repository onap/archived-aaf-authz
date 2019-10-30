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

package org.onap.aaf.auth.rserv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.onap.aaf.misc.env.Trans;

/**
 * Find Acceptable Paths and place them where TypeCode can evaluate.
 *
 * If there are more than one, TypeCode will choose based on "q" value
 * @author Jonathan
 *
 * @param <TRANS>
 */
class Acceptor<TRANS extends Trans>  {
    private List<Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>>> types;
    List<Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>>> acceptable;

    public Acceptor(List<Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>>> types) {
        this.types = types;
        acceptable = new ArrayList<>();
    }

    private boolean eval(HttpCode<TRANS,?> code, String str, List<String> props) {

//        if (plus<0) {
        boolean ok = false;
        boolean any = false;
        for (Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> type : types) {
            ok = true;
            if (type.x.equals(str)) {
                for (Iterator<String> iter = props.iterator();ok && iter.hasNext();) {
                    ok = props(type,iter.next(),iter.next());
                }
                if (ok) {
                    any = true;
                    acceptable.add(type);
                }
            }
        }
//        } else { // Handle Accepts with "+" as in application/xaml+xml
//            int prev = str.indexOf('/')+1;
//            String first = str.substring(0,prev);
//            String nstr;
//            while (prev!=0) {
//                nstr = first + (plus<0?str.substring(prev):str.substring(prev,plus));
//
//                for (Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> type : types) {
//                    if (type.x.equals(nstr)) {
//                        acceptable.add(type);
//                        return type;
//                    }
//                }
//                prev = plus+1;
//                plus=str.indexOf('+', prev);
//            };
//        }
        return any;
    }

    /**
     * Evaluate Properties
     * @param type
     * @param tag
     * @param value
     * @return
     */
    private boolean props(Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> type, String tag, String value) {
        boolean rv = false;
        if (type.y!=null) {
            for (Pair<String,Object> prop : type.y.y){
                if (tag.equals(prop.x)) {
                    if ("charset".equals(tag)) {
                        return prop.x==null?false:prop.y.equals(value.toLowerCase()); // return True if Matched
                    } else if ("version".equals(tag)) {
                        return prop.y.equals(new Version(value)); // Note: Version Class knows Minor Version encoding
                    } else if (tag.equals(Content.Q)) { // replace Q value
                        try {
                            type.y.y.get(0).y=Float.parseFloat(value);
                        } catch (NumberFormatException e) {
                            rv=false; // need to do something to make Sonar happy. But nothing to do.
                        }
                        return true;
                    } else {
                        return value.equals(prop.y);
                    }
                }
            }
        }
        return rv;
    }

    /**
     * parse
     *
     * Note: I'm processing by index to avoid lots of memory creation, which speeds things
     * up for this time critical section of code.
     * @param code
     * @param cntnt
     * @return
     */
    protected boolean parse(HttpCode<TRANS, ?> code, String cntnt) {
        byte bytes[] = cntnt.getBytes();

        int cis,cie=-1,cend;
        int sis,sie,send;
        String name;
        ArrayList<String> props = new ArrayList<>();
        do {
            // Clear these in case more than one Semi
            props.clear(); // on loop, do not want mixed properties
            name=null;

            cis = cie+1; // find comma start
            while (cis<bytes.length && Character.isSpaceChar(bytes[cis]))++cis;
            cie = cntnt.indexOf(',',cis); // find comma end
            cend = cie<0?bytes.length:cie; // If no comma, set comma end to full length, else cie
            while (cend>cis && Character.isSpaceChar(bytes[cend-1]))--cend;
            // Start SEMIS
            sie=cis-1;
            do {
                sis = sie+1;  // semi start is one after previous end
                while (sis<bytes.length && Character.isSpaceChar(bytes[sis]))++sis;
                sie = cntnt.indexOf(';',sis);
                send = sie>cend || sie<0?cend:sie;  // if the Semicolon is after the comma, or non-existent, use comma end, else keep
                while (send>sis && Character.isSpaceChar(bytes[send-1]))--send;
                if (name==null) { // first entry in Comma set is the name, not a property
                    name = new String(bytes,sis,send-sis);
                } else { // We've looped past the first Semi, now process as properties
                    // If there are additional elements (more entities within Semi Colons)
                    // apply Properties
                    int eq = cntnt.indexOf('=',sis);
                    if (eq>sis && eq<send) {
                        props.add(new String(bytes,sis,eq-sis));
                        props.add(new String(bytes,eq+1,send-(eq+1)));
                    }
                }
                // End Property
            } while (sie<=cend && sie>=cis); // End SEMI processing
            // Now evaluate Comma set and return if true
            if (eval(code,name,props))return true; // else loop again to check next comma
        } while (cie>=0); // loop to next comma
        return false; // didn't get even one match
    }

}