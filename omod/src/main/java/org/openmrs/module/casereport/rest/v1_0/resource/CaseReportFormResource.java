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

import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@SubResource(parent = CaseReportResource.class, path = "reportform", supportedClass = CaseReportForm.class, supportedOpenmrsVersions = { "1.11.*,1.12.*" })
public class CaseReportFormResource extends DelegatingSubResource<CaseReportForm, CaseReport, CaseReportResource> {
	
	/**
	 * @see DelegatingSubResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("reportUuid");
		description.addRequiredProperty("reportDate");
		description.addProperty("givenName");
		description.addProperty("middleName");
		description.addProperty("familyName");
		description.addProperty("fullName");
		description.addProperty("gender");
		description.addProperty("birthdate");
		description.addProperty("deathdate");
		description.addProperty("dead");
		description.addProperty("patientIdentifier");
		description.addProperty("identifierType");
		description.addProperty("causeOfDeath");
		description.addProperty("triggers");
		description.addProperty("mostRecentViralLoads");
		description.addProperty("mostRecentCd4Counts");
		description.addProperty("mostRecentHivTests");
		description.addProperty("currentHivWhoStage");
		description.addProperty("currentHivMedications");
		description.addProperty("mostRecentArvStopReason");
		description.addProperty("lastVisitDate");
		description.addProperty("submitter");
		description.addProperty("comments");
		return description;
	}
	
	/**
	 * @see DelegatingSubResource@getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		return getRepresentationDescription(null);
	}
	
	/**
	 * @see DelegatingSubResource#newDelegate()
	 */
	@Override
	public CaseReportForm newDelegate() {
		return new CaseReportForm();
	}
	
	/**
	 * @see DelegatingSubResource#save(Object)
	 */
	@Override
	public CaseReportForm save(CaseReportForm delegate) {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	/**
	 * @see DelegatingSubResource#getParent(Object)
	 */
	@Override
	public CaseReport getParent(CaseReportForm instance) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#setParent(Object, Object)
	 */
	@Override
	public void setParent(CaseReportForm instance, CaseReport parent) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#doGetAll(Object, RequestContext)
	 */
	@Override
	public PageableResult doGetAll(CaseReport parent, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#getByUniqueId(String)
	 */
	@Override
	public CaseReportForm getByUniqueId(String uniqueId) {
		throw new ResourceDoesNotSupportOperationException();
	}
	
	/**
	 * @see DelegatingSubResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(CaseReportForm delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	/**
	 * @see DelegatingSubResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(CaseReportForm delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
}
