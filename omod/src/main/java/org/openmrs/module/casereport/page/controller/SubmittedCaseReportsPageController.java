/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.page.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.FhirDocumentGeneratorListener;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsUtil;

public class SubmittedCaseReportsPageController {
	
	private ObjectMapper mapper = null;
	
	private ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
		}
		return mapper;
	}
	
	public void get(PageModel model, @SpringBean CaseReportService service)
	    throws Exception {
		
		List<CaseReport> caseReports = service.getSubmittedCaseReports(null);
		model.put("caseReports", caseReports);
		SimpleObject reportUuidDocumentMap = new SimpleObject();
		SimpleObject reportUuidSubmittedTriggersMap = new SimpleObject();
		//Sort by date changed(date submitted) with most recent first
		Collections.sort(caseReports, Collections.reverseOrder(new Comparator<CaseReport>() {
			
			@Override
			public int compare(CaseReport cr1, CaseReport cr2) {
				return OpenmrsUtil.compare(cr1.getDateChanged(), cr2.getDateChanged());
			}
		}));
		
		for (CaseReport caseReport : caseReports) {
			String uuid = caseReport.getUuid();
			/*String document = FileUtils.readFileToString(new File(listener.getOutputDirectory(), uuid
			        + FhirDocumentGeneratorListener.FILE_EXT_TXT), FhirDocumentGeneratorListener.ENCODING_UTF8);
			reportUuidDocumentMap.put(uuid, document);*/
			CaseReportForm form = getObjectMapper().readValue(caseReport.getReportForm(), CaseReportForm.class);
			List<String> triggers = new ArrayList<String>();
			for (UuidAndValue uuidAndValue : form.getTriggers()) {
				triggers.add(uuidAndValue.getValue().toString());
			}
			reportUuidSubmittedTriggersMap.put(uuid, StringUtils.join(triggers, ", "));
		}
		
		model.put("reportUuidSubmittedTriggersMap", reportUuidSubmittedTriggersMap);
		model.put("reportUuidDocumentMap", getObjectMapper().writeValueAsString(reportUuidDocumentMap));
	}
}
