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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportActivatorTest extends BaseModuleContextSensitiveTest {
	
	private static final String INVALID_FILE_DIR_NAME = "invalid_cohortqueries";
	
	@Autowired
	private DemoSqlCohortQueryLoader loader;
	
	private CaseReportActivator activator = new CaseReportActivator();
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Before
	public void setup() throws Exception {
		executeDataSet("moduleTestData-initialConcepts.xml");
	}
	
	@After
	public void cleanup() {
		//reset
		loader.setPathPattern(DemoSqlCohortQueryLoader.DEFAULT_PATTERN);
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies fail for a query with no name
	 */
	@Test
	public void contextRefreshed_shouldFailForAQueryWithNoName() throws Exception {
		loader.setPathPattern(INVALID_FILE_DIR_NAME + "/missing_name_cohort_query.json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Failed to load cohort query because of missing name field"));
		
		activator.contextRefreshed();
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies fail for a query with no sql
	 */
	@Test
	public void contextRefreshed_shouldFailForAQueryWithNoSql() throws Exception {
		loader.setPathPattern(INVALID_FILE_DIR_NAME + "/missing_sql_cohort_query.json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Failed to load cohort query because of missing sql field"));
		
		activator.contextRefreshed();
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies fail for a query with an invalid concept mapping
	 */
	@Test
	public void contextRefreshed_shouldFailForAQueryWithAnInvalidConceptMapping() throws Exception {
		loader.setPathPattern(INVALID_FILE_DIR_NAME + "/invalid_concept_cohort_query.json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Invalid concept mapping: CIEL_"));
		
		activator.contextRefreshed();
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies fail for a query with no concept
	 */
	@Test
	public void contextRefreshed_shouldFailForAQueryWithNoConcept() throws Exception {
		loader.setPathPattern(INVALID_FILE_DIR_NAME + "/missing_concept_cohort_query.json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Failed to load cohort query because of missing concept field"));
		
		activator.contextRefreshed();
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies fail for a query with a none ciel concept
	 */
	@Test
	public void contextRefreshed_shouldFailForAQueryWithANoneCielConcept() throws Exception {
		loader.setPathPattern(INVALID_FILE_DIR_NAME + "/missing_none_ciel_concept_cohort_query.json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Only CIEL concept mappings are currently allowed"));
		
		activator.contextRefreshed();
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies fail for a query with a none existent concept
	 */
	@Test
	public void contextRefreshed_shouldFailForAQueryWithANoneExistentConcept() throws Exception {
		loader.setPathPattern(INVALID_FILE_DIR_NAME + "/none_existent_concept_cohort_query.json");
		expectedException.expect(APIException.class);
		expectedException.expectMessage(equalTo("Failed to find concept with mapping: CIEL_9999999"));
		
		activator.contextRefreshed();
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies ignore a cohort query with a duplicate name
	 */
	@Test
	public void contextRefreshed_shouldIgnoreACohortQueryWithADuplicateName() throws Exception {
		final String name = "HIV Switched To Second Line";
		List<SqlCohortDefinition> matches = DefinitionContext.getDefinitionService(SqlCohortDefinition.class)
		        .getDefinitions(name, true);
		assertEquals(0, matches.size());
		final String initialQuery = "some random query";
		SqlCohortDefinition definition = new SqlCohortDefinition("some other new query");
		definition.setName(name);
		definition.setQuery(initialQuery);
		assertNull(definition.getId());
		definition = DefinitionContext.saveDefinition(definition);
		assertNotNull(definition.getId());
		matches = DefinitionContext.getDefinitionService(SqlCohortDefinition.class).getDefinitions(name, true);
		assertEquals(1, matches.size());
		int originalCount = DefinitionContext.getAllDefinitions(SqlCohortDefinition.class, true).size();
		
		activator.contextRefreshed();
		assertEquals(initialQuery, definition.getQuery());
		//should have only added the new unique cohort query
		assertEquals(++originalCount, DefinitionContext.getAllDefinitions(SqlCohortDefinition.class, true).size());
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies save a cohort queries with a name that matches a retired duplicate
	 */
	@Test
	public void contextRefreshed_shouldSaveACohortQueriesWithANameThatMatchesARetiredDuplicate() throws Exception {
		final String name = "HIV Switched To Second Line";
		List<SqlCohortDefinition> matches = DefinitionContext.getDefinitionService(SqlCohortDefinition.class)
		        .getDefinitions(name, true);
		assertEquals(0, matches.size());
		final String initialQuery = "some random query";
		SqlCohortDefinition definition = new SqlCohortDefinition("some other new query");
		definition.setName(name);
		definition.setQuery(initialQuery);
		definition.setRetired(true);
		assertNull(definition.getId());
		definition = DefinitionContext.saveDefinition(definition);
		assertNotNull(definition.getId());
		matches = DefinitionContext.getDefinitionService(SqlCohortDefinition.class).getDefinitions(name, true);
		assertEquals(1, matches.size());
		int originalCount = DefinitionContext.getAllDefinitions(SqlCohortDefinition.class, true).size();
		
		activator.contextRefreshed();
		assertEquals(originalCount + 2, DefinitionContext.getAllDefinitions(SqlCohortDefinition.class, true).size());
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies load queries and register them with the reporting module
	 */
	@Test
	public void contextRefreshed_shouldLoadQueriesAndRegisterThemWithTheReportingModule() throws Exception {
		int originalCount = DefinitionContext.getAllDefinitions(SqlCohortDefinition.class, false).size();
		
		activator.contextRefreshed();
		assertEquals(originalCount + 2, DefinitionContext.getAllDefinitions(SqlCohortDefinition.class, false).size());
		DefinitionService<SqlCohortDefinition> defService = DefinitionContext
		        .getDefinitionService(SqlCohortDefinition.class);
		SqlCohortDefinition query = defService.getDefinitions("HIV Patient Died", true).get(0);
		assertEquals("Select patient_id from patient pa, person p where pa.patient_id=p.person_id and dead = 1",
		    query.getQuery());
		assertEquals("HIV patients that have died", query.getDescription());
		assertEquals(2, query.getParameters().size());
		assertTrue(query.getParameters().contains(new Parameter("CIEL_1990", null, null)));
		assertTrue(query.getParameters().contains(new Parameter("CIEL_1991", null, null)));
		
		query = defService.getDefinitions("HIV Switched To Second Line", true).get(0);
		assertEquals("Select patient_id from patient where date_created > :lastExecutionTime", query.getQuery());
		assertNull(query.getDescription());
		Parameter p = query.getParameter("CIEL_1050");
		assertEquals("CIEL_1050", p.getName());
		assertEquals("CIEL:1050", p.getLabel());
		assertEquals(Concept.class, p.getType());
		p = query.getParameter(CaseReportConstants.LAST_EXECUTION_TIME);
		assertNotNull(p);
		assertEquals("lastExecutionTime", p.getName());
		assertEquals("casereport.lastExecutionTime", p.getLabel());
		assertEquals(Date.class, p.getType());
	}
	
	/**
	 * @see CaseReportActivator#contextRefreshed()
	 * @verifies add the case report tasks if they do not exist
	 */
	@Test
	public void contextRefreshed_shouldAddTheCaseReportTasksIfTheyDoNotExist() throws Exception {
		Map<String, Long> nameRepeatIntervalMap = new HashMap<String, Long>();
		nameRepeatIntervalMap.put("HIV Patient Died", 60L);
		nameRepeatIntervalMap.put("HIV Switched To Second Line", 120L);
		SchedulerService ss = Context.getSchedulerService();
		for (String name : nameRepeatIntervalMap.keySet()) {
			assertNull(ss.getTaskByName(name));
		}
		
		activator.contextRefreshed();
		for (Map.Entry<String, Long> entry : nameRepeatIntervalMap.entrySet()) {
			activator.contextRefreshed();
			TaskDefinition td = ss.getTaskByName(entry.getKey());
			assertNotNull(td);
			assertEquals("casereport.description.schedulerTaskFor", td.getDescription());
			assertEquals(entry.getKey(), td.getProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY));
			assertEquals(entry.getValue(), td.getRepeatInterval());
		}
		
	}
}
