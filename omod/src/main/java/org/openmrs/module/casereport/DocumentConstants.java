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

public final class DocumentConstants {
	
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy h a zzz");
	
	public static final String TYPE_ID_ROOT = "2.16.840.1.113883.1.3";
	
	public static final String TEXT_EXTENSION = "POCD_HD000040";
	
	public static final String TEMPLATE_ID_ROOT = "1.3.6.1.4.1.19376.1.5.3.1.1.18.1.2";
	
	public static final String TEXT_TITLE = "Public Health Case Report - HIV";
	
	public static final String LANGUAGE_CODE = "en-US";
	
	public static final String DOCUMENT_CODE = "55751-2";
	
	public static final String PATIENT_ID_ROOT = "1.3.6.1.4.1.21367.2010.1.2.300";
	
	public static final String SECTION_TEMPLATE_ID_ROOT1 = "2.16.840.1.113883.10.20.1.6";
	
	public static final String SECTION_TEMPLATE_ID_ROOT2 = "1.3.6.1.4.1.19376.1.5.3.1.3.23";
	
	public static final String TEXT_DOCUMENT_NAME = "Public health Case report";
	
	public static final String CODE_SYSTEM_ACTCODE = "2.16.840.1.113883.5.4";
	
	public static final String CODE_SYSTEM_LOINC = "2.16.840.1.113883.6.1";
	
	public static final String CODE_SYSTEM_NAME_LOINC = "LOINC";
	
	public static final String CODE_SYSTEM_SNOMEDCT = "2.16.840.1.113883.6.96";
	
	public static final String CODE_SYSTEM_NAME_SNOMEDCT = "SNOMED CT";
	
	public static final String CODE_SYSTEM_CIEL = "2.16.840.1.113883.3.7201";
	
	public static final String CODE_SYSTEM_NAME_CIEL = "CIEL";
	
	public static final String LOINC_CODE_CLINICAL_INFO = "55752-0";
	
	public static final String LOINC_CODE_MED_INFO = "55753-8";
	
	public static final String LOINC_CODE_DIAGNOSTICS = "30954-2";
	
	public static final String SNOMED_CODE_TRIGGER = "410658008";
	
	public static final String CIEL_CODE_HIV_TREAMENT = "162240";
	
	public static final String ACT_CODE_ASSERTION = "ASSERTION";
	
	public static final String ELEMENT_LIST = "list";
	
	public static final String ELEMENT_ITEM = "item";
	
	public static final String TEXT_CLINICAL_INFO = "Clinical Information";
	
	public static final String TEXT_MED_INFO = "Treatment Information";
	
	public static final String TEXT_TRIGGERS = "Trigger(s): ";
	
	public static final String TEXT_HIV_TREATMENT = "Human immunodeficiency virus treatment regimen";
	
	public static final String TEXT_ARVS = "Antiretrovirals: ";
	
	public static final String TEXT_COMMENTS = "Comments: ";
	
	public static final String TEXT_TRIGGER = "Trigger";
	
	public static final String TEXT_DIAGNOSTICS = "Relevant diagnostic tests and/or laboratory data";
	
	public static final String TEXT_WHO_STAGE = "Current HIV WHO Stage: ";
	
	public static final String TEXT_ARV_STOP_REASON = "Most recent ARV stop reason: ";
	
	public static final String TEXT_LAST_VISIT_DATE = "Date of last patient visit: ";
	
	public static final String TEXT_VIRAL_LOADS = "Viral Loads: ";
	
	public static final String TEXT_CD4_COUNTS = "CD4 Counts: ";
	
	public static final String TEXT_HIV_TESTS = "HIV Tests: ";
}
