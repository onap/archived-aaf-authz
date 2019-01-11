package org.onap.aaf.auth.batch.reports.bodies;

import org.junit.Assert;
import org.junit.Test;
import org.onap.aaf.auth.batch.reports.Notify;
import org.onap.aaf.auth.env.AuthzTrans;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class AbsCredBodyTest {

    @Test
    public void testUserWithValue() {
        String testStr = "test";
        List<String> row = Collections.singletonList(testStr);
        AbsCredBody absCredBody = new AbsCredBody("") {
            @Override
            public String body(AuthzTrans trans, Notify n, String id) {
                return null;
            }
        };
        Assert.assertEquals(testStr, absCredBody.user(row));
    }

    @Test
    public void testUserWithoutValue() {
        //String testStr = "test";
        List<String> row = Collections.EMPTY_LIST;
        AbsCredBody absCredBody = new AbsCredBody("") {
            @Override
            public String body(AuthzTrans trans, Notify n, String id) {
                return null;
            }
        };
        Assert.assertNull(absCredBody.user(row));
    }
}