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
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.DrugOrder;
import org.openmrs.GlobalProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportUtilTest extends BaseModuleContextSensitiveTest {
	
	private static final String XML_DATASET = "moduleTestData-initial.xml";
	
	private static final String XML_OTHER_DATASET = "moduleTestData-other.xml";
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
    private SchedulerService schedulerService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	public static SqlCohortDefinition createTestSqlCohortDefinition(String name, String sql, boolean retired,
	                                                                Parameter... parameters) {
		SqlCohortDefinition definition = new SqlCohortDefinition(sql);
		definition.setName(name);
		definition.setRetired(retired);
		for (Parameter param : parameters) {
			definition.addParameter(param);
		}
		return DefinitionContext.saveDefinition(definition);
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentViralLoads(Patient)
	 * @verifies return the 3 most recent Viral load observations
	 */
	@Test
	public void getMostRecentViralLoads_shouldReturnThe3MostRecentViralLoadObservations() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> viralLoads = CaseReportUtil.getMostRecentViralLoads(patient);
		assertEquals(3, viralLoads.size());
		assertEquals(8003, viralLoads.get(0).getId().intValue());
		assertEquals(8001, viralLoads.get(1).getId().intValue());
		assertEquals(8000, viralLoads.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentCD4counts(Patient)
	 * @verifies return the 3 most recent cd4 count observations
	 */
	@Test
	public void getMostRecentCD4counts_shouldReturnThe3MostRecentCd4CountObservations() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> cd4counts = CaseReportUtil.getMostRecentCD4counts(patient);
		assertEquals(3, cd4counts.size());
		assertEquals(8010, cd4counts.get(0).getId().intValue());
		assertEquals(8008, cd4counts.get(1).getId().intValue());
		assertEquals(8007, cd4counts.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentHIVTests(Patient)
	 * @verifies return the 3 most recent HIV test observations
	 */
	@Test
	public void getMostRecentHIVTests_shouldReturnThe3MostRecentHIVTestObservations() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> hivTests = CaseReportUtil.getMostRecentHIVTests(patient);
		assertEquals(3, hivTests.size());
		assertEquals(8017, hivTests.get(0).getId().intValue());
		assertEquals(8015, hivTests.get(1).getId().intValue());
		assertEquals(8014, hivTests.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentWHOStage(Patient)
	 * @verifies return the most recent WHO stage observation
	 */
	@Test
	public void getMostRecentWHOStage_shouldReturnTheMostRecentWHOStageObservation() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(8020, CaseReportUtil.getMostRecentWHOStage(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getActiveArvDrugOrders(Patient, java.util.Date)
	 * @verifies get the active ARV drug orders for the specified patient
	 */
	@Test
	public void getActiveArvDrugOrders_shouldGetTheActiveARVDrugOrdersForTheSpecifiedPatient() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		Date asOfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2016-01-14 00:00:00.0");
		List<DrugOrder> drugOrders = CaseReportUtil.getActiveArvDrugOrders(patient, asOfDate);
		assertEquals(1, drugOrders.size());
		assertEquals(10000, drugOrders.get(0).getId().intValue());
		assertEquals(20000, drugOrders.get(0).getDrug().getId().intValue());
		asOfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2016-01-16 00:00:00.0");
		
		drugOrders = CaseReportUtil.getActiveArvDrugOrders(patient, asOfDate);
		assertEquals(2, drugOrders.size());
		TestUtil.containsId(drugOrders, 10000);
		TestUtil.containsId(drugOrders, 10001);
		assertEquals(20000, drugOrders.get(0).getDrug().getId().intValue());
		assertEquals(20001, drugOrders.get(1).getDrug().getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentReasonARVsStopped(Patient)
	 * @verifies return the most recent obs for the reason why the patient stopped taking ARVs
	 */
	@Test
	public void getMostRecentReasonARVsStopped_shouldReturnTheMostRecentObsForTheReasonWhyThePatientStoppedTakingARVs()
	    throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(8024, CaseReportUtil.getMostRecentReasonARVsStopped(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getLastVisit(Patient)
	 * @verifies return the last visit for the specified patient
	 */
	@Test
	public void getLastVisit_shouldReturnTheLastVisitForTheSpecifiedPatient() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(101, CaseReportUtil.getLastVisit(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies return null if no cohort query is found that matches the trigger name
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnNullIfNoCohortQueryIsFoundThatMatchesTheTriggerName() throws Exception {
		assertNull(CaseReportUtil.getSqlCohortDefinition("some name that does not exist"));
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies fail if multiple cohort queries are found that match the trigger name
	 */
	@Test
	public void getSqlCohortDefinition_shouldFailIfMultipleCohortQueriesAreFoundThatMatchTheTriggerName() throws Exception {
		final String name = "some name that is a duplicate";
		createTestSqlCohortDefinition(name, "some query", false);
		createTestSqlCohortDefinition(name, "some query", false);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Found multiple Sql Cohort Queries with name:" + name));
		CaseReportUtil.getSqlCohortDefinition(name);
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies not return a retired cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldNotReturnARetiredCohortQuery() throws Exception {
		final String name = "some retired cohort query";
		createTestSqlCohortDefinition(name, "some query", true);
		assertNull(CaseReportUtil.getSqlCohortDefinition(name));
	}
	
	/**
	 * @see CaseReportUtil#getSqlCohortDefinition(String)
	 * @verifies return the matched cohort query
	 */
	@Test
	public void getSqlCohortDefinition_shouldReturnTheMatchedCohortQuery() throws Exception {
		executeDataSet(XML_DATASET);
		SqlCohortDefinition definition = CaseReportUtil.getSqlCohortDefinition("HIV Switched To Second Line");
		assertNotNull(definition);
		assertEquals("5b4f091e-4f28-4810-944b-4e4ccf9bfbb3", definition.getUuid());
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies not create a duplicate trigger for the same patient
	 */
	@Test
	public void executeTask_shouldNotCreateADuplicateTriggerForTheSamePatient() throws Exception {
		executeDataSet(XML_DATASET);
		final String name = "HIV Switched To Second Line";
		final Integer patientId = 2;
		CaseReport caseReport = service.getCaseReport(1);
		assertEquals(patientId, caseReport.getPatient().getId());
		assertEquals(2, caseReport.getReportTriggers().size());
		int originalCaseReportCount = service.getCaseReports().size();
		assertNotNull(caseReport.getCaseReportTriggerByName(name));
		SqlCohortDefinition definition = CaseReportUtil.getSqlCohortDefinition(name);
		definition.setQuery("select patient_id from patient where patient_id=" + patientId);
		DefinitionContext.saveDefinition(definition);
		
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		assertEquals(2, caseReport.getReportTriggers().size());
		assertEquals(originalCaseReportCount, service.getCaseReports().size());
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies set the concept mappings in the evaluation context
	 */
	@Test
	public void executeTask_shouldSetTheConceptMappingsInTheEvaluationContext() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		final String name = "HIV Patient Died";
		Integer[] patientIds = { 2, 7 };
		String[] params = { "CIEL_856", "CIEL_1040" };
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select distinct person_id from obs where concept_id = :" + params[0] + " or concept_id = :"
		        + params[0]);
		def.addParameter(new Parameter(params[0], null, Integer.class));
		def.addParameter(new Parameter(params[1], null, Integer.class));
		DefinitionContext.saveDefinition(def);
		int originalCount = service.getCaseReports().size();
		int originalTriggerCount = service.getCaseReportByPatient(patientService.getPatient(patientIds[0]))
		        .getReportTriggers().size();
		assertEquals(2, originalTriggerCount);
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		List<CaseReport> reports = service.getCaseReports();
		int newCount = reports.size();
		assertEquals(++originalCount, newCount);
		assertEquals(++originalTriggerCount, service.getCaseReportByPatient(patientService.getPatient(patientIds[0]))
		        .getReportTriggers().size());
		assertNotNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies fail if no sql cohort query matches the specified trigger name
	 */
	@Test
	public void executeTask_shouldFailIfNoSqlCohortQueryMatchesTheSpecifiedTriggerName() throws Exception {
		final String name = "some name that doesn't exist";
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("No sql cohort query was found that matches the name:" + name));
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY, name);
		CaseReportUtil.executeTask(taskDefinition);
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies fail for a task where the last execution time cannot be resolved
	 */
	@Test
	public void executeTask_shouldFailForATaskWhereTheLastExecutionTimeCannotBeResolved() throws Exception {
		final String name = "some cohort query";
		CaseReportUtilTest.createTestSqlCohortDefinition(name, "select patient_id from patient where date_changed > :"
		        + CaseReportConstants.LAST_EXECUTION_TIME, false, new Parameter(CaseReportConstants.LAST_EXECUTION_TIME,
		        null, Date.class));
		
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY, name);
		taskDefinition.setRepeatInterval(0L);
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Failed to resolve the value for the last execution time"));
		CaseReportUtil.executeTask(taskDefinition);
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies create case reports for the matched patients
	 */
	@Test
	public void executeTask_shouldCreateCaseReportsForTheMatchedPatients() throws Exception {
		executeDataSet(XML_DATASET);
		final String name = "New HIV Case";
		Integer[] patientIds = { 7, 8 };
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select patient_id from patient where patient_id in (" + patientIds[0] + "," + patientIds[1] + ")");
		DefinitionContext.saveDefinition(def);
		int originalCount = service.getCaseReports().size();
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
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
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies set the last execution time in the evaluation context
	 */
	@Test
	public void executeTask_shouldSetTheLastExecutionTimeInTheEvaluationContext() throws Exception {
		final String name = "some cohort query";
		Integer[] patientIds = { 7, 8 };
		CaseReportUtilTest.createTestSqlCohortDefinition(name, "select patient_id from patient where patient_id in ("
		        + patientIds[0] + "," + patientIds[1] + ") and date_changed > :" + CaseReportConstants.LAST_EXECUTION_TIME,
		    false, new Parameter(CaseReportConstants.LAST_EXECUTION_TIME, null, Date.class));
		int originalCount = service.getCaseReports().size();
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
		
		TaskDefinition taskDefinition = new TaskDefinition();
		taskDefinition.setProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY, name);
		taskDefinition.setLastExecutionTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-18 12:25:57"));
		CaseReportUtil.executeTask(taskDefinition);
		List<CaseReport> reports = service.getCaseReports();
		assertEquals(originalCount, reports.size());
		
		taskDefinition.setLastExecutionTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2008-08-18 12:24:34"));
		CaseReportUtil.executeTask(taskDefinition);
		reports = service.getCaseReports();
		int newCount = reports.size();
		assertEquals(++originalCount, newCount);
		assertNotNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[0])));
		assertNull(service.getCaseReportByPatient(patientService.getPatient(patientIds[1])));
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @verifies add a new trigger to an existing queue item for the patient
	 */
	@Test
	public void executeTask_shouldAddANewTriggerToAnExistingQueueItemForThePatient() throws Exception {
		executeDataSet(XML_DATASET);
		final String name = "HIV Patient Died";
		final Integer patientId = 2;
		Patient patient = patientService.getPatient(patientId);
		CaseReport caseReport = service.getCaseReportByPatient(patient);
		assertNotNull(caseReport);
		int originalTriggerCount = caseReport.getReportTriggers().size();
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select patient_id from patient where patient_id = " + patientId);
		DefinitionContext.saveDefinition(def);
		int originalCount = service.getCaseReports().size();
		CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
		assertEquals(originalCount, service.getCaseReports().size());
		caseReport = service.getCaseReportByPatient(patient);
		assertEquals(++originalTriggerCount, caseReport.getReportTriggers().size());
	}
	
	/**
	 * @see CaseReportUtil#executeTask(TaskDefinition)
	 * @throws Exception
	 */
	@Test
	public void executeTask_shouldAutoSubmitANewReportItemForATriggerSetupToDoSo() throws Exception {
		executeDataSet(XML_DATASET);
		executeDataSet(XML_OTHER_DATASET);
		final String name = "New HIV Case";
		//set the implementation id for test purposes
		AdministrationService adminService = Context.getAdministrationService();
		String implementationIdGpValue = "<implementationId implementationId=\"implId\">"
		        + "   <passphrase>Some passphrase</passphrase>" + "   <description>Some descr</description>"
		        + "   <name>implName</name>" + "</implementationId>";
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID, implementationIdGpValue);
		adminService.saveGlobalProperty(gp);
		Provider provider = new Provider();
		provider.setIdentifier("some provider id");
		provider.setName("Some Name");
		Context.getProviderService().saveProvider(provider);
		gp = new GlobalProperty(CaseReportConstants.GP_AUTO_SUBMIT_PROVIDER_UUID, provider.getUuid());
		adminService.saveGlobalProperty(gp);
		final Integer patientId = 8;
		Patient patient = patientService.getPatient(patientId);
		patient.getPatientIdentifier().setIdentifierType(CaseReportUtil.getCaseReportIdType());
		assertEquals(0, service.getSubmittedCaseReports(patient).size());
		assertNull(service.getCaseReportByPatient(patient));
		
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		def.setQuery("select patient_id from patient where patient_id = " + patientId);
		DefinitionContext.saveDefinition(def);
		TaskDefinition taskDefinition = schedulerService.getTaskByName(name);
		taskDefinition.setProperty(CaseReportConstants.AUTO_SUBMIT_TASK_PROPERTY, "true");
		
		CaseReportUtil.executeTask(taskDefinition);
		
		assertNull(service.getCaseReportByPatient(patient));
		List<CaseReport> reports = service.getSubmittedCaseReports(patient);
		assertEquals(1, reports.size());
		CaseReport report = reports.get(0);
		assertTrue(report.getAutoSubmitted());
		CaseReportForm submittedForm = new ObjectMapper().readValue(report.getReportForm(), CaseReportForm.class);
		assertEquals(provider.getUuid(), submittedForm.getSubmitter().getUuid());
		assertEquals(provider.getIdentifier(), submittedForm.getSubmitter().getValue());
		assertEquals(name, report.getReportTriggers().iterator().next().getName());
	}
}
