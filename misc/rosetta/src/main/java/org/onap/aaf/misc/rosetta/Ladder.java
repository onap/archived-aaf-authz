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

package org.onap.aaf.misc.rosetta;


/**
 * A Ladder is a Stack like Storage Class, but where you can ascend and descend while
 * the elements exists.
 * <p>
 * Like an extension ladder, you can make taller as you go
 * <p>
 * @author Jonathan
 *
 */
public class Ladder<T> {
    public static final int DEFAULT_INIT_SIZE=8;
    private final int init_size;
    private int rung; // as in ladder
    private Object[] struts;

    public Ladder() {
        rung=0;
        init_size = DEFAULT_INIT_SIZE;
        struts=new Object[init_size];
    }

    public Ladder(int initSize) {
        rung=0;
        init_size = initSize;
        struts=new Object[init_size];
    }

    public void bottom() {
        rung = 0;
    }

    public void top() {
        rung = struts.length-1;
        while (rung>0 && struts[rung]==null)--rung;
    }

    public int howHigh() {
        return rung;
    }

    public void jumpTo(int rung) {
        if (rung>=struts.length) {
            Object[] temp = new Object[init_size*((rung/init_size)+1)];
            System.arraycopy(struts, 0, temp, 0, struts.length);
            struts = temp;
        }
        this.rung = rung;
    }

    public int height() {
        return struts.length;
    }

    public void cutTo(int rungs) {
        Object[] temp = new Object[rungs];
        System.arraycopy(struts, 0, temp, 0, Math.min(rungs, struts.length));
        struts = temp;
    }

    public void ascend() {
        ++rung;
        if (rung>=struts.length) {
            Object[] temp = new Object[struts.length+init_size];
            System.arraycopy(struts, 0, temp, 0, struts.length);
            struts = temp;
        }
    }

    public void descend() {
        --rung;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        return (T)struts[rung];
    }

    public void push(T t) {
        struts[rung]=t;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        T t = (T)struts[rung];
        struts[rung]=null;
        return t;
    }

}
