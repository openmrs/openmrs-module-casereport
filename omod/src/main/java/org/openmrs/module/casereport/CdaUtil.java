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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.EN;
import org.marc.everest.datatypes.ENXP;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.ON;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.doc.StructDocElementNode;
import org.marc.everest.datatypes.doc.StructDocTextNode;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Act;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedAuthor;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedCustodian;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalStatement;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component2;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Consumable;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Custodian;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.CustodianOrganization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.EntryRelationship;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ManufacturedProduct;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Material;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Patient;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Person;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RecordTarget;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.StructuredBody;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.SubstanceAdministration;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActRelationshipHasComponent;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.AdministrativeGender;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ContextControl;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActClassDocumentEntryAct;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActMoodDocumentObservation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntry;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntryRelationship;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentActMood;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_DocumentSubstanceMood;
import org.openmrs.Concept;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Contains utility methods for creating entities to add to the CDA document
 */
public class CdaUtil {
	
	public static CE<String> createLoincCE(String code, String displayName) {
		return new CE<String>(code, DocumentConstants.CODE_SYSTEM_LOINC, DocumentConstants.CODE_SYSTEM_NAME_LOINC, null,
		        displayName, null);
	}
	
	public static CD<String> createCielCD(String code, String displayName) {
		return new CD<String>(code, DocumentConstants.CODE_SYSTEM_CIEL, DocumentConstants.CODE_SYSTEM_NAME_CIEL, null,
		        displayName, null);
	}
	
	public static CD<String> createSnomedCD(String code, String displayName) {
		return new CD<String>(code, DocumentConstants.CODE_SYSTEM_SNOMEDCT, DocumentConstants.CODE_SYSTEM_NAME_SNOMEDCT,
		        null, displayName, null);
	}
	
	/**
	 * Create a RecordTarget
	 * 
	 * @param form
	 * @return a RecordTarget instance
	 */
	public static RecordTarget createRecordTarget(CaseReportForm form) {
		RecordTarget rt = new RecordTarget(ContextControl.OverridingPropagating);
		Patient patient = new Patient();
		PN name;
		if (StringUtils.isNotBlank(form.getMiddleName())) {
			name = PN.fromFamilyGiven(null, form.getFamilyName(), form.getGivenName(), form.getMiddleName());
		} else {
			name = PN.fromFamilyGiven(null, form.getFamilyName(), form.getGivenName());
		}
		patient.setName(SET.createSET(name));
		AdministrativeGender gender = AdministrativeGender.Undifferentiated;
		if ("M".equals(form.getGender())) {
			gender = AdministrativeGender.Male;
		} else if ("F".equals(form.getGender())) {
			gender = AdministrativeGender.Female;
		}
		patient.setAdministrativeGenderCode(gender);
		Calendar calendar = Calendar.getInstance();
		CaseReport cr = Context.getService(CaseReportService.class).getCaseReportByUuid(form.getReportUuid());
		calendar.setTime(cr.getPatient().getBirthdate());
		patient.setBirthTime(calendar);
		PatientRole patientRole = new PatientRole();
		patientRole.setPatient(patient);
		if (form.getPatientIdentifier() != null) {
			Object id = form.getPatientIdentifier().getValue();
			if (id != null && StringUtils.isNotBlank(id.toString())) {
				patientRole.setId(SET.createSET(new II(DocumentConstants.PATIENT_ID_ROOT, id.toString())));
			}
		}
		patientRole.setProviderOrganization(createOrganization(form.getAssigningAuthorityId(),
		    form.getAssigningAuthorityName()));
		rt.setPatientRole(patientRole);
		return rt;
	}
	
	/**
	 * Creates an Organization
	 * 
	 * @param id
	 * @param name
	 * @return an Organisation instance
	 */
	private static Organization createOrganization(String id, String name) {
		Organization org = new Organization();
		org.setId(SET.createSET(new II(id)));
		org.setName(SET.createSET(new ON()));
		org.getName().get(0).getParts().add(new ENXP(name));
		return org;
	}
	
