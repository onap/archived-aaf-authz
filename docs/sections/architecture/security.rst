.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.

Security Architecture
=====================
Communicating
-------------
When one compute process needs to communicate to another, it does so with networking.

The service side is always compute process, but the client can be of two types:
 - People (via browser, or perhaps command line tool)
 - Compute process talking to another computer process.

Thus, the essential building blocks of any networked system is made up of 


In larger systems, it is atypical 

Communicating *Securely*
------------------------
Whenever two processing entities exist that need to communicate securely, it is *essential* that 
 - The communications between the two are encrypted
 - The identities of the caller and callee are established (authentication)
 - The caller must be allowed to do what it is asking to do (authorization)




