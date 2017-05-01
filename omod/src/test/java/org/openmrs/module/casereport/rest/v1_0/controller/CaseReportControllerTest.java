/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.v1_0.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.DocumentConstants;
import org.openmrs.module.casereport.DocumentUtil;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.casereport.rest.v1_0.resource.CaseReportResourceTest;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.OpenmrsClassLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

public class CaseReportControllerTest extends BaseCaseReportRestControllerTest {
	
	@Autowired
	private CaseReportService service;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-other.xml");
	}
	
	@Override
	public String getURI() {
		return "casereport";
	}
	
	@Override
	public String getUuid() {
		return CaseReportResourceTest.CASE_REPORT_UUID;
	}
	
	@Override
	public long getAllCount() {
		return service.getCaseReports().size();
	}
	
	@Test
	public void shouldCreateNewCaseReport() throws Exception {
		long initialCount = getAllCount();
		SimpleObject reportQueueItem = new SimpleObject();
		reportQueueItem.add("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		SimpleObject trigger1 = new SimpleObject();
		trigger1.add("name", "HIV Patient Died");
		SimpleObject trigger2 = new SimpleObject();
		trigger2.add("name", "New HIV Case");
		reportQueueItem.add("reportTriggers", Arrays.asList(trigger1, trigger2));
		SimpleObject newReportItem = deserialize(handle(newPostRequest(getURI(), reportQueueItem)));
		assertEquals(++initialCount, getAllCount());
		assertEquals(2, ((List) Util.getByPath(newReportItem, "reportTriggers")).size());
	}
	
	@Test
	public void shouldGetACaseReportByUuid() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		assertEquals(getUuid(), Util.getByPath(result, "uuid"));
		final String patientUuid = service.getCaseReportByUuid(getUuid()).getPatient().getUuid();
		assertEquals(patientUuid, Util.getByPath(result, "patient/uuid"));
		assertNull(Util.getByPath(result, "reportForm"));
	}
	
	@Test
	public void shouldGetACaseReportByUuidWithTheReportFormForFullRepresentation() throws Exception {
		final String uri = getURI() + "/" + getUuid();
		SimpleObject result = deserialize(handle(newGetRequest(uri, new Parameter("v", "full"))));
		assertEquals(getUuid(), Util.getByPath(result, "uuid"));
		final String patientUuid = service.getCaseReportByUuid(getUuid()).getPatient().getUuid();
		assertEquals(patientUuid, Util.getByPath(result, "patient/uuid"));
		assertNotNull(Util.getByPath(result, "reportForm"));
	}
	
	@Test
	public void shouldGetAllCaseReportsIncludingVoidedButExcludingSubmittedAndDismissed() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL, "true"))));
		assertNotNull(result);
		assertEquals(5, Util.getResultsSize(result));
	}
	
	@Test
	public void shouldAddTheTriggerToAnExistingQueueItemForThePatient() throws Exception {
		long initialCount = getAllCount();
		CaseReport existingReport = service.getCaseReportByPatient(Context.getPatientService().getPatient(6));
		int initialTriggerCount = existingReport.getReportTriggers().size();
		SimpleObject reportQueueItem = new SimpleObject();
		reportQueueItem.add("patient", existingReport.getPatient().getUuid());
		SimpleObject trigger = new SimpleObject();
		trigger.add("name", "HIV Patient Died");
		reportQueueItem.add("reportTriggers", Collections.singleton(trigger));
		SimpleObject updatedReport = deserialize(handle(newPostRequest(getURI(), reportQueueItem)));
		assertEquals(initialCount, getAllCount());
		assertEquals(++initialTriggerCount, ((List) Util.getByPath(updatedReport, "reportTriggers")).size());
	}
	
	@Test
	public void shouldGenerateAndIncludeAndNotSaveTheReportFormWhenRequested() throws Exception {
		assertNull(service.getCaseReportByUuid(getUuid()).getReportForm());
		final String uri = getURI() + "/" + getUuid();
		SimpleObject result = deserialize(handle(newGetRequest(uri, new Parameter("v", "full"))));
		assertNotNull(Util.getByPath(result, "reportForm"));
		assertNull(service.getCaseReportByUuid(getUuid()).getReportForm());
	}
	
	@Test
	public void shouldVoidACaseReport() throws Exception {
		CaseReport caseReport = service.getCaseReportByUuid(getUuid());
		assertFalse(caseReport.isVoided());
		final String reason = "testing";
		handle(newDeleteRequest(getURI() + "/" + getUuid(), new Parameter("reason", reason)));
		caseReport = service.getCaseReportByUuid(getUuid());
		assertTrue(caseReport.isVoided());
		assertEquals(reason, caseReport.getVoidReason());
	}
	
	@Test
	public void shouldGetTheCaseReportQueue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI())));
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldFetchAllUnvoidedSubmittedCaseReports() throws Exception {
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        CaseReportRestConstants.PARAM_STATUS, CaseReport.Status.SUBMITTED.name()))));
		assertEquals(service.getSubmittedCaseReports(null).size(), Util.getResultsSize(responseData));
	}
	
	@Test
	public void shouldFetchAllUnvoidedSubmittedCaseReportsForTheSpecifiedPatient() throws Exception {
		final String patientUuid = "5946f880-b197-400b-9caa-a3c661d23041";
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        CaseReportRestConstants.PARAM_STATUS, CaseReport.Status.SUBMITTED.name()), new Parameter(
		        CaseReportRestConstants.PARAM_PATIENT, patientUuid))));
		Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
		assertEquals(service.getSubmittedCaseReports(patient).size(), Util.getResultsSize(responseData));
	}
	
	@Test
	public void shouldFetchAllUnvoidedCaseReports() throws Exception {
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        CaseReportRestConstants.PARAM_STATUS, StringUtils.join(CaseReport.Status.values(), ",")))));
		assertEquals(8, Util.getResultsSize(responseData));
	}
	
	@Test
	public void shouldFetchAllCaseReports() throws Exception {
		final String statuses = StringUtils.join(CaseReport.Status.values(), ",");
		Parameter[] params = new Parameter[] { new Parameter(RestConstants.REQUEST_PROPERTY_FOR_INCLUDE_ALL, "true"),
		        new Parameter(CaseReportRestConstants.PARAM_STATUS, statuses) };
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), params)));
		assertEquals(11, Util.getResultsSize(responseData));
	}
	
	@Test
	public void shouldFetchTheCaseReportForTheSpecifiedPatient() throws Exception {
		Patient patient = Context.getPatientService().getPatient(2);
		Parameter[] params = new Parameter[] { new Parameter(RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID, "forPatient"),
		        new Parameter(CaseReportRestConstants.PARAM_PATIENT, patient.getUuid()) };
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), params)));
		assertEquals(1, Util.getResultsSize(responseData));
		assertEquals("5f7d57f0-9077-11e1-aaa4-00248140a5ef",
		    Util.getByPath(Util.getResultsList(responseData).get(0), "uuid"));
	}
	
	@Test
	public void shouldFetchTheSavedDocumentOfAGivenCaseReport() throws Exception {
		final String uuid = "5e7d57f0-9077-11e1-aaa4-00248140a5ec";
		CaseReport cr = service.getCaseReportByUuid(uuid);
		String contents = IOUtils.toString(OpenmrsClassLoader.getInstance().getResourceAsStream("submitted-doc.xml"));
		File file = DocumentUtil.getSubmittedCaseReportFile(cr);
		FileUtils.writeStringToFile(file, contents, DocumentConstants.ENCODING);
		
		MockHttpServletResponse resp = handle(newGetRequest(getURI() + "/" + uuid + "/document"));
		assertTrue(StringUtils.isNotBlank(resp.getContentAsString()));
		assertTrue(resp.getContentAsString().indexOf("<ClinicalDocument/>") > -1);
	}
}
