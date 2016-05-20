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
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationException;

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
	CaseReport getCaseReport(Integer caseReportId) throws APIException;
	
	/**
	 * Gets a CaseReport that matches the specified uuid
	 *
	 * @param uuid the uuid to match against
	 * @return the case report that matches the specified uuid
	 * @should return the case report that matches the specified uuid
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReportByUuid(String uuid) throws APIException;
	
	/**
	 * Gets all non voided case reports from the database that are not yet submitted nor dismissed
	 * 
	 * @return all non voided case reports in the database
	 * @should return all non voided case reports in the database
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getCaseReports() throws APIException;
	
	/**
	 * Gets case reports from the database that match the specified arguments, developers typically
	 * should only call this method with all methods set to true in case of data migration
	 * 
	 * @param includeVoided specifies whether voided reports should be included
	 * @param includeSubmitted specifies whether submitted reports should be included
	 * @param includeDismissed specifies whether dismissed reports should be included
	 * @return the case reports in the database including voided ones if includeVoided is set to
	 *         true otherwise they will be excluded
	 * @should return all case reports in the database if all arguments are set to true
	 * @should include voided reports in the database if includeVoided is set to true
	 * @should include submitted reports in the database if includeSubmitted is set to true
	 * @should include dismissed reports in the database if includeDismissed is set to true
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getCaseReports(boolean includeVoided, boolean includeSubmitted, boolean includeDismissed)
	    throws APIException;
	
	/**
	 * Gets the non voided case report that matches the specified patient and trigger.
	 * 
	 * @param patient the patient to match against
	 * @param triggerName the trigger to match against
	 * @return the matched case report
	 * @throws APIException
	 * @should get the matched case report
	 * @should fail if multiple case reports are found
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReportByPatientAndTrigger(Patient patient, String triggerName) throws APIException;
	
	/**
	 * Saves a case report to the database
	 * 
	 * @param caseReport the case report to save
	 * @return the saved case report
	 * @should return the saved case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport saveCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Marks the specified case report as submitted in the database
	 * 
	 * @param caseReport the case report to submit
	 * @return the submitted case report
	 * @should submit the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport submitCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Marks the specified case report as dismissed in the database
	 *
	 * @param caseReport the case report to dismiss
	 * @return the dismissed case report
	 * @should dismiss the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport dismissCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Marks the specified case report as voided
	 * 
	 * @param caseReport the case report to void
	 * @param voidReason for voiding
	 * @return the voided case report
	 * @should void the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport voidCaseReport(CaseReport caseReport, String voidReason) throws APIException;
	
	/**
	 * Marks the specified case report as not voided
	 *
	 * @param caseReport the case report to unvoid
	 * @return the none voided case report
	 * @should unvoid the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport unvoidCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Runs the SQL cohort query with the specified name and creates a case report for each matched
	 * patient of none exists
	 *
	 * @param triggerName the name of the sql cohort query to be run
	 * @throws APIException
	 * @throws EvaluationException
	 * @should create case reports for the matched patients
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	void runTrigger(String triggerName) throws APIException, EvaluationException;
	
	/**
	 * Gets the SqlCohortDefinition that matches the specified trigger name, will throw an
	 * APIException if multiple cohort queries are found that match the trigger name
	 * 
	 * @param triggerName the name to match against
	 * @return the sql cohort query that matches the name
	 * @throws APIException
	 * @should return null if no cohort query is found that matches the trigger name
	 * @should fail if multiple cohort queries are found that match the trigger name
	 * @should not return a retired cohort query
	 * @should return the matched cohort query
	 */
	SqlCohortDefinition getSqlCohortDefinition(String triggerName) throws APIException;
}
