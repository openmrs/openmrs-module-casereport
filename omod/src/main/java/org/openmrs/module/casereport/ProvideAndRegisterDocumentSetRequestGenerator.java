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

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.rim.AssociationType1;
import org.dcm4chee.xds2.infoset.rim.ClassificationType;
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
public class ProvideAndRegisterDocumentSetRequestGenerator {
	
	private static ProvideAndRegisterDocumentSetRequestGenerator instance;
	
	private ProvideAndRegisterDocumentSetRequestGenerator() {
	}
	
	public synchronized static ProvideAndRegisterDocumentSetRequestGenerator getInstance() {
		if (instance == null) {
			instance = new ProvideAndRegisterDocumentSetRequestGenerator();
		}
		return instance;
	}
	
	public SubmitObjectsRequest generate(CaseReportForm form) throws Exception {
		SubmitObjectsRequest registryRequest = new SubmitObjectsRequest();
		registryRequest.setRegistryObjectList(new RegistryObjectListType());
		ExtrinsicObjectType extrinsicObj = new ExtrinsicObjectType();
		extrinsicObj.setId(DocumentConstants.XDS_DOC_ID);
		extrinsicObj.setMimeType(MediaType.TEXT_XML.toString());
		extrinsicObj.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_EXTRINSIC_OBJECT);
		extrinsicObj.setName(createName(DocumentConstants.TEXT_TITLE));
		String date = DocUtil.createTS(form.getReportDate()).getValue();
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_CREATION_TIME, date);
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_LANGUAGE_CODE, DocumentConstants.LANGUAGE_CODE);
		String patientId = String.format(DocumentConstants.PATIENT_ID_PATTERN, form.getPatientIdentifier().getValue()
		        .toString(), form.getIdentifierType().getValue().toString());
		InfosetUtil.addOrOverwriteSlot(extrinsicObj, XDSConstants.SLOT_NAME_SOURCE_PATIENT_ID, patientId);
		addCodedValueClassification(extrinsicObj, DocumentConstants.LOINC_CODE_CR, DocumentConstants.CODE_SYSTEM_LOINC,
		    XDSConstants.UUID_XDSDocumentEntry_classCode, DocumentConstants.TEXT_DOCUMENT_NAME);
		
		addObjectToRequest(registryRequest, extrinsicObj, DocumentConstants.XDS_EXTRINSIC_OBJECT);
		
		RegistryPackageType regPackage = new RegistryPackageType();
		regPackage.setId(DocumentConstants.XDS_SUBSET_ID);
		regPackage.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_REG_PACKAGE);
		addObjectToRequest(registryRequest, regPackage, DocumentConstants.XDS_REG_PACKAGE);
		
		ClassificationType classification = new ClassificationType();
		classification.setId(DocumentConstants.XDS_CLASSIFICATION_ID);
		classification.setClassificationNode(XDSConstants.UUID_XDSSubmissionSet);
		classification.setClassifiedObject(DocumentConstants.XDS_SUBSET_ID);
		classification.setObjectType(DocumentConstants.XDS_SYMBOLIC_LINKS_PREFIX + DocumentConstants.XDS_CLASSIFICATION);
		addObjectToRequest(registryRequest, classification, DocumentConstants.XDS_CLASSIFICATION);
		
		AssociationType1 assoc = new AssociationType1();
		classification.setId(DocumentConstants.XDS_ASSOCIATION_ID);
		assoc.setAssociationType(XDSConstants.HAS_MEMBER);
		assoc.setSourceObject(DocumentConstants.XDS_SUBSET_ID);
		assoc.setTargetObject(DocumentConstants.XDS_DOC_ID);
		addObjectToRequest(registryRequest, assoc, DocumentConstants.XDS_ASSOCIATION);
		
		ProvideAndRegisterDocumentSetRequestType docRequest = new ProvideAndRegisterDocumentSetRequestType();
		docRequest.setSubmitObjectsRequest(registryRequest);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		QName qName = new QName(DocumentConstants.XDS_TX_NAMESPACE_URI, DocumentConstants.XDS_ACTION);
		JAXBElement element = new JAXBElement(qName, docRequest.getClass(), docRequest);
		JAXBContext jaxbContext = JAXBContext.newInstance(docRequest.getClass());
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		marshaller.marshal(element, out);
		System.out.println(out.toString());
		
		return registryRequest;
	}
	
	private <T extends IdentifiableType> void addObjectToRequest(SubmitObjectsRequest registryRequest, T object,
	                                                             String objectName) {
		QName qName = new QName(DocumentConstants.XDS_NAMESPACE_URI, objectName);
		JAXBElement<T> element = new JAXBElement<T>(qName, (Class<T>) object.getClass(), object);
		registryRequest.getRegistryObjectList().getIdentifiable().add(element);
	}
	
	private ClassificationType addCodedValueClassification(RegistryObjectType classifiedObj, String code, String codeSystem,
	                                                       String scheme, String name) throws JAXBException {
		ClassificationType classification = new ClassificationType();
		classification.setId(String.valueOf(System.currentTimeMillis()));
		classification.setClassifiedObject(classifiedObj.getId());
		classification.setClassificationScheme(scheme);
		classification.setNodeRepresentation(code);
		if (StringUtils.isNotBlank(name)) {
			classification.setName(createName(name));
		}
		InfosetUtil.addOrOverwriteSlot(classification, DocumentConstants.XDS_SLOT_CODING_SCHEME, codeSystem);
		classifiedObj.getClassification().add(classification);
		
		return classification;
	}
	
	private InternationalStringType createName(String name) {
		InternationalStringType iName = new InternationalStringType();
		LocalizedStringType localizedStringType = new LocalizedStringType();
		localizedStringType.setValue(name);
		iName.getLocalizedString().add(localizedStringType);
		return iName;
	}
}
