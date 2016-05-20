/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Contains tests for CaseReportService
 */
public class CaseReportServiceTest extends BaseModuleContextSensitiveTest {
	
	private static final String XML_DATASET = "moduleTestData-initialCaseReports.xml";
	
	private CaseReportService service;
	
	@Autowired
	private PatientService patientService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		service = Context.getService(CaseReportService.class);
		executeDataSet(XML_DATASET);
	}
	
	/**
	 * @see CaseReportService#getCaseReport(Integer)
	 * @verifies return the case report that matches the specified id
	 */
	@Test
	public void getCaseReport_shouldReturnTheCaseReportThatMatchesTheSpecifiedId() throws Exception {
		CaseReport report = service.getCaseReport(1);
		assertNotNull(report);
		assertEquals("5f7d57f0-9077-11e1-aaa4-00248140a5ef", report.getUuid());
	}
	
	/**
	 * @see CaseReportService#getCaseReportByUuid(String)
	 * @verifies return the case report that matches the specified uuid
	 */
	@Test
	public void getCaseReportByUuid_shouldReturnTheCaseReportThatMatchesTheSpecifiedUuid() throws Exception {
		CaseReport report = service.getCaseReportByUuid("5f7d57f0-9077-11e1-aaa4-00248140a5ef");
		assertNotNull(report);
		assertEquals(1, report.getId().intValue());
	}
	
	/**
	 * @see CaseReportService#getCaseReports()
	 * @verifies return all non voided case reports in the database
	 */
	@Test
	public void getCaseReports_shouldReturnAllNonVoidedCaseReportsInTheDatabase() throws Exception {
		assertEquals(3, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies return all case reports in the database if all arguments are set to true
	 */
	@Test
	public void getCaseReports_shouldReturnAllCaseReportsInTheDatabaseIfAllArgumentsAreSetToTrue() throws Exception {
		assertEquals(6, service.getCaseReports(true, true, true).size());
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies include dismissed reports in the database if includeDismissed is set to true
	 */
	@Test
	public void getCaseReports_shouldIncludeDismissedReportsInTheDatabaseIfIncludeDismissedIsSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(false, false, true);
		assertEquals(4, reports.size());
		assertTrue(TestUtil.containsId(reports, 6));
		assertFalse(TestUtil.containsId(reports, 3));
		assertFalse(TestUtil.containsId(reports, 5));
		
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies include submitted reports in the database if includeSubmitted is set to true
	 */
	@Test
	public void getCaseReports_shouldIncludeSubmittedReportsInTheDatabaseIfIncludeSubmittedIsSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(false, true, false);
		assertEquals(4, reports.size());
		assertTrue(TestUtil.containsId(reports, 5));
		assertFalse(TestUtil.containsId(reports, 3));
		assertFalse(TestUtil.containsId(reports, 6));
	}
	
	/**
	 * @see CaseReportService#getCaseReports(boolean,boolean,boolean)
	 * @verifies include voided reports in the database if includeVoided is set to true
	 */
	@Test
	public void getCaseReports_shouldIncludeVoidedReportsInTheDatabaseIfIncludeVoidedIsSetToTrue() throws Exception {
		List<CaseReport> reports = service.getCaseReports(true, false, false);
		assertEquals(4, reports.size());
		assertTrue(TestUtil.containsId(reports, 3));
		assertFalse(TestUtil.containsId(reports, 5));
		assertFalse(TestUtil.containsId(reports, 6));
	}
	
	/**
	 * @see CaseReportService#getCaseReportByPatientAndTrigger(Patient,String)
	 * @verifies fail if multiple case reports are found
	 */
	@Test
	public void getCaseReportByPatientAndTrigger_shouldFailIfMultipleCaseReportsAreFound() throws Exception {
		executeDataSet("CaseReportServiceTest-duplicateCaseReport.xml");
		final String trigger = "HIV Virus Not Suppressed";
		Patient patient = patientService.getPatient(2);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Found multiple case reports(2) that match the patient with id:"
		        + patient.getId() + " and trigger:" + trigger));
		service.getCaseReportByPatientAndTrigger(patient, trigger);
	}
	
	/**
	 * @see CaseReportService#getCaseReportByPatientAndTrigger(Patient, String)
	 * @verifies get the matched case report
	 */
	@Test
	public void getCaseReportByPatientAndTrigger_shouldGetTheMatchedCaseReport() throws Exception {
		Patient patient = patientService.getPatient(2);
		final String trigger = "HIV Virus Not Suppressed";
		CaseReport caseReport = service.getCaseReportByPatientAndTrigger(patient, trigger);
		assertNotNull(caseReport);
		assertEquals(patient, caseReport.getPatient());
		assertEquals(trigger, caseReport.getTriggerName());
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies return the saved case report
	 */
	@Test
	public void saveCaseReport_shouldReturnTheSavedCaseReport() throws Exception {
		final String name = "some valid cohort query name";
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		DefinitionContext.saveDefinition(definition);
		
		int originalCount = service.getCaseReports().size();
		CaseReport cr = new CaseReport(patientService.getPatient(2), name);
		service.saveCaseReport(cr);
		assertNotNull(cr.getId());
		assertEquals(++originalCount, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies submit the specified case report
	 */
	@Test
	public void submitCaseReport_shouldSubmitTheSpecifiedCaseReport() throws Exception {
		CaseReport cr = service.getCaseReport(1);
		assertFalse(cr.isSubmitted());
		service.submitCaseReport(cr);
		assertTrue(cr.isSubmitted());
	}
	
	/**
	 * @see CaseReportService#dismissCaseReport(CaseReport)
	 * @verifies dismiss the specified case report
	 */
	@Test
	public void dismissCaseReport_shouldDismissTheSpecifiedCaseReport() throws Exception {
		CaseReport cr = service.getCaseReport(1);
		assertFalse(cr.isDismissed());
		service.dismissCaseReport(cr);
		assertTrue(cr.isDismissed());
	}
	
	/**
	 * @see CaseReportService#runTrigger(String)
	 * @verifies create case reports for the matched patients
	 */
	@Test
	public void runTrigger_shouldCreateCaseReportsForTheMatchedPatients() throws Exception {
		final String name = "some cohort query";
		Integer[] patientIds = { 2, 7 };
		SqlCohortDefinition definition = new SqlCohortDefinition("select patient_id from patient where patient_id in ("
		        + patientIds[0] + "," + patientIds[1] + ")");
		definition.setName(name);
		DefinitionContext.saveDefinition(definition);
		int originalCount = service.getCaseReports().size();
		assertNull(service.getCaseReportByPatientAndTrigger(patientService.getPatient(patientIds[0]), name));
		assertNull(service.getCaseReportByPatientAndTrigger(patientService.getPatient(patientIds[1]), name));
		
		service.runTrigger(name);
		List<CaseReport> reports = service.getCaseReports();
		int newCount = reports.size();
		assertEquals(originalCount + 2, newCount);
		assertNotNull(service.getCaseReportByPatientAndTrigger(patientService.getPatient(patientIds[0]), name));
		assertNotNull(service.getCaseReportByPatientAndTrigger(patientService.getPatient(patientIds[1]), name));
	}
	
	/**
	 * @see CaseReportService#getSqlCohortDefinition(String)
	 * @verifies return null if no cohort query is found that matches the trigger name
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnNullIfNoCohortQueryIsFoundThatMatchesTheTriggerName() throws Exception {
		assertNull(service.getSqlCohortDefinition("some name that does not exist"));
	}
	
	/**
	 * @see CaseReportService#getSqlCohortDefinition(String)
	 * @verifies fail if multiple cohort queries are found that match the trigger name
	 */
	@Test
	public void getSqlCohortDefinition_shouldFailIfMultipleCohortQueriesAreFoundThatMatchTheTriggerName() throws Exception {
		final String name = "some name that is a duplicate";
		SqlCohortDefinition definition1 = new SqlCohortDefinition("some query");
		definition1.setName(name);
		DefinitionContext.saveDefinition(definition1);
		SqlCohortDefinition definition2 = new SqlCohortDefinition("some query");
		definition2.setName(name);
		DefinitionContext.saveDefinition(definition2);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Found multiple Sql Cohort Queries with name:" + name));
		service.getSqlCohortDefinition(name);
	}
	
	/**
	 * @see CaseReportService#getSqlCohortDefinition(String)
	 * @verifies not return a retired cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldNotReturnARetiredCohortQuery() throws Exception {
		final String name = "some retired cohort query";
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		definition.setRetired(true);
		DefinitionContext.saveDefinition(definition);
		assertNull(service.getSqlCohortDefinition(name));
	}
	
	/**
	 * @see CaseReportService#getSqlCohortDefinition(String)
	 * @verifies return the matched cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnTheMatchedCohortQuery() throws Exception {
		final String name = "some cohort query";
		assertNull(service.getSqlCohortDefinition(name));
		SqlCohortDefinition definition = new SqlCohortDefinition("some query");
		definition.setName(name);
		DefinitionContext.saveDefinition(definition);
		assertNotNull(service.getSqlCohortDefinition(name));
	}
}
