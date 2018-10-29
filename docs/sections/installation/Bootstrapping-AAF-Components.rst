.. contents::
   :depth: 3
.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Summary
Essentials
Technologies required to run AAF
Optional Technologies for special cases
Data Definitions
AAF Data Definitions
ILM (Identity Lifecycle Management)
Initializing Default Implementation
Extract Sample Configuration
Certificate Authority
Creating your own Certificate Authority (if desired)
Create your Intermediate CAs
Use the Intermediate CA for creating Service/Identity Certs (can be utilized by Certman with LocalCA)
Copy initializations to Host Machine
Load Data and/or Meta-Data into Cassandra
Build Source
Run Java

Summary
-------

AAF Components are all Java(tm) HTTP/S based RESTful services, with the following exceptions:

 - AAF GUI component is an HTTP/S HTML5 generating component.  It uses the same code base, but isn't strictly RESTful according to definition.
 - AAF FS component is a FileServer, and is HTTP only (not TLS), so it can deliver publicly accessible artifacts without Authentication.

Essentials
==========

Technologies required to run AAF
--------------------------------

 - Java(tm).  Version 8.121+
   - Oracle Java previous to Oracle Java SE 8 to version 8 Update 121 is vulnerable to "SWEET32" attack.

     1369383 - CVE-2016-2183 SSL/TLS: Birthday attack against 64-bit block ciphers (SWEET32)

 - Cassandra, Version 2.1.14+
 - X509 Certificates (at minimum to support HTTP/S TLS transactions (TLS1.1 and TLS1.2 are default, but can be configured).

Optional Technologies for special cases
---------------------------------------

 - Build your own Certificate Authority for Bootstrapping and/or Certificate Manager component.
   - openssl
   - bash
   
Data Definitions
----------------

AAF Data Definitions

 - AAF is Data Driven, and therefore, needs to have some structure around the Initial Data so that it can function.  You will need to define:

Your Organization:
 - Example:  Are you a company?  Do you already have a well known internet URL?
 - If so, you should set up AAF Namespaces with this in mind.  Example:

 - for "Kumquat Industries, LTD", with internet presence "kumquats4you.com" (currently, a fictitious name), you would want all your AAF Namespaces to start with:

"com.kumquats4you" 
The examples all use 

"org.osaaf"

However it is recommended that you change this once you figure out your organizations' structure.
Your AAF Root Namespace
This can be within your company namespace, i.e. 

"com.kumquats4you.aaf"

but you might consider putting it under different root structure.
Again, the bootstrapping examples use:

"org.osaaf.aaf" 
 
While creating these, recognize that 
2nd position of the Namespace indicates company/organization
3rd+ position are applications within that company/organization

"com.kumquats4you.dmaap"

Following this "positional" structure is required for expected Authorization behavior.


ILM (Identity Lifecycle Management)
Neither Authentication nor Authorization make any sense outside the context of Identity within your Organization.

Some organizations or companies will have their own ILM managers.

If so you may write your own implementation of "Organization"
Ensure the ILM of choice can be access real-time, or consider exporting the data into File Based mechanism (see entry)
AAF comes with a "DefaultOrganization", which implements a file based localization of ILM in a simple text file

Each line represents an identity in the organization, including essential contact information, and reporting structure 
This file can be updated by bringing in the entire file via ftp or other file transfer protocol, HOWEVER
Provide a process that
Validates no corruption has occurred
Pulls the ENTIRE file down before moving into the place where AAF Components will see it.
Take advantage of UNIX File System behaviors, by MOVING the file into place (mv), rather than copying while AAF is Active
Note: This file-based methodology has been shown to be extremely effective for a 1 million+ Identity organization
TBA-how to add an entry

TBA-what does "sponsorship mean"

Initializing Default Implementation
This is recommended for learning/testing AAF.  You can modify and save off this information for your Organizational use at your discretion.

Extract Sample Configuration
On your Linux box (creating/setting permissions as required)

mkdir -p /opt/app/osaaf

cd /opt/app/osaaf

# Download AAF_sample_config_v1.zip (TBA)

jar -xvf AAF_sample_config_v1.zip

Certificate Authority
You need to identify a SAFE AND SECURE machine when working with your own Certificate Authority.  Realize that if a hacker gets the private keys of your CA or Intermediate CAs, you will be TOTALLY Compromised.

For that reason, many large companies will isolate any machines dealing with Certificates, and that is the recommendation here as well... However, this page cannot explain what works best for you.  JSCEP is an option if you have this setup already.

If you choose to make your own CA, at the very least, once you create your private key for your Root Cert, and your Intermediate Certs, you might consider saving your Private Keys off line and removing from the exposed box.  Again, this is YOUR responsibility, and must follow your policy.



IMPORTANT!  As you create Certificates for Identities, the Identities you use MUST be identities in your ILM.  See /opt/app/aaf/osaaf/data/identities.dat

Creating your own Certificate Authority (if desired)
1) Obtain all the Shell Scripts from the "conf/CA" directory which you can get the from the git repo.

