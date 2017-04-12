/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.Trigger;

/**
 * Contains methods for processing CRUD operations related to case reports
 */
public interface CaseReportService extends OpenmrsService {
	
	/**
	 * Gets a CaseReport that matches the specified id
	 * 
	 * @param caseReportId the id to match against
	 * @return the case report that matches the specified id
	 * @should return the case report that matches the specified id
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReport(Integer caseReportId);
	
	/**
	 * Gets a CaseReport that matches the specified uuid
	 *
	 * @param uuid the uuid to match against
	 * @return the case report that matches the specified uuid
	 * @should return the case report that matches the specified uuid
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReportByUuid(String uuid);
	
	/**
	 * Gets the non voided case report for the specified patient.
	 *
	 * @param patient the patient match against
	 * @return a list of the case reports for the patient
	 * @should get the case report for the patient
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReportByPatient(Patient patient);
	
	/**
	 * Gets all non voided case reports from the database that are not yet submitted nor dismissed
	 * ordered by date created. Basically it fetches the case report queue with the earliest coming
	 * first.
	 * 
	 * @return all non voided case reports in the database
	 * @should return all non voided case reports in the database
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getCaseReports();
	
	/**
	 * Gets case reports from the database that match the specified arguments.
	 *
	 * @param patient the patient to match against
	 * @param includeVoided specifies whether voided reports should be included
	 * @param orderBy The property to use for sorting the results
	 * @param asc The ordering to use, true implies ascending otherwise descending
	 * @param statuses specifies the statuses of the reports to match against
	 * @return the case reports in the database including voided ones if includeVoided is set to
	 *         true otherwise they will be excluded
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getCaseReports(Patient patient, boolean includeVoided, String orderBy, Boolean asc,
	                                CaseReport.Status... statuses);
	
	/**
	 * Saves a case report to the database.
	 * 
	 * @param caseReport the case report to save
	 * @return the saved case report
	 * @should return the saved case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport saveCaseReport(CaseReport caseReport);
	
	/**
	 * Marks the specified case report as submitted in the database. If the submitter details are
	 * not set in the report form, they will default to the logged in user
	 * 
	 * @param caseReport the case report to submit
	 * @return the submitted case report
	 * @should fail if the case report is null
	 * @should fail if the case report is blank
	 * @should fail if the case report is a white space character
	 * @should fail if the case report is voided
	 * @should fail if the case report is already submitted
	 * @should fail if the case report is already dismissed
	 * @should fail if submitter and openmrs impl id GP are not set
	 * @should fail if assigningAuthorityId and openmrs impl id GP are not set
	 * @should overwrite the assigning authority id if submitter is set to authenticated user
	 * @should submit the specified case report
	 * @should call the registered submit event listeners
	 * @should fail if no concept is linked to the trigger
	 * @should fail if the linked concept is not mapped to ciel
	 * @should fail for a query with an invalid concept mapping
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport submitCaseReport(CaseReport caseReport);
	
	/**
	 * Marks the specified case report as dismissed in the database
	 *
	 * @param caseReport the case report to dismiss
	 * @return the dismissed case report
	 * @should dismiss the specified case report
	 * @should fail if the case report is voided
	 * @should fail if the case report is already dismissed
	 * @should fail if the case report is already submitted
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport dismissCaseReport(CaseReport caseReport);
	
	/**
	 * Marks the specified case report as voided
	 *
	 * @param caseReport the case report to void
	 * @param voidReason for voiding
	 * @return the voided case report
	 * @should void the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport voidCaseReport(CaseReport caseReport, String voidReason);
	
	/**
	 * Marks the specified case report as not voided
	 *
	 * @param caseReport the case report to unvoid
	 * @return the none voided case report
	 * @should unvoid the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport unvoidCaseReport(CaseReport caseReport);
	
	/**
	 * Gets all the previously submitted case reports, the method will return only the submitted
	 * case reports for specified patient if not null
	 *
	 * @param patient the patient match against
	 * @return a list of previous submitted case reports for the patient
	 * @should return all the previously submitted case reports for the specified patient
	 * @should return all the previously submitted case reports if no patient is specified
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getSubmittedCaseReports(Patient patient);
	
	/**
	 * Gets all the triggers
	 * 
	 * @return a list of triggers
	 * @should return all the triggers
	 */
	@Authorized(CaseReportConstants.PRIV_GET_TRIGGERS)
	List<Trigger> getTriggers();
}
