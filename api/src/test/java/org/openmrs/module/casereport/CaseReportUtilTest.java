/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportUtilTest extends BaseModuleContextSensitiveTest {
	
	private static final String XML_OTHER_DATASET = "moduleTestData-other.xml";
	
	@Autowired
	PatientService patientService;
	
	/**
	 * @see CaseReportUtil#getThe3MostRecentViralLoads(Patient)
	 * @verifies return the most recent 3 Viral load observations
	 */
	@Test
	public void getThe3MostRecentViralLoads_shouldReturnTheMostRecent3ViralLoadObservations() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> viralLoads = CaseReportUtil.getThe3MostRecentViralLoads(patient);
		assertEquals(3, viralLoads.size());
		assertEquals(8003, viralLoads.get(0).getId().intValue());
		assertEquals(8001, viralLoads.get(1).getId().intValue());
		assertEquals(8000, viralLoads.get(2).getId().intValue());
	}
}
