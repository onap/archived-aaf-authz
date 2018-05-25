.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

AAF Architecture
================
AAF is designed to cover Fine-Grained Authorization, meaning that the Authorizations provided are able to used an Application’s detailed authorizations, such as whether a user may be on a particular page, or has access to a particular Pub-SUB topic controlled within the App.

This is a critical function for Cloud environments, as Services need to be able to be installed and running in a very short time, and should not be encumbered with local configurations of Users, Permissions and Passwords.

To be effective during a computer transaction, Security must not only be secure, but very fast. Given that each transaction must be checked and validated for Authorization and Authentication, it is critical that all elements on this path perform optimally.

|image0|

.. |image0| image:: aaf-object-model.jpg
   :height: 600px
   :width: 800px

Certificate Manager
===================

Overview
--------
Every secure transaction requires 1) Encryption 2) Authentication 3) Authorization.  

 - HTTP/S provides the core Encryption whenever used, so all of AAF Components require HTTP/S to the current protocol standards (current is TLS 1.1+ as of Nov 2016)
 - HTTP/S requires X.509 certificates at least on the Server at minimum. (in this mode, 1 way, a client Certificate is generated)
 - Certificate Manager can generate certificates signed by the AT&T Internal Certificate Authority, which is secure and cost effective if external access are not needed
 - These same certificates can be used for identifying the Application during the HTTP/S transaction, making a separate UserID/Password unnecessary for Authentication.
 - Authentication - In order to tie generated certificates to a specific Application Identity, AAF Certificate Manager embeds a ILM AppID in the Subject.  These are created by AT&T specific Internal Certificate Authority, which only generates certificates for AAF Certman.  Since AAF Certman validates the Sponsorship of the AppID with requests (automatically), the end user can depend on the AppID embedded in the Subject to be valid without resorting to external calls or passwords.

 - ex:
   - Authorization - AAF Certman utilizes AAF's Fine-grained authorizations to ensure that only the right entities perform functions, thus ensuring the integrity of the entire Certificate Process

|image1|

.. |image1| image:: aaf-cm.png
   :height: 768px
   :width: 1024px   
 
Capabilities
------------


Usage Scenarios
---------------


Interactions
------------
