
DIR=$(date +%Y%m%d)
echo $DIR
mkdir -p $DIR
docker container cp cbackup.sh aaf_cass:/opt/app/cass_backup
docker container exec -it aaf_cass bash -e '/opt/app/cass_backup/cbackup.sh'
docker container cp aaf_cass:/opt/app/cass_backup/ $DIR/
mv $DIR/cass_backup/*.dat $DIR
tar -cvzf $DIR.gz $DIR
rm -Rf $DIR

