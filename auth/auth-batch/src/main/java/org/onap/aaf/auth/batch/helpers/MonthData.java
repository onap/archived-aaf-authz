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

package org.onap.aaf.auth.batch.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.onap.aaf.misc.env.util.Split;

import java.util.Set;
import java.util.TreeMap;

public class MonthData {
    public final Map<Integer,Set<Row>> data = new TreeMap<>();
    private File f;
    
    public MonthData(String env) throws IOException {
        f = new File("Monthly"+env+".dat");
        
        if (f.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(f));
            try {
                String line;
                String[] split;
                while ((line=br.readLine())!=null) {
                    if (!line.startsWith("#")) {
                        split = Split.split(',', line);
                        if (split.length==5) {
                            add(Integer.parseInt(split[0]),split[1],
                                Integer.parseInt(split[2]),
                                Integer.parseInt(split[3]),
                                Integer.parseInt(split[4])
                            );
                        }
                    }
                }
            } finally {
                br.close();
            }
        }
    }
    
    public void add(int yrMon, String target, long total, long adds, long drops) {
        Set<Row> row = data.get(yrMon);
        if (row==null) {
            row=new HashSet<>();
            data.put(yrMon, row);
        }
        row.add(new Row(target,total,adds,drops));
    }
    
    public boolean notExists(int yrMon) {
        return data.get(yrMon)==null;
    }
    
     public static class Row implements Comparable<Row> {
        public final String target;
        public final long total;
        public final long adds;
        public final long drops;
        
        public Row(String t, long it, long a, long d) {
            target = t;
            total = it;
            adds = a;
            drops = d;
        }

        @Override
        public int compareTo(Row o) {
            return target.compareTo(o.target);
        }
        
        public String toString() {
            return target + '|' + total + '|' + drops + '|' + adds;
        }
    }

    public void write() throws IOException {
        if (f.exists()) {
            File bu = new File(f.getName()+".bak");
            f.renameTo(bu);
        }
        PrintStream ps = new PrintStream(f);
        try {
            for ( Entry<Integer, Set<Row>> rows : data.entrySet()) {
                for (Row row : rows.getValue()) {
                    ps.printf("%d,%s,%d,%d,%d\n",rows.getKey(),row.target,row.total,row.adds,row.drops);
                }
            }
        } finally {
            ps.close();
        }
    }

}
