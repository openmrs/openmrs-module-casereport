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

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.marc.everest.datatypes.BL;
import org.marc.everest.datatypes.ENXP;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.ON;
import org.marc.everest.datatypes.PN;
import org.marc.everest.datatypes.SD;
import org.marc.everest.datatypes.TS;
import org.marc.everest.datatypes.doc.StructDocElementNode;
import org.marc.everest.datatypes.generic.LIST;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.AssignedAuthor;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Author;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component2;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Component3;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Organization;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Patient;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.PatientRole;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Person;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.RecordTarget;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.Section;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.StructuredBody;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ActRelationshipHasComponent;
import org.marc.everest.rmim.uv.cdar2.vocabulary.AdministrativeGender;
import org.marc.everest.rmim.uv.cdar2.vocabulary.ContextControl;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.web.CdaDocumentGenerator;

/**
 * Contains utility methods for creating entities to add to the CDA document
 */
public class CdaUtil {
	
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
				patientRole.setId(SET.createSET(new II(CdaDocumentGenerator.PATIENT_ID_ROOT, id.toString())));
			}
		}
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
	public static Organization createOrganization(String id, String name) {
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
		Author author = new Author(ContextControl.OverridingPropagating);
		AssignedAuthor assignedAuthor = new AssignedAuthor();
		author.setTime(TS.now());
		String systemId = form.getSubmitter().getValue().toString();
		assignedAuthor.setId(SET.createSET(new II(null, systemId)));
		User user = Context.getUserService().getUserByUsername(systemId);
		Person person = createPerson(user.getPersonName());
		assignedAuthor.setAssignedAuthorChoice(person);
		Organization org = createOrganization(form.getAssigningAuthorityId(), form.getAssigningAuthorityName());
		assignedAuthor.setRepresentedOrganization(org);
		author.setAssignedAuthor(assignedAuthor);
		
		return author;
	}
	
	/**
	 * Create a Person
	 *
	 * @param personName
	 * @return a Person instance
	 */
	public static Person createPerson(PersonName personName) {
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
	
	public static Component2 createComponent(CaseReportForm form) {
		Section section = new Section();
		section.setTemplateId(LIST.createLIST(new II(CdaDocumentGenerator.SECTION_TEMPLATE_ID_ROOT1), new II(
		        CdaDocumentGenerator.SECTION_TEMPLATE_ID_ROOT2)));
		section.setId(form.getReportUuid());
		section.setTitle(CdaDocumentGenerator.TITLE);
		StructDocElementNode list = new StructDocElementNode("list");
		for (DatedUuidAndValue trigger : form.getTriggers()) {
			list.addElement("item", trigger.getValue().toString());
		}
		SD sd = new SD(list);
		sd.setLanguage(CdaDocumentGenerator.LANGUAGE_CODE);
		section.setText(sd);
		Component3 comp3 = new Component3();
		comp3.setSection(section);
		Component2 comp = new Component2(ActRelationshipHasComponent.HasComponent, BL.TRUE);
		comp.setBodyChoice(new StructuredBody(comp3));
		
		return comp;
	}
	
	/**
	 * Create an author with the specified name
	 */
	/*public static Author createAuthorLimited(String id) {
		Author retVal = new Author(ContextControl.OverridingPropagating);
		AssignedAuthor assignedAuthor = new AssignedAuthor();
		retVal.setTime(TS.now());
		
		// Set ID 
		assignedAuthor.setId(SET.createSET(new II("2.16.840.1.113883.4.6", id),
		    new II(String.format("1.3.6.1.4.1.12009.1.99.7.%s", id))));
		retVal.setAssignedAuthor(assignedAuthor);
		
		return retVal;
	}*/
	
	/**
	 * Create a custodian organization
	 */
	/*public static Custodian createCustodian() {
		Custodian retVal = new Custodian();
		AssignedCustodian assignedCustodian = new AssignedCustodian();
		CustodianOrganization organization = new CustodianOrganization();
		Organization copyOrganization = createOrganization();
		
		organization.setId(copyOrganization.getId());
		organization.setTelecom(copyOrganization.getTelecom().get(0));
		organization.setName(copyOrganization.getName().get(0));
		
		assignedCustodian.setRepresentedCustodianOrganization(organization);
		retVal.setAssignedCustodian(assignedCustodian);
		
		return retVal;
	} */
	
	/**
	 * Creates a participant as a father
	 */
	/*public static Participant1 createFatherParticipant() {
		Participant1 retVal = new Participant1();
		retVal.setTypeCode(ParticipationType.IND);
		AssociatedEntity associatedEntity = new AssociatedEntity();
		
		retVal.setTime(TS.now());
		associatedEntity.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.99.7", "3012")));
		associatedEntity.setClassCode(RoleClassAssociative.NextOfKin);
		associatedEntity.setCode("FTH", "CdaHandlerConstants.CODE_SYSTEM_FAMILY_MEMBER");
		associatedEntity.setAssociatedPerson(createPerson("Andrew", "Smith"));
		retVal.setAssociatedEntity(associatedEntity);
		
		return retVal;
	} */
	
	/**
	 * Creates a participant as a spouse
	 */
	/*public static Participant1 createSpouseParticipant() {
		Participant1 retVal = new Participant1();
		retVal.setTypeCode(ParticipationType.IND);
		
		AssociatedEntity associatedEntity = new AssociatedEntity();
		retVal.setTemplateId(LIST.createLIST(new II("1.3.6.1.4.1.19376.1.5.3.1.2.4.1")));
		associatedEntity.setClassCode(RoleClassAssociative.NextOfKin);
		retVal.setTime(TS.now());
		associatedEntity.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.99.7", "3014")));
		associatedEntity.setCode("127848009", "CdaHandlerConstants.CODE_SYSTEM_SNOMED");
		associatedEntity.setAssociatedPerson(createPerson("Jason", "Taylor"));
		retVal.setAssociatedEntity(associatedEntity);
		
		return retVal;
	}*/
	
	/**
	 * Creates a participant as a father of baby
	 */
	/*public static Participant1 createFatherOfBabyParticipant() {
		Participant1 retVal = new Participant1();
		retVal.setTypeCode(ParticipationType.IND);
		
		AssociatedEntity associatedEntity = new AssociatedEntity();
		retVal.setTemplateId(LIST.createLIST(new II("1.3.6.1.4.1.19376.1.5.3.1.2.4.2")));
		associatedEntity.setClassCode(RoleClassAssociative.PersonalRelationship);
		retVal.setTime(TS.now());
		associatedEntity.setId(SET.createSET(new II("1.3.6.1.4.1.12009.1.99.7", "3014")));
		associatedEntity.setCode("xx-fatherofbaby", "CdaHandlerConstants.CODE_SYSTEM_SNOMED");
		associatedEntity.setAssociatedPerson(createPerson("Jason", "Taylor"));
		retVal.setAssociatedEntity(associatedEntity);
		
		return retVal;
	}*/
	
}
