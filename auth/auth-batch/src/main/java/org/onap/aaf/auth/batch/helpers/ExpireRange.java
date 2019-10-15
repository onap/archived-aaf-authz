/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright © 2018 IBM.
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

package org.onap.aaf.auth.batch.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onap.aaf.auth.org.Organization.Identity;
import org.onap.aaf.cadi.Access;

public class ExpireRange {
    private static final String DELETE = "Delete";
    public static final String ONE_MONTH = "OneMonth";
    public static final String TWO_MONTH = "TwoMonth";
    public static final String TWO_WEEK = "TwoWeek";
    public static final String ONE_WEEK = "OneWeek";
    private static final String AAF_BATCH_RANGE = "aaf_batch_range.";
    public final Map<String,List<Range>> ranges;
    private final Map<Integer,Date> intervalDates;
    private static final Date now = new Date();
    public final Range approveDelete ;

    private Range delRange;

    public ExpireRange(final Access access) {
        ranges = new HashMap<>();
        intervalDates = new HashMap<>();
        int i=0;
        approveDelete = new Range(DELETE,0,0,0,-1,0,GregorianCalendar.DAY_OF_YEAR,-100);
        String prop = access.getProperty(AAF_BATCH_RANGE + i,null);
        if(prop==null && i==0) {
                List<Range> lcred = getRangeList("cred");
                List<Range> lur = getRangeList("ur");
                List<Range> lx509 = getRangeList("x509");


                /*
                   Range(Name, ReportingLevel, PeopleInterval, AppInterval, Start(Type,Qty) End(Type,Qty) )
                   Interval of -1 Means "only once"
                   Interval of 0 means none
                   Interval > 0 means only X number of Days.
                */
                delRange = new Range(DELETE,0,0,0,-1,0,GregorianCalendar.WEEK_OF_MONTH,-2);
                lur.add(delRange);
                lcred.add(delRange);
                lx509.add(delRange);

                lcred.add(new Range(ONE_WEEK ,3,-1,1,0,0,GregorianCalendar.WEEK_OF_MONTH,1));
                lcred.add(new Range(TWO_WEEK ,2,-1,-1,GregorianCalendar.WEEK_OF_MONTH,1,GregorianCalendar.WEEK_OF_MONTH,2));
                lcred.add(new Range(ONE_MONTH,1,7,7,GregorianCalendar.WEEK_OF_MONTH,2,GregorianCalendar.MONTH,1));
                lcred.add(new Range(TWO_MONTH,1,-1,-1,GregorianCalendar.MONTH,1,GregorianCalendar.MONTH,2));

                lur.add(  new Range(ONE_MONTH,1,-1,-1,0,0,GregorianCalendar.MONTH,1));
                // Comment out until we can get some more clear actions in place for GUI
                // lur.add(  new Range(ONE_MONTH,1,-1,-1,GregorianCalendar.WEEK_OF_MONTH,2,GregorianCalendar.MONTH,1));
                // lur.add(  new Range(ONE_WEEK,2,-1,1,0,0,GregorianCalendar.WEEK_OF_MONTH,1));

                lx509.add(new Range(ONE_MONTH,1,-1,-1,GregorianCalendar.WEEK_OF_MONTH,2,GregorianCalendar.MONTH,1));
            }
    }

    public Range newFutureRange() {
        return new Range("Approval",7,7,1,0,0,GregorianCalendar.MONTH,1);
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
        private final int peopleInterval; // in Days
        private final int appInterval; // in Days
        private final Date start;
        private final Date end;
        private final Date lowerValid;

        public Range(
                final String name, final int reportingLevel,
                final int peopleInterval, final int appInterval,
                final int startGCType, final int startQty,
                final int endGCType,final int endQty) {
            this.name = name;
            this.reportingLevel = reportingLevel;
            this.peopleInterval = peopleInterval;
            this.appInterval = appInterval;
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


            if(endGCType<0) {
                gc.set(GregorianCalendar.YEAR, -1);
            } else {
                gc.setTime(now);
                gc.add(endGCType, endQty * -1);
            }
            lowerValid = gc.getTime();

        }

        public String name() {
            return name;
        }

        public int reportingLevel() {
            return reportingLevel;
        }

        public boolean needsContact(Date lnd, Identity identity) {
            final int interval;
            if(identity==null || identity.isPerson()) {
                interval = peopleInterval;
            } else {
                interval = appInterval;
            }
            if(interval == 0) {
                return false;
            } else if(interval < 0) { // "-1 = only once "
                return (lnd==null || lnd.before(lowerValid));
            } else {
                Date rv = intervalDates.get(interval);
                if(rv==null) {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(now);
                    gc.add(GregorianCalendar.DAY_OF_YEAR, -1*interval);
                    rv = gc.getTime();
                    intervalDates.put(interval, rv);
                }
                return rv.after(lnd);
            }
        }

        public Date getStart() {
            return start;
        }

        public Date getEnd() {
            return end;
        }

        public boolean inRange(final Date date) {
            if(date==null) {
                return false;
            } else {
                return date.getTime()>=start.getTime() && date.before(end);
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

    public Date now() {
        return now;
    }


}
