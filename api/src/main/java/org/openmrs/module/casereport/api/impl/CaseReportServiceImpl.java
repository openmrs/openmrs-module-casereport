/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.api.impl;

import static org.openmrs.module.casereport.CaseReport.Status;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.ImplementationId;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.openmrs.module.casereport.api.db.CaseReportDAO;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.validator.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link CaseReportService}.
 */
@Transactional(readOnly = true)
public class CaseReportServiceImpl extends BaseOpenmrsService implements CaseReportService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private CaseReportDAO dao;
	
	private ObjectMapper mapper = null;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	/**
	 * @param dao the dao to set
	 */
	public void setDao(CaseReportDAO dao) {
		this.dao = dao;
	}
	
	private ObjectMapper getObjectMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
		}
		return mapper;
	}
	
	private void setProperty(CaseReport caseReport, String propertyName, Object value) {
		
		Boolean isAccessible = null;
		Field field = null;
		try {
			field = CaseReport.class.getDeclaredField(propertyName);
			field.setAccessible(true);
			field.set(caseReport, value);
		}
		catch (Exception e) {
			throw new APIException("Failed to set " + propertyName + " for CaseReport:" + caseReport, e);
		}
		finally {
			if (field != null && isAccessible != null) {
				field.setAccessible(isAccessible);
			}
		}
	}
	
	/**
	 * @See CaseReportService#getCaseReport(Integer)
	 */
	@Override
	public CaseReport getCaseReport(Integer caseReportId) throws APIException {
		return dao.getCaseReport(caseReportId);
	}
	
	/**
	 * @See CaseReportService#getCaseReportByUuid(String)
	 */
	@Override
	public CaseReport getCaseReportByUuid(String uuid) throws APIException {
		return dao.getCaseReportByUuid(uuid);
	}
	
	/**
	 * @see CaseReportService#getCaseReportByPatient(Patient)
	 */
	@Override
	public CaseReport getCaseReportByPatient(Patient patient) throws APIException {
		if (patient == null) {
			throw new APIException("patient is required");
		}
		List<Status> statusesToExclude = Arrays.asList(Status.SUBMITTED, Status.DISMISSED);
		List<CaseReport> caseReports = dao.getCaseReports(patient, statusesToExclude, false);
		if (caseReports.size() == 0) {
			return null;
		} else if (caseReports.size() > 1) {
			throw new APIException("Found multiple case reports(" + caseReports.size() + ") that match the patient with id:"
			        + patient.getId());
		}
		
		return caseReports.get(0);
	}
	
	/**
	 * @See CaseReportService#getCaseReports()
	 */
	@Override
	public List<CaseReport> getCaseReports() throws APIException {
		List<Status> statusesToExclude = Arrays.asList(Status.SUBMITTED, Status.DISMISSED);
		return dao.getCaseReports(null, statusesToExclude, false);
	}
	
	/**
	 * @See CaseReportService#getCaseReports(boolean,boolean, boolean)
	 */
	@Override
	public List<CaseReport> getCaseReports(boolean includeVoided, boolean includeSubmitted, boolean includeDismissed)
	    throws APIException {
		List<Status> statusesToExclude = null;
		if (!includeSubmitted || !includeDismissed) {
			statusesToExclude = new ArrayList<Status>();
			if (!includeSubmitted) {
				statusesToExclude.add(Status.SUBMITTED);
			}
			if (!includeDismissed) {
				statusesToExclude.add(Status.DISMISSED);
			}
		}
		
		return dao.getCaseReports(null, statusesToExclude, includeVoided);
	}
	
	/**
	 * @See CaseReportService#saveCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport saveCaseReport(CaseReport caseReport) throws APIException {
		if (caseReport.getCaseReportId() != null) {
			throw new APIException("Cannot edit a case report, call another appropriate method in CaseReportService");
		}
		
		return dao.saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#submitCaseReport(CaseReport,List, User)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport submitCaseReport(CaseReport caseReport) throws APIException {
		
		if (caseReport.isVoided()) {
			throw new APIException("Cannot submit a voided case report");
		} else if (caseReport.isDismissed()) {
			throw new APIException("Cannot submit a dismissed case report");
		} else if (caseReport.isSubmitted()) {
			throw new APIException("Cannot submit a submitted case report");
		} else if (StringUtils.isBlank(caseReport.getReportForm())) {
			throw new APIException("Case report form cannot be blank");
		}
		
		CaseReportForm form;
		try {
			form = getObjectMapper().readValue(caseReport.getReportForm(), CaseReportForm.class);
		}
		catch (IOException e) {
			throw new APIException("Failed to parse case report form data", e);
		}
		
		boolean requireImplementationId = false;
		if (form.getSubmitter() == null || form.getSubmitter().getValue() == null
		        || StringUtils.isBlank(form.getSubmitter().getValue().toString())) {
			User user = Context.getAuthenticatedUser();
			form.setSubmitter(new UuidAndValue(user.getUuid(), user.getSystemId()));
			requireImplementationId = true;
		}
		
		if (requireImplementationId || StringUtils.isBlank(form.getAssigningAuthorityId())) {
			ImplementationId implId = Context.getAdministrationService().getImplementationId();
			if (implId == null || StringUtils.isBlank(implId.getImplementationId())) {
				throw new APIException("Implementation id must be set to submit case reports if the submitter and "
				        + "assigning authority id are not set");
			}
			form.setAssigningAuthorityId(implId.getImplementationId());
			if (requireImplementationId || StringUtils.isBlank(form.getAssigningAuthorityName())) {
				form.setAssigningAuthorityName(implId.getName());
			}
		}
		
		try {
			setProperty(caseReport, "reportForm", getObjectMapper().writeValueAsString(form));
		}
		catch (IOException e) {
			throw new APIException("Failed to serialize case report form data", e);
		}
		
		//We use a publisher consumer approach to keep the CDA generation logic out of the api
		//It also provides a hook for others to register custom listeners to take other actions
		try {
			eventPublisher.publishEvent(new CaseReportSubmittedEvent(caseReport));
		}
		catch (Throwable t) {
			log.warn("An error occurred while publishing events to the listeners");
		}
		
		setProperty(caseReport, "status", Status.SUBMITTED);
		return dao.saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#dismissCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport dismissCaseReport(CaseReport caseReport) throws APIException {
		if (caseReport.isVoided()) {
			throw new APIException("Cannot dismiss a voided case report");
		} else if (caseReport.isDismissed()) {
			throw new APIException("Cannot dismiss a dismissed case report");
		} else if (caseReport.isSubmitted()) {
			throw new APIException("Cannot dismiss a submitted case report");
		}
		
		setProperty(caseReport, "status", Status.DISMISSED);
		return dao.saveCaseReport(caseReport);
	}
	
	/**
	 * @see CaseReportService#runTrigger(String, TaskDefinition)
	 */
	@Override
	@Transactional(readOnly = false)
	public void runTrigger(String triggerName, TaskDefinition taskDefinition) throws APIException, EvaluationException {
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
			ConceptService cs = Context.getConceptService();
			final String cielMappingPrefix = CaseReportConstants.SOURCE_CIEL_HL7_CODE
			        + CaseReportConstants.CONCEPT_MAPPING_SEPARATOR;
			for (Parameter p : definition.getParameters()) {
				if (p.getName().startsWith(cielMappingPrefix)) {
					String[] sourceAndCode = StringUtils.split(p.getName(), CaseReportConstants.CONCEPT_MAPPING_SEPARATOR);
					String source = sourceAndCode[0];
					String code = sourceAndCode[1];
					Concept concept = cs.getConceptByMapping(code, source);
					if (concept == null) {
						throw new APIException("Failed to find concept with mapping " + source + ":" + code);
					}
					params.put(p.getName(), concept.getConceptId());
				}
			}
		}
		
		evaluationContext.setParameterValues(params);
		Cohort cohort = (Cohort) DefinitionContext.evaluate(definition, evaluationContext);
		
		PatientService ps = Context.getPatientService();
		for (Integer patientId : cohort.getMemberIds()) {
			Patient patient = ps.getPatient(patientId);
			if (patient == null) {
				throw new APIException("No patient found with patientId:" + patientId);
			}
			CaseReport caseReport = getCaseReportByPatient(patient);
			if (caseReport == null) {
				caseReport = new CaseReport(patient, triggerName);
			} else {
				//Don't create a duplicate trigger for the same patient
				if (caseReport.getCaseReportTriggerByName(triggerName) != null) {
					continue;
				}
				caseReport.addTrigger(new CaseReportTrigger(triggerName));
			}
			
			saveCaseReportAfterValidation(caseReport);
		}
	}
	
	private CaseReport saveCaseReportAfterValidation(CaseReport caseReport) {
		ValidateUtil.validate(caseReport);
		return dao.saveCaseReport(caseReport);
	}
	
	/**
	 * @see CaseReportService#getSqlCohortDefinition(String)
	 */
	@Override
	public SqlCohortDefinition getSqlCohortDefinition(String triggerName) throws APIException {
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
	 * @See CaseReportService#voidCaseReport(CaseReport,String)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport voidCaseReport(CaseReport caseReport, String voidReason) throws APIException {
		return dao.saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#unvoidCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport unvoidCaseReport(CaseReport caseReport) throws APIException {
		return saveCaseReportAfterValidation(caseReport);
	}
	
	/**
	 * @see CaseReportService#getSubmittedCaseReports(Patient)
	 */
	@Override
	public List<CaseReport> getSubmittedCaseReports(Patient patient) throws APIException {
		List<Status> statusesToExclude = new ArrayList<Status>(Status.values().length);
		for (Status status : Status.values()) {
			if (status != Status.SUBMITTED) {
				statusesToExclude.add(status);
			}
		}
		
		return dao.getCaseReports(patient, statusesToExclude, false);
	}
}
