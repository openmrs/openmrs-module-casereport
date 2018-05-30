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

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component("caseReportValidator")
@Handler(supports = CaseReport.class, order = 50)
public class CaseReportValidator implements Validator {
	
	@Autowired
	private CaseReportTriggerValidator triggerValidator;
	
	/**
	 * @see Validator#validate(Object, Errors)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return CaseReport.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see Validator#validate(Object, Errors)
	 * @should fail if the case report object is null
	 * @should fail if the patient is null
	 * @should fail if the report has no trigger added
	 * @should fail for a new item if the patient already has an existing report item
	 * @should pass for auto submit item with unique trigger and patient has existing item
	 * @should pass for a valid case report
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (target == null || !(target instanceof CaseReport)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + getClass());
		}
		
		CaseReport caseReport = (CaseReport) target;
		ValidationUtils.rejectIfEmpty(errors, "patient", "casereports.error.patient.required");
		if (!errors.hasErrors()) {
			CaseReportService service = Context.getService(CaseReportService.class);
			if (caseReport.getId() == null && !caseReport.getAutoSubmitted()
			        && service.getCaseReportByPatient(caseReport.getPatient()) != null) {
				errors.reject("casereports.error.patient.alreadyHasQueueItem");
			}
		}
		
		if (CollectionUtils.isEmpty(caseReport.getReportTriggers())) {
			errors.rejectValue("reportTriggers", "casereports.error.atleast.one.trigger.required");
		}
		
		if (!errors.hasErrors()) {
			int index = 0;
			for (CaseReportTrigger trigger : caseReport.getReportTriggers()) {
				try {
					errors.pushNestedPath("reportTriggers[" + index + "]");
					ValidationUtils.invokeValidator(triggerValidator, trigger, errors);
				}
				finally {
					errors.popNestedPath();
					index++;
				}
			}
		}
		
	}
}
