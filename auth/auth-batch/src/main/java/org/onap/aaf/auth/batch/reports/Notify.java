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
 */package org.onap.aaf.auth.batch.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.onap.aaf.auth.batch.Batch;
import org.onap.aaf.auth.batch.reports.bodies.NotifyBody;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.org.Mailer;
import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.auth.org.OrganizationException;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.client.Holder;
import org.onap.aaf.cadi.util.CSV;
import org.onap.aaf.misc.env.APIException;

public class Notify extends Batch {
	private final Mailer mailer;
	private final String mailFrom;
	private final String header;
	private final String footer;
	private List<File> notifyFile;

	public Notify(AuthzTrans trans) throws APIException, IOException, OrganizationException {
		super(trans.env());
		String mailerCls = env.getProperty("MAILER");
		mailFrom = env.getProperty("MAIL_FROM");
		String header_html = env.getProperty("HEADER_HTML");
		String footer_html = env.getProperty("FOOTER_HTML");
		if(mailerCls==null || mailFrom==null || header_html==null || footer_html==null) {
			throw new APIException("Notify requires MAILER, MAILER_FROM, HEADER_HTML and FOOTER_HTML properties");
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
		BufferedReader br = new BufferedReader(new FileReader(header_html));
		try {
			while((line=br.readLine())!=null) {
				sb.append(line);
				sb.append('\n');
			}
			header = sb.toString();
		} finally {
			br.close();
		}
		
		br = new BufferedReader(new FileReader(footer_html));
		try {
			while((line=br.readLine())!=null) {
				sb.append(line);
				sb.append('\n');
			}
			footer = sb.toString();
		} finally {
			br.close();
		}
	
		// Class Load possible data
		NotifyBody.load(env.access());
		
        // Create Intermediate Output 
        File logDir = logDir();
        notifyFile = new ArrayList<>();
        if(args().length>0) {
        	for(int i=0;i<args().length;++i) {
        		notifyFile.add(new File(logDir, args()[i]));
        	}
        }
	}

	@Override
	protected void run(AuthzTrans trans) {
		List<String> toList = new ArrayList<>();
		List<String> ccList = new ArrayList<>();
		AuthzTrans noAvg = trans.env().newTransNoAvg();
		String subject = "Test Notify";
		boolean urgent = false;
		

		
		final Notify notify = this;
		final Holder<List<String>> info = new Holder<>(null);
		final Set<String> errorSet = new HashSet<>();
		
		try {
			for(File f : notifyFile) {
				CSV csv = new CSV(f);
				try {
					csv.visit(new CSV.Visitor() {
						@Override
						public void visit(List<String> row) throws IOException, CadiException {
							if("info".equals(row.get(0))) {
								info.set(row);
							}
							if(info.get()==null) {
								throw new CadiException("First line of Feed MUST contain 'info' record");
							}
							String key = row.get(0)+'|'+info.get().get(1);
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
				
				// now create Notification
				for(NotifyBody nb : NotifyBody.getAll()) {
					for(String id : nb.users()) {
						toList.clear();
						ccList.clear();
						try {
							String bodyS = nb.body(noAvg, notify, id);
							Identity identity = trans.org().getIdentity(noAvg, id);
							if(!identity.isPerson()) {
								identity = identity.responsibleTo();
							}
							for(int i=1;i<nb.escalation();++i) {
								if(identity != null) {
									if(i==1) {
										toList.add(identity.email());
									} else {
										identity=identity.responsibleTo();
										ccList.add(identity.email());
									}
								}
							}
								
							mailer.sendEmail(noAvg, dryRun, mailFrom, toList, ccList, subject, 
									String.format(header,"2.1.9",Identity.mixedCase(identity.firstName()))+
									bodyS +
									footer, urgent);
						} catch (OrganizationException e) {
							trans.error().log(e);
						}
					}
				}

			}
		} finally {
			for(String s : errorSet) {
				trans.audit().log(s);
			}
		}
	}
	
	@Override
	protected void _close(AuthzTrans trans) {
	}

}
