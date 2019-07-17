/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.cm.validation;

import org.junit.Test;
import org.onap.aaf.auth.dao.cass.ArtiDAO;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class JU_CertmanValidator {

    private static final String COLLECTION_NAME = "collection_name";
    private static final int MIN_SIZE = 3;
    private CertmanValidator certmanValidator = new CertmanValidator();


    @Test
    public void nullBlankMin_shouldReportErrorWhenListIsNull() {

        certmanValidator.nullBlankMin(COLLECTION_NAME, null, MIN_SIZE);
        assertEquals(COLLECTION_NAME + " is null.\n", certmanValidator.errs());
    }

    @Test
    public void nullBlankMin_shouldReportErrorWhenListHasNotEnoughElements() {

        certmanValidator.nullBlankMin(COLLECTION_NAME, newArrayList("one", "two"), MIN_SIZE);
        assertEquals(COLLECTION_NAME + " must have at least " + MIN_SIZE + " entries.\n", certmanValidator.errs());
    }

    @Test
    public void nullBlankMin_shouldReportErrorWhenListContainsNullOrEmptyElements() {

        certmanValidator.nullBlankMin(COLLECTION_NAME, newArrayList("one", "", "three"), MIN_SIZE);
        assertEquals("List Item is blank.\n", certmanValidator.errs());
    }

    @Test
    public void nullBlankMin_shouldPassValidation() {

        certmanValidator.nullBlankMin(COLLECTION_NAME, newArrayList("one", "two", "three"), MIN_SIZE);
        assertFalse(certmanValidator.err());
    }

    @Test
    public void artisRequired_shouldReportErrorWhenListIsNull() {

        certmanValidator.artisRequired(null, MIN_SIZE);
        assertEquals("Artifact List is null.\n", certmanValidator.errs());
    }

    @Test
    public void artisRequired_shouldReportErrorWhenListHasNotEnoughElements() {

        certmanValidator.artisRequired(newArrayList(newArtifactData(), newArtifactData()), MIN_SIZE);
        assertEquals("Artifacts must have at least " + MIN_SIZE + " entries.\n", certmanValidator.errs());
    }

    @Test
    public void artisRequired_shouldReportErrorWhenArtifactDoesNotHaveAllRequiredFields() {

        certmanValidator.artisRequired(newArrayList(newArtifactData("id", "", "ca", "dir", "user")), 1);
        assertEquals("machine is blank.\n"  + "NS must be dot separated AlphaNumeric\n", certmanValidator.errs());
    }

    @Test
    public void keys_shouldReportErrorWhenArtifactIsNull() {

        certmanValidator.keys(null);
        assertEquals("Artifact is null.\n", certmanValidator.errs());
    }

    @Test
    public void keys_shouldReportErrorWhenArtifactDoesNotHaveAllRequiredFields() {

        certmanValidator.keys(newArtifactData("", "", "ca", "dir", "user"));
        assertEquals("mechid is blank.\n" + "machine is blank.\n", certmanValidator.errs());
    }

    private ArtiDAO.Data newArtifactData() {
        return new ArtiDAO.Data();
    }

    private ArtiDAO.Data newArtifactData(String mechId, String machine, String ca, String dir, String user) {
        ArtiDAO.Data artifact = new ArtiDAO.Data();
        artifact.mechid = mechId;
        artifact.machine = machine;
        artifact.ca = ca;
        artifact.dir = dir;
        artifact.os_user = user;
        return artifact;

    }
}