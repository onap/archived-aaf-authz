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

package org.onap.aaf.misc.rosetta.marshal;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.onap.aaf.misc.rosetta.Ladder;
import org.onap.aaf.misc.rosetta.Marshal;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.Parsed;

/**
 * Object Marshal
 * Assumes has Fields and other Objects
 * s
 * @author Jonathan
 *
 * @param <T>
 */
public abstract class ObjMarshal<T> extends Marshal<T> {
    // Note: Not Using List or ArrayList, because there is no "Peek" concept in their iterator.
    private Marshal<T>[] pml;
    private int end=0;

    /**
     * @param pm
     */
    @SuppressWarnings("unchecked")
    protected void add(Marshal<T> pm) {
        if (pml==null) {
            pml = new Marshal[Ladder.DEFAULT_INIT_SIZE]; 
        } else if (end>pml.length) {
            Object temp[] = pml; 
            pml = new Marshal[pml.length+Ladder.DEFAULT_INIT_SIZE];
            System.arraycopy(temp, 0, pml, 0, pml.length);
        }
        pml[end]=pm;
        ++end;
    }

    /* (non-Javadoc)
     * @see org.onap.aaf.misc.rosetta.Parse#parse(java.lang.Object, org.onap.aaf.misc.rosetta.Parsed)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Parsed<State> parse(T in, Parsed<State> parsed) throws ParseException {
        Ladder<Iterator<?>> ladder = parsed.state.ladder;
        Iterator<Marshal<T>> iter = (Iterator<Marshal<T>>)ladder.peek();
        if (iter==null) {
            if (pml.length>0) {
                ladder.push(new FieldsIterator());
                parsed.event = START_OBJ;
            } else {
                ladder.push(DONE_ITERATOR);
            }
        } else if (!DONE_ITERATOR.equals(iter)) {
            FieldsIterator fields = (FieldsIterator)iter;
            ladder.ascend(); // look at field info
            Iterator<?> currFieldIter = ladder.peek();
            Marshal<T> marshal;
            if (currFieldIter==null) {
                marshal=fields.next();
            } else if (!DONE_ITERATOR.equals(currFieldIter)) {
                marshal=fields.peek();
                if (marshal==null && fields.hasNext())marshal=fields.next();
            } else if (fields.hasNext()) {
                marshal=fields.next();
                ladder.push(null);
            } else {
                marshal=null;
            }

            if (marshal!=null)
                parsed = marshal.parse(in, parsed);
            ladder.descend();
            if (marshal==null || parsed.event==NONE) {
                parsed.event = END_OBJ;
                ladder.push(DONE_ITERATOR);
            }
        }
        return parsed; // if unchanged, then it will end process
    }

    private class FieldsIterator implements Iterator<Marshal<T>> {
        private int idx = -1;

        @Override
        public boolean hasNext() {
            return idx<end;
        }

        @Override
        public Marshal<T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return pml[++idx];
        }

        public Marshal<T> peek() {
            return idx<0?null:pml[idx];
        }
    
        @Override
        public void remove() {
            pml[idx]=null;
        }
    
    }

}
