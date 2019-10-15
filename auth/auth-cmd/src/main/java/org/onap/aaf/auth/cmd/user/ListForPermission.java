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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;

import org.onap.aaf.auth.cmd.AAFcli;
import org.onap.aaf.auth.cmd.Cmd;
import org.onap.aaf.auth.cmd.Param;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.client.Future;
import org.onap.aaf.cadi.client.Rcli;
import org.onap.aaf.cadi.client.Retryable;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;

import aaf.v2_0.Users;
import aaf.v2_0.Users.User;

/**
 * p
 * @author Jonathan
 *
 */
public class ListForPermission extends Cmd {
    private static final String HEADER = "List Users for Permission";
    public ListForPermission(List parent) {
        super(parent,"perm",
                new Param("type",true),
                new Param("instance",true),
                new Param("action",true));
    }

    @Override
    public int _exec(final int index, final String ... args) throws CadiException, APIException, LocatorException {
        return same(new Retryable<Integer>() {
            @Override
            public Integer code(Rcli<?> client) throws CadiException, APIException {
                int idx = index;
                String type = args[idx++];
                String instance = args[idx++];
                if ("\\*".equals(instance))instance="*";
                String action = args[idx++];
                if ("\\*".equals(action))action="*";
                try {
                    Future<Users> fp = client.read(
                            "/authz/users/perm/" +
                                type + '/' +
                                URLEncoder.encode(instance,Config.UTF_8) + '/' +
                                action,
                            getDF(Users.class)
                            );
                    if (fp.get(AAFcli.timeout())) {
                        if (aafcli.isTest())
                            Collections.sort(fp.value.getUser(), (Comparator<User>) (u1, u2) -> u1.getId().compareTo(u2.getId()));
                        ((org.onap.aaf.auth.cmd.user.List)parent).report(fp.value,false,HEADER,type+"|"+instance+"|"+action);
                        if (fp.code()==404)return 200;
                    } else {
                        error(fp);
                    }
                    return fp.code();
                } catch (UnsupportedEncodingException e) {
                    throw new CadiException(e);
                }
            }
        });
    }

    @Override
    public void detailedHelp(int _indent, StringBuilder sb) {
            int indent = _indent;
        detailLine(sb,indent,HEADER);
        indent+=2;
        detailLine(sb,indent,"This report lists the users associated to Permissions.  Since Users");
        detailLine(sb,indent,"are associated to Roles, and Roles have Permissions, this report");
        detailLine(sb,indent,"accomodates all these linkages.");
        sb.append('\n');
        detailLine(sb,indent,"The URL must contain the Permission's type,instance and action, and ");
        detailLine(sb,indent,"may include \"*\"s (type in as \\\\*).");
        detailLine(sb,indent,"See Perm Create Documentation for definitions.");
        indent-=2;
        api(sb,indent,HttpMethods.GET,"authz/users/perm/<type>/<instance>/<action>",Users.class,true);
    }
}
