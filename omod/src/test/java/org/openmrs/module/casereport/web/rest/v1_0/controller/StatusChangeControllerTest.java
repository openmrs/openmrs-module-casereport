/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.web.rest.v1_0.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportUtil;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.web.rest.StatusChange;
import org.openmrs.module.casereport.web.rest.v1_0.resource.CaseReportResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class StatusChangeControllerTest extends BaseCaseReportRestControllerTest {
	
	@Autowired
	private CaseReportService service;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
	}
	
	@Override
	public String getURI() {
		return "queue/" + getUuid() + "/statuschange";
	}
	
	@Override
	public String getUuid() {
		return CaseReportResourceTest.CASE_REPORT_UUID;
	}
	
	@Override
	public long getAllCount() {
		return 0;
	}
	
	@Override
	public void shouldGetAll() throws Exception {
		expectedException.expect(UnsupportedOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		expectedException.expect(UnsupportedOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
		expectedException.expect(UnsupportedOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
		expectedException.expect(UnsupportedOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Test
	public void shouldSubmitTheCaseReport() throws Exception {
		executeDataSet("moduleTestData-other.xml");
		final String hivNotSuppressed = "HIV Virus Not Suppressed";
		final String anotherTrigger = "Another Trigger";
		final String implementationId = "Test_Impl";
		final String implementationName = "Test_Name";
		ObjectMapper mapper = new ObjectMapper();
		User submitter = Context.getUserService().getUserByUuid("c98a1558-e131-11de-babe-001e378eb67e");
		CaseReport cr = service.getCaseReportByUuid(getUuid());
		assertTrue(StringUtils.isBlank(cr.getReportForm()));
		CaseReportForm form = new CaseReportForm(cr);
		assertNull(form.getSubmitter());
		assertNull(form.getImplementationId());
		assertNull(form.getImplementationName());
		assertEquals(2, form.getTriggers().size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(form.getTriggers(), hivNotSuppressed));
		assertTrue(CaseReportUtil.collContainsItemWithValue(form.getTriggers(), anotherTrigger));
		assertFalse(cr.isSubmitted());
		
		handle(newPostRequest(getURI(), "{\"action\":\"" + StatusChange.Action.SUBMIT + "\",\"triggersToExclude\":[\""
		        + anotherTrigger + "\"],\"submitter\":\"" + submitter.getUuid() + "\",\"implementationId\":\""
		        + implementationId + "\",\"implementationName\":\"" + implementationName + "\"}"));
		
		assertTrue(cr.isSubmitted());
		cr = service.getCaseReportByUuid(getUuid());
		form = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
		assertEquals(submitter.getUuid(), form.getSubmitter().getUuid());
		assertEquals(submitter.getSystemId(), form.getSubmitter().getValue());
		assertEquals(implementationId, form.getImplementationId());
		assertEquals(implementationName, form.getImplementationName());
		assertEquals(1, form.getTriggers().size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(form.getTriggers(), hivNotSuppressed));
		assertFalse(CaseReportUtil.collContainsItemWithValue(form.getTriggers(), anotherTrigger));
	}
	
	@Test
	public void shouldDismissTheCaseReport() throws Exception {
		CaseReport cr = service.getCaseReportByUuid(getUuid());
		assertFalse(cr.isDismissed());
		handle(newPostRequest(getURI(), "{\"action\":\"" + StatusChange.Action.DISMISS + "\"}"));
		assertTrue(cr.isDismissed());
	}
}
