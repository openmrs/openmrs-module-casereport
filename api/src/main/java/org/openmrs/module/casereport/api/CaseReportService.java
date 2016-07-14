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
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Contains methods for processing CRUD operations related to case reports
 */
public interface CaseReportService extends OpenmrsService {
	
	/**
	 * Gets a CaseReport that matches the specified id
	 * 
	 * @param caseReportId the id to match against
	 * @return the case report that matches the specified id
	 * @throws APIException
	 * @should return the case report that matches the specified id
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReport(Integer caseReportId) throws APIException;
	
	/**
	 * Gets a CaseReport that matches the specified uuid
	 *
	 * @param uuid the uuid to match against
	 * @return the case report that matches the specified uuid
	 * @throws APIException
	 * @should return the case report that matches the specified uuid
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReportByUuid(String uuid) throws APIException;
	
	/**
	 * Gets the non voided case report for the specified patient.
	 *
	 * @param patient the patient match against
	 * @return a list of the case reports for the patient
	 * @throws APIException
	 * @should get the case report for the patient
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	CaseReport getCaseReportByPatient(Patient patient) throws APIException;
	
	/**
	 * Gets all non voided case reports from the database that are not yet submitted nor dismissed
	 * 
	 * @return all non voided case reports in the database
	 * @throws APIException
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
	 * @throws APIException
	 * @should return all case reports in the database if all arguments are set to true
	 * @should include voided reports in the database if includeVoided is set to true
	 * @should include submitted reports in the database if includeSubmitted is set to true
	 * @should include dismissed reports in the database if includeDismissed is set to true
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getCaseReports(boolean includeVoided, boolean includeSubmitted, boolean includeDismissed)
	    throws APIException;
	
	/**
	 * Saves a case report to the database, developers should typically call #runTrigger() which
	 * will create case reports if necessary and only call this method to update an existing case
	 * report.
	 * 
	 * @see #runTrigger(String, TaskDefinition)
	 * @param caseReport the case report to save
	 * @return the saved case report
	 * @throws APIException
	 * @should return the saved case report
	 * @should fail when updating existing case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport saveCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Marks the specified case report as submitted in the database. If the submitter details are
	 * not set in the report form, they will default to the logged in user
	 * 
	 * @param caseReport the case report to submit
	 * @param triggersToExclude the triggers to exclude from the submitted report
	 * @param submitter the user submitting the report, defaults to logged in user
	 * @return the submitted case report
	 * @throws APIException
	 * @should submit the specified case report
	 * @should fail if the case report is voided
	 * @should fail if the implementation id is not set
	 * @should set the specified submitter and exclude the specified triggers
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport submitCaseReport(CaseReport caseReport, List<String> triggersToExclude, User submitter) throws APIException;
	
	/**
	 * Marks the specified case report as dismissed in the database
	 *
	 * @param caseReport the case report to dismiss
	 * @return the dismissed case report
	 * @throws APIException
	 * @should dismiss the specified case report
	 * @should fail if the case report is voided
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport dismissCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Runs the SQL cohort query with the specified name and creates a case report for each matched
	 * patient of none exists
	 *
	 * @param triggerName the name of the sql cohort query to be run
	 * @param taskDefinition the scheduler taskDefinition inside which the trigger is being run
	 * @throws APIException
	 * @throws EvaluationException
	 * @should fail if no sql cohort query matches the specified trigger name
	 * @should create case reports for the matched patients
	 * @should set the last execution time in the evaluation context
	 * @should add a new trigger to an existing queue item for the patient
	 * @should not create a duplicate trigger for the same patient
	 * @should set the concept mappings in the evaluation context
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	void runTrigger(String triggerName, TaskDefinition taskDefinition) throws APIException, EvaluationException;
	
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
	
	/**
	 * Marks the specified case report as voided
	 *
	 * @param caseReport the case report to void
	 * @param voidReason for voiding
	 * @return the voided case report
	 * @throws APIException
	 * @should void the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport voidCaseReport(CaseReport caseReport, String voidReason) throws APIException;
	
	/**
	 * Marks the specified case report as not voided
	 *
	 * @param caseReport the case report to unvoid
	 * @return the none voided case report
	 * @throws APIException
	 * @should unvoid the specified case report
	 */
	@Authorized(CaseReportConstants.PRIV_MANAGE_CASE_REPORTS)
	CaseReport unvoidCaseReport(CaseReport caseReport) throws APIException;
	
	/**
	 * Gets all the previously submitted case reports for the specified patient
	 *
	 * @param patient the patient match against
	 * @return a list of previous submitted case reports for the patient
	 * @throws APIException
	 * @should fail if patient is null
	 * @should return all the previously submitted case reports for the specified patient
	 */
	@Authorized(CaseReportConstants.PRIV_GET_CASE_REPORTS)
	List<CaseReport> getSubmittedCaseReports(Patient patient) throws APIException;
}
