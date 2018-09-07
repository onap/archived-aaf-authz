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

package org.onap.aaf.auth.rserv.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.aaf.auth.rserv.CachingFileAccess;
import org.onap.aaf.auth.rserv.HttpCode;
import org.onap.aaf.auth.rserv.Match;
import org.onap.aaf.misc.env.EnvJAXB;
import org.onap.aaf.misc.env.LogTarget;
import org.onap.aaf.misc.env.Store;
import org.onap.aaf.misc.env.Trans;
import org.powermock.modules.junit4.PowerMockRunner;

import junit.framework.Assert;


@RunWith(PowerMockRunner.class)
public class JU_CachingFileAccess {
    CachingFileAccess cachingFileAccess;
    HttpCode httpCode;
    EnvJAXB envJ;
    Trans trans;


    @Before
    public void setUp() throws IOException{
        trans = mock(Trans.class);
        HttpCode hCode = mock(HttpCode.class);
        envJ = mock(EnvJAXB.class);
        LogTarget log = mock(LogTarget.class);
        Long lng = (long) 1234134;
        when(envJ.get(envJ.staticSlot("aaf_cfa_cache_check_interval"),600000L)).thenReturn(lng);
        when(envJ.get(envJ.staticSlot("aaf_cfa_max_size"), 512000)).thenReturn(512000);
        when(envJ.get(envJ.staticSlot("aaf_cfa_web_path"))).thenReturn("TEST");
        when(envJ.getProperty("aaf_cfa_clear_command",null)).thenReturn("null");
        when(envJ.init()).thenReturn(log);
        doNothing().when(log).log((String)any());
        cachingFileAccess = new CachingFileAccess(envJ,"test");



    }

    @Test
    public void testSetEnv() {
        Store store = mock(Store.class);
        Store store1 = mock(Store.class);
        Store store2 = mock(Store.class);
        String test[] = {"aaf_cfa_web_path","aaf_cfa_cache_check_interval","aaf_cfa_max_size"};
        String test1[] = {"aaf_cfa_cache_check_interval"};
        String test2[] = {"aaf_cfa_max_size"};
        cachingFileAccess.setEnv(store, test);
        cachingFileAccess.setEnv(store1, test1); //These don't reach all the branches for some reason
        cachingFileAccess.setEnv(store2, test2);
    }

    @Test
    public void testHandle() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Trans trans = mock(Trans.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getPathInfo()).thenReturn("path/to/file");

        Field matchField = HttpCode.class.getDeclaredField("match");
        matchField.setAccessible(true);
        Match match = mock(Match.class);
        when(match.param(anyString(), anyString())).thenReturn("null/");
        matchField.set(cachingFileAccess, match);
        cachingFileAccess.handle(trans, req, resp);
        when(match.param(anyString(), anyString())).thenReturn("clear");
        cachingFileAccess.handle(trans, req, resp);
    }

    @Test
    public void testWebPath() {
        EnvJAXB envJ = mock(EnvJAXB.class);
        String web_path_test = "TEST";
        Assert.assertEquals(web_path_test, cachingFileAccess.webPath());
    }

    @Test
    public void testCleanupParams() {
        NavigableMap<String,org.onap.aaf.auth.rserv.Content> content = new ConcurrentSkipListMap<>();
        cachingFileAccess.cleanupParams(50, 500); //TODO: find right input
    }

    @Test
    public void testLoad() throws IOException {
        cachingFileAccess.load(null, null, "1220227200L/1220227200L", null, 1320227200L );
        String filePath = "test/output_key";
        File keyfile = new File(filePath);
        RandomAccessFile randFile = new RandomAccessFile (keyfile,"rw");

        String dPath = "test/";
        File directoryPath = new File(dPath);
        directoryPath.mkdir();
        cachingFileAccess.load(null, dPath, "-", null, -1);
        randFile.setLength(1024 * 1024 * 8);
        cachingFileAccess.load(null, filePath, "-", null, -1);
        keyfile.delete();
        directoryPath.delete();
        String filePath1 = "test/output_key";
        File keyfile1 = new File(filePath1);
        keyfile1.createNewFile();
        cachingFileAccess.load(null, filePath1, "-", "test", -1);
        keyfile1.delete();
    }

    @Test
    public void testLoadOrDefault() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        String filePath = "test/output_key";
        File keyfile = new File(filePath);
        cachingFileAccess.loadOrDefault(trans, filePath, "-", null, null);
        keyfile.delete();

        Trans trans = mock(Trans.class);

        String filePath1 = "test/output_key.txt";
        //File keyfile1 = new File(filePath1);
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws FileNotFoundException {
               throw new FileNotFoundException();
            }
        }).when(trans).info();
        //cachingFileAccess.loadOrDefault(trans, "bs", "also bs", "test", null);    //TODO: Needs more testing AAF-111
        //keyfile1.delete();
    }

    @Test
    public void testInvalidate() {
        //NavigableMap<String,org.onap.aaf.auth.rserv.Content> content = new ConcurrentSkipListMap<>();
        //Content con = mock(Content.class);
        //content.put("hello", con);
        cachingFileAccess.invalidate("hello");
    }

}
