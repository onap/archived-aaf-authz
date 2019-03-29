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
# Docker Building Script.  Reads all the components generated by install, on per-version basis
#

# Pull in Variables from d.props
if [ ! -e ./d.props ]; then
    cp d.props.init d.props
fi

. ./d.props

AAF_COMPONENTS=ALL

# process input. originally, an optional positional parameter is used to designate a component.
# A flagged parameter has been added to optionally indicate docker pull registry. Ideally, options
# would be flagged but we're avoiding ripple effect of changing original usage
if [ $# -gt 0 ]; then
    if [ "$1" == "-r" ]; then
        DOCKER_PULL_REGISTRY=$2
    else
        AAF_COMPONENTS=$1
        if [[ $# -gt 1 && $2 == "-r" ]]; then
            # If docker.io is indicated, registry var is void as that is docker default
            if [ $3 == "docker.io" ]; then
                DOCKER_PULL_REGISTRY=''
            else
                DOCKER_PULL_REGISTRY=$3
            fi
        fi
    fi
fi

echo "$0: AAF_COMPONENTS=$AAF_COMPONENTS DOCKER_PULL_REGISTRY=$DOCKER_PULL_REGISTRY"

DOCKER=${DOCKER:=docker}

echo "Building Containers for aaf components, version $VERSION"

# AAF_cass now needs a version...
cd ../auth-cass/docker
bash ./dbuild.sh $DOCKER_PULL_REGISTRY
cd -

# AAF Base version - set the core image, etc
sed -e 's/${AAF_VERSION}/'${VERSION}'/g' \
    -e 's/${DUSER}/'${DUSER}'/g' \
    -e 's/${REGISTRY}/'${DOCKER_PULL_REGISTRY}'/g' \
    Dockerfile.base > Dockerfile
$DOCKER build -t ${ORG}/${PROJECT}/aaf_base:${VERSION} .
$DOCKER tag ${ORG}/${PROJECT}/aaf_base:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_base:${VERSION}
$DOCKER tag ${ORG}/${PROJECT}/aaf_base:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_base:latest
rm Dockerfile

# Create the AAF Config (Security) Images
cd ..
cp auth-cmd/target/aaf-auth-cmd-$VERSION-full.jar sample/bin
cp auth-batch/target/aaf-auth-batch-$VERSION-full.jar sample/bin
cp -Rf ../conf/CA sample


# AAF Config image (for AAF itself)
sed -e 's/${AAF_VERSION}/'${VERSION}'/g' \
    -e 's/${AAF_COMPONENT}/'${AAF_COMPONENT}'/g' \
    -e 's/${DOCKER_REPOSITORY}/'${DOCKER_REPOSITORY}'/g' \
    -e 's/${DUSER}/'${DUSER}'/g' \
    docker/Dockerfile.config > sample/Dockerfile
$DOCKER build -t ${ORG}/${PROJECT}/aaf_config:${VERSION} sample
$DOCKER tag ${ORG}/${PROJECT}/aaf_config:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_config:${VERSION}
$DOCKER tag ${ORG}/${PROJECT}/aaf_config:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_config:latest

cp ../cadi/servlet-sample/target/aaf-cadi-servlet-sample-${VERSION}-sample.jar sample/bin
# AAF Agent Image (for Clients)
sed -e 's/${AAF_VERSION}/'${VERSION}'/g' \
    -e 's/${AAF_COMPONENT}/'${AAF_COMPONENT}'/g' \
    -e 's/${DOCKER_REPOSITORY}/'${DOCKER_REPOSITORY}'/g' \
    -e 's/${DUSER}/'${DUSER}'/g' \
    docker/Dockerfile.client > sample/Dockerfile
$DOCKER build -t ${ORG}/${PROJECT}/aaf_agent:${VERSION} sample
$DOCKER tag ${ORG}/${PROJECT}/aaf_agent:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_agent:${VERSION}
$DOCKER tag ${ORG}/${PROJECT}/aaf_agent:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_agent:latest

# Clean up 
rm sample/Dockerfile sample/bin/aaf-*-${VERSION}-full.jar sample/bin/aaf-cadi-servlet-sample-${VERSION}-sample.jar 
rm -Rf sample/CA
cd -

########
# Second, build a core Docker Image
echo Building aaf_$AAF_COMPONENT...
# Apply currrent Properties to Docker file, and put in place.
sed -e 's/${AAF_VERSION}/'${VERSION}'/g' \
    -e 's/${AAF_COMPONENT}/'${AAF_COMPONENT}'/g' \
    -e 's/${DOCKER_REPOSITORY}/'${DOCKER_REPOSITORY}'/g' \
    -e 's/${DUSER}/'${DUSER}'/g' \
    Dockerfile.core >../aaf_${VERSION}/Dockerfile
cd ..
$DOCKER build -t ${ORG}/${PROJECT}/aaf_core:${VERSION} aaf_${VERSION}
$DOCKER tag ${ORG}/${PROJECT}/aaf_core:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_core:${VERSION}
$DOCKER tag ${ORG}/${PROJECT}/aaf_core:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_core:latest
rm aaf_${VERSION}/Dockerfile
cd -

#######
# Do all the Containers related to AAF Services
#######
if [ $AAF_COMPONENTS == "ALL" ]; then
    AAF_COMPONENTS=$(ls ../aaf_${VERSION}/bin | grep -v '\.')
fi
echo "$0: AAF_COMPONENTS=$AAF_COMPONENTS"

cp ../sample/bin/pod_wait.sh  ../aaf_${VERSION}/bin
for AAF_COMPONENT in ${AAF_COMPONENTS}; do
    echo Building aaf_$AAF_COMPONENT...
    sed -e 's/${AAF_VERSION}/'${VERSION}'/g' \
        -e 's/${AAF_COMPONENT}/'${AAF_COMPONENT}'/g' \
        -e 's/${DOCKER_REPOSITORY}/'${DOCKER_REPOSITORY}'/g' \
        -e 's/${DUSER}/'${DUSER}'/g' \
        Dockerfile.ms >../aaf_${VERSION}/Dockerfile
    cd ..
    $DOCKER build -t ${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION} aaf_${VERSION}
    $DOCKER tag ${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION}
    $DOCKER tag ${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION} ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:latest
    rm aaf_${VERSION}/Dockerfile
    cd -

done
rm ../aaf_${VERSION}/bin/pod_wait.sh
