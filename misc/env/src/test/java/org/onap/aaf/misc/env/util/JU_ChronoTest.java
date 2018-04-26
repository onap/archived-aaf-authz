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
}
