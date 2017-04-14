/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.page.controller.templates;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.ui.framework.Model;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

public class QueueItemFormPageController {
	
	public void get(Model model, @RequestParam("patient") Patient patient,
	                @SpringBean("caseReportService") CaseReportService caseReportService) {
		
		//TODO This controller needs to be removed and expose the triggers via rest
		List<String> existingTriggers = new ArrayList<>();
		CaseReport caseReport = caseReportService.getCaseReportByPatient(patient);
		if (caseReport != null) {
			for (CaseReportTrigger trigger : caseReport.getReportTriggers()) {
				existingTriggers.add(trigger.getName());
			}
		}
		
		model.put("triggers", caseReportService.getTriggers());
		model.put("existingTriggers", existingTriggers);
	}
}
