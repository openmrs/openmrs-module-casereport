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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.util.OpenmrsUtil;

public class CaseReportUtil {
	
	protected static final Log log = LogFactory.getLog(CaseReportUtil.class);
	
	private static Concept getCeilConceptByCode(String code) {
		Concept concept = Context.getConceptService().getConceptByMapping(code, CaseReportConstants.SOURCE_CIEL_HL7_CODE);
		if (concept == null) {
			throw new APIException("Failed to find concept with mapping " + CaseReportConstants.SOURCE_CIEL_HL7_CODE + ":"
			        + code);
		}
		return concept;
	}
	
	private static List<Obs> getMostRecentObsByPatientAndConceptMapping(Patient patient, String code, Integer limit) {
		if (patient == null) {
			throw new APIException("Patient cannot be null");
		}
		
		List<Person> patients = Collections.singletonList((Person) patient);
		List<Concept> concepts = Collections.singletonList(getCeilConceptByCode(code));
		
		return Context.getObsService().getObservations(patients, null, concepts, null, null, null,
		    Collections.singletonList("obsDatetime"), limit, null, null, null, false);
	}
	
	/**
	 * Gets the 3 most recent viral load observations for the specified patient, they are ordered in
	 * a way such that the most recent comes first and the earliest is last. Note that the method
	 * can return less than 3 items in case the patient has had less than 3 of them in total in the
	 * past.
	 * 
	 * @param patient the patient to match against
	 * @return a list of the most recent viral load observations
	 * @should return the 3 most recent Viral load observations
	 */
	public static List<Obs> getMostRecentViralLoads(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, CaseReportConstants.TERM_CODE_VIRAL_LOAD, 3);
	}
	
	/**
	 * Gets the 3 most recent cd4 count observations for the specified patient, they are ordered in
	 * a way such that the most recent comes first and the earliest is last. Note that the method
	 * can return less than 3 items in case the patient has had less than 3 of them in total in the
	 * past.
	 * 
	 * @param patient the patient to match against
	 * @return a list of the most recent cd4 count observations
	 * @should return the 3 most recent cd4 count observations
	 */
	public static List<Obs> getMostRecentCD4counts(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, CaseReportConstants.TERM_CODE_CD4_COUNT, 3);
	}
	
	/**
	 * Gets the 3 most HIV test result observations for the specified patient, they are ordered in a
	 * way such that the most recent comes first and the earliest is last. Note that the method can
	 * return less than 3 items in case the patient has had less than 3 of them in total in the
	 * past.
	 * 
	 * @param patient the patient to match against
	 * @return a list of the most recent HIV test observations
	 * @should return the 3 most recent HIV test observations
	 */
	public static List<Obs> getMostRecentHIVTests(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, CaseReportConstants.TERM_CODE_HIV_TEST, 3);
	}
	
	/**
	 * Gets the most recent WHO stage observation for the specified patient.
	 * 
	 * @param patient the patient to match against
	 * @return the most recent WHO stage observation
	 * @should return the most recent WHO stage observation
	 */
	public static Obs getMostRecentWHOStage(Patient patient) {
		List<Obs> whoStages = getMostRecentObsByPatientAndConceptMapping(patient, CaseReportConstants.TERM_CODE_WHO_STAGE, 1);
		if (whoStages.isEmpty()) {
			return null;
		}
		return whoStages.get(0);
	}
	
	/**
	 * Gets the current ARV medications for the specified patient
	 * 
	 * @param patient the patient to match against
	 * @param asOfDate
	 * @return a list of ARV medications
	 * @should get the current ARV medications for the specified patient
	 */
	public static List<Drug> getCurrentARVMedications(Patient patient, Date asOfDate) {
		List<Drug> arvs = new ArrayList<Drug>();
		Concept arvMedset = getCeilConceptByCode(CaseReportConstants.TERM_CODE_ARV_MED_SET);
		OrderService os = Context.getOrderService();
		OrderType orderType = os.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
		List<Order> orders = os.getActiveOrders(patient, orderType, null, asOfDate);
		for (Order order : orders) {
			DrugOrder drugOrder = (DrugOrder) order;
			if (arvMedset.getSetMembers().contains(order.getConcept()) && !arvs.contains(drugOrder.getDrug())) {
				arvs.add(drugOrder.getDrug());
			}
		}
		return arvs;
	}
	
	/**
	 * Gets the most recent observation for the reason why the specified patient stopped taking
	 * ARVs.
	 *
	 * @param patient the patient to match against
	 * @return the most recent observation for the reason why the patient stopped taking ARVs
	 * @should return the most recent obs for the reason why the patient stopped taking ARVs
	 */
	public static Obs getMostRecentReasonARVsStopped(Patient patient) {
		List<Obs> reasons = getMostRecentObsByPatientAndConceptMapping(patient,
		    CaseReportConstants.TERM_CODE_REASON_FOR_STOPPING_ARVS, 1);
		if (reasons.isEmpty()) {
			return null;
		}
		return reasons.get(0);
	}
	
	/**
	 * Gets the last visit made by the specified patient
	 *
	 * @param patient the patient to match against
	 * @return the last visit for the specified patient
	 * @should return the last visit for the specified patient
	 */
	public static Visit getLastVisit(Patient patient) {
		final List<Visit> visits = Context.getVisitService().getVisitsByPatient(patient, true, false);
		Collections.sort(visits, Collections.reverseOrder(new Comparator<Visit>() {
			
			@Override
			public int compare(Visit v1, Visit v2) {
				return OpenmrsUtil.compare(v1.getStartDatetime(), v2.getStartDatetime());
			}
		}));
		
		if (visits.isEmpty()) {
			return null;
		}
		return visits.get(0);
	}
	
	public static boolean collContainsItemWithValue(Collection<? extends UuidAndValue> coll, String value) {
		for (UuidAndValue uv : coll) {
			if (value.equals(uv.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Gets the concept that matches the specified mapping string, the mapping is expected to be of
	 * the form SOURCE_IDENTIFIER e.g CIEL_856
	 * 
	 * @param mappingString the string to match against
	 * @param failIfNotFound specifies if an exception should be thrown if no match is found
	 * @return the matched concept
	 */
	public static Concept getConceptByMappingString(String mappingString, boolean failIfNotFound) {
		String[] sourceAndCode = StringUtils.split(mappingString, CaseReportConstants.CONCEPT_MAPPING_SEPARATOR);
		String source = sourceAndCode[0];
		String code = sourceAndCode[1];
		Concept concept = Context.getConceptService().getConceptByMapping(code, source);
		if (concept == null && failIfNotFound) {
			throw new APIException("Failed to find concept with mapping " + source + ":" + code);
		}
		return concept;
	}
	
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
	public static SqlCohortDefinition getSqlCohortDefinition(String triggerName) throws APIException {
		SqlCohortDefinition ret = null;
		List<SqlCohortDefinition> matches = DefinitionContext.getDefinitionService(SqlCohortDefinition.class)
		        .getDefinitions(triggerName, true);
		if (matches.size() > 1) {
			throw new APIException("Found multiple Sql Cohort Queries with name:" + triggerName);
		} else if (matches.size() == 0 || matches.get(0).isRetired()) {
			String msg;
			if (matches.size() == 0) {
				msg = "Cannot find a Sql Cohort Query with name:" + triggerName;
			} else {
				msg = triggerName + " is a retired Sql Cohort Query";
			}
			log.warn(msg);
		} else {
			ret = matches.get(0);
		}
		
		return ret;
	}
	
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
	 * @should fail for a task where the last execution time cannot be resolved
	 */
	public static void runTrigger(String triggerName, TaskDefinition taskDefinition) throws APIException,
	    EvaluationException {
		SqlCohortDefinition definition = getSqlCohortDefinition(triggerName);
		if (definition == null) {
			throw new APIException("No sql cohort query was found that matches the name:" + triggerName);
		}
		
		EvaluationContext evaluationContext = new EvaluationContext();
		Map<String, Object> params = new HashMap<String, Object>();
		if (definition.getParameter(CaseReportConstants.LAST_EXECUTION_TIME) != null) {
			Date lastExecutionTime = null;
			if (taskDefinition != null) {
				lastExecutionTime = taskDefinition.getLastExecutionTime();
				if (lastExecutionTime == null && taskDefinition.getRepeatInterval() != null
				        && taskDefinition.getRepeatInterval() > 0) {
					//TODO add a unit test for this
					//default to now minus repeat interval
					lastExecutionTime = DateUtils.addSeconds(new Date(), -taskDefinition.getRepeatInterval().intValue());
				}
			}
			if (lastExecutionTime == null) {
				throw new APIException("Failed to resolve the value for the last execution time");
			}
			params.put(CaseReportConstants.LAST_EXECUTION_TIME, lastExecutionTime);
		}
		
		if (definition.getParameters() != null) {
			for (Parameter p : definition.getParameters()) {
				if (p.getName().startsWith(CaseReportConstants.CIEL_MAPPING_PREFIX)) {
					Concept concept = CaseReportUtil.getConceptByMappingString(p.getName(), true);
					params.put(p.getName(), concept.getConceptId());
				}
			}
		}
		
		evaluationContext.setParameterValues(params);
		Cohort cohort = (Cohort) DefinitionContext.evaluate(definition, evaluationContext);
		
		PatientService ps = Context.getPatientService();
		CaseReportService caseReportService = Context.getService(CaseReportService.class);
		for (Integer patientId : cohort.getMemberIds()) {
			Patient patient = ps.getPatient(patientId);
			if (patient == null) {
				throw new APIException("No patient found with patientId:" + patientId);
			}
			CaseReport caseReport = caseReportService.getCaseReportByPatient(patient);
			if (caseReport == null) {
				caseReport = new CaseReport(patient, triggerName);
			} else {
				//Don't create a duplicate trigger for the same patient
				if (caseReport.getCaseReportTriggerByName(triggerName) != null) {
					continue;
				}
				caseReport.addTrigger(new CaseReportTrigger(triggerName));
			}
			
			caseReportService.saveCaseReport(caseReport);
		}
	}
}
