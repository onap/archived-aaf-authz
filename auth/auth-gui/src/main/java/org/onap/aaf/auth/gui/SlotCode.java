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

package org.onap.aaf.auth.gui;

import org.onap.aaf.misc.env.EnvStore;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.TransStore;

public abstract class SlotCode<TRANS extends TransStore> extends NamedCode {
    private Slot[] slots;

    public SlotCode(boolean no_cache,EnvStore<?> env, String root, Enum<?> ... params) {
        super(no_cache,root);
        slots = new Slot[params.length];
        for (int i=0;i<params.length;++i) {
            slots[i] = env.slot(root + '.' + params[i].name());
        }
    }

    public<T> T get(TRANS trans,Enum<?> en, T dflt) {
        return get(trans,en.ordinal(),dflt);
    }

    public<T> T get(TRANS trans,int idx, T dflt) {
        if (idx>slots.length) {
            return dflt;
        }
        return trans.get(slots[idx],dflt);
    }
}
