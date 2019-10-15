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
import java.util.List;

import org.onap.aaf.misc.rosetta.Ladder;
import org.onap.aaf.misc.rosetta.Marshal;
import org.onap.aaf.misc.rosetta.ParseException;
import org.onap.aaf.misc.rosetta.Parsed;


public abstract class ObjArray<T,S> extends Marshal<T> {
    private String name;
    private Marshal<S> subMarshaller;

    public ObjArray(String name, Marshal<S> subMarshaller) {
        this.name = name;
        this.subMarshaller = subMarshaller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parsed<State> parse(T t, Parsed<State> parsed) throws ParseException {
        Ladder<Iterator<?>> ladder = parsed.state.ladder;
        Iterator<?> iter = ladder.peek();
        if (iter==null) {
            List<S> list = data(t);
            if (list.isEmpty() && parsed.state.smallest) {
                ladder.push(DONE_ITERATOR);
            } else {
                ladder.push(new ListIterator<S>(list));
                parsed.event = START_ARRAY;
                parsed.name = name;
            }
        } else if (!DONE_ITERATOR.equals(iter)) {
            ladder.ascend(); // look at field info
            Iterator<?> memIter = ladder.peek();
            ListIterator<S> mems = (ListIterator<S>)iter;
            S mem;
            if (memIter==null) {
                mem=mems.next();
            } else if (!DONE_ITERATOR.equals(memIter)) {
                mem=mems.peek();
            } else if (iter.hasNext()) {
                mem=null;
                ladder.push(null);
            } else {
                mem=null;
            }

            if (mem!=null)
                parsed = subMarshaller.parse(mem, parsed);
            ladder.descend();
            if (mem==null) {
                if (iter.hasNext()) {
                    parsed.event = NEXT;
                } else {
                    parsed.event = END_ARRAY;
                    ladder.push(DONE_ITERATOR);
                }
            }
        }
        return parsed; // if unchanged, then it will end process
    }

    protected abstract List<S> data(T t);

}
