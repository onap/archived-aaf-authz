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
 */

package org.onap.aaf.auth.util.test;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestKill implements Runnable {

    public static void main(String[] args) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        TestKill tk = new TestKill();
        Future<?> app = es.submit(tk);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutdown Hook, thread: setting interrupt");
                app.cancel(true);
                tk.longProcess();
                es.shutdown();
            }
        });
        System.out.println("Service Start");
        System.out.print("Hit <enter> to end:");
        try {
            System.in.read();
            System.exit(0);
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
    }

    private void longProcess() {
        System.out.println("Starting long cleanup process");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Ending long cleanup process");
    }
}
