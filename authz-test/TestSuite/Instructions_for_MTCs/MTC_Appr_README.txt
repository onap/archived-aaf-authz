#-------------------------------------------------------------------------------
# ============LICENSE_START====================================================
# * org.onap.aaf
# * ===========================================================================
# * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
# * ===========================================================================
# * Licensed under the Apache License, Version 2.0 (the "License");
# * you may not use this file except in compliance with the License.
# * You may obtain a copy of the License at
# * 
#  *      http://www.apache.org/licenses/LICENSE-2.0
# * 
#  * Unless required by applicable law or agreed to in writing, software
# * distributed under the License is distributed on an "AS IS" BASIS,
# * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# * See the License for the specific language governing permissions and
# * limitations under the License.
# * ============LICENSE_END====================================================
# *
# * ECOMP is a trademark and service mark of AT&T Intellectual Property.
# *
#-------------------------------------------------------------------------------
NOTE: You may find slight differences between this readme doc and your actual output in places such as <YOUR_ATTUID>, times, or other such fields that vary for each run.

Do NOT replace anything inside square brackets such as [user.name] Some commands listed here use this notation, but they are set up to work by just copying & pasting the entire command.

run command:		sh ./tc MTC_Appr1
you should see:		MTC_Appr1
					SUCCESS! [MTC_Appr1.2014-11-03_11-26-26]


open a broswer and goto the gui for the machine you're on. For example, this is the home page on test machine zltv1492: 
https://zltv1492.vci.att.com:8085/gui/home 

click on My Approvals

click the submit button at the bottom of the form with no approve or deny buttons selected

you should see:     No Approvals have been sent. Try again

click "Try again" link

you should see:     The Approval Request page

NOTE: a radio button is a (filled or unfilled) circle under approve or deny
click the select all link for approve

you should see:     all radio buttons under approve should be selected

click the select all link for deny

you should see:     all radio buttons under deny should be selected

click the reset button at the bottom of the form

you should see:     NO radio buttons should be selected

Try to select both approve and deny for a single entry

you should:         not be able to

approve or deny entries as you like, then click submit

after you have submitted all approvals, go back to My Approvals page

you should see:     No Approvals to process at this time

in your command line,
run command:		aafcli ns list name com.test.appr.@[user.name].myProject

NOTE: what you see here will depend on which entries you approved and denied. Included are 2 examples of what you can see:

1) If you approve everything

List Namespaces by Name[com.test.appr.<YOUR_ATTUID>.myProject]
--------------------------------------------------------------------------------
com.test.appr.<YOUR_ATTUID>.myProject
    Administrators
        <YOUR_ATTUID>@csp.att.com                                                      
    Responsible Parties
        <YOUR_ATTUID>@csp.att.com                                                      


2) If you deny everything

List Namespaces by Name[com.test.appr.<YOUR_ATTUID>.myProject]
--------------------------------------------------------------------------------


run command:		sh ./tc MTC_Appr2 dryrun
you should see:     a lot of output. It's fine if you see errors for this command.

run command:        aafcli ns list name com.test.appr
you should see:     List Namespaces by Name[com.test.appr]
--------------------------------------------------------------------------------


run command:        aafcli ns list name com.test.appr.@[user.name]
you should see:     List Namespaces by Name[com.test.appr.<YOUR_ATTUID>]
--------------------------------------------------------------------------------

