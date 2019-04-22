#########
##  ============LICENSE_START====================================================
##  org.onap.aaf
##  ===========================================================================
##  Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
##  ===========================================================================
##  Licensed under the Apache License, Version 2.0 (the "License");
##  you may not use this file except in compliance with the License.
##  You may obtain a copy of the License at
##
##       http://www.apache.org/licenses/LICENSE-2.0
##
##  Unless required by applicable law or agreed to in writing, software
##  distributed under the License is distributed on an "AS IS" BASIS,
##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##  See the License for the specific language governing permissions and
##  limitations under the License.
##  ============LICENSE_END====================================================
##

The CSIT functions are started by Jenkins, starting with the "setup.sh"
in the csit/plans/aaf/aafapi directory (where 'csit' is an ONAP Project)

You can emulate the JENKINS build locally
PRELIMINARY) Make sure authz/auth/docker/d.props.csit is the right version

1) Start in the directory you put your ONAP source in
  cd <root onap source dir>
2) If not exist, create a "workspace" directory. 
  mkdir -p workspace
3) cd to workspace
4) export WORKSPACE="${PWD}"
5) Create an empty common functions script
  > common_functions.sh
6) cd to the plans
  cd ../csit/plans/aaf/aafapi
7) Run setup with variables set to the Workspace you created
SCRIPTS=$WORKSPACE; export WORKSPACE SCRIPTS; bash setup.sh

8) To practice the Shutdown, do:
SCRIPTS=$WORKSPACE; export WORKSPACE SCRIPTS; bash teardown.sh
  
OTHER) If nexus isn't working (and you have the latest images to test with), you can 
   export SKIP_PULL=true

