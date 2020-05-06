#!/bin/bash
#########
#  ============LICENSE_START====================================================
#  org.onap.aaf
#  ===========================================================================
#  Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#  ===========================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ============LICENSE_END====================================================
#
# This script is run when starting client Container.
#  It needs to cover the cases where the initial data doesn't exist, and when it has already been configured (don't overwrite)
#

#
# error handling.  REQUIRED: if this script fails, it must give non-zero exit value
#
# We exit non-zero with an explanation echod to standard
# out in some situations, like bad input or failed keygen.
# We exit non-zero without explanation in other situations
# like command not found, or file access perms error.
#
# exit without explaining to stdout if some error
set -e

[ -z "$JAVA_HOME" ] && { echo FAILURE: JAVA_HOME is not set; exit 1;}
JAVA=${JAVA_HOME}/bin/java

[ -e ${JAVA_HOME} ] || { echo FAILURE: java home does not exist: ${JAVA_HOME}; exit 1;}
[ -e ${JAVA} ]      || { echo FAILURE: java executable does not exist: ${JAVA}; exit 1;}

AAF_INTERFACE_VERSION=2.1

# Extract Name, Domain and NS from FQI
[ -z "$APP_FQI" ] && { echo FAILURE: APP_FQI is not set; exit 1; }

FQIA=($(echo ${APP_FQI} | tr '@' '\n'))
FQI_SHORT=${FQIA[0]}
FQI_DOMAIN=${FQIA[1]}
[ -z "$FQI_SHORT" ] && { echo FAILURE: malformed APP_FQI, should be like email form: name@domain; exit 1; }
[ -z "$FQI_DOMAIN" ] && { echo FAILURE: malformed APP_FQI, should be like email form: name@domain; exit 1; }

