
LIB=/media/sf_Users/sg481n/AAF-DOC/authz/authz-service/target/opt/app/aaf/authz-service/lib

ETC=/media/sf_Users/sg481n/AAF-DOC/authz/authz-service/target/opt/app/aaf/authz-service/etc
DME2REG=/media/sf_Users/sg481n/AAF-DOC/authz/authz-service/target/opt/dme2reg

echo "this is LIB" $LIB
echo "this is ETC" $ETC
echo "this is DME2REG" $DME2REG

CLASSPATH=$ETC
for FILE in `find $LIB -name *.jar`; do
  CLASSPATH=$CLASSPATH:$FILE
done
java -classpath $CLASSPATH -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG org.onap.aaf.authz.service.AuthAPI






