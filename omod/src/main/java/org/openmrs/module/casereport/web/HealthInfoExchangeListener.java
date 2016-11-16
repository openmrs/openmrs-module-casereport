/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/***
 * An instance of this class listens for event fired when a case report is submitted so that it can
 * generate and submit a CDA message to the HIE
 */
@Component
public class HealthInfoExchangeListener implements ApplicationListener<CaseReportSubmittedEvent> {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see ApplicationListener#onApplicationEvent(ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(CaseReportSubmittedEvent event) {
		try {
			CaseReport caseReport = (CaseReport) event.getSource();
			CaseReportForm form = new ObjectMapper().readValue(caseReport.getReportForm(), CaseReportForm.class);
			String cdaDocument = CdaDocumentGenerator.getInstance().generate(form);
			//TODO send the cda message
		}
		catch (Exception e) {
			log.warn("An error occurred while submitting the CDA message");
			throw new APIException("An error occurred while submitting the cda message for the case report", e);
		}
	}
}