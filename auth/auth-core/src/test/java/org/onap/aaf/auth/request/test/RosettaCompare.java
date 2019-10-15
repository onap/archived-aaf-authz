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

package org.onap.aaf.auth.request.test;

import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Data;
import org.onap.aaf.misc.env.Data.TYPE;
import org.onap.aaf.misc.rosetta.env.RosettaDF;
import org.onap.aaf.misc.rosetta.env.RosettaData;
import org.onap.aaf.misc.rosetta.env.RosettaEnv;

public abstract class RosettaCompare<T> {
    protected Class<T> cls;
    private static int count = 0;

    public RosettaCompare(Class<T> cls) {
        this.cls = cls;
    }

    public void run(RosettaEnv env) throws APIException {
        RosettaDF<T> nsrDF = env.newDataFactory(cls);
        compare(nsrDF.newData().option(Data.PRETTY),newOne(),this);
    }

    private void compare(RosettaData<T> rdt, T t, RosettaCompare<T> comp) throws APIException {
        //System.out.println("########### Testing " + cls.getName() + " ##############");
        String s = rdt.load(t).out(TYPE.JSON).asString();
        //System.out.println(s);
        T t2 = rdt.in(TYPE.JSON).load(s).asObject();
        comp.compare(t, t2);

        //System.out.println();

        s = rdt.load(t).out(TYPE.XML).asString();
        //System.out.println(s);
        t2 = rdt.in(TYPE.XML).load(s).asObject();
        comp.compare(t, t2);
    }

    public synchronized static String instance() {
        return "_"+ ++count;
    }

    public abstract void compare(T t1, T t2);
    public abstract T newOne();

}