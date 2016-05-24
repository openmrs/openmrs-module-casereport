/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p/>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.web.v1_0.resource;

import org.junit.Before;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportResourceTest extends BaseDelegatingResourceTest<CaseReportResource, CaseReport> {
	
	public static final String CASE_REPORT_UUID = "5f7d57f0-9077-11e1-aaa4-00248140a5ef";
	
	@Autowired
	CaseReportService service;
	
	@Override
	public CaseReport newObject() {
		return service.getCaseReportByUuid(getUuidProperty());
	}
	
	@Override
	public String getDisplayProperty() {
		return "Patient#2 Trigger(s): HIV Virus Not Suppressed, Another Trigger";
	}
	
	@Override
	public String getUuidProperty() {
		return CASE_REPORT_UUID;
	}
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
	}
	
	@Override
	public void validateDefaultRepresentation() throws Exception {
		super.validateDefaultRepresentation();
		assertPropEquals("patient", getObject().getPatient());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("reportTriggers");
		assertPropNotPresent("auditInfo");
	}
	
	@Override
	public void validateFullRepresentation() throws Exception {
		super.validateFullRepresentation();
		assertPropEquals("patient", getObject().getPatient());
		assertPropEquals("voided", getObject().getVoided());
		assertPropPresent("reportTriggers");
		assertPropPresent("auditInfo");
	}
}
