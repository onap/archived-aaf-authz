.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

=====================
Prerequisites for AAF
=====================

To *run* AAF, you need the following:
  * Java 8+, openjdk is fine (see Version below)
  * Cassandra
     * a separate installation is fine
     * Docker based AAF Envs offer a single instance Cassandra for convenience.  Single Instance Cassandra is **NOT** recommended for real AAF systems.
  * Machine - one of the following
     * Standalone Java Processes - Bare Metal or VMs.  No additional running environments necessary
     * docker - typically available via packages for O/S
     * kubernetes - various installs available

To *build* AAF, you additionally need:
  * Java 8+, openjdk is fine (see Version below)
  * git
  * maven
  * for Container Based, you'll need Docker to build as well
     * Note: 'minikube' works well to provide both Docker and Kubernetes Single Instance installations. 
     

---------------------------
Current Technology Versions
---------------------------

 - Java(tm).  Version 8.121+
   - Oracle Java previous to Oracle Java SE 8 to version 8 Update 121 is vulnerable to "SWEET32" attack.

     1369383 - CVE-2016-2183 SSL/TLS: Birthday attack against 64-bit block ciphers (SWEET32)

 - Cassandra, Version 3.11+

 - X509 Certificates (at minimum to support HTTP/S TLS transactions (TLS1.1 and TLS1.2 are default, but can be configured).

