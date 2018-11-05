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
. drun.sh $@

echo "Waiting for Cass to be initialized"
for I in 1 2 3 4 5 6 7 8 9 10 11 12 13 14; do
  $DOCKER exec -it aaf_cass bash aaf_cmd wait 2> /dev/null
  if [ "$?" -ne "0" ]; then
    echo "Container not ready... Sleep 10"
    sleep 10
  else
    echo "aaf_cass is ready"
    break
  fi 
done


