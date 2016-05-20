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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
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
	 * @verifies fail if the trigger name is a white space character
	 */
	@Test
	public void validate_shouldFailIfTheTriggerNameIsAWhiteSpaceCharacter() throws Exception {
		CaseReport caseReport = new CaseReport(new Patient(), " ");
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("triggerName"));
		assertEquals("casereports.error.triggerName.required", errors.getFieldError("triggerName").getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if the trigger name is an empty string
	 */
	@Test
	public void validate_shouldFailIfTheTriggerNameIsAnEmptyString() throws Exception {
		CaseReport caseReport = new CaseReport(new Patient(), "");
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("triggerName"));
		assertEquals("casereports.error.triggerName.required", errors.getFieldError("triggerName").getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if the trigger name is null
	 */
	@Test
	public void validate_shouldFailIfTheTriggerNameIsNull() throws Exception {
		CaseReport caseReport = new CaseReport(new Patient(), null);
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("triggerName"));
		assertEquals("casereports.error.triggerName.required", errors.getFieldError("triggerName").getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object, Errors)
	 * @verifies fail if a case report with the same trigger already exists for the patient
	 */
	@Test
	public void validate_shouldFailIfACaseReportWithTheSameTriggerAlreadyExistsForThePatient() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
		final String trigger = "HIV Virus Not Suppressed";
		final Patient patient = Context.getPatientService().getPatient(2);
		assertNotNull(Context.getService(CaseReportService.class).getCaseReportByPatientAndTrigger(patient, trigger));
		CaseReport caseReport = new CaseReport(patient, trigger);
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasErrors());
		assertTrue(errors.hasGlobalErrors());
		assertEquals("casereports.error.duplicate", errors.getGlobalErrors().get(0).getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if no sql cohort query matches the trigger name
	 */
	@Test
	public void validate_shouldFailIfNoSqlCohortQueryMatchesTheTriggerName() throws Exception {
		final String name = "some non existent trigger";
		assertNull(Context.getService(CaseReportService.class).getSqlCohortDefinition(name));
		CaseReport caseReport = new CaseReport(Context.getPatientService().getPatient(2), name);
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("triggerName"));
		assertEquals("casereport.error.sqlCohortQuery.notFound", errors.getFieldError("triggerName").getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if the sql cohort query associated to the trigger is retired
	 */
	@Test
	public void validate_shouldFailIfTheSqlCohortQueryAssociatedToTheTriggerIsRetired() throws Exception {
		final String name = "some retired cohort query";
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		definition.setRetired(true);
		DefinitionContext.saveDefinition(definition);
		
		CaseReport caseReport = new CaseReport(Context.getPatientService().getPatient(2), name);
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
		assertTrue(errors.hasFieldErrors("triggerName"));
		assertEquals("casereport.error.sqlCohortQuery.notFound", errors.getFieldError("triggerName").getCode());
	}
	
	/**
	 * @see CaseReportValidator#validate(Object,Errors)
	 * @verifies fail if multiple sql cohort queries match the trigger name
	 */
	@Test
	public void validate_shouldFailIfMultipleSqlCohortQueriesMatchTheTriggerName() throws Exception {
		final String name = "some name that is a duplicate";
		SqlCohortDefinition definition1 = new SqlCohortDefinition("some query");
		definition1.setName(name);
		DefinitionContext.saveDefinition(definition1);
		SqlCohortDefinition definition2 = new SqlCohortDefinition("some query");
		definition2.setName(name);
		DefinitionContext.saveDefinition(definition2);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Found multiple Sql Cohort Queries with name:" + name));
		
		CaseReport caseReport = new CaseReport(Context.getPatientService().getPatient(2), name);
		Errors errors = new BindException(caseReport, "casereport");
		validator.validate(caseReport, errors);
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
