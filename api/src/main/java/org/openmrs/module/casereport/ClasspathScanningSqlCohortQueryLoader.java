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

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.APIException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Base class for SqlCohortQueryLoaders that scan the classpath for json files that contain cohort
 * queries
 */
public abstract class ClasspathScanningSqlCohortQueryLoader implements SqlCohortQueryLoader {
	
	private PathMatchingResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
	
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * Implementation of this method should the portion of the path pattern to be used when scanning
	 * for .json files that contains sql cohort queries. Note that the pattern should not have a
	 * leading forward slash and should not include 'classpath*:' prefix. An Example pattern is
	 * 'cohortqueries/*.json' which would become 'classpath*:/cohortqueries/*.json' as the path
	 * pattern to be used by the scanner
	 * 
	 * @return
	 */
	public abstract String getPathPattern();
	
	/**
	 * @see SqlCohortQueryLoader#load()
	 */
	@Override
	public List<SqlCohortQuery> load() throws IOException {
		String pattern = getPathPattern();
		if (StringUtils.isBlank(pattern)) {
			throw new APIException("path pattern is required");
		} else if (!pattern.endsWith(".json")) {
			throw new APIException("path pattern should end with .json");
		}
		
		Resource[] resources = resourceResolver.getResources("classpath*:/" + pattern);
		List<SqlCohortQuery> sqlCohortQueries = new ArrayList<SqlCohortQuery>();
		for (Resource resource : resources) {
			sqlCohortQueries.add(mapper.readValue(resource.getInputStream(), SqlCohortQuery.class));
		}
		
		return sqlCohortQueries;
	}
}
