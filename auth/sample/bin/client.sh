#!/bin/bash
# This script is run when starting aaf_config Container.
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


# Setup SSO info for Deploy ID
function sso_encrypt() {
 $JAVA -cp /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar org.onap.aaf.cadi.CmdLine digest ${1} ~/.aaf/keyfile
}

if [ ! -e " ~/.aaf/keyfile" ]; then
    mkdir -p ~/.aaf
    SSO=~/.aaf/sso.props
    $JAVA -cp /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar org.onap.aaf.cadi.CmdLine keygen ~/.aaf/keyfile
    chmod 400 ~/.aaf/keyfile
    echo cadi_latitude=${LATITUDE} > ${SSO}
    echo cadi_longitude=${LONGITUDE} >> ${SSO}
    echo aaf_id=${DEPLOY_FQI} >> ${SSO}
    if [ ! "${DEPLOY_PASSWORD}" = "" ]; then
       echo aaf_password=enc:$(sso_encrypt ${DEPLOY_PASSWORD}) >> ${SSO}
    fi
    echo aaf_locate_url=https://${AAF_FQDN}:8095 >> ${SSO}
    echo aaf_url=https://AAF_LOCATE_URL/AAF_NS.service:${AAF_INTERFACE_VERSION} >> ${SSO}
    echo cadi_truststore=$(ls /opt/app/aaf_config/public/*trust*) >> ${SSO}
    echo cadi_truststore_password=enc:$(sso_encrypt changeit) >> ${SSO}
fi

# Only initialize once, automatically...
if [ ! -e /opt/app/osaaf/local/${NS}.props ]; then
    for D in bin logs; do
        rsync -avzh --exclude=.gitignore /opt/app/aaf_config/$D/* /opt/app/osaaf/$D
    done

    # setup Configs
    $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar config $APP_FQI \
        cadi_etc_dir=/opt/app/osaaf/local 

    # Place Certificates
    $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar place ${APP_FQI} ${APP_FQDN}

    # Validate
    $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar validate \
        cadi_prop_files=/opt/app/osaaf/local/${NS}.props
fi

# Now run a command
CMD=$2
if [ ! "$CMD" = "" ]; then
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
            rsync -uh --exclude=.gitignore /opt/app/aaf_config/$D/* /opt/app/osaaf/$D
        done
        ;;
    showpass)
        echo "## Show Passwords"
        $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar showpass ${APP_FQI} ${APP_FQDN}
        ;;
    check)
        $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/${NS}.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar check ${APP_FQI} ${APP_FQDN}
        ;;
    validate)
        echo "## validate requested"
        $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar validate /opt/app/osaaf/local/${NS}.props
        ;;
    bash)
        if [ ! -e ~/.bash_aliases ]; then
            echo "alias cadi='$JAVA -cp /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar org.onap.aaf.cadi.CmdLine \$*'" >~/.bash_aliases
            echo "alias agent='$JAVA -cp /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar org.onap.aaf.cadi.configure.Agent \$*'" >>~/.bash_aliases
        fi
        shift
        cd /opt/app/osaaf/local || exit
        /bin/bash "$@"
        ;;
    setProp)
        cd /opt/app/osaaf/local || exit
        FILES=$(grep -l "$1" ./*.props)
	if [ "$FILES" = "" ]; then 
  	    FILES="$3"
	    ADD=Y
	fi
        for F in $FILES; do
            echo "Changing $1 in $F"
	    if [ "$ADD" = "Y" ]; then
		echo $2 >> $F
	    else 
               sed -i.backup -e "s/\\(${1}.*=\\).*/\\1${2}/" $F
	    fi
            cat $F
        done
        ;;
    encrypt)
        cd /opt/app/osaaf/local || exit
	echo $1
        FILES=$(grep -l "$1" ./*.props)
	if [ "$FILES" = "" ]; then
             FILES=/opt/app/osaaf/local/${NS}.cred.props
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
            PWD=$("$JAVA" -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar cadi digest "$ORIG_PW" /opt/app/osaaf/local/${NS}.keyfile)
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
            $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/${NS}.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar cadi | tail -n +6
            ;;
        agent)
            echo "--- agent Tool Comands ---"
            $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/${NS}.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar
            ;;
        esac
        echo ""
        ;;
    *)
        $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/${NS}.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar "$CMD" "$@"
        ;;
    esac
fi
