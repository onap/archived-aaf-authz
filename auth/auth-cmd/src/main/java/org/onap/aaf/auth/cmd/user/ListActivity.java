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

import aaf.v2_0.History;

/**
 *  * @author Jonathan
 *
 */
public class ListActivity extends Cmd {
    private static final String HEADER = "List Activity of User";

    public ListActivity(List parent) {
        super(parent,"activity",
                new Param("user",true));
    }

    @Override
    public int _exec(int _idx, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = _idx;
        final String user = fullID(args[idx++]);
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {

                Future<History> fp = client.read(
                        "/authz/hist/user/"+user,
                        getDF(History.class)
                        );
                if (fp.get(AAFcli.timeout())) {
                    activity(fp.value,HEADER + " [ " + user + " ]");
                } else {
                    error(fp);
                }
                return fp.code();
            }
        });
    }


    @Override
    public void detailedHelp(int indent, StringBuilder sb) {
        detailLine(sb,indent,HEADER);
        api(sb,indent,HttpMethods.GET,"authz/hist/user/<user>",History.class,true);
    }

}
