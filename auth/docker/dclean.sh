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
# Pull in Variables from d.props
. ./d.props
DOCKER=${DOCKER:=docker}

if [ "$1" == "" ]; then
    AAF_COMPONENTS="$(cat components) config core agent base "
else
    AAF_COMPONENTS="$@"
fi

echo "Y" | $DOCKER container prune
for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    $DOCKER image rm $ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
    if [ ! "$PREFIX" = "" ]; then
      $DOCKER image rm $DOCKER_REPOSITORY/$ORG/$PROJECT/aaf_$AAF_COMPONENT:${VERSION}
      $DOCKER image rm $DOCKER_REPOSITORY/$ORG/$PROJECT/aaf_$AAF_COMPONENT:latest
    fi
done

echo "Y" | $DOCKER image prune
