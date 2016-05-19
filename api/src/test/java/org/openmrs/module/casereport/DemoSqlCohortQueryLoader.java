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

import org.springframework.stereotype.Component;

/**
 * An implementation of SqlCohortQueryLoader for testing purposes
 */
@Component("demoSqlCohortQueryLoader")
public class DemoSqlCohortQueryLoader extends ClasspathScanningSqlCohortQueryLoader {
	
	public static final String DEFAULT_LOCATION = "test_cohortqueries";
	
	private String locationName = DEFAULT_LOCATION;
	
	/**
	 * @see ClasspathScanningSqlCohortQueryLoader#getLocation()
	 */
	@Override
	public String getLocation() {
		return locationName;
	}
	
	public void setLocation(String location) {
		this.locationName = location;
	}
}
