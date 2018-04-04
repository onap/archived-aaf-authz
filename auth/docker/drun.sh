HOSTNAME=meriadoc.mithril.sbc.com
HOST_IP=172.17.0.3
CASS_HOST="cass.mithril.sbc.com:172.17.0.3"
VERSION=2.1.0-SNAPSHOT

if [ "$1" == "" ]; then
  AAF_COMPONENTS=`ls ../aaf_${VERSION}/bin | grep -v '\.'`
else
  AAF_COMPONENTS=$1
fi
  
for AAF_COMPONENT in ${AAF_COMPONENTS}; do 
	
	case "$AAF_COMPONENT" in
		"service") PORTMAP="8100:8100";;
		"locate") PORTMAP="443:8095";;
		"oauth") PORTMAP="8140:8140";;
		"gui") PORTMAP="8200:8200";;
		"cm") PORTMAP="8150:8150";;
		"hello") PORTMAP="8130:8130";;
		"fs") PORTMAP="80:8096";;
	esac
	
#	if [ "`docker container ls | grep aaf_$AAF_COMPONENT:$VERSION`" == "" ]; then 
		echo docker run \
		  --name aaf_$AAF_COMPONENT \
		  --hostname="$HOSTNAME" \
		  --add-host="$CASS_HOST" \
		  --publish $PORTMAP \
		  --volume=/opt/app/osaaf/etc:/opt/app/osaaf/etc \
		  --link aaf_cass:cassandra \
		  aaf_$AAF_COMPONENT:$VERSION
#	else
	  #echo docker container start -ia aaf_$AAF_COMPONENT
#	fi
done
#		  --add-host="$HOSTNAME:$HOST_IP" \
