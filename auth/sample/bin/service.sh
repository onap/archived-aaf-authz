#!/bin/bash
# This script is run when starting aaf_config Container.
#  It needs to cover the cases where the initial data doesn't exist, and when it has already been configured (don't overwrite)
#
JAVA=/usr/bin/java
LOCAL=/opt/app/osaaf/local
DATA=/opt/app/osaaf/data
PUBLIC=/opt/app/osaaf/public
CONFIG=/opt/app/aaf_config
# Temp use for clarity of code
FILE=

# Setup Bash, first time only
if [ ! -e "$HOME/.bash_aliases" ] || [ -z "$(grep aaf_config $HOME/.bash_aliases)" ]; then
  echo "alias cadi='$CONFIG/bin/agent.sh EMPTY cadi \$*'" >>$HOME/.bash_aliases
  echo "alias agent='$CONFIG/bin/agent.sh EMPTY \$*'" >>$HOME/.bash_aliases
  chmod a+x $CONFIG/bin/agent.sh
  . $HOME/.bash_aliases
fi

# Only load Identities once
# echo "Check Identities"
FILE="$DATA/identities.dat"
if [ ! -e $FILE ]; then
    mkdir -p $DATA
    cp $CONFIG/data/sample.identities.dat $FILE
fi

# Load up Cert/X509 Artifacts
# echo "Check Signer Keyfile"
FILE="$LOCAL/org.osaaf.aaf.signer.p12"
if [ ! -e $FILE ]; then
    mkdir -p $LOCAL
    mkdir -p $PUBLIC
    if [ -e $CONFIG/cert/org.osaaf.aaf.signer.p12 ]; then
        cp $CONFIG/cert/org.osaaf.aaf.signer.p12 $FILE
    else
        echo "Decode"
        base64 -d $CONFIG/cert/demoONAPsigner.p12.b64 > $FILE
	base64 -d $CONFIG/cert/truststoreONAP.p12.b64 > $PUBLIC/truststoreONAP.p12 
	base64 -d $CONFIG/cert/truststoreONAPall.jks.b64 > $PUBLIC/truststoreONAPall.jks
	ln -s $PUBLIC/truststoreONAPall.jks $LOCAL
	cp $CONFIG/cert/AAF_RootCA.cer $PUBLIC
	CM_TRUST_CAS="$PUBLIC/AAF_RootCA.cer"
	echo "cadi_keystore_password=something easy" >> $CONFIG/local/aaf.props        
    fi
fi

