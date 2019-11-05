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

package org.onap.aaf.misc.env.util;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Chrono {
    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

    public final static DateFormat dateFmt;
    public final static DateFormat dateOnlyFmt;
    public final static DateFormat niceDateFmt;
    public final static DateFormat utcFmt;
    public final static DateFormat niceUTCDateFmt;
    public final static DateFormat iso8601Fmt;
    public final static DateFormat batchFmt;
    // Give general access to XML DataType Factory, since it's pretty common
    public static final DatatypeFactory xmlDatatypeFactory;
    
    static {
        try {
            xmlDatatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
        dateOnlyFmt = new SimpleDateFormat("yyyy-MM-dd");
        niceDateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm zzz");
        dateFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        utcFmt =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        utcFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        niceUTCDateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm zzz");
        niceUTCDateFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        iso8601Fmt =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        batchFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss+SSSS");
        // Note: DB and thus Batch conversions should always be in UTC Time
        batchFmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    

    public static class Formatter8601 extends Formatter {

        @Override
        public String format(LogRecord r) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFmt.format(new Date(r.getMillis())));
            sb.append(' ');
            sb.append(r.getThreadID());
            sb.append(' ');
            sb.append(r.getLevel());
            sb.append(": ");
            sb.append(r.getMessage());
            sb.append('\n');
            return sb.toString();
        }
        
    }
    
    /**
     * timeStamp
     * 
     * Convenience method to setup an XML dateTime (XMLGregorianCalendar) with "now" 
     * @return
     */
    public static XMLGregorianCalendar timeStamp() {
        return xmlDatatypeFactory.newXMLGregorianCalendar(new GregorianCalendar());
    }

    /**
     * timestamp
     * 
     * Convenience method to setup an XML dateTime (XMLGregorianCalendar) with passed in Date 
     * @param date
     * @return
     */
    public static XMLGregorianCalendar timeStamp(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return xmlDatatypeFactory.newXMLGregorianCalendar(gc);
    }

    public static XMLGregorianCalendar timeStamp(GregorianCalendar gc) {
        return xmlDatatypeFactory.newXMLGregorianCalendar(gc);
    }

    public static String utcStamp() {
        return utcFmt.format(new Date());
    }

    public static String utcStamp(Date date) {
        if (date==null)return "";
        return utcFmt.format(date);
    }

    public static String utcStamp(GregorianCalendar gc) {
        if (gc==null)return "";
        return utcFmt.format(gc.getTime());
    }

    public static String utcStamp(XMLGregorianCalendar xgc) {
        if (xgc==null)return "";
        return utcFmt.format(xgc.toGregorianCalendar().getTime());
    }
    
    public static String niceUTCStamp() {
        return niceUTCDateFmt.format(new Date());
    }

    public static String niceUTCStamp(Date date) {
        if (date==null)return "";
        return niceUTCDateFmt.format(date);
    }

    public static String niceUTCStamp(GregorianCalendar gc) {
        if (gc==null)return "";
        return niceUTCDateFmt.format(gc.getTime());
    }

    public static String niceUTCStamp(XMLGregorianCalendar xgc) {
        if (xgc==null)return "";
        return niceUTCDateFmt.format(xgc.toGregorianCalendar().getTime());
    }

    public static String dateStamp() {
        return dateFmt.format(new Date());
    }

    public static String dateStamp(GregorianCalendar gc) {
        if (gc == null)return "";
        return dateFmt.format(gc.getTime());
    }

    public static String dateStamp(Date date) {
        if (date == null)return "";
        return dateFmt.format(date);
    }

    public static String dateStamp(XMLGregorianCalendar xgc) {
        if (xgc==null)return "";
        return dateFmt.format(xgc.toGregorianCalendar().getTime());
    }

    /**
     * JAXB compatible dataTime Stamp
     * 
     * Java 6 does not format Timezone with -05:00 format, and JAXB XML breaks without it.
     * 
     * @return
     */
    public static String dateTime() {
        return dateTime(new GregorianCalendar());
    }

    /**
     * JAXB compatible dataTime Stamp
     * 
     * Java 6 does not format Timezone with -05:00 format, and JAXB XML breaks without it.
     * 
     * @return
     */
    public static String dateTime(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        return dateTime(gc);
    }

    /**
     * JAXB compatible dataTime Stamp
     * 
     * Java 6 does not format Timezone with -05:00 format, and JAXB XML breaks without it.
     * 
     * @return
     */
    public static String dateTime(GregorianCalendar gc) {
        if (gc == null)return "";
        TimeZone tz = gc.getTimeZone();
        int tz1 = (tz.getRawOffset()+tz.getDSTSavings())/0x8CA0;
        int tz1abs = Math.abs(tz1);
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d%c%02d:%02d", 
                gc.get(GregorianCalendar.YEAR),
                gc.get(GregorianCalendar.MONTH)+1,
                gc.get(GregorianCalendar.DAY_OF_MONTH),
                gc.get(GregorianCalendar.HOUR),
                gc.get(GregorianCalendar.MINUTE),
                gc.get(GregorianCalendar.SECOND),
                gc.get(GregorianCalendar.MILLISECOND),
                tz1==tz1abs?'+':'-',
                tz1abs/100,
                ((tz1abs-(tz1abs/100)*100)*6)/10 // Get the "10s", then convert to mins (without losing int place)
                );
    }

    /**
     * JAXB compatible dataTime Stamp
     * 
     * Java 6 does not format Timezone with -05:00 format, and JAXB XML breaks without it.
     * 
     * @return
     */
    public static String dateTime(XMLGregorianCalendar xgc) {
        return xgc==null?"":dateTime(xgc.toGregorianCalendar());
    }

    public static String dateOnlyStamp() {
        return dateOnlyFmt.format(new Date());
    }

    public static String dateOnlyStamp(GregorianCalendar gc) {
        return gc == null?"":dateOnlyFmt.format(gc.getTime());
    }

    public static String dateOnlyStamp(Date date) {
        return date == null?"":dateOnlyFmt.format(date);
    }

    public static String dateOnlyStamp(XMLGregorianCalendar xgc) {
        return xgc==null?"":dateOnlyFmt.format(xgc.toGregorianCalendar().getTime());
    }

    public static String niceDateStamp() {
        return niceDateFmt.format(new Date());
    }

    public static String niceDateStamp(Date date) {
        return date==null?"":niceDateFmt.format(date);
    }

    public static String niceDateStamp(GregorianCalendar gc) {
        return gc==null?"":niceDateFmt.format(gc.getTime());
    }

    public static String niceDateStamp(XMLGregorianCalendar xgc) {
        return xgc==null?"":niceDateFmt.format(xgc.toGregorianCalendar().getTime());
    }


    //////////////////////  HELPFUL Strings
    public static final String BAD_DIR_CHARS_REGEX = "[/:\\;.]";
    public static final String SPLIT_DIR_REGEX = "/";

    public static long firstMomentOfDay(long utc) {
        GregorianCalendar begin = new GregorianCalendar();
        begin.setTimeInMillis(utc);
        return firstMomentOfDay(begin).getTimeInMillis();
    }    
    
    public static long lastMomentOfDay(long utc) {
        GregorianCalendar end = new GregorianCalendar();
        end.setTimeInMillis(utc);
        return lastMomentOfDay(end).getTimeInMillis();
    }

    public static GregorianCalendar firstMomentOfDay(GregorianCalendar begin) {
        if (begin==null)begin = new GregorianCalendar();
        begin.set(GregorianCalendar.HOUR, 0);
        begin.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
        begin.set(GregorianCalendar.MINUTE, 0);
        begin.set(GregorianCalendar.SECOND, 0);
        begin.set(GregorianCalendar.MILLISECOND, 0);
        return begin;
    }    

    public static GregorianCalendar lastMomentOfDay(GregorianCalendar end) {
        if (end==null)end = new GregorianCalendar();
        end.set(GregorianCalendar.HOUR, 11);
        end.set(GregorianCalendar.MINUTE, 59);
        end.set(GregorianCalendar.SECOND, 59);
        end.set(GregorianCalendar.MILLISECOND, 999);
        end.set(GregorianCalendar.AM_PM, GregorianCalendar.PM);
        return end;
    }

    // UUID needs to be converted from UUID Epoch
    public static final Date uuidToDate(UUID id) {
        return new Date((id.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH)/10000);
    }

    public static final long uuidToUnix(UUID id) {
        return (id.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH)/10000;
    }

    public static float millisFromNanos(long start, long end) {
        return (end - start) / 1000000f;
    }


    private static long sequence = new SecureRandom().nextInt();
    private static synchronized long sequence() {
        return ++sequence;
    }
    
    public static final UUID dateToUUID(Date origTime) {
        return dateToUUID(origTime.getTime());
    }
    
    public static final UUID dateToUUID(long origTime) {
    /*
     * From Cassandra : http://wiki.apache.org/cassandra/FAQ
      Magic number obtained from #cassandra's thobbs, who
      claims to have stolen it from a Python library.
    */
        long time = origTime * 10000 + NUM_100NS_INTERVALS_SINCE_UUID_EPOCH;
        long timeLow = time &       0xffffffffL;
        long timeMid = time &   0xffff00000000L;
        long timeHi = time & 0xfff000000000000L;
        long upperLong = (timeLow << 32) | (timeMid >> 16) | (1 << 12) | (timeHi >> 48) ;
        return new java.util.UUID(upperLong, (0xC000000000000000L | sequence()));
    }

}
