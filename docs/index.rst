.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

AAF - Application Authorization Framework
==================================================
.. The purpose of AAF (Application Authorization Framework) is to organize software authorizations so that applications, tools and services can match the access needed to perform job functions.  

AAF is designed to cover Fine-Grained Authorization, meaning that the Authorizations provided are able to used an Application's detailed authorizations, such as whether a user may be on a particular page, or has access to a particular Pub-SUB topic controlled within the App.

This is a critical function for Cloud environments, as Services need to be able to be installed and running in a very short time, and should not be encumbered with local configurations of Users, Permissions and Passwords.

To be effective during a computer transaction, Security must not only be secure, but very fast. Given that each transaction must be checked and validated for Authorization and Authentication, it is critical that all elements on this path perform optimally.


Sections
++++++++

.. toctree::
   :maxdepth: 1
   :glob:

   sections/architecture/index
   sections/installation/index
   sections/configuration/index
   sections/development/index
   sections/logging
   sections/release-notes
   
Introduction
------------
AAF contains some elements of Role Based Authorization, but includes Attribute Based Authorization elements as well. 

|image0|

.. |image0| image:: sections/architecture/images/aaf-object-model.jpg
   :height: 600px
   :width: 800px


Essential Components
--------------------
The core component to deliver this Enterprise Access is a RESTful service, with runtime instances registered in a Cloud Directory (DME2) and backed by a resilient Datastore (Cassandra as of release 1.3)

The Data is managed by RESTful API, with Admin functions supplemented by Character Based User interface and certain GUI elements.

-The Service accessible by provided Caching Clients and by specialized plugins

-CADI (A Framework for providing Enterprise Class Authentication and Authorization with minimal configuration to Containers and Standalone Services)

-Cassandra (GRID Core)
