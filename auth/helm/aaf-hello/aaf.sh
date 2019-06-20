. ../../docker/aaf.props
IMAGE=onap/aaf/aaf_agent:$VERSION

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
                     "name": "aaf_locator_container",
                     "value": "helm"
		   },{
                     "name": "aaf_locator_fqdn",
                     "value": "'$HOSTNAME'"
		   },{
                     "name": "aaf_locator_url",
                     "value": "https://aaf-locate:8095"
		   },{
                     "name": "aaf_locator_public_hostname",
                     "value": "'$HOSTNAME'"
		   },{
                     "name": "AAF_ENV",
                     "value": "'$AAF_ENV'"
		   },{
                     "name": "LATITUDE",
                     "value": "'$LATITUDE'"
		   },{
                     "name": "LONGITUDE",
                     "value": "'$LONGITUDE'"
		   },{
                     "name": "CASSANDRA_CLUSTER",
                     "value": "'$CASSANDRA_CLUSTER'"
		   },{
                     "name": "CASSANDRA_USER",
                     "value": "'$CASSANDRA_USER'"
		   },{
                     "name": "CASSANDRA_PASSWORD",
                     "value": "'$CASSANDRA_PASSWORD'"
		   },{
                     "name": "CASSANDRA_PORT",
                     "value": "'$CASSANDRA_PORT'"
		   }
                ],
                "stdin": true,
                "stdinOnce": true,
                "tty": true,
                "volumeMounts": [
                    {
                        "mountPath": "/opt/app/osaaf",
                        "name": "aaf-config-vol"
                    },
                    {
                        "mountPath": "/opt/app/aaf/status",
                        "name": "aaf-status-vol"
                    }
                ]
            }
        ],
      "volumes": [
            {
                "name": "aaf-config-vol",
                "persistentVolumeClaim": {
                    "claimName": "aaf-config-pvc"
                }
            },
            {
                "name": "aaf-status-vol",
                "persistentVolumeClaim": {
                    "claimName": "aaf-status-pvc"
                }
            }
        ]
   }
}
' --restart=Never  -- bash 
