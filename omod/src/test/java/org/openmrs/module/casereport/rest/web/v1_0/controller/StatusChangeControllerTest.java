/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.web.v1_0.controller;

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
import org.openmrs.GlobalProperty;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.web.StatusChange;
import org.openmrs.module.casereport.rest.web.v1_0.resource.CaseReportResourceTest;
import org.openmrs.util.OpenmrsConstants;
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
		final String implId = "Test_Impl";
		//set the implementation id for test purposes
		AdministrationService adminService = Context.getAdministrationService();
		String implementationIdGpValue = "<implementationId id=\"1\" implementationId=\"" + implId + "\">\n"
		        + "   <passphrase id=\"2\">Some passphrase</passphrase>\n"
		        + "   <description id=\"3\">Some descr</description>\n" + "   <name id=\"4\">Some name</name>\n"
		        + "</implementationId>";
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_IMPLEMENTATION_ID, implementationIdGpValue);
		adminService.saveGlobalProperty(gp);
		
		executeDataSet("moduleTestData-other.xml");
		final String hivNotSuppressed = "HIV Virus Not Suppressed";
		final String anotherTrigger = "Another Trigger";
		ObjectMapper mapper = new ObjectMapper();
		User submitter = Context.getUserService().getUserByUuid("c98a1558-e131-11de-babe-001e378eb67e");
		CaseReport cr = service.getCaseReportByUuid(getUuid());
		assertTrue(StringUtils.isBlank(cr.getReportForm()));
		CaseReportForm form = new CaseReportForm(cr);
		assertNull(form.getSubmitterName());
		assertNull(form.getSubmitterSystemId());
		assertEquals(2, form.getTriggerAndDateCreatedMap().size());
		assertTrue(form.getTriggerAndDateCreatedMap().keySet().contains(hivNotSuppressed));
		assertTrue(form.getTriggerAndDateCreatedMap().keySet().contains(anotherTrigger));
		assertFalse(cr.isSubmitted());
		
		handle(newPostRequest(getURI(), "{\"action\":\"" + StatusChange.Action.SUBMIT + "\",\"triggersToExclude\":[\""
		        + anotherTrigger + "\"],\"submitter\":\"" + submitter.getUuid() + "\"}"));
		
		assertTrue(cr.isSubmitted());
		cr = service.getCaseReportByUuid(getUuid());
		form = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
		assertEquals(submitter.getPersonName().getFullName(), form.getSubmitterName());
		assertEquals(submitter.getSystemId(), form.getSubmitterSystemId());
		assertEquals(1, form.getTriggerAndDateCreatedMap().size());
		assertTrue(form.getTriggerAndDateCreatedMap().keySet().contains(hivNotSuppressed));
		assertFalse(form.getTriggerAndDateCreatedMap().keySet().contains(anotherTrigger));
	}
	
	@Test
	public void shouldDismissTheCaseReport() throws Exception {
		CaseReport cr = service.getCaseReportByUuid(getUuid());
		assertFalse(cr.isDismissed());
		handle(newPostRequest(getURI(), "{\"action\":\"" + StatusChange.Action.DISMISS + "\"}"));
		assertTrue(cr.isDismissed());
	}
}
