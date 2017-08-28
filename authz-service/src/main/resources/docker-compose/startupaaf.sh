# lji: this startup file shadows the existing extry point startup.sh file of the container
# because we need to pass in the cassandra cluster location 

LIB=/opt/app/aaf/authz-service/2.0.15/lib

ETC=/opt/app/aaf/authz-service/2.0.15/etc
DME2REG=/opt/dme2reg

echo "this is LIB" $LIB
echo "this is ETC" $ETC
echo "this is DME2REG" $DME2REG

CLASSPATH=$ETC
for FILE in `find $LIB -name *.jar`; do
  CLASSPATH=$CLASSPATH:$FILE
done

FILEPATHS="/opt/app/aaf/common/com.osaaf.common.props /opt/app/aaf/authz-service/2.0.15/etc/com.osaaf.common.props"
for FILEPATH in $FILEPATHS: 
do 
  if [ -e ${FILEPATH} ]; then
    if [ -z `grep "cassandra.clusters=$CASSANDRA_CLUSTER" $FILEPATH` ]; then 
      echo "cassandra.clusters=$CASSANDRA_CLUSTER" >> $FILEPATH; 
    fi
  fi
done


java -classpath $CLASSPATH -DDME2_EP_REGISTRY_CLASS=DME2FS -DAFT_DME2_EP_REGISTRY_FS_DIR=$DME2REG com.att.authz.service.AuthAPI

# keet it running so we can check fs
while sleep 2; do echo thinking; done
