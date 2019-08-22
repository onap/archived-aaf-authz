.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.


Development
============
.. _Example RESTful Client: https://gerrit.onap.org/r/gitweb?p=aaf/authz.git;a=blob;f=cadi/oauth-enduser/src/main/java/org/onap/aaf/cadi/enduser/SimpleRESTClient.java;h=30344de521ae628221bdb54642a71733304a5656;hb=HEAD
.. _Developer Video: https://wiki.onap.org/download/attachments/38111886/ONAPClient.mp4?version=1&modificationDate=1532378616000&api=v2

============
Resources
============
  * `Example RESTful Client`_ (Java Client)
  * `Developer Video`_ (might be large)

=========================
ONAP Test Env
=========================

-------
Purpose
-------

The AAF Test Environment is a single instance VM setp so that 
  - ONAP Developers can utilize AAF with their personal machines without having to run their own AAF. 
  - ONAP Developers can put the Permissions and Roles required for their Apps into a common AAF Environment with others
  - AAF will pull (on demand) all the ONAP related Data (Perms/Roles/Identities) and put into "Bootstrap Data".  
	- This Bootstrap data becomes available on the myriad Testing Systems so that
		- They don't have to create AAF Data loading on their own.
		- The data is already consistent with other ONAP entities.

------
Access
------

You must be connected to the WindRiver "pod-onap-01" VPN to gain access
to AAF Beijing

----------------
DNS (/etc/hosts)
----------------

At this time, there is no known DNS available for ONAP Entities.  It is
recommended that you add the following entry into your "/etc/hosts" on
your accessing machine:

    /etc/hosts:

    10.12.6.214 aaf-onap-beijing-test aaf-onap-beijing-test.osaaf.org

--------------------
Finding AAF Services
--------------------

AAF can be run as standalone Java Services, Docker and Kubernetes. For Kubernetes and some Docker installs, AAF's Services  need to be able to be contacted both inside the K8S, not just one name or port. 

AAF has a Locator Service, default port of 8095, which will give the URLs of Running Services.  The CADI Client uses this, but any Authenticated Client may make queries.

With El Alto, you can request Internal or External URLs.

Example
  * assumes ONAP Test Env URL, and access to ONAP Test Systems
  * put URL in browser

External URL:https://aaf-onap-test.osaaf.org:8095/locate/org.osaaf.aaf.service:2.1
Internal URL:https://aaf-onap-test.osaaf.org:8095/locate/onap.org.osaaf.aaf.service:2.1

Where "onap" is the Container name

------------------------------
Environment Artifacts (AAF FS)
------------------------------

    AAF has an HTTP Fileserver to gain access to needed public info.

    http://aaf-onap-beijing-test.osaaf.org/-

-----------
Credentials
-----------

    AAF does support User/Password, and allows additional plugins as it
    did in Amsterdam, however, User/Password credentials are inferior to
    PKI technology, and does not match the ONAP Design goal of TLS and
    PKI Identity across the board.  Therefore, while an individual
    organization might avail themselves of the User/Password facilities
    within AAF, for ONAP, we are avoiding.

    THEREFORE: **GO WITH CERTIFICATE IDENTITY**


Root Certificate
^^^^^^^^^^^^^^^^

    `AAF\_RootCA.cer <http://aaf-onap-beijing-test.osaaf.org/AAF_RootCA.cer>`__

AAF CA
^^^^^^

    At time of Beijing, an official Certificate Authority for ONAP was
    not declared, installed or operationalized.  Secure TLS requires
    certificates, so for the time being, the Certificate Authority is
    being run by AAF Team.

Root Certificate
''''''''''''''''

    | The Root Certificate for ONAP Certificate Authority used by AAF
      is \ `AAF\_RootCA.cer <http://aaf-onap-beijing-test.osaaf.org/AAF_RootCA.cer>`__
    | Depending on your Browser/ Operating System, clicking on this link
      will allow you to install this Cert into your Browser for GUI
      access (see next)

    This Root Certificate is also available in "truststore" form, ready
    to be used by Java or other processes:

-  

   -  

      -  `truststoreONAP.p12 <http://aaf-onap-beijing-test.osaaf.org/truststoreONAP.p12>`__ 
             -  This Truststore has ONLY the ONAP AAF\_RootCA in it.

      -  `truststoreONAPall.jks <http://aaf-onap-beijing-test.osaaf.org/truststoreONAPall.jks>`__
             - This Truststore has the ONAP AAF\_RootCA in it PLUS all the Public CA Certs that are in Java 1.8.131 (note: this is in jks format, because the original JAVA truststore was in jks format)

    Note: as of Java 8, pkcs12 format is recommended, rather than jks.
     Java's "keytool" utility provides a conversion for .jks for Java 7
    and previous.

Identity
''''''''

    Certificates certify nothing if there is no identity or process to
    verify the Identity.  Typically, for a company, an HR department
    will establish the formal organization, specifically, who reports to
    whom.  For ONAP, at time of Beijing, no such formalized "Org Chart"
    existed, so we'll be building this up as we go along.

    Therefore, with each Certificate Request, we'll need identity
    information as well, that will be entered into an ONAP Identity
    file.  Again, as a real company, this can be derived or accessed
    real-time (if available) as an "Organization Plugin".  Again, as
    there appears to be no such central formal system in ONAP, though,
    of course, Linux Foundation logins have some of this information for
    ALL LF projects.  Until ONAP declares such a system or decides how
    we might integrate with LF for Identity and we have time to create
    an Integration strategy, AAF will control this data.

    For each Identity, we'll need:

  People
        

    | # 0 - unique ID (for Apps, just make sure it is unique, for
      People, one might consider your LinuxFoundation ID)
    | # 1 - full name (for App, name of the APP)
    | # 2 - first name (for App, 
    | # 3 - last name
    | # 4 - phone
    | # 5 - official email
    | # 6 - type - person
    | # 7 - reports to: If you are working as part of a Project, list
      the PTL of your Project.  If you are PTL, just declare you are the
      PTL 

  Applications
              

    | # 0 - unique ID - For ONAP Test, this will be the same a the App
      Acronym.
    | # 1 - full name of the App
    | # 2 - App Acronym
    | # 3 - App Description, or just "Application"
    | # 5 - official email - a Distribution list for the Application, or
      the Email of the Owner
    | # 6 - type - application
    | # 7 - reports to: give the Application Owner's Unique ID.  Note,
      this should also be the Owner in AAF Namespace

Obtaining a Certificate
'''''''''''''''''''''''

Services/Clients
    See `Automated Configuration and Certificates`_.

.. _Automated Configuration and Certificates: AAF_4.1_config.html

People
      

    People Certificates can be used for browsers, curl, etc.

    Automation and tracking of People Certificates will be proposed for
    Dublin.



