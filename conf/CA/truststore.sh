#!/bin/bash
#########
#  ============LICENSE_START====================================================
#  org.onap.aaf
#  ===========================================================================
#  Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
#  ===========================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  ============LICENSE_END====================================================
#
echo "FYI, by convention, truststore passwords are 'changeit', but you may add something more sophisticated"
# PCKS12 does not appear to be able to mark CAs as Trusted
# openssl pkcs12 -export -name AAF_Root_CA -in certs/ca.crt -nokeys -out truststore.p12
keytool -importcert -file certs/ca.crt -trustcacerts -alias AAF_ROOT_CA -keystore truststore.jks
