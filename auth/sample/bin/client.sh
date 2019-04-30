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
JAVA=/usr/bin/java
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

CLPATH="$CONFIG_BIN/aaf-auth-cmd-*-full.jar"

JAVA_CADI="$JAVA -cp $CLPATH org.onap.aaf.cadi.CmdLine"
JAVA_AGENT="$JAVA -cp $CLPATH -Dcadi_prop_files=$SSO org.onap.aaf.cadi.configure.Agent"
JAVA_AGENT_SELF="$JAVA -cp $CLPATH -Dcadi_prop_files=$LOCAL/${NS}.props org.onap.aaf.cadi.configure.Agent"
JAVA_AAFCLI="$JAVA -cp $CLPATH -Dcadi_prop_files=$LOCAL/org.osaaf.aaf.props org.onap.aaf.auth.cmd.AAFcli"

# Check for local dir
if [ ! -d $LOCAL ]; then
    mkdir -p $LOCAL
    for D in bin logs; do
        mkdir -p $OSAAF/$D
        cp $CONFIG/$D/* $OSAAF/$D
    done
fi

# Setup Bash, first time only
if [ ! -e "$HOME/.bashrc" ] || [ -z "$(grep cadi $HOME/.bashrc)" ]; then
  echo "alias cadi='$JAVA_CADI \$*'" >>$HOME/.bashrc
  echo "alias agent='$CONFIG_BIN/agent.sh agent \$*'" >>$HOME/.bashrc
  echo "alias aafcli='$JAVA_AAFCLI \$*'" >>$HOME/.bashrc
  chmod a+x $CONFIG_BIN/agent.sh
  . $HOME/.bashrc
fi

# Setup SSO info for Deploy ID
function sso_encrypt() {
   $JAVA_CADI digest ${1} $DOT_AAF/keyfile
}

if [ ! -e "$DOT_AAF/truststoreONAPall.jks" ]; then
    mkdir -p $DOT_AAF
    base64 -d $CONFIG/cert/truststoreONAPall.jks.b64 > $DOT_AAF/truststoreONAPall.jks
fi

# Create Deployer Info, located at /root/.aaf
if [ ! -e "$DOT_AAF/keyfile" ]; then
    $JAVA_CADI keygen $DOT_AAF/keyfile
    chmod 400 $DOT_AAF/keyfile

    # Add Deployer Creds to Root's SSO
    DEPLOY_FQI="${DEPLOY_FQI:=$app_id}"
    echo "aaf_id=${DEPLOY_FQI}" > ${SSO}
    if [ ! "${DEPLOY_PASSWORD}" = "" ]; then
       echo aaf_password=enc:$(sso_encrypt ${DEPLOY_PASSWORD}) >> ${SSO}
    fi
    
    # Cover case where using app.props
    aaf_locater_container_ns=${aaf_locator_container_ns:=$CONTAINER_NS} 

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

    echo "cadi_truststore=$DOT_AAF/truststoreONAPall.jks" >> ${SSO}
    echo cadi_truststore_password=enc:$(sso_encrypt changeit) >> ${SSO}
    echo "Caller Properties Initialized"
    INITIALIZED="true"
fi
echo "cat SSO"
cat ${SSO}

# Should we clean up?
if [ "${VERSION}" != "$(cat ${LOCAL}/VERSION)" ]; then
  echo "Clean up directory ${LOCAL}"
  rm -Rf ${LOCAL}/*
fi
echo "${VERSION}" > $LOCAL/VERSION

# Only initialize once, automatically...
if [ ! -e $LOCAL/${NS}.props ]; then
    if [ -e '/opt/app/aaf_config/bin' ]; then
      cp /opt/app/aaf_config/bin/*.jar $LOCAL
      echo "#!/bin/bash" > agent
      echo 'case "$1" in' >> agent
      echo '  ""|-?|--help)CMD="";FQI="";FQDN="";;' >> agent
      echo '  validate)CMD="$1";FQI="";FQDN="${2:-'"$NS.props"'}";;' >> agent
      echo '    *)CMD="$1";FQI="${2:-'"$APP_FQI"'}";FQDN="${3:-'"$APP_FQDN"'}";;' >> agent
      echo 'esac' >> agent
      echo 'java -cp '$(ls aaf-auth-cmd-*-full.jar)' -Dcadi_prop_files='"$NS"'.props org.onap.aaf.cadi.configure.Agent $CMD $FQI $FQDN' >> agent

      echo "#!/bin/bash" > cadi
      echo "java -cp $(ls aaf-auth-cmd-*-full.jar) -Dcadi_prop_files=$NS.props org.onap.aaf.cadi.CmdLine " '$*' >> cadi
      # echo "#!/bin/bash" > aafcli
      # echo "java -cp $(ls aaf-auth-cmd-*-full.jar) -Dcadi_prop_files=$NS.props org.onap.aaf.auth.cmd.AAFcli " '$*' >> aafcli

      echo "#!/bin/bash" > testConnectivity
      echo "java -cp $(ls aaf-auth-cmd-*-full.jar) org.onap.aaf.cadi.aaf.TestConnectivity $NS.props" >> testConnectivity
      chmod ug+x agent cadi testConnectivity
    fi

    echo "#### Create Configuration files "
    $JAVA_AGENT config $APP_FQI \
        cadi_etc_dir=$LOCAL \
        cadi_prop_files=$SSO
	#aaf_url=https://AAF_LOCATE_URL/AAF_NS.locate:${AAF_INTERFACE_VERSION} 
    cat $LOCAL/$NS.props

    echo
    echo "#### Certificate Authorization Artifact"
    # TMP=$(mktemp)
    TMP=$LOCAL/agent.log
    $JAVA_AGENT read ${APP_FQI} ${APP_FQDN} \
        cadi_prop_files=${SSO} \
        cadi_etc_dir=$LOCAL | tee $TMP

    if [ -n "$(grep 'Namespace:' $TMP)" ]; then
        echo "#### Place Certificates (by deployer)"
        $JAVA_AGENT place ${APP_FQI} ${APP_FQDN} \
            cadi_prop_files=${SSO} \
            cadi_etc_dir=$LOCAL
    
        echo "#### Validate Configuration and Certificate with live call"
        echo "Obtained Certificates"
        INITIALIZED="true"
    else
	echo "#### Certificate Authorization Artifact must be valid to continue"
    fi
    rm $TMP    
fi

# Now run a command
CMD=$2
if [ -z "$CMD" ]; then
    if [ -n "$INITIALIZED" ]; then
      echo "Initialization complete"
    else
        $JAVA_AGENT_SELF validate $FQI $FQDN
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
    showpass)
        echo "## Show Passwords"
        $JAVA_AGENT showpass ${APP_FQI} ${APP_FQDN}
        ;;
    check)
        echo "## Check Certificate"
        $JAVA_AGENT check ${APP_FQI} ${APP_FQDN}
        ;;
    validate)
        echo "## validate requested"
        $JAVA_AGENT_SELF validate $FQI $FQDN
        ;;
    renew)
        echo "## Renew Certificate"
        $JAVA_AGENT place ${APP_FQI} ${APP_FQDN}
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
