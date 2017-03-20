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

import static junit.framework.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.ClinicalDocumentGenerator;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.test.Util;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

@Ignore
public class ClinicalDocumentGeneratorTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @see ClinicalDocumentGenerator#generate()
	 * @verifies generate a CDA document
	 */
	@Test
	public void generate_shouldGenerateACDADocument() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-initialConcepts.xml");
		executeDataSet("moduleTestData-other.xml");
		executeDataSet("moduleTestData-HIE.xml");
		
		CaseReport caseReport = Context.getService(CaseReportService.class).getCaseReport(1);
		Patient patient = caseReport.getPatient();
		CaseReportForm form = new CaseReportForm(caseReport);
		form.setReportUuid(caseReport.getUuid());
		form.setReportDate(caseReport.getDateCreated());
		
		ClinicalDocument clinicalDocument = new ClinicalDocumentGenerator(form).generate();
		SimpleObject so = null;
		assertEquals("Composition", Util.getByPath(so, "resourceType"));
		assertEquals(caseReport.getUuid(), Util.getByPath(so, "id"));
		assertEquals("generated", Util.getByPath(so, "text/status"));
		assertEquals(caseReport.getUuid(), Util.getByPath(so, "identifier/value"));
		final String reportDate = "2016-03-30T00:00:00-04:00";
		assertEquals(reportDate, Util.getByPath(so, "date"));
		assertEquals(patient.getPersonName().getFullName(), Util.getByPath(so, "subject/display"));
		assertEquals(Context.getUserService().getUserByUuid(form.getSubmitter().getUuid()).getUsername(),
		    Util.getByPath(so, "author[0]/display"));
		assertEquals("Test_Impl", Util.getByPath(so, "custodian/reference"));
		assertEquals("Test_Name", Util.getByPath(so, "custodian/display"));
		String patientPath = "contained[0]";
		assertEquals("Patient", Util.getByPath(so, patientPath + "/resourceType"));
		assertEquals("patient", Util.getByPath(so, patientPath + "/id"));
		assertEquals(patient.getPatientIdentifier().getIdentifierType().getName(),
		    Util.getByPath(so, patientPath + "/identifier[0]/system"));
		assertEquals(patient.getPatientIdentifier().getIdentifier(),
		    Util.getByPath(so, patientPath + "/identifier[0]/value"));
		assertEquals(patient.getPersonName().getFullName(), Util.getByPath(so, patientPath + "/name[0]/text"));
		assertEquals(patient.getPersonName().getGivenName(), Util.getByPath(so, patientPath + "/name[0]/given[0]"));
		assertEquals(patient.getPersonName().getFamilyName(), Util.getByPath(so, patientPath + "/name[0]/family[0]"));
		assertEquals(patient.getPersonName().getMiddleName(), Util.getByPath(so, patientPath + "/name[0]/given[1]"));
		assertEquals("male", Util.getByPath(so, patientPath + "/gender"));
		assertEquals(new SimpleDateFormat("yyyy-MM-dd").format(patient.getBirthdate()),
		    Util.getByPath(so, patientPath + "/birthDate"));
		assertEquals("2016-03-20T00:00:00-04:00", Util.getByPath(so, patientPath + "/deceasedDateTime"));
		assertEquals(form.getTriggers().get(0).getValue(), Util.getByPath(so, "contained[1]/detail"));
		assertEquals(form.getTriggers().get(1).getValue(), Util.getByPath(so, "contained[2]/detail"));
		assertEquals(reportDate, Util.getByPath(so, "event[0]/period/start"));
		assertEquals(reportDate, Util.getByPath(so, "event[0]/period/end"));
		assertEquals(2, ((List) Util.getByPath(so, "event[0]/detail")).size());
		assertEquals(form.getTriggers().get(0).getValue(), Util.getByPath(so, "event[0]/detail[0]/display"));
		assertEquals(form.getTriggers().get(1).getValue(), Util.getByPath(so, "event[0]/detail[1]/display"));
		assertEquals(7, ((List) Util.getByPath(so, "section")).size());
		assertEquals(3, ((List) Util.getByPath(so, "section[0]/entry")).size());
		assertEquals(3, ((List) Util.getByPath(so, "section[1]/entry")).size());
		assertEquals(3, ((List) Util.getByPath(so, "section[2]/entry")).size());
		assertEquals(1, ((List) Util.getByPath(so, "section[3]/entry")).size());
		assertEquals(2, ((List) Util.getByPath(so, "section[4]/entry")).size());
		assertEquals(1, ((List) Util.getByPath(so, "section[5]/entry")).size());
		assertEquals(1, ((List) Util.getByPath(so, "section[6]/entry")).size());
	}
}
