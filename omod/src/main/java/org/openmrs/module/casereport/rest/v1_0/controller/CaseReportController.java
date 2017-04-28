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

import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.DocumentUtil;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + CaseReportRestConstants.REST_NAMESPACE)
public class CaseReportController extends MainResourceController {
	
	@Autowired
	private CaseReportService service;
	
	/**
	 * @see MainResourceController#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return CaseReportRestConstants.REST_NAMESPACE;
	}
	
	@RequestMapping(value = "/" + CaseReportConstants.MODULE_ID + "/{uuid}/document", method = RequestMethod.GET)
	@ResponseBody
	public String getSubmittedDocumentContents(@PathVariable("uuid") String uuid) {
		
		CaseReport cr = service.getCaseReportByUuid(uuid);
		if (cr == null) {
			throw new ObjectNotFoundException();
		}
		
		return DocumentUtil.getSubmittedDocumentContents(cr);
	}
	
}
