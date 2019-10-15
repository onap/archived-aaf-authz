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

package org.onap.aaf.auth.cmd;

import java.util.List;

import org.onap.aaf.cadi.CadiException;
import org.onap.aaf.cadi.LocatorException;
import org.onap.aaf.misc.env.APIException;

public class Help extends Cmd {
    private List<Cmd> cmds;

    public Help(AAFcli aafcli, List<Cmd> cmds) {
        super(aafcli, "help", 
            new Param("-d (more details)", false),
            new Param("command",false));
        this.cmds = cmds;
    }

    @Override
    public int _exec( int idxValue, final String ... args) throws CadiException, APIException, LocatorException {
            int idx = idxValue;
        boolean first = true;
        StringBuilder sb = new StringBuilder("AAF Command Line Tool");
        StringBuilder details;
        multiChar(sb, 21, '-',0);
        sb.append("\n  SingleLine Commands");
        multiChar(sb, 21, '-',2);
        sb.append("\n    force   - add to regular commands to override depency checks");
        sb.append("\n    details - add to role list or perm list commands for rich format");
        multiChar(sb, 48, '-',2);
        // if details !=null, then extra details are written to it.
        details = aafcli.isDetailed()?new StringBuilder():null;

        String comp = args.length>idx?args[idx++]:null;
        if ("help".equalsIgnoreCase(comp)) {
            build(sb,null);
            detailedHelp(4, sb);
            sb.append('\n');
        } else {
            for (Cmd c : cmds) {
                if (!(c instanceof DeprecatedCMD)) {
                    if (comp!=null) {
                        if (comp.equals(c.getName())) {
                            multiChar(sb,2,' ',0);
                            c.build(sb,details);
                        }
                    } else {
                        if (first) {
                            first=false;
                        } else {
                            multiChar(sb,80,'-',2);
                        }
                        multiChar(sb,2,' ',0);
                        c.build(sb,details);
                        if (details!=null) {
                            c.detailedHelp(4, sb);
                        }
                    }
                }
            }
        }
        pw().println(sb.toString());
        return 200 /*HttpStatus.OK_200*/;
    }

    @Override
    public void detailedHelp(int indentValue, StringBuilder sb) {
            int indent = indentValue;
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
        detailLine(sb,indent,"force               - force deletions that have relationships");
        detailLine(sb,indent,"details               - cause list commands (role, perm) to print rich format");
        detailLine(sb,indent,"                       - In GUI CmdLine, use HourGlass option (top right)");
        sb.append('\n');
        detailLine(sb,indent-4,"CmdLine Arguments: change behavior of the aafcli program");
        detailLine(sb,indent,"-i - Read commands from Shell Standard Input");
        detailLine(sb,indent,"-f - Read commands from a file");
        detailLine(sb,indent,"-r - Clear Command Line SSO credential");
        detailLine(sb,indent,"-a - In test mode, do not stop execution on unexpected error");
        detailLine(sb,indent,"-t - Test Mode will not print variable fields that could break tc runs");
        detailLine(sb,indent+6,"such as expiration dates of a credential");
        detailLine(sb,indent,"-s - Request specific Start Date (not immediately)");
        detailLine(sb,indent+6,"Format YYYY-MM-DD.  Can also be set with \"set " + Cmd.STARTDATE + "=<value>\"");
        detailLine(sb,indent,"-e - Set Expiration/End Date, where commands support");
        detailLine(sb,indent+6,"Format YYYY-MM-DD.  Can also be set with \"set " + Cmd.ENDDATE + "=<value>\"");
    }
}
