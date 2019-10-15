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

package org.onap.aaf.auth.oauth;

import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.aaf.cadi.principal.BearerPrincipal;

public class JU_OAuth2FilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterChain chain;
    @Mock
    private BearerPrincipal principal;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void testDoFilterWithContentType() throws IOException, ServletException {
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        OAuth2Filter filter = new OAuth2Filter();
        filter.doFilter(request, null, chain);

        verify(chain, only()).doFilter(request, null);
    }

    @Test
    public void testDoFilter() throws IOException, ServletException {
        when(request.getContentType()).thenReturn("somethingElse");
        when(request.getUserPrincipal()).thenReturn(principal);
        when(request.getHeader("Authorization")).thenReturn("Bearer 1;Bearer2");

        OAuth2Filter filter = new OAuth2Filter();
        filter.init(null);
        filter.destroy();
        filter.doFilter(request, null, chain);

        verify(chain, only()).doFilter(request, null);
        verify(principal, only()).setBearer("1");
    }

    @Test
    public void testDoFilterWithoutBearerPrincipal() throws IOException, ServletException {
        when(request.getContentType()).thenReturn("somethingElse");
        when(request.getHeader("Authorization")).thenReturn("Bearer 1;Bearer2");

        OAuth2Filter filter = new OAuth2Filter();
        filter.doFilter(request, null, chain);

        verify(chain, only()).doFilter(request, null);
    }
}
