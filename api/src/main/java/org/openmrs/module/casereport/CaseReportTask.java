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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * An instance of this task runs a specified SQL cohort query and creates case reports for an
 * patients that get returned from the query, the name of the sql cohort query and the associated
 * concept must be specified as task properties with the property names as
 * CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY and CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY
 * respectively.
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
				log.debug("Executing case report task...");
			}
			
			startExecuting();
			
			try {
				String trigger = getTaskDefinition().getProperty(CaseReportConstants.TRIGGER_NAME_TASK_PROPERTY);
				if (StringUtils.isBlank(trigger)) {
					throw new APIException("The Trigger Name property is required for a Case Report Task");
				}
				
				CaseReportUtil.executeTask(getTaskDefinition());
				
				if (log.isDebugEnabled()) {
					log.debug("Case report task executed successfully!");
				}
			}
			catch (Exception e) {
				log.error("Error while running case report task: ", e);
			}
			finally {
				stopExecuting();
			}
		}
	}
}
