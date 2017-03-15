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

import static org.openmrs.module.casereport.CaseReportConstants.MODULE_ID;

public final class DocumentConstants {
	
	public static final String GP_PREFIX = MODULE_ID + ".";
	
	public static final String GP_OPENHIM_URL = MODULE_ID + ".openHIMUrl";
	
	public static final String GP_OPENHIM_CLIENT_ID = MODULE_ID + ".openHIMClientId";
	
	public static final String GP_OPENHIM_CLIENT_PASSWORD = MODULE_ID + ".openHIMClientPassword";
	
	public static final String OID_PREFIX = "2.25.";
	
	public static final String CONFIDENTIALITY_N = "N";
	
	public static final String CONFIDENTIALITY_R = "R";
	
	public static final String CONFIDENTIALITY_V = "V";
	
	public static final String PID_3_PATTERN = "PID-3|%s";
	
	public static final String PID_5_PATTERN = "PID-5|%s^%s^^^";
	
	public static final String PID_7_PATTERN = "PID-7|%s";
	
	public static final String PID_8_PATTERN = "PID-8|%s";
	
	public static final String XDS_SYMBOLIC_LINKS_PREFIX = "urn:oasis:names:tc:ebxml-regrep:ObjectType:RegistryObject:";
	
	public static final String XDS_REG_PACKAGE = "RegistryPackage";
	
	public static final String XDS_CLASSIFICATION = "Classification";
	
	public static final String XDS_ASSOCIATION = "Association";
	
	public static final String XDS_DOC_ID = "Document1";
	
	public static final String XDS_SUBSET_ID = "SubSet1";
	
	public static final String XDS_CLASSIFICATION_ID = "Class1";
	
	public static final String XDS_ASSOCIATION_ID = "Assoc1";
	
	public static final String XDS_SLOT_CODING_SCHEME = "codingScheme";
	
	public static final String TYPE_ID_ROOT = "2.16.840.1.113883.1.3";
	
	public static final String TEXT_EXTENSION = "POCD_HD000040";
	
	//This template id ensures this document is processed by the 
	//MedicalDocumentsDocumentProcessor in the shr-cdahandler module
	public static final String TEMPLATE_IHE_MED_DOC = "1.3.6.1.4.1.19376.1.5.3.1.1.1";
	
	public static final String TEMPLATE_HL7_GENERAL_HEADER = "2.16.840.1.113883.10.20.3";
	
	//This template id ensures this obs entries are processed by the
	//CodedResultsSectionProcessor in the shr-cdahandler module
	public static final String SECTION_TEMPLATE_ID_ROOT1 = "1.3.6.1.4.1.19376.1.5.3.1.3.27";
	
	//This template id ensures this obs entries are processed by the
	//SimpleObservationEntryProcessor in the shr-cdahandler module
	public static final String OBS_TEMPLATE_ID_ROOT = "1.3.6.1.4.1.19376.1.5.3.1.4.13";
	
	public static final String TEXT_TITLE = "Public Health Case Report - HIV";
	
	public static final String LANGUAGE_CODE = "en-US";
	
	public static final String TEXT_DOCUMENT_NAME = "Public health Case report";
	
	public static final String CODE_SYSTEM_LOINC = "2.16.840.1.113883.6.1";
	
	public static final String CODE_SYSTEM_NAME_LOINC = "LOINC";
	
	public static final String CODE_SYSTEM_SNOMEDCT = "2.16.840.1.113883.6.96";
	
	public static final String CODE_SYSTEM_NAME_SNOMEDCT = "SNOMED CT";
	
	public static final String CODE_SYSTEM_CIEL = "2.16.840.1.113883.3.7201";
	
	public static final String CODE_SYSTEM_CONFIDENTIALITY = "2.16.840.1.113883.5.25";
	
	public static final String CODE_SYSTEM_FORMAT_CODE_SET = "1.3.6.1.4.1.19376.1.2.3";
	
	public static final String CODE_SYSTEM_NAME_CIEL = "CIEL";
	
	public static final String LOINC_CODE_CR = "55751-2";
	
	public static final String LOINC_CODE_CAUSE_OF_DEATH = "79378-6";
	
	//TODO we need to assign this a proper code
	public static final String LOINC_CODE_TYPE_CODE_CR = "*";
	
	public static final String LOINC_CODE_DIAGNOSTICS = "30954-2";
	
	public static final String SNOMED_CODE_TRIGGER = "410658008";
	
