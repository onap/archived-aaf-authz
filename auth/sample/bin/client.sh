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
JAVA=${JAVA_HOME}/bin/java
AAF_INTERFACE_VERSION=2.1

# Extract Name, Domain and NS from FQI
FQIA=($(echo ${APP_FQI} | tr '@' '\n'))
FQI_SHORT=${FQIA[0]}
FQI_DOMAIN=${FQIA[1]}
#   Reverse DOMAIN for NS
FQIA_E=($(echo ${FQI_DOMAIN} | tr '.' '\n'))
for (( i=( ${#FQIA_E[@]} -1 ); i>0; i-- )); do
   NS=${NS}${FQIA_E[i]}'.'
done
NS=${NS}${FQIA_E[0]}
CONFIG="/opt/app/aaf_config"
OSAAF="/opt/app/osaaf"
LOCAL="$OSAAF/local"
DOT_AAF="$HOME/.aaf"
SSO="$DOT_AAF/sso.props"

if [ -e "$CONFIG" ]; then
  CONFIG_BIN="$CONFIG/bin" 
else 
  CONFIG_BIN="."
fi

AGENT_JAR="$CONFIG_BIN/aaf-cadi-aaf-*-full.jar"

JAVA_AGENT="$JAVA -Dcadi_loglevel=DEBUG -Dcadi_etc_dir=${LOCAL} -Dcadi_log_dir=${LOCAL} -jar $AGENT_JAR "

# Setup SSO info for Deploy ID
function sso_encrypt() {
   $JAVA_AGENT cadi digest ${1} $DOT_AAF/keyfile
}

# Setup Bash, first time only
if [ ! -e "$HOME/.bashrc" ] || [ -z "$(grep agent $HOME/.bashrc)" ]; then
  echo "alias agent='$CONFIG_BIN/agent.sh agent \$*'" >>$HOME/.bashrc
  chmod a+x $CONFIG_BIN/agent.sh
  . $HOME/.bashrc
fi
if [ ! -e "$DOT_AAF/truststoreONAPall.jks" ]; then
    mkdir -p $DOT_AAF
    base64 -d $CONFIG/cert/truststoreONAPall.jks.b64 > $DOT_AAF/truststoreONAPall.jks
fi

# Create Deployer Info, located at /root/.aaf
if [ ! -e "$DOT_AAF/keyfile" ]; then
    $JAVA_AGENT cadi keygen $DOT_AAF/keyfile
    chmod 400 $DOT_AAF/keyfile
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
        echo "aaf_locate_url=https://$aaf-locator.${CONTAINER_NS}:8095" >> ${SSO}
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
    INITIALIZED="true"
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

# Should we clean up?
if [ "${VERSION}" != "$(cat ${LOCAL}/VERSION 2> /dev/null)" ]; then
  echo "Clean up directory ${LOCAL}"
  rm -Rf ${LOCAL}/*
fi
echo "${VERSION}" > $LOCAL/VERSION

echo "Namespace is ${NS}"
# Only initialize once, automatically...
if [ ! -e $LOCAL/${NS}.props ]; then
    echo "#### Create Configuration files "
    $JAVA_AGENT config $APP_FQI $APP_FQDN 
    cat $LOCAL/$NS.props

    echo
    echo "#### Certificate Authorization Artifact"
    # TMP=$(mktemp)
    TMP=$LOCAL/agent.log
    $JAVA_AGENT read ${APP_FQI} ${APP_FQDN} | tee $TMP

    if [ -n "$(grep 'Namespace:' $TMP)" ]; then
        echo "#### Place Certificates (by deployer)"
        $JAVA_AGENT place $APP_FQI $APP_FQDN
    
        if [ -z "$(grep cadi_alias $NS.cred.props)" ]; then
	    echo "FAILED to get Certificate"
          INITIALIZED="false"
        else 
          echo "Obtained Certificates"
          echo "#### Validate Configuration and Certificate with live call"
          $JAVA_AGENT validate cadi_prop_files=${NS}.props
          INITIALIZED="true"
        fi
    else
	echo "#### Certificate Authorization Artifact must be valid to continue"
    fi
    rm $TMP    
else
    INITIALIZED="true"
fi

# Now run a command
CMD=$2
if [ -z "$CMD" ]; then
    if [ -n "$INITIALIZED" ]; then
      echo "Initialization complete"
    fi
else 
    shift
    shift
    case "$CMD" in
    ls)
        echo ls requested
        find /opt/app/osaaf -depth
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
        $JAVA_AGENT read $APP_FQI $APP_FQDN cadi_prop_files=${SSO} cadi_loglevel=INFO
        ;;
    showpass)
        echo "## Show Passwords"
        $JAVA_AGENT showpass $APP_FQI $APP_FQDN cadi_prop_files=${SSO} cadi_loglevel=ERROR
        ;;
    check)
        echo "## Check Certificate"
        echo "$JAVA_AGENT check $APP_FQI $APP_FQDN cadi_prop_files=${LOCAL}/${NS}.props"
        $JAVA_AGENT check $APP_FQI $APP_FQDN cadi_prop_files=${LOCAL}/${NS}.props
        ;;
    validate)
        echo "## validate requested"
        $JAVA_AGENT validate $APP_FQI $APP_FQDN
        ;;
    place)
        echo "## Renew Certificate"
        $JAVA_AGENT place $APP_FQI $APP_FQDN cadi_prop_files=${SSO}
        ;;
    renew)
        echo "## Renew Certificate"
        $JAVA_AGENT place $APP_FQI $APP_FQDN
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
	sh /opt/app/osaaf/logs/taillog
	;;
    testConnectivity|testconnectivity)
        echo "--- Test Connectivity ---"
        $JAVA -cp $CONFIG_BIN/aaf-auth-cmd-*-full.jar org.onap.aaf.cadi.aaf.TestConnectivity $LOCAL/org.osaaf.aaf.props 
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
    #    $JAVA -Dcadi_prop_files=$LOCAL/${NS}.props -cp $CONFIG_BIN/aaf-auth-cmd-*-full.jar:$CONFIG_BIN/aaf-cadi-servlet-sample-*-sample.jar org.onap.aaf.sample.cadi.jetty.JettyStandalone ${NS}.props
    #    ;;
    *)
        $JAVA_AGENT "$CMD" "$@"
        ;;
    esac
fi
