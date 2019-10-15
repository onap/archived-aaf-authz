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

package org.onap.aaf.cadi.client;

import java.io.IOException;

import org.onap.aaf.cadi.SecuritySetter;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.config.SecurityInfoC;

/**
 * AbsAuthentication is a class representing how to Authenticate onto a Client.
 *
 * Methods of setting Authentication on a Client vary, so CLIENT is a Generic Type
 * This allows the ability to apply security onto Different Client Types, as they come 
 * into vogue, or change over time.
 *
 * Password is encrypted at rest.
 *  
 * @author Jonathan
 *
 * @param <CLIENT>
 */
public abstract class AbsAuthentication<CLIENT> implements SecuritySetter<CLIENT> {
    // HTTP Header for Authentication is "Authorization".  This was from an early stage of internet where 
    // Access by Credential "Authorized" you for everything on the site.  Since those early days, it became
    // clear that "full access" wasn't appropriate, so the split between Authentication and Authorization
    // came into being... But the Header remains.
    public static final String AUTHORIZATION = "Authorization";
    private static final Symm symm;

    protected static final String REPEAT_OFFENDER = "This call is aborted because of repeated usage of invalid Passwords";
    private static final int MAX_TEMP_COUNT = 10;
    private static final int MAX_SPAM_COUNT = 10000;
    private static final long WAIT_TIME = 1000*60*4L;
    private final byte[] headValue;
    private String user;
    protected final SecurityInfoC<CLIENT> securityInfo;
    protected long lastMiss;
    protected int count;

    static {
        try {
            symm = Symm.encrypt.obtain();
        } catch (IOException e) {
            throw new RuntimeException("Cannot create critical internal encryption key",e);
        }
    
    }

    public AbsAuthentication(final SecurityInfoC<CLIENT> securityInfo, final String user, final byte[] headValue) throws IOException {
        this.headValue = headValue==null?null:symm.encode(headValue);
        this.user = user;
        this.securityInfo = securityInfo;
        lastMiss=0L;
        count=0;
    }

    protected String headValue() throws IOException {
        if (headValue==null) {
            return "";
        } else {
            return new String(symm.decode(headValue));
        }
    }

    protected void setUser(String id) {
        user = id;
    }

    @Override
    public String getID() {
        return user;
    }

    public boolean isDenied() {
        if (lastMiss>0 && lastMiss>System.currentTimeMillis()) {
            return true;
        } else {
            lastMiss=0L;
            return false;
        }
    }

    public synchronized int setLastResponse(int httpcode) {
        if (httpcode == 401) {
            ++count;
            if (lastMiss==0L && count>MAX_TEMP_COUNT) {
                lastMiss=System.currentTimeMillis()+WAIT_TIME;
            }
            //                if (count>MAX_SPAM_COUNT) {
            //                    System.err.printf("Your service has %d consecutive bad service logins to AAF. \nIt will now exit\n",
            //                            count);
            //                    System.exit(401);
            //                }
            if (count%1000==0) {
                System.err.printf("Your service has %d consecutive bad service logins to AAF. AAF Access will be disabled after %d\n",
                        count,MAX_SPAM_COUNT);
            }

        } else {
            lastMiss=0;
        }
        return count;
    }

    public int count() {
        return count;
    }

}