	/**
	 * Create an author with the specified name
	 * 
	 * @param form
	 * @return an Author instance
	 */
	public static Author createAuthor(CaseReportForm form) {
		Author author = new Author();
		AssignedAuthor assignedAuthor = new AssignedAuthor();
		String systemId = form.getSubmitter().getValue().toString();
		assignedAuthor.setId(SET.createSET(new II(null, systemId)));
		User user = Context.getUserService().getUserByUsername(systemId);
		Person person = createPerson(user.getPersonName());
		assignedAuthor.setAssignedAuthorChoice(person);
		assignedAuthor.setRepresentedOrganization(createOrganization(form.getAssigningAuthorityId(),
		    form.getAssigningAuthorityName()));
		author.setAssignedAuthor(assignedAuthor);
		return author;
	}
	
	/**
	 * Create a Person
	 *
	 * @param personName
	 * @return a Person instance
	 */
	private static Person createPerson(PersonName personName) {
		Person person = new Person();
		PN name;
		if (StringUtils.isNotBlank(personName.getMiddleName())) {
			name = PN.fromFamilyGiven(null, personName.getFamilyName(), personName.getGivenName(),
			    personName.getMiddleName());
		} else {
			name = PN.fromFamilyGiven(null, personName.getFamilyName(), personName.getGivenName());
		}
		person.setName(SET.createSET(name));
		return person;
	}
	
	public static Custodian createCustodian(CaseReportForm form) {
		CustodianOrganization custodianOrganization = new CustodianOrganization();
		custodianOrganization.setId(SET.createSET(new II(form.getAssigningAuthorityId())));
		custodianOrganization.setName(new ON());
		custodianOrganization.getName().getParts().add(new ENXP(form.getAssigningAuthorityName()));
		AssignedCustodian assignedCustodian = new AssignedCustodian(custodianOrganization);
		return new Custodian(assignedCustodian);
	}
	
	public static Component2 createRootComponent(CaseReportForm form) throws ParseException {
		Component2 comp = new Component2(ActRelationshipHasComponent.HasComponent, BL.TRUE, new StructuredBody());
		comp.getBodyChoiceIfStructuredBody().setComponent(createComponents(form));
		return comp;
	}
	
	public static ArrayList<Component3> createComponents(CaseReportForm form) throws ParseException {
		//Add the triggers component
		ArrayList<Component3> components = new ArrayList<Component3>();
		Section triggersSection = createSectionWithLoincCode(DocumentConstants.LOINC_CODE_CLINICAL_INFO,
		    DocumentConstants.TEXT_CLINICAL_INFO);
		StructDocElementNode triggersTextNode = createTextNodeForTriggers(form);
		triggersSection.setText(new SD(triggersTextNode));
		triggersSection.getEntry().add(createEntryForTriggers(form));
		Component3 triggersComponent = new Component3();
		triggersComponent.setSection(triggersSection);
		components.add(triggersComponent);
		
		//Add the ARV medications component
		if (CollectionUtils.isNotEmpty(form.getCurrentHivMedications())) {
			Section medsSection = createSectionWithLoincCode(DocumentConstants.LOINC_CODE_MED_INFO,
			    DocumentConstants.TEXT_MED_INFO);
			StructDocElementNode medsTextNode = createTextNodeForMedications(form);
			medsSection.setText(new SD(medsTextNode));
			medsSection.getEntry().add(createEntryForMedications(form));
			Component3 medsComponent = new Component3();
			medsComponent.setSection(medsSection);
			components.add(medsComponent);
		}
		
		//Add other clinical data
		if (form.containsDiagnosticData()) {
			Section diagnosticsSection = createSectionWithLoincCode(DocumentConstants.LOINC_CODE_DIAGNOSTICS,
			    DocumentConstants.TEXT_DIAGNOSTICS);
			StructDocElementNode diagnosticTextNode = createTextNodeForDiagnostics(form);
			diagnosticsSection.setText(new SD(diagnosticTextNode));
			// diagnosticsSection.getEntry().add(createEntryForDiagnostics(form));
			Component3 diagnosticsComponent = new Component3();
			diagnosticsComponent.setSection(diagnosticsSection);
			components.add(diagnosticsComponent);
		}
		
		return components;
	}
	
