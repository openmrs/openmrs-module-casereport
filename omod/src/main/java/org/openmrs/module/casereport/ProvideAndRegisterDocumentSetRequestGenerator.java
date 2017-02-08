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
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.rim.ExtrinsicObjectType;
import org.dcm4chee.xds2.infoset.rim.RegistryObjectListType;
import org.dcm4chee.xds2.infoset.rim.SubmitObjectsRequest;

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
	
	public SubmitObjectsRequest generate(CaseReportForm reportForm) throws Exception {
		SubmitObjectsRequest registryRequest = new SubmitObjectsRequest();
		registryRequest.setRegistryObjectList(new RegistryObjectListType());
		ExtrinsicObjectType extrinsicObject = new ExtrinsicObjectType();
		ProvideAndRegisterDocumentSetRequestType docRequest = new ProvideAndRegisterDocumentSetRequestType();
		docRequest.setSubmitObjectsRequest(registryRequest);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		QName qName = new QName("urn:ihe:iti:xds-b:2007", "ProvideAndRegisterDocumentSetRequest", "xdsb");
		JAXBElement element = new JAXBElement(qName, docRequest.getClass(), docRequest);
		JAXBContext jaxbContext = JAXBContext.newInstance(docRequest.getClass());
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
		marshaller.marshal(element, out);
		System.out.println(out.toString());
		
		return registryRequest;
	}
}
