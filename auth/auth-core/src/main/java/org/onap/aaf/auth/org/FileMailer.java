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
package org.onap.aaf.auth.org;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.util.Chrono;

public class FileMailer implements Mailer {
    private Path dir;
    private String mail_from;
    private String testName;
    private int count;


    public FileMailer(Access access) throws APIException {
        count = 0;
    
        mail_from = access.getProperty("MAIL_FROM", null);
        if(mail_from==null) {
            throw new APIException("MAIL_FROM property is required for Email Notifications");
        }
        String env = access.getProperty("CASS_ENV", "UNKNOWN");
        String logdir = access.getProperty("LOG_DIR",null);
        if(logdir==null) {
            logdir=access.getProperty(env+".LOG_DIR", "logs/"+env);
        }
        dir = Paths.get(logdir+"/email/"+Chrono.dateOnlyStamp());
        if(!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new APIException("Cannot create directory: " + dir.toString(),e);
            }
        }
    
        boolean dryrun = Boolean.parseBoolean(access.getProperty("DRY_RUN","false"));
        String str = access.getProperty("MAX_EMAIL", null);
        int maxEmail = str==null || str.isEmpty()?Integer.MAX_VALUE:Integer.parseInt(str);
        if(dryrun && maxEmail==1) {
            testName = "email_test";
        } else {
            testName = null;
        }
    }

    @Override
    public boolean sendEmail(AuthzTrans trans, String test, List<String> toList, List<String> ccList,
            String subject, String body, Boolean urgent) throws OrganizationException {
        boolean status = false;
        try {
            Path path;
            if(testName==null) {
                path = Files.createTempFile(dir, "email", ".hdr");
            } else {
                path = Paths.get(dir.toString(), "emailTEST"+test+".hdr");
            }
            BufferedWriter bw = Files.newBufferedWriter(path);
            try {
                bw.write("TO: ");
                boolean first = true;
                for(String to : toList) {
                    if(first) {
                        first = false;
                    } else {
                        bw.write(',');
                    }
                    bw.write(to);
                }
                bw.newLine();
            
                bw.write("CC: ");
                first = true;
                for(String cc : ccList) {
                    if(first) {
                        first = false;
                    } else {
                        bw.write(',');
                    }
                    bw.write(cc);
                }
                bw.newLine();
            
                bw.write("FROM: ");
                bw.write(mail_from);
                bw.newLine();
            
                bw.write("SUBJECT: ");
                bw.write(subject);
                bw.newLine();
            
                if(urgent) {
                    bw.write("Importance: High");  
                    bw.newLine();
                }

            } finally {
                bw.close();
            }

            path = Paths.get(path.toString().replaceAll(".hdr", ".html"));
            bw = Files.newBufferedWriter(path);
            try {
                bw.write(body);
                bw.newLine();
            } finally {
                bw.close();
            }
            status = true;
        } catch ( IOException e) {
            throw new OrganizationException(e);
        }
        ++count;
        return status;
    }

    @Override
    public String mailFrom() {
        return mail_from;
    }

    @Override
    public int count() {
        return count;
    }
}
