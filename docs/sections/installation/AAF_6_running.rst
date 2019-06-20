.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Running AAF
=========================

The default methodology for running AAF for ONAP is to run as on of the ONAP OOM charts.  This allows AAF to be starting in the right order, be available, etc for ONAP Components. 

AAF is very effective as Standalone Java processes, Docker or non OOM Helm Charts, because AAF is an infrastructure component used by ONAP.  It is not an integral ONAP Component, 
which means that it can be run independently in a number way.  From someone's home computer to the extreme Scale and Resilency requirements of Class A Enterprises.

OOM
---

Please see OOM Documentation to run AAF with other ONAP Components

Helm
----

Use git to pull down "authz" code set.  Build if desired.

	- Ensure Docker and K8s is install (Minikube is very useful for local machines, see internet)
	- Ensure Helm is installed an configured (see internet)
	- cd authz/auth/helm
	- helm --namespace onap -n dublin install aaf
		- Control with kubectl (see K8S/Minikub docs)
        
	- A sample App that generates all its certificates and configurations automatically is available as "aaf-hello
	- helm --namespace onap -n hello install aaf-hello

Docker
------

Use git to pull down "authz" code set. See Build info for Docker.

	- cd authz/auth/docker
	- if you need Docker Container, cd ../cass/docker
		- bash dinstall.sh  
			- (note: if you add the word "publish", cqlsh is available for your local apps at port 9042)

		- bash dcqlsh.sh    (puts you into CQLSH inside Docker)
			- docker container exec -it aaf-cass  bash -c "cqlsh -k authz --request-timeout=60"
		- bash dbash.sh     (short cut to get you a Shell in the "cass" container)
	
	- all the d...sh scripts utilize short-cut names.  
		- "cass" is actually aaf-cass in Docker, "service" is actually aaf-service in Docker

	- dbuild.sh      (builds new Docker Containers, see Build)
	- dclean.sh      (cleans out Docker Containers, getting ready for a new build)
	- drun.sh <blank|shortcut name>       (Creates and Starts Container for all or one AAF components)
        - dstop.sh <blank|shortcut name>      (Stops Container for all or one AAF Components)
	- dstart.sh <blank|shortcut name>     (Use when container exists for all or one AAF Components)
 
