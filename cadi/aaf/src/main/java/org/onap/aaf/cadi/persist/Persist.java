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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.util.Holder;
import org.onap.aaf.cadi.client.Result;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

public abstract class Persist<T,CT extends Persistable<T>> extends PersistFile {
    private static final long ONE_DAY = 86400000L;
    private static final long CLEAN_CHECK = 2*60*1000L; // check every 2 mins
    private static Timer clean;

    // store all the directories to review
    // No Concurrent HashSet, or at least, it is all implemented with HashMap in older versions
    private static Queue<Persist<?,?>> allPersists = new ConcurrentLinkedQueue<Persist<?,?>>();

    private Map<String,CT> tmap;
    protected RosettaEnv env;
    private RosettaDF<T> df;


    public Persist(Access access, RosettaEnv env, Class<T> cls, String sub_dir) throws CadiException, APIException {
        super(access, sub_dir);
        this.env = env;
        df = env.newDataFactory(cls);
        tmap = new ConcurrentHashMap<>();
        synchronized(Persist.class) {
            if (clean==null) {
                clean = new Timer(true);
                clean.schedule(new Clean(access), 20000, CLEAN_CHECK);
            }
        }
        allPersists.add(this);
    }

    public void close() {
        allPersists.remove(this);
    }

    protected abstract CT newCacheable(T t, long expires_secsFrom1970, byte[] hash, Path path) throws APIException, IOException;

    public RosettaDF<T> getDF() {
        return df;
    }
    public Result<CT> get(final String key, final byte[] hash, Loader<CT> rl) throws CadiException, APIException, LocatorException {
        if (key==null) {
            return null;
        }
        Holder<Path> hp = new Holder<Path>(null);
        CT ct = tmap.get(key);
        // Make sure cached Item is synced with Disk, but only even Minute to save Disk hits
        if (ct!=null && ct.checkSyncTime()) { // check File Time only every SYNC Period (2 min)
            if (ct.hasBeenTouched()) {
                tmap.remove(key);
                ct = null;
                access.log(Level.DEBUG,"File for",key,"has been touched, removing memory entry");
            }
        }

        // If not currently in memory, check with Disk (which might have been updated by other processes)
        if (ct==null) {
            Holder<Long> hl = new Holder<Long>(0L);
            T t;
            if ((t = readDisk(df, hash, key, hp, hl))!=null) {
                try {
                    if ((ct = newCacheable(t,hl.get(),hash,hp.get()))!=null) {
                        tmap.put(key, ct);
                    }
                    access.log(Level.DEBUG,"Read Token from",key);
                } catch (IOException e) {
                    access.log(e,"Reading Token from",key);
                }
            } // if not read, then ct still==null

            // If not in memory, or on disk, get from Remote... IF reloadable (meaning, isn't hitting too often, etc).
            if (ct==null || ct.checkReloadable()) {
                // Load from external (if makes sense)
                Result<CT> rtp = rl.load(key);
                if (rtp.isOK()) {
                    ct = rtp.value;
                    try {
                        Path p = getPath(key);
                        writeDisk(df, ct.get(),ct.getHash(),p,ct.expires());
                        access.log(Level.DEBUG, "Writing token",key);
                    } catch (CadiException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new CadiException(e);
                    }
                } else {
                    return Result.err(rtp);
                }
            }

            if (ct!=null) {
                tmap.put(key, ct);
            }
        } else {
            access.log(Level.DEBUG,"Found token in memory",key);
        }
        // ct can only be not-null here
        ct.inc();
        return Result.ok(200,ct);
    }

    public void put(String key, CT ct) throws CadiException {
        writeDisk(df, ct.get(), ct.getHash(), key, ct.expires());
        tmap.put(key,ct);
    }

    public void delete(String key) {
        tmap.remove(key);
        deleteFromDisk(key);
    }

    public interface Loader<CT> {
        Result<CT> load(String key) throws APIException, CadiException, LocatorException;
    }

