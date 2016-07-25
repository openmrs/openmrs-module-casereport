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

import java.util.List;
import java.util.Map;

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
		patient.setDeathDate(CaseReportConstants.DATE_FORMATTER.parse("2016-07-07T00:00:00.000-0400"));
		ps.savePatient(patient);
		CaseReport caseReport = service.getCaseReport(1);
		assertEquals(patient, caseReport.getPatient());
		assertNull(caseReport.getReportForm());
		CaseReportForm reportForm = new CaseReportForm(caseReport);
		assertNotNull(reportForm);
		assertEquals(patient.getPersonName().getGivenName(), reportForm.getGivenName());
		assertEquals(patient.getPersonName().getMiddleName(), reportForm.getMiddleName());
		assertEquals(patient.getPersonName().getFamilyName(), reportForm.getFamilyName());
		assertEquals(patient.getPersonName().getFullName(), reportForm.getFullName());
		assertEquals(patient.getPatientIdentifier().getUuid(), reportForm.getPatientIdentifier().getUuid());
		assertEquals(patient.getPatientIdentifier().getIdentifier(), reportForm.getPatientIdentifier().getValue());
		assertEquals(patient.getPatientIdentifier().getIdentifierType().getUuid(), reportForm.getIdentifierType().getUuid());
		assertEquals(patient.getPatientIdentifier().getIdentifierType().getName(), reportForm.getIdentifierType().getValue());
		assertEquals(patient.getGender(), reportForm.getGender());
		assertEquals("1975-04-08T00:00:00.000-0500", reportForm.getBirthdate());
		assertEquals("2016-07-07T00:00:00.000-0400", reportForm.getDeathdate());
		assertEquals(patient.isDead(), reportForm.getDead());
		assertEquals(2, reportForm.getTriggers().size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(reportForm.getTriggers(), "HIV Virus Not Suppressed"));
		assertTrue(CaseReportUtil.collContainsItemWithValue(reportForm.getTriggers(), "Another Trigger"));
		assertEquals(3, reportForm.getMostRecentViralLoads().size());
		assertEquals(3, reportForm.getMostRecentCd4Counts().size());
		assertEquals(3, reportForm.getMostRecentHivTests().size());
		assertEquals(2, reportForm.getCurrentHivMedications().size());
		assertEquals("WHO HIV stage 2", reportForm.getCurrentHivWhoStage().getValue());
		assertEquals("Regimen failure", reportForm.getMostRecentArvStopReason().getValue());
		assertEquals("2016-06-15T00:00:00.000-0400", reportForm.getLastVisitDate().getValue());
		assertEquals(patient.getCauseOfDeath().getUuid(), reportForm.getCauseOfDeath().getUuid());
		assertEquals(patient.getCauseOfDeath().getName().getName(), reportForm.getCauseOfDeath().getValue());
		Map<String, List<DatedUuidAndValue>> map = reportForm.getPreviousReportUuidTriggersMap();
		assertEquals(2, map.size());
		final String casereportUuid1 = "er7d57f0-9088-11e1-aaa4-00248140a5ec";
		final String casereportUuid2 = "ui7d57f0-9188-11e1-aaa4-00248140a5ec";
		assertTrue(map.containsKey(casereportUuid1));
		assertTrue(map.containsKey(casereportUuid2));
		assertEquals(2, map.get(casereportUuid1).size());
		assertEquals(1, map.get(casereportUuid2).size());
		assertTrue(CaseReportUtil.collContainsItemWithValue(map.get(casereportUuid1), "Some weird trigger"));
		assertTrue(CaseReportUtil.collContainsItemWithValue(map.get(casereportUuid1), "Some Unique Trigger"));
		assertTrue(CaseReportUtil.collContainsItemWithValue(map.get(casereportUuid2), "Some weird trigger"));
	}
}
