
The CSIT functions are started by Jenkins, starting with the "setup.sh"
in the csit/plans/aaf/aafapi directory (where 'csit' is an ONAP Project)

You can emulate the JENKINS build locally

1) Start in the directory you put your ONAP source in
  cd <root onap source dir>
2) If not exist, create a "workspace" directory. 
  mkdir -p workspace
3) Create an empty common functions script
  > workspace/common_functions.sh
4) cd to the plans
  cd csit/plans/aaf/aafapi
5) Run setup with variables set to the Workspace you created
WORKSPACE=/workspace; SCRIPTS=$WORKSPACE; export WORKSPACE SCRIPTS; bash setup.sh

6) To practice the Shutdown, do:
WORKSPACE=/workspace; SCRIPTS=$WORKSPACE; export WORKSPACE SCRIPTS; bash teardown.sh
  

