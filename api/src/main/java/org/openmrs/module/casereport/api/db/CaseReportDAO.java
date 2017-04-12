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
	 * Fetches the case reports match the specified criteria from the DB
	 * 
	 * @param patient the patient to match
	 * @param includeVoided specifies whether voided reports should be returned
	 * @param orderBy The column to use to sort the results
	 * @param asc The ordering to use, true implies ascending otherwise descending
	 * @param statuses The statuses to match against
	 * @return a list of case reports
	 */
	List<CaseReport> getCaseReports(Patient patient, boolean includeVoided, String orderBy, Boolean asc,
	                                CaseReport.Status... statuses);
	
	CaseReport saveCaseReport(CaseReport caseReport);
}
