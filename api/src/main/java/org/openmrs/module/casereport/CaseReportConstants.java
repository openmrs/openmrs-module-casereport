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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CaseReportConstants {
	
	public static final String MODULE_ID = "casereport";
	
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public static final String TRIGGER_PATIENT_DIED = "Patient Died";
	
	public static final String TRIGGER_NAME_TASK_PROPERTY = "Trigger Name";
	
	public static final String CONCEPT_TASK_PROPERTY = "Concept";
	
	public static final String AUTO_SUBMIT_TASK_PROPERTY = "Auto Submit";
	
	public static final String PRIV_GET_CASE_REPORTS = "Get Case Reports";
	
	public static final String PRIV_MANAGE_CASE_REPORTS = "Manage Case Reports";
	
	public static final String PRIV_GET_TRIGGERS = "Get Triggers";
	
	public static final String LAST_EXECUTION_TIME = "lastExecutionTime";
	
	public static final String SOURCE_CIEL_HL7_CODE = "CIEL";
	
	public static final String CONCEPT_MAPPING_SEPARATOR = "_";
	
	public static final String CIEL_MAPPING_PREFIX = CaseReportConstants.SOURCE_CIEL_HL7_CODE
	        + CaseReportConstants.CONCEPT_MAPPING_SEPARATOR;
	
	public static final String CIEL_CODE_VIRAL_LOAD = "856";
	
	public static final String CIEL_CODE_CD4_COUNT = "5497";
	
	public static final String CIEL_CODE_HIV_TEST = "1040";
	
	public static final String CIEL_CODE_WHO_STAGE = "5356";
	
	public static final String CIEL_CODE_ARV_MED_SET = "1085";
	
	public static final String CIEL_CODE_CURRENT_ARVS = "1088";
	
	public static final String CIEL_CODE_REASON_FOR_STOPPING_ARVS = "1252";
	
	public static final String CIEL_CODE_DATE_OF_LAST_VISIT = "164093";
	
}
