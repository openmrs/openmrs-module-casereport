/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.web.rest.v1_0.resource;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
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

@Resource(name = RestConstants.VERSION_1 + "/casereport/queue", supportedClass = CaseReport.class, supportedOpenmrsVersions = {
        "1.10.*", "1.11.*,1.12.*" })
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
		return new CaseReport();
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
	 * @see DataDelegatingCrudResource#save(Object)
	 */
	@Override
	public CaseReport save(CaseReport caseReport) {
		return Context.getService(CaseReportService.class).saveCaseReport(caseReport);
	}
	
	/**
	 * @see DataDelegatingCrudResource#doGetAll(RequestContext)
	 */
	@Override
	protected PageableResult doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<CaseReport>(Context.getService(CaseReportService.class).getCaseReports(
		    context.getIncludeAll(), false, false), context);
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
