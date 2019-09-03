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

package org.onap.aaf.auth.env;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.org.Organization;
import org.onap.aaf.cadi.Lur;
import org.onap.aaf.cadi.Permission;
import org.onap.aaf.cadi.principal.TaggedPrincipal;
import org.onap.aaf.misc.env.Decryptor;
import org.onap.aaf.misc.env.Encryptor;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.StaticSlot;
import org.onap.aaf.misc.env.TimeTaken;

/**
 * A NULL implementation of AuthzTrans, for use in DirectAAF Taf/Lurs
 */
public class NullTrans implements AuthzTrans {
    private static final AuthzTrans singleton = new NullTrans();
    
    public static final AuthzTrans singleton() {
        return singleton;
    }

    private Date now;
    
    public void checkpoint(String text) {}
    public void checkpoint(String text, int additionalFlag) {}
    public Metric auditTrail(int indent, StringBuilder sb, int... flag) {return null;}

    @Override
    public Metric auditTrail(LogTarget lt, int indent, StringBuilder sb, int... flag) {
        return null;
    }

    public LogTarget fatal() {
        return LogTarget.NULL;
    }

    public LogTarget error() {
        return LogTarget.NULL;
    }

    public LogTarget audit() {
        return LogTarget.NULL;
    }

    /* (non-Javadoc)
     * @see com.att.env.Env#init()
     */
    @Override
    public LogTarget init() {
        return LogTarget.NULL;
    }

    public LogTarget warn() {
        return LogTarget.NULL;
    }

    public LogTarget info() {
        return LogTarget.NULL;
    }

    public LogTarget debug() {
        return LogTarget.NULL;
    }

    public LogTarget trace() {
        return LogTarget.NULL;
    }

    @Override
    public TimeTaken start(String name, int flag, Object ... values) {
        return new TimeTaken(name,flag, values) {
            public void output(StringBuilder sb) {
                sb.append(String.format(name,values));
                sb.append(' ');
                sb.append(millis());
                sb.append("ms");
            }
        };
    }

    @Override
    public String setProperty(String tag, String value) {
        return value;
    }

    @Override
    public String getProperty(String tag) {
        return tag;
    }

    @Override
    public String getProperty(String tag, String deflt) {
        return deflt;
    }

    @Override
    public Decryptor decryptor() {
        return null;
    }

    @Override
    public Encryptor encryptor() {
        return null;
    }
    @Override
    public AuthzTrans set(HttpServletRequest req, HttpServletResponse resp) {
        return null;
    }

    @Override
    public HttpServletRequest hreq() {
         return null;
    }

    @Override
    public HttpServletResponse hresp() {
         return null;
    }
    

    @Override
    public String user() {
        return null;
    }

    @Override
    public TaggedPrincipal getUserPrincipal() {
        return null;
    }

    @Override
    public void setUser(TaggedPrincipal p) {
    }
    
    @Override
    public String ip() {
        return null;
    }

    @Override
    public int port() {
        return 0;
    }
    @Override
    public String meth() {
        return null;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public void put(Slot slot, Object value) {
    }
    @Override
    public <T> T get(Slot slot, T deflt) {
        return null;
    }
    @Override
    public <T> T get(StaticSlot slot, T dflt) {
        return null;
    }
    @Override
    public Slot slot(String name) {
        return null;
    }
    @Override
    public AuthzEnv env() {
        return null;
    }
    @Override
    public String agent() {
        return null;
    }

    @Override
    public void setLur(Lur lur) {
    }

    @Override
    public Lur getLur() {
    	return null;
    }

    @Override
    public boolean fish(Permission ... p) {
        return false;
    }

    @Override
    public Organization org() {
        return Organization.NULL;
    }

    @Override
    public void logAuditTrail(LogTarget lt) {
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#requested(org.onap.aaf.auth.env.test.AuthzTrans.REQD_TYPE)
     */
    @Override
    public boolean requested(REQD_TYPE requested) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.env.test.AuthzTrans#requested(org.onap.aaf.auth.env.test.AuthzTrans.REQD_TYPE, boolean)
     */
    @Override
    public void requested(REQD_TYPE requested, boolean b) {
    }

    @Override
    public Date now() {
        if (now==null) {
            now = new Date();
        }
        return now;
    }
    @Override
    public void setTag(String tag) {
    }
    @Override
    public String getTag() {
        return null;
    }
    @Override
    public void clearCache() {
    }
}

