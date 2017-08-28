/*******************************************************************************
 * Copyright (c) 2016 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.authz.gui;

import static com.att.xgen.html.HTMLGen.TABLE;
import static com.att.xgen.html.HTMLGen.TD;
import static com.att.xgen.html.HTMLGen.TR;

import java.io.IOException;
import java.util.ArrayList;

import com.att.authz.gui.table.AbsCell;
import org.onap.aaf.inno.env.APIException;
import org.onap.aaf.inno.env.Env;
import org.onap.aaf.inno.env.Slot;
import org.onap.aaf.inno.env.Trans;
import org.onap.aaf.inno.env.TransStore;
import com.att.xgen.Cache;
import com.att.xgen.DynamicCode;
import com.att.xgen.Mark;
import com.att.xgen.html.HTMLGen;
import com.att.xgen.html.State;

public class Table<S extends State<Env>, TRANS extends TransStore> extends NamedCode {
	private final Slot ROW_MSG_SLOT, EMPTY_TABLE_SLOT;
	private final String title;
	private final String[] columns;
	private final Rows rows;
	
	public Table(String title, TRANS trans, Data<S,TRANS> data, String ... attrs)  {
		super(true,attrs);
		ROW_MSG_SLOT=trans.slot("TABLE_ROW_MSG");
		EMPTY_TABLE_SLOT=trans.slot("TABLE_EMPTY");
		this.columns = data.headers();
		boolean alt = false;
		for(String s : attrs) {
			if("class=std".equals(s) || "class=stdform".equals(s)) {
				alt=true;
			}
		}
		rows = new Rows(data,alt?1:0);
		this.title = title;
		
		// Derive an ID from title (from no spaces, etc), and prepend to IDAttributes (Protected from NamedCode)
		idattrs = new String[attrs.length+1];
		idattrs[0] = title.replaceAll("\\s","");
		System.arraycopy(attrs, 0, idattrs, 1, attrs.length);
	}

	@Override
	public void code(Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
		Mark table = new Mark();
		Mark tr = new Mark();
		hgen.incr(table,TABLE)
				.leaf("caption", "class=title").text(title).end()
				.incr(tr,TR);
					for(String column : columns) {
						hgen.leaf("th").text(column).end();
					}
				hgen.end(tr);
				
		// Load Rows Dynamically
		cache.dynamic(hgen, rows);
		// End Table
		hgen.end(table); 
			
		// Print Message from Row Gathering, if available
		cache.dynamic(hgen, new DynamicCode<HTMLGen,S,TRANS>() {
			@Override
			public void code(S state, TRANS trans, Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
				String msg;
				if((msg = trans.get(EMPTY_TABLE_SLOT, null))!=null) {
					hgen.incr("style").text("#inner tr,caption,input,p.preamble {display: none;}#inner p.notfound {margin: 0px 0px 0px 20px}").end();
					hgen.incr(HTMLGen.P,"class=notfound").text(msg).end().br();
				} else if((msg=trans.get(ROW_MSG_SLOT,null))!=null) { 
					hgen.p(msg).br();
				}
			}
		});
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
		public Cells get(S state,TRANS trans);
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
		public void code(S state, TRANS trans, Cache<HTMLGen> cache, HTMLGen hgen) throws APIException, IOException {
			Mark tr = new Mark();
			Mark td = new Mark();
			
			int alt = this.alt;
			Cells cells = data.get(state, trans);
			if(cells.cells.length>0) {
				for(AbsCell[] row : cells.cells) {
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
					for(AbsCell cell :row) {
						hgen.leaf(td, TD,cell.attrs());
						cell.write(hgen);
						hgen.end(td);
					}
					hgen.end(tr);
				}
				// Pass Msg back to Table code, in order to place after Table Complete
				if(cells.msg!=null) {
					trans.put(ROW_MSG_SLOT,cells.msg);
				}

			} else {
				trans.put(EMPTY_TABLE_SLOT,cells.msg);
			}
		}
	}
}
