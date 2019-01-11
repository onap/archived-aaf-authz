package org.onap.aaf.auth.layer;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import org.onap.aaf.misc.env.Data.TYPE;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FacadeImplTest {

    FacadeImpl facade;
    HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        facade = new FacadeImpl() {
        };
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void setContentType() {
        TYPE type = TYPE.JSON;
        facade.setContentType(response, type);
        verify(response).setContentType("application/json");

        type = TYPE.XML;
        facade.setContentType(response, type);
        verify(response).setContentType("text.xml");
    }

    @Test
    public void setCacheControlOff() {
        facade.setCacheControlOff(response);
        verify(response).setHeader("Cache-Control", "no-store");
        verify(response).setHeader("Pragma", "no-cache");
    }
}