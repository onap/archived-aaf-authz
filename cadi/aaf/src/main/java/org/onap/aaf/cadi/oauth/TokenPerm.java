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

package org.onap.aaf.cadi.oauth;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.aaf.AAFPermission;
import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.cadi.persist.Persisting;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.InJson;
import org.onap.aaf.misc.rosetta.Parse;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.Parsed;
import org.onap.aaf.misc.rosetta.InJson.State;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

import aaf.v2_0.Perms;
import aafoauth.v2_0.Introspect;

public class TokenPerm extends Persisting<Introspect>{
    private static final List<AAFPermission> NULL_PERMS = new ArrayList<>();
    private Introspect introspect;
    private List<AAFPermission> perms;
    private String scopes;
    public TokenPerm(Persist<Introspect,?> p, RosettaDF<Perms> permsDF, Introspect ti, byte[] hash, Path path) throws APIException {
        super(p,ti,ti.getExp(),hash,path); // ti.getExp() is seconds after Jan 1, 1970 )
        this.introspect = ti;
        if (ti.getContent()==null || ti.getContent().length()==0) {
            perms = NULL_PERMS;
        } else {
            LoadPermissions lp;
            try {
                lp = new LoadPermissions(new StringReader(ti.getContent()));
                perms = lp.perms;
            } catch (ParseException e) {
                throw new APIException("Error parsing Content",e);
            }
        }
        scopes = ti.getScope();
    }

    public List<AAFPermission> perms() {
        return perms;
    }

    public String getClientId() {
        return introspect.getClientId();
    }

    public String getUsername() {
        return introspect.getUsername();
    }

    public String getToken() {
        return introspect.getAccessToken();
    }

    public synchronized String getScopes() {
        return scopes;
    }

    public Introspect getIntrospect() {
        return introspect;
    }

    // Direct Parse Perms into List
    public static class LoadPermissions {
        public List<AAFPermission> perms;

        public LoadPermissions(Reader r) throws ParseException {
            PermInfo pi = new PermInfo();
            InJson ij = new InJson();
            Parsed<State> pd =  ij.newParsed();
            boolean inPerms = false, inPerm = false;
            while ((pd = ij.parse(r,pd.reuse())).valid()) {
                switch(pd.event) {
                    case Parse.START_DOC:
                        perms = new ArrayList<>();
                        break;
                    case Parse.START_ARRAY:
                        inPerms = "perm".equals(pd.name);
                        break;
                    case '{':
                        if (inPerms) {
                            inPerm=true;
                            pi.clear();
                        }
                        break;
                    case ',':
                        if (inPerm) {
                            pi.eval(pd);
                        }
                        break;
                    case '}':
                        if (inPerms) {
                            if (inPerm) {
                                pi.eval(pd);
                                AAFPermission perm = pi.create();
                                if (perm!=null) {
                                    perms.add(perm);
                                }
                            }
                            inPerm=false;
                        }
                        break;
                    case Parse.END_ARRAY:
                        if (inPerms) {
                            inPerms=false;
                        }
                        break;
                    case Parse.END_DOC:
                        break;
                }
            }
        }
    }

    // Gathering object for parsing objects, then creating AAF Permission
    private static class PermInfo {
        public String ns,type,instance,action;
        public void clear() {
            ns=type=instance=action=null;
        }
        public void eval(Parsed<State> pd) {
            if (pd.hasName()) {
                switch(pd.name) {
                    case "ns":
                        ns=pd.sb.toString();
                        break;
                    case "type":
                        type=pd.sb.toString();
                        break;
                    case "instance":
                        instance=pd.sb.toString();
                        break;
                    case "action":
                        action=pd.sb.toString();
                        break;
                }
            }
        }
        public AAFPermission create() {
            if (type!=null && instance!=null && action !=null) {
                return new AAFPermission(ns,type, instance, action);
            } else {
                return null;
            }
        }
    }
}