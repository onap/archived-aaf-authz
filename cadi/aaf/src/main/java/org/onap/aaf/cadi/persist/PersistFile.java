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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Symm;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.Symm.Encryption;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.rosetta.env.RosettaDF;

public class PersistFile {

    private static final String HASH_NO_MATCH = "Hash does not match in Persistence";
    private static final Object LOCK = new Object();

    protected static Symm symm;
    public Access access;
    protected final Path tokenPath;
    protected final String tokenDir;
    private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");

    public PersistFile(Access access, String sub_dir) throws CadiException, APIException {
        this.access = access;
        tokenPath = Paths.get(access.getProperty(Config.CADI_TOKEN_DIR,"tokens"), sub_dir);
        try {
            if (!Files.exists(tokenPath)) {
                if (isWindows) {
                    // Sorry Windows users, you need to secure your own paths
                    Files.createDirectories(tokenPath);
                } else {
                    Set<PosixFilePermission> spfp = PosixFilePermissions.fromString("rwxr-x---");
                    Files.createDirectories(tokenPath,PosixFilePermissions.asFileAttribute(spfp));
                }
            }
            tokenDir=tokenPath.toRealPath().toString();
        } catch (IOException e) {
            throw new CadiException(e);
        }
        synchronized(LOCK) {
            if (symm==null) {
                symm = Symm.obtain(access);
            }
        }
    }

    public<T> Path writeDisk(final RosettaDF<T> df, final T t, final byte[] cred, final String filename, final long expires) throws CadiException {
        return writeDisk(df,t,cred,Paths.get(tokenDir,filename),expires);
    }

    public<T> Path writeDisk(final RosettaDF<T> df, final T t, final byte[] cred, final Path target, final long expires) throws CadiException {
        // Make sure File is completely written before making accessible on disk... avoid corruption.
        try {
            Path tpath = Files.createTempFile(tokenPath,target.getFileName().toString(), ".tmp");
            final OutputStream dos = Files.newOutputStream(tpath, StandardOpenOption.CREATE,StandardOpenOption.WRITE);
                try {
                // Write Expires so that we can read unencrypted.
                for (int i=0;i<Long.SIZE;i+=8) {
                    dos.write((byte)((expires>>i)&0xFF));
                }

                symm.exec(new Symm.SyncExec<Void>() {
                    @Override
                    public Void exec(Encryption enc) throws Exception {
                        CipherOutputStream os = enc.outputStream(dos, true);
                        try {
                            int size = cred==null?0:cred.length;
                            for (int i=0;i<Integer.SIZE;i+=8) {
                                os.write((byte)((size>>i)&0xFF));
                            }
                            if (cred!=null) {
                                os.write(cred);
                            }
                            df.newData().load(t).to(os);
                        } finally {
                            // Note: Someone on the Web noticed that using a DataOutputStream would not full close out without a flush first,
                            // leaving files open.
                            try {
                                os.flush();
                            } catch (IOException e) {
                                access.log(Level.INFO, "Note: Caught Exeption while flushing CipherStream.  Handled.");
                            }
                            try {
                                os.close();
                            } catch (IOException e) {
                                access.log(Level.INFO, "Note: Caught Exeption while closing CipherStream.  Handled.");
                            }
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                throw new CadiException(e);
            } finally {
                dos.close();
            }
            return Files.move(tpath, target, StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CadiException(e);
        }

    }

    public <T> T readDisk(final RosettaDF<T> df, final byte[] cred, final String filename,final Holder<Path> hp, final Holder<Long> hl) throws CadiException {
        if (hp.get()==null) {
            hp.set(Paths.get(tokenDir,filename));
        }
        return readDisk(df,cred,hp.get(),hl);
    }

    public <T> T readDisk(final RosettaDF<T> df, final byte[] cred, final Path target, final Holder<Long> hexpired) throws CadiException {
        // Try from Disk
        T t = null;
        if (Files.exists(target)) {
            try {
                final InputStream is = Files.newInputStream(target,StandardOpenOption.READ);
                try {
                    // Read Expired unencrypted
                    long exp=0;
                    for (int i=0;i<Long.SIZE;i+=8) {
                        exp |= ((long)is.read()<<i);
                    }
                    hexpired.set(exp);

                    t = symm.exec(new Symm.SyncExec<T>() {
                        @Override
                        public T exec(Encryption enc) throws Exception {
                            CipherInputStream dis = enc.inputStream(is,false);
                            try {
                                int size=0;
                                for (int i=0;i<Integer.SIZE;i+=8) {
                                    size |= ((int)dis.read()<<i);
                                }
                                if (size>256) {
                                    throw new CadiException("Invalid size in Token Persistence");
                                } else if (cred!=null && size!=cred.length) {
                                    throw new CadiException(HASH_NO_MATCH);
                                }
                                if (cred!=null) {
                                    byte[] array = new byte[size];
                                    if (dis.read(array)>0) {
                                        for (int i=0;i<size;++i) {
                                            if (cred[i]!=array[i]) {
                                                throw new CadiException(HASH_NO_MATCH);
                                            }
                                        }
                                    }
                                }
                                return df.newData().load(dis).asObject();
                            } finally {
                                dis.close();
                            }
                        }
                    });
                } finally {
                    is.close();
                }
            } catch (NoSuchFileException e) {
                return t;
            } catch (Exception e) {
                throw new CadiException(e);
            }
        }
        return t;
    }

    public long readExpiration(final Path target) throws CadiException {
        long exp=0L;
        if (Files.exists(target)) {
            try {
                final InputStream is = Files.newInputStream(target,StandardOpenOption.READ);
                try {
                    for (int i=0;i<Long.SIZE;i+=8) {
                        exp |= ((long)is.read()<<i);
                    }
                } finally {
                    is.close();
                }
                return exp;
            } catch (Exception e) {
                throw new CadiException(e);
            }
        }
        return exp;
    }

    public void deleteFromDisk(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            access.log(Level.ERROR, e);
        }
    }

    public void deleteFromDisk(String token) {
        Path tpath = Paths.get(tokenDir,token);
        try {
            Files.deleteIfExists(tpath);
        } catch (IOException e) {
            access.log(Level.ERROR, e);
        }
    }

    public Path getPath(String filename) {
        return Paths.get(tokenDir,filename);
    }

    public FileTime getFileTime(String filename, Holder<Path> hp) throws IOException {
        Path p = hp.get();
        if (p==null) {
            hp.set(p=Paths.get(tokenDir,filename));
        }
        return Files.getLastModifiedTime(p);
    }

}
