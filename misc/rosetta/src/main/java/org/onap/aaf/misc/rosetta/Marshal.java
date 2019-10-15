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

package org.onap.aaf.misc.rosetta;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.TimeTaken;

public abstract class Marshal<T> implements Parse<T, Marshal.State> {

    /* (non-Javadoc)
     * @see org.onap.aaf.misc.rosetta.Parse#newParsed()
     */
    @Override
    public Parsed<State> newParsed() throws ParseException {
        return new Parsed<State>(new State());
    }

    @Override
    public TimeTaken start(Env env) {
        //TODO is a way to mark not-JSON?
        return env.start("Rosetta Marshal", Env.JSON);
    };

    public static class State {
        // Note:  Need a STATEFUL stack... one that will remain stateful until marked as finished
        // "finished" is know by Iterators with no more to do/null
        // Thus the concept of "Ladder", which one ascends and decends
        public Ladder<Iterator<?>> ladder = new Ladder<Iterator<?>>();
        public boolean smallest = true;
    }

    public static final Iterator<Void> DONE_ITERATOR = new Iterator<Void>() {
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

    /**
     * Typical definition of Done is when Iterator in Ladder is "DONE_ITERATOR"
     *
     * It is important, however, that the "Ladder Rung" is set to the right level.
     *
     * @param state
     * @return
     */
    public boolean amFinished(State state) {
        return DONE_ITERATOR.equals(state.ladder.peek());
    }

}
