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
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.User;

/**
 * An instance of this class encapsulates data for a single trigger, typically a trigger has a name
 * which is the name of the sql cohort query that was executed to generate the case report it
 * belongs to.
 * 
 * @see CaseReport
 */
public class CaseReportTrigger extends BaseOpenmrsObject implements Auditable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Integer caseReportTriggerId;
	
	private String name;
	
	private CaseReport caseReport;
	
	private User creator;
	
	private Date dateCreated;
	
	public CaseReportTrigger() {
	}
	
	public CaseReportTrigger(String name) {
		this.name = name;
	}
	
	public Integer getCaseReportTriggerId() {
		return caseReportTriggerId;
	}
	
	public void setCaseReportTriggerId(Integer caseReportTriggerId) {
		this.caseReportTriggerId = caseReportTriggerId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public CaseReport getCaseReport() {
		return caseReport;
	}
	
	public void setCaseReport(CaseReport caseReport) {
		this.caseReport = caseReport;
	}
	
	@Override
	public Integer getId() {
		return getCaseReportTriggerId();
	}
	
	@Override
	public void setId(Integer id) {
		setCaseReportTriggerId(id);
	}
	
	@Override
	public User getCreator() {
		return creator;
	}
	
	@Override
	public void setCreator(User user) {
		this.creator = user;
	}
	
	@Override
	public Date getDateCreated() {
		return dateCreated;
	}
	
	@Override
	public void setDateCreated(Date date) {
		this.dateCreated = date;
	}
	
	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		String str = "";
		if (StringUtils.isNotBlank(getName())) {
			str += getName();
		}
		if (StringUtils.isBlank(str) && getId() != null) {
			str += "Trigger #" + getId();
		}
		if (StringUtils.isBlank(str)) {
			str = super.toString();
		}
		
		return str;
	}
	
	@Override
	public User getChangedBy() {
		//currently not in use
		return null;
	}
	
	@Override
	public void setChangedBy(User user) {
		//currently not in use
	}
	
	@Override
	public Date getDateChanged() {
		//currently not in use
		return null;
	}
	
	@Override
	public void setDateChanged(Date date) {
		//currently not in use
	}
}
