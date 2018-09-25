#!/bin/bash
# 
# Copies of Repo data need to be added to "dats" dir for loading by push.sh
#
# Further, repo data has dates that are out of date.  We need to update reasonable
# expiration dates
#

DIR=/opt/app/aaf/cass_init
cd $DIR/dats
ID_FILE=$DIR/opt/app/aaf/cass_init/

    if [ -e $ID_FILE ]; then
      if [ "$(uname -s)" = "Darwin" ]; then 
        DATE=$(date "+%Y-%m-%d %H:%M:%S.000+0000" -v "+6m")
      else 
        DATE=$(date "+%Y-%m-%d %H:%M:%S.000+0000" -d "+6 months")
      fi
      echo $DATE
      CRED="cred.dat"
      # Enter for People
      echo "Default Passwords for People"
      for ID in $(grep '|a|' $ID_FILE | sed -e "s/|.*//"); do
	 if [ "$ID" = "aaf" ]; then
	    DOMAIN="aaf.osaaf.org";
	 else
            DOMAIN="$ID.onap.org";
	 fi
	 unset FIRST
	 for D in ${DOMAIN//./ }; do
            if [ -z "$FIRST" ]; then
	      NS="$D"
	      FIRST="N"
	    else
              NS="$D.$NS"
	    fi
         done     
	 echo "$ID@$DOMAIN|2|${DATE}|0xd993c5617486296f1b99d04de31633332b8ba1a550038e23860f9dbf0b2fcf95|Initial ID|$NS|53344|" >> $CRED
      done
    
      for ID in $(grep '|e|' $ID_FILE | sed -e "s/|.*//"); do
	 echo "$ID@people.osaaf.org|2|${DATE}|0xd993c5617486296f1b99d04de31633332b8ba1a550038e23860f9dbf0b2fcf95|Initial ID|org.osaaf.people|53344|" >> $CRED
      done
    
      mv user_role.dat tmp
      sed "s/\(^.*|\)\(.*|\)\(.*|\)\(.*\)/\1${DATE}|\3\4/" tmp > user_role.dat 

      for DAT in ns perm role ns_attrib user_role cred; do 
          $DOCKER container cp $DAT.dat aaf_cass:/tmp/$DAT.dat
          $DOCKER exec aaf_cass bash /usr/bin/cqlsh -k authz -e "COPY authz.$DAT FROM '/tmp/$DAT.dat' WITH DELIMITER='|'"
          $DOCKER exec -t aaf_cass rm /tmp/$DAT.dat
      done
      rm $CRED
      mv tmp user_role.dat
    else
        echo DInstall requires access to 'identities.dat'
    fi
    cd -

