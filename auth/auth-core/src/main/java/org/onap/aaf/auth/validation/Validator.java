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

package org.onap.aaf.auth.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.onap.aaf.auth.layer.Result;


public class Validator {
    private static final String ESSENTIAL = "\\x25\\x28\\x29\\x2C-\\x2E\\x30-\\x39\\x3D\\x40-\\x5A\\x5F\\x61-\\x7A";
    private static final Pattern ESSENTIAL_CHARS = Pattern.compile("["+ESSENTIAL+"]+");
    public static final Pattern ACTION_CHARS = Pattern.compile(
                "["+ESSENTIAL+"]+" +    // All AlphaNumeric+
                "|\\*"                        // Just Star
                );
    public static final Pattern INST_CHARS = Pattern.compile(
                "["+ESSENTIAL+"]+[\\*]*" +                // All AlphaNumeric+ possibly ending with *
                "|\\*" +                                // Just Star
                "|(([:/]\\*)|([:/][!]{0,1}["+ESSENTIAL+"]+[\\*]*[:/]*))+"    // Key :asdf:*:sdf*:sdk
                );
    public static final Pattern ID_CHARS = Pattern.compile("[\\w.-]+@[\\w.-]+");
    public static final Pattern NAME_CHARS = Pattern.compile("[\\w.-]+");
    public static final Pattern DESC_CHAR = Pattern.compile("["+ESSENTIAL+"\\x20]+");
    protected static List<String> nsKeywords;
    private final Pattern actionChars;
    private final Pattern instChars;
    private StringBuilder msgs;

    static {
        nsKeywords = new ArrayList<>();
        nsKeywords.add(".access");
        nsKeywords.add(".owner");
        nsKeywords.add(".admin");
        nsKeywords.add(".member");
        nsKeywords.add(".perm");
        nsKeywords.add(".role");
        nsKeywords.add(".ns");
        nsKeywords.add(".cred");
    }

    public Validator() {
        actionChars = ACTION_CHARS;
        instChars = INST_CHARS;
    }

    public final String errs() {
        return msgs.toString();
    }

    public final Validator nullOrBlank(String name, String str) {
        if (str==null) {
            msg(name + " is null.");
        } else if (str.length()==0) {
            msg(name + " is blank.");
        }
        return this;
    }

    public final Validator isNull(String name, Object o) {
        if (o==null) {
            msg(name + " is null.");
        }
        return this;
    }

    protected final boolean noMatch(String str, Pattern p) {
        return str==null || !p.matcher(str).matches();
    }

    protected final void match(String text, String str, Pattern p) {
        if(str==null || !p.matcher(str).matches()) {
            msg(text);
        }
    }

    protected final boolean nob(String str, Pattern p) {
        return str==null || !p.matcher(str).matches(); 
    }

    protected final void msg(String ... strs) {
        if (msgs==null) {
            msgs=new StringBuilder();
        }
        for (String str : strs) {
            msgs.append(str);
        }
        msgs.append('\n');
    }

    public final boolean err() {
        return msgs!=null;
    }

    public final Validator notOK(Result<?> res) {
        if (res==null) {
            msgs.append("Result object is blank");
        } else if (res.notOK()) {
            msgs.append(res.getClass().getSimpleName()).append(" is not OK");
        }
        return this;
    }

    protected Validator intRange(String text, int target, int start, int end) {
        if (target<start || target>end) {
            msg(text + " is out of range (" + start + '-' + end + ')');
        }
        return this;
    }

    protected Validator floatRange(String text, float target, float start, float end) {
        if (target<start || target>end) {
            msg(text + " is out of range (" + start + '-' + end + ')');
        }
        return this;
    }

    protected Validator description(String type, String description) {
        if (description != null && noMatch(description, DESC_CHAR)) {
            msg(type + " Description is invalid.");
        }
        return this;
    }

    public final Validator permType(String type) {
        if (nob(type,NAME_CHARS)) {
            msg("Perm Type [" +type + "] is invalid.");
        }
        return this;
    }

    public final Validator permTypeWithUser(String user, String type) {
        if (type==null) {
            msg("Perm Type is null");
        } else if (user==null) {
            msg("User is null");
        } else {
            if(!(type.startsWith(user) && type.endsWith(":id"))) {
              if(nob(type,NAME_CHARS)) {
                msg("Perm Type [" + type + "] is invalid.");
              }
            }
        }
        return this;
    }

    public final Validator permType(String type, String ns) {
        if (type==null) {
            msg("Perm Type is null");
        } else if (ns==null) {
            msg("Perm NS is null");
        } else if (nob(type,NAME_CHARS)) {
            msg("Perm Type [" + (ns+(type.length()==0?"":'.')) + type + "] is invalid.");
        }
        return this;
    }

    public final Validator permInstance(String instance) {
        if(!"/".equals(instance) && nob(instance,instChars)) {
            msg("Perm Instance [" + instance + "] is invalid.");
        }
        return this;
    }

    public final Validator permAction(String action) {
        // TODO check for correct Splits?  Type|Instance|Action ?
        if (nob(action, actionChars)) {
            msg("Perm Action [" + action + "] is invalid.");
        }
        return this;
    }

    public final Validator role(String user, String role) {
        boolean quit = false;
        if(role==null) {
            msg("Role is null");
            quit = true;
        }
        if(user==null) {
            msg("User is null");
            quit = true;
        }
        if(!quit) {
            if(role.startsWith(user) && role.endsWith(":user")) {
                if(!(role.length() == user.length() + 5)) {
                    msg("Role [" + role + "] is invalid.");
                }
            } else if (nob(role, NAME_CHARS)) {
                msg("Role [" + role + "] is invalid.");
            }
        }
        return this;
    }


    public final Validator role(String role) {
        if (nob(role, NAME_CHARS)) {
            msg("Role [" + role + "] is invalid.");
        }
        return this;
    }

    public final Validator ns(String ns) {
        if (ns==null) {
            msg("NS is null");
            return this;
        } else if (nob(ns,NAME_CHARS)) {
            msg("NS [" + ns + "] is invalid.");
        } 
        for (String s : nsKeywords) {
            if (ns.endsWith(s)) {
                msg("NS [" + ns + "] may not be named with NS keywords");
                break;
            }
        }
        return this;
    }

    public final Validator key(String key) {
        if (nob(key,NAME_CHARS)) {
            msg("NS Prop Key [" + key + "] is invalid");
        }
        return this;
    }

    public final Validator value(String value) {
        if (nob(value,ESSENTIAL_CHARS)) {
            msg("NS Prop value [" + value + "] is invalid");
        }
        return this;
    }


}