    /**
     * Clean will examine resources, and remove those that have expired.
     *
     * If "highs" have been exceeded, then we'll expire 10% more the next time.  This will adjust after each run
     * without checking contents more than once, making a good average "high" in the minimum speed.
     *
     * @author Jonathan
     *
     */
    private static final class Clean extends TimerTask {
        private final Access access;
        private long hourly;

        public Clean(Access access) {
            this.access = access;
            hourly=0;
        }

        private static class Metrics {
            public int mexists = 0, dexists=0;
            public int mremoved = 0, dremoved=0;
        }

        public void run() {
            final long now = System.currentTimeMillis();
            final long dayFromNow = now + ONE_DAY;
            final Metrics metrics = new Metrics();
            for (final Persist<?,?> persist : allPersists) {
                // Clear memory
                if (access.willLog(Level.DEBUG)) {
                    access.log(Level.DEBUG, "Persist: Cleaning memory cache for",persist.tokenPath.toAbsolutePath());
                }
                for (Entry<String, ?> es : persist.tmap.entrySet()) {
                    ++metrics.mexists;
                    Persistable<?> p = (Persistable<?>)es.getValue();
                    if (p.checkSyncTime()) {
                        if (p.count()==0) {
                            ++metrics.mremoved;
                            persist.tmap.remove(es.getKey());
                            access.printf(Level.DEBUG, "Persist: removed cached item %s from memory\n", es.getKey());
                        } else {
                            p.clearCount();
                        }
                    } else if (Files.exists(p.path())) {

                    }
                }
                // Clear disk
                try {
                    final StringBuilder sb = new StringBuilder();
                    Files.walkFileTree(persist.tokenPath, new FileVisitor<Path>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            sb.setLength(0);
                            sb.append("Persist: Cleaning files from ");
                            sb.append(dir.toAbsolutePath());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (attrs.isRegularFile()) {
                                ++metrics.dexists;
                                try {

                                    long exp = persist.readExpiration(file)*1000; // readExpiration is seconds from 1970
                                    if (now > exp) {  // cover for bad token
                                        sb.append("\n\tFile ");
                                        sb.append(file.getFileName());
                                        sb.append(" expired ");
                                        sb.append(Chrono.dateTime(new Date(exp)));
                                        persist.deleteFromDisk(file);
                                        ++metrics.dremoved;
                                    } else if (exp > dayFromNow) {
                                        sb.append("\n\tFile ");
                                        sb.append(file.toString());
                                        sb.append(" data corrupted.");
                                        persist.deleteFromDisk(file);
                                        ++metrics.dremoved;
                                    }
                                } catch (CadiException e) {
                                    sb.append("\n\tError reading File ");
                                    sb.append(file.toString());
                                    sb.append(". ");
                                    sb.append(e.getMessage());
                                    ++metrics.dremoved;
                                }

                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                            access.log(Level.ERROR,"Error visiting file %s (%s)\n",file.toString(),exc.getMessage());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            access.log(Level.DEBUG, sb);
                            return FileVisitResult.CONTINUE;
                        }

                    });
                } catch (IOException e) {
                    access.log(e, "Exception while cleaning Persistance");
                }

            }

            // We want to print some activity of Persistence Check at least hourly, even if no activity has occurred, but not litter the log if nothing is happening
            boolean go=false;
            Level level=Level.WARN;
            if (access.willLog(Level.INFO)) {
                go = true;
                level=Level.INFO;
            } else if (access.willLog(Level.WARN)) {
                go = metrics.mremoved>0 || metrics.dremoved>0 || --hourly <= 0;
            }

            if (go) {
                access.printf(level, "Persist Cache: removed %d of %d items from memory and %d of %d from disk",
                    metrics.mremoved, metrics.mexists, metrics.dremoved, metrics.dexists);
                hourly = 3600000/CLEAN_CHECK;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        close(); // can call twice.
    }



}
