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

package org.onap.aaf.misc.env.log4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JU_LogFileNamerTest {
    private File dir = new File(".");

    private String ending = new SimpleDateFormat("YYYYMMdd").format(new Date());

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() throws IOException {
        LogFileNamer logFileNamer = new LogFileNamer(dir.getCanonicalPath(), "log");
        assertEquals(logFileNamer, logFileNamer.noPID());

        logFileNamer.setAppender("Append");
        assertEquals(System.getProperty("LOG4J_FILENAME_Append"),
            dir.getCanonicalFile() + File.separator + "log-Append" + ending + "_0.log");

        logFileNamer.setAppender("Append");
        assertEquals(System.getProperty("LOG4J_FILENAME_Append"),
            dir.getCanonicalFile() + File.separator + "log-Append" + ending + "_1.log");
    }

    @Test
    public void testBlankRoot() throws IOException {
        LogFileNamer logFileNamer = new LogFileNamer(dir.getCanonicalPath(), "");
        assertEquals(logFileNamer, logFileNamer.noPID());

        logFileNamer.setAppender("Append");
        assertEquals(System.getProperty("LOG4J_FILENAME_Append"),
            dir.getCanonicalPath() + File.separator + "Append" + ending + "_0.log");

        logFileNamer.setAppender("Append");
        assertEquals(System.getProperty("LOG4J_FILENAME_Append"),
            dir.getCanonicalPath() + File.separator + "Append" + ending + "_1.log");
    }

    @After
    public void tearDown() throws IOException {
        File file = new File("./log-Append" + ending + "_0.log");
        if (file.exists()) {
            Files.delete(Paths.get(file.getAbsolutePath()));
        }
        file = new File("./log-Append" + ending + "_1.log");
        if (file.exists()) {
            Files.delete(Paths.get(file.getAbsolutePath()));
        }
        file = new File("./Append" + ending + "_0.log");
        if (file.exists()) {
            Files.delete(Paths.get(file.getAbsolutePath()));
        }
        file = new File("./Append" + ending + "_1.log");
        if (file.exists()) {
            Files.delete(Paths.get(file.getAbsolutePath()));
        }
    }

}
