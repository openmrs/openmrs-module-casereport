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

import java.io.Serializable;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

/**
 * An instance of this class encapsulates data for a single case report for a patient
 */
public class CaseReport extends BaseOpenmrsData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer caseReportId;
	
	private String trigger;
	
	private Patient patient;
	
	private Status status = Status.NEW;
	
	private String report;
	
	public CaseReport() {
	}
	
	public CaseReport(String trigger, Patient patient) {
		this.trigger = trigger;
		this.patient = patient;
	}
	
	public enum Status {
		NEW, SUBMITTED, DISMISSED;
	}
	
	@Override
	public Integer getId() {
		return getCaseReportId();
	}
	
	@Override
	public void setId(Integer id) {
		setCaseReportId(id);
	}
	
	public Integer getCaseReportId() {
		return caseReportId;
	}
	
	public void setCaseReportId(Integer caseReportId) {
		this.caseReportId = caseReportId;
	}
	
	public String getTrigger() {
		return trigger;
	}
	
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getReport() {
		return report;
	}
	
	public void setReport(String report) {
		this.report = report;
	}
	
	public boolean isSubmitted() {
		return getStatus() == Status.SUBMITTED;
	}
	
	public boolean isDismissed() {
		return getStatus() == Status.DISMISSED;
	}
}
