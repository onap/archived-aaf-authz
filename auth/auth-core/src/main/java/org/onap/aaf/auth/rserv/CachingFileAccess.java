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

package org.onap.aaf.auth.rserv;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Store;
import org.onap.aaf.misc.env.Trans;
/*
 * CachingFileAccess
 *
 * Author: Jonathan Gathman, Gathsys 2010
 *
 */
public class CachingFileAccess<TRANS extends Trans> extends HttpCode<TRANS, Void> {
    public static void setEnv(Store store, String[] args) {
        for (int i=0;i<args.length-1;i+=2) { // cover two parms required for each
            if (CFA_WEB_PATH.equals(args[i])) {
                store.put(store.staticSlot(CFA_WEB_PATH), args[i+1]);
            } else if (CFA_CACHE_CHECK_INTERVAL.equals(args[i])) {
                store.put(store.staticSlot(CFA_CACHE_CHECK_INTERVAL), Long.parseLong(args[i+1]));
            } else if (CFA_MAX_SIZE.equals(args[i])) {
                store.put(store.staticSlot(CFA_MAX_SIZE), Integer.parseInt(args[i+1]));
            }
        }
    }

    private static String MAX_AGE = "max-age=3600"; // 1 hour Caching
    private final Map<String,String> typeMap;
    private final NavigableMap<String,Content> content;
    private final Set<String> attachOnly;
    public final static String CFA_WEB_PATH = "aaf_cfa_web_path";
    // when to re-validate from file
    // Re validating means comparing the Timestamp on the disk, and seeing it has changed.  Cache is not marked
    // dirty unless file has changed, but it still makes File IO, which for some kinds of cached data, i.e.
    // deployed GUI elements is unnecessary, and wastes time.
    // This parameter exists to cover the cases where data can be more volatile, so the user can choose how often the
    // File IO will be accessed, based on probability of change.  "0", of course, means, check every time.
    private final static String CFA_CACHE_CHECK_INTERVAL = "aaf_cfa_cache_check_interval";
    private final static String CFA_MAX_SIZE = "aaf_cfa_max_size"; // Cache size limit
    private final static String CFA_CLEAR_COMMAND = "aaf_cfa_clear_command";

    // Note: can be null without a problem, but included
    // to tie in with existing Logging.
    public LogTarget logT = null;
    public long checkInterval; // = 600000L; // only check if not hit in 10 mins by default
    public int maxItemSize; // = 512000; // max file 500k
    private Timer timer;
    private String web_path;
    // A command key is set in the Properties, preferably changed on deployment.
    // it is compared at the beginning of the path, and if so, it is assumed to issue certain commands
    // It's purpose is to protect, to some degree the command, even though it is HTTP, allowing
    // local batch files to, for instance, clear caches on resetting of files.
    private String clear_command;

    public CachingFileAccess(EnvJAXB env, String ... args) throws IOException {
        super(null,"Caching File Access");
        setEnv(env,args);
        content = new ConcurrentSkipListMap<>(); // multi-thread changes possible

        attachOnly = new HashSet<>();     // short, unchanged

        typeMap = new TreeMap<>(); // Structure unchanged after Construction
        typeMap.put("ico","image/icon");
        typeMap.put("html","text/html");
        typeMap.put("css","text/css");
        typeMap.put("js","text/javascript");
        typeMap.put("txt","text/plain");
        typeMap.put("xml","text/xml");
        typeMap.put("xsd","text/xml");
        attachOnly.add("xsd");
        typeMap.put("crl", "application/x-pkcs7-crl");
        typeMap.put("appcache","text/cache-manifest");

        typeMap.put("json","text/json");
        typeMap.put("ogg", "audio/ogg");
        typeMap.put("jpg","image/jpeg");
        typeMap.put("gif","image/gif");
        typeMap.put("png","image/png");
        typeMap.put("svg","image/svg+xml");
        typeMap.put("jar","application/x-java-applet");
        typeMap.put("jnlp", "application/x-java-jnlp-file");
        typeMap.put("class", "application/java");
        typeMap.put("props", "text/plain");
        typeMap.put("jks", "application/octet-stream");

        // Fonts
        typeMap.put("ttf","font/ttf");
        typeMap.put("woff","font/woff");
        typeMap.put("woff2","font/woff2");


        timer = new Timer("Caching Cleanup",true);
        timer.schedule(new Cleanup(content,500),60000,60000);

        // Property params
        web_path = env.get(env.staticSlot(CFA_WEB_PATH));
        env.init().log("CachingFileAccess path: " + new File(web_path).getCanonicalPath());
        Object obj;
        obj = env.get(env.staticSlot(CFA_CACHE_CHECK_INTERVAL),600000L);  // Default is 10 mins
        if (obj instanceof Long) {
          checkInterval=(Long)obj;
        } else {
          checkInterval=Long.parseLong((String)obj);
        }

        obj = env.get(env.staticSlot(CFA_MAX_SIZE), 512000);    // Default is max file 500k
        if (obj instanceof Integer) {
          maxItemSize=(Integer)obj;
        } else {
          maxItemSize =Integer.parseInt((String)obj);
        }

         clear_command = env.getProperty(CFA_CLEAR_COMMAND,null);
    }



