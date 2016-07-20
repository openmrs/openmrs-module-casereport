/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport;

import java.io.File;

import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

/**
 * And instance of this class generates a CDA document containing details in the specified case
 * report form and writes the to a file in the casereport directory in the application data
 * directory
 */
@Component(DefaultPostSubmitListener.BEAN_ID)
public class DefaultPostSubmitListener implements PostSubmitListener {
	
	public final static String BEAN_ID = "defaultPostSubmissionListener";
	
	private File outputDirectory;
	
	public File getOutputDirectory() {
		if (outputDirectory == null) {
			outputDirectory = OpenmrsUtil.getDirectoryInApplicationDataDirectory("casereport");
		}
		return outputDirectory;
	}
	
	/**
	 * @see PostSubmitListener#afterSubmit(CaseReportForm)
	 */
	@Override
	public void afterSubmit(CaseReportForm caseReportForm) {
		
	}
}
