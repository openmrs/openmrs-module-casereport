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

public class CaseReportTriggerValidatorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	public CaseReportTriggerValidator validator;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if the case report trigger object is null
	 */
	@Test
	public void validate_shouldFailIfTheCaseReportTriggerObjectIsNull() throws Exception {
		Errors errors = new BindException(new CaseReportTrigger(), "trigger");
		expectedException.expect(IllegalArgumentException.class);
		String expectedMsg = "The parameter obj should not be null and must be of type" + CaseReportTrigger.class;
		expectedException.expectMessage(expectedMsg);
		validator.validate(null, errors);
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if the trigger name is null
	 */
	@Test
	public void validate_shouldFailIfTheTriggerNameIsNull() throws Exception {
		CaseReportTrigger trigger = new CaseReportTrigger();
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertEquals("casereports.error.trigger.name.required", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if the trigger name is a white space character
	 */
	@Test
	public void validate_shouldFailIfTheTriggerNameIsAWhiteSpaceCharacter() throws Exception {
		CaseReportTrigger trigger = new CaseReportTrigger(" ");
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertEquals("casereports.error.trigger.name.required", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if the trigger name is an empty string
	 */
	@Test
	public void validate_shouldFailIfTheTriggerNameIsAnEmptyString() throws Exception {
		CaseReportTrigger trigger = new CaseReportTrigger("");
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertEquals("casereports.error.trigger.name.required", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if the case report field is null
	 */
	@Test
	public void validate_shouldFailIfTheCaseReportFieldIsNull() throws Exception {
		CaseReportTrigger trigger = new CaseReportTrigger("some valid name");
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("caseReport"));
		assertEquals("casereports.error.trigger.casereport.required", errors.getFieldError("caseReport").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if a case report with the same trigger already exists for the patient
	 */
	@Test
	public void validate_shouldFailIfACaseReportWithTheSameTriggerAlreadyExistsForThePatient() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
		final String name = "HIV Switched To Second Line";
		final Patient patient = Context.getPatientService().getPatient(2);
		CaseReportTrigger existingTrigger = null;
		CaseReport existingCaseReport = Context.getService(CaseReportService.class).getCaseReportByPatient(patient);
		for (CaseReportTrigger crt : existingCaseReport.getReportTriggers()) {
			if (name.equalsIgnoreCase(crt.getName())) {
				existingTrigger = crt;
			}
		}
		assertNotNull(existingTrigger);
		
		CaseReportTrigger trigger = new CaseReportTrigger(name);
		CaseReport caseReport = new CaseReport();
		caseReport.setPatient(Context.getPatientService().getPatient(2));
		trigger.setCaseReport(caseReport);
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertEquals("casereport.error.trigger.duplicate", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
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
		
		CaseReportTrigger trigger = new CaseReportTrigger(name);
		CaseReport caseReport = new CaseReport();
		caseReport.setPatient(Context.getPatientService().getPatient(2));
		trigger.setCaseReport(caseReport);
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object, Errors)
	 * @verifies fail if no sql cohort query matches the trigger name
	 */
	@Test
	public void validate_shouldFailIfNoSqlCohortQueryMatchesTheTriggerName() throws Exception {
		final String name = "some non existent trigger";
		assertNull(CaseReportUtil.getSqlCohortDefinition(name));
		
		CaseReportTrigger trigger = new CaseReportTrigger(name);
		CaseReport caseReport = new CaseReport();
		caseReport.setPatient(Context.getPatientService().getPatient(2));
		trigger.setCaseReport(caseReport);
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertEquals("casereport.error.sqlCohortQuery.notFound", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies fail if the sql cohort query associated to the trigger is retired
	 */
	@Test
	public void validate_shouldFailIfTheSqlCohortQueryAssociatedToTheTriggerIsRetired() throws Exception {
		final String name = "some retired cohort query";
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		definition.setRetired(true);
		DefinitionContext.saveDefinition(definition);
		
		CaseReportTrigger trigger = new CaseReportTrigger(name);
		CaseReport caseReport = new CaseReport();
		caseReport.setPatient(Context.getPatientService().getPatient(7));
		trigger.setCaseReport(caseReport);
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertTrue(errors.hasFieldErrors("name"));
		assertEquals("casereport.error.sqlCohortQuery.notFound", errors.getFieldError("name").getCode());
	}
	
	/**
	 * @see CaseReportTriggerValidator#validate(Object,Errors)
	 * @verifies pass for a valid case report trigger
	 */
	@Test
	public void validate_shouldPassForAValidCaseReportTrigger() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
		CaseReportTrigger trigger = Context.getService(CaseReportService.class).getCaseReport(1).getReportTriggers()
		        .iterator().next();
		Errors errors = new BindException(trigger, "trigger");
		validator.validate(trigger, errors);
		assertFalse(errors.hasErrors());
	}
}
