#!/bin/bash
. ./d.props

function run_it() {
  docker run $@ \
    --mount 'type=volume,src=aaf_config,dst='$CONF_ROOT_DIR',volume-driver=local' \
    --add-host="$HOSTNAME:$HOST_IP" \
    --add-host="aaf.osaaf.org:$HOST_IP" \
    --env AAF_ENV=${AAF_ENV} \
    --env AAF_REGISTER_AS=${AAF_REGISTER_AS} \
    --env LATITUDE=${LATITUDE} \
    --env LONGITUDE=${LONGITUDE} \
    --name aaf_config_$USER \
    $PREFIX${ORG}/${PROJECT}/aaf_config:${VERSION} \
    /bin/bash $PARAMS
}

function set_prop() {
docker exec -t aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP setProp "$1" "$2" "$3"
}

function encrypt_it() {
  docker exec -t aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP encrypt "$1" "$2"
}

function set_it() {
  docker exec -t aaf_config_$USER /bin/bash /opt/app/aaf_config/bin/agent.sh NOOP setProp "$1" "$2"
}

P12_LOAD="no"

for PROP in AAF_INITIAL_X509_P12 AAF_INITIAL_X509_PASSWORD AAF_SIGNER_P12 AAF_SIGNER_PASSWORD CADI_X509_ISSUERS; do
    if [ "${!PROP}" != "" ]; then
    	  P12_LOAD='yes'
	  break;
    fi 
done

# First Time Run does a bit more setup
if [ "$(docker volume ls | grep aaf_config)" = "" ] && [ ${P12_LOAD} = "yes" ]; then
  echo "Initializing first aaf_config"
  if [ "$(docker container ls | grep aaf_config_$USER)" = "" ]; then
	PARAMS="bash"
	run_it -t -d 
  else 
	echo "aaf_config_$USER is already running"
  fi	
  docker container cp ${AAF_INITIAL_X509_P12} aaf_config_$USER:/opt/app/osaaf/local/org.osaaf.aaf.p12
  docker container cp ${AAF_SIGNER_P12} aaf_config_$USER:/opt/app/osaaf/local/org.osaaf.aaf.signer.p12

  if [ -z "$CM_CA_LOCAL" ]; then
    CM_CA_LOCAL="org.onap.aaf.auth.cm.ca.LocalCA,/opt/app/osaaf/local/org.osaaf.aaf.signer.p12;${AAF_SIGNER_ALIAS};enc:"
  fi
  set_prop cm_ca.local "${CM_CA_LOCAL}" org.osaaf.aaf.cm.ca.props
  set_prop cadi_x509_issuers "${CADI_X509_ISSUERS}" org.osaaf.aaf.props

  encrypt_it cadi_keystore_password "${AAF_INITIAL_X509_PASSWORD}"
  encrypt_it cm_ca.local "${AAF_SIGNER_PASSWORD}"

  echo -n "Stopping "
  docker container stop aaf_config_$USER 
  echo -n "Removing "
  docker container rm aaf_config_$USER
fi

PARAMS="$@"
if [ "$PARAMS" != "" ]; then
  run_it -it --rm 
fi

