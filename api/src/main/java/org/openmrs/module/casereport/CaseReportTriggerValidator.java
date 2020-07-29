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

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component("caseReportTriggerValidator")
@Handler(supports = CaseReportTrigger.class, order = 50)
public class CaseReportTriggerValidator implements Validator {
	
	/**
	 * @see Validator#validate(Object, Errors)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return CaseReportTrigger.class.isAssignableFrom(clazz);
	}
	
	/**
	 * @see Validator#validate(Object, Errors)
	 * <strong>Should</strong> fail if the case report trigger object is null
	 * <strong>Should</strong> fail if the trigger name is null
	 * <strong>Should</strong> fail if the trigger name is an empty string
	 * <strong>Should</strong> fail if the trigger name is a white space character
	 * <strong>Should</strong> fail if the case report field is null
	 * <strong>Should</strong> fail if a case report with the same trigger already exists for the patient
	 * <strong>Should</strong> pass for a valid case report trigger
	 */
	@Override
	public void validate(Object target, Errors errors) {
		if (target == null || !(target instanceof CaseReportTrigger)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + getClass());
		}
		
		CaseReportTrigger trigger = (CaseReportTrigger) target;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "casereports.error.trigger.name.required");
		ValidationUtils.rejectIfEmpty(errors, "caseReport", "casereports.error.trigger.casereport.required");
		
		if (errors.hasErrors()) {
			return;
		}
		
		CaseReportService service = Context.getService(CaseReportService.class);
		CaseReport duplicate = service.getCaseReportByPatient(trigger.getCaseReport().getPatient());
		if (duplicate != null) {
			CaseReportTrigger crt = duplicate.getCaseReportTriggerByName(trigger.getName());
			if (crt != null && !crt.equals(trigger)) {
				errors.rejectValue("name", "casereport.error.trigger.duplicate", new Object[] { trigger.getName() }, null);
			}
		}
	}
}
