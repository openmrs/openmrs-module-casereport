/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.v1_0.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class CaseReportForPatientSearchHandler implements SearchHandler {
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	@Qualifier("patientService")
	private PatientService pService;
	
	private SearchQuery query = new SearchQuery.Builder("Allows fetching a case report item for a patient")
	        .withRequiredParameters(CaseReportRestConstants.PARAM_PATIENT).build();
	
	private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("1.11.*", "1.12.*", "2.1.*", "2.2.*", "2.3.*" );
	
	/**
	 * @see SearchHandler#getSearchConfig()
	 */
	@Override
	public SearchConfig getSearchConfig() {
		return new SearchConfig("forPatient", CaseReportRestConstants.REST_NAMESPACE + "/casereport", SUPPORTED_VERSIONS,
		        query);
	}
	
	/**
	 * @see SearchHandler#search(RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext requestContext) throws ResponseException {
		Patient patient = null;
		if (StringUtils.isNotBlank(requestContext.getParameter(CaseReportRestConstants.PARAM_PATIENT))) {
			patient = pService.getPatientByUuid(requestContext.getParameter(CaseReportRestConstants.PARAM_PATIENT));
		}
		
		List<CaseReport> reports = Collections.singletonList(service.getCaseReportByPatient(patient));
		return new AlreadyPaged<>(requestContext, reports, false);
	}
}
