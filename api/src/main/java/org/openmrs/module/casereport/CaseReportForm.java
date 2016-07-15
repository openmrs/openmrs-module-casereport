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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;

/**
 * An instance of this class encapsulates the report form data
 */
public class CaseReportForm {
	
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	private String givenName;
	
	private String middleName;
	
	private String familyName;
	
	private String gender;
	
	private String birthdate;
	
	private String deathdate;
	
	private Boolean dead;
	
	private String patientIdentifier;
	
	private String identifierType;
	
	private String causeOfDeath;
	
	private Map<String, String> triggerAndDateCreatedMap;
	
	private List<String> previousSubmittedCaseReports;
	
	private Map<String, Double> mostRecentDateAndViralLoadMap;
	
	private Map<String, Double> mostRecentDateAndCd4CountMap;
	
	private Map<String, String> mostRecentDateAndHivTestMap;
	
	private String mostRecentHivWhoStage;
	
	private List<String> currentHivMedications;
	
	private String mostRecentArvStopReason;
	
	private String lastVisitDate;
	
	private String submitterName;
	
	private String submitterSystemId;
	
	private String assigningAuthority;
	
	public CaseReportForm() {
		
	}
	
	public CaseReportForm(CaseReport caseReport) {
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
		}
		setDead(patient.getDead());
		if (patient.getDead()) {
			if (patient.getCauseOfDeath() != null) {
				setCauseOfDeath(patient.getCauseOfDeath().getName().getName());
			}
			if (patient.getDeathDate() != null) {
				setDeathdate(DATE_FORMATTER.format(patient.getDeathDate()));
			}
		}
		PatientIdentifier id = patient.getPatientIdentifier();
		if (id != null) {
			setPatientIdentifier(id.getIdentifier());
			setIdentifierType(id.getIdentifierType().getName());
		}
		
		Map<String, String> triggers = new LinkedHashMap<String, String>(caseReport.getReportTriggers().size());
		for (CaseReportTrigger tr : caseReport.getReportTriggers()) {
			triggers.put(tr.getName(), DATE_FORMATTER.format(tr.getDateCreated()));
		}
		setTriggerAndDateCreatedMap(triggers);
		
		List<Obs> mostRecentViralLoads = CaseReportUtil.getMostRecentViralLoads(patient);
		Map<String, Double> dateViralLoadMap = new LinkedHashMap<String, Double>(3);
		for (Obs o : mostRecentViralLoads) {
			dateViralLoadMap.put(DATE_FORMATTER.format(o.getObsDatetime()), o.getValueNumeric());
		}
		setMostRecentDateAndViralLoadMap(dateViralLoadMap);
		
		List<Obs> mostRecentCd4Counts = CaseReportUtil.getMostRecentCD4counts(patient);
		Map<String, Double> dateCD4CountMap = new LinkedHashMap<String, Double>(3);
		for (Obs o : mostRecentCd4Counts) {
			dateCD4CountMap.put(DATE_FORMATTER.format(o.getObsDatetime()), o.getValueNumeric());
		}
		setMostRecentDateAndCd4CountMap(dateCD4CountMap);
		
		List<Obs> mostRecentHivTests = CaseReportUtil.getMostRecentHIVTests(patient);
		Map<String, String> dateHivTestMap = new LinkedHashMap<String, String>(3);
		for (Obs o : mostRecentHivTests) {
			dateHivTestMap.put(DATE_FORMATTER.format(o.getObsDatetime()), o.getValueAsString(Context.getLocale()));
		}
		setMostRecentDateAndHivTestMap(dateHivTestMap);
		
		List<Drug> arvMeds = CaseReportUtil.getCurrentARVMedications(patient, null);
		List<String> currentArvs = new ArrayList<String>(3);
		for (Drug d : arvMeds) {
			currentArvs.add(d.getName());
		}
		setCurrentHivMedications(currentArvs);
		
		Obs mostRecentWHOStageObs = CaseReportUtil.getMostRecentWHOStage(patient);
		if (mostRecentWHOStageObs != null) {
			setMostRecentHivWhoStage(mostRecentWHOStageObs.getValueAsString(Context.getLocale()));
		}
		
		Obs mostRecentArvStopReasonObs = CaseReportUtil.getMostRecentReasonARVsStopped(patient);
		if (mostRecentArvStopReasonObs != null) {
			setMostRecentArvStopReason(mostRecentArvStopReasonObs.getValueAsString(Context.getLocale()));
		}
		
