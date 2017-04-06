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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubmittedCaseReportsSearchHandler implements SearchHandler {
	
	public static final String PARAM_PATIENT = "patient";
	
	@Autowired
	private CaseReportService caseReportService;
	
	@Autowired
	private PatientService patientService;
	
	private SearchQuery query = new SearchQuery.Builder(
	        "Allows fetching submitted case reports, supporting filtering by patient").withOptionalParameters(PARAM_PATIENT)
	        .build();
	
	private static final List<String> SUPPORTED_VERSIONS = Arrays.asList("1.11.*", "1.12.*");
	
	/**
	 * @see SearchHandler#getSearchConfig()
	 */
	@Override
	public SearchConfig getSearchConfig() {
		return new SearchConfig("default", RestConstants.VERSION_1 + "/casereport", SUPPORTED_VERSIONS, query);
	}
	
	/**
	 * @see SearchHandler#search(RequestContext)
	 */
	@Override
	public PageableResult search(RequestContext requestContext) throws ResponseException {
		Patient patient = null;
		if (StringUtils.isNotBlank(requestContext.getParameter(PARAM_PATIENT))) {
			//patient = patientService.getPatientByUuid(requestContext.getParameter(PARAM_PATIENT));
		}
		return null;
		//return new NeedsPaging(caseReportService.getSubmittedCaseReports(patient), requestContext);
	}
}
