/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.cssa.rserv;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.inno.env.Env;
import com.att.inno.env.EnvJAXB;
import com.att.inno.env.LogTarget;
import com.att.inno.env.Store;
import com.att.inno.env.TimeTaken;
import com.att.inno.env.Trans;
/*
 * CachingFileAccess
 * 
 *  
 */
public class CachingFileAccess<TRANS extends Trans> extends HttpCode<TRANS, Void> {
	public static void setEnv(Store store, String[] args) {
		for(int i=0;i<args.length-1;i+=2) { // cover two parms required for each 
			if(CFA_WEB_DIR.equals(args[i])) {
				store.put(store.staticSlot(CFA_WEB_DIR), args[i+1]); 
			} else if(CFA_CACHE_CHECK_INTERVAL.equals(args[i])) {
				store.put(store.staticSlot(CFA_CACHE_CHECK_INTERVAL), Long.parseLong(args[i+1]));
			} else if(CFA_MAX_SIZE.equals(args[i])) {
				store.put(store.staticSlot(CFA_MAX_SIZE), Integer.parseInt(args[i+1]));
			}
		}
	}
	
	private static String MAX_AGE = "max-age=3600"; // 1 hour Caching
	private final Map<String,String> typeMap;
	private final NavigableMap<String,Content> content;
	private final Set<String> attachOnly;
	private final static String WEB_DIR_DEFAULT = "theme";
	public final static String CFA_WEB_DIR = "CFA_WebPath";
	// when to re-validate from file
	// Re validating means comparing the Timestamp on the disk, and seeing it has changed.  Cache is not marked
	// dirty unless file has changed, but it still makes File IO, which for some kinds of cached data, i.e. 
	// deployed GUI elements is unnecessary, and wastes time.
	// This parameter exists to cover the cases where data can be more volatile, so the user can choose how often the
	// File IO will be accessed, based on probability of change.  "0", of course, means, check every time.
	private final static String CFA_CACHE_CHECK_INTERVAL = "CFA_CheckIntervalMS";
	private final static String CFA_MAX_SIZE = "CFA_MaxSize"; // Cache size limit
	private final static String CFA_CLEAR_COMMAND = "CFA_ClearCommand";

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
	
	public CachingFileAccess(EnvJAXB env, String ... args) {
		super(null,"Caching File Access");
		setEnv(env,args);
		content = new ConcurrentSkipListMap<String,Content>(); // multi-thread changes possible

		attachOnly = new HashSet<String>();     // short, unchanged

		typeMap = new TreeMap<String,String>(); // Structure unchanged after Construction
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
		
		timer = new Timer("Caching Cleanup",true);
		timer.schedule(new Cleanup(content,500),60000,60000);
		
		// Property params
		web_path = env.getProperty(CFA_WEB_DIR,WEB_DIR_DEFAULT);
		Object obj;
		obj = env.get(env.staticSlot(CFA_CACHE_CHECK_INTERVAL),600000L);  // Default is 10 mins
		if(obj instanceof Long) {checkInterval=(Long)obj;
		} else {checkInterval=Long.parseLong((String)obj);}
		
		obj = env.get(env.staticSlot(CFA_MAX_SIZE), 512000);    // Default is max file 500k
		if(obj instanceof Integer) {maxItemSize=(Integer)obj;
		} else {maxItemSize =Integer.parseInt((String)obj);}
	 	 	
	 	clear_command = env.getProperty(CFA_CLEAR_COMMAND,null);
	}

	