For this example, we'll put everything in /opt/app/osaaf

mkdir /opt/app/osaaf/CA, if required

$ cd /opt/app/osaaf/CA

view README.txt for last minute info

view and/or change "subject.aaf" for your needs. This format will be used on all generated certs from the CA.

$ cat subject.aaf

If you will be using PKCS11 option, review the "cfg.pkcs11" file as well

$ cat cfg.pkcs11

$ bash newca.sh

Obviously, save off your passphrase in an encrypted place... how you do this is your procedure

At this point, your Root CA information has been created.  If you want to start over, you may use "bash clean.sh"

Create your Intermediate CAs
2) You do NOT sign regular Cert requests with your Root.  You only sign with Intermediate CA.  The "intermediate.sh" will create a NEW Intermediate CA Directory and copy appropriate Shell scripts over.  Do this for as many Intermediate CAs as you need.

$ bash newIntermediate.sh

creates directories in order, intermediate_1, intermediate_2, etc.

Use the Intermediate CA for creating Service/Identity Certs (can be utilized by Certman with LocalCA)
3) When creating a Manual Certificate, DO THIS from the Intermediate CA needed

$ cd intermediate_1

4) Create initial Certificate for AAF

IMPORTANT!  As you create Certificates for Identities, the Identities you use MUST be identities in your ILM.  See /opt/app/aaf/osaaf/data/identities.dat

To create LOCALLY, meaning create the CSR, and submit immediately, do the following

$ bash manual.sh <machine-name> -local

FQI (Fully Qualified Identity):

<identity from identities.dat>@<domain, ex: aaf.osaaf.org>

To create Information suitable for Emailing, and signing the returned CSR

$ bash manual.sh <machine-name>

FQI (Fully Qualified Identity):

<identity from identities.dat>@<domain, ex: aaf.osaaf.org>

5) Create p12 file for AAF

REMAIN in the intermediate directory...

$ bash p12.sh <machine-name>

Copy initializations to Host Machine
AAF is setup so it can run 

On the O/S, using Java
On Docker
On K8s
In each case, even for Docker/K8s, we utilize the File O/S for host specific information.   This is because

Many things are Host Specific
The Hostname required for TLS interactions
Cassandra specific information (when external/clustered)
Logging (if logging is done in container, it will be lost if container goes down)
To make things simpler, we are assuming that the file structure will be "/opt/app/osaaf".  The code supports changing this, but documentation will wait until use cases arises for ONAP.

Steps:

1) Copy "osaaf.zip" to your Host Machine, where osaaf.zip is provided by AAF SME. // TODO POST SAMPLE HERE

2) Copy your "p12" file generated by your CA (see above), and place in your "certs" directory

3) SSH (or otherwise login) to your Docker/K8s Host Machine

4) setup your directories (you might need to be root, then adjust what you need for O/S File Permissions

$ mkdir /opt/app/osaaf

$ cd /opt/app/osaaf

$ mkdir cred logs

$ unzip ~/osaaf.zip

$ mv ~/<p12 file from CA above> cred

$ 

Unzip the "osaaf.zip" so it goes into the /opt/app/osaaf directory (should have "etc", "data", "public" and "certs" directories)

4) Modify "org.osaaf.props" to have 



Load Data and/or Meta-Data into Cassandra
Setting this initial Data can be done directly onto Cassadra using "cqlsh" using the following "cql" files:

init<version>.cql (whatever is latest in the "zip" file)
osaaf.cql
      This file contains initial Authorization Structures, see AAF Data Structures. 
            This is where you would modify your own initial Structures.
Build Source
(if not done already)

Run Java
Note: If you have a Kubernets requirement (support), it is STILL RECOMMENDED you run AAF as stand-alone Java Components on your system, and work out any modifications required BEFORE trying to run in Kubernetes.

TBA <java -Dcadi_prop_files=/opt/app/osaaf/etc/org.osaaf.locator.props -cp <path> File>

