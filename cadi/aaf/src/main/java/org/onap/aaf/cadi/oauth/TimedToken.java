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

package org.onap.aaf.cadi.oauth;

import java.nio.file.Path;

import org.onap.aaf.cadi.persist.Persist;
import org.onap.aaf.cadi.persist.Persistable;
import org.onap.aaf.cadi.persist.Persisting;

import aafoauth.v2_0.Token;

/**
 * TimedToken
 *   Tokens come from the Token Server with an "Expired In" setting.  This class will take that, and
 *   create a date from time of Creation, which works with local code.
 *   
 * We create a Derived class, so that it can be used as is the originating Token type.
 *
 * "expired" is local computer time 
 * @author Jonathan
 *
 */
// Package on purpose
public class TimedToken extends Token implements Persistable<Token> {
    private Persisting<Token> cacheable; // no double inheritance... 

//    public TimedToken(Token t, byte[] hash) {
//        this(t,(System.currentTimeMillis()/1000)+t.getExpiresIn(),hash,null);
//    }
//
    public TimedToken(Persist<Token,?> p, Token t, byte[] hash, Path path){
        this(p,t,t.getExpiresIn()+(System.currentTimeMillis()/1000),hash, path);
    }

    public TimedToken(Persist<Token,?> p, Token t, long expires_secsFrom1970, byte[] hash, Path path) {
        cacheable = new Persisting<Token>(p, t,expires_secsFrom1970, hash, path);
        accessToken=t.getAccessToken();
        expiresIn=t.getExpiresIn();
        refreshToken=t.getRefreshToken();
        scope = t.getScope();
        state = t.getState();
        tokenType = t.getTokenType();
    }


    @Override
    public Token get() {
        return cacheable.get();
    }

    @Override
    public boolean checkSyncTime() {
        return cacheable.checkSyncTime();
    }

    @Override
    public boolean checkReloadable() {
        return cacheable.checkReloadable();
    }

    @Override
    public boolean hasBeenTouched() {
        return cacheable.hasBeenTouched();
    }

    @Override
    public long expires() {
        return cacheable.expires();
    }

    @Override
    public boolean expired() {
        return cacheable.expired();
    }

    @Override
    public boolean match(byte[] hashIn) {
        return cacheable.match(hashIn);
    }

    @Override
    public byte[] getHash() {
        return cacheable.getHash();
    }

    @Override
    public void inc() {
        cacheable.inc();
    }

    @Override
    public int count() {
        return cacheable.count();
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.oauth.Persistable#clearCount()
     */
    @Override
    public void clearCount() {
        cacheable.clearCount();
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.persist.Persistable#path()
     */
    @Override
    public Path path() {
        return cacheable.path();
    }

}
