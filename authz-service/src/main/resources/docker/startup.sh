
LIB=/opt/app/aaf/authz-service/1.0.0-SNAPSHOT/lib

ETC=/opt/app/aaf/authz-service/1.0.0-SNAPSHOT/etc
DME2REG=/opt/dme2reg

echo "this is LIB" $LIB
echo "this is ETC" $ETC
echo "this is DME2REG" $DME2REG

CLASSPATH=$ETC
for FILE in `find $LIB -name *.jar`; do
  CLASSPATH=$CLASSPATH:$FILE
done
java -classpath $CLASSPATH -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG org.onap.aaf.authz.service.AuthAPI






