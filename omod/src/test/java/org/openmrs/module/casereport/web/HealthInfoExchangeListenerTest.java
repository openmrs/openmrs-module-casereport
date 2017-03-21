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

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

@Ignore
public class HealthInfoExchangeListenerTest extends BaseModuleWebContextSensitiveTest {
	
	/**
	 * @see org.openmrs.module.casereport.HealthInfoExchangeListener#onApplicationEvent(CaseReportSubmittedEvent)
	 */
	@Test
	public void onApplicationEvent_shouldSubmitTheDocument() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-other.xml");
		executeDataSet("moduleTestData-HIE.xml");
		PatientService ps = Context.getPatientService();
		PatientIdentifierType idType = ps.getPatientIdentifierType(1);
		idType.setName("1.3.6.1.4.1.21367.2010.1.2.301");
		ps.savePatientIdentifierType(idType);
		ps.getPatient(2).getPatientIdentifier().setIdentifier("12345");
		
		CaseReportService service = Context.getService(CaseReportService.class);
		CaseReport caseReport = service.getCaseReport(1);
		Patient patient = caseReport.getPatient();
		patient.setDead(true);
		patient.setDeathDate(CaseReportConstants.DATE_FORMATTER.parse("2016-03-20T00:00:00.000-0400"));
		ConceptService cs = Context.getConceptService();
		Concept causeOfDeath = cs.getConcept(22);
		causeOfDeath.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(cs
		        .getConceptSourceByName(CaseReportConstants.SOURCE_CIEL_HL7_CODE), "1067", null), null));
		patient.setCauseOfDeath(causeOfDeath);
		CaseReportForm form = new CaseReportForm(caseReport);
		form.setComments("Testing...");
		caseReport.setReportForm(new ObjectMapper().writeValueAsString(form));
		service.submitCaseReport(caseReport);
		
		//TODO Add assertion for the file contents
	}
}
