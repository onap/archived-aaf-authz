.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

AAF Environments
=========================

AAF can run globally, with high resilience in several different *kinds* of environments.  AAF runs globally, because, thanks to the Locator Component, you can place any network accessible element anywhere in the world, and find the best component entry FQDN and Port based on global Position (Latitude/Longitude).

Env Types
---------
  1. Standalone O/S - AAF runs as Standalone Java Processes on bare metal or VMs.
  2. Docker containers - AAF runs in containers, controlled by standard Docker Commands.
  3. ONAP CSIT (CSIT env is an isolated Testing Environment, run within Jenkins for validation)
  4. ONAP HEAT (Deprecated after Beijing)
  5. Helm (Kubernetes) - this is a Standalone Helm method, suitable for external use of AAF as a Kubernetes Installation
  6. ONAP OOM - AAF deployed as ONAP OOM component.  In this mode, AAF starts first, then provides configurations, certificates, etc to ONAP Components.

Cassandra
---------

A key element of AAF's Global reach and resilience, is that it uses the Cassandra DB.  This automatically updates its data on a global scale, which allows AAF to have consistent data across geography.  

AAF works best if you create a Network Topology based on "Datacenters" which you place in key geographies for your Entity.  You should organize your Cassanda Instances to be colocated with one or more AAF Service/Locator/Oauth instances for optimal performance. Futher, there should always be 3 or more Cassandra instances in each DataCenter (don't forget your "Seed Node" setup, see Cassandra Documentation).

These kinds of setups are achievable fairly easily when using the Standalone O/S model, as it give you maximum control over what is deployed where.

Container based Cassandras, and especially Kubernetes are more of a challenge as to work well, you need to dictate Seed Nodes, etc, but Kubernetes controls which machines any given Cassandra instance resides.

These issues must be solved for best AAF results within your own installations.  For that reason, AAF can be configured to simply "point to" any Cassandra Instance scheme you setup.

However, for testing purposes, requiring a Cassandra Setup beforehand is daunting and problematic, especially for automated testing scenarios, which are used extensively within the ONAP Community.

For this reason, all of the Docker based Installations (#2-#6) come with a preconfigured with a single instance Cassandra, ready to run with ONAP data as soon as started.

