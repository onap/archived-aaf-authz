.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

How to Onboard new ONAP Entities
=================================
In running AAF
cd /opt/app/osaaf/data
vi identities.dat
insert like the following
  ngi|ONAP NGI Application|NGI|ONAP Application|||a|aaf_admin

Save (:wq)

In GUI:
AS AAF:

ns create org.onap.ngi mmanager aaf_admin

AS aaf_admin:

got to GUI "MyNamespaces"
got to Cred Details
Create a Password - demo123456!

Create "As Cert Artifact"
(show web page)

From GUI
role create org.onap.ngi.service ngi@ngi.onap.org
perm grant org.onap.ngi.access|*|* org.onap.ngi.service

<Link to Certificates>
