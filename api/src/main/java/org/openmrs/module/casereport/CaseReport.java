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
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

/**
 * An instance of this class encapsulates data for a single case report for a patient. A case report
 * can have several child trigger instances associated to it
 * 
 * @see CaseReportTrigger
 */
public class CaseReport extends BaseOpenmrsData implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer caseReportId;
	
	private Patient patient;
	
	private Status status = Status.NEW;
	
	private Set<CaseReportTrigger> reportTriggers;
	
	private String report;
	
	public CaseReport() {
	}
	
	public CaseReport(Patient patient, String... triggerNames) {
		this.patient = patient;
		for (String tName : triggerNames) {
			addTrigger(new CaseReportTrigger(tName));
		}
		
	}
	
	public enum Status {
		NEW, DRAFT, SUBMITTED, DISMISSED;
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
	
	public Set<CaseReportTrigger> getReportTriggers() {
		if (reportTriggers == null) {
			reportTriggers = new LinkedHashSet<CaseReportTrigger>();
		}
		return reportTriggers;
	}
	
	public void addTrigger(CaseReportTrigger trigger) {
		trigger.setCaseReport(this);
		getReportTriggers().add(trigger);
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
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String str = "";
		if (patient != null) {
			str += patient.toString();
		}
		if (CollectionUtils.isNotEmpty(getReportTriggers())) {
			str += " Trigger(s):" + StringUtils.join(getReportTriggers(), ",");
		}
		if (StringUtils.isBlank(str) && getId() != null) {
			str += "CaseReport #" + getId();
		}
		if (StringUtils.isBlank(str)) {
			str = super.toString();
		}
		
		return str;
	}
}
