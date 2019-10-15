/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * *
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/

package org.onap.aaf.auth.local.test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransFilter;
import org.onap.aaf.auth.local.AbsData.Reuse;
import org.onap.aaf.auth.local.AbsData;
import org.onap.aaf.auth.local.DataFile;
import org.onap.aaf.auth.local.TextIndex;
import org.onap.aaf.auth.local.TextIndex.Iter;
import org.onap.aaf.auth.local.test.JU_AbsData;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;

@RunWith(MockitoJUnitRunner.class)
public class JU_TextIndex {
    TextIndex textIndex;
    Iter iter;
    Trans trans;
    DataFile datafile;
    @Mock
    File file;

    private class AbsDataStub extends AbsData {

    
        public AbsDataStub(File dataf, char sepChar, int maxLineSize, int fieldOffset) {
            super(dataf, sepChar, maxLineSize, fieldOffset);
            // TODO Auto-generated constructor stub
        
        }
    
    }

    @Before
    public void setUp() throws IOException{
        char character = 'x';
        String filePath = "test/output_key";
        File keyfile = new File(filePath);
        FileOutputStream is = new FileOutputStream(keyfile);
        OutputStreamWriter osw = new OutputStreamWriter(is);
        BufferedWriter  w = new BufferedWriter(osw);
        for (int i = 0; i< 10; i++) {        //Write lines to file
            w.write("a\nsdfasdfxasdf" + i + "\n");
        }
        w.close();
    
        datafile = new DataFile(keyfile, "r");
        datafile.open();
        datafile = new DataFile(keyfile, "rws");// "S" for synchronized
        datafile.open();
    
        trans = mock(Trans.class);
        TimeTaken ttMock = mock(TimeTaken.class);
        TimeTaken ttMock1 = mock(TimeTaken.class);
        when(trans.start("Open Files", Env.SUB)).thenReturn(ttMock);
        when(trans.start("Read", Env.SUB)).thenReturn(ttMock);
        textIndex = new TextIndex(keyfile);
        textIndex.close();
        textIndex.open();
        //textIndex.create(trans, datafile, 4, character, 2, 0);    //TODO: AAF-111 once actual input is aquired
        keyfile.delete();
    
        iter = textIndex.new Iter();
    }

    @Test
    public void testClose() throws IOException {
        textIndex.close();
    }

    @Test
    public void testFind() throws IOException {
        char character = 'x';
        String filePath = "test/output_.key";
        File keyfile = new File(filePath);
        AbsDataStub ads = new AbsDataStub(keyfile, character, 0, 0);
        Reuse reuse = ads.reuse();
        textIndex.find("a", reuse , 0);
    }

    @Test
    public void testIterNext() {
        iter.next();
        iter.hasNext();
    }

    @Test
    public void testIdx() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        TextIndex outerObject = new TextIndex(file);
        Class<?> idxClass = TextIndex.class.getDeclaredClasses()[0];
        Constructor<?> idxConstructor = idxClass.getDeclaredConstructors()[0];
        Class[] cArg = new Class[2];
        cArg[0] = Object.class;
        cArg[1] = Integer.class;
        idxConstructor.setAccessible(true);
        //Object innerObject = idxConstructor.newInstance(outerObject,cArg);
        //idxConstructor.hashCode();                                            //TODO: AAF-111 access inner private class
    }

}
