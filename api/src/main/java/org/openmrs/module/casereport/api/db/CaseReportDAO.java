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
	
	/**
	 * @see CaseReportService#getCaseReport(Integer)
	 */
	CaseReport getCaseReport(Integer caseReportId);
	
	/**
	 * @see CaseReportService#getCaseReportByUuid(String)
	 */
	CaseReport getCaseReportByUuid(String uuid);
	
	/**
	 * @see CaseReportService#getCaseReports(Patient, boolean, CaseReport.Status...)
	 */
	List<CaseReport> getCaseReports(Patient patient, boolean includeVoided, CaseReport.Status... statuses);
	
	/**
	 * @see CaseReportService#saveCaseReport(CaseReport)
	 */
	CaseReport saveCaseReport(CaseReport caseReport);
}
