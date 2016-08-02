/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.DatedUuidAndValue;
import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.Resource;
import org.openmrs.module.webservices.rest.web.v1_0.wrapper.openmrs1_8.UserAndPassword1_8;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Composition;
import ca.uhn.fhir.model.dstu2.resource.DetectedIssue;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.CompositionStatusEnum;
import ca.uhn.fhir.model.dstu2.valueset.NarrativeStatusEnum;
import ca.uhn.fhir.model.primitive.BooleanDt;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.model.primitive.XhtmlDt;

public class FhirUtil {
	
	private static final String SYSTEM_URL_LOINC = "http:loinc.org";
	
	private static final String SYSTEM_URL_CIEL = "http://openconceptlab.com/orgs/CIEL/";
	
	private static final String SYSTEM_URL_FHIR = "http://hl7.org/fhir/list-order";
	
	private static final String TITLE = "HIV Case Report";
	
	private static final String CASE_REPORT_FOR = "CaseReport for";
	
	private static final String MANAGED_BY = "Managed by";
	
	private static final String TITLE_VIRAL_LOADS = "Viral Load(s)";
	
	private static final String TITLE_CD4_COUNTS = "CD4 count(s)";
	
	private static final String TITLE_HIV_TESTS = "HIV Test, Qualitative test result(s)";
	
	private static final String TITLE_CURRENT_WHO_STAGE = "Current WHO Stage";
	
	private static final String TITLE_CURRENT_ARVS = "Current HIV medications (ARVs)";
	
	private static final String TITLE_REASON_ARVS_STOPPED = "Reason ARVs stopped";
	
	private static final String TITLE_DATE_OF_LAST_VISIT = "Date of last visit";
	
	private static final String TEXT_VIRAL_LOADS = "This is a measure of the number of copies/ml of DNA/RNA in patients with HIV";
	
	private static final String TEXT_CURRENT_WHO_STAGE = "Question asked on encounter form. Expects a numeric answer defining";
	
	private static final String TEXT_CURRENT_ARVS = "Current HIV medications (ARVs)";
	
	private static final String TEXT_REASON_ARVS_STOPPED = "Question on encounter form. Part of the plan gathers a reason for stopping ARVs. This concept captures that information";
	
	private static final String TEXT_DATE_OF_LAST_VISIT = "Date of event (last visit)";
	
	private static final String TEXT_CD4_COUNTS = "Measure of CD4 (T-helper cells) in blood";
	
	private static final String TEXT_HIV_TESTS = "Qualitative interpretation of rapid HIV screening test.";
	
