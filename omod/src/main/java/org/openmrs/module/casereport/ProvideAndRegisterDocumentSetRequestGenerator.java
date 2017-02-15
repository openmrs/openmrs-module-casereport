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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

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
import org.dcm4chee.xds2.infoset.rim.RegistryObjectListType;
import org.dcm4chee.xds2.infoset.rim.RegistryObjectType;
import org.dcm4chee.xds2.infoset.rim.RegistryPackageType;
import org.dcm4chee.xds2.infoset.rim.SubmitObjectsRequest;
import org.dcm4chee.xds2.infoset.util.InfosetUtil;
import org.springframework.http.MediaType;

/**
 * Generates a soap envelope
 */
public final class ProvideAndRegisterDocumentSetRequestGenerator {
	
	protected static final Log log = LogFactory.getLog(ProvideAndRegisterDocumentSetRequestGenerator.class);
	
	private static Integer idCounter = 1;
	
	private static ProvideAndRegisterDocumentSetRequestGenerator instance;
	
	private ProvideAndRegisterDocumentSetRequestGenerator() {
	}
	
	public synchronized static ProvideAndRegisterDocumentSetRequestGenerator getInstance() {
		if (instance == null) {
			instance = new ProvideAndRegisterDocumentSetRequestGenerator();
		}
		return instance;
	}
	