		Visit visit = CaseReportUtil.getLastVisit(patient);
		if (visit != null) {
			setLastVisitDate(DATE_FORMATTER.format(visit.getStartDatetime()));
		}
		
		List<CaseReport> submittedReports = Context.getService(CaseReportService.class).getSubmittedCaseReports(patient);
		if (CollectionUtils.isNotEmpty(submittedReports)) {
			List<String> prevSubmittedReports = new ArrayList<String>(submittedReports.size());
			ObjectMapper mapper = new ObjectMapper();
			for (CaseReport cr : submittedReports) {
				//We need to get the triggers that were actually submitted in the final report
				//instead of the report triggers that were directly set on the queue item
				try {
					CaseReportForm prevForm = mapper.readValue(cr.getReportForm(), CaseReportForm.class);
					for (String t : prevForm.getTriggerAndDateCreatedMap().keySet()) {
						if (!prevSubmittedReports.contains(t)) {
							prevSubmittedReports.add(t);
						}
					}
				}
				catch (IOException e) {
					throw new APIException("Failed to parse report form data for previous case report:" + cr, e);
				}
			}
			setPreviousSubmittedCaseReports(prevSubmittedReports);
		}
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
	
	public String getCauseOfDeath() {
		return causeOfDeath;
	}
	
	public void setCauseOfDeath(String causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
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
	
	public Map<String, String> getMostRecentDateAndHivTestMap() {
		return mostRecentDateAndHivTestMap;
	}
	
	public void setMostRecentDateAndHivTestMap(Map<String, String> mostRecentDateAndHivTestMap) {
		this.mostRecentDateAndHivTestMap = mostRecentDateAndHivTestMap;
	}
	
	public Map<String, Double> getMostRecentDateAndCd4CountMap() {
		return mostRecentDateAndCd4CountMap;
	}
	
	public void setMostRecentDateAndCd4CountMap(Map<String, Double> mostRecentDateAndCd4CountMap) {
		this.mostRecentDateAndCd4CountMap = mostRecentDateAndCd4CountMap;
	}
	
	public Map<String, Double> getMostRecentDateAndViralLoadMap() {
		return mostRecentDateAndViralLoadMap;
	}
	
	public void setMostRecentDateAndViralLoadMap(Map<String, Double> mostRecentDateAndViralLoadMap) {
		this.mostRecentDateAndViralLoadMap = mostRecentDateAndViralLoadMap;
	}
	
	public List<String> getCurrentHivMedications() {
		return currentHivMedications;
	}
	
	public void setCurrentHivMedications(List<String> currentHivMedications) {
		this.currentHivMedications = currentHivMedications;
	}
	
	public String getMostRecentArvStopReason() {
		return mostRecentArvStopReason;
	}
	
	public void setMostRecentArvStopReason(String mostRecentArvStopReason) {
		this.mostRecentArvStopReason = mostRecentArvStopReason;
	}
	
	public String getMostRecentHivWhoStage() {
		return mostRecentHivWhoStage;
	}
	
	public void setMostRecentHivWhoStage(String mostRecentHivWhoStage) {
		this.mostRecentHivWhoStage = mostRecentHivWhoStage;
	}
	
	public List<String> getPreviousSubmittedCaseReports() {
		return previousSubmittedCaseReports;
	}
	
	public void setPreviousSubmittedCaseReports(List<String> previousSubmittedCaseReports) {
		this.previousSubmittedCaseReports = previousSubmittedCaseReports;
	}
	
	public String getLastVisitDate() {
		return lastVisitDate;
	}
	
	public void setLastVisitDate(String lastVisitDate) {
		this.lastVisitDate = lastVisitDate;
	}
	
	public String getAssigningAuthority() {
		return assigningAuthority;
	}
	
	public void setAssigningAuthority(String assigningAuthority) {
		this.assigningAuthority = assigningAuthority;
	}
	
	public String getSubmitterName() {
		return submitterName;
	}
	
	public void setSubmitterName(String submitterName) {
		this.submitterName = submitterName;
	}
	
	public String getSubmitterSystemId() {
		return submitterSystemId;
	}
	
	public void setSubmitterSystemId(String submitterSystemId) {
		this.submitterSystemId = submitterSystemId;
	}
}
