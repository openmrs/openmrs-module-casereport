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

import static org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType.Document;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.rim.AssociationType1;
import org.dcm4chee.xds2.infoset.rim.ClassificationType;
import org.dcm4chee.xds2.infoset.rim.ExternalIdentifierType;
import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;
import org.dcm4chee.xds2.infoset.rim.IdentifiableType;
import org.dcm4chee.xds2.infoset.rim.InternationalStringType;
import org.dcm4chee.xds2.infoset.rim.LocalizedStringType;
import org.dcm4chee.xds2.infoset.rim.ObjectFactory;
import org.dcm4chee.xds2.infoset.rim.RegistryObjectListType;
import org.dcm4chee.xds2.infoset.rim.RegistryObjectType;
import org.dcm4chee.xds2.infoset.rim.RegistryPackageType;
import org.dcm4chee.xds2.infoset.rim.SubmitObjectsRequest;
import org.dcm4chee.xds2.infoset.util.InfosetUtil;
import org.marc.everest.datatypes.TS;
import org.marc.everest.formatters.xml.datatypes.r1.DatatypeFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.R1FormatterCompatibilityMode;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.vocabulary.AdministrativeGender;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.springframework.http.MediaType;

/**
 * Generates an XDS.b ProvideAndRegisterDocumentSetRequestType object from a CaseReportForm
 */
public final class ProvideAndRegisterDocGenerator {
	
	protected static final Log log = LogFactory.getLog(ProvideAndRegisterDocGenerator.class);
	
	private int idCounter;
	
	private ObjectFactory objectFactory = new ObjectFactory();
	
	private CaseReportForm form;
	
	/**
	 * @param form the CaseReportForm from which to generate for a document set request
	 */
	ProvideAndRegisterDocGenerator(CaseReportForm form) {
		this.form = form;
	}
	
