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

package org.onap.aaf.auth.rserv;

import java.util.List;

import org.onap.aaf.misc.env.Trans;



/**
 * A Class to hold Service "ContentTypes", and to match incoming "Accept" types from HTTP.
 * <p>
 * This is a multi-use class built to use the same Parser for ContentTypes and Accept.
 * <p>
 * Thus, you would create and use "Content.Type" within your service, and use it to match
 * Accept Strings.  What is returned is an Integer (for faster processing), which can be
 * used in a switch statement to act on match different Actions.  The server should
 * know which behaviors match.
 * <p>
 * "bestMatch" returns an integer for the best match, or -1 if no matches.
 *
 * @author Jonathan
 *
 */
public abstract class Content<TRANS extends Trans> {
    public static final String Q = "q";
    protected abstract Pair<String,Pair<HttpCode<TRANS,?>,List<Pair<String,Object>>>> types(HttpCode<TRANS,?> code, String str);
    protected abstract boolean props(Pair<String, Pair<HttpCode<TRANS,?>,List<Pair<String,Object>>>> type, String tag, String value);

    /**
     * Parse a Content-Type/Accept.  As found, call "types" and "props", which do different
     * things depending on if it's a Content-Type or Accepts. 
     * <p>
     * For Content-Type, it builds a tree suitable for Comparison
     * For Accepts, it compares against the tree, and builds an acceptable type list
     * <p>
     * Since this parse code is used for every incoming HTTP transaction, I have removed the implementation
     * that uses String.split, and replaced with integers evaluating the Byte array.  This results
     * in only the necessary strings created, resulting in 1/3 better speed, and less 
     * Garbage collection.
     * <p>
     * @param trans
     * @param code
     * @param cntnt
     * @return
     */
    protected boolean parse(HttpCode<TRANS,?> code, String cntnt) {
        byte bytes[] = cntnt.getBytes();
        boolean contType=false,contProp=true;
        int cis,cie=-1,cend;
        int sis,sie,send;
        do {
            cis = cie+1;
            cie = cntnt.indexOf(',',cis);
            cend = cie<0?bytes.length:cie;
            // Start SEMIS
            sie=cis-1;
            Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> me = null;
            do {
                sis = sie+1;
                sie = cntnt.indexOf(';',sis);
                send = sie>cend || sie<0?cend:sie;
                if (me==null) {
                    String semi = new String(bytes,sis,send-sis);
                    // trans.checkpoint(semi);
                    // Look at first entity within comma group
                    // Is this an acceptable Type?
                    me=types(code, semi);
                    if (me==null) {
                        sie=-1; // skip the rest of the processing... not a type
                    } else {
                        contType=true;
                    }
                } else { // We've looped past the first Semi, now process as properties
                    // If there are additional elements (more entities within Semi Colons)
                    // apply Propertys
                    int eq = cntnt.indexOf('=',sis);
                    if (eq>sis && eq<send) {
                        String tag = new String(bytes,sis,eq-sis);
                        String value = new String(bytes,eq+1,send-(eq+1));
                        // trans.checkpoint("    Prop " + tag + "=" + value);
                        boolean bool =  props(me,tag,value);
                        if (!bool) {
                            contProp=false;
                        }
                    }
                }
                // End Property
            } while (sie<=cend && sie>=cis);
            // End SEMIS
        } while (cie>=0);
        return contType && contProp; // for use in finds, True if a type found AND all props matched
    }

}
