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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.TestUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class CaseReportUtilTest extends BaseModuleContextSensitiveTest {
	
	private static final String XML_OTHER_DATASET = "moduleTestData-other.xml";
	
	@Autowired
	PatientService patientService;
	
	/**
	 * @see CaseReportUtil#getMostRecentViralLoads(Patient)
	 * @verifies return the 3 most recent Viral load observations
	 */
	@Test
	public void getMostRecentViralLoads_shouldReturnThe3MostRecentViralLoadObservations() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> viralLoads = CaseReportUtil.getMostRecentViralLoads(patient);
		assertEquals(3, viralLoads.size());
		assertEquals(8003, viralLoads.get(0).getId().intValue());
		assertEquals(8001, viralLoads.get(1).getId().intValue());
		assertEquals(8000, viralLoads.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentCD4counts(Patient)
	 * @verifies return the 3 most recent cd4 count observations
	 */
	@Test
	public void getMostRecentCD4counts_shouldReturnThe3MostRecentCd4CountObservations() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> cd4counts = CaseReportUtil.getMostRecentCD4counts(patient);
		assertEquals(3, cd4counts.size());
		assertEquals(8010, cd4counts.get(0).getId().intValue());
		assertEquals(8008, cd4counts.get(1).getId().intValue());
		assertEquals(8007, cd4counts.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentHIVTests(Patient)
	 * @verifies return the 3 most recent HIV test observations
	 */
	@Test
	public void getMostRecentHIVTests_shouldReturnThe3MostRecentHIVTestObservations() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		List<Obs> hivTests = CaseReportUtil.getMostRecentHIVTests(patient);
		assertEquals(3, hivTests.size());
		assertEquals(8016, hivTests.get(0).getId().intValue());
		assertEquals(8014, hivTests.get(1).getId().intValue());
		assertEquals(8013, hivTests.get(2).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentWHOStage(Patient)
	 * @verifies return the most recent WHO stage observation
	 */
	@Test
	public void getMostRecentWHOStage_shouldReturnTheMostRecentWHOStageObservation() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(8020, CaseReportUtil.getMostRecentWHOStage(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getCurrentARVMedications(Patient, java.util.Date)
	 * @verifies get the current ARV medications for the specified patient
	 */
	@Test
	public void getCurrentARVMedications_shouldGetTheCurrentARVMedicationsForTheSpecifiedPatient() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		Date asOfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2016-01-14 00:00:00.0");
		List<Drug> meds = CaseReportUtil.getCurrentARVMedications(patient, asOfDate);
		assertEquals(1, meds.size());
		assertEquals(20000, meds.get(0).getId().intValue());
		asOfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2016-01-16 00:00:00.0");
		
		meds = CaseReportUtil.getCurrentARVMedications(patient, asOfDate);
		assertEquals(2, meds.size());
		TestUtil.containsId(meds, 20000);
		TestUtil.containsId(meds, 20001);
	}
	
	/**
	 * @see CaseReportUtil#getMostRecentReasonARVsStopped(Patient)
	 * @verifies return the most recent obs for the reason why the patient stopped taking ARVs
	 */
	@Test
	public void getMostRecentReasonARVsStopped_shouldReturnTheMostRecentObsForTheReasonWhyThePatientStoppedTakingARVs()
	    throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(8024, CaseReportUtil.getMostRecentReasonARVsStopped(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#getLastVisit(Patient)
	 * @verifies return the last visit for the specified patient
	 */
	@Test
	public void getLastVisit_shouldReturnTheLastVisitForTheSpecifiedPatient() throws Exception {
		executeDataSet(XML_OTHER_DATASET);
		Patient patient = patientService.getPatient(2);
		assertEquals(101, CaseReportUtil.getLastVisit(patient).getId().intValue());
	}
	
	/**
	 * @see CaseReportUtil#convertToCdaDocument(CaseReportForm)
	 * @verifies return the generated json
	 */
	@Test
	public void convertToCdaDocument_shouldReturnTheGeneratedJson() throws Exception {
		//System.out.println(CaseReportUtil.convertToCdaDocument(null));
	}
}
