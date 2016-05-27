/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p/>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.web.v1_0.controller;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.web.CaseReportWebConstants;
import org.openmrs.module.casereport.rest.web.v1_0.resource.CaseReportResourceTest;
import org.openmrs.module.webservices.rest.test.Util;
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
		return "queue/" + CaseReportResourceTest.CASE_REPORT_UUID + "/statuschange";
	}
	
	@Override
	public String getUuid() {
		return null;
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
		CaseReport cr = service.getCaseReportByUuid(CaseReportResourceTest.CASE_REPORT_UUID);
		assertFalse(cr.isSubmitted());
		Object r = handle(newPostRequest(getURI(), "{\"action\":\""
		        + CaseReportWebConstants.REST_ACTION_SUBMIT_REQUEST_PARAM_NAME + "\"}"));
		Util.log("HERE\n", r);
		assertTrue(cr.isSubmitted());
	}
	
	@Test
	public void shouldDismissTheCaseReport() throws Exception {
		CaseReport cr = service.getCaseReportByUuid(CaseReportResourceTest.CASE_REPORT_UUID);
		assertFalse(cr.isDismissed());
		Object r = handle(newPostRequest(getURI(), "{\"action\":\""
		        + CaseReportWebConstants.REST_ACTION_DISMISS_REQUEST_PARAM_NAME + "\"}"));
		assertTrue(cr.isDismissed());
	}
}
