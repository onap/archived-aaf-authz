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
  for D in local; do
    rsync -avzh /opt/app/aaf_config/$D/org.osaaf.aaf* /opt/app/osaaf/$D
  done
  for D in public etc logs; do 
     rsync -avzh --exclude=.gitignore /opt/app/aaf_config/$D/* /opt/app/osaaf/$D
  done
  $JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar config osaaf@aaf.osaaf.org \
    cadi_etc_dir=/opt/app/osaaf/local \
    cadi_prop_files=/opt/app/aaf_config/local/initialConfig.props:/opt/app/aaf_config/local/aaf.props \
    cadi_latitude=38.4329 \
    cadi_longitude=-90.43248
  #cp /opt/app/aaf_config/
else
  CMD=$2
  shift
  if [ "$CMD" = "" ]; then
    echo "AAF already configured for this Volume"
  else
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
            cat $1
 	  else
	    echo "### ERROR ####"
	    echo "   \"cat\" may only be used with files ending with \".props\""
	  fi
        fi
	;;	
      update)
        for D in public data etc local logs; do 
          rsync -uh --exclude=.gitignore /opt/app/aaf_config/$D /opt/app/osaaf
        done
	;;
      validate)
	echo "## validate requested"
	$JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar validate cadi_prop_files=/opt/app/osaaf/local/org.osaaf.aaf.props
	;;
      bash)
	if [ ! "grep aaf_config ~/.bashrc" == "" ]; then 
		echo "alias cadi='/bin/bash /opt/app/aaf_config/bin/agent.sh $*'" >> ~/.bashrc
		. ~/.bashrc
	fi
	shift
	/bin/bash $*
	;;
       encrypt)
	echo $1 $2 $3
	cd /opt/app/osaaf/local
	
	for F in `grep -l $2 *.props`; do 
	  echo "Changing $F"
	  PWD=`$JAVA -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar cadi digest $3 /opt/app/osaaf/local/org.osaaf.aaf.keyfile`
	  sed -i.old -e "s/\($2=\).*/\1enc=$PWD/" /opt/app/osaaf/local/org.osaaf.aaf.cred.props
	  cat $F
	done  
	;;
       *)
	$JAVA -Dcadi_prop_files=/opt/app/osaaf/local/org.osaaf.aaf.props -jar /opt/app/aaf_config/bin/aaf-cadi-aaf-*-full.jar $*
    esac
  fi
fi  

