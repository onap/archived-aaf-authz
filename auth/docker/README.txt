# Start the container in bash mode, so it stays up
sh agent.sh bash


# in another shell, find out your Container name
docker container ls | grep aaf_agent

# copy keystore for this AAF Env 
docker container cp -L org.osaaf.aaf.p12 <Your Container>:/opt/app/osaaf/local
# (in Agent Window)
agent encrypt cadi_keystore_password

# If you intend to use Certman to sign certs, it is a "local" CA
# copy Signing Keystore into container
docker container cp -L org.osaaf.aaf.signer.p12 <Your Container>:/opt/app/osaaf/local
# (in Agent Window)
agent encrypt cm_ca.local 

# Add in Cassandra Password 
agent encrypt cassandra.clusters.password

# Check to make sure all passwords are set
grep "enc:" *.props


# When good, run AAF
bash drun.sh

# watch logs in Agent Window
cd ../logs
sh taillog