	public ProvideAndRegisterDocumentSetRequestType generate(CaseReportForm form) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Generating ProvideAndRegisterDocumentSetRequest...");
		}
		//Create DocumentEntry/ExtrinsicObject
		ExtrinsicObjectType extrinsicObj = new ExtrinsicObjectType();
		extrinsicObj.setId(DocumentConstants.XDS_DOC_ID);
		extrinsicObj.setMimeType(MediaType.TEXT_XML.toString());
		extrinsicObj.setObjectType(XDSConstants.UUID_XDSDocumentEntry);
		extrinsicObj.setName(createName(DocumentConstants.TEXT_TITLE));
		String reportDate = DocUtil.createTS(form.getReportDate()).getValue();
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_CREATION_TIME, reportDate);
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_LANGUAGE_CODE, DocumentConstants.LANGUAGE_CODE);
		String patientId = String.format(DocumentConstants.PATIENT_ID_PATTERN, form.getPatientIdentifier().getValue()
		        .toString(), form.getIdentifierType().getValue().toString());
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_SOURCE_PATIENT_ID, patientId);
		addClassification(extrinsicObj, DocumentConstants.LOINC_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSDocumentEntry_classCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		addClassification(extrinsicObj, DocumentConstants.CODE_CONFIDENTIALITY_N,
		    DocumentConstants.CODE_SYSTEM_CONFIDENTIALITY, XDSConstants.UUID_XDSDocumentEntry_confidentialityCode,
		    DocumentConstants.TEXT_NORMAL);
		addClassification(extrinsicObj, DocumentConstants.CONNECTATHON_CODE_FACILITY,
		    DocumentConstants.CODE_SYSTEM_CONNECTATHON_FACILITY,
		    XDSConstants.UUID_XDSDocumentEntry_healthCareFacilityTypeCode, DocumentConstants.TEXT_FACILITY);
		addClassification(extrinsicObj, DocumentConstants.CONNECTATHON_CODE_FORMAT,
		    DocumentConstants.CODE_SYSTEM_CONNECTATHON_FORMAT, XDSConstants.UUID_XDSDocumentEntry_formatCode,
		    DocumentConstants.TEXT_FORMAT);
		addClassification(extrinsicObj, DocumentConstants.CONNECTATHON_CODE_PRACTICE,
		    DocumentConstants.CODE_SYSTEM_CONNECTATHON_PRACTICE, XDSConstants.UUID_XDSDocumentEntry_practiceSettingCode,
		    DocumentConstants.TEXT_PRACTICE);
		addClassification(extrinsicObj, DocumentConstants.LOINC_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSDocumentEntry_typeCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		addExternalIdentifier(extrinsicObj, patientId, XDSConstants.UUID_XDSDocumentEntry_patientId,
		    DocumentConstants.TEXT_DOC_PATIENT_ID);
		//String docUniqueId = generateOIDFromUuid(UUID.fromString(form.getReportUuid()));
		String docUniqueId = generateOIDFromUuid(UUID.randomUUID());
		addExternalIdentifier(extrinsicObj, docUniqueId, XDSConstants.UUID_XDSDocumentEntry_uniqueId,
		    DocumentConstants.TEXT_DOC_UNIQUE_ID);
		SubmitObjectsRequest registryRequest = new SubmitObjectsRequest();
		registryRequest.setRegistryObjectList(new RegistryObjectListType());
		
		addObjectToRequest(registryRequest, extrinsicObj, DocumentConstants.XDS_EXTRINSIC_OBJECT);
		
		//Create RegistryPackage/SubmissionSet
		RegistryPackageType regPackage = new RegistryPackageType();
		regPackage.setId(DocumentConstants.XDS_SUBSET_ID);
		regPackage.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_REG_PACKAGE);
		regPackage.setName(createName(DocumentConstants.TEXT_TITLE));
		String dateSubmitted = DocUtil.createTS(new Date()).getValue();
		InfosetUtil.addOrOverwriteSlot(regPackage, XDSConstants.SLOT_NAME_SUBMISSION_TIME, dateSubmitted);
		addClassification(regPackage, DocumentConstants.LOINC_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSSubmissionSet_contentTypeCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		addExternalIdentifier(regPackage, patientId, XDSConstants.UUID_XDSSubmissionSet_patientId,
		    DocumentConstants.TEXT_SUBSET_PATIENT_ID);
		String subUniqueId = generateOIDFromUuid(UUID.randomUUID());
		addExternalIdentifier(regPackage, subUniqueId, XDSConstants.UUID_XDSSubmissionSet_uniqueId,
		    DocumentConstants.TEXT_SUBSET_UNIQUE_ID);
		//TODO use GP for sourceId
		addExternalIdentifier(regPackage, "1.3.6.1.4.1.21367.2010.1.2", XDSConstants.UUID_XDSSubmissionSet_sourceId,
		    DocumentConstants.TEXT_SUBSET_SOURCE_ID);
		
		addObjectToRequest(registryRequest, regPackage, DocumentConstants.XDS_REG_PACKAGE);
		
		//Create the classification of the TX
		ClassificationType classification = new ClassificationType();
		classification.setId(DocumentConstants.XDS_CLASSIFICATION_ID);
		classification.setClassificationNode(XDSConstants.UUID_XDSSubmissionSet);
		classification.setClassifiedObject(DocumentConstants.XDS_SUBSET_ID);
		classification.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_CLASSIFICATION);
		addObjectToRequest(registryRequest, classification, DocumentConstants.XDS_CLASSIFICATION);
		
		//Create the association that links the DocumentEntry to the RegistryPackage
		AssociationType1 assoc = new AssociationType1();
		assoc.setId(DocumentConstants.XDS_ASSOCIATION_ID);
		assoc.setAssociationType(XDSConstants.HAS_MEMBER);
		assoc.setSourceObject(DocumentConstants.XDS_SUBSET_ID);
		assoc.setTargetObject(DocumentConstants.XDS_DOC_ID);
		addObjectToRequest(registryRequest, assoc, DocumentConstants.XDS_ASSOCIATION);
		InfosetUtil.addOrOverwriteSlot(assoc, XDSConstants.SLOT_NAME_SUBMISSIONSET_STATUS, DocumentConstants.TEXT_ORIGINAL);
		
		ProvideAndRegisterDocumentSetRequestType docRequest = new ProvideAndRegisterDocumentSetRequestType();
		docRequest.setSubmitObjectsRequest(registryRequest);
		Document document = new Document();
		document.setId(DocumentConstants.XDS_DOC_ID);
		document.setValue("Testing...".getBytes());
		//document.setValue(CdaDocumentGenerator.getInstance().generate(form).getBytes());
		docRequest.getDocument().add(document);
		
		return docRequest;
	}
	
	private <T extends IdentifiableType> void addObjectToRequest(SubmitObjectsRequest registryRequest, T object,
	                                                             String objectName) {
		QName qName = new QName(DocumentConstants.XDS_NAMESPACE_URI, objectName);
		JAXBElement<T> element = new JAXBElement<>(qName, (Class<T>) object.getClass(), object);
		registryRequest.getRegistryObjectList().getIdentifiable().add(element);
	}
	
	/**
	 * Adds a classification to the specified RegistryObjectType.
	 * 
	 * @param classifiedObj
	 * @param code
	 * @param codeSystem
	 * @param scheme
	 * @param localizedString
	 * @return
	 * @throws JAXBException
	 */
	private void addClassification(RegistryObjectType classifiedObj, String code, String codeSystem, String scheme,
	                               String localizedString) throws JAXBException {
		ClassificationType classification = new ClassificationType();
		classification.setId("id_" + idCounter.toString());
		idCounter++;
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
	 * @param classifiedObj
	 * @param scheme
	 * @param localizedString
	 * @return
	 * @throws JAXBException
	 */
	private void addExternalIdentifier(final RegistryObjectType classifiedObj, String value, final String scheme,
	                                   final String localizedString) throws JAXBException {
		
		ExternalIdentifierType extId = new ExternalIdentifierType();
		extId.setRegistryObject(classifiedObj.getId());
		extId.setId("id_" + idCounter.toString());
		idCounter++;
		extId.setValue(value);
		extId.setIdentificationScheme(scheme);
		if (StringUtils.isNotBlank(localizedString)) {
			extId.setName(createName(localizedString));
		}
		classifiedObj.getExternalIdentifier().add(extId);
	}
	
	private InternationalStringType createName(String name) {
		InternationalStringType iName = new InternationalStringType();
		LocalizedStringType localizedStringType = new LocalizedStringType();
		localizedStringType.setValue(name);
		iName.getLocalizedString().add(localizedStringType);
		return iName;
	}
	
	private String generateOIDFromUuid(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return DocumentConstants.OID_PREFIX + new BigInteger(bb.array()).abs().toString();
	}
}
