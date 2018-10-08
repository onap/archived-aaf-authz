#!/bin/bash
# 
# Copies of Repo data need to be added to "dats" dir for loading by push.sh
#
# Further, repo data has dates that are out of date and may have IDs not valid.  We need to make
# sure the important data is consistent with Identities.
#
# This is expected to be run from a controlling Machine data "authz/auth/sample/cass_data" when a backup 
# is created that is intended to be "Bootstrap Data"
#
# Date resets on key data load on the system at load time
#

DIR=$(pwd)
ID_FILE=../data/sample.identities.dat

if [ -e $ID_FILE ]; then
  if [ "$(uname -s)" = "Darwin" ]; then 
    DATE=$(date "+%Y-%m-%d %H:%M:%S.000+0000" -v "+6m")
  else 
    DATE=$(date "+%Y-%m-%d %H:%M:%S.000+0000" -d "+6 months")
  fi
  echo $DATE


  #### CRED
  # Enter for People
  CRED="cred.dat"
  echo "Create default Passwords for all Identities in $CRED"
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


  ##### USER_ROLE
  echo "Scrubbing user_roles not in $ID_FILE"
  > user_role.dat
  for ID in $(grep -v "#" $ID_FILE | awk -F\| '{print $1}' | grep -v "^$"); do
      grep "$ID@" dats/user_role.dat >> user_role.dat
  done

  for D in ns ns_attrib perm role config artifact; do 
      cp dats/$D.dat .
  done

  UR="$(mktemp)"
  DUR="$(mktemp)"

  cat user_role.dat | awk -F\| '{print $1}' | sort -u > $UR
  cat dats/user_role.dat | awk -F\| '{print $1}' | sort -u > $DUR

  echo "Removed IDs from user_roles"
  diff $UR $DUR | grep "^>" | sort -u 

  rm "$UR" "$DUR"
  
  mv user_role.dat tmp
  sed "s/\(^.*|\)\(.*|\)\(.*|\)\(.*\)/\1${DATE}|\3\4/" tmp > user_role.dat 
  rm tmp

else
    echo $0 requires access to $ID_FILE
fi


