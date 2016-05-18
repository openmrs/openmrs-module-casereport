/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
}
