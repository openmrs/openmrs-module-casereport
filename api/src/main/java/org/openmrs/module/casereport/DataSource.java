/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport;

import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;

/**
 * Super interface for spring beans that will be invoked to get extra data to include in the
 * generated report form
 */
public interface DataSource {
	
	/**
	 * @param patient the patient associated to the case report that the form data being generated
	 *            belongs to
	 * @return an ObjectNode object
	 */
	ObjectNode getData(Patient patient);
	
}
