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

import static org.openmrs.module.casereport.CaseReportConstants.DATE_FORMATTER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;

/**
 * An instance of this class encapsulates the serialized report form data
 */
public class CaseReportForm {
	
	private String reportUuid;
	
	private Date reportDate;
	
	private String givenName;
	
	private String middleName;
	
	private String familyName;
	
	private String fullName;
	
	private String gender;
	
	private String birthdate;
	
	private String deathdate;
	
	private Boolean dead;
	
	private UuidAndValue patientIdentifier;
	
	private UuidAndValue identifierType;
	
	private UuidAndValue causeOfDeath;
	
	private List<DatedUuidAndValue> triggers;
	
	private Map<String, List<DatedUuidAndValue>> previousReportUuidTriggersMap;
	
	private List<DatedUuidAndValue> mostRecentViralLoads;
	
	private List<DatedUuidAndValue> mostRecentCd4Counts;
	
	private List<DatedUuidAndValue> mostRecentHivTests;
	
	private UuidAndValue currentHivWhoStage;
	
	private List<DatedUuidAndValue> currentHivMedications;
	
	private UuidAndValue mostRecentArvStopReason;
	
	private UuidAndValue lastVisitDate;
	
	private UuidAndValue submitter;
	
	private String assigningAuthorityId;
	
	private String assigningAuthorityName;
	
	private String comments;
	
	public CaseReportForm() {
	}
	
	public CaseReportForm(CaseReport caseReport) {
		setReportUuid(caseReport.getUuid());
		setReportDate(caseReport.getDateCreated());
		Patient patient = caseReport.getPatient();
		setGender(patient.getGender());
		if (patient.getBirthdate() != null) {
			setBirthdate(DATE_FORMATTER.format(patient.getBirthdate()));
		}
		PersonName name = patient.getPersonName();
		if (name != null) {
			setGivenName(name.getGivenName());
			setMiddleName(name.getMiddleName());
			setFamilyName(name.getFamilyName());
			setFullName(name.getFullName());
		}
		setDead(patient.getDead());
		if (patient.getDead()) {
			if (patient.getCauseOfDeath() != null) {
				setCauseOfDeath(new UuidAndValue(patient.getCauseOfDeath().getUuid(), patient.getCauseOfDeath().getName()
				        .getName()));
			}
			if (patient.getDeathDate() != null) {
				setDeathdate(DATE_FORMATTER.format(patient.getDeathDate()));
			}
		}
		PatientIdentifier id = patient.getPatientIdentifier();
		if (id != null) {
			setPatientIdentifier(new UuidAndValue(id.getUuid(), id.getIdentifier()));
			setIdentifierType(new UuidAndValue(id.getIdentifierType().getUuid(), id.getIdentifierType().getName()));
		}
		
		for (CaseReportTrigger tr : caseReport.getReportTriggers()) {
			getTriggers().add(new DatedUuidAndValue(tr.getUuid(), tr.getName(), DATE_FORMATTER.format(tr.getDateCreated())));
		}
		
		List<Obs> mostRecentViralLoads = CaseReportUtil.getMostRecentViralLoads(patient);
		for (Obs o : mostRecentViralLoads) {
			getMostRecentViralLoads().add(
			    new DatedUuidAndValue(o.getUuid(), o.getValueNumeric(), DATE_FORMATTER.format(o.getObsDatetime())));
		}
		
		List<Obs> mostRecentCd4Counts = CaseReportUtil.getMostRecentCD4counts(patient);
		for (Obs o : mostRecentCd4Counts) {
			getMostRecentCd4Counts().add(
			    new DatedUuidAndValue(o.getUuid(), o.getValueNumeric(), DATE_FORMATTER.format(o.getObsDatetime())));
		}
		
		List<Obs> mostRecentHivTests = CaseReportUtil.getMostRecentHIVTests(patient);
		for (Obs o : mostRecentHivTests) {
			getMostRecentHivTests().add(
			    new DatedUuidAndValue(o.getUuid(), o.getValueAsString(Context.getLocale()), DATE_FORMATTER.format(o
			            .getObsDatetime())));
		}
		
		List<DrugOrder> arvOrders = CaseReportUtil.getActiveArvDrugOrders(patient, null);
		for (DrugOrder drugOrder : arvOrders) {
			String displayName = "";
			displayName += drugOrder.getConcept().getName().getName();
			if (drugOrder.getDrug() != null && StringUtils.isNotBlank(drugOrder.getDrug().getName())) {
				if (!displayName.equalsIgnoreCase(drugOrder.getDrug().getName())) {
					displayName += (" (" + drugOrder.getDrug().getName() + ")");
				}
			}
			String dateActivated = DATE_FORMATTER.format(drugOrder.getDateActivated());
			getCurrentHivMedications().add(new DatedUuidAndValue(drugOrder.getDrug().getUuid(), displayName, dateActivated));
		}
		
		Obs mostRecentWHOStageObs = CaseReportUtil.getMostRecentWHOStage(patient);
		if (mostRecentWHOStageObs != null) {
			setCurrentHivWhoStage(new UuidAndValue(mostRecentWHOStageObs.getUuid(),
			        mostRecentWHOStageObs.getValueAsString(Context.getLocale())));
		}
		
		Obs mostRecentArvStopReasonObs = CaseReportUtil.getMostRecentReasonARVsStopped(patient);
		if (mostRecentArvStopReasonObs != null) {
			setMostRecentArvStopReason(new UuidAndValue(mostRecentArvStopReasonObs.getUuid(),
			        mostRecentArvStopReasonObs.getValueAsString(Context.getLocale())));
		}
		
		Visit visit = CaseReportUtil.getLastVisit(patient);
		if (visit != null) {
			setLastVisitDate(new UuidAndValue(visit.getUuid(), DATE_FORMATTER.format(visit.getStartDatetime())));
		}
		
		List<CaseReport> submittedReports = Context.getService(CaseReportService.class).getSubmittedCaseReports(patient);
		if (CollectionUtils.isNotEmpty(submittedReports)) {
			ObjectMapper mapper = new ObjectMapper();
			for (CaseReport cr : submittedReports) {
				//We need to get the triggers that were actually submitted in the final report
				//instead of the report triggers that were directly set on the queue item
				CaseReportForm prevForm;
				if (StringUtils.isNotBlank(cr.getReportForm())) {
					try {
						prevForm = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
					}
					catch (IOException e) {
						throw new APIException("Failed to parse report form data for previous case report:" + cr, e);
					}
					getPreviousReportUuidTriggersMap().put(cr.getUuid(), prevForm.getTriggers());
				}
			}
		}
	}
	
