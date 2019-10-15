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

package org.onap.aaf.cadi.persist;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.onap.aaf.cadi.Access.Level;

public class Persisting<T> implements Persistable<T> {
    private static final byte[] EMPTY = new byte[0];
    private final byte[] hash; // need to be able to validate disk entry

    private static final long SYNC_TIME = 1000*60*1L; // Checking File change max 1 min
    private FileTime lastTouched;
    private int count;
    private long expires;
    private long nextCheck;
    private T t;
    private Path path;
    private Persist<T, ?> persist;

    public Persisting(Persist<T,?> p, T t, long expiresSecsFrom1970, byte[] hash, Path path) {
        persist = p;
        this.t=t;
        expires = expiresSecsFrom1970;
        this.path = path;
        try {
            lastTouched = Files.getLastModifiedTime(path);
        } catch (IOException e) {
            lastTouched = null;
        }
        count=0;
        nextCheck=0;
        if (hash==null) {
            this.hash = EMPTY;
        } else {
            this.hash = hash;
        }
    }

    @Override
    public T get() {
        return t;
    }

    @Override
    public long expires() {
        return expires;
    }

    @Override
    public boolean expired() {
        return System.currentTimeMillis()/1000>expires;
    }

    @Override
    public boolean hasBeenTouched() {
        try {
            FileTime modT = Files.getLastModifiedTime(path);
            if (lastTouched==null) {
                lastTouched = modT;
                return true;
            } else {
                return !modT.equals(lastTouched);
            }
        } catch (NoSuchFileException e) {
            persist.access.log(Level.DEBUG, "File not found " +  e.getMessage() + ", this is ok, marking as touched.");
            return true;
        } catch (IOException e) {
            persist.access.log(e, "Accessing File Time");
            return true;
        }
    }

    @Override
    public synchronized boolean checkSyncTime() {
        long temp=System.currentTimeMillis();
        if (nextCheck==0 || nextCheck<temp) {
            nextCheck = temp+SYNC_TIME;
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.oauth.Persistable#checkReloadTime()
     */
    @Override
    public boolean checkReloadable() {
        //TODO other elements to add here... 
        // Ideas:  Is it valid?
        //         if not, How many times has it been checked in the last minute
        return expired();
    }

    @Override
    public byte[] getHash() {
        return hash;
    }

    @Override
    public boolean match(byte[] hashIn) {
        if (hash==null || hashIn==null || hash.length!=hashIn.length) {
            return false;
        }
        for (int i=0;i<hashIn.length;++i) {
            if (hash[i]!=hashIn[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized void inc() {
        ++count;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.oauth.Cacheable#count()
     */
    @Override
    public int count() {
        return count;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.cadi.oauth.Persistable#clearCount()
     */
    @Override
    public synchronized void clearCount() {
        count=0;
    }

    @Override
    public Path path() {
        return path;
    }

}
