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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.web.v1_0.resource.CaseReportResourceTest;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportControllerTest extends BaseCaseReportRestControllerTest {
	
	@Autowired
	CaseReportService service;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
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
	
	@Override
	@Test
	public void shouldGetDefaultByUuid() throws Exception {
		super.shouldGetDefaultByUuid();
	}
	
	@Override
	@Test
	public void shouldGetFullByUuid() throws Exception {
		super.shouldGetFullByUuid();
	}
	
	@Test
	public void shouldGetTheCaseReportQueue() throws Exception {
		SimpleObject result = deserialize(handle(newGetRequest(getURI())));
		assertNotNull(result);
		assertEquals(getAllCount(), Util.getResultsSize(result));
	}
	
	@Test
	public void shouldFailIfTheGenerateFormParamIsSpecifiedForAnyRepresentationOtherThanFull() throws Exception {
		expectedException.expect(GenericRestException.class);
		expectedException.expectMessage("generateForm request parameter can only specified for the full representation");
		deserialize(handle(newGetRequest(getURI() + "/" + getUuid(), new Parameter("generateForm", "true"))));
	}
	
	@Test
	public void shouldGenerateTheFormIfTheGenerateFormParamIsSpecifiedForTheFullRepresentation() throws Exception {
		assertNull(service.getCaseReportByUuid(getUuid()).getReportForm());
		deserialize(handle(newGetRequest(getURI() + "/" + getUuid(), new Parameter("v", "full"), new Parameter(
		        "generateForm", "true"))));
		assertTrue(StringUtils.isNotBlank(service.getCaseReportByUuid(getUuid()).getReportForm()));
	}
}
