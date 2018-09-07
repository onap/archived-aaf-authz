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

package org.onap.aaf.auth.layer.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletRequest;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

import junit.framework.Assert;

public class JU_Result {
    Result result;
//    @Mock
//    RV value;
    int status=0;
    String details = "details";
    String[] variables;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void setUp(){
        //result = mock(Result.class);

    }

    @Test
    public void testOk() {
        Object value = null;
        Collection col = new ArrayList();
        List list = mock(List.class);
        Set set = mock(Set.class);
        Integer[] R = new Integer[1];

        Assert.assertNotNull(Result.ok());
        Assert.assertNotNull(Result.ok(value));
        Assert.assertNotNull(Result.ok(col));
        Assert.assertNotNull(Result.ok(list));
        Assert.assertNotNull(Result.ok(set));
        Assert.assertNotNull(Result.ok(R));

        Collection<String> col1 = new ArrayList();
        List<String> list1 = new ArrayList();
        Set<String> set1 = new HashSet<>();
        Integer[] R1 = new Integer[0];
        set1.add("derp");
        list1.add("test");
        col1.add("TEST");

        Assert.assertNotNull(Result.ok(col1));
        Assert.assertNotNull(Result.ok(list1));
        Assert.assertNotNull(Result.ok(set1));
        Assert.assertNotNull(Result.ok(R1));
    }

    @Test
    public void testErr() {
        Result result = Result.create(null, 0, null, null);
        Result r = result;
        Exception e = mock(Exception.class);

        Assert.assertNotNull(result.err(r));                    //Result case
        Assert.assertNotNull(result.err(e));                    //Exception case
        Assert.assertNotNull(result.err(0, "test", "test"));    //Multiple case

    }

    @Test
    public void testCreate() {
        Result result = Result.create(null, 0, null, null);
        Assert.assertNotNull(Result.create(null, 0, null, null));
        Assert.assertNotNull(Result.create(null, 0, null, "arg"));
        Assert.assertNotNull(result.create(0, result));
    }

    @Test
    public void testOks() {
        Result result = Result.create(null, 0, null, null);

        Assert.assertNotNull(result.isOK());
        Assert.assertNotNull(result.notOK());
        Assert.assertNotNull(result.isOKhasData());
        Assert.assertNotNull(result.notOKorIsEmpty());

        Result result1 = Result.create(null, 5, "test", "test");
        Assert.assertNotNull(result1.emptyList(true));
        Assert.assertNotNull(result1.isOK());
        Assert.assertNotNull(result1.notOK());
        Assert.assertNotNull(result1.isOKhasData());
        Assert.assertNotNull(result1.notOKorIsEmpty());

        Result result2 = Result.create(null, 0, "test", "test");
        Assert.assertNotNull(result2.emptyList(false));
        Assert.assertNotNull(result2.isOKhasData());
        Assert.assertNotNull(result2.notOKorIsEmpty());
    }

    @Test
    public void testEmptyList() {
        Result result = Result.create(null, 0, null, null);

        Assert.assertNotNull(result.emptyList(true));
        Assert.assertNotNull(result.emptyList(false));
        Assert.assertFalse(result.isEmpty());
    }

    @Test
    public void testPartialContent() {
        Result result = Result.create(null, 0, null, null);

        Assert.assertNotNull(result.partialContent(true));
        Assert.assertNotNull(result.partialContent(false));
        Assert.assertFalse(result.partialContent());

        Result result1 = Result.create(null, 1, "test", null);
        Assert.assertNotNull(result1.partialContent(true));
        Assert.assertNotNull(result1.partialContent());
    }

    @Test
    public void testToString() {
        Result result = Result.create(null, 0, null, null);

        Assert.assertNull(result.toString() );

        Result result1 = Result.create(null, 5, "test", "test");

        Assert.assertNotNull(result1.toString());

        int value = 1;
        Result result2 = Result.create(value , 5, "test", "test");

        Assert.assertNotNull(result2.toString());
    }

    @Test
    public void testErrorString() {
        Result result = Result.create(null, 0, "test", "test");
        Assert.assertEquals("Error - test", result.errorString());
        Result result1 = Result.create(null, 1, "test", "test");
        Assert.assertEquals("Security - test",result1.errorString());
        Result result2 = Result.create(null, 2, "test", "test");
        Assert.assertEquals("Denied - test",result2.errorString());
        Result result3 = Result.create(null, 3, "test", "test");
        Assert.assertEquals("Policy - test",result3.errorString());
        Result result4 = Result.create(null, 4, "test", "test");
        Assert.assertEquals("BadData - test",result4.errorString());
        Result result5 = Result.create(null, 5, "test", "test");
        Assert.assertEquals("NotImplemented - test",result5.errorString());
        Result result6 = Result.create(null, 6, "test", "test");
        Assert.assertEquals("NotFound - test",result6.errorString());
        Result result7 = Result.create(null, 7, "test", "test");
        Assert.assertEquals("AlreadyExists - test",result7.errorString());
        Result result8 = Result.create(null, 8, "test", "test");
        Assert.assertEquals("ActionNotComplete - test",result8.errorString());
    }


}








