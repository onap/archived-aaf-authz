/*
 * Copyright (C) 2019 Ericsson Software Technology AB. All rights reserved.
 *
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
 * limitations under the License
 */
package org.onap.aaf.auth.cm.cmpv2client.impl;

/**
 * The CAOfflineException wraps java.net.ConnectException. Exception thrown during Http Method call towards External CA
 * Server if Offline. Signals an error occurred while attempting to connect a socket to a remote address and port. The
 * connection was refused remotely (e.g., no process is listening on the remote address/port).
 */
public class CAOfflineException extends Exception {

    private static final long serialVersionUID = 2L;

    /**
     * Creates a new instance without detail message.
     */
    public CAOfflineException() {
        super();
    }

    /**
     * Constructs an instance with the specified detail message.
     *
     * @param msg the detail message.
     */
    public CAOfflineException(String msg) {
        super(msg);
    }
}
