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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class CaseReportActivator extends BaseModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Case Report Module");
	}
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 * @should fail for a query with no name
	 * @should fail for a query with no sql
	 * @should ignore a cohort query with a duplicate name
	 * @should save a cohort queries with a name that matches a retired duplicate
	 * @should load queries and register them with the reporting module
	 */
	public void contextRefreshed() {
		
		log.info("Loading queries...");
		
		List<SqlCohortQueryLoader> loaders = new ArrayList<SqlCohortQueryLoader>();
		loaders.addAll(Context.getRegisteredComponents(SqlCohortQueryLoader.class));
		if (loaders.isEmpty()) {
			loaders.add(new DefaultSqlCohortQueryLoader());
		}
		
		List<SqlCohortQuery> cohortQueries = new ArrayList<SqlCohortQuery>();
		for (SqlCohortQueryLoader loader : loaders) {
			try {
				cohortQueries.addAll(loader.load());
			}
			catch (IOException e) {
				throw new APIException("Failed to load some cohort queries by SqlQueryLoader: " + loader, e);
			}
		}
		
		for (SqlCohortQuery cohortQuery : cohortQueries) {
			if (StringUtils.isBlank(cohortQuery.getName())) {
				throw new APIException("Failed to load cohort query because of missing name field");
			} else if (StringUtils.isBlank(cohortQuery.getSql())) {
				throw new APIException("Failed to load cohort query because of missing sql field");
			}
			
			List<SqlCohortDefinition> duplicates = DefinitionContext.getDefinitionService(SqlCohortDefinition.class)
			        .getDefinitions(cohortQuery.getName(), true);
			
			if (duplicates.size() == 0 || (duplicates.size() == 1 && duplicates.get(0).isRetired())) {
				CohortDefinition definition = new SqlCohortDefinition(cohortQuery.getSql());
				definition.setName(cohortQuery.getName());
				definition.setDescription(cohortQuery.getDescription());
				DefinitionContext.saveDefinition(definition);
			}
		}
		
		log.info("Case Report Module refreshed");
	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Case Report Module");
	}
	
	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		log.info("Case Report Module started");
	}
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Case Report Module");
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("Case Report Module stopped");
	}
	
}
