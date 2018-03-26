/*******************************************************************************
 * ============LICENSE_START====================================================
 * * org.onap.aaf
 * * ===========================================================================
 * * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * * ===========================================================================
 * * Licensed under the Apache License, Version 2.0 (the "License");
 * * you may not use this file except in compliance with the License.
 * * You may obtain a copy of the License at
 * * 
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 * * 
 *  * Unless required by applicable law or agreed to in writing, software
 * * distributed under the License is distributed on an "AS IS" BASIS,
 * * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * * See the License for the specific language governing permissions and
 * * limitations under the License.
 * * ============LICENSE_END====================================================
 * *
 * *
 ******************************************************************************/
package org.onap.aaf.client.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.onap.aaf.cadi.Access;
import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.Symm;

// TODO: Ian [JUnit] JU_TestAccess isn't implementing all abstract methdods of Access
public class JU_TestAccess implements Access {
	private Symm symm;

	public JU_TestAccess() throws CadiException {
		symm = Symm.obtain(this);
	}
	
	public void log(Level level, Object... elements) {
		boolean first = true;
		for(int i=0;i<elements.length;++i) {
			if(first)first = false;
			else System.out.print(' ');
			System.out.print(elements[i].toString());
		}
		System.out.println();
	}

	public void log(Exception e, Object... elements) {
		e.printStackTrace();
		log(Level.ERROR,elements);
	}

	@Override
	public void printf(Level level, String fmt, Object... elements) {
		if(willLog(level)) {
			System.out.printf(fmt, elements);
		}
	}

	public void setLogLevel(Level level) {
		
	}

	public ClassLoader classLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	public String getProperty(String string, String def) {
		String rv = System.getProperty(string);
		return rv==null?def:rv;
	}

	public void load(InputStream is) throws IOException {
		
	}

	public String decrypt(String encrypted, boolean anytext) throws IOException {
		return (encrypted!=null && (anytext==true || encrypted.startsWith(Symm.ENC)))
			? symm.depass(encrypted)
			: encrypted;
	}

	/* (non-Javadoc)
	 * @see org.onap.aaf.cadi.Access#willLog(org.onap.aaf.cadi.Access.Level)
	 */
	@Override
	public boolean willLog(Level level) {
		return true;
	}

	@Override
	public Properties getProperties() {
		return System.getProperties();
	}

}