# echo "Check keyfile"
FILE="$LOCAL/org.osaaf.aaf.p12"
if [ ! -e $FILE ]; then
    if [ -e $CONFIG/cert/org.osaaf.aaf.p12 ]; then
        cp $CONFIG/cert/org.osaaf.aaf.p12 $FILE
    else
        echo "Bootstrap Creation of Keystore from Signer"
        cd $CONFIG/CA
	
        # Remove this after Casablanca
	CADI_X509_ISSUERS="CN=intermediateCA_1, OU=OSAAF, O=ONAP, C=US:CN=intermediateCA_7, OU=OSAAF, O=ONAP, C=US"
	bash bootstrap.sh $LOCAL/org.osaaf.aaf.signer.p12 'something easy'
	cp aaf.bootstrap.p12 $FILE
	if [ -n "$CADI_X509_ISSUERS" ]; then
            CADI_X509_ISSUERS="$CADI_X509_ISSUERS:"
        fi
	BOOT_ISSUER="$(cat aaf.bootstrap.issuer)"
	CADI_X509_ISSUERS="$CADI_X509_ISSUERS$BOOT_ISSUER"

	I=${BOOT_ISSUER##CN=};I=${I%%,*}
        CM_CA_PASS="something easy"
        CM_CA_LOCAL="org.onap.aaf.auth.cm.ca.LocalCA,$LOCAL/org.osaaf.aaf.signer.p12;aaf_intermediate_9;enc:"
	CM_TRUST_CAS="$PUBLIC/AAF_RootCA.cer"
    fi
fi

# Only initialize once, automatically...
if [ ! -e $LOCAL/org.osaaf.aaf.props ]; then
    rsync -avzh --exclude=.gitignore $CONFIG/local/org.osaaf.aaf* $LOCAL
    for D in public etc logs; do
        rsync -avzh --exclude=.gitignore $CONFIG/$D/* /opt/app/osaaf/$D
    done

    TMP=$(mktemp)
    echo aaf_env=${AAF_ENV} >> ${TMP}
    echo cadi_latitude=${LATITUDE} >> ${TMP}
    echo cadi_longitude=${LONGITUDE} >> ${TMP}
    echo cadi_x509_issuers=${CADI_X509_ISSUERS} >> ${TMP}
    echo aaf_register_as=${AAF_REGISTER_AS} >> ${TMP}
    echo aaf_locate_url=https://${AAF_REGISTER_AS}:8095 >> ${TMP}

    cat $TMP

    $JAVA -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar config aaf@aaf.osaaf.org \
        cadi_etc_dir=$LOCAL \
        cadi_prop_files=$CONFIG/local/initialConfig.props:$CONFIG/local/aaf.props:${TMP}
    rm ${TMP}
    # Default Password for Default Cass
    CASS_PASS=$("$JAVA" -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar cadi digest "cassandra" $LOCAL/org.osaaf.aaf.keyfile)
    sed -i.backup -e "s/\\(cassandra.clusters.password=enc:\\)/\\1$CASS_PASS/" $LOCAL/org.osaaf.aaf.cassandra.props

    if [ -n "$CM_CA_LOCAL" ]; then
      if [ -n "$CM_CA_PASS" ]; then
          CM_CA_LOCAL=$CM_CA_LOCAL$("$JAVA" -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar cadi digest "$CM_CA_PASS" $LOCAL/org.osaaf.aaf.keyfile)	
      fi
      # Move and copy method, rather than sed, because of slashes in CM_CA_LOCAL makes too complex
      FILE=$LOCAL/org.osaaf.aaf.cm.ca.props
      mv $FILE $FILE.backup
      grep -v "cm_ca.local=" $FILE.backup > $FILE
      echo "cm_ca.local=$CM_CA_LOCAL" >> $FILE
      echo "cm_trust_cas=$CM_TRUST_CAS" >> $FILE
    fi
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
        rsync -uh --exclude=.gitignore $CONFIG/local/org.osaaf.aaf* $LOCAL
        for D in public data etc logs; do
            rsync -uh --exclude=.gitignore $CONFIG/$D/* /opt/app/osaaf/$D
        done
        ;;
    validate)
        echo "## validate requested"
        $JAVA -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar validate cadi_prop_files=$LOCAL/org.osaaf.aaf.props
        ;;
    onap)
        echo Initializing ONAP configurations.
	;;
    bash)
        shift
        cd $LOCAL || exit
        /bin/bash "$@"
        ;;
    setProp)
        cd $LOCAL || exit
        FILES=$(grep -l "$1" ./*.props)
	if [ "$FILES" = "" ]; then 
  	    FILES="$3"
	    ADD=Y
	fi
        for F in $FILES; do
            echo "Changing $1 in $F"
	    if [ "$ADD" = "Y" ]; then
		echo "$1=$2" >> $F
	    else 
		VALUE=${2//\//\\\/}
                sed -i.backup -e "s/\(${1}=\).*/\1${VALUE}/" $F
	    fi
            cat $F
        done
        ;;
    encrypt)
        cd $LOCAL || exit
	echo $1
        FILES=$(grep -l "$1" ./*.props)
	if [ "$FILES" = "" ]; then
             FILES=$LOCAL/org.osaaf.aaf.cred.props
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
            PWD=$("$JAVA" -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar cadi digest "$ORIG_PW" $LOCAL/org.osaaf.aaf.keyfile)
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
            $JAVA -Dcadi_prop_files=$LOCAL/org.osaaf.aaf.props -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar cadi | tail -n +6
            ;;
        agent)
            echo "--- agent Tool Comands ---"
            $JAVA -Dcadi_prop_files=$LOCAL/org.osaaf.aaf.props -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar
            ;;
        esac
        echo ""
        ;;
    *)
        $JAVA -Dcadi_prop_files=$LOCAL/org.osaaf.aaf.props -jar $CONFIG/bin/aaf-cadi-aaf-*-full.jar "$CMD" "$@"
        ;;
    esac
fi
