#!/bin/sh
##############################################################################
# - Copyright 2012, 2016 AT&T Intellectual Properties
##############################################################################
umask 022
ROOT_DIR=${INSTALL_ROOT}/${distFilesRootDirPath}

# Grab the IID of all resources running under the name and same version(s) we're working on and stop those instances
${LRM_HOME}/bin/lrmcli -running | \
	grep ${artifactId} | \
	grep ${version} | \
	cut -f1 | \
while read _iid
do
	if [ -n "${_iid}" ]; then
		${LRM_HOME}/bin/lrmcli -shutdown -iid ${_iid} | grep SUCCESS
		if [ $? -ne 0 ]; then
			echo "$LRMID-{_iid} Shutdown failed"
		fi
	fi
done
	
# Grab the resources configured under the name and same version we're working on and delete those instances
${LRM_HOME}/bin/lrmcli -configured | \
	grep ${artifactId} | \
	grep ${version} | \
	cut -f1,2,3 | \
while read _name _version _routeoffer
do
	if [ -n "${_name}" ]; then
		${LRM_HOME}/bin/lrmcli -delete -name ${_name} -version ${_version} -routeoffer ${_routeoffer} | grep SUCCESS
		if [ $? -ne 0 ]; then
			echo "${_version} Delete failed"
		fi
	fi
done	

rm -rf ${ROOT_DIR}

exit 0
