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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

public class CaseReportValidatorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	public CaseReportValidator validator;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if the case report object is null
	 */
	@Test
	public void validate_shouldFailIfTheCaseReportObjectIsNull() throws Exception {
		Errors errors = new BindException(new CaseReport(), "casereport");
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("The parameter obj should not be null and must be of type" + CaseReport.class);
		validator.validate(null, errors);
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if the patient is null
	 */
	@Test
	public void validate_shouldFailIfThePatientIsNull() throws Exception {
		CaseReport caseReport = new CaseReport(null, "some name");
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("patient"));
		assertEquals("casereports.error.patient.required", errors.getFieldError("patient").getCode());
		
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if the report has no trigger added
	 */
	@Test
	public void validate_shouldFailIfTheReportHasNoTriggerAdded() throws Exception {
		CaseReport caseReport = new CaseReport();
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("reportTriggers"));
		assertEquals("casereports.error.atleast.one.trigger.required", errors.getFieldError("reportTriggers").getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies pass for a valid case report
	 */
	@Test
	public void validate_shouldPassForAValidCaseReport() throws Exception {
		final String name = "some valid cohort query name";
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		DefinitionContext.saveDefinition(definition);
		
		CaseReport caseReport = new CaseReport(Context.getPatientService().getPatient(2), name);
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertFalse(errors.hasErrors());
	}
}
