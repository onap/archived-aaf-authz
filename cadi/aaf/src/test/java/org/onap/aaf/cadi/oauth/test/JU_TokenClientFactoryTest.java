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

package org.onap.aaf.cadi.oauth.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import org.junit.Test;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.aaf.v2_0.AAFLocator;
import org.onap.aaf.cadi.config.Config;
import org.onap.aaf.cadi.locator.PropertyLocator;
import org.onap.aaf.cadi.oauth.TokenClientFactory;
import org.onap.aaf.misc.env.APIException;

import junit.framework.Assert;

public class JU_TokenClientFactoryTest  {

    /**
     * Acceptable Locator Patterns for choosing AAFLocator over others
     */
    @Test
    public void testLocatorString() {
        /*
        PropAccess access = new PropAccess();
        access.setProperty(Config.AAF_LOCATE_URL, "https://xytz.sbbc.dd:8095/locate");
        access.setProperty(Config.CADI_LATITUDE, "39.000");
        access.setProperty(Config.CADI_LONGITUDE, "-72.000");
        TokenClientFactory tcf;
        try {
            System.out.println("one");
            tcf = TokenClientFactory.instance(access);
            System.out.println("two");
            Assert.assertEquals(true, tcf.bestLocator("https://xytz.sbbc.dd/locate/hello") instanceof AAFLocator);
            System.out.println("three");
            Assert.assertEquals(true, tcf.bestLocator("https://xytz.sbbc.dd:8234/locate/hello") instanceof AAFLocator);
            System.out.println("four");
            Assert.assertEquals(true, tcf.bestLocator("https://AAF_LOCATE_URL/hello") instanceof AAFLocator);
            System.out.println("five");
            Assert.assertEquals(true, tcf.bestLocator("https://AAF_LOCATE_URL/AAF_FS.hello/2.0") instanceof AAFLocator);
            System.out.println("six");
            Assert.assertEquals(true, tcf.bestLocator("https://xytz.sbbc.dd:8234/locate") instanceof PropertyLocator);
            System.out.println("seven");
            Assert.assertEquals(true, tcf.bestLocator("https://xytz.sbbc.dd:8234/Something") instanceof PropertyLocator);
        } catch (APIException | GeneralSecurityException | IOException | CadiException | LocatorException | URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
        */
    }

}