	/**
	 * Generates a CDA document containing details in the specified case report form.
	 *
	 * @param caseReportForm
	 * @return the generated json text
	 * @should return the generated json
	 */
	public static String createCdaDocument(CaseReportForm caseReportForm) throws Exception {
		Composition composition = new Composition();
		composition.setId(caseReportForm.getReportUuid());
		String text = CASE_REPORT_FOR + " " + caseReportForm.getFullName() + ", " + MANAGED_BY + " "
		        + caseReportForm.getAssigningAuthorityName();
		composition.setText(new NarrativeDt(new XhtmlDt(text), NarrativeStatusEnum.GENERATED));
		composition.setIdentifier(new IdentifierDt("urn:ietf:rfc:3986", caseReportForm.getReportUuid()));
		composition.setDate(new DateTimeDt(caseReportForm.getReportDate()));
		composition.setType(createLoincCoding("55751-2"));
		composition.setClassElement(createLoincCoding("LP173421-1"));
		composition.setTitle(TITLE);
		composition.setStatus(CompositionStatusEnum.FINAL);
		composition.setConfidentiality("N");
		addPatient(composition, caseReportForm);
		
		User user = Context.getUserService().getUserByUuid(caseReportForm.getSubmitter().getUuid());
		Resource r = Context.getService(RestService.class).getResourceBySupportedClass(UserAndPassword1_8.class);
		composition.setAuthor(Arrays.asList(createReference(r.getUri(new UserAndPassword1_8(user)), user.getUsername())));
		composition.setCustodian(createReference(caseReportForm.getAssigningAuthorityId(),
		    caseReportForm.getAssigningAuthorityName()));
		addEvent(composition, caseReportForm);
		
		if (CollectionUtils.isNotEmpty(caseReportForm.getMostRecentViralLoads())) {
			addSection(composition, TITLE_VIRAL_LOADS, CaseReportConstants.TERM_CODE_VIRAL_LOAD, TEXT_VIRAL_LOADS, true,
			    caseReportForm.getMostRecentViralLoads());
		}
		
		if (CollectionUtils.isNotEmpty(caseReportForm.getMostRecentCd4Counts())) {
			addSection(composition, TITLE_CD4_COUNTS, CaseReportConstants.TERM_CODE_CD4_COUNT, TEXT_CD4_COUNTS, false,
			    caseReportForm.getMostRecentCd4Counts());
		}
		
		if (CollectionUtils.isNotEmpty(caseReportForm.getMostRecentHivTests())) {
			addSection(composition, TITLE_HIV_TESTS, CaseReportConstants.TERM_CODE_HIV_TEST, TEXT_HIV_TESTS, false,
			    caseReportForm.getMostRecentHivTests());
		}
		
		if (caseReportForm.getCurrentHivWhoStage() != null) {
			addSection(composition, TITLE_CURRENT_WHO_STAGE, CaseReportConstants.TERM_CODE_WHO_STAGE,
			    TEXT_CURRENT_WHO_STAGE, false, Arrays.asList(caseReportForm.getCurrentHivWhoStage()));
		}
		
		if (CollectionUtils.isNotEmpty(caseReportForm.getCurrentHivMedications())) {
			addSection(composition, TITLE_CURRENT_ARVS, CaseReportConstants.TERM_CODE_CURRENT_ARVS, TEXT_CURRENT_ARVS,
			    false, caseReportForm.getCurrentHivMedications());
		}
		
		if (caseReportForm.getMostRecentArvStopReason() != null) {
			addSection(composition, TITLE_REASON_ARVS_STOPPED, CaseReportConstants.TERM_CODE_REASON_FOR_STOPPING_ARVS,
			    TEXT_REASON_ARVS_STOPPED, false, Arrays.asList(caseReportForm.getMostRecentArvStopReason()));
		}
		
		if (caseReportForm.getLastVisitDate() != null) {
			addSection(composition, TITLE_DATE_OF_LAST_VISIT, CaseReportConstants.TERM_CODE_DATE_OF_LAST_VISIT,
			    TEXT_DATE_OF_LAST_VISIT, false, Arrays.asList(caseReportForm.getLastVisitDate()));
		}
		
		return FhirContext.forDstu2().newJsonParser().setPrettyPrint(true).encodeResourceToString(composition);
	}
	
	private static void addPatient(Composition composition, CaseReportForm form) throws Exception {
		composition.setSubject(createReference("#patient", form.getFullName()));
		Patient patient = new Patient();
		patient.setId("patient");
		HumanNameDt name = patient.addName();
		name.setGiven(Arrays.asList(new StringDt(form.getGivenName()), new StringDt(form.getMiddleName())));
		name.setFamily(Arrays.asList(new StringDt(form.getFamilyName())));
		name.setText(new StringDt(form.getFullName()));
		AdministrativeGenderEnum gender = AdministrativeGenderEnum.UNKNOWN;
		if ("M".equals(form.getGender())) {
			gender = AdministrativeGenderEnum.MALE;
		} else if ("F".equals(form.getGender())) {
			gender = AdministrativeGenderEnum.FEMALE;
		}
		patient.setGender(gender);
		patient.setBirthDate(new DateDt(CaseReportConstants.DATE_FORMATTER.parse(form.getBirthdate())));
		if (StringUtils.isNotBlank(form.getDeathdate())) {
			patient.setDeceased(new DateTimeDt(CaseReportConstants.DATE_FORMATTER.parse(form.getDeathdate())));
		} else {
			patient.setDeceased(new BooleanDt(form.getDead()));
		}
		if (form.getPatientIdentifier() != null) {
			Object id = form.getPatientIdentifier().getValue();
			if (id != null && StringUtils.isNotBlank(id.toString())) {
				IdentifierDt identifier = patient.addIdentifier();
				identifier.setValue(id.toString());
				if (form.getIdentifierType() != null) {
					Object idType = form.getIdentifierType().getValue();
					if (idType != null && StringUtils.isNotBlank(idType.toString())) {
						identifier.setSystem(new UriDt(idType.toString()));
					}
				}
			}
		}
		
		composition.getContained().getContainedResources().add(patient);
	}
	
