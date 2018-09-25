#mkdir -p $DIR
docker container exec -it aaf_cass bash -e '/opt/app/aaf/cass_init/pull.sh'
docker container cp aaf_cass:/opt/app/aaf/cass_init/dat.gz "dat$(date +%Y%m%d).gz"

