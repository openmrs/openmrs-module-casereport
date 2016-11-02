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
import org.openmrs.module.casereport.CaseReportForm;

/**
 * Generates a CDA document
 */
public final class CdaDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private static CdaDocumentGenerator instance;
	
	private CdaDocumentGenerator() {
	}
	
	public synchronized static CdaDocumentGenerator getInstance() {
		if (instance == null) {
			instance = new CdaDocumentGenerator();
		}
		return instance;
	}
	
	/**
	 * Generates and returns a CDA document for the specified report form
	 * 
	 * @param reportForm
	 * @return the generated CDA document
	 */
	public String generate(CaseReportForm reportForm) {
		return null;
	}
}
