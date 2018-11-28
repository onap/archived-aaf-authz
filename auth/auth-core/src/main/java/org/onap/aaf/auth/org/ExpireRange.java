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

package org.onap.aaf.auth.org;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onap.aaf.cadi.Access;

public class ExpireRange {
	private static final String AAF_BATCH_RANGE = "aaf_batch_range.";
	public Map<String,List<Range>> ranges;
	public final Date now;
	
	public ExpireRange(final Access access) {
		now = new Date();
		ranges = new HashMap<>();
		int i=0;
		String prop = access.getProperty(AAF_BATCH_RANGE + i,null);
		if(prop==null) {
			if(i==0) {
				List<Range> lcred = getRangeList("cred");
				List<Range> lur = getRangeList("ur");
				List<Range> lx509 = getRangeList("x509");
				
				Range del = new Range("Delete",0,0,-1,0,GregorianCalendar.WEEK_OF_MONTH,-2);
				lur.add(del);
				lcred.add(del);
				lx509.add(del);
				
				lcred.add(new Range("CredOneWeek",3,1,0,0,GregorianCalendar.WEEK_OF_MONTH,1));
				lcred.add(new Range("CredTwoWeek",2,1,GregorianCalendar.WEEK_OF_MONTH,1,GregorianCalendar.WEEK_OF_MONTH,2));
				lcred.add(new Range("OneMonth",1,7,GregorianCalendar.WEEK_OF_MONTH,2,GregorianCalendar.MONTH,1));
				lcred.add(new Range("TwoMonth",1,0,GregorianCalendar.MONTH,1,GregorianCalendar.MONTH,2));
				
				lur.add(new Range("OneMonth",1,7,GregorianCalendar.WEEK_OF_MONTH,2,GregorianCalendar.MONTH,1));
				
				lx509.add(new Range("OneMonth",1,7,GregorianCalendar.WEEK_OF_MONTH,2,GregorianCalendar.MONTH,1));
			}
		}
	}
	
	public Set<String> names() {
		Set<String> names = new HashSet<>();
        for(List<Range> lr : ranges.values()) {
        	for(Range r : lr) {
        		names.add(r.name);
        	}
        }

		return names;
	}
	
	private synchronized List<Range> getRangeList(final String key) {
		List<Range> rv = ranges.get(key);
		if(rv==null) {
			rv = new ArrayList<>();
			ranges.put(key, rv);
		}
		return rv;
	}
	
	public class Range {
		private final String name;
		private final int reportingLevel;
		private final int interval; // in Days
		private final Date start;
		private final Date end;
		
		public Range(
				final String name, final int reportingLevel, final int interval,  
				final int startGCType, final int startQty,  
				final int endGCType,final int endQty) {
			this.name = name;
			this.reportingLevel = reportingLevel;
			this.interval = interval;
			GregorianCalendar gc = new GregorianCalendar();
			if(startGCType<0) {
				gc.set(GregorianCalendar.YEAR, 1);
			} else {
				gc.setTime(now);
				gc.add(startGCType, startQty);
			}
			start = gc.getTime();
			
			if(endGCType<0) {
				gc.set(GregorianCalendar.YEAR, 1);
			} else {
				gc.setTime(now);
				gc.add(endGCType, endQty);
			}
			end = gc.getTime();
		}
		
		public String name() {
			return name;
		}
		
		public int reportingLevel() {
			return reportingLevel;
		}

		public Date getStart() {
			return start;
		}
		
		public Date getEnd() {
			return end;
		}
		
		private boolean inRange(final Date date) {
			if(date==null) {
				return false;
			} else {
				return date.getTime()>=start.getTime() && date.before(end);
			}
		}

		public boolean shouldContact(final Date lastContact) {
			if(reportingLevel<=0) {
				return false;
			} else if(lastContact==null) {
				return true;
			} else if(interval==0) {
				return lastContact.before(start);
			} else {
				GregorianCalendar gc = new GregorianCalendar();
				gc.setTime(now);
				gc.add(GregorianCalendar.DAY_OF_WEEK, interval);
				return lastContact.before(gc.getTime());
			}
		}
	}

	public Range getRange(final String key, final Date date) {
		Range rv = null;
		if(date!=null) {
			List<Range> lr = ranges.get(key);
			if(lr==null) {
				return null;
			} else {
				for(Range r : lr) {
					if(r.inRange(date)) {
						rv = r;
						break;
					}
				}
			}
		}
		return rv;
	}
	

}
