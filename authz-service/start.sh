DIR=`pwd`
LIB=$DIR/target/swm/package/nix/dist_files/opt/app/aaf/authz-service/2.0.15/lib
ETC=$DIR/src/main/sample
DME2REG=$DIR/../dme2reg

CLASSPATH=$ETC
for FILE in `find $LIB -depth 1 -name *.jar`; do
  CLASSPATH=$CLASSPATH:$FILE
done
java -classpath $CLASSPATH -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG com.att.authz.service.AuthAPI