	private static void addEvent(Composition composition, CaseReportForm form) throws ParseException {
		Composition.Event event = composition.addEvent();
		event.addCode(createCoding("http://hl7.org/fhir/v3/ActCode", "_ObservationIssueTriggerCodedObservationType"));
		event.setPeriod(new PeriodDt().setStart(new DateTimeDt(form.getReportDate())).setEnd(
		    new DateTimeDt(form.getReportDate())));
		
		List<ResourceReferenceDt> details = new ArrayList<ResourceReferenceDt>();
		CaseReportService service = Context.getService(CaseReportService.class);
		Resource r = Context.getService(RestService.class).getResourceBySupportedClass(CohortDefinition.class);
		for (DatedUuidAndValue trigger : form.getTriggers()) {
			String t = trigger.getValue().toString();
			details.add(createReference("#" + trigger.getUuid(), t));
			DetectedIssue detectedIssue = new DetectedIssue();
			detectedIssue.setId(trigger.getUuid());
			detectedIssue.setDetail(t);
			detectedIssue.setDate(new DateTimeDt(CaseReportConstants.DATE_FORMATTER.parse(trigger.getDate())));
			SqlCohortDefinition sqlDef = null;
			try {
				sqlDef = service.getSqlCohortDefinition(t);
			}
			catch (APIException e) {
				//ignore
			}
			if (sqlDef != null) {
				detectedIssue.setReference(r.getUri(sqlDef));
			}
			
			composition.getContained().getContainedResources().add(detectedIssue);
		}
		event.setDetail(details);
	}
	
	private static void addSection(Composition composition, String title, String code, String text, boolean isOrdered,
	                               List<? extends UuidAndValue> entries) {
		Composition.Section section = composition.addSection();
		section.setTitle(title);
		section.setCode(createCielCoding(code));
		section.setText(new NarrativeDt(new XhtmlDt(text), NarrativeStatusEnum.GENERATED));
		section.setMode("snapshot");
		if (isOrdered) {
			section.setOrderedBy(createCoding(SYSTEM_URL_FHIR, "event-date"));
		}
		Resource r = Context.getService(RestService.class).getResourceBySupportedClass(Obs.class);
		if (CollectionUtils.isNotEmpty(entries)) {
			for (UuidAndValue uuidAndValue : entries) {
				String value = null;
				if (uuidAndValue != null && uuidAndValue.getValue() != null) {
					value = uuidAndValue.getValue().toString();
				}
				section.addEntry().setReference(r.getUri(uuidAndValue)).setDisplay(value);
			}
		}
	}
	
	private static CodeableConceptDt createCoding(String system, String code) {
		return new CodeableConceptDt(system, code);
	}
	
	private static CodeableConceptDt createLoincCoding(String code) {
		return createCoding(SYSTEM_URL_LOINC, code);
	}
	
	private static CodeableConceptDt createCielCoding(String code) {
		return createCoding(SYSTEM_URL_CIEL, code);
	}
	
	private static ResourceReferenceDt createReference(String uri, String name) {
		return new ResourceReferenceDt().setReference(uri).setDisplay(name);
	}
	
}
