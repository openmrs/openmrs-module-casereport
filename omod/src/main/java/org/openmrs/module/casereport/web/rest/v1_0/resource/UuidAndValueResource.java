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

import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/casereport/uuidandvalue", supportedClass = UuidAndValue.class, supportedOpenmrsVersions = {
        "1.10.*", "1.11.*,1.12.*" })
public class UuidAndValueResource extends DelegatingCrudResource<UuidAndValue> {
	
	/**
	 * @see DelegatingCrudResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addRequiredProperty("uuid");
		description.addRequiredProperty("value");
		return description;
	}
	
	/**
	 * @see DelegatingCrudResource@getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		return getRepresentationDescription(null);
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public UuidAndValue newDelegate() {
		return new UuidAndValue();
	}
	
	/**
	 * @see DelegatingCrudResource#save(Object)
	 */
	@Override
	public UuidAndValue save(UuidAndValue delegate) {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	/**
	 * @see DelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public UuidAndValue getByUniqueId(String uniqueId) {
		return new UuidAndValue(uniqueId, null);
	}
	
	/**
	 * @see DelegatingCrudResource#delete(Object, String, RequestContext)
	 */
	@Override
	protected void delete(UuidAndValue delegate, String reason, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
	
	/**
	 * @see DelegatingCrudResource#purge(Object, RequestContext)
	 */
	@Override
	public void purge(UuidAndValue delegate, RequestContext context) throws ResponseException {
		throw new ResourceDoesNotSupportOperationException("read-only resource");
	}
}
