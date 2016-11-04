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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * And instance of this class generates a FHIR document containing details in the specified case
 * report form and writes the to a file in the casereport directory in the application data
 * directory.
 */
@Component(FhirDocumentGeneratorListener.BEAN_ID)
public class FhirDocumentGeneratorListener implements ApplicationListener<CaseReportSubmittedEvent> {
	
	public final static String BEAN_ID = "fhirDocumentGeneratorListener";
	
	public static final String ENCODING_UTF8 = "UTF-8";
	
	public static final String FILE_EXT_TXT = ".txt";
	
	private File outputDirectory;
	
	public File getOutputDirectory() {
		if (outputDirectory == null) {
			outputDirectory = OpenmrsUtil.getDirectoryInApplicationDataDirectory("casereport");
		}
		return outputDirectory;
	}
	
	/**
	 * @see ApplicationListener#onApplicationEvent(ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(CaseReportSubmittedEvent event) {
		try {
			CaseReport caseReport = (CaseReport) event.getSource();
			CaseReportForm form = new ObjectMapper().readValue(caseReport.getReportForm(), CaseReportForm.class);
			form.setReportUuid(caseReport.getUuid());
			form.setReportDate(caseReport.getDateCreated());
			String fhirTemplate = FhirUtil.createFhirDocument(form);
			File file = new File(getOutputDirectory(), caseReport.getUuid() + FILE_EXT_TXT);
			FileUtils.writeStringToFile(file, fhirTemplate, ENCODING_UTF8);
		}
		catch (Exception e) {
			throw new APIException("Failed to save the fhir message for the submitted report", e);
		}
	}
}