	@Override
	public void handle(TRANS trans, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String key = pathParam(req, ":key");
		if(key.equals(clear_command)) {
			String cmd = pathParam(req,":cmd");
			resp.setHeader("Content-type",typeMap.get("txt"));
			if("clear".equals(cmd)) {
				content.clear();
				resp.setStatus(HttpStatus.OK_200);
			} else {
				resp.setStatus(HttpStatus.BAD_REQUEST_400);
			}
			return;
		}
		Content c = load(logT , web_path,key, null, checkInterval);
		if(c.attachmentOnly) {
			resp.setHeader("Content-disposition", "attachment");
		}
		c.write(resp.getOutputStream());
		c.setHeader(resp);
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
		if(timeCheck<0) {
			timeCheck=checkInterval; // if time < 0, then use default
		}
		String fileName = dataRoot + '/' + key;
		Content c = content.get(key);
		long systime = System.currentTimeMillis(); 
		File f=null;
		if(c!=null) {
			// Don't check every hit... only after certain time value
			if(c.date < systime + timeCheck) {
				f = new File(fileName);
				if(f.lastModified()>c.date) {
					c=null;
				}
			}
		}
		if(c==null) {	
			if(logTarget!=null) {
				logTarget.log("File Read: ",key);
			}
			
			if(f==null){
				f = new File(fileName);
			}

			boolean cacheMe;
			if(f.exists()) {
				if(f.length() > maxItemSize) {
					c = new DirectFileContent(f);
					cacheMe = false;
				} else {
					c = new CachedContent(f);
					cacheMe = checkInterval>0;
				}
				
				if(mediaType==null) { // determine from file Ending
					int idx = key.lastIndexOf('.');
					String subkey = key.substring(++idx);
					if((c.contentType = idx<0?null:typeMap.get(subkey))==null) {
						// if nothing else, just set to default type...
						c.contentType = "application/octet-stream";
					}
					c.attachmentOnly = attachOnly.contains(subkey);
				} else {
					c.contentType=mediaType;
					c.attachmentOnly = false;
				}
				
				c.date = f.lastModified();
				
				if(cacheMe) {
					content.put(key, c);
				}
			} else {
				c=NULL;
			}
		} else {
			if(logTarget!=null)logTarget.log("Cache Read: ",key);
		}

		// refresh hit time
		c.access = systime;
		return c;
	}
	
	public Content loadOrDefault(Trans trans, String targetDir, String targetFileName, String sourcePath, String mediaType) throws IOException {
		try {
			return load(trans.info(),targetDir,targetFileName,mediaType,0);
		} catch(FileNotFoundException e) {
			String targetPath = targetDir + '/' + targetFileName;
			TimeTaken tt = trans.start("File doesn't exist; copy " + sourcePath + " to " + targetPath, Env.SUB);
			try {
				FileInputStream sourceFIS = new FileInputStream(sourcePath);
				FileChannel sourceFC = sourceFIS.getChannel();
				File targetFile = new File(targetPath);
				targetFile.getParentFile().mkdirs(); // ensure directory exists
				FileOutputStream targetFOS = new FileOutputStream(targetFile);
				try {
					ByteBuffer bb = ByteBuffer.allocate((int)sourceFC.size());
					sourceFC.read(bb);
					bb.flip();  // ready for reading
					targetFOS.getChannel().write(bb);
				} finally {
					sourceFIS.close();
					targetFOS.close();
				}
			} finally {
				tt.done();
			}
			return load(trans.info(),targetDir,targetFileName,mediaType,0);
		}
	}

	public void invalidate(String key) {
		content.remove(key);
	}
	
	private static final Content NULL=new Content() {
		
		@Override
		public void setHeader(HttpServletResponse resp) {
			resp.setStatus(HttpStatus.NOT_FOUND_404);
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
			resp.setStatus(HttpStatus.OK_200);
			resp.setHeader("Content-type",contentType);
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
				while((read = fr.read(buff,0,1024))>=0) {
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
				while((read = fis.read(buff,0,1024))>=0) {
					os.write(buff,0,read);
				}
			} finally {
				fis.close();
			}
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
			return data.toString();
		}
		
		public void write(Writer writer) throws IOException {
			synchronized(this) {
				// do the String Transformation once, and only if actually used
				if(cdata==null) {
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
			if(size>maxSize) {
				ArrayList<Comp> scont = new ArrayList<Comp>(size);
				Object[] entries = content.entrySet().toArray();
				for(int i=0;i<size;++i) {
					scont.add(i, new Comp((Map.Entry<String,Content>)entries[i]));
				}
				Collections.sort(scont);
				int end = size - ((maxSize/4)*3); // reduce to 3/4 of max size
				System.out.println("------ Cleanup Cycle ------ " + new Date().toString() + " -------");
				for(int i=0;i<end;++i) {
					Entry<String, Content> entry = scont.get(i).entry;
					content.remove(entry.getKey());
					System.out.println("removed Cache Item " + entry.getKey() + "/" + new Date(entry.getValue().access).toString());
				}
				for(int i=end;i<size;++i) {
					Entry<String, Content> entry = scont.get(i).entry;
					System.out.println("remaining Cache Item " + entry.getKey() + "/" + new Date(entry.getValue().access).toString());
				}
			}
		}
	}
}
