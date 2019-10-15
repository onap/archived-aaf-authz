/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.rserv.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.rserv.TypedCode;
import org.onap.aaf.misc.env.TransJAXB;
import org.onap.aaf.misc.env.impl.EnvFactory;


/**
 * Test the functioning of the "Content" class, which holds, and routes to the right code based on Accept values
 */
public class JU_Content {


    @Test
    public void test() throws Exception {
        final String BOOL = "Boolean";
        final String XML = "XML";
        TransJAXB trans = EnvFactory.newTrans();
        try {
        HttpCode<TransJAXB, String> cBool = new HttpCode<TransJAXB,String>(BOOL,"Standard String") {
            @Override
            public void handle(TransJAXB trans, HttpServletRequest req, HttpServletResponse resp) {
                try {
                    resp.getOutputStream().write(context.getBytes());
                } catch (IOException e) {
                }
            }
        };

        HttpCode<TransJAXB,String> cXML = new HttpCode<TransJAXB,String>(XML, "Standard String") {
            @Override
            public void handle(TransJAXB trans, HttpServletRequest req, HttpServletResponse resp) {
                try {
                    resp.getOutputStream().write(context.getBytes());
                } catch (IOException e) {
                }
            }
        };

        TypedCode<TransJAXB> ct = new TypedCode<TransJAXB>()
                .add(cBool,"application/" + Boolean.class.getName()+"+xml;charset=utf8;version=1.1")
                .add(cXML,"application/xml;q=.9");
        String expected = "application/java.lang.Boolean+xml;charset=utf8;version=1.1,application/xml;q=0.9";
        assertEquals(expected,ct.toString());

        //BogusReq req = new BogusReq();
        //expected = (expected);
        //HttpServletResponse resp = new BogusResp();
    
        assertNotNull("Same Content String and Accept String",ct.prep(trans,expected));

        //expects Null (not run)
        // A Boolean xml that must have charset utf8 and match version 1.2 or greater
        expected = ("application/java.lang.Boolean+xml;charset=utf8;version=1.2");
        assertNull("Accept Minor Version greater than Content Minor Version",ct.prep(trans,expected));

        // Same with (too many) spaces
        expected = (" application/java.lang.Boolean+xml ; charset = utf8 ; version = 1.2   ");
        assertNull("Accept Minor Version greater than Content Minor Version",ct.prep(trans,expected));

        //expects Null (not run)
        expected = ("application/java.lang.Boolean+xml;charset=utf8;version=2.1");
        assertNull("Major Versions not the same",ct.prep(trans,expected));

        expected = ("application/java.lang.Boolean+xml;charset=utf8;version=1.0");
        assertNotNull("Content Minor Version is greater than Accept Minor Version",ct.prep(trans,expected));

        expected = "application/java.lang.Squid+xml;charset=utf8;version=1.0,application/xml;q=.9";
        assertNotNull("2nd one will have to do...",ct.prep(trans,expected));

        expected = "application/java.lang.Boolean+xml;charset=UTF8;version=1.0";
        assertNotNull("Minor Charset in Caps acceptable",ct.prep(trans,expected));

        // expects no run 
        expected="application/java.lang.Boolean+xml;charset=MyType;version=1.0";
        assertNull("Unknown Minor Charset",ct.prep(trans,expected));

        expected="";
        assertNotNull("Blank Acceptance",ct.prep(trans,expected));
    
        expected=null;
        assertNotNull("Null Acceptance",ct.prep(trans,expected));

        expected = ("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        assertNotNull("Matches application/xml, and other content not known",ct.prep(trans,expected));
    
        // No SemiColon
        expected = ("i/am/bogus,application/xml");
        assertNotNull("Match second entry, with no Semis",ct.prep(trans,expected));

         } finally {
            StringBuilder sb = new StringBuilder();
            trans.auditTrail(0, sb);
            //System.out.println(sb);
        }
    }
//
//    Original API used HTTPServletRequest and HTTPServletResponse.  Due to the fact that sometimes we use Accept, and others Content-TYpe
//    I changed it to simply accept a string
//
//    Jonathan 3/8/2013
//
//    @SuppressWarnings("rawtypes")
//    class BogusReq implements HttpServletRequest {
//        private String accept;
//
//        public void accept(String accept) {
//            this.accept = accept;
//        }
//
//        @Override
//        public Object getAttribute(String name) {
//            return accept;
//        }
//
//
//        @Override
//        public Enumeration getAttributeNames() {
//            return null;
//        }
//
//        @Override
//        public String getCharacterEncoding() {
//            return null;
//        }
//
//        @Override
//        public void setCharacterEncoding(String env)
//                throws UnsupportedEncodingException {
//        
//
//        }
//
//        @Override
//        public int getContentLength() {
//        
//            return 0;
//        }
//
//        @Override
//        public String getContentType() {
//        
//            return null;
//        }
//
//        @Override
//        public ServletInputStream getInputStream() throws IOException {
//        
//            return null;
//        }
//
//        @Override
//        public String getParameter(String name) {
//        
//            return null;
//        }
//
//        @Override
//        public Enumeration getParameterNames() {
//        
//            return null;
//        }
//
//        @Override
//        public String[] getParameterValues(String name) {
//        
//            return null;
//        }
//
//        @Override
//        public Map getParameterMap() {
//        
//            return null;
//        }
//
//        @Override
//        public String getProtocol() {
//        
//            return null;
//        }
//
//        @Override
//        public String getScheme() {
//        
//            return null;
//        }
//
//        @Override
//        public String getServerName() {
//        
//            return null;
//        }
//
//        @Override
//        public int getServerPort() {
//        
//            return 0;
//        }
//
//        @Override
//        public BufferedReader getReader() throws IOException {
//        
//            return null;
//        }
//
//        @Override
//        public String getRemoteAddr() {
//        
//            return null;
//        }
//
//        @Override
//        public String getRemoteHost() {
//        
//            return null;
//        }
//
//        @Override
//        public void setAttribute(String name, Object o) {
//        
//
//        }
//
//        @Override
//        public void removeAttribute(String name) {
//        
//
//        }
//
//        @Override
//        public Locale getLocale() {
//        
//            return null;
//        }
//
//        @Override
//        public Enumeration getLocales() {
//        
//            return null;
//        }
//
//        @Override
//        public boolean isSecure() {
//        
//            return false;
//        }
//
//        @Override
//        public RequestDispatcher getRequestDispatcher(String path) {
//        
//            return null;
//        }
//
//        @Override
//        public String getRealPath(String path) {
//        
//            return null;
//        }
//
//        @Override
//        public int getRemotePort() {
//        
//            return 0;
//        }
//
//        @Override
//        public String getLocalName() {
//        
//            return null;
//        }
//
//        @Override
//        public String getLocalAddr() {
//        
//            return null;
//        }
//
//        @Override
//        public int getLocalPort() {
//        
//            return 0;
//        }
//
//        @Override
//        public String getAuthType() {
//        
//            return null;
//        }
//
//        @Override
//        public Cookie[] getCookies() {
//        
//            return null;
//        }
//
//        @Override
//        public long getDateHeader(String name) {
//        
//            return 0;
//        }
//
//        @Override
//        public String getHeader(String name) {
//            return accept;
//        }
//
//        @Override
//        public Enumeration getHeaders(String name) {
//        
//            return null;
//        }
//
//        @Override
//        public Enumeration getHeaderNames() {
//        
//            return null;
//        }
//
//        @Override
//        public int getIntHeader(String name) {
//        
//            return 0;
//        }
//
//        @Override
//        public String getMethod() {
//        
//            return null;
//        }
//
//        @Override
//        public String getPathInfo() {
//        
//            return null;
//        }
//
//        @Override
//        public String getPathTranslated() {
//        
//            return null;
//        }
//
//        @Override
//        public String getContextPath() {
//        
//            return null;
//        }
//
//        @Override
//        public String getQueryString() {
//        
//            return null;
//        }
//
//        @Override
//        public String getRemoteUser() {
//        
//            return null;
//        }
//
//        @Override
//        public boolean isUserInRole(String role) {
//        
//            return false;
//        }
//
//        @Override
//        public Principal getUserPrincipal() {
//        
//            return null;
//        }
//
//        @Override
//        public String getRequestedSessionId() {
//        
//            return null;
//        }
//
//        @Override
//        public String getRequestURI() {
//        
//            return null;
//        }
//
//        @Override
//        public StringBuffer getRequestURL() {
//        
//            return null;
//        }
//
//        @Override
//        public String getServletPath() {
//        
//            return null;
//        }
//
//        @Override
//        public HttpSession getSession(boolean create) {
//        
//            return null;
//        }
//
//        @Override
//        public HttpSession getSession() {
//        
//            return null;
//        }
//
//        @Override
//        public boolean isRequestedSessionIdValid() {
//        
//            return false;
//        }
//
//        @Override
//        public boolean isRequestedSessionIdFromCookie() {
//        
//            return false;
//        }
//
//        @Override
//        public boolean isRequestedSessionIdFromURL() {
//        
//            return false;
//        }
//
//        @Override
//        public boolean isRequestedSessionIdFromUrl() {
//        
//            return false;
//        }
//    }
//
//    public class BogusResp implements HttpServletResponse {
//        public String contentType;
//
//        @Override
//        public String getCharacterEncoding() {
//    
//            return null;
//        }
//
//        @Override
//        public String getContentType() {
//            return contentType;
//        }
//
//        @Override
//        public ServletOutputStream getOutputStream() throws IOException {
//    
//            return null;
//        }
//
//        @Override
//        public PrintWriter getWriter() throws IOException {
//    
//            return null;
//        }
//
//        @Override
//        public void setCharacterEncoding(String charset) {
//    
//        
//        }
//
//        @Override
//        public void setContentLength(int len) {
//    
//        
//        }
//
//        @Override
//        public void setContentType(String type) {
//            contentType = type;
//        }
//
//        @Override
//        public void setBufferSize(int size) {
//    
//        
//        }
//
//        @Override
//        public int getBufferSize() {
//    
//            return 0;
//        }
//
//        @Override
//        public void flushBuffer() throws IOException {
//    
//        
//        }
//
//        @Override
//        public void resetBuffer() {
//    
//        
//        }
//
//        @Override
//        public boolean isCommitted() {
//    
//            return false;
//        }
//
//        @Override
//        public void reset() {
//    
//        
//        }
//
//        @Override
//        public void setLocale(Locale loc) {
//    
//        
//        }
//
//        @Override
//        public Locale getLocale() {
//    
//            return null;
//        }
//
//        @Override
//        public void addCookie(Cookie cookie) {
//    
//        
//        }
//
//        @Override
//        public boolean containsHeader(String name) {
//    
//            return false;
//        }
//
//        @Override
//        public String encodeURL(String url) {
//    
//            return null;
//        }
//
//        @Override
//        public String encodeRedirectURL(String url) {
//    
//            return null;
//        }
//
//        @Override
//        public String encodeUrl(String url) {
//    
//            return null;
//        }
//
//        @Override
//        public String encodeRedirectUrl(String url) {
//    
//            return null;
//        }
//
//        @Override
//        public void sendError(int sc, String msg) throws IOException {
//    
//        
//        }
//
//        @Override
//        public void sendError(int sc) throws IOException {
//    
//        
//        }
//
//        @Override
//        public void sendRedirect(String location) throws IOException {
//    
//        
//        }
//
//        @Override
//        public void setDateHeader(String name, long date) {
//    
//        
//        }
//
//        @Override
//        public void addDateHeader(String name, long date) {
//    
//        
//        }
//
//        @Override
//        public void setHeader(String name, String value) {
//    
//        
//        }
//
//        @Override
//        public void addHeader(String name, String value) {
//    
//        
//        }
//
//        @Override
//        public void setIntHeader(String name, int value) {
//    
//        
//        }
//
//        @Override
//        public void addIntHeader(String name, int value) {
//    
//        
//        }
//
//        @Override
//        public void setStatus(int sc) {
//    
//        
//        }
//
//        @Override
//        public void setStatus(int sc, String sm) {
//    
//        
//        }
//    
//    }
//
}
