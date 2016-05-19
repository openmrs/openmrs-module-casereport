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
	
	public static final String DEFAULT_PATTERN = "valid_cohortqueries/*.json";
	
	private String pattern = DEFAULT_PATTERN;
	
	/**
	 * @see ClasspathScanningSqlCohortQueryLoader#getPathPattern()
	 */
	@Override
	public String getPathPattern() {
		return pattern;
	}
	
	public void setPathPattern(String pattern) {
		this.pattern = pattern;
	}
}
