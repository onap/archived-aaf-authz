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

package org.onap.aaf.auth.gui;

import static org.onap.aaf.misc.xgen.html.HTMLGen.TABLE;
import static org.onap.aaf.misc.xgen.html.HTMLGen.TD;
import static org.onap.aaf.misc.xgen.html.HTMLGen.TR;

import java.io.IOException;
import java.util.ArrayList;

import org.onap.aaf.auth.gui.table.AbsCell;
import org.onap.aaf.misc.env.APIException;
import org.onap.aaf.misc.env.Env;
import org.onap.aaf.misc.env.Slot;
import org.onap.aaf.misc.env.Trans;
import org.onap.aaf.misc.env.TransStore;
import org.onap.aaf.misc.xgen.Cache;
import org.onap.aaf.misc.xgen.Code;
import org.onap.aaf.misc.xgen.DynamicCode;
import org.onap.aaf.misc.xgen.Mark;
import org.onap.aaf.misc.xgen.html.HTMLGen;
import org.onap.aaf.misc.xgen.html.State;

public class Table<S extends State<Env>, TRANS extends TransStore> extends NamedCode {
    private final Slot ROW_MSG_SLOT, EMPTY_TABLE_SLOT;
    private final String title;
    private final String[] columns;
    private final Rows rows;
    private Code<HTMLGen> other;


    public Table(String title, TRANS trans, Data<S,TRANS> data, Code<HTMLGen> other, String name, String ... attrs)  {
        this(title,trans,data,name, attrs);
        this.other = other;
    }

    public Table(String title, TRANS trans, Data<S,TRANS> data, String name, String ... attrs)  {
        super(true,name);
        for (String a : attrs) {
            addAttr(false, a);
        }
        ROW_MSG_SLOT=trans.slot("TABLE_ROW_MSG");
        EMPTY_TABLE_SLOT=trans.slot("TABLE_EMPTY");
        this.columns = data.headers();
        boolean alt = false;
        for (String s : attrs) {
            if ("class=std".equals(s) || "class=stdform".equals(s)) {
                alt=true;
            }
        }
        rows = new Rows(data,alt?1:0);
        this.title = title;
        // Derive an ID from title (from no spaces, etc), and prepend to IDAttributes (Protected from NamedCode)
        addAttr(true,title(trans).replaceAll("\\s",""));

        other = null;
    }

    @Override
    public void code(final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
        cache.dynamic(hgen, new DynamicCode<HTMLGen,S,TRANS>() {
            @Override
            public void code(S state, TRANS trans, Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
                rows.data.prefix(state, trans, cache, xgen);
            }
        });
        Mark table = new Mark();
        Mark tr = new Mark();

        hgen.incr(table,TABLE);
        if (title==null) {
            cache.dynamic(hgen, new DynamicCode<HTMLGen,S,TRANS>() {
                @Override
                public void code(S state, TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                    hgen.leaf("caption", "class=title").text(title(trans)).end();
                }
            });
        } else {
            hgen.leaf("caption", "class=title").text(title).end();
        }
        hgen.incr(tr,TR);
                for (String column : columns) {
                    hgen.leaf("th").text(column).end();
                }
            hgen.end(tr);

        // Load Rows Dynamically
        cache.dynamic(hgen, rows);
        // End Table
        hgen.end(table);

        if (other!=null) {
            other.code(cache,hgen);
        }

        // Print Message from Row Gathering, if available
        cache.dynamic(hgen, new DynamicCode<HTMLGen,S,TRANS>() {
            @Override
            public void code(S state, TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
                String msg;
                if ((msg = trans.get(EMPTY_TABLE_SLOT, null))!=null) {
                    hgen.incr("style").text("#inner tr,caption,input,p.preamble {display: none;}#inner p.notfound {margin: 0px 0px 0px 20px}").end();
                    hgen.incr(HTMLGen.P,"class=notfound").text(msg).end().br();
                } else if ((msg=trans.get(ROW_MSG_SLOT,null))!=null) {
                    hgen.p(msg).br();
                }
            }
        });
        cache.dynamic(hgen, new DynamicCode<HTMLGen,S,TRANS>() {
            @Override
            public void code(S state, TRANS trans, Cache<HTMLGen> cache, HTMLGen xgen) throws APIException, IOException {
                rows.data.postfix(state, trans, cache, xgen);
            }
        });

    }

    protected String title(TRANS trans) {
        return title;
    }

    public static class Cells {
        public static final Cells EMPTY = new Cells();
        private Cells() {
            cells = new AbsCell[0][0];
            msg = "No Data Found";
        }

        public Cells(ArrayList<AbsCell[]> arrayCells, String msg) {
            cells = new AbsCell[arrayCells.size()][];
            arrayCells.toArray(cells);
            this.msg = msg;
        }
        public AbsCell[][] cells;
        public String msg;

    }

    public interface Data<S extends State<Env>, TRANS extends Trans> {
        // Note: Trans is not first to avoid Method Name Collision
        public void prefix(S state, TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen);
        public Cells get(TRANS trans,S state);
        public void postfix(S state, TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen);
        public String[] headers();
    }

    private class Rows extends DynamicCode<HTMLGen,S,TRANS> {
        private Data<S,TRANS> data;
        private int alt;

        public Rows(Data<S,TRANS> data, int alt) {
            this.data = data;
            this.alt = alt;
        }

        @Override
        public void code(final S state, final TRANS trans, final Cache<HTMLGen> cache, final HTMLGen hgen) throws APIException, IOException {
            Mark tr = new Mark();
            Mark td = new Mark();

            int alt = this.alt;
            Cells cells = data.get(trans,state);
            if (cells.cells.length>0) {
                for (AbsCell[] row : cells.cells) {
                    if (row.length==0) {
                        hgen.text("</table>")
                            .hr()
                            .text("<table>");
                    } else {
                        switch(alt) {
                            case 1:
                                alt=2;
                            case 0:
                                hgen.incr(tr,TR);
                                break;
                            default:
                                alt=1;
                                hgen.incr(tr,TR,"class=alt");
                        }
                        for (AbsCell cell :row) {
                            hgen.leaf(td, TD,cell.attrs());
                            cell.write(hgen);
                            hgen.end(td);
                        }
                        hgen.end(tr);
                    }
                }
                // Pass Msg back to Table code, in order to place after Table Complete
                if (cells.msg!=null) {
                    trans.put(ROW_MSG_SLOT,cells.msg);
                }
            } else {
                trans.put(EMPTY_TABLE_SLOT,cells.msg);
            }
        }
    }

//    public Table<S,TRANS> setPrefix(DynamicCode<HTMLGen, AuthGUI, AuthzTrans> dynamicCode) {
//        prefix = dynamicCode;
//        return this;
//    }
//
//    public Table<S,TRANS> setPostfix(DynamicCode<HTMLGen, AuthGUI, AuthzTrans> dynamicCode) {
//        postfix = dynamicCode;
//        return this;
//    }

}
