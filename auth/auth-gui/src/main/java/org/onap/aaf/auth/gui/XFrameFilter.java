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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class XFrameFilter implements Filter {
    enum TYPE {none,self};
    // Note: Content-Security Params need to be worked out for GUI before activating.
    private final String xframe;//,csp;

    public XFrameFilter(TYPE type) {
        switch(type) {
        case self:
            xframe="SAMEORIGIN";
//            csp="default-src 'self'";
            break;
        case none:
        default:
            xframe="DENY";
//            csp="default-src 'none'";
            break;
    
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain fc) throws IOException, ServletException {
        if (resp instanceof HttpServletResponse) {
            @SuppressWarnings("unused")
            HttpServletResponse hresp = (HttpServletResponse)resp;
            ((HttpServletResponse)resp).addHeader("X-Frame-Options", xframe);
//            ((HttpServletResponse)resp).addHeader("Content-Security-Policy",csp);
        }
        fc.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void destroy() {
    }


}
