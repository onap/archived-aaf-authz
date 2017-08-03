#!/bin/sh
##############################################################################
# - Copyright 2012, 2016 AT&T Intellectual Properties
##############################################################################
umask 022
ROOT_DIR=${INSTALL_ROOT}/${distFilesRootDirPath}
LOGGING_PROP_FILE=${ROOT_DIR}/etc/log4j.properties
RUN_FILE=${ROOT_DIR}/etc/tconn.sh

cd ${ROOT_DIR}

mkdir -p logs || fail 1 "Error on creating the logs directory."
mkdir -p back || fail 1 "Error on creating the back directory."
chmod 777 back || fail 1 "Error on creating the back directory."

# 
# Some Functions that Vastly cleanup this install file...
# You wouldn't believe how ugly it was before.  Unreadable... JG 
#
fail() {
	rc=$1
	shift;
    echo "ERROR: $@"
    exit $rc
}

#
# Set the "SED" replacement for this Variable.  Error if missing
# Note that Variable in the Template is surrounded by "_" i.e. _ROOT_DIR_
#   Replacement Name
#   Value
#
required() {
	if [ -z "$2" ]; then
	  ERRS+="\n\t$1 must be set for this installation"
	fi
	SED_E+=" -e s|$1|$2|g"
}

#
# Set the "SED" replacement for this Variable. Use Default (3rd parm) if missing
# Note that Variable in the Template is surrounded by "_" i.e. _ROOT_DIR_
#   Replacement Name
#   Value
#   Default Value
#
default() {
    if [ -z "$2" ]; then
    	SED_E+=" -e s|$1|$3|g"
    else 
    	SED_E+=" -e s|$1|$2|g"
    fi
}

# Linux requires this.  Mac blows with it.  Who knows if Windoze even does SED
if [ -z "$SED_OPTS" ]; then
	SED_E+=" -c "
else
	SED_E+=$SED_OPTS;
fi 


# 
# Use "default" function if there is a property that isn't required, but can be defaulted
# use "required" function if the property must be set by the environment
#
	required _ROOT_DIR_ ${ROOT_DIR}
	default _COMMON_DIR_ ${COMMON_DIR} ${ROOT_DIR}/../../common
	required _AFT_ENVIRONMENT_ ${AFT_ENVIRONMENT}
	required _ENV_CONTEXT_ ${ENV_CONTEXT}
	required _HOSTNAME_ ${HOSTNAME}
	required _ARTIFACT_ID_ ${artifactId}
	required _ARTIFACT_VERSION_ ${version}
	
	# Specifics for Service
	if [ "${artifactId}" = "authz-service" ]; then
		default _AUTHZ_SERVICE_PORT_ ${PORT} 0
		required _AUTHZ_CASS_CLUSTERS_ ${AUTHZ_CASS_CLUSTERS}
		required _AUTHZ_CASS_PORT_ ${AUTHZ_CASS_PORT}
		required _AUTHZ_CASS_PWD_ ${AUTHZ_CASS_PWD}
		default _AUTHZ_CASS_USER_ ${AUTHZ_CASS_USER} authz
		required _AUTHZ_KEYSTORE_PASSWORD_ ${AUTHZ_KEYSTORE_PASSWORD}
		required _AUTHZ_KEY_PASSWORD_ ${AUTHZ_KEY_PASSWORD}
		required _SCLD_PLATFORM_ ${SCLD_PLATFORM}
	fi

	default _EMAIL_FROM_ ${EMAIL_FROM} authz@ems.att.com
    default _EMAIL_HOST_ ${EMAIL_HOST} mailhost.att.com
	default _ROUTE_OFFER_ ${ROUTE_OFFER} BAU_SE
	default _DME_TIMEOUT_ ${DME_TIMEOUT} 3000

	# Choose defaults for log level and logfile size
	if [ "${SCLD_PLATFORM}" = "PROD" ]; then
	        LOG4J_LEVEL=WARN
	fi
	default _LOG4J_LEVEL_ ${LOG4J_LEVEL} INFO  
	default _LOG4J_SIZE_ ${LOG4J_SIZE} 10000KB
	default _LOG_DIR_ ${LOG_DIR} ${ROOT_DIR}/logs
	default _MAX_LOG_FILE_SIZE_ ${MAX_LOG_FILE_SIZE} 10000KB
	default _MAX_LOG_FILE_BACKUP_COUNT_ ${MAX_LOG_FILE_BACKUP_COUNT} 7
	default _RESOURCE_MIN_COUNT_ ${RESOURCE_MIN_COUNT} 1
	default _RESOURCE_MAX_COUNT_ ${RESOURCE_MAX_COUNT} 1

	required _LOGGING_PROP_FILE_ ${LOGGING_PROP_FILE}
	required _AFT_LATITUDE_ ${LATITUDE}
	required _AFT_LONGITUDE_ ${LONGITUDE}
	required _HOSTNAME_ ${HOSTNAME}
	
	# Divide up Version
	default _MAJOR_VER_ "`expr ${version} : '\([0-9]*\)\..*'`"
	default _MINOR_VER_ "`expr ${version} : '[0-9]*\.\([0-9]*\)\..*'`"
	default _PATCH_VER_ "`expr ${version} : '[0-9]\.[0-9]*\.\(.*\)'`"

	

# Now Fail if Required items are not set... 
# Report all of them at once!
if [ "${ERRS}" != "" ] ; then
	fail 1 "${ERRS}"
fi

#echo ${SED_E}

for i in ${PROPERTIES_FILE} ${LRM_XML} ${LOGGING_PROP_FILE} ${RUN_FILE} ; do
  if [ -r ${i} ]; then
	  if [ -w ${i} ]; then
#	  	echo ${i}
	     sed ${SED_E} -i'.sed' ${i} || fail 8 "could not sed ${i} "
	     mv -f ${i}.sed ${ROOT_DIR}/back
	   fi
	fi
done

#
# Add the resource to LRM using the newly created/substituted XML file.
#
# Note: No LRM for authz-test
#if [ -r ${LRM_XML} ]; then
#	${LRM_HOME}/bin/lrmcli -addOrUpgrade -file ${LRM_XML} || fail 1 "Add to LRM Failed"
#	${LRM_HOME}/bin/lrmcli -start -name com.att.authz.${artifactId} -version ${version} -routeoffer ${ROUTE_OFFER} | grep SUCCESS
#fi
#
# Note: Must exit 0 or, it will be exit default 1 and fail
exit 0
