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

package org.onap.aaf.auth.validation.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import static org.mockito.Matchers.*;
import org.mockito.Mock;

import org.junit.Before;
import org.junit.Test;
import org.junit.Test;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.env.AuthzTransOnlyFilter;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.validation.Validator;
import org.onap.aaf.cadi.principal.TaggedPrincipal;

import junit.framework.Assert;

public class JU_Validator {

    Validator validator;
    String base = "\\x25\\x28\\x29\\x2C-\\x2E\\x30-\\x39\\x3D\\x40-\\x5A\\x5F\\x61-\\x7A";

    @Before
    public void setUp() {
        validator = new Validator();
    }

    @Test
    public void testNullOrBlank() {
        validator.nullOrBlank(null, "str");
        validator.nullOrBlank("test", "");
        validator.nullOrBlank("test", null);
    }

    @Test
    public void testIsNull() {
        Object o = new Object();
        validator.isNull(null, null);
        validator.isNull(null, o);
    }

    @Test
    public void testDescription() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class c = validator.getClass();
        Class[] cArg = new Class[2];
        cArg[0] = String.class;
        cArg[1] = String.class;        //Steps to test a protected method
        Method descriptionMethod = c.getDeclaredMethod("description", cArg);
        descriptionMethod.setAccessible(true);
        descriptionMethod.invoke(validator,"test", "test1");
        descriptionMethod.invoke(validator,null, null);
        descriptionMethod.invoke(validator,null, "[\\\\x25\\\\x28\\\\x29\\\\x2C-\\\\x2E\\\\x30-\\\\x39\\\\x3D\\\\x40-\\\\x5A\\\\x5F\\\\x61-\\\\x7A\\\\x20]+");


    }

    @Test
    public void testPermType() {
        Assert.assertNotNull(validator.permType("[\\\\w.-]+"));
        Assert.assertNotNull(validator.permType(null));
        Assert.assertNotNull(validator.permType(""));
        Assert.assertNotNull(validator.permType("aewfew"));
    }

    @Test
    public void testPermType1() {
        Assert.assertNotNull(validator.permType("[\\\\w.-]+",null));
        Assert.assertNotNull(validator.permType(null,null));
        Assert.assertNotNull(validator.permType("","test"));
        Assert.assertNotNull(validator.permType("aewfew","test"));
    }

    @Test
    public void testPermInstance() {

        String middle = "]+[\\\\*]*|\\\\*|(([:/]\\\\*)|([:/][!]{0,1}[";
        Assert.assertNotNull(validator.permInstance("[" + base + middle + base + "]+[\\\\*]*[:/]*))+"));
        Assert.assertNotNull(validator.permInstance(null));
        Assert.assertNotNull(validator.permInstance(""));
        Assert.assertNotNull(validator.permInstance("test"));
    }

    @Test
    public void testErr() {
        Assert.assertFalse(validator.err());
        validator.isNull("test", null);
        Assert.assertTrue(validator.err());
    }

    @Test
    public void testErrs() {
        validator.isNull("test", null);
        Assert.assertNotNull(validator.errs());
    }

    @Test
    public void testPermAction() {
        Assert.assertNotNull(validator.permAction("[" + base + "]+" + "|\\\\*"));
        Assert.assertNotNull(validator.permAction("test"));
    }

    @Test
    public void testRole() {
        Assert.assertNotNull(validator.role("[\\\\w.-]+"));
        Assert.assertNotNull(validator.role(null));
        Assert.assertNotNull(validator.role(""));
        Assert.assertNotNull(validator.role("aewfew"));
    }

    @Test
    public void testNs() {
        Assert.assertNotNull(validator.ns("[\\\\w.-]+"));
        Assert.assertNotNull(validator.ns(""));
        Assert.assertNotNull(validator.ns(".access"));
    }

    @Test
    public void testKey() {
        Assert.assertNotNull(validator.key("[\\\\w.-]+"));
        Assert.assertNotNull(validator.key(""));
        Assert.assertNotNull(validator.key(".access"));
    }

    @Test
    public void testValue() {
        Assert.assertNotNull(validator.value(base));
        Assert.assertNotNull(validator.value(""));
        Assert.assertNotNull(validator.value(".access"));
    }

    @Test
    public void testNotOK() {
        Result<?> test = mock(Result.class);
        validator.isNull("test", null);
        when(test.notOK()).thenReturn(true);
        Assert.assertNotNull(validator.notOK(null));
        Assert.assertNotNull(validator.notOK(test));
    }

    @Test
    public void testIntRange() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class c = validator.getClass();
        Class[] cArg = new Class[4];
        cArg[0] = String.class;
        cArg[1] = int.class;
        cArg[2] = int.class;
        cArg[3] = int.class;        //Steps to test a protected method
        Method intRangeMethod = c.getDeclaredMethod("intRange", cArg);
        intRangeMethod.setAccessible(true);
        intRangeMethod.invoke(validator,"Test",5,1,10);
        intRangeMethod.invoke(validator,"Test",1,5,10);
        intRangeMethod.invoke(validator,"Test",11,5,10);
        intRangeMethod.invoke(validator,"Test",5,6,4);
    }

    @Test
    public void testFloatRange() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class c = validator.getClass();
        Class[] cArg = new Class[4];
        cArg[0] = String.class;
        cArg[1] = float.class;
        cArg[2] = float.class;
        cArg[3] = float.class;        //Steps to test a protected method
        Method floatRangeMethod = c.getDeclaredMethod("floatRange", cArg);
        floatRangeMethod.setAccessible(true);
        floatRangeMethod.invoke(validator,"Test",5f,1f,10f);
        floatRangeMethod.invoke(validator,"Test",1f,5f,10f);
        floatRangeMethod.invoke(validator,"Test",11f,5f,10f);
        floatRangeMethod.invoke(validator,"Test",5f,6f,4f);
    }

    @Test
    public void test() {
        assertTrue(Validator.ACTION_CHARS.matcher("HowdyDoody").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("Howd?yDoody").matches());
        assertTrue(Validator.ACTION_CHARS.matcher("_HowdyDoody").matches());
        assertTrue(Validator.INST_CHARS.matcher("HowdyDoody").matches());
        assertFalse(Validator.INST_CHARS.matcher("Howd?yDoody").matches());
        assertTrue(Validator.INST_CHARS.matcher("_HowdyDoody").matches());

        //
        assertTrue(Validator.ACTION_CHARS.matcher("*").matches());
        assertTrue(Validator.INST_CHARS.matcher("*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":*:*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":*:*").matches());

        assertFalse(Validator.ACTION_CHARS.matcher(":hello").matches());
        assertTrue(Validator.INST_CHARS.matcher(":hello").matches());
        assertFalse(Validator.INST_CHARS.matcher("hello:").matches());
        assertFalse(Validator.INST_CHARS.matcher("hello:d").matches());

        assertFalse(Validator.ACTION_CHARS.matcher(":hello:*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":hello:*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":hello:d*:*").matches());
        assertFalse(Validator.INST_CHARS.matcher(":hello:d*d:*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":hello:d*:*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("HowdyDoody*").matches());
        assertFalse(Validator.INST_CHARS.matcher("Howdy*Doody").matches());
        assertTrue(Validator.INST_CHARS.matcher("HowdyDoody*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("*HowdyDoody").matches());
        assertFalse(Validator.INST_CHARS.matcher("*HowdyDoody").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":h*").matches());
        assertFalse(Validator.INST_CHARS.matcher(":h*h*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":h*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":h:h*:*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":h:h*:*").matches());
        assertFalse(Validator.INST_CHARS.matcher(":h:h*h:*").matches());
        assertFalse(Validator.INST_CHARS.matcher(":h:h*h*:*").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":h:*:*h").matches());
        assertFalse(Validator.INST_CHARS.matcher(":h:*:*h").matches());
        assertTrue(Validator.INST_CHARS.matcher(":com.test.*:ns:*").matches());


        assertFalse(Validator.ACTION_CHARS.matcher("1234+235gd").matches());
        assertTrue(Validator.ACTION_CHARS.matcher("1234-235gd").matches());
        assertTrue(Validator.ACTION_CHARS.matcher("1234-23_5gd").matches());
        assertTrue(Validator.ACTION_CHARS.matcher("1234-235g,d").matches());
        assertTrue(Validator.ACTION_CHARS.matcher("1234-235gd(Version12)").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("123#4-23@5g:d").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("123#4-23@5g:d").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("1234-23 5gd").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("1234-235gd ").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(" 1234-235gd").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(" ").matches());

        // Allow % and =   (Needed for Escaping & Base64 usages) jg
        assertTrue(Validator.ACTION_CHARS.matcher("1234%235g=d").matches());
        assertFalse(Validator.ACTION_CHARS.matcher(":1234%235g=d").matches());
        assertTrue(Validator.INST_CHARS.matcher("1234%235g=d").matches());
        assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d").matches());
        assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:%20==").matches());
        assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:==%20:=%23").matches());
        assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:*:=%23").matches());
        assertTrue(Validator.INST_CHARS.matcher(":1234%235g=d:==%20:*").matches());
        assertTrue(Validator.INST_CHARS.matcher(":*:==%20:*").matches());

        // Allow / instead of :  (more natural instance expression) jg
        assertFalse(Validator.INST_CHARS.matcher("1234/a").matches());
        assertTrue(Validator.INST_CHARS.matcher("/1234/a").matches());
        assertTrue(Validator.INST_CHARS.matcher("/1234/*/a/").matches());
        assertTrue(Validator.INST_CHARS.matcher("/1234//a").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("1234/a").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("/1234/*/a/").matches());
        assertFalse(Validator.ACTION_CHARS.matcher("1234//a").matches());


        assertFalse(Validator.INST_CHARS.matcher("1234+235gd").matches());
        assertTrue(Validator.INST_CHARS.matcher("1234-235gd").matches());
        assertTrue(Validator.INST_CHARS.matcher("1234-23_5gd").matches());
        assertTrue(Validator.INST_CHARS.matcher("1234-235g,d").matches());
        assertTrue(Validator.INST_CHARS.matcher("m1234@shb.dd.com").matches());
        assertTrue(Validator.INST_CHARS.matcher("1234-235gd(Version12)").matches());
        assertFalse(Validator.INST_CHARS.matcher("123#4-23@5g:d").matches());
        assertFalse(Validator.INST_CHARS.matcher("123#4-23@5g:d").matches());
        assertFalse(Validator.INST_CHARS.matcher("").matches());


        for( char c=0x20;c<0x7F;++c) {
            boolean b;
            switch(c) {
                case '?':
                case '|':
                case '*':
                    continue; // test separately
                case '~':
                case ',':
                    b = false;
                    break;
                default:
                    b=true;
            }
        }

        assertFalse(Validator.ID_CHARS.matcher("abc").matches());
        assertFalse(Validator.ID_CHARS.matcher("").matches());
        assertTrue(Validator.ID_CHARS.matcher("abc@att.com").matches());
        assertTrue(Validator.ID_CHARS.matcher("ab-me@att.com").matches());
        assertTrue(Validator.ID_CHARS.matcher("ab-me_.x@att._-com").matches());

        assertFalse(Validator.NAME_CHARS.matcher("ab-me_.x@att._-com").matches());
        assertTrue(Validator.NAME_CHARS.matcher("ab-me").matches());
        assertTrue(Validator.NAME_CHARS.matcher("ab-me_.xatt._-com").matches());


        // 7/22/2016
        assertTrue(Validator.INST_CHARS.matcher(
                "/!com.att.*/role/write").matches());
        assertTrue(Validator.INST_CHARS.matcher(
                ":!com.att.*:role:write").matches());

    }

}
