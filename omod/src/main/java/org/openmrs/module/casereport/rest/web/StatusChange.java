/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.web;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.User;
import org.openmrs.module.casereport.CaseReport;

public class StatusChange {
	
	public enum Action {
		SUBMIT, DISMISS
	}
	
	private CaseReport caseReport;
	
	private Action action;
	
	private List<String> triggersToExclude;
	
	private User submitter;
	
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
	
	public User getSubmitter() {
		return submitter;
	}
	
	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}
	
	public List<String> getTriggersToExclude() {
		if (triggersToExclude == null) {
			triggersToExclude = new ArrayList<String>();
		}
		return triggersToExclude;
	}
	
	public void setTriggersToExclude(List<String> triggersToExclude) {
		this.triggersToExclude = triggersToExclude;
	}
}
