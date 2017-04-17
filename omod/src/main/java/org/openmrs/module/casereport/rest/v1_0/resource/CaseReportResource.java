/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.v1_0.resource;

import static org.openmrs.module.casereport.CaseReport.Status;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.CaseReportUtil;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.casereport.rest.CaseReportRestException;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.GenericRestException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.PatientResource1_9;

@Resource(name = CaseReportRestConstants.REST_NAMESPACE + "/casereport", supportedClass = CaseReport.class, supportedOpenmrsVersions = { "1.11.*,1.12.*" })
public class CaseReportResource extends DataDelegatingCrudResource<CaseReport> {
	
	/**
	 * @see DataDelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		if (representation instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("patient", Representation.DEFAULT);
			description.addProperty("status");
			description.addProperty("resolutionDate");
			description.addProperty("reportTriggers", Representation.DEFAULT);
			description.addProperty("voided");
			description.addSelfLink();
			description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (representation instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("display");
			description.addProperty("patient", Representation.DEFAULT);
			description.addProperty("status");
			description.addProperty("resolutionDate");
			description.addProperty("reportForm");
			description.addProperty("reportTriggers", Representation.DEFAULT);
			description.addProperty("voided");
			description.addProperty("auditInfo");
			description.addSelfLink();
			return description;
		}
		return null;
	}
	
	/**
	 * @see DataDelegatingCrudResource#newDelegate()
	 */
	@Override
	public CaseReport newDelegate() {
		//this is never really called since we override the create method and implement other creation logic
		return null;
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("patient");
		description.addRequiredProperty("reportTriggers");
		return description;
	}
	
	@PropertySetter("reportTriggers")
	public void setTriggers(CaseReport instance, CaseReportTrigger... reportTriggers) {
		for (CaseReportTrigger trigger : reportTriggers) {
			instance.addTrigger(trigger);
		}
	}
	
	@PropertyGetter("display")
	public String getDisplayString(CaseReport delegate) {
		return delegate.toString();
	}
	
	@PropertyGetter("reportForm")
	public CaseReportForm getReportForm(CaseReport delegate) {
		CaseReportForm form;
		if (StringUtils.isBlank(delegate.getReportForm())) {
			form = new CaseReportForm(delegate);
		} else {
			try {
				form = new ObjectMapper().readValue(delegate.getReportForm(), CaseReportForm.class);
			}
			catch (IOException e) {
				throw new GenericRestException("Failed to parse report form data", e);
			}
		}
		return form;
	}
	
	/**
	 * @see DataDelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public CaseReport getByUniqueId(String uniqueId) {
		return Context.getService(CaseReportService.class).getCaseReportByUuid(uniqueId);
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#create(SimpleObject,
	 *      RequestContext)
	 */
	@Override
	public Object create(SimpleObject propertiesToCreate, RequestContext context) throws ResponseException {
		CaseReport caseReport = new CaseReport();
		setConvertedProperties(caseReport, propertiesToCreate, this.getCreatableProperties(), true);
		
		//Hack to avoid creating duplicates for the same patient
		String[] triggerNames = new String[caseReport.getReportTriggers().size()];
		int index = 0;
		for (CaseReportTrigger t : caseReport.getReportTriggers()) {
			triggerNames[index] = t.getName();
			index++;
		}
		caseReport = CaseReportUtil.createReportIfNecessary(caseReport.getPatient(), triggerNames);
		if (caseReport == null) {
			throw new CaseReportRestException(CaseReportConstants.MODULE_ID + ".error.trigger.duplicate",
			        new Object[] { triggerNames[0] });
		}
		
		caseReport = save(caseReport);
		
		return ConversionUtil.convertToRepresentation(caseReport, Representation.DEFAULT);
	}
	
	/**
	 * @see DataDelegatingCrudResource#save(Object)
	 */
	@Override
	public CaseReport save(CaseReport caseReport) {
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @see DataDelegatingCrudResource#doSearch(RequestContext)
	 */
	@Override
	protected PageableResult doSearch(RequestContext context) {
		Patient patient = null;
		Status[] statuses = null;
		if (StringUtils.isNotBlank(context.getParameter(CaseReportRestConstants.PARAM_STATUS))) {
			String[] values = StringUtils.split(context.getParameter(CaseReportRestConstants.PARAM_STATUS).trim(), ",");
			statuses = new Status[values.length];
			for (int i = 0; i < statuses.length; i++) {
				statuses[i] = Status.valueOf(values[i]);
			}
		}
		
		CaseReportService service = Context.getService(CaseReportService.class);
		RestService rs = Context.getService(RestService.class);
		if (StringUtils.isNotBlank(context.getParameter(CaseReportRestConstants.PARAM_PATIENT))) {
			String uuid = context.getParameter(CaseReportRestConstants.PARAM_PATIENT);
			patient = ((PatientResource1_9) rs.getResourceBySupportedClass(Patient.class)).getByUniqueId(uuid);
		}
		
		return new NeedsPaging<>(service.getCaseReports(patient, context.getIncludeAll(), null, null, statuses), context);
	}
	
	/**
	 * @see DataDelegatingCrudResource#doGetAll(RequestContext)
	 */
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		//This actually returns the case report queue and should never return all
		Status[] statuses = new Status[] { Status.NEW, Status.DRAFT };
		CaseReportService service = Context.getService(CaseReportService.class);
		
		return new NeedsPaging(service.getCaseReports(null, context.getIncludeAll(), "dateCreated", true, statuses), context);
	}
	
	/**
	 * @see DataDelegatingCrudResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(CaseReport caseReport, String reason, RequestContext requestContext) throws ResponseException {
		Context.getService(CaseReportService.class).voidCaseReport(caseReport, reason);
	}
	
	/**
	 * @see DataDelegatingCrudResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(CaseReport caseReport, RequestContext requestContext) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
}