	public static final String CIEL_CODE_HEALTH_STATUS = "159640";
	
	public static final String CIEL_CODE_DEAD = "160432";
	
	public static final String GP_ID_FORMAT = GP_PREFIX + "idFormat";
	
	public static final String GP_ORG_ID = GP_PREFIX + "organisationIdentifier";
	
	public static final String GP_CONFIDENTIALITY_CODE = GP_PREFIX + "confidentialityCode";
	
	public static final String GP_FACILITY_TYPE_CODE = GP_PREFIX + "healthCareFacilityTypeCode";
	
	public static final String GP_FACILITY_TYPE_CODING_SCHEME = GP_PREFIX + "healthCareFacilityTypeCodingSystem";
	
	public static final String GP_FACILITY_TYPE_NAME = GP_PREFIX + "healthCareFacilityTypeDisplayName";
	
	public static final String GP_PRACTICE_CODE = GP_PREFIX + "practiceSettingCode";
	
	public static final String GP_PRACTICE_CODING_SCHEME = GP_PREFIX + "practiceSettingCodingSystem";
	
	public static final String GP_PRACTICE_NAME = GP_PREFIX + "practiceSettingDisplayName";
	
	//This code is actually for a medical summaries, We only used it   
	//so that the CDA handler can be assigned to it as the handler
	public static final String IHE_PCC_CODE_FORMAT = "urn:ihe:pcc:xds-ms:2007";
	
	public static final String ELEMENT_LIST = "list";
	
	public static final String ELEMENT_ITEM = "item";
	
	public static final String TEXT_HIV_TREATMENT = "Current antiretroviral drugs used for treatment";
	
	public static final String TEXT_TRIGGER = "Trigger";
	
	public static final String TEXT_DIAGNOSTICS = "Relevant diagnostic tests and/or laboratory data";
	
	public static final String TEXT_CODE_HEALTH_STATUS = "Patient health status";
	
	public static final String TEXT_DEAD = "Dead";
	
	public static final String TEXT_CAUSE_OF_DEATH = "Cause of death";
	
	public static final String LABEL_ARVS = "Antiretrovirals: ";
	
	public static final String LABEL_COMMENTS = "Comments: ";
	
	public static final String LABEL_TRIGGERS = "Trigger(s): ";
	
	public static final String LABEL_DEATH_INFO = "Death Information: ";
	
	public static final String LABEL_DATE_OF_DEATH = "Date of death: ";
	
	public static final String LABEL_OTHER_DIAGNOSTICS = "Other Diagnostic Data: ";
	
	public static final String LABEL_WHO_STAGE = "Current HIV WHO Stage: ";
	
	public static final String LABEL_ARV_STOP_REASON = "Most recent ARV stop reason: ";
	
	public static final String LABEL_LAST_VISIT_DATE = "Date of last patient visit: ";
	
	public static final String LABEL_CD4_RECENT_COUNT = "Most Recent CD4 Count: ";
	
	public static final String LABEL_HIV_RECENT_TEST = "Most Recent HIV Test: ";
	
	public static final String LABEL_RECENT_VIRAL_LOAD = "Most Recent Viral Load: ";
	
	public static final String TEXT_CURRENT_WHO_STAGE = "Current WHO HIV Stage";
	
	public static final String TEXT_REASON_ARVS_STOPPED = "Reason antiretrovirals stopped";
	
	public static final String TEXT_DATE_OF_LAST_VISIT = "Date of last patient visit";
	
	public static final String TEXT_CD4_COUNT = "CD4 count";
	
	public static final String TEXT_HIV_TEST = "HIV rapid test 1, qualitative";
	
	public static final String TEXT_VIRAL_LOAD = "HIV Viral Load";
	
	public static final String TEXT_FORMAT = "XDS Medical Summaries";
	
	public static final String TEXT_DOC_PATIENT_ID = "XDSDocumentEntry.patientId";
	
	public static final String TEXT_DOC_UNIQUE_ID = "XDSDocumentEntry.uniqueId";
	
	public static final String TEXT_SUBSET_PATIENT_ID = "XDSSubmissionSet.patientId";
	
	public static final String TEXT_SUBSET_UNIQUE_ID = "XDSSubmissionSet.uniqueId";
	
	public static final String TEXT_SUBSET_SOURCE_ID = "XDSSubmissionSet.sourceId";
	
	public static final String TEXT_ORIGINAL = "Original";
}
