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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.definition.DefinitionContext;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * An instance of this task runs a specified SQL cohort query and creates case reports for an
 * patients that get returned from the query, the name of the sql cohort query must be specified as
 * a task property with the name CaseReportTask.TRIGGER_NAME_TASK_PROPERTY
 */
public class CaseReportTask extends AbstractTask {
	
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see AbstractTask#execute()
	 */
	@Override
	public void execute() {
		if (!isExecuting) {
			if (log.isDebugEnabled()) {
				log.debug("Starting case report task ...");
			}
			
			startExecuting();
			
			try {
				String trigger = getTaskDefinition().getProperty("Trigger Name");
				if (StringUtils.isBlank(trigger)) {
					throw new APIException("The Triggery Name property is required for a Case Report Task");
				}
				
				List<SqlCohortDefinition> duplicates = DefinitionContext.getDefinitionService(SqlCohortDefinition.class)
				        .getDefinitions(trigger, true);
				if (duplicates.size() > 1) {
					throw new APIException("Found multiple Sql Cohort Queries with name:" + trigger);
				} else if (duplicates.size() == 0) {
					throw new APIException("Cannot find a Sql Cohort Query with name:" + trigger);
				} else if (duplicates.get(0).isRetired()) {
					throw new APIException(trigger + " is a retired Sql Cohort Query");
				}
				
				EvaluationContext evaluationContext = new EvaluationContext();
				Cohort cohort = (Cohort) DefinitionContext.evaluate(duplicates.get(0), evaluationContext);
				
				PatientService ps = Context.getPatientService();
				CaseReportService crs = Context.getService(CaseReportService.class);
				for (Integer patientId : cohort.getMemberIds()) {
					Patient patient = ps.getPatient(patientId);
					if (patient == null) {
						throw new APIException("No patient found with patientId:" + patientId);
					}
					
					crs.saveCaseReport(new CaseReport(trigger, patient));
				}
				
				if (log.isDebugEnabled()) {
					log.debug("Case report task completed successfully ...");
				}
			}
			catch (Exception e) {
				log.error("Error while running case report task:", e);
			}
			finally {
				stopExecuting();
			}
		}
	}
}
