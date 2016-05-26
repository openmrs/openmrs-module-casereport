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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportTrigger;
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
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	private PatientService patientService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet(XML_DATASET);
	}
	
	private void createTestSqlCohortDefinition(String name, String sql, boolean retired) {
		SqlCohortDefinition definition = new SqlCohortDefinition(sql);
		definition.setName(name);
		definition.setRetired(retired);
		DefinitionContext.saveDefinition(definition);
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
		assertEquals(2, report.getReportTriggers().size());
		Iterator<CaseReportTrigger> it = report.getReportTriggers().iterator();
		assertEquals("HIV Virus Not Suppressed", it.next().getName());
		assertEquals("Another Trigger", it.next().getName());
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
	 * @see CaseReportService#getCaseReportByPatient(Patient)
	 * @verifies get the case report for the patient
	 */
	@Test
	public void getCaseReportByPatient_shouldGetTheCaseReportForThePatient() throws Exception {
		CaseReport caseReport = service.getCaseReportByPatient(patientService.getPatient(2));
		assertNotNull(caseReport);
		assertEquals(1, caseReport.getId().intValue());
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
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies return the saved case report
	 */
	@Test
	public void saveCaseReport_shouldReturnTheSavedCaseReport() throws Exception {
		int originalCount = service.getCaseReports().size();
		CaseReport cr = new CaseReport(patientService.getPatient(7), "HIV Virus Not Suppressed");
		service.saveCaseReport(cr);
		assertNotNull(cr.getId());
		assertEquals(++originalCount, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies update an existing case report
	 */
	@Test
	public void saveCaseReport_shouldUpdateAnExistingCaseReport() throws Exception {
		CaseReport caseReport = service.getCaseReport(4);
		assertNull(caseReport.getDateChanged());
		assertNull(caseReport.getChangedBy());
		
		caseReport.setReportForm("{}");
		service.saveCaseReport(caseReport);
		//Flush so that the AuditableInterceptor is invoked to set Auditable fields
		Context.flushSession();
		assertNotNull(caseReport.getDateChanged());
		assertNotNull(caseReport.getChangedBy());
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies change the status of a report from draft to new if the reportForm is blank
	 */
	@Test
	public void saveCaseReport_shouldChangeTheStatusOfAReportFromDraftToNewIfTheReportFormIsBlank() throws Exception {
		CaseReport caseReport = service.getCaseReport(2);
		assertEquals(CaseReport.Status.DRAFT, caseReport.getStatus());
		assertTrue(StringUtils.isNotBlank(caseReport.getReportForm()));
		caseReport.setReportForm(null);
		service.saveCaseReport(caseReport);
		assertEquals(CaseReport.Status.NEW, caseReport.getStatus());
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies not change the status of a report from draft to new if the reportForm is not blank
	 */
	@Test
	public void saveCaseReport_shouldNotChangeTheStatusOfAReportFromDraftToNewIfTheReportFormIsNotBlank() throws Exception {
		CaseReport caseReport = service.getCaseReport(2);
		assertEquals(CaseReport.Status.DRAFT, caseReport.getStatus());
		assertTrue(StringUtils.isNotBlank(caseReport.getReportForm()));
		service.saveCaseReport(caseReport);
		assertEquals(CaseReport.Status.DRAFT, caseReport.getStatus());
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies change the status of a report from new to draft if the reportForm is not blank
	 */
	@Test
	public void saveCaseReport_shouldChangeTheStatusOfAReportFromNewToDraftIfTheReportFormIsNotBlank() throws Exception {
		CaseReport caseReport = service.getCaseReport(1);
		assertEquals(CaseReport.Status.NEW, caseReport.getStatus());
		assertTrue(StringUtils.isBlank(caseReport.getReportForm()));
		caseReport.setReportForm("{}");
		service.saveCaseReport(caseReport);
		assertEquals(CaseReport.Status.DRAFT, caseReport.getStatus());
	}
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 * @verifies not change the status of a report from new to draft if the reportForm is blank
	 */
	@Test
	public void saveCaseReport_shouldNotChangeTheStatusOfAReportFromNewToDraftIfTheReportFormIsBlank() throws Exception {
		CaseReport caseReport = service.getCaseReport(1);
		assertEquals(CaseReport.Status.NEW, caseReport.getStatus());
		assertTrue(StringUtils.isBlank(caseReport.getReportForm()));
		service.saveCaseReport(caseReport);
		assertEquals(CaseReport.Status.NEW, caseReport.getStatus());
	}
	
	/**
	 * @see CaseReportService#submitCaseReport(CaseReport)
	 * @verifies submit the specified case report
	 */
	@Test
	public void submitCaseReport_shouldSubmitTheSpecifiedCaseReport() throws Exception {
		CaseReport cr = service.getCaseReport(4);
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
		CaseReport cr = service.getCaseReport(4);
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
		Integer[] patientIds = { 7, 8 };
		createTestSqlCohortDefinition(name, "select patient_id from patient where patient_id in (" + patientIds[0] + ","
		        + patientIds[1] + ")", false);
		int originalCount = service.getCaseReports().size();
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		
		service.runTrigger(name);
		List<CaseReport> reports = service.getCaseReports();
		int newCount = reports.size();
		assertEquals(originalCount + 2, newCount);
		CaseReport caseReport1 = service.getCaseReportByPatient(patientService.getPatient(patientIds[0]));
		assertNotNull(caseReport1);
		assertEquals(1, caseReport1.getReportTriggers().size());
		assertEquals(name, caseReport1.getReportTriggers().iterator().next().getName());
		CaseReport caseReport2 = service.getCaseReportByPatient(patientService.getPatient(patientIds[1]));
		assertNotNull(caseReport2);
		assertEquals(1, caseReport2.getReportTriggers().size());
		assertEquals(name, caseReport2.getReportTriggers().iterator().next().getName());
	}
	
	/**
	 * @see CaseReportService#runTrigger(String)
	 * @verifies add a new trigger to an existing queue item for the patient
	 */
	@Test
	public void runTrigger_shouldAddANewTriggerToAnExistingQueueItemForThePatient() throws Exception {
		final String name = "some valid cohort query name";
		final Integer patientId = 2;
		CaseReport caseReport = service.getCaseReportByPatient(patientService.getPatient(patientId));
		assertNotNull(caseReport);
		int originalTriggerCount = caseReport.getReportTriggers().size();
		createTestSqlCohortDefinition(name, "select patient_id from patient where patient_id = " + patientId, false);
		
		service = Context.getService(CaseReportService.class);
		int originalCount = service.getCaseReports().size();
		service.runTrigger(name);
		assertEquals(originalCount, service.getCaseReports().size());
		caseReport = service.getCaseReportByPatient(patientService.getPatient(patientId));
		assertEquals(++originalTriggerCount, caseReport.getReportTriggers().size());
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
		createTestSqlCohortDefinition(name, "some query", false);
		createTestSqlCohortDefinition(name, "some query", false);
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
		createTestSqlCohortDefinition(name, "some query", true);
		assertNull(service.getSqlCohortDefinition(name));
	}
	
	/**
	 * @see CaseReportService#getSqlCohortDefinition(String)
	 * @verifies return the matched cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnTheMatchedCohortQuery() throws Exception {
		SqlCohortDefinition definition = service.getSqlCohortDefinition("HIV Virus Not Suppressed");
		assertNotNull(definition);
		assertEquals("5b4f091e-4f28-4810-944b-4e4ccf9bfbb3", definition.getUuid());
	}
	
	/**
	 * @see CaseReportService#generateReportForm(CaseReport)
	 * @verifies generate the report form
	 */
	@Test
	public void generateReportForm_shouldGenerateTheReportForm() throws Exception {
		CaseReport caseReport = service.getCaseReport(1);
		assertNull(caseReport.getReportForm());
		caseReport = service.generateReportForm(caseReport);
		String reportForm = caseReport.getReportForm();
		assertNotNull(reportForm);
		Map<String, Object> reportFormMap = new ObjectMapper().readValue(reportForm, Map.class);
		assertEquals("Horatio", reportFormMap.get("givenName"));
		assertEquals("Hornblower", reportFormMap.get("familyName"));
		assertEquals("M", reportFormMap.get("gender"));
		assertEquals("Test", reportFormMap.get("middleName"));
		assertEquals("1975-04-08T00:00:00.000-0500", reportFormMap.get("birthdate"));
	}
}
