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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This path matching algorithm avoids using split strings during the critical transactional run-time.  By pre-analyzing the
 * content at "set Param" time, and storing data in an array-index model which presumably is done once and at the beginning, 
 * we can match in much less time when it actually counts.
 * <p>
 * @author Jonathan
 *
 */
public class Match {
    private Map<String, Integer> params;
    private byte[]     values[];
    private Integer vars[];
    private boolean wildcard;


    /*
     * These two methods are pairs of searching performance for variables Spark Style.
     * setParams evaluates the target path, and sets a HashMap that will return an Integer.
     * the Keys are both :key and key so that there will be no string operations during
     * a transaction
     * <p>
     * For the Integer, if the High Order is 0, then it is just one value.  If High Order >0, then it is 
     * a multi-field option, i.e. ending with a wild-card.
     */
    public Match(String path) {
        // IF DEBUG: System.out.print("\n[" + path + "]");
        params = new HashMap<>();
        if (path!=null) {
            String[] pa = path.split("/");
            values = new byte[pa.length][];
            vars = new Integer[pa.length];
        
            int val = 0;
            String key;
            for (int i=0;i<pa.length && !wildcard;++i) {
                if (pa[i].startsWith(":")) {
                    if (pa[i].endsWith("*")) {
                        val = i | pa.length<<16; // load end value in high order bits
                        key = pa[i].substring(0, pa[i].length()-1);// remove *
                        wildcard = true;
                    } else {
                        val = i;
                        key = pa[i];
                    }
                    params.put(key,val); //put in :key 
                    params.put(key.substring(1,key.length()), val); // put in just key, better than adding a missing one, like Spark
                    // values[i]=null; // null stands for Variable
                    vars[i]=val;
                } else {
                    values[i]=pa[i].getBytes();
                    if (pa[i].endsWith("*")) {
                        wildcard = true;
                        if (pa[i].length()>1) {
                            /* remove * from value */
                            int newlength = values[i].length-1;
                            byte[] real = new byte[newlength];
                            System.arraycopy(values[i],0,real,0,newlength);
                            values[i]=real;
                        } else {
                            vars[i]=0; // this is actually a variable, if it only contains a "*"
                        }
                    }
                    // vars[i]=null;
                }
            }
        }
    }

    /*
     * This is the second of the param evaluation functions.  First, we look up to see if there is
     * any reference by key in the params Map created by the above.
     * <p>
     * The resulting Integer, if not null, is split high/low order into start and end.
     * We evaluate the string for '/', rather than splitting into  String[] to avoid the time/mem needed
     * We traverse to the proper field number for slash, evaluate the end (whether wild card or no), 
     * and return the substring.  
     * <p>
     * The result is something less than .003 milliseconds per evaluation
     * <p>
     */
    public String param(String path,String key) {
        Integer val = params.get(key); // :key or key
        if (val!=null) {
            int start = val & 0xFFFF;
            int end = (val >> 16) & 0xFFFF;
            int idx = -1;
            int i;
            for (i=0;i<start;++i) {
                idx = path.indexOf('/',idx+1);
                if (idx<0)break;
            }
            if (i==start) { 
                ++idx;
                if (end==0) {
                    end = path.indexOf('/',idx);
                    if (end<0)end=path.length();
                } else {
                    end=path.length();
                }
                return path.substring(idx,end);
            } else if (i==start-1) { // if last spot was left blank, i.e. :key*
                return "";
            }
        }
        return null;
    }

    public boolean match(String path) {
        if (path==null|| path.length()==0 || "/".equals(path) ) {
            if (values==null)return true;
            switch(values.length) {
                case 0: return true;
                case 1: return values[0].length==0;
                default: return false;
            }
        }        
        boolean rv = true;
        byte[] pabytes = path.getBytes();
        int field=0;
        int fieldIdx = 0;

        int lastField = values.length;
        int lastByte = pabytes.length;
        boolean fieldMatched = false; // = lastByte>0?(pabytes[0]=='/'):false;
        // IF DEBUG: System.out.println("\n -- " + path + " --");
        for (int i=0;rv && i<lastByte;++i) {
            if (field>=lastField) { // checking here allows there to be a non-functional ending /
                rv = false;
                break;
            }
            if (values[field]==null) { // it's a variable, just look for /s
                if (wildcard && field==lastField-1) return true;// we've made it this far.  We accept all remaining characters
                Integer val = vars[field];
                int start = val & 0xFFFF;
                int end = (val >> 16) & 0xFFFF;
                if (end==0)end=start+1;
                int k = i;
                for (int j=start; j<end && k<lastByte; ++k) {
                    // IF DEBUG: System.out.print((char)pabytes[k]);
                    if (pabytes[k]=='/') {
                        ++field;
                        ++j;
                    }
                }
            
                if (k==lastByte && pabytes[k-1]!='/')++field;
                if (k>i)i=k-1; // if we've incremented, have to accommodate the outer for loop incrementing as well
                fieldMatched = false; // reset
                fieldIdx = 0;
            } else {
                // IF DEBUG: System.out.print((char)pabytes[i]);
                if (pabytes[i]=='/') { // end of field, eval if Field is matched
                    // if double slash, check if supposed to be empty
                    if (fieldIdx==0 && values[field].length==0) {
                        fieldMatched = true;
                    }
                    rv = fieldMatched && ++field<lastField;
                    // reset
                    fieldMatched = false; 
                    fieldIdx = 0;
                } else if (values[field].length==0) {
                    // double slash in path, but content in field.  We check specially here to avoid 
                    // Array out of bounds issues.
                    rv = false;
                } else {
                    if (fieldMatched) {
                        rv =false; // field is already matched, now there's too many bytes
                    } else {
                        rv = pabytes[i]==values[field][fieldIdx++]; // compare expected (pabytes[i]) with value for particular field
                        fieldMatched=values[field].length==fieldIdx; // are all the bytes match in the field?
                        if (fieldMatched && (i==lastByte-1 || (wildcard && field==lastField-1)))
                            return true; // last field info
                    }
                }
            }
        }
        if (field!=lastField || pabytes.length!=lastByte) rv = false; // have we matched all the fields and all the bytes?
        return rv;
    }

    public Set<String> getParamNames() {
        return params.keySet();
    }
}