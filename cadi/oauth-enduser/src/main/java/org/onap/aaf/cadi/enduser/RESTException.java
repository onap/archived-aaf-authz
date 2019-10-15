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
package org.onap.aaf.cadi.enduser;

import org.onap.aaf.cadi.client.Future;

public class RESTException extends Exception {
    /**
     * <p>
     */
    private static final long serialVersionUID = -5232371598208651058L;
    private Future<?> future;

    public RESTException(Future<?> future) {
        this.future = future;
    }

    public int getCode() {
        return future.code();
    }

    public String getMsg() {
        return future.body();
    }

    public String errorString() {
        String body = future.body();
        return "RESTClient Error: "  + future.code() + ": " + (body.isEmpty()?"<no message in call>":body);
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage() {
        return errorString();
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#getLocalizedMessage()
     */
    @Override
    public String getLocalizedMessage() {
        return errorString();
    }


}
