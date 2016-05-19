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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Base class for SqlCohortQueryLoader that scan the classpath for json files that contain cohort
 * queries
 */
public abstract class ClasspathScanningSqlCohortQueryLoader implements SqlCohortQueryLoader {
	
	private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
	
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Implementation of this method should return the name of the folder on the classpath that
	 * contains .json files with sql cohort queries
	 * 
	 * @return
	 */
	public abstract String getLocation();
	
	/**
	 * @see SqlCohortQueryLoader#load()
	 */
	@Override
	public List<SqlCohortQuery> load() throws IOException {
		Resource[] resources = resourceResolver.getResources("classpath*:/" + getLocation() + "/*.json");
		List<SqlCohortQuery> sqlCohortQueries = new ArrayList<SqlCohortQuery>();
		for (Resource resource : resources) {
			sqlCohortQueries.add(mapper.readValue(resource.getInputStream(), SqlCohortQuery.class));
			
		}
		
		return sqlCohortQueries;
	}
}
