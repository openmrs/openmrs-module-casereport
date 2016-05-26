/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.web.v1_0.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/casereport/queue", supportedClass = CaseReport.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.10.*", "1.11.*" })
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
			description.addProperty("patient", Representation.REF);
			description.addProperty("status");
			description.addProperty("reportTriggers", Representation.REF);
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
	
	@PropertyGetter("reportForm")
	public String getReportForm(CaseReport delegate) {
		return delegate.getReportForm();
	}
	
	@PropertyGetter("display")
	public String getDisplayString(CaseReport delegate) {
		return delegate.toString();
	}
	
	/**
	 * @see DataDelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public CaseReport getByUniqueId(String uniqueId) {
		return Context.getService(CaseReportService.class).getCaseReportByUuid(uniqueId);
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
		//TODO Implement
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DataDelegatingCrudResource#newDelegate()
	 */
	@Override
	public CaseReport newDelegate() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DataDelegatingCrudResource#save(Object)
	 */
	@Override
	public CaseReport save(CaseReport caseReport) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DataDelegatingCrudResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(CaseReport caseReport, RequestContext requestContext) throws ResponseException {
		throw new UnsupportedOperationException();
	}
}
