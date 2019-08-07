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

package org.onap.aaf.auth.gui;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.auth.common.Define;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.rserv.HttpMethods;
import org.onap.aaf.misc.env.Slot;

public class Display {
    private final Page get;
    public Display(final AAF_GUI gui, final HttpMethods meth, final Page page) {
        get = page;
        final String[] fields = page.fields();
        final Slot slots[] = new Slot[fields.length];
        String prefix = page.name() + '.';
        for (int i=0;i<slots.length;++i) {
            slots[i] = gui.env.slot(prefix + fields[i]);
        }

        /*
         * We handle all the "Form POST" calls here with a naming convention that allows us to create arrays from strings.
         * 
         * On the HTTP side, elements concatenate their name with their Index number (if multiple).  In this code, 
         * we turn such names into arrays with same index number.  Then, we place them in the Transaction "Properties" so that 
         * it can be transferred to subclasses easily.
         */ 
        if (meth.equals(HttpMethods.POST)) {
            // Here, we'll expect FORM URL Encoded Data, which we need to get from the body
            gui.route(gui.env, meth, page.url(), 
                new HttpCode<AuthzTrans,AAF_GUI>(gui,page.name()) {
                    @Override
                    public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        trans.put(gui.slot_httpServletRequest, req);
                        for (int i=0; i<fields.length;++i) {
                            int idx = fields[i].indexOf("[]");
                            if (idx<0) { // single value
                                trans.put(slots[i], req.getParameter(fields[i])); // assume first value
                            } else { // multi value - Expect Values to be set with Field root name "field.<int>" corresponding to an array of types
                                String field=fields[i].substring(0, idx)+'.';
                                String[] array = new String[16];
                                for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements();) {
                                    String key = names.nextElement();
                                    if (key.startsWith(field)) {
                                        try {
                                            int x = Integer.parseInt(key.substring(field.length()));
                                            if (x>=array.length) {
                                                String[] temp = new String[x+10];
                                                System.arraycopy(temp, 0, temp, 0, array.length);
                                                array = temp;
                                            }
                                            array[x]=req.getParameter(key);
                                        } catch (NumberFormatException e) {
                                            trans.debug().log(e);
                                        }
                                    }
                                }
                                trans.put(slots[i], array);
                            }
                        }
                        page.replay(context,trans,resp.getOutputStream(),"general");
                    }
                }, "application/x-www-form-urlencoded","*/*");

        } else {
            // Transfer whether Page shouldn't be cached to local Final var.
            final boolean no_cache = page.no_cache;
            
            gui.route(gui.env, meth, page.url(), 
                new HttpCode<AuthzTrans,AAF_GUI>(gui,page.name()) {
                    @Override
                    public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                        trans.put(gui.slot_httpServletRequest, req);
                        for (int i=0; i<slots.length;++i) {
                            int idx = fields[i].indexOf("[]");
                            if (idx<0) { // single value
                                if(asUser(trans, req,fields[i])) {
                                    trans.put(slots[i], req.getParameter(fields[i]));
                                }
                            } else { // multi value
                                String[] array = new String[30];
                                String field=fields[i].substring(0, idx);
                                
                                for (Enumeration<String> mm = req.getParameterNames();mm.hasMoreElements();) {
                                    String key = mm.nextElement();
                                    if (key.startsWith(field)) {
                                        try {
                                            int x = Integer.parseInt(key.substring(field.length()));
                                            if (x>=array.length) {
                                                String[] temp = new String[x+10];
                                                System.arraycopy(temp, 0, temp, 0, array.length);
                                                array = temp;
                                            }
                                            array[x]=req.getParameter(key);
                                        } catch (NumberFormatException e) {
                                            trans.debug().log(e);
                                        }
                                    }
                                }
                                trans.put(slots[i], array);
                            }
                        }
                        page.replay(context,trans,resp.getOutputStream(),"general");
                    }
                    
                    /**
                     * When the field is "as_user", make sure permission is granted
                     */
                    private boolean asUser(AuthzTrans trans, HttpServletRequest req, String field) {
                        if("as_user".equals(field)) {
                            return req.isUserInRole(Define.ROOT_NS()+"|access|*|*");
                        }
                        return true;
                    }

                    @Override
                    public boolean no_cache() {
                        return no_cache;
                    }
                }, "text/html","*/*");
        }

    }
    
    public Page page() { 
        return get;
    }
}