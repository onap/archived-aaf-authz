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
 * * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * *
 ******************************************************************************/
package com.att.cmd;

import java.util.List;

import com.att.aft.dme2.internal.jetty.http.HttpStatus;
import com.att.cadi.CadiException;
import com.att.cadi.LocatorException;
import com.att.inno.env.APIException;

public class Help extends Cmd {
	private List<Cmd> cmds;

	public Help(AAFcli aafcli, List<Cmd> cmds) {
		super(aafcli, "--help", 
			new Param("-d (more details)", false),
			new Param("command",false));
		this.cmds = cmds;
	}

	@Override
	public int _exec( int _idx, final String ... args) throws CadiException, APIException, LocatorException {
	        int idx = _idx;
		boolean first = true;
		StringBuilder sb = new StringBuilder("AAF Command Line Tool");
		StringBuilder details;
		if(aafcli.isDetailed() ){
			multiChar(sb, 21, '-',0);
			details=new StringBuilder();// use for temporary writing of details
		} else {
			multiChar(sb, 21, '-',0);
			details = null;
		}
		String comp = args.length>idx?args[idx++]:null;
		if("help".equalsIgnoreCase(comp)) {
			build(sb,null);
			detailedHelp(4, sb);
			sb.append('\n');
		} else {
		    for(Cmd c : cmds) {
		    	if(comp!=null) {
		    		if(comp.equals(c.getName())) {
		    			multiChar(sb,2,' ',0);
		    			c.build(sb,details);
		    		}
		    	} else {
		    		if(first) {
		    			first=false;
		    		} else {
		    			multiChar(sb,80,'-',2);
		    		}
		    		multiChar(sb,2,' ',0);
		    		c.build(sb,details);
		    		if(details!=null) {
		    			c.detailedHelp(4, sb);
//					multiChar(sb,80,'-',2);
		    		}
		    	}
		    }
		}
		pw().println(sb.toString());
		return HttpStatus.OK_200;
	}
	
	@Override
	public void detailedHelp(int _indent, StringBuilder sb) {
	        int indent = _indent;
		detailLine(sb,indent,"To print main help, enter \"aafcli\" or \"aafcli --help \"");
		detailLine(sb,indent,"To print narrow the help content, enter sub-entries after aafcli,");
		detailLine(sb,indent+2,"i.e. \"aafcli perm\"");
		detailLine(sb,indent,"To see version of AAF CLI, enter \"aafcli --version \"");
		sb.append('\n');
		detailLine(sb,indent,"State Commands: change variables or credentials between calls.");
		indent+=4;
		detailLine(sb,indent,"set <tag>=<value>   - Set any System Property to a new value");
		detailLine(sb,indent,"as <id:password>    - Change Credentials.  Password may be encrypted");
		detailLine(sb,indent,"expect <int> [int]* - In test mode, check for proper HTTP Status Codes");
		detailLine(sb,indent,"sleep <int>         - Wait for <int> seconds");
		sb.append('\n');
		detailLine(sb,indent-4,"CmdLine Arguments: change behavior of the aafcli program");
		detailLine(sb,indent,"-i - Read commands from Shell Standard Input");
		detailLine(sb,indent,"-f - Read commands from a file");
		detailLine(sb,indent,"-a - In test mode, do not stop execution on unexpected error");
		detailLine(sb,indent,"-t - Test Mode will not print variable fields that could break tc runs");
		detailLine(sb,indent+6,"such as expiration dates of a credential");
		detailLine(sb,indent,"-s - Request specific Start Date (not immediately)");
		detailLine(sb,indent+6,"Format YYYY-MM-DD.  Can also be set with \"set " + Cmd.STARTDATE + "=<value>\"");
		detailLine(sb,indent,"-e - Set Expiration/End Date, where commands support");
		detailLine(sb,indent+6,"Format YYYY-MM-DD.  Can also be set with \"set " + Cmd.ENDDATE + "=<value>\"");
	}
}
