.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

AAF Environment - Beijing
=========================

Access
~~~~~~

You must be connected to the WindRiver "pod-onap-01" VPN to gain access
to AAF Beijing

DNS (/etc/hosts)
~~~~~~~~~~~~~~~~

At this time, there is no known DNS available for ONAP Entities.  It is
recommended that you add the following entry into your "/etc/hosts" on
your accessing machine:

    /etc/hosts:

    10.12.6.214 aaf-onap-beijing-test aaf-onap-beijing-test.osaaf.org

Environment Artifacts (AAF FS)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    AAF has an HTTP Fileserver to gain access to needed public info.

    http://aaf-onap-beijing-test.osaaf.org/-

Credentials
~~~~~~~~~~~

    AAF does support User/Password, and allows additional plugins as it
    did in Amsterdam, however, User/Password credentials are inferior to
    PKI technology, and does not match the ONAP Design goal of TLS and
    PKI Identity across the board.  Therefore, while an individual
    organization might avail themselves of the User/Password facilities
    within AAF, for ONAP, we are avoiding.

    THEREFORE: **GO WITH CERTIFICATE IDENTITY**

Certificates
~~~~~~~~~~~~

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
             - This Truststore has the ONAP AAF\_RootCA in it PLUS all
             the Public CA Certs that are in Java 1.8.131 (note: this is
             in jks format, because the original JAVA truststore was in
             jks format)

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

    There are 3 types of Certificates available for AAF and ONAP
    community through AAF.  People, App Client-only, and App Service
    (can be used for both Client and Service)

Process (This process may fluctuate, or move to iTrack, so revisit this page for each certificate you request)
                                                                                                              

1. 

   1. 

      1. 

         1. Email the AAF Team
            (jonathan.gathman@`att.com <http://att.com>`__, for now)

         2. Put "REQUEST ONAP CERTIFICATE" in the Subject Line

         3. If you have NOT established an Identity, see above, put the
            Identity information in first

         4. Then declare which of the three kinds of Certificates you
            want.

            1. **People** and **App Client-only** certificates will be
               Manual

               1. You will receive a reply email with instructions on
                  creating and signing a CSR, with a specific Subject.

               2. Reply back with the CSR attached. DO NOT CHANGE the
                  Subject.  

                  1. Subject is NOT NEGOTIABLE. If it does not match the
                     original Email, you will be rejected, and will
                     waste everyone's time.

               3. You will receive back the certificate itself, and some
                  openssl instructions to build a .p12 file (or maybe a
                  ready-to-run Shell Script)

            2. *App Service Certificate* is supported by AAF's Certman

               1. However, this requires the establishment of Deployer
                  Identities, as no Certificate is deployed without
                  Authorization.

               2. Therefore, for now, follow the "Manual" method,
                  described in 4.a, but include the Machine to be the
                  "cn="

People
      

    People Certificates can be used for browsers, curl, etc.

    Automation and tracking of People Certificates will be proposed for
    Casablanca.

    In the meantime, for testing purposes, you may request a certificate
    from AAF team, see process.

Application Client-only
                       

    Application Client-only certificates are not tied to a specific
    machine.  They function just like people, only it is expected that
    they are used within "keystores" as identity when talking to AAF
    enabled components.

    PLEASE USE your APP NAME IN CI/CD (OOM, etc) in your request.  That
    makes the most sense for identity.

    Automation and tracking of Application Certificates will be proposed
    for Casablanca. 

    In the meantime, for testing purposes, you may request a certificate
    from AAF team, see process.

Application Service 
                    

    This kind of Certificate must have the Machine Name in the "CN="
    position.  

    AAF supports Automated Certificate Deployment, but this has not been
    integrated with OOM at this time (April 12, 2018).  

-  

   -  Please request Manual Certificate, but specify the Machine as
          well.  Machine should be a name, so you might need to provide
          your Clients with instructions on adding to /etc/hosts until
          ONAP address Name Services for ONAP Environments (i.e. DNS)

    **GUI**

    https://aaf-onap-beijing-test.osaaf.org

    Note: this link is actually to the AAF Locator, which redirects you
    to an available GUI

    The GUI uses the ONAP AAF Certificate Authority (private).  Before
    you can use the Browser, you will need to

-  

   -  Accept the `Root
      Certificate <#AAFEnvironment-Beijing-RootCertificate>`__

   -  Obtain a Personal Certificate above

   -  Add the Personal Certificate/Private key to your Browser.
      Typically, this is done by having it packaged in a
      P\ https://zoom.us/j/793296315
