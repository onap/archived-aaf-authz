DIR=`pwd`
#DME2REG=$DIR/../dme2reg
DME2REG=/opt/dme2reg
#CLASSPATH=etc:target/authz-cmd-1.0.0-SNAPSHOT-jar-with-dependencies.jar

#java -cp $CLASSPATH \
	#-Dcadi_prop_files=../authz-service/src/main/sample/authAPI.props \
	#-DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG \
	#com.att.cmd.AAFcli $*

CLASSPATH=/opt/app/aaf/authz-service/1.0.0-SNAPSHOT/etc:/opt/app/aaf/authz-service/1.0.0-SNAPSHOT/lib/authz-cmd-1.0.0-SNAPSHOT-jar-with-dependencies.jar  
#java -cp $CLASSPATH -Dcadi_prop_files=../authz-service/src/main/sample/authAPI.props -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG com.att.cmd.AAFcli $*
java -cp $CLASSPATH -Dcadi_prop_files=/opt/app/aaf/authz-service/1.0.0-SNAPSHOT/etc/authAPI.props -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG org.onap.aaf.cmd.AAFcli $*
