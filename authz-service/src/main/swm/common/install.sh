#!/bin/sh
##############################################################################
# AAF Installs
# - Copyright 2015, 2016 AT&T Intellectual Properties
##############################################################################
umask 022
ROOT_DIR=${INSTALL_ROOT}${distFilesRootDirPath}
COMMON_DIR=${INSTALL_ROOT}${distFilesRootDirPath}/../../common
LRM_XML=${ROOT_DIR}/etc/lrm-${artifactId}.xml
LOGGING_PROP_FILE=${ROOT_DIR}/etc/log4j.properties
LOGGER_PROP_FILE=${ROOT_DIR}/etc/logging.props
AAFLOGIN=${ROOT_DIR}/bin/aaflogin
JAVA_HOME=/opt/java/jdk/jdk180
JAVA=$JAVA_HOME/bin/java
CADI_JAR=`ls $ROOT_DIR/lib/cadi-core*.jar`

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

# 
# Password behavior:
#     For each Password passed in:
#       If Password starts with "enc:???", then replace it as is
#       If not, then check for CADI_KEYFILE... see next
#     If the CADI_KEYFILE is set, the utilize this as the CADI Keyfile
#     	If it does not exist, create it, and change to "0400" mode
#     Utilize the Java and "cadi-core" found in Library to
#       Encrypt Password with Keyfile, prepending "enc:???"
#
passwd() {
  #
  # Test if var exists, and is required
  #
  if [ "${!1}" = "" ]; then
    if [ "${2}" = "required" ]; then
     	ERRS+="\n\t$1 must be set for this installation" 
    fi
  else
    #
    # Test if needs encrypting
    #
    if [[ ${!1} = enc:* ]]; then
      SED_E+=" -e s|_${1}_|${!1}|g"
    else
      if [ "${CADI_KEYFILE}" != "" ]  &&  [ -e "${CADI_JAR}" ]; then
        #
        # Create or use Keyfile listed in CADI_KEYFILE
        #
        if [ -e "${CADI_KEYFILE}" ]; then
          if [ "$REPORTED_CADI_KEYFILE" = "" ]; then
            echo "Using existing CADI KEYFILE (${CADI_KEYFILE})"
            REPORTED_CADI_KEYFILE=true
          fi
        else
           echo "Creating CADI_KEYFILE (${CADI_KEYFILE})"
           $JAVA -jar $CADI_JAR keygen ${CADI_KEYFILE}
           chmod 0400 ${CADI_KEYFILE}
        fi

        PASS=`$JAVA -jar $CADI_JAR digest ${!1} ${CADI_KEYFILE}`
        SED_E+=" -e s|_${1}_|enc:$PASS|g"
      else
        if [ "$REPORTED_CADI_KEYFILE" = "" ]; then
          if [ "${CADI_KEYFILE}" = "" ]; then
            ERRS+="\n\tCADI_KEYFILE must be set for this installation" 
          fi
          if [ ! -e "${CADI_JAR}" ]; then
            ERRS+="\n\t${CADI_JAR} must exist to deploy passwords"
          fi
          REPORTED_CADI_KEYFILE=true
        fi
      fi
    fi
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
	default _COMMON_DIR_ ${AUTHZ_COMMON_DIR} ${COMMON_DIR}
	required _JAVA_HOME_ ${JAVA_HOME}
	required _SCLD_PLATFORM_ ${SCLD_PLATFORM}
	required _HOSTNAME_ ${TARGET_HOSTNAME_FQ}
	required _ARTIFACT_ID_ ${artifactId}
	default _ARTIFACT_VERSION_ ${AFTSWM_ACTION_NEW_VERSION}
	default _RESOURCE_REGISTRATION_ ${RESOURCE_REGISTRATION} true
	default _AUTHZ_DATA_DIR_ ${AUTHZ_DATA_DIR} ${ROOT_DIR}/../../data
	default _CM_URL_ ${CM_URL} ""
	
	# Specifics for Service
	if [ "${artifactId}" = "authz-service" ]; then
		PROPERTIES_FILE=${ROOT_DIR}/etc/authAPI.props
		default _RESOURCE_MIN_COUNT_ ${RESOURCE_MIN_COUNT} 1
		default _RESOURCE_MAX_COUNT_ ${RESOURCE_MAX_COUNT} 5
		required _AUTHZ_SERVICE_PORT_RANGE_ ${AUTHZ_SERVICE_PORT_RANGE}
		
	elif [ "${artifactId}" = "authz-gui" ]; then
		PROPERTIES_FILE=${ROOT_DIR}/etc/authGUI.props
		required _AUTHZ_GUI_PORT_RANGE_ ${AUTHZ_GUI_PORT_RANGE}
		default _RESOURCE_MIN_COUNT_ ${RESOURCE_MIN_COUNT} 1
		default _RESOURCE_MAX_COUNT_ ${RESOURCE_MAX_COUNT} 2

	elif [ "${artifactId}" = "authz-gw" ]; then
		PROPERTIES_FILE=${ROOT_DIR}/etc/authGW.props
		default _AUTHZ_GW_PORT_RANGE_ ${AUTHZ_GW_PORT_RANGE} 8095-8095
		default _RESOURCE_MIN_COUNT_ 1
		default _RESOURCE_MAX_COUNT_ 1

	elif [ "${artifactId}" = "authz-fs" ]; then
		PROPERTIES_FILE=${ROOT_DIR}/etc/FileServer.props
		OTHER_FILES=${ROOT_DIR}/data/test.html
		default _AUTHZ_FS_PORT_RANGE_ ${AUTHZ_FS_PORT_RANGE} 8096-8096
		default _RESOURCE_MIN_COUNT_ 1
		default _RESOURCE_MAX_COUNT_ 1

	elif [ "${artifactId}" = "authz-certman" ]; then
		PROPERTIES_FILE=${ROOT_DIR}/etc/certman.props
		default _AUTHZ_CERTMAN_PORT_RANGE_ ${AUTHZ_CERTMAN_PORT_RANGE} 8150-8159
		default _RESOURCE_MIN_COUNT_ 1
		default _RESOURCE_MAX_COUNT_ 1
	elif [ "${artifactId}" = "authz-batch" ]; then
		PROPERTIES_FILE=${ROOT_DIR}/etc/authBatch.props
		cd /
		OTHER_FILES=`find ${ROOT_DIR}/bin -depth -type f`
		cd -
		default _RESOURCE_MIN_COUNT_ 1
		default _RESOURCE_MAX_COUNT_ 1
		required _AUTHZ_GUI_URL_ ${AUTHZ_GUI_URL}
	else
		PROPERTIES_FILE=NONE
	fi

	if [ "${DME2_FS}" != "" ]; then
		SED_E+=" -e s|_DME2_FS_|-DDME2_EP_REGISTRY_CLASS=DME2FS\$\{AAF_SPACE\}-DAFT_DME2_EP_REGISTRY_FS_DIR=${DME2_FS}|g"
	else
		SED_E+=" -e s|_DME2_FS_||g"
	fi
	

	default _EMAIL_FROM_ ${EMAIL_FROM} authz@ems.att.com
    default _EMAIL_HOST_ ${EMAIL_HOST} mailhost.att.com
	default _ROUTE_OFFER_ ${ROUTE_OFFER} BAU_SE
	default _DME_TIMEOUT_ ${DME_TIMEOUT} 3000

	# Choose defaults for log level and logfile size
	if [ "${SCLD_PLATFORM}" = "PROD" ]; then
	        LOG4J_LEVEL=WARN
	fi

	default _AFT_ENVIRONMENT_ ${AFT_ENVIRONMENT} AFTUAT
	default _ENV_CONTEXT_ ${ENV_CONTEXT} DEV
	default _LOG4J_LEVEL_ ${LOG4J_LEVEL} WARN  
	default _LOG4J_SIZE_ ${LOG4J_SIZE} 10000KB
	default _LOG_DIR_ ${LOG_DIR} ${ROOT_DIR}/logs
	default _MAX_LOG_FILE_SIZE_ ${MAX_LOG_FILE_SIZE} 10000KB
	default _MAX_LOG_FILE_BACKUP_COUNT_ ${MAX_LOG_FILE_BACKUP_COUNT} 7

	if [ "${artifactId}" != "authz-batch" ]; then
		required _LRM_XML_ ${LRM_XML}
	fi
	required _AFT_LATITUDE_ ${LATITUDE}
	required _AFT_LONGITUDE_ ${LONGITUDE}
	required _HOSTNAME_ ${HOSTNAME}

	required _PROPERTIES_FILE_ ${PROPERTIES_FILE}
	required _LOGGING_PROP_FILE_ ${LOGGING_PROP_FILE}
	
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

for i in ${PROPERTIES_FILE} ${LRM_XML} ${LOGGING_PROP_FILE} ${AAFLOGIN} ${OTHER_FILES} ; do
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
if [ -r ${LRM_XML} ]; then
	${LRM_HOME}/bin/lrmcli -addOrUpgrade -file ${LRM_XML} || fail 1 "Add to LRM Failed"
	${LRM_HOME}/bin/lrmcli -start -name com.att.authz.${artifactId} -version ${version} -routeoffer ${ROUTE_OFFER} | grep SUCCESS
fi


# Note: Must exit 0 or, it will be exit default 1 and fail
exit 0
