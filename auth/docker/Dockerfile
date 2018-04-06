FROM openjdk:8
MAINTAINER AAF Team, AT&T 2018
ENV VERSION=${AAF_VERSION}

LABEL description="aaf ${AAF_COMPONENT}"
LABEL version=${AAF_VERSION}


#RUN apt-get update
#RUN apt-get install -y softhsm2
#RUN apt-get install -y libsofthsm2
#RUN apt-get install -y opensc

COPY lib /opt/app/aaf/${AAF_COMPONENT}/lib
COPY theme /opt/app/aaf/${AAF_COMPONENT}/theme
COPY bin /opt/app/aaf/${AAF_COMPONENT}/bin

CMD ["/bin/bash","/opt/app/aaf/${AAF_COMPONENT}/bin/${AAF_COMPONENT}"]

# For Debugging installation
#CMD ["/bin/bash","-c","pwd;cd /opt/app/osaaf;find /opt/app/osaaf -depth;df -k; cat /opt/app/aaf/${AAF_COMPONENT}/bin/${AAF_COMPONENT}"]
# Java Debugging VM Args
#     "-Xdebug",\
#     "-Xnoagent",\
#     "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000",\

# TLS Debugging VM Args
#     "-Djavax.net.debug","ssl", \
     
