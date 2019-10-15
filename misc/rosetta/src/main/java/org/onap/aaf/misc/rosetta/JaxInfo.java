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

package org.onap.aaf.misc.rosetta;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

public class JaxInfo {
    private static final String DEFAULT = "##default";
    public static final int DATA = 0;
    public static final int ARRAY = 1;
    public static final int OBJECT = 2;

    public final String name;
    public final Class<?> clss;
    public Map<String, JaxInfo> extensions; // Classes, which might be found at runtime, that extend this class.  Lazy Instantiation
    public final JaxInfo[] members;
    public final boolean isArray;
    public final boolean isString;
    public final boolean required;
    public final boolean nillable;
    public String ns;
    public boolean isObject() {return members!=null;}

    private JaxInfo(String n, String ns, Class<?> c, JaxInfo[] members, boolean string, boolean array, boolean required, boolean nillable) {
        name = n;
        this.ns = ns;
        clss = c;
        this.members = members;
        this.isString = string;
        isArray = array;
        this.required = required;
        this.nillable = nillable;
        extensions = null;
    }


    public int getType() {
        if (isArray)return ARRAY;
        else if (members!=null)return OBJECT;
        return DATA;
    }

    public JaxInfo getDerived(String derivedName) {
        JaxInfo derived;
        // Lazy Instantiation
        if (extensions == null) {
            extensions = new HashMap<>();
            derived = null;
        } else {
            derived = extensions.get(derivedName);
        }

        if (derived == null) {
            //TODO for the moment, Classes are in same package
            Package pkg = clss.getPackage();
            try {
                Class<?> dc = getClass().getClassLoader().loadClass(pkg.getName()+'.'+Character.toUpperCase(derivedName.charAt(0))+derivedName.substring(1));
                derived = JaxInfo.build(dc, this); // Use this JAXInfo's name so the tags are correct
                extensions.put(derivedName, derived);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return derived;
    }

    public static JaxInfo get(JaxInfo[] fields, String name) {
        for (JaxInfo f : fields) {
            if (name.equals(f.name)) return f;
        }
        return null;
    }

    /**
     * Build up JAXB Information (recursively)
     *
     * @param cls
     * @param rootNns
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     * @throws ParseException
     */
    public static JaxInfo build(Class<?> cls, JaxInfo parent) throws NoSuchFieldException, ClassNotFoundException, ParseException {
        return new JaxInfo(parent.name,parent.ns, cls,buildFields(cls,parent.ns),parent.isString, parent.isArray,parent.required,parent.nillable);
    }
    /**
     * Build up JAXB Information (recursively)
     *
     * @param cls
     * @param rootNns
     * @return
     * @throws SecurityException
     * @throws NoSuchFieldException
     * @throws ClassNotFoundException
     * @throws ParseException
     */
    public static JaxInfo build(Class<?> cls, String ... rootNns) throws SecurityException, NoSuchFieldException, ClassNotFoundException, ParseException {
        String defaultNS;
        if (rootNns.length>0 && rootNns[0]!=null) {
            defaultNS = rootNns[0];
        } else {
            Package pkg = cls.getPackage();
            XmlSchema xs = pkg.getAnnotation(XmlSchema.class);
            defaultNS = xs==null?"":xs.namespace();
        }
        String name;
        if (rootNns.length>1) {
            name = rootNns[1];
        } else {
            XmlRootElement xre = cls.getAnnotation(XmlRootElement.class);
            if (xre!=null) {
                name = xre.name();
            } else {
                XmlType xt = cls.getAnnotation(XmlType.class);
                if (xt!=null) {
                    name=xt.name();
                } else {
                    throw new ParseException("Need a JAXB Object with XmlRootElement, or stipulate in parms");
                }
            }
        }

        return new JaxInfo(name,defaultNS, cls,buildFields(cls,defaultNS),false,false,false,false);
    }

    // Build up the name and members of this particular class
    // This is recursive, if a member is a JAXB Object as well.
    private static JaxInfo[] buildFields(Class<?> clazz, String defaultNS) throws SecurityException, NoSuchFieldException, ClassNotFoundException {
        ArrayList<JaxInfo> fields = null; // allow for lazy instantiation, because many structures won't have XmlType
        Class<?> cls = clazz;
        // Build up Method names from JAXB Annotations
        XmlType xt;
        while ((xt = cls.getAnnotation(XmlType.class))!=null) {
            if (fields==null)fields = new ArrayList<>();
            for (String field : xt.propOrder()) {
                if ("".equals(field)) break; // odd bug.  "" returned when no fields exist, rather than empty array
                Field rf = cls.getDeclaredField(field);
                Class<?> ft = rf.getType();

                boolean required = false;
                boolean nillable = false;
                String xmlName = field;
                String namespace = defaultNS;

                XmlElement xe = rf.getAnnotation(XmlElement.class);
                if (xe!=null) {
                    xmlName=xe.name();
                    required = xe.required();
                    nillable = false;
                    if (DEFAULT.equals(xmlName)) {
                        xmlName = field;
                    }
                    namespace = xe.namespace();
                    if (DEFAULT.equals(namespace)) {
                        namespace = defaultNS;
                    }
                }
                // If object is a List, then it is possible multiple, per XML/JAXB evaluation
                if (ft.isAssignableFrom(List.class)) {
                    Type t = rf.getGenericType();
                    String classname = t.toString();
                    int start = classname.indexOf('<');
                    int end = classname.indexOf('>');
                    Class<?> genClass = Class.forName(classname.substring(start+1, end));
                    xe = genClass.getAnnotation(XmlElement.class);
                    if (xe!=null && !DEFAULT.equals(xe.namespace())) {
                        namespace = xe.namespace();
                    }
                    // add recursed recursed member, marked as array
                    fields.add(new JaxInfo(xmlName,namespace,genClass,buildFields(genClass,namespace), genClass.equals(String.class),true,required,nillable));
                } else {
                    boolean isString = ft.equals(String.class) || ft.equals(XMLGregorianCalendar.class);
                    // add recursed member
                    fields.add(new JaxInfo(xmlName,namespace,ft,buildFields(ft,namespace),isString,false,required,nillable));
                }
            }
            cls = cls.getSuperclass();
        };
        if (fields!=null) {
            JaxInfo[] rv = new JaxInfo[fields.size()];
            fields.toArray(rv);
            return rv;
        } else {
            return null;
        }
    }


    public StringBuilder dump(StringBuilder sb, int idx) {
        for (int i=0;i<idx;++i)sb.append(' ');
        sb.append("Field ");
        sb.append(name);
        sb.append(" [");
        sb.append(clss.getName());
        sb.append("] ");
        if (isArray)sb.append(" (array)");
        if (required)sb.append(" (required)");
        if (nillable)sb.append(" (nillable)");
        if (members!=null) {
            for (JaxInfo f : members) {
                sb.append('\n');
                f.dump(sb,idx+2);
            }
        }
        return sb;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Structure of ");
        sb.append(clss.getName());
        sb.append('\n');
        dump(sb,2);
        return sb.toString();
    }
}