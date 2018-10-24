#!/bin/bash
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
 
JAVA_CADI="$JAVA -cp $CONFIG/bin/aaf-auth-cmd-*-full.jar org.onap.aaf.cadi.CmdLine"
JAVA_AGENT="$JAVA -cp $CONFIG/bin/aaf-auth-cmd-*-full.jar -Dcadi_prop_files=$SSO org.onap.aaf.cadi.configure.Agent"
JAVA_AGENT_SELF="$JAVA -cp $CONFIG/bin/aaf-auth-cmd-*-full.jar -Dcadi_prop_files=$LOCAL/${NS}.props org.onap.aaf.cadi.configure.Agent"
JAVA_AAFCLI="$JAVA -cp $CONFIG/bin/aaf-auth-cmd-*-full.jar -Dcadi_prop_files=$LOCAL/org.osaaf.aaf.props org.onap.aaf.auth.cmd.AAFcli"

# Check for local dir
if [ ! -d $LOCAL ]; then
    mkdir -p $LOCAL
    for D in bin logs; do
        rsync -avzh --exclude=.gitignore $CONFIG/$D/* /opt/app/osaaf/$D
    done
fi

# Setup Bash, first time only
if [ ! -e "$HOME/.bash_aliases" ] || [ -z "$(grep agent $HOME/.bash_aliases)" ]; then
  echo "alias cadi='$JAVA_CADI \$*'" >>$HOME/.bash_aliases
  echo "alias agent='$OSAAF/bin/agent.sh EMPTY \$*'" >>$HOME/.bash_aliases
  echo "alias aafcli='$JAVA_AAFCLI \$*'" >>$HOME/.bash_aliases
  chmod a+x $OSAAF/bin/agent.sh
  . $HOME/.bash_aliases
fi

# Setup SSO info for Deploy ID
function sso_encrypt() {
   $JAVA_CADI digest ${1} $DOT_AAF/keyfile
}


# Create Deployer Info, located at /root/.aaf
if [ ! -e "$DOT_AAF/keyfile" ]; then
    mkdir -p $DOT_AAF
    $JAVA_CADI keygen $DOT_AAF/keyfile
    chmod 400 $DOT_AAF/keyfile
    echo cadi_latitude=${LATITUDE} > ${SSO}
    echo cadi_longitude=${LONGITUDE} >> ${SSO}
    echo aaf_id=${DEPLOY_FQI} >> ${SSO}
    if [ ! "${DEPLOY_PASSWORD}" = "" ]; then
       echo aaf_password=enc:$(sso_encrypt ${DEPLOY_PASSWORD}) >> ${SSO}
    fi
    echo aaf_locate_url=https://${AAF_FQDN}:8095 >> ${SSO}
    echo aaf_url=https://AAF_LOCATE_URL/AAF_NS.service:${AAF_INTERFACE_VERSION} >> ${SSO}

    base64 -d $CONFIG/cert/truststoreONAPall.jks.b64 > $DOT_AAF/truststoreONAPall.jks
    echo "cadi_truststore=$DOT_AAF/truststoreONAPall.jks" >> ${SSO}
    echo cadi_truststore_password=enc:$(sso_encrypt changeit) >> ${SSO}
    echo "Caller Properties Initialized"
    INITIALIZED="true"
fi

# Only initialize once, automatically...
if [ ! -e $LOCAL/${NS}.props ]; then
    echo "#### Create Configuration files "
    $JAVA_AGENT config $APP_FQI \
	aaf_url=https://AAF_LOCATE_URL/AAF_NS.locate:${AAF_INTERFACE_VERSION} \
        cadi_etc_dir=$LOCAL
    cat $LOCAL/$NS.props

    echo
    echo "#### Certificate Authorization Artifact"
    TMP=$(mktemp)
    $JAVA_AGENT read ${APP_FQI} ${APP_FQDN} \
        cadi_prop_files=${SSO} \
        cadi_etc_dir=$LOCAL > $TMP
    cat $TMP
    echo
    if [ -n "$(grep 'Namespace:' $TMP)" ]; then
        echo "#### Place Certificates (by deployer)"
        $JAVA_AGENT place ${APP_FQI} ${APP_FQDN} \
            cadi_prop_files=${SSO} \
            cadi_etc_dir=$LOCAL
    
        echo "#### Validate Configuration and Certificate with live call"
        $JAVA_AGENT_SELF validate 
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
      $JAVA_AGENT
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
    update)
        for D in bin logs; do
            rsync -uh --exclude=.gitignore $CONFIG/$D/* /opt/app/osaaf/$D
        done
        ;;
    showpass)
        echo "## Show Passwords"
        $JAVA_AGENT showpass ${APP_FQI} ${APP_FQDN}
        ;;
    check)
        $JAVA_AGENT check ${APP_FQI} ${APP_FQDN}
        ;;
    validate)
        echo "## validate requested"
        $JAVA_AGENT_SELF validate 
        ;;
    bash)
        shift
        cd $LOCAL || exit
        /bin/bash "$@"
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
    #    $JAVA -Dcadi_prop_files=$LOCAL/${NS}.props -cp $CONFIG/bin/aaf-auth-cmd-*-full.jar:$CONFIG/bin/aaf-cadi-servlet-sample-*-sample.jar org.onap.aaf.sample.cadi.jetty.JettyStandalone ${NS}.props
    #    ;;
    *)
        $JAVA_AGENT "$CMD" "$@"
        ;;
    esac
fi
