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
import java.util.HashMap;
import java.util.List;



import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;


/**
 * TypedCode organizes implementation code based on the Type and Version of code it works with so that it can
 * be located quickly at runtime based on the "Accept" HTTP Header.
 *
 * FYI: For those in the future wondering why I would create a specialized set of "Pair" for the data content:
 *   1) TypeCode is used in Route, and this code is used for every transaction... it needs to be blazingly fast
 *   2) The actual number of objects accessed is quite small and built at startup.  Arrays are best
 *   3) I needed a small, well defined tree where each level is a different Type.  Using a "Pair" Generic definitions,
 *      I created type-safety at each level, which you can't get from a TreeSet, etc.
 *   4) Chaining through the Network is simply object dereferencing, which is as fast as Java can go.
 *   5) The drawback is that in your code is that all the variables are named "x" and "y", which can be a bit hard to
 *       read both in code, and in the debugger.  However, TypeSafety allows your IDE (Eclipse) to help you make the
 *      choices.  Also, make sure you have a good "toString()" method on each object so you can see what's happening
 *      in the IDE Debugger.
 *
 * Empirically, this method of obtaining routes proved to be much faster than the HashSet implementations available in otherwise
 * competent Open Source.
 *
 * @author Jonathan
 *
 * @param <TRANS>
 */
public class TypedCode<TRANS extends Trans> extends Content<TRANS> {
        private List<Pair<String, Pair<HttpCode<TRANS,?>,List<Pair<String, Object>>>>> types;

        public TypedCode() {
            types = new ArrayList<>();
        }