	private static StructDocElementNode createTextNodeForDiagnostics(CaseReportForm form) throws ParseException {
		StructDocElementNode rootListNode = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		if (form.getCurrentHivWhoStage() != null) {
			rootListNode.addElement(DocumentConstants.ELEMENT_ITEM, DocumentConstants.TEXT_WHO_STAGE
			        + form.getCurrentHivWhoStage().getValue().toString());
		}
		if (form.getMostRecentArvStopReason() != null) {
			rootListNode.addElement(DocumentConstants.ELEMENT_ITEM, DocumentConstants.TEXT_ARV_STOP_REASON
			        + form.getMostRecentArvStopReason().getValue().toString());
		}
		if (form.getLastVisitDate() != null) {
			Date date = CaseReportConstants.DATE_FORMATTER.parse(form.getLastVisitDate().getValue().toString());
			String dateStr = DocumentConstants.DATE_FORMATTER.format(date);
			rootListNode.addElement(DocumentConstants.ELEMENT_ITEM, DocumentConstants.TEXT_LAST_VISIT_DATE + dateStr);
		}
		if (CollectionUtils.isNotEmpty(form.getMostRecentViralLoads())) {
			addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_VIRAL_LOADS, form.getMostRecentViralLoads());
		}
		if (CollectionUtils.isNotEmpty(form.getMostRecentCd4Counts())) {
			addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_CD4_COUNTS, form.getMostRecentCd4Counts());
		}
		if (CollectionUtils.isNotEmpty(form.getMostRecentHivTests())) {
			addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_HIV_TESTS, form.getMostRecentHivTests());
		}
		
		return rootListNode;
	}
	
	private static Section createSectionWithLoincCode(String code, String displayName) {
		Section section = new Section();
		//section.setTemplateId(LIST.createLIST(new II(CdaDocumentGenerator.SECTION_TEMPLATE_ID_ROOT1), new II(
		//        CdaDocumentGenerator.SECTION_TEMPLATE_ID_ROOT2)));
		//section.setTemplateId(LIST.createLIST(new II(CdaDocumentGenerator.SECTION_TEMPLATE_ID_ROOT1)));
		section.setCode(createLoincCE(code, displayName));
		section.setTitle(displayName);
		return section;
	}
	
	private static StructDocElementNode createTextNodeForTriggers(CaseReportForm form) {
		StructDocElementNode rootListNode = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_TRIGGERS, form.getTriggers());
		if (StringUtils.isNotBlank(form.getComments())) {
			rootListNode.addElement(DocumentConstants.ELEMENT_ITEM, DocumentConstants.TEXT_COMMENTS + form.getComments());
		}
		return rootListNode;
	}
	
	private static StructDocElementNode createTextNodeForMedications(CaseReportForm form) {
		StructDocElementNode rootListNode = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_ARVS, form.getCurrentHivMedications());
		return rootListNode;
	}
	
	private static void addNestedListToRootNode(StructDocElementNode parentNode, String label,
	                                            List<? extends UuidAndValue> itemsToAdd) {
		StructDocElementNode itemList = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		for (UuidAndValue item : itemsToAdd) {
			itemList.addElement(DocumentConstants.ELEMENT_ITEM, item.getValue().toString());
		}
		StructDocTextNode labelNode = new StructDocTextNode(label);
		parentNode.addElement(DocumentConstants.ELEMENT_ITEM, labelNode, itemList);
	}
	
	private static Entry createEntryForTriggers(CaseReportForm form) throws ParseException {
		ArrayList<EntryRelationship> relationships = new ArrayList<EntryRelationship>(form.getTriggers().size());
		SchedulerService ss = Context.getSchedulerService();
		for (DatedUuidAndValue trigger : form.getTriggers()) {
			String triggerName = trigger.getValue().toString();
			TaskDefinition taskDefinition = ss.getTaskByName(triggerName);
			if (taskDefinition == null) {
				throw new APIException("No scheduled task found with for trigger:" + triggerName);
			}
			String conceptMap = taskDefinition.getProperty(CaseReportConstants.CONCEPT_TASK_PROPERTY);
			if (StringUtils.isBlank(conceptMap) || !conceptMap.startsWith(CaseReportConstants.CIEL_MAPPING_PREFIX)) {
				throw new APIException("The scheduled task associated to the " + triggerName
				        + " trigger has an invalid value for the concept property");
			}
			Concept concept = CaseReportUtil.getConceptByMappingString(conceptMap, true);
			String code = StringUtils.split(conceptMap, CaseReportConstants.CONCEPT_MAPPING_SEPARATOR)[1];
			CD<String> value = createCielCD(code, concept.getDisplayString());
			CD<String> question = new CD<String>(DocumentConstants.ACT_CODE_ASSERTION, DocumentConstants.CODE_SYSTEM_ACTCODE);
			Date triggerDate = CaseReportConstants.DATE_FORMATTER.parse(trigger.getDate());
			Observation observation = createObservation(question, value, triggerDate, ActStatus.Completed);
			relationships.add(createEntryRelationship(observation));
		}
		CD<String> question = createSnomedCD(DocumentConstants.SNOMED_CODE_TRIGGER, DocumentConstants.TEXT_TRIGGER);
		//TODO Set the answer for this observation?
		Entry entry = new Entry(x_ActRelationshipEntry.DRIV, null, createObservation(question, null, null,
		    ActStatus.Completed));
		entry.getClinicalStatementIfObservation().setEntryRelationship(relationships);
		return entry;
	}
	
	private static Entry createEntryForMedications(CaseReportForm form) {
		ArrayList<EntryRelationship> relationships = new ArrayList<EntryRelationship>(form.getTriggers().size());
		for (UuidAndValue medication : form.getCurrentHivMedications()) {
			SubstanceAdministration subMedication = createSubstanceAdministration(medication);
			relationships.add(createEntryRelationship(subMedication));
		}
		CD<String> question = createCielCD(DocumentConstants.CIEL_CODE_HIV_TREAMENT, DocumentConstants.TEXT_HIV_TREATMENT);
		Act act = new Act(x_ActClassDocumentEntryAct.Act, x_DocumentActMood.Eventoccurrence);
		act.setNegationInd(BL.FALSE);
		act.setCode(question);
		act.setStatusCode(ActStatus.Completed);
		Entry entry = new Entry(x_ActRelationshipEntry.DRIV, null, act);
		entry.getClinicalStatementIfAct().setEntryRelationship(relationships);
		return entry;
	}
	
	private static EntryRelationship createEntryRelationship(ClinicalStatement clinicalStatement) {
		EntryRelationship relationship = new EntryRelationship();
		relationship.setTypeCode(x_ActRelationshipEntryRelationship.HasComponent);
		relationship.setClinicalStatement(clinicalStatement);
		return relationship;
	}
	
	private static SubstanceAdministration createSubstanceAdministration(UuidAndValue uuidAndValue) {
		//TODO Should these be drug orders? And what if a concept has no CIEL mapping?
		Material material = new Material();
		material.setName(EN.createEN(null, new ENXP(uuidAndValue.getValue().toString())));
		ManufacturedProduct mp = new ManufacturedProduct();
		mp.setManufacturedDrugOrOtherMaterial(material);
		Consumable consumable = new Consumable(mp);
		SubstanceAdministration sa = new SubstanceAdministration(x_DocumentSubstanceMood.Eventoccurrence, consumable);
		sa.setNegationInd(BL.FALSE);
		sa.setStatusCode(ActStatus.Active);
		return sa;
	}
	
	private static Observation createObservation(CD<String> questionConcept, ANY value, Date obsdatetime,
	                                             ActStatus statusCode) {
		Observation observation = new Observation(x_ActMoodDocumentObservation.Eventoccurrence, questionConcept);
		observation.setValue(value);
		observation.setStatusCode(statusCode);
		if (obsdatetime != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(obsdatetime);
			observation.setEffectiveTime(new TS(calendar));
		}
		return observation;
	}
}
