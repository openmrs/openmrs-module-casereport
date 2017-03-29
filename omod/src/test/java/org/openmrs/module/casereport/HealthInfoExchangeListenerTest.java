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

import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class HealthInfoExchangeListenerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	HealthInfoExchangeListener listener;
	
	@Rule
	public WireMockRule wireMockRule = new WireMockRule(5000);
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-other.xml");
		executeDataSet("moduleTestData-HIE.xml");
	}
	
	/**
	 * @see org.openmrs.module.casereport.HealthInfoExchangeListener#onApplicationEvent(CaseReportSubmittedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldSubmitTheDocumentSuccessfullyToTheConfiguredUrl() throws Exception {
		
		CaseReportService service = Context.getService(CaseReportService.class);
		CaseReport caseReport = service.getCaseReport(1);
		CaseReportForm form = new CaseReportForm(caseReport);
		Provider p = Context.getProviderService().getProvider(1);
		form.setSubmitter(new UuidAndValue(p.getUuid(), p.getIdentifier()));
		caseReport.setReportForm(new ObjectMapper().writeValueAsString(form));
		
		TestUtils.createPostStub(true);
		
		listener.onApplicationEvent(new CaseReportSubmittedEvent(caseReport));
		
		final String path = "/xdsrepository";
		String expectedUrl = "http://localhost:5000" + path;
		WireMock.verify(1,
		    WireMock.postRequestedFor(WireMock.urlEqualTo(path)).withRequestBody(WireMock.containing(expectedUrl)));
	}
	
	/**
	 * @see org.openmrs.module.casereport.HealthInfoExchangeListener#onApplicationEvent(CaseReportSubmittedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldFailForAResponseThatIsNotASuccess() throws Exception {
		
		CaseReportService service = Context.getService(CaseReportService.class);
		CaseReport caseReport = service.getCaseReport(1);
		CaseReportForm form = new CaseReportForm(caseReport);
		Provider p = Context.getProviderService().getProvider(1);
		form.setSubmitter(new UuidAndValue(p.getUuid(), p.getIdentifier()));
		caseReport.setReportForm(new ObjectMapper().writeValueAsString(form));
		
		TestUtils.createPostStub(false);
		
		expectedException.expect(APIException.class);
		String errorMsg = "Severity: Error, Code: XDSDocumentUniqueIdError, Message: Document id 2.25.123 is duplicate"
		        + System.getProperty("line.separator");
		expectedException.expectMessage(Matchers.equalTo(errorMsg));
		
		listener.onApplicationEvent(new CaseReportSubmittedEvent(caseReport));
	}
}
