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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.springframework.beans.factory.annotation.Autowired;

public class TriggerControllerTest extends BaseCaseReportRestControllerTest {
	
	@Autowired
	private CaseReportService service;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
	}
	
	@Override
	public String getURI() {
		return "trigger";
	}
	
	@Override
	public String getUuid() {
		return null;
	}
	
	@Override
	public long getAllCount() {
		return service.getTriggers().size();
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
	public void shouldFetchAllTriggers() throws Exception {
		SimpleObject responseData = deserialize(handle(newGetRequest(getURI())));
		assertEquals(4, Util.getResultsSize(responseData));
	}
}
