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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;

/**
 * An instance of this class encapsulates the report form data
 */
public class CaseReportForm {
	
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private String givenName;
	
	private String middleName;
	
	private String familyName;
	
	private String gender;
	
	private String birthdate;
	
	private Boolean dead;
	
	private String patientIdentifier;
	
	private String identifierType;
	
	private String CaseOfDeath;
	
	private Map<String, String> triggerAndDateCreatedMap;
	
	private List<String> previousSubmittedCaseReports;
	
	private Map<Date, Integer> mostRecentDateAndViralLoadMap;
	
	private Map<Date, Integer> mostRecentDateAndCd4CountMap;
	
	private Map<Date, String> mostRecentDateAndHivTestMap;
	
	private Integer mostRecentHivWhoStage;
	
	private List<String> mostRecentHivMedications;
	
	private String mostRecentArvStopReason;
	
	public CaseReportForm() {
		
	}
	
	public CaseReportForm(Patient patient, CaseReport caseReport) {
		setGender(patient.getGender());
		setBirthdate(DATE_FORMATTER.format(patient.getBirthdate()));
		setDead(patient.getDead());
		PersonName name = patient.getPersonName();
		if (name != null) {
			setGivenName(name.getGivenName());
			setMiddleName(name.getMiddleName());
			setFamilyName(name.getFamilyName());
		}
		PatientIdentifier id = patient.getPatientIdentifier();
		if (id != null) {
			setPatientIdentifier(id.getIdentifier());
			setIdentifierType(id.getIdentifierType().getName());
		}
		Map<String, String> triggers = new HashMap<String, String>(caseReport.getReportTriggers().size());
		for (CaseReportTrigger tr : caseReport.getReportTriggers()) {
			triggers.put(tr.getName(), DATE_FORMATTER.format(tr.getDateCreated()));
		}
		setTriggerAndDateCreatedMap(triggers);
	}
	
	public String getBirthdate() {
		return birthdate;
	}
	
	public void setBirthdate(String birthdate) {
		this.birthdate = birthdate;
	}
	
	public String getCaseOfDeath() {
		return CaseOfDeath;
	}
	
	public void setCaseOfDeath(String caseOfDeath) {
		CaseOfDeath = caseOfDeath;
	}
	
	public Boolean getDead() {
		return dead;
	}
	
	public void setDead(Boolean dead) {
		this.dead = dead;
	}
	
	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
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
	
	public String getFamilyName() {
		return familyName;
	}
	
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public Map<String, String> getTriggerAndDateCreatedMap() {
		return triggerAndDateCreatedMap;
	}
	
	public void setTriggerAndDateCreatedMap(Map<String, String> triggerAndDateCreatedMap) {
		this.triggerAndDateCreatedMap = triggerAndDateCreatedMap;
	}
	
	public String getIdentifierType() {
		return identifierType;
	}
	
	public void setIdentifierType(String identifierType) {
		this.identifierType = identifierType;
	}
	
	public Map<Date, String> getMostRecentDateAndHivTestMap() {
		return mostRecentDateAndHivTestMap;
	}
	
	public void setMostRecentDateAndHivTestMap(Map<Date, String> mostRecentDateAndHivTestMap) {
		this.mostRecentDateAndHivTestMap = mostRecentDateAndHivTestMap;
	}
	
	public Map<Date, Integer> getMostRecentDateAndCd4CountMap() {
		return mostRecentDateAndCd4CountMap;
	}
	
	public void setMostRecentDateAndCd4CountMap(Map<Date, Integer> mostRecentDateAndCd4CountMap) {
		this.mostRecentDateAndCd4CountMap = mostRecentDateAndCd4CountMap;
	}
	
	public Map<Date, Integer> getMostRecentDateAndViralLoadMap() {
		return mostRecentDateAndViralLoadMap;
	}
	
	public void setMostRecentDateAndViralLoadMap(Map<Date, Integer> mostRecentDateAndViralLoadMap) {
		this.mostRecentDateAndViralLoadMap = mostRecentDateAndViralLoadMap;
	}
	
	public List<String> getMostRecentHivMedications() {
		return mostRecentHivMedications;
	}
	
	public void setMostRecentHivMedications(List<String> mostRecentHivMedications) {
		this.mostRecentHivMedications = mostRecentHivMedications;
	}
	
	public String getMostRecentArvStopReason() {
		return mostRecentArvStopReason;
	}
	
	public void setMostRecentArvStopReason(String mostRecentArvStopReason) {
		this.mostRecentArvStopReason = mostRecentArvStopReason;
	}
	
	public Integer getMostRecentHivWhoStage() {
		return mostRecentHivWhoStage;
	}
	
	public void setMostRecentHivWhoStage(Integer mostRecentHivWhoStage) {
		this.mostRecentHivWhoStage = mostRecentHivWhoStage;
	}
	
	public List<String> getPreviousSubmittedCaseReports() {
		return previousSubmittedCaseReports;
	}
	
	public void setPreviousSubmittedCaseReports(List<String> previousSubmittedCaseReports) {
		this.previousSubmittedCaseReports = previousSubmittedCaseReports;
	}
}
