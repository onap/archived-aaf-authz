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

package org.onap.aaf.auth.helpers.test;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.aaf.auth.helpers.InputIterator;

import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import org.junit.Test;

public class JU_InputIterator {
    
    InputIterator inputIterator;
    File f;
    BufferedReader bReader;
    PrintStream pStream;
    
    @Before
    public void setUp() throws IOException {
        f = new File("file");
        f.createNewFile();
        bReader = new BufferedReader(new FileReader(f));
        pStream = new PrintStream(f);
        inputIterator = new InputIterator(bReader, pStream, "prompt", "instructions");
    }

    @Test
    public void test() {
        inputIterator.iterator();
        inputIterator.iterator().hasNext();
        inputIterator.iterator().next();
        inputIterator.iterator().remove();
    }
    
    @After
    public void cleanUp() {
        if(f.exists()) {
            f.delete();
        }
    }
}
