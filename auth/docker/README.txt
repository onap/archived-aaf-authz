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
#
# Edit the following in <your ONAP authz dir>/auth/sample/local
# 
aaf.props
org.osaaf.aaf.cm.ca.props  (leave out Password)

# cd to main docker dir
cd ../../docker

# Start the container in bash mode, so it stays up
sh agent.sh bash

# in another shell, find out your Container name
docker container ls | grep aaf_agent

# CD to directory with CA info in it.
# (example)
cd /opt/app/osaaf/CA/intermediate_7

# copy keystore for this AAF Env 
docker container cp -L org.osaaf.aaf.p12 aaf_agent_<Your ID>:/opt/app/osaaf/local
# (in Agent Window)
agent encrypt cadi_keystore_password

# If you intend to use Certman to sign certs, it is a "local" CA
# copy Signing Keystore into container
docker container cp -L org.osaaf.aaf.signer.p12 aaf_agent_<Your ID>:/opt/app/osaaf/local
# (in Agent Window)
agent encrypt cm_ca.local 

# Check to make sure all passwords are set
grep "enc:" *.props


# When good, run AAF
bash drun.sh

# watch logs in Agent Window
cd ../logs
sh taillog
