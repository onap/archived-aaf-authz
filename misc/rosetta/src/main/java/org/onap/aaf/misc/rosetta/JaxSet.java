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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlType;

/**
 * For specific XML class, quickly find a Setter Method which will load the object
 *
 * Object type of Setter must match String at this time.
 *
 * @author Jonathan
 *
 * @param <T>
 */
public class JaxSet<T> {
    private static Map<Class<?>,JaxSet<?>> jsets = new HashMap<>();
    private Map<String,Setter<T>> members;

    private JaxSet(Class<?> cls) {
        members = new TreeMap<>();
        XmlType xmltype = cls.getAnnotation(XmlType.class);
        Class<?> paramType[] = new Class[] {String.class};
        for (String str : xmltype.propOrder()) {
            try {
                String setName = "set" + Character.toUpperCase(str.charAt(0)) + str.subSequence(1, str.length());
                Method meth = cls.getMethod(setName,paramType );
                if (meth!=null) {
                    members.put(str, new Setter<T>(meth) {
                        public void set(T o, Object t) throws ParseException {
                            try {
                                this.meth.invoke(o, t);
                            } catch (Exception e) {
                                throw new ParseException(e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                // oops
            }
        }
    }

    public static abstract class Setter<O> {
        protected final Method meth;
        public Setter(Method meth) {
            this.meth = meth;
        }
        public abstract void set(O o, Object obj) throws ParseException;
    }

    public static <X> JaxSet<X> get(Class<?> cls) {
        synchronized(jsets) {
            @SuppressWarnings("unchecked")
            JaxSet<X> js = (JaxSet<X>)jsets.get(cls);
            if (js == null) {
                jsets.put(cls, js = new JaxSet<>(cls));
            }
            return js;
        }
    }

    public Setter<T> get(String key) {
        return members.get(key);
    }
}
