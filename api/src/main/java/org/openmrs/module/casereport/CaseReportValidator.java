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

@Component
@Handler(supports = CaseReport.class, order = 50)
public class CaseReportValidator implements Validator {
	
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
	 * @should fail if the trigger name is null
	 * @should fail if the trigger name is an empty string
	 * @should fail if the trigger name is a white space character
	 * @should fail if a case report with the same trigger already exists for the patient
	 * @should fail if multiple sql cohort queries match the trigger name
	 * @should fail if no sql cohort query matches the trigger name
	 * @should fail if the sql cohort query associated to the trigger is retired
	 * @should pass for a valid case report
	 */
	@Override
	public void validate(Object target, Errors errors) {
		//System.out.println("Validating:"+target);
		if (target == null || !(target instanceof CaseReport)) {
			throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + CaseReport.class);
		}
		
		CaseReport caseReport = (CaseReport) target;
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "patient", "casereports.error.patient.required");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "triggerName", "casereports.error.triggerName.required");
		if (errors.hasErrors()) {
			return;
		}
		
		CaseReportService service = Context.getService(CaseReportService.class);
		if (service.getSqlCohortDefinition(caseReport.getTriggerName()) == null) {
			errors.rejectValue("triggerName", "casereport.error.sqlCohortQuery.notFound");
		}
		
		CaseReport duplicate = service
		        .getCaseReportByPatientAndTrigger(caseReport.getPatient(), caseReport.getTriggerName());
		if (duplicate != null && !duplicate.equals(caseReport)) {
			errors.reject("casereports.error.duplicate");
		}
	}
}