    @Override
    public void handle(TRANS trans, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String key = pathParam(req, ":key");
        int slash = key.indexOf('/');
        if(key.length()>2 && slash>=0 && key.substring(0,slash).equals(clear_command)) {
            resp.setHeader("Content-Type",typeMap.get("txt"));
            if ("clear".equals(key.substring(slash+1))) {
                content.clear();
                resp.setStatus(200/*HttpStatus.OK_200*/);
            } else {
                resp.setStatus(400/*HttpStatus.BAD_REQUEST_400 */);
            }
            return;
        }
        Content c = load(logT , web_path,key, null, checkInterval);
        if (c.attachmentOnly) {
            resp.setHeader("Content-disposition", "attachment");
        }
        c.setHeader(resp);
        c.write(resp.getOutputStream());
        trans.checkpoint(req.getPathInfo());
    }


    public String webPath() {
        return web_path;
    }

    /**
     * Reset the Cleanup size and interval
     *
     * The size and interval when started are 500 items (memory size unknown) checked every minute in a background thread.
     *
     * @param size
     * @param interval
     */
    public void cleanupParams(int size, long interval) {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new Cleanup(content,size), interval, interval);
    }



    /**
     * Load a file, first checking cache
     *
     *
     * @param logTarget - logTarget can be null (won't log)
     * @param dataRoot - data root storage directory
     * @param key - relative File Path
     * @param mediaType - what kind of file is it.  If null, will check via file extension
     * @param timeCheck - "-1" will take system default - Otherwise, will compare "now" + timeCheck(Millis) before looking at File mod
     * @return
     * @throws IOException
     */
    public Content load(LogTarget logTarget, String dataRoot, String key, String mediaType, long _timeCheck) throws IOException {
        long timeCheck = _timeCheck;
        if (timeCheck<0) {
            timeCheck=checkInterval; // if time < 0, then use default
        }
        boolean isRoot;
        String fileName;
        if ("-".equals(key)) {
            fileName = dataRoot;
            isRoot = true;
        } else {
            fileName=dataRoot + '/' + key;
            isRoot = false;
        }
        Content c = content.get(key);
        long systime = System.currentTimeMillis();
        File f=null;
        if ((c!=null) && (c.date < systime + timeCheck)) {
            // Don't check every hit... only after certain time value
                f = new File(fileName);
                if (f.lastModified()>c.date) {
                    c=null;
                }
            }
        if (c==null) {
            if (logTarget!=null) {
                logTarget.log("File Read: ",key);
            }

            if (f==null){
                f = new File(fileName);
            }
            boolean cacheMe;
            if (f.exists()) {
                if (f.isDirectory()) {
                    cacheMe = false;
                    c = new DirectoryContent(f,isRoot);
                } else {
                    if (f.length() > maxItemSize) {
                        c = new DirectFileContent(f);
                        cacheMe = false;
                    } else {
                        c = new CachedContent(f);
                        cacheMe = checkInterval>0;
                    }

                    if (mediaType==null) { // determine from file Ending
                        int idx = key.lastIndexOf('.');
                        String subkey = key.substring(++idx);
                        if ((c.contentType = idx<0?null:typeMap.get(subkey))==null) {
                            // if nothing else, just set to default type...
                            c.contentType = "application/octet-stream";
                        }
                        c.attachmentOnly = attachOnly.contains(subkey);
                    } else {
                        c.contentType=mediaType;
                        c.attachmentOnly = false;
                    }

                    c.date = f.lastModified();

                    if (cacheMe) {
                        content.put(key, c);
                    }
                }
            } else {
                c=NULL;
            }
        } else {
            if (logTarget!=null)logTarget.log("Cache Read: ",key);
        }

        // refresh hit time
        c.access = systime;
        return c;
    }


    public void invalidate(String key) {
        content.remove(key);
    }

    private static final Content NULL=new Content() {

        @Override
        public void setHeader(HttpServletResponse resp) {
            resp.setStatus(404/*NOT_FOUND_404*/);
            resp.setHeader("Content-type","text/plain");
        }

        @Override
        public void write(Writer writer) throws IOException {
        }

        @Override
        public void write(OutputStream os) throws IOException {
        }

    };

    private static abstract class Content {
        private long date;   // date of the actual artifact (i.e. File modified date)
        private long access; // last accessed

        protected String  contentType;
        protected boolean attachmentOnly;

        public void setHeader(HttpServletResponse resp) {
            resp.setStatus(200/*OK_200*/);
            resp.setHeader("Content-Type",contentType);
            resp.setHeader("Cache-Control", MAX_AGE);
        }

        public abstract void write(Writer writer) throws IOException;
        public abstract void write(OutputStream os) throws IOException;

    }

    private static class DirectFileContent extends Content {
        private File file;
        public DirectFileContent(File f) {
            file = f;
        }

        public String toString() {
            return file.getName();
        }

        public void write(Writer writer) throws IOException {
            FileReader fr = new FileReader(file);
            char[] buff = new char[1024];
            try {
                int read;
                while ((read = fr.read(buff,0,1024))>=0) {
                    writer.write(buff,0,read);
                }
            } finally {
                fr.close();
            }
        }

        public void write(OutputStream os) throws IOException {
            FileInputStream fis = new FileInputStream(file);
            byte[] buff = new byte[1024];
            try {
                int read;
                while ((read = fis.read(buff,0,1024))>=0) {
                    os.write(buff,0,read);
                }
            } finally {
                fis.close();
            }
        }

    }
    private static class DirectoryContent extends Content {
        private static final Pattern A_NUMBER = Pattern.compile("\\d");
        private static final String H1 = "<html><head><title>AAF Fileserver</title></head><body><h1>AAF Fileserver</h1><h2>";
        private static final String H2 = "</h2><ul>\n";
        private static final String F = "\n</ul></body></html>";
        private File[] files;
        private String name;
        private boolean notRoot;

        public DirectoryContent(File directory, boolean isRoot) {
            notRoot = !isRoot;

            files = directory.listFiles();
            Arrays.sort(files,new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    // See if there are Numbers in the name
                    Matcher m1 = A_NUMBER.matcher(f1.getName());
                    Matcher m2 = A_NUMBER.matcher(f2.getName());
                    if (m1.find() && m2.find()) {
                        // if numbers, are the numbers in the same start position
                        int i1 = m1.start();
                        int i2 = m2.start();

                        // If same start position and the text is the same, then reverse sort
                        if (i1==i2 && f1.getName().startsWith(f2.getName().substring(0,i1))) {
                            // reverse sort files that start similarly, but have numbers in them
                            return f2.compareTo(f1);
                        }
                    }
                    return f1.compareTo(f2);
                }

            });
            name = directory.getName();
            attachmentOnly = false;
            contentType = "text/html";
        }


        @Override
        public void write(Writer w) throws IOException {
            w.append(H1);
            w.append(name);
            w.append(H2);
            for (File f : files) {
                w.append("<li><a href=\"");
                if (notRoot) {
                    w.append(name);
                    w.append('/');
                }
                w.append(f.getName());
                w.append("\">");
                w.append(f.getName());
                w.append("</a></li>\n");
            }
            w.append(F);
            w.flush();
        }

        @Override
        public void write(OutputStream os) throws IOException {
            write(new OutputStreamWriter(os));
        }

    }

    private static class CachedContent extends Content {
        private byte[] data;
        private int end;
        private char[] cdata;

        public CachedContent(File f) throws IOException {
            // Read and Cache
            ByteBuffer bb = ByteBuffer.allocate((int)f.length());
            FileInputStream fis = new FileInputStream(f);
            try {
                fis.getChannel().read(bb);
            } finally {
                fis.close();
            }

            data = bb.array();
            end = bb.position();
            cdata=null;
        }

        public String toString() {
            return Arrays.toString(data);
        }

        public void write(Writer writer) throws IOException {
            synchronized(this) {
                // do the String Transformation once, and only if actually used
                if (cdata==null) {
                    cdata = new char[end];
                    new String(data).getChars(0, end, cdata, 0);
                }
            }
            writer.write(cdata,0,end);
        }
        public void write(OutputStream os) throws IOException {
            os.write(data,0,end);
        }

    }

    public void setEnv(LogTarget env) {
        logT = env;
    }

    /**
     * Cleanup thread to remove older items if max Cache is reached.
     * @author Jonathan
     *
     */
    private static class Cleanup extends TimerTask {
        private int maxSize;
        private NavigableMap<String, Content> content;

        public Cleanup(NavigableMap<String, Content> content, int size) {
            maxSize = size;
            this.content = content;
        }

        private class Comp implements Comparable<Comp> {
            public Map.Entry<String, Content> entry;

            public Comp(Map.Entry<String, Content> en) {
                entry = en;
            }

            @Override
            public int compareTo(Comp o) {
                return (int)(entry.getValue().access-o.entry.getValue().access);
            }

        }
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            int size = content.size();
            if (size>maxSize) {
                ArrayList<Comp> scont = new ArrayList<>(size);
                Object[] entries = content.entrySet().toArray();
                for (int i=0;i<size;++i) {
                    scont.add(i, new Comp((Map.Entry<String,Content>)entries[i]));
                }
                Collections.sort(scont);
                int end = size - ((maxSize/4)*3); // reduce to 3/4 of max size
                //System.out.println("------ Cleanup Cycle ------ " + new Date().toString() + " -------");
                for (int i=0;i<end;++i) {
                    Entry<String, Content> entry = scont.get(i).entry;
                    content.remove(entry.getKey());
                    //System.out.println("removed Cache Item " + entry.getKey() + "/" + new Date(entry.getValue().access).toString());
                }
//                for (int i=end;i<size;++i) {
//                    Entry<String, Content> entry = scont.get(i).entry;
//                    //System.out.println("remaining Cache Item " + entry.getKey() + "/" + new Date(entry.getValue().access).toString());
//                }
            }
        }
    }
}
