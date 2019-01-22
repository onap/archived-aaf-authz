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
FROM ${DOCKER_REPOSITORY}/onap/aaf/aaf_core:${AAF_VERSION}
MAINTAINER AAF Team, AT&T 2018
ENV VERSION=${AAF_VERSION}

LABEL description="aaf_${AAF_COMPONENT}"
LABEL version=${AAF_VERSION}

COPY pod/* /opt/app/aaf/pod/

#CMD ["bash","-c","cd /opt/app/aaf;bin/${AAF_COMPONENT}"]
CMD []

# For Debugging installation
# CMD ["/bin/bash","-c","pwd;cd /opt/app/osaaf;find /opt/app/osaaf -depth;df -k; cat /opt/app/aaf/${AAF_COMPONENT}/bin/${AAF_COMPONENT};cat /etc/hosts;/opt/app/aaf/${AAF_COMPONENT}/bin/${AAF_COMPONENT}"]
# Java Debugging VM Args
#     "-Xdebug",\
#     "-Xnoagent",\
#     "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000",\

# TLS Debugging VM Args
#     "-Djavax.net.debug","ssl", \
     
