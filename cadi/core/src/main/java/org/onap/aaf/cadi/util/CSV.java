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

package org.onap.aaf.cadi.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.onap.aaf.cadi.CadiException;

/**
 * Read CSV file for various purposes
 * 
 * @author Instrumental(Jonathan)
 *
 */
public class CSV {
	private File csv;
	
	public CSV(File file) {
		csv = file;
	}
	
	public CSV(String csvFilename) {
		csv = new File(csvFilename);
	}
	

	/**
	 * Create your code to accept the List<String> row.
	 * 
	 * Your code may keep the List... CSV does not hold onto it.
	 * 
	 * @author Instrumental(Jonathan)
	 *
	 */
	public interface Visitor {
		void visit(List<String> row) throws IOException, CadiException;
	}
	
	public void visit(Visitor visitor) throws IOException, CadiException {
		BufferedReader br = new BufferedReader(new FileReader(csv));
		try {
			String line;
			StringBuilder sb = new StringBuilder();
			while((line = br.readLine())!=null) {
				line=line.trim();
				if(!line.startsWith("#") && line.length()>0) {
//					System.out.println(line);  uncomment to debug
					List<String> row = new ArrayList<>();
					boolean quotes=false;
					boolean escape=false;
					char c;
					for(int i=0;i<line.length();++i) {
						switch(c=line.charAt(i)) {
							case '"':
								if(quotes) {
									if(i<line.length()-1) { // may look ahead
										if('"' == line.charAt(i+1)) {
											sb.append(c);
											++i;
										} else {
											quotes = false;
										}
									} else {
										quotes=false;
									}
								} else {
									quotes=true;
								}
								break;
							case '\\':
								if(escape) {
									sb.append(c);
									escape = false;
								} else {
									escape = true;
								}
								break;
							case ',':
								if(quotes) {
									sb.append(c);
								} else {
									row.add(sb.toString());
									sb.setLength(0);
								}
								break;
							default:
								sb.append(c);
						}
					}
					if(sb.length()>0) {
						row.add(sb.toString());
						sb.setLength(0);
					}
					visitor.visit(row);
				}
			}
		} finally {
			br.close();
		}
	}
	
	public Writer writer() throws FileNotFoundException {
		return new Writer(false);
	}

	public Writer writer(boolean append) throws FileNotFoundException {
		return new Writer(append);
	}

	public class Writer {
		private PrintStream ps;
		private Writer(final boolean append) throws FileNotFoundException {
			ps = new PrintStream(new FileOutputStream(csv,append));
		}
		public void row(Object ... objs) {
			if(objs.length>0) {
				boolean first = true;
				for(Object o : objs) {
					if(first) {
						first = false;
					} else {
						ps.append(',');
					}
					if(o instanceof String[]) {
						for(String str : (String[])o) {
							print(str);
						}
					} else {
						print(o.toString());
					}
				}
				ps.println();
			}
		}
		
		private void print(String s) {
			boolean quote = s.matches(".*[,|\"].*");
			if(quote) {
				ps.append('"');
				ps.print(s.replace("\"", "\"\"")
						  .replace("'", "''")
						  .replace("\\", "\\\\"));
				ps.append('"');
			} else {
				ps.append(s);
			}

			
		}
		/**
		 * Note: CSV files do not actually support Comments as a standard, but it is useful
		 * @param comment
		 */
		public void comment(String comment) {
			ps.print("# ");
			ps.println(comment);
		}
		
		public void flush() {
			ps.flush();
		}
		
		public void close() {
			ps.close();
		}
		
		public String toString() {
			return csv.getAbsolutePath();
		}
	}

	public void delete() {
		csv.delete();
	}
	
	public String toString() {
		return csv.getAbsolutePath();
	}

}
