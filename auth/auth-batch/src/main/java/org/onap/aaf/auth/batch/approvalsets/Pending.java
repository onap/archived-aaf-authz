/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
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
package org.onap.aaf.auth.batch.approvalsets;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.onap.aaf.cadi.util.CSV.Writer;
import org.onap.aaf.misc.env.util.Chrono;

public class Pending {
    public static final String REMIND = "remind";

    int qty;
    boolean hasNew;
    Date earliest;

    /**
     * Use this constructor to indicate when last Notified
     * @param lastNotified
     */
    public Pending(Date lastNotified) {
        qty = 1;
        hasNew = lastNotified==null;
        earliest = lastNotified;
    }

    /**
     * Create from CSV Row
     * @param row
     * @throws ParseException
     */
    public Pending(List<String> row) throws ParseException {
        hasNew = Boolean.parseBoolean(row.get(2));
        String d = row.get(3);
        if(d==null || d.isEmpty()) {
            earliest = null;
        } else {
            earliest = Chrono.dateOnlyFmt.parse(d);
        }
        qty = Integer.parseInt(row.get(4));
    }

    /**
     *  Write CSV Row
     * @param approveCW
     * @param key
     */
    public void row(Writer approveCW, String key) {
        approveCW.row(REMIND,key,hasNew,Chrono.dateOnlyStamp(earliest),qty);
    }

    public void inc() {
        ++qty;
    }

    public void inc(Pending value) {
        qty += value.qty;
        if(earliest == null) {
            earliest = value.earliest;
        } else if(value.earliest != null && value.earliest.before(earliest)) {
            earliest = value.earliest;
        }
    }

    public void earliest(Date lastnotified) {
        if(lastnotified == null) {
            hasNew=true;
        } else if (earliest == null || lastnotified.before(earliest)) {
            earliest = lastnotified;
        }
    }

    public int qty() {
        return qty;
    }

    public Date earliest() {
        return earliest;
    }

    public boolean newApprovals() {
        return hasNew;
    }

    public static Pending create() {
        return new Pending((Date)null);
    }

}