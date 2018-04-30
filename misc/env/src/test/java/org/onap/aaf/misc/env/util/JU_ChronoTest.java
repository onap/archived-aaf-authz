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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class JU_ChronoTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testFormatter8601() {
		Chrono.Formatter8601 formatter = new Chrono.Formatter8601();

		LogRecord record = new LogRecord(Level.WARNING, "Log Record to test log formating");

		Date date = new Date(118, 02, 02);
		long time = date.getTime();

		record.setMillis(time);

		String expectedString = Chrono.dateFmt.format(date) + " " + record.getThreadID() + " " + record.getLevel()
				+ ": " + record.getMessage() + "\n";
		assertEquals(expectedString, formatter.format(record));
	}

	@Test
	public void testTimeStampWithDate() {
		Date date = Calendar.getInstance().getTime();
		XMLGregorianCalendar timeStamp = Chrono.timeStamp(date);

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		XMLGregorianCalendar expectedCalendar = Chrono.xmlDatatypeFactory.newXMLGregorianCalendar(gc);

		assertEquals(expectedCalendar, timeStamp);
	}

	@Test
	public void testUTCStamp() {
		final Date date = Calendar.getInstance().getTime();
		String expectedUTCTime = Chrono.utcFmt.format(date);

		String stamp = Chrono.utcStamp(date);

		assertEquals(stamp, expectedUTCTime);

		Date date1 = null;
		assertEquals("", Chrono.utcStamp(date1));

		GregorianCalendar gc = null;
		assertEquals(Chrono.utcStamp(gc), "");
		gc = new GregorianCalendar();
		gc.setTime(date);
		assertEquals(Chrono.utcStamp(gc), expectedUTCTime);

		XMLGregorianCalendar xgc = null;
		assertEquals(Chrono.utcStamp(xgc), "");
		xgc = Chrono.timeStamp(gc);
		assertEquals(Chrono.utcStamp(xgc), expectedUTCTime);

	}

	@Test
	public void testDateStamp() {
		final Date date = Calendar.getInstance().getTime();
		String expectedUTCTime = Chrono.dateFmt.format(date);

		String stamp = Chrono.dateStamp(date);

		assertEquals(stamp, expectedUTCTime);

		Date date1 = null;
		assertEquals("", Chrono.dateStamp(date1));

		GregorianCalendar gc = null;
		assertEquals(Chrono.dateStamp(gc), "");
		gc = new GregorianCalendar();
		gc.setTime(date);
		assertEquals(Chrono.dateStamp(gc), expectedUTCTime);

		XMLGregorianCalendar xgc = null;
		assertEquals(Chrono.dateStamp(xgc), "");
		xgc = Chrono.timeStamp(gc);
		assertEquals(Chrono.dateStamp(xgc), expectedUTCTime);
	}

	@Test
	public void testDateTime() {
		final Date date = Calendar.getInstance().getTime();
		date.setTime(1525023883297L);

		String expectedDateTime = "2018-04-29T11:14:43.297+05:30";

		String stamp = Chrono.dateTime(date);

		assertEquals(stamp, expectedDateTime);

		GregorianCalendar gc = null;
		assertEquals(Chrono.dateTime(gc), "");
		gc = new GregorianCalendar();
		gc.setTime(date);
		assertEquals(Chrono.dateTime(gc), expectedDateTime);

		XMLGregorianCalendar xgc = null;
		assertEquals(Chrono.dateTime(xgc), "");
		xgc = Chrono.timeStamp(gc);
		assertEquals(Chrono.dateTime(xgc), expectedDateTime);
	}

	@Test
	public void testDateOnlyStamp() {
		final Date date = Calendar.getInstance().getTime();
		date.setTime(1525023883297L);

		String expectedDateTime = Chrono.dateOnlyFmt.format(date);

		String stamp = Chrono.dateOnlyStamp(date);

		assertEquals(stamp, expectedDateTime);

		Date date1 = null;
		assertEquals("", Chrono.dateOnlyStamp(date1));

		GregorianCalendar gc = null;
		assertEquals(Chrono.dateOnlyStamp(gc), "");
		gc = new GregorianCalendar();
		gc.setTime(date);
		assertEquals(Chrono.dateOnlyStamp(gc), expectedDateTime);

		XMLGregorianCalendar xgc = null;
		assertEquals(Chrono.dateOnlyStamp(xgc), "");
		xgc = Chrono.timeStamp(gc);
		assertEquals(Chrono.dateOnlyStamp(xgc), expectedDateTime);
	}

	@Test
	public void testNiceDateStamp() {
		final Date date = Calendar.getInstance().getTime();
		date.setTime(1525023883297L);

		String expectedDateTime = Chrono.niceDateFmt.format(date);

		String stamp = Chrono.niceDateStamp(date);

		assertEquals(stamp, expectedDateTime);

		Date date1 = null;
		assertEquals("", Chrono.niceDateStamp(date1));

		GregorianCalendar gc = null;
		assertEquals(Chrono.niceDateStamp(gc), "");
		gc = new GregorianCalendar();
		gc.setTime(date);
		assertEquals(Chrono.niceDateStamp(gc), expectedDateTime);

		XMLGregorianCalendar xgc = null;
		assertEquals(Chrono.niceDateStamp(xgc), "");
		xgc = Chrono.timeStamp(gc);
		assertEquals(Chrono.niceDateStamp(xgc), expectedDateTime);
	}

	@Test
	public void testMoment() {
		final Date date = Calendar.getInstance().getTime();
		date.setTime(1525023883297L);

		long firstMoment = 1524940200000L;
		long lastMoment = 1525026599999L;

		assertEquals(firstMoment, Chrono.firstMomentOfDay(date.getTime()));
		assertEquals(lastMoment, Chrono.lastMomentOfDay(date.getTime()));

		float timeInMillis = (lastMoment - firstMoment) / 1000000f;
		assertEquals(timeInMillis, Chrono.millisFromNanos(firstMoment, lastMoment), 0);

	}
}
