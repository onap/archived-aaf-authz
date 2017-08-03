#!/bin/sh

DIR=`pwd` 
#DME2REG=$DIR/../dme2reg 
DME2REG=/opt/dme2reg 
#CLASSPATH=etc:target/authz-cmd-2.0.15-jar-with-dependencies.jar  
CLASSPATH=/opt/app/aaf/authz-service/2.0.15/etc:/opt/app/aaf/authz-service/2.0.15/lib/authz-cmd-2.0.15-jar-with-dependencies.jar  
#java -cp $CLASSPATH -Dcadi_prop_files=../authz-service/src/main/sample/authAPI.props -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG com.att.cmd.AAFcli $*
java -cp $CLASSPATH -Dcadi_prop_files=/opt/app/aaf/authz-service/2.0.15/etc/authAPI.props -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG com.att.cmd.AAFcli $*
