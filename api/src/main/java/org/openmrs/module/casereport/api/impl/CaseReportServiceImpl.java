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

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.api.db.CaseReportDAO;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link CaseReportService}.
 */
@Transactional(readOnly = true)
public class CaseReportServiceImpl extends BaseOpenmrsService implements CaseReportService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private CaseReportDAO dao;
	
	private ObjectMapper mapper = null;
	
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
	
	private void setStatus(CaseReport caseReport, CaseReport.Status status) {
		Boolean isAccessible = null;
		Field field = null;
		try {
			field = CaseReport.class.getDeclaredField("status");
			field.setAccessible(true);
			field.set(caseReport, status);
		}
		catch (Exception e) {
			throw new APIException("Failed to set status for CaseReport:" + caseReport, e);
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
		List<CaseReport> caseReports = dao.getCaseReports(patient, false, false, false);
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
		return dao.getCaseReports(null, false, false, false);
	}
	
	/**
	 * @See CaseReportService#getCaseReports(boolean,boolean, boolean)
	 */
	@Override
	public List<CaseReport> getCaseReports(boolean includeVoided, boolean includeSubmitted, boolean includeDismissed)
	    throws APIException {
		return dao.getCaseReports(null, includeVoided, includeSubmitted, includeDismissed);
	}
	
	/**
	 * @See CaseReportService#saveCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport saveCaseReport(CaseReport caseReport) throws APIException {
		if (CaseReport.Status.SUBMITTED != caseReport.getStatus() && CaseReport.Status.DISMISSED != caseReport.getStatus()) {
			if (StringUtils.isBlank(caseReport.getReportForm())) {
				if (CaseReport.Status.NEW != caseReport.getStatus()) {
					setStatus(caseReport, CaseReport.Status.NEW);
				}
			} else {
				if (CaseReport.Status.DRAFT != caseReport.getStatus()) {
					setStatus(caseReport, CaseReport.Status.DRAFT);
				}
			}
		}
		
		return dao.saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#submitCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport submitCaseReport(CaseReport caseReport) throws APIException {
		if (caseReport.isVoided()) {
			throw new APIException("Can't submit a voided case report");
		}
		//TODO Implement more submission logic here
		setStatus(caseReport, CaseReport.Status.SUBMITTED);
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#dismissCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport dismissCaseReport(CaseReport caseReport) throws APIException {
		if (caseReport.isVoided()) {
			throw new APIException("Can't dismiss a voided case report");
		}
		setStatus(caseReport, CaseReport.Status.DISMISSED);
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @see CaseReportService#runTrigger(String)
	 */
	@Override
	@Transactional(readOnly = false)
	public void runTrigger(String triggerName) throws APIException, EvaluationException {
		SqlCohortDefinition definition = getSqlCohortDefinition(triggerName);
		if (definition != null) {
			EvaluationContext evaluationContext = new EvaluationContext();
			Cohort cohort = (Cohort) DefinitionContext.evaluate(definition, evaluationContext);
			
			PatientService ps = Context.getPatientService();
			CaseReportService service = Context.getService(CaseReportService.class);
			for (Integer patientId : cohort.getMemberIds()) {
				Patient patient = ps.getPatient(patientId);
				if (patient == null) {
					throw new APIException("No patient found with patientId:" + patientId);
				}
				CaseReport caseReport = getCaseReportByPatient(patient);
				if (caseReport == null) {
					caseReport = new CaseReport(patient, triggerName);
				} else {
					caseReport.addTrigger(new CaseReportTrigger(triggerName));
				}
				
				service.saveCaseReport(caseReport);
			}
		}
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
	 * @see CaseReportService#generateReportForm(CaseReport)
	 * @param caseReport
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport generateReportForm(CaseReport caseReport) throws APIException {
		CaseReportService service = Context.getService(CaseReportService.class);
		Patient patient = caseReport.getPatient();
		CaseReportForm form = new CaseReportForm();
		form.setGivenName(patient.getGivenName());
		form.setMiddleName(patient.getMiddleName());
		form.setFamilyName(patient.getFamilyName());
		form.setGender(patient.getGender());
		if (patient.getBirthdate() != null) {
			form.setBirthDate(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(patient.getBirthdate()));
		}
		List<String> triggers = new ArrayList(caseReport.getReportTriggers().size());
		for (CaseReportTrigger tr : caseReport.getReportTriggers()) {
			triggers.add(tr.getName());
		}
		String serializedReportForm;
		try {
			
			serializedReportForm = getObjectMapper().writeValueAsString(form);
		}
		catch (IOException e) {
			throw new APIException(e);
		}
		
		caseReport.setReportForm(serializedReportForm);
		service.saveCaseReport(caseReport);
		
		return caseReport;
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
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
}
