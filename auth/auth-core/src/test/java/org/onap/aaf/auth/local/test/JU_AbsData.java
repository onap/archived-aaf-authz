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

package org.onap.aaf.auth.local.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.local.AbsData;
import org.onap.aaf.auth.local.DataFile;
import org.onap.aaf.auth.local.TextIndex;
import org.onap.aaf.auth.local.AbsData.Iter;
import org.onap.aaf.auth.local.AbsData.Reuse;

import junit.framework.Assert;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;

public class JU_AbsData {
    char character = 'x';
    String filePath = "test/output_.key";
    File keyfile = new File(filePath);
    AuthzTrans trans = mock(AuthzTrans.class);

    private class AbsDataStub extends AbsData {


        public AbsDataStub(File dataf, char sepChar, int maxLineSize, int fieldOffset) {
            super(dataf, sepChar, maxLineSize, fieldOffset);
            // TODO Auto-generated constructor stub

        }

    }

    @Test
    public void testStub() throws IOException {
        char character = 'x';
        String filePath = "test/output_.key";
        File keyfile = new File(filePath);
        FileOutputStream is = new FileOutputStream(keyfile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        BufferedWriter  w = new BufferedWriter(osw);
        for (int i = 0; i< 10; i++) {        //Write lines to file
            w.write("a\nsdfasdfxasdf" + i + "\n");
        }
        w.close();
        AbsDataStub ads = new AbsDataStub(keyfile, character, 0, 0);
        ads.skipLines(0);
        ads.name();

        long lng = 1823286886660L;
        //ads.open(trans, lng);
        keyfile.delete();
    }

    @Test
    public void testClose() throws IOException {
        AbsDataStub ads = new AbsDataStub(keyfile, character, 0, 0);
        ads.close(trans);
    }

    @Test
    public void testReuse() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        char character = 'x';
        AbsDataStub ads = new AbsDataStub(keyfile, character, 0, 0);
        Reuse reuse = ads.reuse();
        reuse.reset();
        Assert.assertEquals("", reuse.at(1));
        Assert.assertNull(reuse.next());
        //reuse.atToEnd(0);
        //reuse.pos(10);
        keyfile.delete();
    }

    @Test
    public void testIter() throws IOException {
        AbsDataStub ads = new AbsDataStub(keyfile, character, 0, 0);
        TextIndex textIndex = new TextIndex(keyfile);
        //Iter iter = ads.iterator();        //Need actual input to run textIndex.create to have a datafile to read

    }

}
