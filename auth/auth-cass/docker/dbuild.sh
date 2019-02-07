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
# Build AAF Cass Docker Script locally
#
# Pull in AAF Env Variables from AAF install
if [ -e ../../docker/d.props ]; then
  . ../../docker/d.props
fi
DOCKER=${DOCKER:-docker}

echo "Building aaf_cass Container for aaf_cass:$VERSION"

DIR=$(pwd)
cd ..
sed -e 's/${AAF_VERSION}/'${VERSION}'/g' \
    -e 's/${USER}/'${USER}'/g' \
    $DIR/Dockerfile.cass > Dockerfile
cd ..
cp -Rf sample/cass_data auth-cass/cass_data
cp sample/data/sample.identities.dat auth-cass
cp auth-batch/target/aaf-auth-batch-$VERSION-full.jar auth-cass

echo $DOCKER build -t ${ORG}/${PROJECT}/aaf_cass:${VERSION} auth-cass
$DOCKER build -t ${ORG}/${PROJECT}/aaf_cass:${VERSION} auth-cass
$DOCKER tag ${ORG}/${PROJECT}/aaf_cass:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_cass:${VERSION}
$DOCKER tag ${ORG}/${PROJECT}/aaf_cass:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_cass:latest

cd -
rm Dockerfile
rm -Rf cass_data
rm sample.identities.dat
rm aaf-auth-batch-$VERSION-full.jar
cd $DIR