	/**
	 * Generates a ProvideAndRegisterDocumentSetRequestType object from its backing CaseReportForm
	 * object
	 * 
	 * @return ProvideAndRegisterDocumentSetRequestType object
	 * @throws Exception
	 */
	public ProvideAndRegisterDocumentSetRequestType generate() throws Exception {
		if (log.isDebugEnabled()) {
			CaseReportService crs = Context.getService(CaseReportService.class);
			CaseReport cr = crs.getCaseReportByUuid(form.getReportUuid());
			log.debug("Generating ProvideAndRegisterDocumentSetRequest for: " + cr);
		}
		
		//reset in case this method is called multiple times on the same instance
		idCounter = 0;
		
		//Create DocumentEntry/ExtrinsicObject
		ExtrinsicObjectType extrinsicObj = new ExtrinsicObjectType();
		extrinsicObj.setId(DocumentConstants.XDS_DOC_ID);
		extrinsicObj.setMimeType(MediaType.TEXT_XML.toString());
		extrinsicObj.setObjectType(XDSConstants.UUID_XDSDocumentEntry);
		extrinsicObj.setName(createName(DocumentConstants.TEXT_TITLE));
		String reportDate = DocumentUtil.createTS(form.getReportDate()).getValue();
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_CREATION_TIME, reportDate);
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_LANGUAGE_CODE, DocumentConstants.LANGUAGE_CODE);
		AdministrationService as = Context.getAdministrationService();
		String patientId = String.format(DocumentUtil.getIdFormat(), form.getIdentifierType().getValue().toString(), form
		        .getPatientIdentifier().getValue().toString());
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_SOURCE_PATIENT_ID, patientId);
		
		String[] sourcePatientInfo = createPatientInfo(patientId);
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_SOURCE_PATIENT_INFO, sourcePatientInfo);
		
		String providerId = form.getSubmitter().getValue().toString();
		PersonName personName = DocumentUtil.getPersonNameForProvider(providerId);
		String authorId = String.format(DocumentConstants.PROV_ID_FORMAT, form.getAssigningAuthorityId(),
		    personName.getGivenName(), personName.getFamilyName(), providerId);
		ClassificationType authorClassification = new ClassificationType();
		authorClassification.setId(DocumentConstants.DOC_ID_PREFIX + idCounter++);
		authorClassification.setClassifiedObject(extrinsicObj.getId());
		authorClassification.setClassificationScheme(XDSConstants.UUID_XDSDocumentEntry_author);
		authorClassification.setNodeRepresentation("");
		InfosetUtil.addOrOverwriteSlot(authorClassification, XDSConstants.SLOT_NAME_AUTHOR_PERSON, authorId);
		extrinsicObj.getClassification().add(authorClassification);
		
		addClassification(extrinsicObj, DocumentConstants.LOINC_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSDocumentEntry_classCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		
		String confidentiality = DocumentUtil.getConfidentialityCode();
		addClassification(extrinsicObj, confidentiality, DocumentConstants.CODE_SYSTEM_CONFIDENTIALITY,
		    XDSConstants.UUID_XDSDocumentEntry_confidentialityCode,
		    DocumentUtil.getConfidentialityCodeNameMap().get(confidentiality));
		
		String facilityTypeCode = as.getGlobalProperty(DocumentConstants.GP_FACILITY_TYPE_CODE);
		String facilityTypeCodeScheme = as.getGlobalProperty(DocumentConstants.GP_FACILITY_TYPE_CODING_SCHEME);
		String facilityTypeName = as.getGlobalProperty(DocumentConstants.GP_FACILITY_TYPE_NAME);
		addClassification(extrinsicObj, facilityTypeCode, facilityTypeCodeScheme,
		    XDSConstants.UUID_XDSDocumentEntry_healthCareFacilityTypeCode, facilityTypeName);
		
		addClassification(extrinsicObj, DocumentConstants.IHE_PCC_CODE_FORMAT,
		    DocumentConstants.CODE_SYSTEM_FORMAT_CODE_SET, XDSConstants.UUID_XDSDocumentEntry_formatCode,
		    DocumentConstants.TEXT_FORMAT);
		
		String practiceCode = as.getGlobalProperty(DocumentConstants.GP_PRACTICE_CODE);
		String practiceCodingScheme = as.getGlobalProperty(DocumentConstants.GP_PRACTICE_CODING_SCHEME);
		String practiceName = as.getGlobalProperty(DocumentConstants.GP_PRACTICE_NAME);
		addClassification(extrinsicObj, practiceCode, practiceCodingScheme,
		    XDSConstants.UUID_XDSDocumentEntry_practiceSettingCode, practiceName);
		
		addClassification(extrinsicObj, DocumentConstants.LOINC_CODE_TYPE_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSDocumentEntry_typeCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		
		addExternalIdentifier(extrinsicObj, patientId, XDSConstants.UUID_XDSDocumentEntry_patientId,
		    DocumentConstants.TEXT_DOC_PATIENT_ID);
		
		String docUniqueId = generateOIDFromUuid(UUID.fromString(form.getReportUuid()));
		addExternalIdentifier(extrinsicObj, docUniqueId, XDSConstants.UUID_XDSDocumentEntry_uniqueId,
		    DocumentConstants.TEXT_DOC_UNIQUE_ID);
		
		SubmitObjectsRequest registryRequest = new SubmitObjectsRequest();
		registryRequest.setRegistryObjectList(new RegistryObjectListType());
		addObjectToRequest(registryRequest, extrinsicObj);
		
		//Create RegistryPackage/SubmissionSet
		RegistryPackageType regPackage = new RegistryPackageType();
		regPackage.setId(DocumentConstants.XDS_SUBSET_ID);
		regPackage.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_REG_PACKAGE);
		regPackage.setName(createName(DocumentConstants.TEXT_TITLE));
		String dateSubmitted = DocumentUtil.createTS(new Date()).getValue();
		InfosetUtil.addOrOverwriteSlot(regPackage, XDSConstants.SLOT_NAME_SUBMISSION_TIME, dateSubmitted);
		addClassification(regPackage, DocumentConstants.LOINC_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSSubmissionSet_contentTypeCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		
		addExternalIdentifier(regPackage, patientId, XDSConstants.UUID_XDSSubmissionSet_patientId,
		    DocumentConstants.TEXT_SUBSET_PATIENT_ID);
		
		String subUniqueId = generateOIDFromUuid(UUID.randomUUID());
		addExternalIdentifier(regPackage, subUniqueId, XDSConstants.UUID_XDSSubmissionSet_uniqueId,
		    DocumentConstants.TEXT_SUBSET_UNIQUE_ID);
		
		addExternalIdentifier(regPackage, DocumentUtil.getOrganisationOID(), XDSConstants.UUID_XDSSubmissionSet_sourceId,
		    DocumentConstants.TEXT_SUBSET_SOURCE_ID);
		
		addObjectToRequest(registryRequest, regPackage);
		
		//Create the classification of the TX
		ClassificationType classification = new ClassificationType();
		classification.setId(DocumentConstants.XDS_CLASSIFICATION_ID);
		classification.setClassificationNode(XDSConstants.UUID_XDSSubmissionSet);
		classification.setClassifiedObject(DocumentConstants.XDS_SUBSET_ID);
		classification.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_CLASSIFICATION);
		addObjectToRequest(registryRequest, classification);
		
		//Create the association that links the DocumentEntry to the RegistryPackage
		AssociationType1 assoc = new AssociationType1();
		assoc.setId(DocumentConstants.XDS_ASSOCIATION_ID);
		assoc.setAssociationType(XDSConstants.HAS_MEMBER);
		assoc.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_ASSOCIATION);
		assoc.setSourceObject(DocumentConstants.XDS_SUBSET_ID);
		assoc.setTargetObject(DocumentConstants.XDS_DOC_ID);
		InfosetUtil.addOrOverwriteSlot(assoc, XDSConstants.SLOT_NAME_SUBMISSIONSET_STATUS, DocumentConstants.TEXT_ORIGINAL);
		addObjectToRequest(registryRequest, assoc);
		
		ProvideAndRegisterDocumentSetRequestType docRequest = new ProvideAndRegisterDocumentSetRequestType();
		docRequest.setSubmitObjectsRequest(registryRequest);
		ClinicalDocument cdaDocument = new ClinicalDocumentGenerator(form).generate();
		
		XmlIts1Formatter fmtr = new XmlIts1Formatter();
		//This instructs the XML ITS1 Formatter we want to use CDA datatypes
		fmtr.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.ClinicalDocumentArchitecture));
		//The cda is ~8KB, might as well initialize the
		//byte array to a fairly large size
		ByteArrayOutputStream cdaOutput = new ByteArrayOutputStream(8192);
		fmtr.graph(cdaOutput, cdaDocument);
		
		Document document = new Document();
		document.setId(DocumentConstants.XDS_DOC_ID);
		document.setValue(cdaOutput.toByteArray());
		docRequest.getDocument().add(document);
		
		return docRequest;
	}
	
	/**
	 * Creates the source patient info that goes in the sourcePatientInfo slot of the provide and
	 * register request
	 * 
	 * @param patientId the patient Id
	 * @return an array of the patient info
	 * @throws ParseException
	 */
	private String[] createPatientInfo(String patientId) throws ParseException {
		List<String> patientInfoList = new ArrayList<>(4);
		patientInfoList.add(String.format(DocumentConstants.PID_3_PATTERN, patientId));
		String gName = "";
		String fName = "";
		String mName = "";
		if (form.getGivenName() != null) {
			gName = form.getGivenName();
		}
		if (form.getFamilyName() != null) {
			fName = form.getFamilyName();
		}
		if (form.getMiddleName() != null) {
			mName = form.getMiddleName();
		}
		patientInfoList.add(String.format(DocumentConstants.PID_5_PATTERN, fName, gName, mName));
		if (form.getBirthdate() != null) {
			Date bDate = CaseReportConstants.DATE_FORMATTER.parse(form.getBirthdate());
			//The XDS repository expects birth date set to day precision
			patientInfoList.add(String.format(DocumentConstants.PID_7_PATTERN, DocumentUtil.createTS(bDate, TS.DAY)));
		}
		AdministrativeGender gender = AdministrativeGender.Undifferentiated;
		if ("M".equals(form.getGender())) {
			gender = AdministrativeGender.Male;
		} else if ("F".equals(form.getGender())) {
			gender = AdministrativeGender.Female;
		}
		patientInfoList.add(String.format(DocumentConstants.PID_8_PATTERN, gender));
		String[] patientInfo = new String[patientInfoList.size()];
		int index = 0;
		for (String s : patientInfoList) {
			patientInfo[index] = s;
			index++;
		}
		
		return patientInfo;
	}
	
	/**
	 * Adds an IdentifiableType object to the specified SubmitObjectsRequest
	 * 
	 * @param registryRequest the SubmitObjectsRequest object
	 * @param object IdentifiableType object
	 */
	private <T extends IdentifiableType> void addObjectToRequest(SubmitObjectsRequest registryRequest, T object) {
		
		JAXBElement<T> element;
		
		if (ExtrinsicObjectType.class.isAssignableFrom(object.getClass())) {
			element = (JAXBElement<T>) objectFactory.createExtrinsicObject((ExtrinsicObjectType) object);
		} else if (RegistryPackageType.class.isAssignableFrom(object.getClass())) {
			element = (JAXBElement<T>) objectFactory.createRegistryPackage((RegistryPackageType) object);
		} else if (ClassificationType.class.isAssignableFrom(object.getClass())) {
			element = (JAXBElement<T>) objectFactory.createClassification((ClassificationType) object);
		} else if (AssociationType1.class.isAssignableFrom(object.getClass())) {
			element = (JAXBElement<T>) objectFactory.createAssociation((AssociationType1) object);
		} else {
			throw new APIException("Can't add object to the RegistryObjectList because it's of an unsupported type:"
			        + object.getClass());
		}
		
		registryRequest.getRegistryObjectList().getIdentifiable().add(element);
	}
	
	/**
	 * Adds a classification to the specified RegistryObjectType.
	 * 
	 * @param classifiedObj the object to which to add the classified object
	 * @param code the value for the nodeRepresentation attribute
	 * @param codeSystem the OID of teh coding scheme the code belongs
	 * @param scheme the XDS.b classification scheme URN
	 * @param localizedString the internationalized text label for the scheme
	 * @throws JAXBException
	 */
	private void addClassification(RegistryObjectType classifiedObj, String code, String codeSystem, String scheme,
	                               String localizedString) throws JAXBException {
		ClassificationType classification = new ClassificationType();
		classification.setId(DocumentConstants.DOC_ID_PREFIX + idCounter++);
		classification.setClassifiedObject(classifiedObj.getId());
		classification.setClassificationScheme(scheme);
		classification.setNodeRepresentation(code);
		if (StringUtils.isNotBlank(localizedString)) {
			classification.setName(createName(localizedString));
		}
		InfosetUtil.addOrOverwriteSlot(classification, DocumentConstants.XDS_SLOT_CODING_SCHEME, codeSystem);
		classifiedObj.getClassification().add(classification);
	}
	
	/**
	 * Adds an externalIdentifier to the specified RegistryObjectType.
	 *
	 * @param classifiedObj the object to which to add the external identifier object
	 * @param value the identifier
	 * @param scheme the XDS.b identification scheme URN
	 * @param localizedString the internationalized text label for the scheme
	 */
	private void addExternalIdentifier(final RegistryObjectType classifiedObj, String value, final String scheme,
	                                   final String localizedString) {
		
		ExternalIdentifierType extId = new ExternalIdentifierType();
		extId.setRegistryObject(classifiedObj.getId());
		extId.setId(DocumentConstants.DOC_ID_PREFIX + idCounter++);
		extId.setValue(value);
		extId.setIdentificationScheme(scheme);
		if (StringUtils.isNotBlank(localizedString)) {
			extId.setName(createName(localizedString));
		}
		classifiedObj.getExternalIdentifier().add(extId);
	}
	
	/**
	 * Creates an InternationalStringType for the specified name
	 * 
	 * @param name the localized name
	 * @return the InternationalStringType object
	 */
	private InternationalStringType createName(String name) {
		InternationalStringType iName = new InternationalStringType();
		LocalizedStringType localizedStringType = new LocalizedStringType();
		localizedStringType.setValue(name);
		iName.getLocalizedString().add(localizedStringType);
		return iName;
	}
	
	/**
	 * Generates and returns an OID from the specified uuid object
	 * 
	 * @param uuid the uuid object
	 * @return the generated OID
	 */
	private String generateOIDFromUuid(UUID uuid) {
		return DocumentConstants.OID_PREFIX + DocumentUtil.convertToDecimal(uuid);
	}
}
