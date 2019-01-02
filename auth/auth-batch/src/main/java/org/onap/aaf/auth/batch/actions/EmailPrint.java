/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright © 2018 IBM.
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

package org.onap.aaf.auth.batch.actions;

import java.io.PrintStream;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.org.Organization;

public class EmailPrint extends Email {

    private static final int LINE_LENGTH = 100;

    public EmailPrint(String... defaultCC) {
        super(defaultCC);
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.actions.Email#exec(org.onap.aaf.auth.org.test.Organization, java.lang.StringBuilder)
     */
    @Override
    protected Result<Void> exec(AuthzTrans trans, Organization org, StringBuilder msg) {
        PrintStream out = System.out;
        boolean first = true;
        out.print("To: ");
        for (String s: toList) {
            if (first) {
                first = false;
            }
            else {out.print(',');}
            out.print(s);
        }
        out.println();
        
        first = true;
        out.print("CC: ");
        for (String s: ccList) {
            if (first) {
                first = false;
            }
            else {out.print(',');}
            out.print(s);
        }
        out.println();

        out.print("Subject: ");
        out.println(subject);
        out.println();
        boolean go = true;
        
        for (int start=0, end=LINE_LENGTH;go;start=end,end=Math.min(msg.length(), start+LINE_LENGTH)) {
            int ret = msg.indexOf("\n",start+1);
            switch(ret) {
                case -1:
                    out.println(msg.substring(start,end));
                    break;
                case 0:
                    end=start+1;
                    out.println();
                    break;
                default:
                    if (ret<end) {
                        end = ret;
                    }
                    if (end==start+LINE_LENGTH) {
                        // Word-wrapping
                        ret = msg.lastIndexOf(" ", end);
                        if (ret>start && ret<end) {
                            end=ret+1;
                        }
                        out.println(msg.substring(start,end));
                    } else {
                        out.print(msg.substring(start,end));
                    }
            }
            go = end<msg.length();
        }
        return Result.ok();

    }

}
