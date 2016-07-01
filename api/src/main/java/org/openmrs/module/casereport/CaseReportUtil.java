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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;

public class CaseReportUtil {
	
	private static final String SOURCE_CIEL_HL7_CODE = "CIEL";
	
	private static final String TERM_CODE_VIRAL_LOAD = "856";
	
	private static final String TERM_CODE_CD4_COUNT = "5497";
	
	private static final String TERM_CODE_HIV_TEST = "1040";
	
	private static final String TERM_CODE_WHO_STAGE = "5356";
	
	private static final String TERM_CODE_ARV_MED_SET = "1085";
	
	/**
	 * Gets the 3 most recent viral load observations for the specified patient, they are ordered in
	 * a way such that the most recent comes first and the earliest is last. Note that the method
	 * can return less than 3 items in case the patient has had less than 3 of them in total in the
	 * past.
	 * 
	 * @param patient the patient to match against
	 * @return a list of the most recent viral load observations
	 * @should return the 3 most recent Viral load observations
	 */
	public static List<Obs> getMostRecentViralLoads(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, TERM_CODE_VIRAL_LOAD, SOURCE_CIEL_HL7_CODE, 3);
	}
	
	/**
	 * Gets the 3 most recent cd4 count observations for the specified patient, they are ordered in
	 * a way such that the most recent comes first and the earliest is last. Note that the method
	 * can return less than 3 items in case the patient has had less than 3 of them in total in the
	 * past.
	 * 
	 * @param patient the patient to match against
	 * @return a list of the most recent cd4 count observations
	 * @should return the 3 most recent cd4 count observations
	 */
	public static List<Obs> getMostRecentCD4counts(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, TERM_CODE_CD4_COUNT, SOURCE_CIEL_HL7_CODE, 3);
	}
	
	/**
	 * Gets the 3 most HIV test result observations for the specified patient, they are ordered in a
	 * way such that the most recent comes first and the earliest is last. Note that the method can
	 * return less than 3 items in case the patient has had less than 3 of them in total in the
	 * past.
	 * 
	 * @param patient the patient to match against
	 * @return a list of the most recent HIV test observations
	 * @should return the 3 most recent HIV test observations
	 */
	public static List<Obs> getMostRecentHIVTests(Patient patient) {
		return getMostRecentObsByPatientAndConceptMapping(patient, TERM_CODE_HIV_TEST, SOURCE_CIEL_HL7_CODE, 3);
	}
	
	/**
	 * Gets the most recent WHO stage observation for the specified patient.
	 * 
	 * @param patient the patient to match against
	 * @return the most recent WHO stage observation
	 * @should return the most recent WHO stage observation
	 */
	public static Obs getMostRecentWHOStage(Patient patient) {
		List<Obs> whoStages = getMostRecentObsByPatientAndConceptMapping(patient, TERM_CODE_WHO_STAGE, SOURCE_CIEL_HL7_CODE,
		    1);
		if (whoStages.isEmpty()) {
			return null;
		}
		return whoStages.get(0);
	}
	
	/**
	 * Gets the current ARV medications for the specified patient
	 * 
	 * @param patient the patient to match against
	 * @param asOfDate
	 * @return a list of ARV medications
	 * @should get the current ARV medications for the specified patient
	 */
	public static List<Drug> getCurrentARVMedications(Patient patient, Date asOfDate) {
		List<Drug> arvs = new ArrayList<Drug>();
		Concept arvMedset = getConceptByMapping(TERM_CODE_ARV_MED_SET, SOURCE_CIEL_HL7_CODE);
		OrderService os = Context.getOrderService();
		OrderType orderType = os.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID);
		List<Order> orders = os.getActiveOrders(patient, orderType, null, asOfDate);
		for (Order order : orders) {
			DrugOrder drugOrder = (DrugOrder) order;
			if (arvMedset.getSetMembers().contains(order.getConcept()) && !arvs.contains(drugOrder.getDrug())) {
				arvs.add(drugOrder.getDrug());
			}
		}
		return arvs;
	}
	
	private static Concept getConceptByMapping(String code, String source) {
		Concept concept = Context.getConceptService().getConceptByMapping(code, source);
		if (concept == null) {
			throw new APIException("Failed to find concept with mapping " + source + ":" + code);
		}
		return concept;
	}
	
	private static List<Obs> getMostRecentObsByPatientAndConceptMapping(Patient patient, String code, String source,
	                                                                    Integer limit) {
		if (patient == null) {
			throw new APIException("Patient cannot be null");
		}
		
		List<Person> patients = Collections.singletonList((Person) patient);
		List<Concept> concepts = Collections.singletonList(getConceptByMapping(code, source));
		
		return Context.getObsService().getObservations(patients, null, concepts, null, null, null,
		    Collections.singletonList("obsDatetime"), limit, null, null, null, false);
	}
}
