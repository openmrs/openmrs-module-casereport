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

import java.util.Collections;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;

public class CaseReportUtil {
	
	private static final String SOURCE_CIEL_HL7_CODE = "CIEL";
	
	private static final String TERM_CODE_VIRAL_LOAD = "856";
	
	/**
	 * Gets the 3 most recent viral load observations for the specified patient ordersed such that
	 * the most recent is first and the earliest last
	 * 
	 * @should return the most recent 3 Viral load observations
	 * @return a list of Obs
	 */
	public static List<Obs> getThe3MostRecentViralLoads(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, TERM_CODE_VIRAL_LOAD, SOURCE_CIEL_HL7_CODE, 3);
	}
	
	private static List<Obs> getMostRecentObsByPatientAndConceptMapping(Patient patient, String code, String source,
	                                                                    Integer limit) {
		if (patient == null) {
			throw new APIException("Patient cannot be null");
		}
		Concept concept = Context.getConceptService().getConceptByMapping(code, source);
		if (concept == null) {
			throw new APIException("Failed to find concept with mapping " + SOURCE_CIEL_HL7_CODE + ":"
			        + TERM_CODE_VIRAL_LOAD);
		}
		
		return Context.getObsService().getObservations(Collections.singletonList((Person) patient), null,
		    Collections.singletonList(concept), null, null, null, Collections.singletonList("obsDatetime"), limit, null,
		    null, null, false);
	}
}
