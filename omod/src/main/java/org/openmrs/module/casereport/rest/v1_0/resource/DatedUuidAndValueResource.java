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

import org.openmrs.module.casereport.DatedUuidAndValue;
import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;

@Resource(name = CaseReportRestConstants.REST_NAMESPACE + "/dateduuidandvalue", supportedClass = DatedUuidAndValue.class, supportedOpenmrsVersions = { "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*"  })
public class DatedUuidAndValueResource extends UuidAndValueResource {
	
	/**
	 * @see UuidAndValueResource#getRepresentationDescription(Representation)
	 */
	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
		DelegatingResourceDescription description = super.getRepresentationDescription(rep);
		description.addRequiredProperty("date");
		return description;
	}
	
	/**
	 * @see UuidAndValueResource @getCreatableProperties()
	 */
	@Override
	public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
		return getRepresentationDescription(null);
	}
	
	/**
	 * @see DelegatingCrudResource#newDelegate()
	 */
	@Override
	public DatedUuidAndValue newDelegate() {
		return new DatedUuidAndValue();
	}
	
	/**
	 * @see DelegatingCrudResource#getByUniqueId(String)
	 */
	@Override
	public DatedUuidAndValue getByUniqueId(String uniqueId) {
		return new DatedUuidAndValue(uniqueId, null, null);
	}
}
