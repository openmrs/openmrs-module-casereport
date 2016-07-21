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

import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportTrigger;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@SubResource(parent = CaseReportResource.class, path = "trigger", supportedClass = CaseReportTrigger.class, supportedOpenmrsVersions = {
        "1.8.*", "1.9.*", "1.10.*", "1.11.*" })
public class CaseReportTriggerResource extends DelegatingSubResource<CaseReportTrigger, CaseReport, CaseReportResource> {
	
	/**
	 * @see DataDelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
		if (representation instanceof DefaultRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("display");
			//description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
			return description;
		} else if (representation instanceof FullRepresentation) {
			DelegatingResourceDescription description = new DelegatingResourceDescription();
			description.addProperty("uuid");
			description.addProperty("name");
			description.addProperty("display");
			description.addProperty("auditInfo");
			return description;
		}
		return null;
	}
	
	@PropertyGetter("display")
	public String getDisplayString(CaseReportTrigger delegate) {
		return delegate.toString();
	}
	
	/**
	 * @see DelegatingSubResource#getParent(Object)
	 */
	@Override
	public CaseReport getParent(CaseReportTrigger instance) {
		return instance.getCaseReport();
	}
	
	/**
	 * @see DelegatingSubResource#setParent(Object, Object)
	 */
	@Override
	public void setParent(CaseReportTrigger instance, CaseReport parent) {
		parent.addTrigger(instance);
	}
	
	/**
	 * @see DelegatingSubResource#doGetAll(Object, RequestContext)
	 */
	@Override
	public PageableResult doGetAll(CaseReport parent, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#getByUniqueId(String)
	 */
	@Override
	public CaseReportTrigger getByUniqueId(String uniqueId) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(CaseReportTrigger delegate, String reason, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#newDelegate()
	 */
	@Override
	public CaseReportTrigger newDelegate() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#save(Object)
	 */
	@Override
	public CaseReportTrigger save(CaseReportTrigger delegate) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(CaseReportTrigger delegate, RequestContext context) throws ResponseException {
		throw new UnsupportedOperationException();
	}
}