        /**
         * Construct Typed Code based on ContentType parameters passed in
         *
         * @param code
         * @param others
         * @return
         */
        public TypedCode<TRANS> add(HttpCode<TRANS,?> code, String ... others) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String str : others) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(str);
            }
            parse(code, sb.toString());

            return this;
        }

        @Override
        protected Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> types(HttpCode<TRANS,?> code, String str) {
            Pair<String, Pair<HttpCode<TRANS,?>,List<Pair<String, Object>>>> type = null;
            ArrayList<Pair<String, Object>> props = new ArrayList<>();
            // Want Q percentage is to be first in the array everytime.  If not listed, 1.0 is default
            props.add(new Pair<String,Object>(Q,1f));
            Pair<HttpCode<TRANS,?>, List<Pair<String,Object>>> cl = new Pair<HttpCode<TRANS,?>, List<Pair<String,Object>>>(code, props);
//            // breakup "plus" stuff, i.e. application/xaml+xml
//            int plus = str.indexOf('+');
//            if (plus<0) {
                type = new Pair<String, Pair<HttpCode<TRANS,?>,List<Pair<String,Object>>>>(str, cl);
                types.add(type);
                return type;
//            } else {
//                int prev = str.indexOf('/')+1;
//                String first = str.substring(0,prev);
//                String nstr;
//                while (prev!=0) {
//                    nstr = first + (plus>-1?str.substring(prev,plus):str.substring(prev));
//                    type = new Pair<String, Pair<HttpCode<TRANS,?>,List<Pair<String,Object>>>>(nstr, cl);
//                    types.add(type);
//                    prev = plus+1;
//                    plus = str.indexOf('+',prev);
//                }
//            return type;
//            }
        }

        @Override
        protected boolean props(Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> type, String tag, String value) {
            if (tag.equals(Q)) { // reset the Q value (first in array)
                boolean rv = true;
                try {
                    type.y.y.get(0).y=Float.parseFloat(value);
                    return rv;
                } catch (NumberFormatException e) {
                    rv=false; // Note: this awkward syntax forced by Sonar, which doesn't like doing nothing with Exception
                              // which is what should happen
                }
            }
            return type.y.y.add(new Pair<String,Object>(tag,"version".equals(tag)?new Version(value):value));
        }

        public Pair<String, Pair<HttpCode<TRANS, ?>, List<Pair<String, Object>>>> prep(TRANS trans, String compare){
            Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> c,rv=null;
            if (types.size()==1 && "".equals((c=types.get(0)).x)) { // if there are no checks for type, skip
                rv = c;
            } else {
                if (compare==null || compare.length()==0) {
                    rv = types.get(0); // first code is used
                } else {
                    Acceptor<TRANS> acc = new Acceptor<TRANS>(types);
                    boolean accepted;
                    TimeTaken tt = trans.start(compare, Env.SUB);
                    try {
                        accepted = acc.parse(null, compare);
                    } finally {
                        tt.done();
                    }
                    if (accepted) {
                        switch(acc.acceptable.size()) {
                            case 0:
//                                // TODO best Status Code?
//                                resp.setStatus(HttpStatus.NOT_ACCEPTABLE_406);
                                break;
                            case 1:
                                rv = acc.acceptable.get(0);
                                break;
                            default: // compare Q values to get Best Match
                                float bestQ = -1.0f;
                                Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> bestT = null;
                                for (Pair<String, Pair<HttpCode<TRANS,?>, List<Pair<String, Object>>>> type : acc.acceptable) {
                                    Float f = (Float)type.y.y.get(0).y; // first property is always Q
                                    if (f>bestQ) {
                                        bestQ=f;
                                        bestT = type;
                                    }
                                }
                                if (bestT!=null) {
                                    // When it is a GET, the matched type is what is returned, so set ContentType
//                                    if (isGet)resp.setContentType(bestT.x); // set ContentType of Code<TRANS,?>
//                                    rv = bestT.y.x;
                                    rv = bestT;
                                }
                        }
                    } else {
                        trans.checkpoint("No Match found for Accept");
                    }
                }
            }
            return rv;
        }

        /**
         * Print on String Builder content related to specific Code
         *
         * This is for Reporting and Debugging purposes, so the content is not cached.
         *
         * If code is "null", then all content is matched
         *
         * @param code
         * @return
         */
        public StringBuilder relatedTo(HttpCode<TRANS, ?> code, StringBuilder sb) {
            boolean first = true;
            for (Pair<String, Pair<HttpCode<TRANS, ?>, List<Pair<String, Object>>>> pair : types) {
                if (code==null || pair.y.x == code) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(',');
                    }
                    sb.append(pair.x);
                    for (Pair<String,Object> prop : pair.y.y) {
                        // Don't print "Q".  it's there for internal use, but it is only meaningful for "Accepts"
                        if (!prop.x.equals(Q) || !prop.y.equals(1f) ) {
                            sb.append(';');
                            sb.append(prop.x);
                            sb.append('=');
                            sb.append(prop.y);
                        }
                    }
                }
            }
            return sb;
        }

        public List<Pair<String, Object>> getContent(HttpCode<TRANS,?> code) {
            for (Pair<String, Pair<HttpCode<TRANS, ?>, List<Pair<String, Object>>>> pair : types) {
                if (pair.y.x == code) {
                    return pair.y.y;
                }
            }
            return null;
        }

        public String toString() {
            return relatedTo(null,new StringBuilder()).toString();
        }

        public void api(RouteReport tr) {
            // Need to build up a map, because Prop entries can be in several places.
            HashMap<HttpCode<?,?>,StringBuilder> psb = new HashMap<>();
            StringBuilder temp;
            tr.desc = null;

            // Read through Code/TypeCode trees for all accepted Typecodes
            for (Pair<String, Pair<HttpCode<TRANS, ?>, List<Pair<String, Object>>>> tc : types) {
                // If new, then it's new Code set, create prefix content
                if ((temp=psb.get(tc.y.x))==null) {
                    psb.put(tc.y.x,temp=new StringBuilder());
                    if (tr.desc==null) {
                        tr.desc = tc.y.x.desc();
                    }
                } else {
                    temp.append(',');
                }
                temp.append(tc.x);

                // add all properties
                for (Pair<String, Object> props : tc.y.y) {
                    temp.append(';');
                    temp.append(props.x);
                    temp.append('=');
                    temp.append(props.y);
                }
            }
            // Gather all ContentType possibilities for the same code together

            for (StringBuilder sb : psb.values()) {
                tr.contextTypes.add(sb.toString());
            }
        }

        public String first() {
            if (types.size()>0) {
                return types.get(0).x;
            }
            return null;
        }

    }