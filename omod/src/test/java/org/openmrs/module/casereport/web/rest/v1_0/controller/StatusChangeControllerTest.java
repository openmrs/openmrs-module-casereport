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
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportUtil;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.StatusChange;
import org.openmrs.module.casereport.web.rest.v1_0.resource.CaseReportResourceTest;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class StatusChangeControllerTest extends BaseCaseReportRestControllerTest {
	
	@Autowired
	private CaseReportService service;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(5000);
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
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
		expectedException.expect(ResourceDoesNotSupportOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Override
	public void shouldGetDefaultByUuid() throws Exception {
		expectedException.expect(ResourceDoesNotSupportOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Override
	public void shouldGetFullByUuid() throws Exception {
		expectedException.expect(ResourceDoesNotSupportOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Override
	public void shouldGetRefByUuid() throws Exception {
		expectedException.expect(ResourceDoesNotSupportOperationException.class);
		super.shouldGetRefByUuid();
	}
	
	@Test
	public void shouldSubmitTheCaseReport() throws Exception {
		executeDataSet("moduleTestData-other.xml");
		executeDataSet("moduleTestData-HIE.xml");
		final String hivSwitchToSecondLine = "HIV Switched To Second Line";
		final String newHivCase = "New HIV Case";
		ObjectMapper mapper = new ObjectMapper();
		CaseReport cr = service.getCaseReportByUuid(getUuid());
		assertTrue(StringUtils.isBlank(cr.getReportForm()));
		CaseReportForm form = new CaseReportForm(cr);
		assertEquals(2, form.getTriggers().size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(form.getTriggers(), hivSwitchToSecondLine));
		assertTrue(CaseReportUtil.collContainsItemWithValue(form.getTriggers(), newHivCase));
		assertFalse(cr.isSubmitted());
		
		form.getTriggers().remove(form.getTriggerByName(newHivCase));
		
		final String successResponse = "<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\""
		        + "    xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">"
		        + "    <s:Header>"
		        + "        <wsa:Action s:mustUnderstand=\"true\">urn:ihe:iti:2007:RegisterDocumentSet-bResponse</wsa:Action>"
		        + "        <wsa:RelatesTo>urn:uuid:1ec52e14-4aad-4ba1-b7d3-fc9812a21340</wsa:RelatesTo>" + "    </s:Header>"
		        + "    <s:Body>" + "        <rs:RegistryResponse xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\""
		        + "            status=\"urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success\"/>" + "    </s:Body>"
		        + "</s:Envelope>";
		
		WireMock.stubFor(WireMock
		        .post(WireMock.urlEqualTo("/xdsrepository"))
		        .withHeader("Accept", WireMock.containing("application/soap+xml"))
		        .willReturn(
		            WireMock.aResponse().withStatus(200).withHeader("Content-Type", "application/soap+xml")
		                    .withBody(successResponse)));
		
		handle(newPostRequest(getURI(),
		    "{\"action\":\"" + StatusChange.Action.SUBMIT + "\",\"reportForm\":" + mapper.writeValueAsString(form) + "}"));
		
		assertTrue(cr.isSubmitted());
		cr = service.getCaseReportByUuid(getUuid());
		CaseReportForm savedForm = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
		assertEquals(1, savedForm.getTriggers().size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(savedForm.getTriggers(), hivSwitchToSecondLine));
		assertFalse(CaseReportUtil.collContainsItemWithValue(savedForm.getTriggers(), newHivCase));
	}
	
	@Test
	public void shouldDismissTheCaseReport() throws Exception {
		CaseReport cr = service.getCaseReportByUuid(getUuid());
		assertFalse(cr.isDismissed());
		handle(newPostRequest(getURI(), "{\"action\":\"" + StatusChange.Action.DISMISS + "\"}"));
		assertTrue(cr.isDismissed());
	}
}
