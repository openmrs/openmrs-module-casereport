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

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.NotTransactional;

public class CaseReportUtilBehaviorTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private SchedulerService schedulerService;
	
	@Test
	@NotTransactional
    @Ignore
	public void executeTask_shouldNotCreateMultipleCaseReportsForTheSamePatient() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		final String name = "New HIV Case";
		SqlCohortDefinition def = CaseReportUtil.getSqlCohortDefinition(name);
		final int pId = 7;
		def.setQuery("select patient_id from patient where patient_id =" + pId);
		DefinitionContext.saveDefinition(def);
		assertNull(service.getCaseReportByPatient(patientService.getPatient(pId)));
		int originalCount = service.getCaseReports().size();
		int N = 50;
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < N; i++) {
			
			threads.add(new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Context.openSession();
						Context.authenticate("admin", "test");
						CaseReportUtil.executeTask(schedulerService.getTaskByName(name));
					}
					catch (Exception e) {
						throw new APIException(e);
					}
					finally {
						Context.closeSession();
					}
				}
				
			}));
			
		}
		for (int i = 0; i < N; ++i) {
			threads.get(i).start();
		}
		for (int i = 0; i < N; ++i) {
			threads.get(i).join();
		}
		
		Assert.assertEquals(++originalCount, service.getCaseReports().size());
	}
	
}
