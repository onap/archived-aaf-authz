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

package org.onap.aaf.auth.dao.cached;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.onap.aaf.auth.dao.Cached.Getter;
import org.onap.aaf.auth.dao.cass.CredDAO;
import org.onap.aaf.auth.dao.cass.CredDAO.Data;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.Access.Level;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Hash;
import org.onap.aaf.cadi.PropAccess;
import org.onap.aaf.cadi.util.CSV;

public class FileGetter {
	private static final String AAF_FILEGETTER = "aaf_filegetter";
	public static boolean isLoaded = false;
	private static FileGetter singleton;

	private Map<String,List<CredDAO.Data>> data; 
	private SimpleDateFormat sdf;
	private FileGetter(Access access) {
		if(access!=null) {
			String filename = access.getProperty(AAF_FILEGETTER,null);
			if((filename!=null)&&(!isLoaded)) { 
					data = new TreeMap<>();
					sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSSS");
					CSV csv = new CSV(access, filename).setDelimiter('|');
					try {
						access.log(Level.INIT, "Loading Filebased Cred from",filename);
			    		csv.visit(row -> {
			    			if(row.size()<1) {
			    				access.log(Level.INIT, "Bad Row");
			    			}
			    			int type;
			    			  try {
			    				  type =Integer.parseInt(row.get(1));
			    			  } catch(Exception e) {
			    				  access.log(Level.INIT, e, "skipping ", row.get(0));
			    				  return;
			    			  }
			    			if(CredDAO.CERT_SHA256_RSA == type) {
			    				return;
			    			}
			    			CredDAO.Data cdd = new CredDAO.Data();
			    			cdd.id=row.get(0);
			    			cdd.type = type;
			    			try {
								cdd.expires = sdf.parse(row.get(2));
								cdd.cred = ByteBuffer.wrap(Hash.fromHex(row.get(3)));
								cdd.notes= row.get(4);
								cdd.ns = row.get(5);
								cdd.other = Integer.parseInt(row.get(6));
								if(row.size()>8) {
									cdd.tag = row.get(8);
								} else {
									cdd.tag = "";
								}
								List<CredDAO.Data> lcdd = data.get(cdd.id);
								if(lcdd == null) {
									lcdd = new ArrayList<>();
									data.put(cdd.id, lcdd);
								}
								lcdd.add(cdd);
								
							} catch (ParseException e) {
								access.log(Level.INIT, e);
							}
			    			
			    		});
			    		access.printf(Level.INIT, "Filebased Cred finished...");
						isLoaded = true;
					} catch( CadiException | IOException e) {
						access.log(Level.ERROR, e);
					}
				
			}
		}
	}

	public static synchronized FileGetter singleton(Access access) {
		if(singleton==null) {
			singleton = new FileGetter(access);
		}
		return singleton;
		
	}
	public Getter<CredDAO.Data> getter(String id) {
		return new FGetter(id);
	}
	private static List<CredDAO.Data> EMPTY = new ArrayList<>(); 
	public class FGetter implements Getter<CredDAO.Data> {
		private final List<CredDAO.Data> lcdd; 	
		public FGetter(final String id) {
			lcdd = data.get(id);
		}
		@Override
		public Result<List<Data>> get() {
			return Result.ok(lcdd==null?EMPTY:lcdd);
		}
	}
	
	public static void main(String[] args) {
		PropAccess access = new PropAccess(args);
		access.setProperty(AAF_FILEGETTER,"/Users/jg1555/cred.dat");
		FileGetter fg = FileGetter.singleton(access);
		
		for(String id : new String[] {"m01891@aaf.att.com","bogus"}) {
			Getter<CredDAO.Data> g = fg.getter(id);
			Result<List<CredDAO.Data>> r = g.get();
			if(r.isOKhasData()) {
				for(CredDAO.Data cdd : r.value) {
					System.out.println(cdd);
				}
			}
		}
	}
}

