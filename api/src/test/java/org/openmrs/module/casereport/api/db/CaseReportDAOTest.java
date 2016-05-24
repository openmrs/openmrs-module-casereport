/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportDAOTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private CaseReportDAO dao;
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	private PatientService patientService;
	
	/**
	 * @see CaseReportDAO#saveCaseReport(CaseReport)
	 * @verifies return the saved case report
	 */
	@Test
	public void saveCaseReport_shouldReturnTheSavedCaseReport() throws Exception {
		final String name = "some valid cohort query name";
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		DefinitionContext.saveDefinition(definition);
		
		service = Context.getService(CaseReportService.class);
		int originalCount = service.getCaseReports().size();
		CaseReport cr = new CaseReport(patientService.getPatient(2), name);
		dao.saveCaseReport(cr);
		assertNotNull(cr.getId());
		assertEquals(++originalCount, service.getCaseReports().size());
	}
}
