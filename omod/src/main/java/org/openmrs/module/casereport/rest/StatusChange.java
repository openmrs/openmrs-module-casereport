/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest;

import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;

public class StatusChange {
	
	public enum Action {
		SUBMIT, DISMISS
	}
	
	private CaseReport caseReport;
	
	private Action action;
	
	private CaseReportForm reportForm;
	
	public CaseReport getCaseReport() {
		return caseReport;
	}
	
	public void setCaseReport(CaseReport caseReport) {
		this.caseReport = caseReport;
	}
	
	public Action getAction() {
		return action;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public CaseReportForm getReportForm() {
		return reportForm;
	}
	
	public void setReportForm(CaseReportForm reportForm) {
		this.reportForm = reportForm;
	}
}
