/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 IBM.
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
 */package org.onap.aaf.auth.batch.reports;

 import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.approvalsets.Pending;
import org.onap.aaf.auth.batch.helpers.CQLBatch;
import org.onap.aaf.auth.batch.helpers.CQLBatchLoop;
import org.onap.aaf.auth.batch.helpers.LastNotified;
import org.onap.aaf.auth.batch.reports.bodies.NotifyBody;
import org.onap.aaf.auth.batch.reports.bodies.NotifyPendingApprBody;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Mailer;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.TimeTaken;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.util.Chrono;

 public class Notify extends Batch {
     private static final String HTML_CSS = "HTML_CSS";
     private final Mailer mailer;
     private final String header;
     private final String footer;
     private final int maxEmails;
     private final int indent;
     private final boolean urgent;
     public final String guiURL;
     private PropAccess access;
     private AuthzTrans noAvg;
     private CQLBatch cqlBatch;
    private LastNotified  lastN;
    private CQLBatchLoop cbl;

     public Notify(AuthzTrans trans) throws APIException, IOException, OrganizationException {
         super(trans.env());
         access = env.access();
         session = cluster.connect();

         String mailerCls = env.getProperty("MAILER");
         String mailFrom = env.getProperty("MAIL_FROM");
         String header_html = env.getProperty("HEADER_HTML");
         String footer_html = env.getProperty("FOOTER_HTML");
         String str = env.getProperty("MAX_EMAIL");
         guiURL = env.getProperty("GUI_URL");
         maxEmails = str==null||str.isEmpty()?Integer.MAX_VALUE:Integer.parseInt(str);
         if(mailerCls==null || mailFrom==null || guiURL==null || header_html==null || footer_html==null) {
             throw new APIException("Notify requires MAILER, MAILER_FROM, GUI_URL, HEADER_HTML and FOOTER_HTML properties");
         }
         try {
             Class<?> mailc = Class.forName(mailerCls);
             Constructor<?> mailcst = mailc.getConstructor(Access.class);
             mailer = (Mailer)mailcst.newInstance(env.access());
         } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
             throw new APIException("Unable to construct " + mailerCls,e);
         }

         String line;
         StringBuilder sb = new StringBuilder();
         File fhh = new File(header_html);
         if(!fhh.exists()) {
             throw new APIException(header_html + " does not exist");
         }
         BufferedReader br = new BufferedReader(new FileReader(fhh));
         try {
             while((line=br.readLine())!=null) {
                 sb.append(line);
                 sb.append('\n');
             }
             String html_css = env.getProperty(HTML_CSS);
             String temp;
             int hc = sb.indexOf(HTML_CSS);
             if(hc!=0 && html_css!=null) {
                 temp = sb.replace(hc,hc+HTML_CSS.length(), html_css).toString();
             } else {
                 temp = sb.toString();
             }
             header = temp.replace("AAF:ENV", batchEnv);
         } finally {
             br.close();
         }

         // Establish index from header
         int lastTag = header.lastIndexOf('<');
         if(lastTag>0) {
             int prevCR = header.lastIndexOf('\n',lastTag);
             if(prevCR>0) {
                 indent = lastTag-prevCR;
             } else {
                 indent = 6; //arbitrary
             }
         } else {
             indent = 6;
         }

         urgent = false;
         
         sb.setLength(0);
         fhh = new File(footer_html);
         if(!fhh.exists()) {
             throw new APIException(footer_html + " does not exist");
         }

         br = new BufferedReader(new FileReader(fhh));
         try {
             while((line=br.readLine())!=null) {
                 sb.append(line);
                 sb.append('\n');
             }
             footer = sb.toString();
         } finally {
             br.close();
         }

         noAvg = trans.env().newTransNoAvg();
         cqlBatch = new CQLBatch(noAvg.debug(),session); 
          cbl = new CQLBatchLoop(cqlBatch,50,dryRun);
         
          lastN = new LastNotified(session);
     }

     /*
      * Note: We try to put things related to Notify as Main Class in Run, where we might have put in 
      * Constructor, so that we can have other Classes call just the "notify" method.
      */
     @Override
     protected void run(AuthzTrans trans) {

         final Holder<List<String>> info = new Holder<>(null);
         final Set<String> errorSet = new HashSet<>();
         String fmt = "%s"+Chrono.dateOnlyStamp()+".csv";

         try {
             // Class Load possible data
             NotifyBody.load(env.access());


             // Create Intermediate Output 
             File logDir = logDir();
             Set<File> notifyFile = new HashSet<>();
             if(args().length>0) {
                 for(int i=0;i<args().length;++i) {
                     notifyFile.add(new File(logDir, args()[i]));
                 }
             } else {
                 File file;
                 for(NotifyBody nb : NotifyBody.getAll()) {
                     file = new File(logDir,String.format(fmt, nb.name()));
                     if(file.exists()) {
                         trans.info().printf("Processing '%s' in %s",nb.type(),file.getCanonicalPath());
                         notifyFile.add(file);
                     } else {
                         trans.info().printf("No Files found for %s",nb.name());
                     }
                 }
             }

             for(File f : notifyFile) {
                 CSV csv = new CSV(env.access(),f);
                 try {
                     csv.visit(new CSV.Visitor() {
                         @Override
                         public void visit(List<String> row) throws IOException, CadiException {
                             if("info".equals(row.get(0))) {
                                 info.set(row);
                             }
                             if(info.get()==null) {
                                 throw new CadiException("First line of Feed MUST contain 'info' record");
                             }                             String key = row.get(0)+'|'+info.get().get(1);
                             NotifyBody body = NotifyBody.get(key);
                             if(body==null) {
                                 errorSet.add("No NotifyBody defined for " + key);
                             } else {
                                 body.store(row);
                             }
                         }
                     });
                 } catch (IOException | CadiException e) {
                     e.printStackTrace();
                 }

             }    

             // now create Notification
             for(NotifyBody nb : NotifyBody.getAll()) {
                 int count = notify(noAvg, nb);
                 trans.info().printf("Emailed %d for %s",count,nb.name());
             }
             
             //
             // Do Pending Approval Notifies. We do this separately, because we are consolidating
             // all the new entries, etc.
             //
             List<CSV> csvList = new ArrayList<>();
             for(String s : new String[] {"Approvals","ApprovalsNew"}) {
                 File f = new File(logDir(),String.format(fmt, s));
                 if(f.exists()) {
                     csvList.add(new CSV(access,f));
                 }
             }
             
             Map<String,Pending> mpending = new TreeMap<>();
             Holder<Integer> count = new Holder<>(0);
             for(CSV approveCSV : csvList) {
                TimeTaken tt = trans.start("Load Analyzed Reminders",Trans.SUB,approveCSV.name());
                try {
                    approveCSV.visit(row -> {
                        switch(row.get(0)) {
                            case Pending.REMIND:
                                try {
                                    String user = row.get(1);
                                    Pending p = new Pending(row);
                                    Pending mp = mpending.get(user);
                                    if(mp==null) {
                                        mpending.put(user, p);
                                    } else {
                                        mp.inc(p); // FYI, unlikely
                                    }
                                    count.set(count.get()+1);
                                } catch (ParseException e) {
                                    trans.error().log(e);
                                } 
                            break;
                        }
                    });
                } catch (IOException | CadiException e) {
                    trans.error().log(e);
                } finally {
                    tt.done();
                }
            }
            trans.info().printf("Read %d Reminder Rows", count.get());
            
            NotifyPendingApprBody npab = new NotifyPendingApprBody(access);

            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.DAY_OF_MONTH, 7); // Get from INFO?
            Date oneWeek = gc.getTime();
            CSV.Saver rs = new CSV.Saver();
            
            TimeTaken tt = trans.start("Obtain Last Notifications for Approvers", Trans.SUB);
            try {
                lastN.add(mpending.keySet());
            } finally {
                tt.done();
            }
            
            Pending p;
            
            tt = trans.start("Notify for Pending", Trans.SUB);
            List<String> idList = new ArrayList<String>();
            String id;
            try {
                for(Entry<String, Pending> es : mpending.entrySet()) {
                    id = es.getKey();
                    idList.clear();
                    idList.add(id);
                    p = es.getValue();
                    boolean nap = p.newApprovals();
                    if(!nap) {
                        Date dateLastNotified = lastN.lastNotified(id,"pending","");
                        if(dateLastNotified==null || dateLastNotified.after(oneWeek) ) {
                            nap=true;
                        }
                    }
                    if(nap) {
                        rs.row("appr", id,p.qty(),batchEnv);
                        npab.store(rs.asList());
                        if(notify(noAvg, npab)>0) {
                            npab.record(trans,cbl.inc(), id, idList, lastN);
                            npab.inc();
                        }
                    }
                }
            } finally {
                cbl.flush();
                tt.done();
                trans.info().printf("Notified %d persons of Pending Approvals", npab.count());
            }

        } catch (APIException | IOException e1) {
            trans.error().log(e1);
        } finally {
             for(String s : errorSet) {
                 trans.audit().log(s);
             }
         }
     }

     private int notify(AuthzTrans trans, NotifyBody nb) {
         List<String> toList = new ArrayList<>();
         List<String> ccList = new ArrayList<>();
         List<String> idList = new ArrayList<>();

         String run = nb.type()+nb.name();
         String test = dryRun?run:null;
         
         ONE_EMAIL:
         for(String id : nb.users()) {
             toList.clear();
             ccList.clear();
             idList.clear();
             try {
                 List<Identity> identities = trans.org().getIDs(trans, id, nb.escalation());
                 if(identities.isEmpty()) {
                     trans.warn().printf("%s is invalid for this Organization. Skipping notification.",id);
                 } else {
                     Identity identity = null;
                     for(Identity ident : identities) {
                         if(identity==null) {
                             identity = ident;
                             toList.add(ident.email());
                         } else {
                             ccList.add(ident.email());
                         }
                         idList.add(ident.fullID());
                     }
                     if(identity==null) { // Actually, identity can't be null here, because
                         break;           // if(identities.isEmpty() {..} else {... <here>
                     }                    // So this is here to avoid Sonar false positive only
                     StringBuilder content = new StringBuilder();
                     content.append(String.format(header,version,Identity.mixedCase(identity.firstName())));

                     nb.body(trans, content, indent, this, id);
                     content.append(footer);

                     if(mailer.sendEmail(trans, test, toList, ccList, nb.subject(),content.toString(), urgent)) {
                        nb.record(trans,cbl.inc(), id, idList, lastN);
                        nb.inc();
                     } else {
                         trans.error().log("Mailer failed to send Mail");
                     }
                     if(maxEmails>0 && nb.count()>=maxEmails) {
                         break ONE_EMAIL;
                     }
                 }
             } catch (OrganizationException e) {
                 trans.error().log(e);
             }
         }
         cbl.flush();
         return nb.count();
     }

    /* (non-Javadoc)
     * @see org.onap.aaf.auth.batch.Batch#_close(org.onap.aaf.auth.env.AuthzTrans)
     */
    @Override
    protected void _close(AuthzTrans trans) {
        cbl.flush();
    }

 }
