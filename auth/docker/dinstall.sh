#!/bin/bash 
#########
#  ============LICENSE_START====================================================
#  org.onap.aaf
#  ===========================================================================
#  Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#  ===========================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ============LICENSE_END====================================================
#
# This is only called from HEAT, as it needs a single check and wait for Cassandra to be ready
#
cd ../auth-cass/docker
. dinstall.sh $@
cd -
if [ "$1" = "publish" ]; then
  shift
fi
. drun.sh 
