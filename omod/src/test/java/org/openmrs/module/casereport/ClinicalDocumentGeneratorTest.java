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
import static org.openmrs.module.casereport.TestUtils.attributeHasText;
import static org.openmrs.module.casereport.TestUtils.elementContainsText;
import static org.openmrs.module.casereport.TestUtils.elementExists;
import static org.openmrs.module.casereport.TestUtils.getAttribute;
import static org.openmrs.module.casereport.TestUtils.getCount;
import static org.openmrs.module.casereport.TestUtils.getElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.marc.everest.formatters.xml.datatypes.r1.DatatypeFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.R1FormatterCompatibilityMode;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.ClinicalDocumentGenerator;
import org.openmrs.module.casereport.UuidAndValue;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.w3c.dom.Document;

public class ClinicalDocumentGeneratorTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @see ClinicalDocumentGenerator#generate()
	 * @verifies generate a CDA document
	 */
	@Test
	public void generate_shouldGenerateACDADocument() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-other.xml");
		executeDataSet("moduleTestData-HIE.xml");
		final String implId = "Test_Impl";
		final String implName = "Test_Name";
		
		CaseReport caseReport = Context.getService(CaseReportService.class).getCaseReport(1);
		Patient patient = caseReport.getPatient();
		patient.setDead(true);
		patient.setDeathDate(CaseReportConstants.DATE_FORMATTER.parse("2016-03-20T00:00:00.000-0400"));
		ConceptService cs = Context.getConceptService();
		Concept causeOfDeath = cs.getConcept(22);
		causeOfDeath.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(cs
		        .getConceptSourceByName(CaseReportConstants.SOURCE_CIEL_HL7_CODE), "1067", null), null));
		patient.setCauseOfDeath(causeOfDeath);
		CaseReportForm form = new CaseReportForm(caseReport);
		final String comments = "Testing...";
		form.setComments(comments);
		Provider provider = Context.getProviderService().getProvider(1);
		UuidAndValue submitter = new UuidAndValue(provider.getUuid(), provider.getIdentifier());
		form.setSubmitter(submitter);
		form.setAssigningAuthorityId(implId);
		form.setAssigningAuthorityName(implName);
		
		ClinicalDocument clinicalDocument = new ClinicalDocumentGenerator(form).generate();
		
		XmlIts1Formatter fmtr = new XmlIts1Formatter();
		fmtr.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.ClinicalDocumentArchitecture));
		ByteArrayOutputStream cdaOutput = new ByteArrayOutputStream(8192);
		fmtr.graph(cdaOutput, clinicalDocument);
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		        .parse(new ByteArrayInputStream(cdaOutput.toByteArray()));
		
		final String orgOID = "1.3.6.1.4.1.21367.2010.1.2";
		assertEquals(orgOID, getAttribute(doc, "ClinicalDocument/id", "root"));
		assertEquals(caseReport.getUuid(), getAttribute(doc, "ClinicalDocument/id", "extension"));
		assertEquals("20160330000000.000-0400", getAttribute(doc, "ClinicalDocument/effectiveTime", "value"));
		assertEquals("N", getAttribute(doc, "//confidentialityCode", "code"));
		assertTrue(attributeHasText(doc, "ClinicalDocument/effectiveTime", "value"));
		PatientIdentifier pid = patient.getPatientIdentifier();
		assertEquals(pid.getIdentifierType().getName(), getAttribute(doc, "//patientRole/id", "root"));
		assertEquals(pid.getIdentifier(), getAttribute(doc, "//patientRole/id", "extension"));
		assertEquals(patient.getFamilyName(), getElement(doc, "//patient/name/family"));
		assertEquals(patient.getGivenName(), getElement(doc, "//patient/name/given[1]"));
		assertEquals(patient.getMiddleName(), getElement(doc, "//patient/name/given[2]"));
		assertEquals(patient.getGender(), getAttribute(doc, "//patient/administrativeGenderCode", "code"));
		assertEquals("19750408000000.000-0500", getAttribute(doc, "//patient/birthTime", "value"));
		assertEquals(orgOID, getAttribute(doc, "//providerOrganization/id", "root"));
		assertEquals(implName, getElement(doc, "//providerOrganization/name"));
		assertEquals(implId, getAttribute(doc, "//assignedAuthor/id", "root"));
		assertEquals(provider.getIdentifier(), getAttribute(doc, "//assignedAuthor/id", "extension"));
		assertEquals(provider.getPerson().getFamilyName(), getElement(doc, "//assignedPerson/name/family"));
		assertEquals(provider.getPerson().getGivenName(), getElement(doc, "//assignedPerson/name/given"));
		assertTrue(elementExists(doc, "//representedOrganization"));
		assertEquals(orgOID, getAttribute(doc, "//representedCustodianOrganization/id", "root"));
		assertEquals(implId, getAttribute(doc, "//representedCustodianOrganization/id", "extension"));
		assertEquals(implName, getElement(doc, "//representedCustodianOrganization/name"));
		assertEquals(5, getCount(doc, "//text/list/item"));
		assertEquals(2, getCount(doc, "//text/list/item[1]/list/item"));
		assertTrue(elementContainsText(doc, "//text/list/item[2]", comments));
		assertEquals(3, getCount(doc, "//text/list/item[3]/list/item"));
		assertEquals(2, getCount(doc, "//text/list/item[4]/list/item"));
		assertEquals(6, getCount(doc, "//text/list/item[5]/list/item"));
		assertEquals(12, getCount(doc, "//entry"));
	}
}
