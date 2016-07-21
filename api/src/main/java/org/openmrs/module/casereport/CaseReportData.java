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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Drug;
import org.openmrs.ImplementationId;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;

/**
 * An instance of this class encapsulates the report form data i.e the actual hydrated domain
 * objects which is different from the CaseReportForm which is made up of serialized values and
 * uuids.
 */
public class CaseReportData {
	
	private Patient patient;
	
	private PatientIdentifier identifier;
	
	private List<CaseReportTrigger> triggers;
	
	private List<Obs> mostRecentViralLoads;
	
	private List<Obs> mostRecentCd4Counts;
	
	private List<Obs> mostRecentHivTests;
	
	private Obs mostRecentHivWhoStage;
	
	private List<Drug> currentHivMedications;
	
	private Obs mostRecentArvStopReason;
	
	private Visit lastVisit;
	
	private User submitter;
	
	private ImplementationId implementationId;
	
	public CaseReportData(CaseReportForm caseReportForm) {
		//setPatient(caseReportForm.getPatient());
		setIdentifier(getPatient().getPatientIdentifier());
		//getTriggers().addAll(caseReportForm.getReportTriggers());
		getMostRecentViralLoads().addAll(CaseReportUtil.getMostRecentViralLoads(getPatient()));
		getMostRecentCd4Counts().addAll(CaseReportUtil.getMostRecentCD4counts(getPatient()));
		getMostRecentHivTests().addAll(CaseReportUtil.getMostRecentHIVTests(getPatient()));
		setMostRecentHivWhoStage(CaseReportUtil.getMostRecentWHOStage(getPatient()));
		getCurrentHivMedications().addAll(CaseReportUtil.getCurrentARVMedications(getPatient(), null));
		setMostRecentArvStopReason(CaseReportUtil.getMostRecentReasonARVsStopped(getPatient()));
		setLastVisit(CaseReportUtil.getLastVisit(getPatient()));
		
		CaseReportService service = Context.getService(CaseReportService.class);
		/*if (CollectionUtils.isNotEmpty(submittedReports)) {
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
		}*/
	}
	
	public Patient getPatient() {
		return patient;
	}
	
	public void setPatient(Patient patient) {
		this.patient = patient;
	}
	
	public PatientIdentifier getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(PatientIdentifier identifier) {
		this.identifier = identifier;
	}
	
	public Visit getLastVisit() {
		return lastVisit;
	}
	
	public void setLastVisit(Visit lastVisit) {
		this.lastVisit = lastVisit;
	}
	
	public List<CaseReportTrigger> getTriggers() {
		if (triggers == null) {
			triggers = new ArrayList<CaseReportTrigger>();
		}
		return triggers;
	}
	
	public void setTriggers(List<CaseReportTrigger> triggers) {
		this.triggers = triggers;
	}
	
	public List<Obs> getMostRecentViralLoads() {
		if (mostRecentViralLoads == null) {
			mostRecentViralLoads = new ArrayList<Obs>(3);
		}
		return mostRecentViralLoads;
	}
	
	public void setMostRecentViralLoads(List<Obs> mostRecentViralLoads) {
		this.mostRecentViralLoads = mostRecentViralLoads;
	}
	
	public List<Obs> getMostRecentCd4Counts() {
		if (mostRecentCd4Counts == null) {
			mostRecentCd4Counts = new ArrayList<Obs>(3);
		}
		return mostRecentCd4Counts;
	}
	
	public void setMostRecentCd4Counts(List<Obs> mostRecentCd4Counts) {
		this.mostRecentCd4Counts = mostRecentCd4Counts;
	}
	
	public List<Obs> getMostRecentHivTests() {
		if (mostRecentHivTests == null) {
			mostRecentHivTests = new ArrayList<Obs>(3);
		}
		return mostRecentHivTests;
	}
	
	public void setMostRecentHivTests(List<Obs> mostRecentHivTests) {
		this.mostRecentHivTests = mostRecentHivTests;
	}
	
	public Obs getMostRecentHivWhoStage() {
		return mostRecentHivWhoStage;
	}
	
	public void setMostRecentHivWhoStage(Obs mostRecentHivWhoStage) {
		this.mostRecentHivWhoStage = mostRecentHivWhoStage;
	}
	
	public List<Drug> getCurrentHivMedications() {
		if (currentHivMedications == null) {
			currentHivMedications = new ArrayList<Drug>();
		}
		return currentHivMedications;
	}
	
	public void setCurrentHivMedications(List<Drug> currentHivMedications) {
		this.currentHivMedications = currentHivMedications;
	}
	
	public Obs getMostRecentArvStopReason() {
		return mostRecentArvStopReason;
	}
	
	public void setMostRecentArvStopReason(Obs mostRecentArvStopReason) {
		this.mostRecentArvStopReason = mostRecentArvStopReason;
	}
	
	public User getSubmitter() {
		return submitter;
	}
	
	public void setSubmitter(User submitter) {
		this.submitter = submitter;
	}
	
	public ImplementationId getImplementationId() {
		return implementationId;
	}
	
	public void setImplementationId(ImplementationId implementationId) {
		this.implementationId = implementationId;
	}
}