	@JsonIgnore
	public Date getReportDate() {
		return reportDate;
	}
	
	@JsonIgnore
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}
	
	@JsonIgnore
	public String getReportUuid() {
		return reportUuid;
	}
	
	@JsonIgnore
	public void setReportUuid(String reportUuid) {
		this.reportUuid = reportUuid;
	}
	
	public String getFamilyName() {
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public String getGivenName() {
		return givenName;
	}
	
	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}
	
	public String getMiddleName() {
		return middleName;
	}
	
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}
	
	public String getDeathdate() {
		return deathdate;
	}
	
	public void setDeathdate(String deathdate) {
		this.deathdate = deathdate;
	}
	
	public UuidAndValue getCauseOfDeath() {
		return causeOfDeath;
	}
	
	public void setCauseOfDeath(UuidAndValue causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
	}
	
	public Boolean getDead() {
		return dead;
	}
	
	public void setDead(Boolean dead) {
		this.dead = dead;
	}
	
	public UuidAndValue getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(UuidAndValue patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public List<DatedUuidAndValue> getTriggers() {
		if (triggers == null) {
			triggers = new ArrayList<DatedUuidAndValue>();
		}
		return triggers;
	}
	
	public void setTriggers(List<DatedUuidAndValue> triggers) {
		this.triggers = triggers;
	}
	
	public UuidAndValue getIdentifierType() {
		return identifierType;
	}
	
	public void setIdentifierType(UuidAndValue identifierType) {
		this.identifierType = identifierType;
	}
	
	public List<DatedUuidAndValue> getMostRecentHivTests() {
		if (mostRecentHivTests == null) {
			mostRecentHivTests = new ArrayList<DatedUuidAndValue>(3);
		}
		return mostRecentHivTests;
	}
	
	public void setMostRecentHivTests(List<DatedUuidAndValue> mostRecentHivTests) {
		this.mostRecentHivTests = mostRecentHivTests;
	}
	
	public List<DatedUuidAndValue> getMostRecentCd4Counts() {
		if (mostRecentCd4Counts == null) {
			mostRecentCd4Counts = new ArrayList<DatedUuidAndValue>(3);
		}
		return mostRecentCd4Counts;
	}
	
	public void setMostRecentCd4Counts(List<DatedUuidAndValue> mostRecentCd4Counts) {
		this.mostRecentCd4Counts = mostRecentCd4Counts;
	}
	
	public List<DatedUuidAndValue> getMostRecentViralLoads() {
		if (mostRecentViralLoads == null) {
			mostRecentViralLoads = new ArrayList<DatedUuidAndValue>(3);
		}
		return mostRecentViralLoads;
	}
	
	public void setMostRecentViralLoads(List<DatedUuidAndValue> mostRecentViralLoads) {
		this.mostRecentViralLoads = mostRecentViralLoads;
	}
	
	public List<DatedUuidAndValue> getCurrentHivMedications() {
		if (currentHivMedications == null) {
			currentHivMedications = new ArrayList<DatedUuidAndValue>();
		}
		return currentHivMedications;
	}
	
	public void setCurrentHivMedications(List<DatedUuidAndValue> currentHivMedications) {
		this.currentHivMedications = currentHivMedications;
	}
	
	public UuidAndValue getMostRecentArvStopReason() {
		return mostRecentArvStopReason;
	}
	
	public void setMostRecentArvStopReason(UuidAndValue mostRecentArvStopReason) {
		this.mostRecentArvStopReason = mostRecentArvStopReason;
	}
	
	public UuidAndValue getCurrentHivWhoStage() {
		return currentHivWhoStage;
	}
	
	public void setCurrentHivWhoStage(UuidAndValue currentHivWhoStage) {
		this.currentHivWhoStage = currentHivWhoStage;
	}
	
	public Map<String, List<DatedUuidAndValue>> getPreviousReportUuidTriggersMap() {
		if (previousReportUuidTriggersMap == null) {
			previousReportUuidTriggersMap = new HashMap<String, List<DatedUuidAndValue>>();
		}
		return previousReportUuidTriggersMap;
	}
	
	public void setPreviousReportUuidTriggersMap(Map<String, List<DatedUuidAndValue>> previousReportUuidTriggersMap) {
		this.previousReportUuidTriggersMap = previousReportUuidTriggersMap;
	}
	
	public UuidAndValue getLastVisitDate() {
		return lastVisitDate;
	}
	
	public void setLastVisitDate(UuidAndValue lastVisitDate) {
		this.lastVisitDate = lastVisitDate;
	}
	
	public UuidAndValue getSubmitter() {
		return submitter;
	}
	
	public void setSubmitter(UuidAndValue submitter) {
		this.submitter = submitter;
	}
	
	public String getAssigningAuthorityId() {
		return assigningAuthorityId;
	}
	
	public void setAssigningAuthorityId(String assigningAuthorityId) {
		this.assigningAuthorityId = assigningAuthorityId;
	}
	
	public String getAssigningAuthorityName() {
		return assigningAuthorityName;
	}
	
	public void setAssigningAuthorityName(String assigningAuthorityName) {
		this.assigningAuthorityName = assigningAuthorityName;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	public DatedUuidAndValue getTriggerByName(String trigger) {
		for (DatedUuidAndValue uuidAndValue : getTriggers()) {
			if (trigger.equalsIgnoreCase(uuidAndValue.getValue().toString())) {
				return uuidAndValue;
			}
		}
		return null;
	}
	
	public boolean containsDiagnosticData() {
		if (getCurrentHivWhoStage() != null || getMostRecentArvStopReason() != null || getLastVisitDate() != null) {
			return true;
		}
		if (CollectionUtils.isNotEmpty(getMostRecentViralLoads()) || CollectionUtils.isNotEmpty(getMostRecentCd4Counts())
		        || CollectionUtils.isNotEmpty(getMostRecentHivTests())) {
			return true;
		}
		return false;
	}
}
