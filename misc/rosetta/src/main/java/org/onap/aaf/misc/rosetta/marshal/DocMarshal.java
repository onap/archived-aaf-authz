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

public class DocMarshal<T> extends Marshal<T> {
    private Marshal<T> root;

    public DocMarshal(Marshal<T> root) {
        this.root = root;
    }

    @Override
    public Parsed<State> parse(T t, Parsed<State> parsed) throws ParseException {
        Ladder<Iterator<?>> ladder = parsed.state.ladder;
        Iterator<?> iter = ladder.peek();
        if (iter==null) {
            ladder.push(PENDING_ITERATOR);
            parsed.event = START_DOC;
        } else if (DONE_ITERATOR.equals(iter)) {
        } else {
            ladder.ascend(); // look at field info
                Iterator<?> currFieldIter = ladder.peek();
                if (!DONE_ITERATOR.equals(currFieldIter)){
                    parsed = root.parse(t, parsed);
                }
            ladder.descend();
            if (DONE_ITERATOR.equals(currFieldIter) || parsed.event==NONE) {
                parsed.event = END_DOC;
                ladder.push(DONE_ITERATOR);
            }
        }
        return parsed; // if unchanged, then it will end process

    }

    public static final Iterator<Void> PENDING_ITERATOR = new Iterator<Void>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Void next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return null;
        }

        @Override
        public void remove() {
        }
    };

    public static<T> DocMarshal<T> root(Marshal<T> m) {
        return (DocMarshal<T>)new DocMarshal<T>(m);
    }

}