#   Reverse DOMAIN for NS
FQIA_E=($(echo ${FQI_DOMAIN} | tr '.' '\n'))
for (( i=( ${#FQIA_E[@]} -1 ); i>0; i-- )); do
   NS=${NS}${FQIA_E[i]}'.'
done
NS=${NS}${FQIA_E[0]}
CONFIG=${CONFIG:-"/opt/app/aaf_config"}

#  perhaps AAF HOME? (root of aaf installation)
OSAAF=${OSAAF:-"/opt/app/osaaf"}

# this is the 'place' operation's destination
LOCAL=${LOCAL:-"$OSAAF/local"}
DOT_AAF=${DOT_AAF:-"${HOME}/.aaf"}
SSO="$DOT_AAF/sso.props"

# for *backup files
backupDir=${BACKUP_DIR:-${LOCAL}}

if [ -e "$CONFIG" ]; then
  CONFIG_BIN="$CONFIG/bin" 
else 
  CONFIG_BIN="."
fi

AGENT_JAR="$CONFIG_BIN/aaf-cadi-aaf-*-full.jar"

JAVA_AGENT="$JAVA -Dcadi_loglevel=DEBUG -Dcadi_etc_dir=${LOCAL} -Dcadi_log_dir=${LOCAL} -jar $AGENT_JAR "

function backup() {
  # any backup files?
  if stat -t *.backup > /dev/null 2>&1; then
    # move them somewhere else?
    if [ "${backupDir}" != "${LOCAL}" ]; then
      mkdir -p ${backupDir}
      mv -f ${LOCAL}/*.backup ${backupDir}
    fi
  fi
}

# Setup SSO info for Deploy ID
function sso_encrypt() {
   $JAVA_AGENT cadi digest ${1} $DOT_AAF/keyfile || {
      echo agent fails to digest password
      exit 1
   }
}

# Setup Bash, first time only, Agent only
if [ ! -f "$HOME/.bashrc" ] || [ -z "$(grep agent $HOME/.bashrc)" ]; then
  echo "alias agent='$CONFIG_BIN/agent.sh agent \$*'" >> $HOME/.bashrc
  chmod a+x $CONFIG_BIN/agent.sh
  . $HOME/.bashrc
fi

if [ ! -e "$DOT_AAF/truststoreONAPall.jks" ]; then
    mkdir -p $DOT_AAF
    base64 -d $CONFIG/cert/truststoreONAPall.jks.b64 > $DOT_AAF/truststoreONAPall.jks
fi

# Create Deployer Info, located at /root/.aaf
if [ ! -e "$DOT_AAF/keyfile" ]; then

    $JAVA_AGENT cadi keygen $DOT_AAF/keyfile || {
       echo "Cannot create $DOT_AAF/keyfile"
       exit 1
    }

    chmod 400 $DOT_AAF/keyfile

fi

if [ ! -e "${SSO}" ]; then
    echo Creating and adding content to ${SSO}
    echo "cadi_keyfile=$DOT_AAF/keyfile" > ${SSO}

    # Add Deployer Creds to Root's SSO
    DEPLOY_FQI="${DEPLOY_FQI:=$app_id}"
    echo "aaf_id=${DEPLOY_FQI}" >> ${SSO}
    if [ ! "${DEPLOY_PASSWORD}" = "" ]; then
       echo aaf_password=enc:$(sso_encrypt ${DEPLOY_PASSWORD}) >> ${SSO}
    fi
    
    # Cover case where using app.props
    aaf_locator_container_ns=${aaf_locator_container_ns:=$CONTAINER_NS}
    if [ "$aaf_locator_container" = "docker" ]; then
        echo "aaf_locate_url=https://aaf-locate:8095" >> ${SSO}
        echo "aaf_url_cm=https://aaf-cm:8150" >> ${SSO}
        echo "aaf_url=https://aaf-service:8100" >> ${SSO}
    else 
        echo "aaf_locate_url=https://${aaf_locator_fqdn}:8095" >> ${SSO}
        echo "aaf_url_cm=https://AAF_LOCATE_URL/%CNS.%NS.cm:2.1" >> ${SSO}
        echo "aaf_url=https://AAF_LOCATE_URL/%CNS.%NS.service:2.1" >> ${SSO}
    fi

    echo "cadi_truststore=$DOT_AAF/truststoreONAPall.jks" >> ${SSO}
    echo "cadi_truststore_password=changeit" >> ${SSO}
    echo "cadi_latitude=${LATITUDE}" >> ${SSO}
    echo "cadi_longitude=${LONGITUDE}" >> ${SSO}
    echo "hostname=${aaf_locator_fqdn}" >> ${SSO}

    # Push in all AAF and CADI properties to SSO
    for E in $(env); do
        if [ "${E:0:4}" = "aaf_" ] || [ "${E:0:5}" = "cadi_" ]; then
           # Use Deployer ID in ${SSO}
           if [ "app_id" != "${E%=*}" ]; then
              S="${E/_helm/.helm}"
              S="${S/_oom/.oom}"
             echo "$S" >> ${SSO}
           fi
        fi
    done

    . ${SSO}
    echo "Caller Properties Initialized"
    echo "cat SSO"
    cat ${SSO}
fi

# Check for local dir
if [ -d $LOCAL ]; then
    echo "$LOCAL exists"
else
    mkdir -p $LOCAL
    echo "Created $LOCAL"
fi

cd $LOCAL
echo "Existing files in $LOCAL"
ls -l

# Should we refresh the client version??
if [ "${VERSION}" != "$(cat ${LOCAL}/VERSION 2> /dev/null)" ]; then
  echo "Clean up directory ${LOCAL}"
  rm -Rf ${LOCAL}/*

  echo "${VERSION}" > $LOCAL/VERSION
  cp $AGENT_JAR $LOCAL
  echo "#!/bin/bash" > $LOCAL/agent
     echo 'java -jar aaf-cadi-aaf-*-full.jar $*' >> $LOCAL/agent
  echo "#!/bin/bash" > $LOCAL/cadi
     echo 'java -jar aaf-cadi-aaf-*-full.jar cadi $*' >> $LOCAL/cadi
  chmod 755 $LOCAL/agent $LOCAL/cadi
fi

echo "Namespace is ${NS}"

# Only initialize once, automatically...
if [ ! -f $LOCAL/${NS}.props ]; then
    [ -z "$APP_FQDN" ] && { echo FAILURE: APP_FQDN is not set; exit 1; }

    echo "#### Create Configuration files "
    > $LOCAL/$NS
    $JAVA_AGENT config $APP_FQI $APP_FQDN --nopasswd || {
        echo Cannot create config files
        exit 1
    }
    cat $LOCAL/$NS.props

    echo
    echo "#### Certificate Authorization Artifact"
    # TMP=$(mktemp)
    TMP=$LOCAL/agent.log


    $JAVA_AGENT read ${APP_FQI} ${APP_FQDN} | tee $TMP ;  [ ${PIPESTATUS[0]} -eq 0 ] || {
        echo Cannot read artificate;
        exit 1;
    }


    if [ -n "$(grep 'Namespace:' $TMP)" ]; then
        echo "#### Place Certificates (by deployer)"
        $JAVA_AGENT place $APP_FQI $APP_FQDN || {
          echo Failed to obtain new certificate
          exit 1

        }
    
        if [ -z "$(grep cadi_alias ${LOCAL}/$NS.cred.props)" ]; then
	  echo "FAILED to get Certificate, cadi_alias is not defined."
          exit 1
        else 
          echo "Obtained Certificates"
          echo "#### Validate Configuration and Certificate with live call"
          $JAVA_AGENT validate cadi_prop_files=${NS}.props || {
              echo Failed to validate new certificate
              exit 1
          }
        fi
    else
	echo "#### Certificate Authorization Artifact must be valid to continue"
    fi
    rm $TMP    
else
    INITIALIZED="true"
fi

if [ -z "$*" ]; then
    echo "Initialization complete"
else 
    # Now run a command
    CMD=$1
    shift
    case "$CMD" in
    ls)
        echo ls requested
        find ${OSAAF} -depth
        ;;
    cat)
        if [ "$1" = "" ]; then
            echo "usage: cat <file... ONLY files ending in .props>"
        else
            if [[ $1 == *.props ]]; then
                echo
                echo "## CONTENTS OF $3"
                echo
                cat "$1"
            else
                echo "### ERROR ####"
                echo "   \"cat\" may only be used with files ending with \".props\""
            fi
        fi
        ;;
    read)
        echo "## Read Artifacts"
        $JAVA_AGENT read $APP_FQI $APP_FQDN cadi_prop_files=${SSO} cadi_loglevel=INFO || {
           echo Command faile, cannot read artifacts
           exit 1
        }
        ;;
    showpass)
        echo "## Show Passwords"
        $JAVA_AGENT showpass $APP_FQI $APP_FQDN cadi_prop_files=${SSO} cadi_loglevel=ERROR || {
           echo Failure showing password
           exit 1
        }
        ;;
    check)
        echo "## Check Certificate"
        echo "$JAVA_AGENT check $APP_FQI $APP_FQDN cadi_prop_files=${LOCAL}/${NS}.props"
	# inspects and repots on certificate validation and renewal date
        $JAVA_AGENT check $APP_FQI $APP_FQDN cadi_prop_files=${LOCAL}/${NS}.props || {
           echo Checking certificate fails.
           exit 1
        }
        ;;
    validate)
        echo "## validate requested"
	# attempt to send request to aaf; authenticate with this local certificate
        $JAVA_AGENT validate $APP_FQI $APP_FQDN || {
           echo Validation fails.
           exit 1
        }
        ;;
    place)
        echo "## Renew Certificate"
        $JAVA_AGENT place $APP_FQI $APP_FQDN cadi_prop_files=${SSO} || {
           echo Placing certificate fails.
           exit 1
        }
        ;;
    renew)
        echo "## Renew Certificate"
        $JAVA_AGENT place $APP_FQI $APP_FQDN || {
           echo Failure renewing certificate
           exit 1
        }
        ;;
    bash)
        shift
        cd $LOCAL || exit
        exec bash "$@" 
        ;;
    setProp)
        cd $LOCAL || exit
        FILES=$(grep -l "$1" ./*.props)
	if [ -z "$FILES" ]; then 
  	    if [ -z "$3" ]; then
               FILES=${NS}.props
            else 
               FILES="$3"
            fi
	    ADD=Y
	fi
        for F in $FILES; do
	    if [ "$ADD" = "Y" ]; then
                echo "Changing $1 to $F"
		echo "$1=$2" >> $F
	    else 
               echo "Changing $1 in $F"
               sed -i.backup -e "s/\\(${1}.*=\\).*/\\1${2}/" $F
	    fi
            cat $F
        done
        ;;
    encrypt)
        cd $LOCAL || exit
	echo $1
        FILES=$(grep -l "$1" ./*.props)
	if [ "$FILES" = "" ]; then
             FILES=$LOCAL/${NS}.cred.props
	     ADD=Y
        fi
        for F in $FILES; do
            echo "Changing $1 in $F"
            if [ "$2" = "" ]; then
                read -r -p "Password (leave blank to cancel): " -s ORIG_PW
                echo " "
                if [ "$ORIG_PW" = "" ]; then
                    echo canceling...
                    break
                fi
            else
                ORIG_PW="$2"
            fi
            PWD=$($JAVA_CADI digest "$ORIG_PW" $LOCAL/${NS}.keyfile)
            if [ "$ADD" = "Y" ]; then
                  echo "$1=enc:$PWD" >> $F
            else 
            	sed -i.backup -e "s/\\($1.*enc:\\).*/\\1$PWD/" $F
	   fi
            cat $F
        done
        ;;
    taillog) 
	sh ${OSAAF}/logs/taillog
	;;
    testConnectivity|testconnectivity)
        echo "--- Test Connectivity ---"
        $JAVA -cp $AGENT_JAR org.onap.aaf.cadi.aaf.TestConnectivity $LOCAL/${NS}.props  || {
           echo Failure while testing connectivity
           exit 1
        }
	;;
    --help | -?)
        case "$1" in
        "")
            echo "--- Agent Container Comands ---"
            echo "  ls                      - Lists all files in Configuration"
            echo "  cat <file.props>>       - Shows the contents (Prop files only)"
            echo "  validate                - Runs a test using Configuration"
            echo "  setProp <tag> [<value>] - set value on 'tag' (if no value, it will be queried from config)"
            echo "  encrypt <tag> [<pass>]  - set passwords on Configuration (if no pass, it will be queried)"
            echo "  bash                    - run bash in Container"
            echo "     Note: the following aliases are preset"
            echo "       cadi               - CADI CmdLine tool"
            echo "       agent              - Agent Java tool (see above help)"
            echo ""
            echo " --help|-? [cadi|agent]   - This help, cadi help or agent help"
            ;;
        cadi)
            echo "--- cadi Tool Comands ---"
            $JAVA_CADI
            ;;
        agent)
            echo "--- agent Tool Comands ---"
            $JAVA_AGENT
            ;;
        aafcli)
            echo "--- aafcli Tool Comands ---"
            $JAVA_AAFCLI
            ;;
        esac
        echo ""
        ;;
    ### Possible Dublin
    # sample)
    #    echo "--- run Sample Servlet App ---"
    #    $JAVA -Dcadi_prop_files=$LOCAL/${NS}.props -cp $AGENT_JAR:$CONFIG_BIN/aaf-cadi-servlet-sample-*-sample.jar org.onap.aaf.sample.cadi.jetty.JettyStandalone ${NS}.props
    #    ;;
    *)
        $JAVA_AGENT "$CMD" "$@"
        ;;
    esac
fi

backup
