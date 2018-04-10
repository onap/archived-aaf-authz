#!/bin/bash dbuild.sh
#
# Docker Building Script.  Reads all the components generated by install, on per-version basis
#
# Pull in Variables from d.props
ORG=onap
PROJECT=aaf
DOCKER_REPOSITORY=nexus3.onap.org:10003
VERSION=2.1.0-SNAPSHOT
./d.props
# TODO add ability to do DEBUG settings

if ["$1" == ""]; then
  AAF_COMPONENTS=`ls ../aaf_*HOT/bin | grep -v '\.'`
else
  AAF_COMPONENTS=$1
fi

for AAF_COMPONENT in ${AAF_COMPONENTS}; do
        echo Building aaf_$AAF_COMPONENT...
        sed -e 's/${AAF_VERSION}/'${VERSION}'/g' -e 's/${AAF_COMPONENT}/'${AAF_COMPONENT}'/g' Dockerfile > ../aaf_${VERSION}/Dockerfile
        cd ..
        docker build -t ${DOCKER_REPOSITORY}/${ORG}/${PROJECT}/aaf_${AAF_COMPONENT}:${VERSION}  aaf_${VERSION}
        rm aaf_${VERSION}/Dockerfile
        cd -
done


