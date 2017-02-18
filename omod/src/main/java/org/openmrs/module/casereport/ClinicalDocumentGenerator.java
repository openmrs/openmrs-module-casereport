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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.ANY;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.ENXP;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.ON;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.REAL;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.doc.StructDocElementNode;
import org.marc.everest.datatypes.doc.StructDocTextNode;
import org.marc.everest.datatypes.generic.CD;
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedAuthor;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedCustodian;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component2;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Custodian;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.CustodianOrganization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Entry;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Observation;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Patient;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Person;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RecordTarget;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.StructuredBody;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActRelationshipHasComponent;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActStatus;
import org.marc.everest.rmim.uv.cdar2.vocabulary.AdministrativeGender;
import org.marc.everest.rmim.uv.cdar2.vocabulary.BindingRealm;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ContextControl;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActMoodDocumentObservation;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_ActRelationshipEntry;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_BasicConfidentialityKind;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

/**
 * Generates a ClinicalDocument object
 */
public final class ClinicalDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private CaseReportForm form;
	
	/**
	 * @param form the CaseReportForm from which to generate a ClinicalDocument
	 */
	public ClinicalDocumentGenerator(CaseReportForm form) {
		this.form = form;
	}
	
	/**
	 * Generates and returns a ClinicalDocument object from its backing CaseReportForm object
	 * 
	 * @return ClinicalDocument object
	 * @should generate a ClinicalDocument object
	 */
	public ClinicalDocument generate() throws Exception {
		if (log.isDebugEnabled()) {
			CaseReportService crs = Context.getService(CaseReportService.class);
			CaseReport cr = crs.getCaseReportByUuid(form.getReportUuid());
			log.debug("Generating ClinicalDocument for: " + cr);
		}
		
		ClinicalDocument cdaDocument = new ClinicalDocument();
		cdaDocument.setRealmCode(new SET<>(new CS<>(BindingRealm.UniversalRealmOrContextUsedInEveryInstance)));
		cdaDocument.setTypeId(DocumentConstants.TYPE_ID_ROOT, DocumentConstants.TEXT_EXTENSION);
		cdaDocument.setTemplateId(Arrays.asList(new II(DocumentConstants.TEMPLATE_ID_ROOT)));
		cdaDocument.setId(form.getAssigningAuthorityId(), form.getReportUuid());
		cdaDocument.setCode(createLoincCE(DocumentConstants.LOINC_CODE_CR, DocumentConstants.TEXT_DOCUMENT_NAME));
		cdaDocument.setTitle(DocumentConstants.TEXT_TITLE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(form.getReportDate());
		cdaDocument.setEffectiveTime(calendar);
		cdaDocument.setConfidentialityCode(x_BasicConfidentialityKind.Normal);
		cdaDocument.setLanguageCode(DocumentConstants.LANGUAGE_CODE);
		cdaDocument.getRecordTarget().add(createRecordTarget());
		cdaDocument.getAuthor().add(createAuthor());
		cdaDocument.setCustodian(createCustodian());
		cdaDocument.setComponent(createRootComponent());
		
		return cdaDocument;
	}
	
	private CE<String> createLoincCE(String code, String displayName) {
		return new CE<>(code, DocumentConstants.CODE_SYSTEM_LOINC, DocumentConstants.CODE_SYSTEM_NAME_LOINC, null,
		        displayName, null);
	}
	
	private CD<String> createCielCD(String code, String displayName) {
		return new CD<>(code, DocumentConstants.CODE_SYSTEM_CIEL, DocumentConstants.CODE_SYSTEM_NAME_CIEL, null,
		        displayName, null);
	}
	
	private CD<String> createSnomedCD(String code, String displayName) {
		return new CD<>(code, DocumentConstants.CODE_SYSTEM_SNOMEDCT, DocumentConstants.CODE_SYSTEM_NAME_SNOMEDCT, null,
		        displayName, null);
	}
	
	/**
	 * Create a RecordTarget
	 *
	 * @return a RecordTarget instance
	 */
	private RecordTarget createRecordTarget() throws ParseException {
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
		if (form.getBirthdate() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(CaseReportConstants.DATE_FORMATTER.parse(form.getBirthdate()));
			patient.setBirthTime(calendar);
		}
		PatientRole patientRole = new PatientRole();
		patientRole.setPatient(patient);
		Object id = form.getPatientIdentifier().getValue();
		Object idType = form.getIdentifierType().getValue();
		patientRole.setId(SET.createSET(new II(idType.toString(), id.toString())));
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
	private Organization createOrganization(String id, String name) {
		Organization org = new Organization();
		org.setId(SET.createSET(new II(id)));
		org.setName(SET.createSET(new ON()));
		org.getName().get(0).getParts().add(new ENXP(name));
		return org;
	}
	
	/**
	 * Create an author with the specified name
	 *
	 * @return an Author instance
	 */
	private Author createAuthor() {
		Author author = new Author();
		AssignedAuthor assignedAuthor = new AssignedAuthor();
		String systemId = form.getSubmitter().getValue().toString();
		assignedAuthor.setId(SET.createSET(new II(form.getAssigningAuthorityId(), systemId)));
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
	private Person createPerson(PersonName personName) {
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
	
	private Custodian createCustodian() {
		CustodianOrganization custodianOrganization = new CustodianOrganization();
		custodianOrganization.setId(SET.createSET(new II(form.getAssigningAuthorityId())));
		custodianOrganization.setName(new ON());
		custodianOrganization.getName().getParts().add(new ENXP(form.getAssigningAuthorityName()));
		AssignedCustodian assignedCustodian = new AssignedCustodian(custodianOrganization);
		return new Custodian(assignedCustodian);
	}
	
	private Component2 createRootComponent() throws ParseException {
		Component2 comp = new Component2(ActRelationshipHasComponent.HasComponent, BL.TRUE, new StructuredBody());
		comp.getBodyChoiceIfStructuredBody().setComponent(createComponents());
		return comp;
	}
	
	private ArrayList<Component3> createComponents() throws ParseException {
		//Add the triggers component
		ArrayList<Component3> components = new ArrayList<>();
		Section triggersSection = createSectionWithLoincCode(DocumentConstants.LOINC_CODE_CLINICAL_INFO,
		    DocumentConstants.TEXT_CLINICAL_INFO);
		StructDocElementNode triggersTextNode = createTextNodeForTriggers();
		triggersSection.setText(new SD(triggersTextNode));
		triggersSection.setEntry(createEntriesForTriggers());
		Component3 triggersComponent = new Component3();
		triggersComponent.setSection(triggersSection);
		components.add(triggersComponent);

		//Add the ARV medications component
		if (CollectionUtils.isNotEmpty(form.getCurrentHivMedications())) {
			Section medsSection = createSectionWithLoincCode(DocumentConstants.LOINC_CODE_MED_INFO,
			    DocumentConstants.TEXT_MED_INFO);
			StructDocElementNode medsTextNode = createTextNodeForMedications();
			medsSection.setText(new SD(medsTextNode));
			medsSection.setEntry(createEntriesForMedications());
			Component3 medsComponent = new Component3();
			medsComponent.setSection(medsSection);
			components.add(medsComponent);
		}
		
		//Add other clinical data
		if (form.containsDiagnosticData()) {
			Section diagnosticsSection = createSectionWithLoincCode(DocumentConstants.LOINC_CODE_DIAGNOSTICS,
			    DocumentConstants.TEXT_DIAGNOSTICS);
			StructDocElementNode diagnosticTextNode = createTextNodeForDiagnostics();
			diagnosticsSection.setText(new SD(diagnosticTextNode));
			diagnosticsSection.setEntry(createEntriesForDiagnostics());
			Component3 diagnosticsComponent = new Component3();
			diagnosticsComponent.setSection(diagnosticsSection);
			components.add(diagnosticsComponent);
		}
		
		return components;
	}
	
	private ArrayList<Entry> createEntriesForDiagnostics() throws ParseException {
		ArrayList<Entry> entries = new ArrayList<>();
		if (form.getCurrentHivWhoStage() != null) {
			Entry e = createEntryFromCielQuestionCodeAndObsWithCodedValue(CaseReportConstants.CIEL_CODE_WHO_STAGE,
			    DocumentConstants.TEXT_CURRENT_WHO_STAGE, form.getCurrentHivWhoStage());
			entries.add(e);
		}
		if (form.getMostRecentArvStopReason() != null) {
			Entry e = createEntryFromCielQuestionCodeAndObsWithCodedValue(
			    CaseReportConstants.CIEL_CODE_REASON_FOR_STOPPING_ARVS, DocumentConstants.TEXT_REASON_ARVS_STOPPED,
			    form.getMostRecentArvStopReason());
			entries.add(e);
		}
		if (form.getLastVisitDate() != null) {
			CD<String> question = createCielCD(CaseReportConstants.CIEL_CODE_DATE_OF_LAST_VISIT,
			    DocumentConstants.TEXT_DATE_OF_LAST_VISIT);
			String dateStr = form.getLastVisitDate().getValue().toString();
			Date visitDate = CaseReportConstants.DATE_FORMATTER.parse(dateStr);
			entries.add(createObservationEntry(question, DocUtil.createTS(visitDate), dateStr));
		}
		if (form.getMostRecentCd4Count() != null) {
			Entry e = createEntryFromCielQuestionCodeAndObsWithNumericValue(CaseReportConstants.CIEL_CODE_CD4_COUNT,
			    DocumentConstants.TEXT_CD4_COUNT, form.getMostRecentCd4Count());
			entries.add(e);
		}
		if (form.getMostRecentHivTest() != null) {
			Entry e = createEntryFromCielQuestionCodeAndObsWithCodedValue(CaseReportConstants.CIEL_CODE_HIV_TEST,
			    DocumentConstants.TEXT_HIV_TEST, form.getMostRecentHivTest());
			entries.add(e);
		}
		if (form.getMostRecentViralLoad() != null) {
			Entry e = createEntryFromCielQuestionCodeAndObsWithNumericValue(CaseReportConstants.CIEL_CODE_VIRAL_LOAD,
			    DocumentConstants.TEXT_VIRAL_LOAD, form.getMostRecentViralLoad());
			entries.add(e);
		}
		
		return entries;
	}
	
	/**
	 * Creates an Entry along with its Observation field from a DatedUuidAndValue instance, the
	 * qnCielCode argument value must be a CIEL code
	 *
	 * @param cielQuestionCode CIEL dictionary code for the Obs question
	 * @param qnText
	 * @param numericObsValue
	 * @return Entry Object
	 * @throws ParseException
	 */
	private Entry createEntryFromCielQuestionCodeAndObsWithNumericValue(String cielQuestionCode, String qnText,
	                                                                    DatedUuidAndValue numericObsValue)
	    throws ParseException {
		CD<String> question = createCielCD(cielQuestionCode, qnText);
		return createObservationEntry(question, new REAL(Double.valueOf(numericObsValue.getValue().toString())),
		    numericObsValue.getDate());
	}
	
	/**
	 * Creates an Entry along with its Observation field from a UuidAndValue or DatedUuidAndValue
	 * instance, if value is an instance of UuidAndValue, the method loads the Obs from the database
	 * and uses its obsDatetime as the value. The qnCielCode argument value must be a CIEL code
	 *
	 * @param cielQuestionCode CIEL dictionary code for the Obs question
	 * @param qnText
	 * @param codedObsValue
	 * @return Entry Object
	 * @throws ParseException
	 */
	private Entry createEntryFromCielQuestionCodeAndObsWithCodedValue(String cielQuestionCode, String qnText,
	                                                                  UuidAndValue codedObsValue) throws ParseException {
		Obs obs = Context.getObsService().getObsByUuid(codedObsValue.getUuid());
		if (obs == null) {
			throw new APIException("Failed to find Obs with uuid:" + codedObsValue.getUuid());
		}
		
		DatedUuidAndValue dValue;
		if (DatedUuidAndValue.class.isAssignableFrom(codedObsValue.getClass())) {
			dValue = (DatedUuidAndValue) codedObsValue;
		} else {
			dValue = new DatedUuidAndValue(codedObsValue);
			dValue.setDate(CaseReportConstants.DATE_FORMATTER.format(obs.getObsDatetime()));
		}
		String name = dValue.getValue().toString();
		
		return createObservationEntryWithACielQuestionCodeAndCodedValue(cielQuestionCode, qnText, obs.getValueCoded(),
		    dValue.getDate(), name);
	}
	
	private Entry createObservationEntryWithACielQuestionCodeAndCodedValue(String cielQuestionCode, String questionText,
	                                                                       Concept value, String obsDatetime,
	                                                                       String originalTextValue) throws ParseException {
		CD<String> question = createCielCD(cielQuestionCode, questionText);
		CD<String> val = createCD(value, originalTextValue);
		if (val == null) {
			throw new APIException("No valid mapping found for the concept with id " + value.getId()
			        + " to the any of the following sources: CIEL, LOINC and SNOMED CT");
		}
		return createObservationEntry(question, val, obsDatetime);
	}
	
	private Entry createObservationEntry(CD<String> obsQuestion, ANY obsValue, String obsDatetime) throws ParseException {
		if (StringUtils.isBlank(obsDatetime)) {
			throw new APIException("A date is required in order to create an Observation for an entry");
		}
		Date obsDate = CaseReportConstants.DATE_FORMATTER.parse(obsDatetime);
		Observation observation = createObservation(obsQuestion, obsValue, obsDate, ActStatus.Completed);
		return new Entry(x_ActRelationshipEntry.DRIV, null, observation);
	}
	
	private StructDocElementNode createTextNodeForDiagnostics() throws ParseException {
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
		if (form.getMostRecentCd4Count() != null) {
			addDatedValueToListNode(rootListNode, form.getMostRecentCd4Count(), DocumentConstants.TEXT_CD4_RECENT_COUNT);
		}
		if (form.getMostRecentHivTest() != null) {
			addDatedValueToListNode(rootListNode, form.getMostRecentHivTest(), DocumentConstants.TEXT_HIV_RECENT_TEST);
		}
		if (form.getMostRecentViralLoad() != null) {
			addDatedValueToListNode(rootListNode, form.getMostRecentViralLoad(), DocumentConstants.TEXT_RECENT_VIRAL_LOAD);
		}
		
		return rootListNode;
	}
	
	private void addDatedValueToListNode(StructDocElementNode listNode, DatedUuidAndValue datedValue, String label)
	    throws ParseException {
		Date date = CaseReportConstants.DATE_FORMATTER.parse(datedValue.getDate());
		String dateStr = DocumentConstants.DATE_FORMATTER.format(date);
		listNode.addElement(DocumentConstants.ELEMENT_ITEM, label + datedValue.getValue() + " (" + dateStr + ")");
	}
	
	private Section createSectionWithLoincCode(String code, String displayName) {
		Section section = new Section();
		section.setTemplateId(LIST.createLIST(new II(DocumentConstants.SECTION_TEMPLATE_ID_ROOT1)));
		section.setCode(createLoincCE(code, displayName));
		section.setTitle(displayName);
		return section;
	}
	
	private StructDocElementNode createTextNodeForTriggers() {
		StructDocElementNode rootListNode = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_TRIGGERS, form.getTriggers());
		if (StringUtils.isNotBlank(form.getComments())) {
			rootListNode.addElement(DocumentConstants.ELEMENT_ITEM, DocumentConstants.TEXT_COMMENTS + form.getComments());
		}
		return rootListNode;
	}
	
	private StructDocElementNode createTextNodeForMedications() {
		StructDocElementNode rootListNode = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		addNestedListToRootNode(rootListNode, DocumentConstants.TEXT_ARVS, form.getCurrentHivMedications());
		return rootListNode;
	}
	
	private void addNestedListToRootNode(StructDocElementNode parentNode, String label,
	                                     List<? extends UuidAndValue> itemsToAdd) {
		StructDocElementNode itemList = new StructDocElementNode(DocumentConstants.ELEMENT_LIST);
		for (UuidAndValue item : itemsToAdd) {
			itemList.addElement(DocumentConstants.ELEMENT_ITEM, item.getValue().toString());
		}
		StructDocTextNode labelNode = new StructDocTextNode(label);
		parentNode.addElement(DocumentConstants.ELEMENT_ITEM, labelNode, itemList);
	}
	
	private ArrayList<Entry> createEntriesForTriggers() throws ParseException {
		ArrayList<Entry> entries = new ArrayList<>(form.getTriggers().size());
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
			CD<String> question = createSnomedCD(DocumentConstants.SNOMED_CODE_TRIGGER, DocumentConstants.TEXT_TRIGGER);
			CD<String> value = createCielCD(code, concept.getDisplayString());
			entries.add(createObservationEntry(question, value, trigger.getDate()));
		}
		
		return entries;
	}
	
	private ArrayList<Entry> createEntriesForMedications() throws ParseException {
		ArrayList<Entry> entries = new ArrayList<>(form.getCurrentHivMedications().size());
		ConceptService cs = Context.getConceptService();
		for (DatedUuidAndValue med : form.getCurrentHivMedications()) {
			Drug drug = cs.getDrugByUuid(med.getUuid());
			String name = med.getValue().toString();
			if (drug == null) {
				throw new APIException("Cannot find drug with uuid " + med.getUuid() + ", seems like the drug named " + name
				        + " was deleted.");
			}
			Entry e = createObservationEntryWithACielQuestionCodeAndCodedValue(DocumentConstants.CIEL_CODE_HIV_TREAMENT,
			    DocumentConstants.TEXT_HIV_TREATMENT, drug.getConcept(), med.getDate(), name);
			entries.add(e);
		}
		
		return entries;
	}
	
	/**
	 * Gets the best code for the sepcified concept, the logic is such that it first looks for the
	 * CIEL code, the LOINC code, SNOMED concept code otherwise null.
	 *
	 * @param concept
	 * @return A CD<String> object
	 */
	private CD<String> createCD(Concept concept, String originalText) {
		String code = null;
		String codeSystem = null;
		String codeSystemName = null;
		for (ConceptMap map : concept.getConceptMappings()) {
			if (DocumentConstants.CODE_SYSTEM_NAME_CIEL.equalsIgnoreCase(map.getConceptReferenceTerm().getConceptSource()
			        .getName())) {
				code = map.getConceptReferenceTerm().getCode();
				codeSystem = DocumentConstants.CODE_SYSTEM_CIEL;
				codeSystemName = DocumentConstants.CODE_SYSTEM_NAME_CIEL;
			} else if (DocumentConstants.CODE_SYSTEM_NAME_LOINC.equalsIgnoreCase(map.getConceptReferenceTerm()
			        .getConceptSource().getName())) {
				code = map.getConceptReferenceTerm().getCode();
				codeSystem = DocumentConstants.CODE_SYSTEM_LOINC;
				codeSystemName = DocumentConstants.CODE_SYSTEM_NAME_LOINC;
			} else if (DocumentConstants.CODE_SYSTEM_NAME_SNOMEDCT.equalsIgnoreCase(map.getConceptReferenceTerm()
			        .getConceptSource().getName())) {
				code = map.getConceptReferenceTerm().getCode();
				codeSystem = DocumentConstants.CODE_SYSTEM_SNOMEDCT;
				codeSystemName = DocumentConstants.CODE_SYSTEM_NAME_SNOMEDCT;
			}
		}
		
		if (StringUtils.isNotBlank(code) && StringUtils.isNotBlank(codeSystem)) {
			String origText = concept.getDisplayString().equalsIgnoreCase(originalText) ? null : originalText;
			return new CD<>(code, codeSystem, codeSystemName, null, concept.getDisplayString(), origText);
		}
		
		return null;
	}
	
	private Observation createObservation(CD<String> questionConcept, ANY value, Date obsdatetime, ActStatus statusCode) {
		Observation observation = new Observation(x_ActMoodDocumentObservation.Eventoccurrence, questionConcept);
		observation.setTemplateId(LIST.createLIST(new II(DocumentConstants.OBS_TEMPLATE_ID_ROOT)));
		observation.setValue(value);
		observation.setStatusCode(statusCode);
		observation.setEffectiveTime(DocUtil.createTS(obsdatetime));
		return observation;
	}
}
