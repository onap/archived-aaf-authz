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

package org.onap.aaf.auth.cmd.user;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Delgs;

/**
 *  * @author Jonathan
 *
 */
public class ListDelegates extends Cmd {
    private static final String HEADER = "List Delegates"; 
    private static final String[] options = {"user","delegate"};
    public ListDelegates(List parent) {
        super(parent,"delegates", 
                new Param(optionsToString(options),true),
                new Param("id",true));
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
        int idx = _idx;
         final String key = args[idx++];
        //int option = whichOption(options,key);
        final String id = fullID(args[idx++]);
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
    
                Future<Delgs> fp = client.read(
                        "/authz/delegates/" + key + '/' + id, 
                        getDF(Delgs.class)
                        );
                if (fp.get(AAFcli.timeout())) {
                    ((List)parent).report(fp.value,HEADER + " by " + key, id);
                    if (fp.code()==404)return 200;
                } else {
                    error(fp);
                }
                return fp.code();
            }
        });
    }

    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,HEADER);
        indent+=2;
        detailLine(sb,indent,"Delegates are those people temporarily assigned to cover the");
        detailLine(sb,indent,"responsibility of Approving, etc, while the actual Responsible");
        detailLine(sb,indent,"Party is absent.  Typically, this is for Vacation, or Business");
        detailLine(sb,indent,"Travel.");
        sb.append('\n');
        detailLine(sb,indent,"Delegates can be listed by the User or by the Delegate");
        indent-=2;
        api(sb,indent,HttpMethods.GET,"authz/delegates/user/<id>",Delgs.class,true);
        api(sb,indent,HttpMethods.GET,"authz/delegates/delegate/<id>",Delgs.class,false);
    }


}
