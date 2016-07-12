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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportFormTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private CaseReportService service;
	
	/**
	 * @see CaseReportForm#getIdentifierType()
	 * @verifies generate the report form for the specified patient
	 */
	@Test
	public void shouldGenerateTheReportFormForTheSpecifiedPatient() throws Exception {
		executeDataSet("moduleTestData-initialCaseReports.xml");
		executeDataSet("moduleTestData-other.xml");
		PatientService ps = Context.getPatientService();
		Patient patient = ps.getPatient(2);
		patient.setDead(true);
		patient.setCauseOfDeath(Context.getConceptService().getConcept(22));
		patient.setDeathDate(CaseReportForm.DATE_FORMATTER.parse("2016-07-07T00:00:00.000-0400"));
		ps.savePatient(patient);
		CaseReport caseReport = service.getCaseReport(1);
		assertEquals(patient, caseReport.getPatient());
		assertNull(caseReport.getReportForm());
		CaseReportForm reportForm = new CaseReportForm(caseReport);
		assertNotNull(reportForm);
		assertEquals("Horatio", reportForm.getGivenName());
		assertEquals("Test", reportForm.getMiddleName());
		assertEquals("Hornblower", reportForm.getFamilyName());
		assertEquals("101-6", reportForm.getPatientIdentifier());
		assertEquals("OpenMRS Identification Number", reportForm.getIdentifierType());
		assertEquals(patient.getGender(), reportForm.getGender());
		assertEquals("1975-04-08T00:00:00.000-0500", reportForm.getBirthdate());
		assertEquals("2016-07-07T00:00:00.000-0400", reportForm.getDeathdate());
		assertEquals(patient.isDead(), reportForm.getDead());
		assertNotNull(reportForm.getTriggerAndDateCreatedMap());
		assertEquals(3, reportForm.getMostRecentDateAndViralLoadMap().size());
		assertEquals(3, reportForm.getMostRecentDateAndCd4CountMap().size());
		assertEquals(3, reportForm.getMostRecentDateAndHivTestMap().size());
		assertEquals(2, reportForm.getCurrentHivMedications().size());
		assertEquals("WHO HIV stage 2", reportForm.getMostRecentHivWhoStage());
		assertEquals("Regimen failure", reportForm.getMostRecentArvStopReason());
		assertEquals("2016-06-15T00:00:00.000-0400", reportForm.getLastVisitDate());
		assertEquals("UNKNOWN", reportForm.getCauseOfDeath());
		assertEquals(2, reportForm.getPreviousSubmittedCaseReports().size());
		assertTrue(reportForm.getPreviousSubmittedCaseReports().contains("Some weird trigger"));
		assertTrue(reportForm.getPreviousSubmittedCaseReports().contains("Some Unique Trigger"));
	}
}
