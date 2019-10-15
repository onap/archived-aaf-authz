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

package org.onap.aaf.cadi.aaf;

import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.Permission;
import org.onap.aaf.misc.env.util.Split;

/**
 * A Class that understands the AAF format of Permission (name/type/action)
 *  or String "name|type|action"
 *
 * @author Jonathan
 *
 */
public class AAFPermission implements Permission {
    private static final List<String> NO_ROLES;
    protected String ns,type,instance,action,key;
    private List<String> roles;

    static {
        NO_ROLES = new ArrayList<>();
    }

    protected AAFPermission() {roles=NO_ROLES;}

    public AAFPermission(String ns, String name, String instance, String action) {
        this.ns = ns;
        type = name;
        this.instance = instance;
        this.action = action;
        if (ns==null) {
            key = type + '|' + instance + '|' + action;
        } else {
            key = ns + '|' + type + '|' + instance + '|' + action;
        }
        this.roles = NO_ROLES;

    }

    public AAFPermission(String ns, String name, String instance, String action, List<String> roles) {
        this.ns = ns;
        type = name;
        this.instance = instance;
        this.action = action;
        if (ns==null) {
            key = type + '|' + instance + '|' + action;
        } else {
            key = ns + '|' + type + '|' + instance + '|' + action;
        }
        this.roles = roles==null?NO_ROLES:roles;
    }

    /**
     * Match a Permission
     * if Permission is Fielded type "Permission", we use the fields
     * otherwise, we split the Permission with '|'
     *
     * when the type or action starts with REGEX indicator character ( ! ),
     * then it is evaluated as a regular expression.
     *
     * If you want a simple field comparison, it is faster without REGEX
     */
    public boolean match(Permission p) {
        if(p==null) {
            return false;
        }
        String aafNS;
        String aafType;
        String aafInstance;
        String aafAction;
        if (p instanceof AAFPermission) {
            AAFPermission ap = (AAFPermission)p;
            // Note: In AAF > 1.0, Accepting "*" from name would violate multi-tenancy
            // Current solution is only allow direct match on Type.
            // 8/28/2014 Jonathan - added REGEX ability
            aafNS = ap.getNS();
            aafType = ap.getType();
            aafInstance = ap.getInstance();
            aafAction = ap.getAction();
        } else {
            // Permission is concatenated together: separated by
            String[] aaf = Split.splitTrim('|', p.getKey());
            switch(aaf.length) {
                case 1:
                    aafNS = aaf[0];
                    aafType="";
                    aafInstance = aafAction = "*";
                    break;
                case 2:
                    aafNS = aaf[0];
                    aafType = aaf[1];
                    aafInstance = aafAction = "*";
                    break;
                case 3:
                    aafNS = aaf[0];
                    aafType = aaf[1];
                    aafInstance = aaf[2];
                    aafAction = "*";
                    break;
                default:
                    aafNS = aaf[0];
                    aafType = aaf[1];
                    aafInstance = aaf[2];
                    aafAction = aaf[3];
                break;
            }
        }
        boolean typeMatches;
        if (aafNS==null) {
            if (ns==null) {
                typeMatches = aafType.equals(type);
            } else {
                typeMatches = aafType.equals(ns+'.'+type);
            }
        } else if (ns==null) {
            typeMatches = type.equals(aafNS+'.'+aafType);
        } else if (aafNS.length() == ns.length()) {
            typeMatches = aafNS.equals(ns) && aafType.equals(type);
        } else { // Allow for restructuring of NS/Perm structure
            typeMatches = (aafNS+'.'+aafType).equals(ns+'.'+type);
        }
        return (typeMatches &&
                PermEval.evalInstance(instance, aafInstance) &&
                PermEval.evalAction(action, aafAction));
    }

    public String getNS() {
        return ns;
    }

    public String getType() {
        return type;
    }

    public String getFullType() {
        return ns + '.' + type;
    }

    public String getInstance() {
        return instance;
    }

    public String getAction() {
        return action;
    }

    public String getKey() {
        return key;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.Permission#permType()
     */
    public String permType() {
        return "AAF";
    }

    public List<String> roles() {
        return roles;
    }
    public String toString() {
        return "AAFPermission:" +
                "\n\tNS: " + ns +
                "\n\tType: " + type +
                "\n\tInstance: " + instance +
                "\n\tAction: " + action +
                "\n\tKey: " + key;
    }
}
