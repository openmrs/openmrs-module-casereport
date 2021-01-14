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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.openmrs.module.casereport.TestUtils.attributeContainsText;
import static org.openmrs.module.casereport.TestUtils.elementExists;
import static org.openmrs.module.casereport.TestUtils.elementHasText;
import static org.openmrs.module.casereport.TestUtils.getAttribute;
import static org.openmrs.module.casereport.TestUtils.getElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.ihe.ObjectFactory;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.w3c.dom.Document;

public class ProvideAndRegisterDocGeneratorTest extends BaseModuleWebContextSensitiveTest {
	
	private ObjectFactory objectFactory = new ObjectFactory();
	
	/**
	 * @see ProvideAndRegisterDocGenerator#generate()
	 */
	@Test
	public void generate_shouldGenerateProvideAndRegisterDocumentSetRequest() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-other.xml");
		executeDataSet("moduleTestData-HIE.xml");
		
		CaseReport caseReport = Context.getService(CaseReportService.class).getCaseReport(1);
		CaseReportForm form = new CaseReportForm(caseReport);
		Provider provider = Context.getProviderService().getProvider(1);
		UuidAndValue submitter = new UuidAndValue(provider.getUuid(), provider.getIdentifier());
		form.setSubmitter(submitter);
		
		ProvideAndRegisterDocGenerator pnrGen = new ProvideAndRegisterDocGenerator(form);
		ProvideAndRegisterDocumentSetRequestType docRequest = pnrGen.generate();
		JAXBElement docRequestElement = objectFactory.createProvideAndRegisterDocumentSetRequest(docRequest);
		JAXBContext jaxbContext = JAXBContext.newInstance(ProvideAndRegisterDocumentSetRequestType.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		marshaller.marshal(docRequestElement, out);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		        .parse(new ByteArrayInputStream(out.toByteArray()));
		
		assertEquals(0, getElement(doc, "//Slot[@name='creationTime']/ValueList/Value").indexOf("20160330000000.000"));
		final String patientId = "101-6^^^&2.16.840.1.113883.1.3&ISO";
		assertEquals(patientId, getElement(doc, "//ExtrinsicObject/Slot[@name='sourcePatientId']/ValueList/Value"));
		assertEquals("PID-3|" + patientId, getElement(doc, "//Slot[@name='sourcePatientInfo']/ValueList/Value[1]"));
		assertEquals("PID-5|Hornblower^Horatio^Test^^",
		    getElement(doc, "//Slot[@name='sourcePatientInfo']/ValueList/Value[2]"));
		assertEquals("PID-7|19750408", getElement(doc, "//Slot[@name='sourcePatientInfo']/ValueList/Value[3]"));
		assertEquals("PID-8|M", getElement(doc, "//Slot[@name='sourcePatientInfo']/ValueList/Value[4]"));
		assertEquals("Test^User^Super^^^^^^&" + DocumentUtil.getOrganizationOID() + "&ISO",
		    getElement(doc, "//Slot[@name='authorPerson']/ValueList/Value"));
		final String scheme = "classificationScheme";
		assertEquals(
		    "N",
		    getAttribute(doc, "//Classification[@" + scheme + "='" + XDSConstants.UUID_XDSDocumentEntry_confidentialityCode
		            + "']/", "nodeRepresentation"));
		
		final String facilityScheme = XDSConstants.UUID_XDSDocumentEntry_healthCareFacilityTypeCode;
		assertEquals("Hospital Unit",
		    getAttribute(doc, "//Classification[@" + scheme + "='" + facilityScheme + "']/", "nodeRepresentation"));
		
		assertEquals("Connect-a-thon healthcareFacilityTypeCodes",
		    getElement(doc, "//Classification[@" + scheme + "='" + facilityScheme + "']/Slot/ValueList/Value"));
		
		assertEquals("Hospital Unit",
		    getAttribute(doc, "//Classification[@" + scheme + "='" + facilityScheme + "']/Name/LocalizedString", "value"));
		
		final String practiceScheme = XDSConstants.UUID_XDSDocumentEntry_practiceSettingCode;
		assertEquals("General Medicine",
		    getAttribute(doc, "//Classification[@" + scheme + "='" + practiceScheme + "']/", "nodeRepresentation"));
		
		assertEquals("Connect-a-thon practiceSettingCodes",
		    getElement(doc, "//Classification[@" + scheme + "='" + practiceScheme + "']/Slot/ValueList/Value"));
		
		assertEquals("General Medicine",
		    getAttribute(doc, "//Classification[@" + scheme + "='" + practiceScheme + "']/Name/LocalizedString", "value"));
		final String idScheme = "identificationScheme";
		assertEquals(
		    patientId,
		    getAttribute(doc, "//ExternalIdentifier[@" + idScheme + "='" + XDSConstants.UUID_XDSDocumentEntry_patientId
		            + "']", "value"));
		
		final String uniqueId = DocumentConstants.OID_PREFIX
		        + DocumentUtil.convertToDecimal(UUID.fromString(caseReport.getUuid()));
		assertEquals(
		    uniqueId,
		    getAttribute(doc, "//ExternalIdentifier[@" + idScheme + "='" + XDSConstants.UUID_XDSDocumentEntry_uniqueId
		            + "']", "value"));
		
		assertTrue(elementHasText(doc, "//Slot[@name='submissionTime']/ValueList/Value"));
		
		assertEquals(
		    patientId,
		    getAttribute(doc, "//ExternalIdentifier[@" + idScheme + "='" + XDSConstants.UUID_XDSSubmissionSet_patientId
		            + "']", "value"));
		
		assertTrue(attributeContainsText(doc, "//ExternalIdentifier[@" + idScheme + "='"
		        + XDSConstants.UUID_XDSSubmissionSet_uniqueId + "']", "value", DocumentConstants.OID_PREFIX));
		
		assertEquals(
		    DocumentUtil.getOrganizationOID(),
		    getAttribute(doc, "//ExternalIdentifier[@" + idScheme + "='" + XDSConstants.UUID_XDSSubmissionSet_sourceId
		            + "']", "value"));
		
		assertTrue(elementExists(doc, "//Association"));
		assertTrue(elementHasText(doc, "//*[local-name() = 'Document']"));
	}
}
