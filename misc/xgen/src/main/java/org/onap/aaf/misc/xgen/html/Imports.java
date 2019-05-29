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

package org.onap.aaf.misc.xgen.html;

import java.util.ArrayList;
import java.util.List;

public class Imports implements Thematic{
    List<String> css;
    List<String> js;
    public final int backdots;
    private String theme;
    
    public Imports(int backdots) {
        css = new ArrayList<>();
        js = new ArrayList<>();
        this.backdots = backdots;
        theme = "";
    }
    
    public Imports css(String str) {
        css.add(str);
        return this;
    }
    
    public Imports js(String str) {
        js.add(str);
        return this;
    }

    public Imports theme(String str) {
        theme = str==null?"":str;
        return this;
    }

    /**
     * Pass in a possible Theme.  If it is "" or null, it will resolve to default Theme set in Imports
     * 
     * @param theTheme
     * @return
     */
    @Override
    public String themePath(String theTheme) {
        StringBuilder src = dots(new StringBuilder());
        if (theTheme==null||theTheme.length()==0) {
            src.append(theme);
            if (theme.length()>0)src.append('/');
        } else {
            src.append(theTheme);
            src.append('/');
        }

        return src.toString();
    }
    
    /**
     * Pass in a possible Theme.  If it is "" or null, it will resolve to default Theme set in Imports
     * 
     * @param theTheme
     * @return
     */
    @Override
    public String themeResolve(String theTheme) {
        return (theTheme==null||theTheme.length()==0)
            ?theme
            :theTheme;
    }

    public StringBuilder dots(StringBuilder src) {
        for (int i=0;i<backdots;++i) {
            src.append("../");
        }
        return src;
    }
    
};

