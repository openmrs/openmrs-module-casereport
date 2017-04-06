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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rest/" + CaseReportRestConstants.REST_NAMESPACE)
public class CaseReportResourceController extends MainResourceController {
	
	/**
	 * @see MainResourceController#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return CaseReportRestConstants.REST_NAMESPACE;
	}
	
	/**
	 * @see MainResourceController#get(java.lang.String, javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public SimpleObject get(@PathVariable("resource") String resource, HttpServletRequest request,
	                        HttpServletResponse response) throws ResponseException {
		
		Set paramKeys = request.getParameterMap().keySet();
		if (CaseReportRestConstants.QUEUE.equals(resource)) {
			resource = "casereport";
		} else if ("casereport".equals(resource) && !paramKeys.contains(RestConstants.REQUEST_PROPERTY_FOR_SEARCH_ID)) {
			//Do not allow fetching all case reports
			throw new ResourceDoesNotSupportOperationException();
		}
		
		return super.get(resource, request, response);
	}
}
