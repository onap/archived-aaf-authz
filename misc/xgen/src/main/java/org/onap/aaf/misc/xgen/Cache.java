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

package org.onap.aaf.misc.xgen;


public interface Cache<G extends XGen<G>> {
    public void dynamic(G hgen, Code<G> code);
    
    public static class Null<N extends XGen<N>> implements Cache<N> {
		private static Null<?> singleton = new Null();
        @Override
        public void dynamic(N hgen, Code<N> code) {} // NO_OP, no matter what type

        @SuppressWarnings("rawtypes")
        public static Null<?> singleton() { return singleton;}
    }

}