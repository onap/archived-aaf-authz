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

package org.onap.aaf.auth.batch.helpers;

public class CQLBatchLoop {
	
	private final CQLBatch cqlBatch;
	private final int maxBatch;
	private final StringBuilder sb;
	private final boolean dryRun;
	private int i;
	private int count;
	private int batches;
	
	public CQLBatchLoop(CQLBatch cb, int max, boolean dryRun) {
		cqlBatch = cb;
		i=0;
		count = 0;
		maxBatch = max;
		sb = cqlBatch.begin();
		this.dryRun = dryRun;
	}

	/**
	 * Put at the first part of your Loop Logic... It checks if you have enough lines to
	 * push a batch.
	 */
	public void preLoop() {
		if(i<0) {
			cqlBatch.begin();
		} else if(i>=maxBatch || sb.length()>24000) {
			cqlBatch.execute(dryRun);
			cqlBatch.begin();
			i=0;
			++batches;
		}
	}
	
	/**
	 * Assume this is another line in the Batch
	 * @return
	 */
	public StringBuilder inc() {
		++i;
		++count;
		return sb;
	}
	
	/**
	 * Close up when done.  However, can go back to "preLoop" safely.
	 */
	public void flush() {
		if(i>0) {
			cqlBatch.execute(dryRun);
			++batches;
		}
		i=-1;
	}

	public int total() {
		return count;
	}
	
	public int batches() {
		return batches;
	}

	public void reset() {
		count = 0;
		batches = 0;
		i = -1;
	}
}
