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

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

/**
 * @author Jonathan
 * 
 *         Catch \n and indent according to current indent levels of JavaGen
 */
public class IndentPrintWriter extends PrintWriter {
	public static final int INDENT_MULTIPLIER = 2;
	private boolean addIndent;
	private int indent;
	private int col;

	public IndentPrintWriter(Writer out) {
		super(out);
		addIndent = false;
		indent = col = 0;
	}
	
	public IndentPrintWriter(OutputStream out) {
		super(out);
		addIndent = false;
		indent = col = 0;
	}


    @Override
    public void write(String str) {
    	int len = str.length();
		for(int i=0;i<len;++i) {
			write((int)str.charAt(i));
		}
    }
    
    @Override
    public void println() {
    	write((int)'\n');
    }
	@Override
  public void write(String str, int off, int len)  {
		int finalLength = Math.min(str.length(),off+len);
		for(int i=off;i<finalLength;++i) {
			write((int)str.charAt(i));
		}
	}
	@Override
  public void write(int b) {
		if (b == '\n') {
			addIndent = true;
			col = 0;
		} else if (addIndent) {
			addIndent = false;
			toIndent();
		} else {
			++col;
		}
		super.write(b);
	}

	@Override
	public void write(char[] buf, int off, int len) {
		for (int i = 0; i < len; ++i)
			write(buf[i] + off);
	}

	public void setIndent(int size) {
		indent = size;
	}

	public void inc() {
		++indent;
	}
	
	public void dec() {
		--indent;
	}

	public void toCol(int idx) {
		while(idx>col++) {
		    super.write((int)' ');
    }
	}

	public int getIndent() {
		return indent;
	}

	public void toIndent() {
		int end = indent * INDENT_MULTIPLIER;
		for (int i = 0; i < end; ++i) {
			super.write((int) ' ');
		}
		col = end;
	}
}
