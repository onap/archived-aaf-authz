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

/**
 * Need an Iterator that can peek the current value without changing
 * @author Jonathan
 *
 * @param <T>
 */
final class ListIterator<T> implements Iterator<T> {
    private T curr;
    private Iterator<T> delg;
    public ListIterator(List<T> list) {
        curr = null;
        delg = list.iterator();
    }
    @Override
    public boolean hasNext() {
        return delg.hasNext();
    }

    @Override
    public T next() {
        return curr = delg.hasNext()?delg.next():null;
    }

    public T peek() {
        return curr==null?next():curr;
    }

    @Override
    public void remove() {
        delg.remove();
    }

}