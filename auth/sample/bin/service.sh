#!/bin/bash
# This script is run when starting aaf_config Container.
#  It needs to cover the cases where the initial data doesn't exist, and when it has already been configured (don't overwrite)
#
JAVA=/usr/bin/java

# Only load Identities once
if [ ! -e /opt/app/osaaf/data/identities.dat ]; then
    mkdir -p /opt/app/osaaf/data
    cp /opt/app/aaf_config/data/sample.identities.dat /opt/app/osaaf/data/identities.dat
fi

# Only initialize once, automatically...
if [ ! -e /opt/app/osaaf/local/org.osaaf.aaf.props ]; then
    rsync -avzh --exclude=.gitignore /opt/app/aaf_config/local/org.osaaf.aaf* /opt/app/osaaf/local
    for D in public etc logs; do
        rsync -avzh --exclude=.gitignore /opt/app/aaf_config/$D/* /opt/app/osaaf/$D
    done

    TMP=$(mktemp)
    echo aaf_env=${AAF_ENV} >> ${TMP}
    echo cadi_latitude=${LATITUDE} >> ${TMP}
    echo cadi_longitude=${LONGITUDE} >> ${TMP}
    echo aaf_register_as=${AAF_REGISTER_AS} >> ${TMP}
    echo aaf_locate_url=https://${AAF_REGISTER_AS}:8095 >> ${TMP}

    $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar config aaf@aaf.osaaf.org \
        cadi_etc_dir=/opt/app/osaaf/local \
        cadi_prop_files=/opt/app/aaf_config/local/initialConfig.props:/opt/app/aaf_config/local/aaf.props:${TMP}
    rm ${TMP}
    # Default Password for Default Cass
    CASS_PASS=$("$JAVA" -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar cadi digest "cassandra" /opt/app/osaaf/local/org.osaaf.aaf.keyfile)
    sed -i.backup -e "s/\\(cassandra.clusters.password=enc:\\)/\\1$CASS_PASS/" /opt/app/osaaf/local/org.osaaf.aaf.cassandra.props
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
        rsync -uh --exclude=.gitignore /opt/app/aaf_config/local/org.osaaf.aaf* /opt/app/osaaf/local
        for D in public data etc logs; do
            rsync -uh --exclude=.gitignore /opt/app/aaf_config/$D/* /opt/app/osaaf/$D
        done
        ;;
    validate)
        echo "## validate requested"
        $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar validate cadi_prop_files=/opt/app/osaaf/local/org.osaaf.aaf.props
        ;;
    bash)
        echo "alias agent='/bin/bash /opt/app/aaf_config/bin/agent.sh EMPTY \$*'" >>~/.bashrc
        if [ ! "$(grep aaf_config ~/.bashrc)" = "" ]; then
            echo "alias cadi='/bin/bash /opt/app/aaf_config/bin/agent.sh EMPTY cadi \$*'" >>~/.bashrc
            echo "alias agent='/bin/bash /opt/app/aaf_config/bin/agent.sh EMPTY \$*'" >>~/.bashrc
            #. ~/.bashrc
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
		echo "$1=$2" >> $F
	    else 
		VALUE=${2//\//\\\/}
                sed -i.backup -e "s/\(${1}=\).*/\1\"${VALUE}\"/" $F
	    fi
            cat $F
        done
        ;;
    encrypt)
        cd /opt/app/osaaf/local || exit
	echo $1
        FILES=$(grep -l "$1" ./*.props)
	if [ "$FILES" = "" ]; then
             FILES=/opt/app/osaaf/local/org.osaaf.aaf.cred.props
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
            PWD=$("$JAVA" -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar cadi digest "$ORIG_PW" /opt/app/osaaf/local/org.osaaf.aaf.keyfile)
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
            $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/org.osaaf.aaf.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar cadi | tail -n +6
            ;;
        agent)
            echo "--- agent Tool Comands ---"
            $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/org.osaaf.aaf.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar
            ;;
        esac
        echo ""
        ;;
    *)
        $JAVA -Dcadi_prop_files=/opt/app/osaaf/local/org.osaaf.aaf.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar "$CMD" "$@"
        ;;
    esac
fi
