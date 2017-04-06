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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.v1_0.resource.CaseReportResourceTest;
import org.openmrs.module.casereport.rest.v1_0.search.SubmittedCaseReportsSearchHandler;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;

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
		return "queue";
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
	public void shouldGetTheCaseReportQueue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI())));
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldCreateNewCaseReportQueueItem() throws Exception {
		long initialCount = getAllCount();
		assertEquals(initialCount, Util.getResultsSize(deserialize(handle(newGetRequest(getURI())))));
		SimpleObject reportQueueItem = new SimpleObject();
		reportQueueItem.add("patient", "5946f880-b197-400b-9caa-a3c661d23041");
		SimpleObject trigger1 = new SimpleObject();
		trigger1.add("name", "HIV Patient Died");
		SimpleObject trigger2 = new SimpleObject();
		trigger2.add("name", "New HIV Case");
		reportQueueItem.add("reportTriggers", Arrays.asList(trigger1, trigger2));
		SimpleObject newReportItem = deserialize(handle(newPostRequest(getURI(), reportQueueItem)));
		assertEquals(++initialCount, Util.getResultsSize(deserialize(handle(newGetRequest(getURI())))));
		assertEquals(2, ((List) Util.getByPath(newReportItem, "reportTriggers")).size());
	}
	
	@Test
	public void shouldAddTheTriggerToAnExistingQueueItemForThePatient() throws Exception {
		long initialCount = getAllCount();
		CaseReport existingReport = service.getCaseReportByPatient(Context.getPatientService().getPatient(6));
		int initialTriggerCount = existingReport.getReportTriggers().size();
		assertEquals(initialCount, Util.getResultsSize(deserialize(handle(newGetRequest(getURI())))));
		SimpleObject reportQueueItem = new SimpleObject();
		reportQueueItem.add("patient", existingReport.getPatient().getUuid());
		SimpleObject trigger = new SimpleObject();
		trigger.add("name", "HIV Patient Died");
		reportQueueItem.add("reportTriggers", Collections.singleton(trigger));
		SimpleObject updatedReport = deserialize(handle(newPostRequest(getURI(), reportQueueItem)));
		assertEquals(initialCount, Util.getResultsSize(deserialize(handle(newGetRequest(getURI())))));
		assertEquals(++initialTriggerCount, ((List) Util.getByPath(updatedReport, "reportTriggers")).size());
	}
	
	@Test
	public void shouldGenerateAndIncludeAndNotSaveTheReportFormWhenRequested() throws Exception {
		assertNull(service.getCaseReportByUuid(getUuid()).getReportForm());
		SimpleObject result = deserialize(handle(newGetRequest(getURI() + "/" + getUuid(), new Parameter("v", "full"))));
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
	public void shouldFetchAllUnvoidedSubmittedCaseReports() throws Exception {
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID, "default"))));
		assertEquals(4, Util.getResultsSize(responseData));
	}
	
	@Test
	public void shouldFetchAllUnvoidedSubmittedCaseReportsForTheSpecifiedPatient() throws Exception {
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI(), new Parameter(
		        RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID, "default"), new Parameter(
		        SubmittedCaseReportsSearchHandler.PARAM_PATIENT, "5946f880-b197-400b-9caa-a3c661d23041"))));
		assertEquals(2, Util.getResultsSize(responseData));
	}
}
