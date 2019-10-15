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

package org.onap.aaf.auth.local.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.Test;
import org.onap.aaf.auth.local.DataFile;
import org.onap.aaf.auth.local.DataFile.Token;
import org.onap.aaf.auth.local.DataFile.Token.Field;

public class JU_DataFile {

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

//    @Test
//    public void netYetTested() {
//        fail("Tests not yet implemented");
//    }

//    @Test
//    public void test() throws Exception {
//        File file = new File("../authz-batch/data/v1.dat");
//        DataFile df = new DataFile(file,"r");
//        int count = 0;
//        List<String> list = new ArrayList<>();
//        try {
//            df.open();
//            Token tok = df.new Token(1024000);
//            Field fld = tok.new Field('|');
//
//            while (tok.nextLine()) {
//                ++count;
//                fld.reset();
//                list.add(fld.at(0));
//            }
////            Collections.sort(list);
//            for (String s: list) {
//                System.out.println(s);
//
//            }
//        } finally {
//            System.out.printf("%15s:%12d\n","Total",count);
//        }
//    }

}
