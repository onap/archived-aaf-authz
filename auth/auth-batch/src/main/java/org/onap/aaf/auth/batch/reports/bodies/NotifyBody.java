/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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
package org.onap.aaf.auth.batch.reports.bodies;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.onap.aaf.auth.batch.helpers.LastNotified;
import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.misc.env.APIException;

public abstract class NotifyBody {
    private static final String DUPL = "<td style=\"text-indent: 4em;\">''</td>";
    private static final Map<String,NotifyBody> bodyMap = new HashMap<>();

    protected Map<String,List<List<String>>> rows;
    protected final String env;
    protected final String guiUrl;
    
    private final String name;
    private final String type;
    private String date;
    private int escalation;
    private int count;
    
    public NotifyBody(Access access, final String type, final String name) {
        rows = new TreeMap<>();
        this.name = name;
        this.type = type;
        date="";
        escalation = 1;
        count = 0;
        env = access.getProperty("CASS_ENV","DEVL");
        guiUrl = access.getProperty("GUI_URL", "");
    }
    
    public void store(List<String> row) {
        if(!row.isEmpty()) {
            if("info".equals(row.get(0))) {
                if(row.size()>2) {
                    date = row.get(2);
                }
                if(row.size()>3) {
                    escalation = Integer.parseInt(row.get(3));
                }
                return;
            } else if(type.equals(row.get(0))) {
                String user = user(row);
                if(user!=null) {
                    List<List<String>> lss = rows.get(user); 
                    if(lss == null) {
                        lss = new ArrayList<>();
                        rows.put(user,lss);
                    }
                    lss.add(row);
                }
            }
        }
    }

    public String name() {
        return name;
    }
    
    public String type() {
        return type;
    }
    
    public String date() {
        return date;
    }
    public int escalation() {
        return escalation;
    }
    
    public Set<String> users() {
        return rows.keySet();
    }
    
    /**
     * ID must be set from Row for Email lookup
     * 
     * @param trans
     * @param n
     * @param id
     * @param row
     * @return
     */
    public abstract boolean body(AuthzTrans trans, StringBuilder sb, int indent, Notify n, String id);
    
    /**
     * Return "null" if user not found in row... Code will handle.
     * @param row
     * @return
     */
    protected abstract String user(List<String> row);
    
    /**
     * Provide a context-sensitive Subject, which includes ENV as well as details
     * 
     * @return
     */
    public abstract String subject();

    /**
     * Record the fact that a particular Notification was marked as "sent" by Emailer.
     * 
     * @param trans
     * @param approver
     * @param ln
     */
    public abstract void record(AuthzTrans trans, StringBuilder query, String id, List<String> notified, LastNotified ln);
    
    /**
     * Get Notify Body based on key of
     * type|name
     */
    public static NotifyBody get(String key) {
        return bodyMap.get(key);
    }
    
    /**
     * Return set of loaded NotifyBodies
     * 
     */
    public static Collection<NotifyBody> getAll() {
        // Note: The same Notify Body is entered several times with different keys.
        // Therefore, need a Set of Values, not all the Values.
        Set<NotifyBody> set = new HashSet<>();
        set.addAll(bodyMap.values());
        return set;
    }
    
    /**
     * @param propAccess 
     * @throws URISyntaxException 
     * 
     */
    public static void load(Access access) throws IOException {
        // class load available NotifyBodies
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Package pkg = NotifyBody.class.getPackage();
        String path = pkg.getName().replace('.', '/');
        URL url = cl.getResource(path);
        List<String> classNames = new ArrayList<>();
        String urlString = url.toString();
        if(urlString.startsWith("jar:file:")) {
            int exclam = urlString.lastIndexOf('!');
            JarFile jf = new JarFile(urlString.substring(9,exclam));
            try {
                Enumeration<JarEntry> jfe = jf.entries();
                while(jfe.hasMoreElements()) {
                    String name = jfe.nextElement().getName();
                    if(name.startsWith(path) && name.endsWith(".class")) {
                        classNames.add(name.substring(0,name.length()-6).replace('/', '.'));
                    }
                }
            } finally {
                jf.close();
            }
        } else {
            File dir = new File(url.getFile());
            for( String f : dir.list()) {
                if(f.endsWith(".class")) {
                    classNames.add(pkg.getName()+'.'+f.substring(0,f.length()-6));
                }
            }
        }
        for(String cls : classNames) {
            try {
                Class<?> c = cl.loadClass(cls);
                if((c!=null)&&(!Modifier.isAbstract(c.getModifiers()))) {
                       Constructor<?> cst = c.getConstructor(Access.class);
                        NotifyBody nb = (NotifyBody)cst.newInstance(access);
                        if(nb!=null) {
                            bodyMap.put("info|"+nb.name, nb);
                            bodyMap.put(nb.type+'|'+nb.name, nb);
                          }
                }
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    protected void print(StringBuilder sb, int indent, Object ... objs) {
        for(int i=0;i<indent;++i) {
            sb.append(' ');
        }
        for(Object o : objs) {
            sb.append(o.toString());
        }
    }
            
    protected void println(StringBuilder sb, int indent, Object ... objs) {
        print(sb,indent,objs);
        sb.append('\n');
    }

    protected void printf(StringBuilder sb, int indent, String fmt, Object ... objs) {
        print(sb,indent,String.format(fmt, objs));
    }

    protected String printCell(StringBuilder sb, int indent, String current, String prev) {
        if(current.equals(prev)) {
            println(sb,indent,DUPL);
        } else {
            printCell(sb,indent,current);
        }
        return current; // use to set prev...
    }
    
    protected void printCell(StringBuilder sb, int indent, String current) {
        println(sb,indent,"<td>",current,"</td>");
    }
    
    public synchronized void inc() {
        ++count;
    }
    
    public int count() {
        return count;
    }
}
