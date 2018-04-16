# BEGIN Store prev
if [ -e "6day" ]; then
   rm -Rf 6day
fi

PREV=6day
for D in 5day 4day 3day 2day yesterday; do
   if [ -e "$D" ]; then
      mv "$D" "$PREV"
   fi
   PREV="$D"
done

if [ -e "today" ]; then
    mv today yesterday
    gzip yesterday/*
fi

# END Store prev
date
docker exec -t aaf_cass bash -c "mkdir -p /opt/app/cass_backup"
docker container cp cbackup.sh aaf_cass:/opt/app/cass_backup/backup.sh
# echo "login as Root, then run \nbash /opt/app/cass_backup/backup.sh"
docker exec -t aaf_cass bash /opt/app/cass_backup/backup.sh
mkdir today
docker container cp aaf_cass:/opt/app/cass_backup/. today

date
