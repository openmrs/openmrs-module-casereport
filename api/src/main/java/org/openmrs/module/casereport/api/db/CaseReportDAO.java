/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api.db;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;

/**
 * Database access methods for {@link CaseReportService}.
 */
public interface CaseReportDAO {
	
	CaseReport getCaseReport(Integer caseReportId);
	
	CaseReport getCaseReportByUuid(String uuid);
	
	/**
	 * Gets case reports from the database that match the specified arguments.
	 * 
	 * @param patient the patient to match against
	 * @param trigger the trigger to match against
	 * @param includeVoided specifies whether voided reports should be included
	 * @param includeSubmitted specifies whether submitted reports should be included
	 * @param includeDismissed specifies whether dismissed reports should be included
	 * @return the case reports in the database including voided ones if includeVoided is set to
	 *         true otherwise they will be excluded
	 */
	List<CaseReport> getCaseReports(Patient patient, String trigger, boolean includeVoided, boolean includeSubmitted,
	                                boolean includeDismissed);
	
	CaseReport saveCaseReport(CaseReport caseReport);
}
