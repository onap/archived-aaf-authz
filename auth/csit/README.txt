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

1) Start in the directory you put your ONAP source in
  cd <root onap source dir>
2) If not exist, create a "workspace" directory. 
  mkdir -p workspace
3) Create an empty common functions script
  > workspace/common_functions.sh
4) cd to the plans
  cd csit/plans/aaf/aafapi
5) Run setup with variables set to the Workspace you created
WORKSPACE=/workspace; SCRIPTS=$WORKSPACE; export WORKSPACE SCRIPTS; bash setup.sh

6) To practice the Shutdown, do:
WORKSPACE=/workspace; SCRIPTS=$WORKSPACE; export WORKSPACE SCRIPTS; bash teardown.sh
  

