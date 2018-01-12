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
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Concept;
import org.openmrs.ImplementationId;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportTask;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.CaseReportUtil;
import org.openmrs.module.casereport.Trigger;
import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.openmrs.module.casereport.api.db.CaseReportDAO;
import org.openmrs.scheduler.TaskDefinition;
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
	
	private List<Trigger> triggers = null;
	
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
		
		CaseReportService crs = Context.getService(CaseReportService.class);
		List<CaseReport> caseReports = crs.getCaseReports(patient, false, null, null, getQueueStatuses());
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
		return Context.getService(CaseReportService.class).getCaseReports(null, false, "dateCreated", true,
		    getQueueStatuses());
	}
	
	/**
	 * @see CaseReportService#getCaseReports(Patient, boolean, String, Boolean, Status...)
	 */
	@Override
	public List<CaseReport> getCaseReports(Patient patient, boolean includeVoided, String orderBy, Boolean asc,
	                                       Status... statuses) {
		return dao.getCaseReports(patient, includeVoided, orderBy, asc, statuses);
	}
	
	/**
	 * @See CaseReportService#saveCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport saveCaseReport(CaseReport caseReport) throws APIException {
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
		
		//We need to find the task associated to the queue item so we can get the triggers concept
		Collection<TaskDefinition> taskDefinitions = getCaseReportTaskDefinitions();
		for (CaseReportTrigger crt : caseReport.getReportTriggers()) {
			Concept triggerConcept = null;
			for (TaskDefinition taskDef : taskDefinitions) {
				String tName = taskDef.getProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY);
				//if this queue item was created by this task
				if (crt.getName().equalsIgnoreCase(tName)) {
					String mapping = taskDef.getProperty(CaseReportConstants.CONCEPT_TASK_PROPERTY);
					if (StringUtils.isNotBlank(mapping)) {
						if (mapping.startsWith(CaseReportConstants.CIEL_MAPPING_PREFIX)) {
							triggerConcept = CaseReportUtil.getConceptByMappingString(mapping, true);
							break;
						} else {
							throw new APIException("Only CIEL concept mappings are currently allowed");
						}
					}
				}
			}
			
			if (triggerConcept == null) {
				throw new APIException("No concept was found that is linked to the trigger: " + crt.getName());
			}
		}
		
		CaseReportForm form;
		try {
			form = getObjectMapper().readValue(caseReport.getReportForm(), CaseReportForm.class);
			form.setReportUuid(caseReport.getUuid());
			form.setReportDate(caseReport.getDateCreated());
		}
		catch (IOException e) {
			throw new APIException("Failed to parse case report form data", e);
		}
		
		Provider provider;
		if (caseReport.getAutoSubmitted()) {
			String uuid = Context.getAdministrationService().getGlobalProperty(
			    CaseReportConstants.GP_AUTO_SUBMIT_PROVIDER_UUID);
			if (StringUtils.isBlank(uuid)) {
				throw new APIException(CaseReportConstants.GP_AUTO_SUBMIT_PROVIDER_UUID
				        + " global property value is required to allow auto submission of case reports");
			}
			provider = Context.getProviderService().getProviderByUuid(uuid);
			if (provider == null) {
				throw new APIException("No provider account found with uuid: " + uuid);
			}
		} else {
			User user = Context.getAuthenticatedUser();
			Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(user.getPerson(), false);
			if (providers.isEmpty()) {
				throw new APIException("A user needs to have a provider account to be able to submit case reports");
			}
			provider = providers.iterator().next();
		}
		
		if (StringUtils.isBlank(provider.getIdentifier())) {
			throw new APIException("A provider account of a case report submitter needs to have an identifier");
		} else if (provider.getPerson() == null) {
			if (provider.getName() == null || StringUtils.split(provider.getName().trim()).length < 2) {
				throw new APIException("A provider account of a case report submitter has to be linked to a "
				        + "person record or should have a name with at least 2 name fields specified");
			}
		}
		
		form.setSubmitter(new UuidAndValue(provider.getUuid(), provider.getIdentifier()));
		
		ImplementationId implId = Context.getAdministrationService().getImplementationId();
		if (implId == null || StringUtils.isBlank(implId.getImplementationId())) {
			throw new APIException("Implementation id must be set to submit case reports if the submitter and "
			        + "assigning authority id are not set");
		}
		form.setAssigningAuthorityId(implId.getImplementationId());
		form.setAssigningAuthorityName(implId.getName());
		setProperty(caseReport, "status", Status.SUBMITTED);
		setProperty(caseReport, "resolutionDate", new Date());
		
		try {
			caseReport.setReportForm(getObjectMapper().writeValueAsString(form));
		}
		catch (IOException e) {
			throw new APIException("Failed to serialize case report form data", e);
		}
		
		//We use a publisher consumer approach to keep the web layer out of the api
		//It also provides a hook for others to register custom listeners to take other actions.
		eventPublisher.publishEvent(new CaseReportSubmittedEvent(caseReport));
		
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
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
		setProperty(caseReport, "resolutionDate", new Date());
		
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#voidCaseReport(CaseReport,String)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport voidCaseReport(CaseReport caseReport, String voidReason) throws APIException {
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @See CaseReportService#unvoidCaseReport(CaseReport)
	 */
	@Override
	@Transactional(readOnly = false)
	public CaseReport unvoidCaseReport(CaseReport caseReport) throws APIException {
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @see CaseReportService#getSubmittedCaseReports(Patient)
	 */
	@Override
	public List<CaseReport> getSubmittedCaseReports(Patient patient) throws APIException {
		return Context.getService(CaseReportService.class).getCaseReports(patient, false, "resolutionDate", false,
		    Status.SUBMITTED);
	}
	
	/**
	 * @see CaseReportService#getTriggers()
	 */
	@Override
	public List<Trigger> getTriggers() {
		if (triggers == null) {
			triggers = new ArrayList<>();
			for (TaskDefinition td : getCaseReportTaskDefinitions()) {
				triggers.add(new Trigger(td.getProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY)));
			}
		}
		
		return triggers;
	}
	
	private List<TaskDefinition> getCaseReportTaskDefinitions() {
		List<TaskDefinition> taskDefinitions = new ArrayList<TaskDefinition>();
		Collection<TaskDefinition> taskDefs = Context.getSchedulerService().getRegisteredTasks();
		for (TaskDefinition taskDef : taskDefs) {
			if (CaseReportTask.class.getName().equals(taskDef.getTaskClass())) {
				taskDefinitions.add(taskDef);
			}
		}
		return taskDefinitions;
	}
	
	/**
	 * Returns the statuses that constitute queue items
	 * 
	 * @return an array of statuses
	 */
	private Status[] getQueueStatuses() {
		return new Status[] { Status.NEW, Status.DRAFT };
	}
}
