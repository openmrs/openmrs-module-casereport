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

/**
 * Instances of the subclasses of this class are notified after a case report has been submitted, a
 * typical implementation of this would be a class that forwards the report form details to an
 * external system. Instances of the implementing classes should be registered as spring beans.
 */
public interface PostSubmitListener {
	
	/**
	 * This method is called after a case report has been submitted
	 * 
	 * @param caseReportForm the case report details
	 */
	void afterSubmit(CaseReportForm caseReportForm);
}
