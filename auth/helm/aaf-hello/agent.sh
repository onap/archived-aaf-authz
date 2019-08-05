function prop () {
   echo $(grep " $1" values.yaml | grep -v "#"| sed -e "s/.*$1: *//")
}

REPO=$(prop repository)
if [ -z "$REPO" ]; then
  REPO="nexus3.onap.org:10001"
fi

if [ "\"\"" = "$REPO" ]; then
    IMAGE="$(prop agentImage)"
else 
    IMAGE="$REPO/$(prop agentImage)"
fi

APP_FQI=$(prop fqi)
FQDN=$(prop fqdn)
LATITUDE=$(prop cadi_latitude)
LONGITUDE=$(prop cadi_longitude)
DEPLOY_FQI=$(prop deploy_fqi)
echo "Enter Password for Deployer: $DEPLOY_FQI"
#read DEPLOY_PASSWORD
#if [ -z "$DEPLOY_PASSWORD" ]; then
  # ONAP TEST Password.  DO NOT PUT REAL PASSWORDS HERE!!!
  DEPLOY_PASSWORD='"demo123456!"'
#fi
DEPLOYMENT=$(kubectl -n onap get deployments | grep ${FQDN//\"} | cut -f1 -d' ')
if [ -z "$DEPLOYMENT" ]; then
  DEPLOYMENT=$FQDN
fi
echo Running from $IMAGE for Deployment $DEPLOYMENT

kubectl -n onap run -it --rm aaf-agent-$USER --image=$IMAGE --overrides='
{
    "spec": {
        "containers": [
            {
		"name": "aaf-agent-'$USER'",
                "image": "'$IMAGE'",
                "imagePullPolicy": "IfNotPresent",
                "command": [
                   "bash",
                   "-c",
                   "/opt/app/aaf_config/bin/agent.sh && cd /opt/app/osaaf/local && exec bash"
                 ],
                "env": [
                   {
                     "name": "APP_FQI",
                     "value": '$APP_FQI'
                   },{
                     "name": "APP_FQDN",
                     "value": '$FQDN'
                   },{
                     "name": "DEPLOY_FQI",
                     "value": '$DEPLOY_FQI'
                   },{
                     "name": "DEPLOY_PASSWORD",
                     "value": '$DEPLOY_PASSWORD'
                   },{
                     "name": "aaf_locate_url",
                     "value": "https://aaf-locate.onap:8095"
                   },{
                     "name": "aaf_locator_container",
                     "value": "helm"
                   },{
                     "name": "aaf_locator_container_ns",
                     "value": "onap"
                   },{
                     "name": "aaf_locator_public_fqdn",
                     "value": "aaf.osaaf.org"
                   },{
                     "name": "aaf_locator_fqdn",
                     "value": '$FQDN'
                   },{
                     "name": "cadi_latitude",
                     "value": '$LATITUDE'
                   },{
                     "name": "cadi_longitude",
                     "value": '$LONGITUDE'
                   }
                ],
                "stdin": true,
                "stdinOnce": true,
                "tty": true,
                "volumeMounts": [
                    {
                        "mountPath": "/opt/app/osaaf",
                        "name": "'${FQDN//\"}'-vol"
                    }
                ]
            }
        ],
        "volumes": [
            {
                "name": "'${FQDN//\"}'-vol",
                "persistentVolumeClaim": {
                    "claimName": "'${DEPLOYMENT//\"}'-pvc"
                }
            }
         ]
   }
}
' --restart=Never  -- bash
