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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.DatedUuidAndValue;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Generates a CDA document
 */
public final class CdaDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private static final String RESOURCE_PATH = "org/openmrs/module/casereport/templates/";
	
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
	
	private static CdaDocumentGenerator instance;
	
	private CdaDocumentGenerator() {
	}
	
	public synchronized static CdaDocumentGenerator getInstance() {
		if (instance == null) {
			instance = new CdaDocumentGenerator();
		}
		return instance;
	}
	
	/**
	 * Generates and returns a CDA document for the specified report form
	 * 
	 * @param reportForm
	 * @return the generated CDA document
	 */
	public String generate(CaseReportForm reportForm) throws IOException {
		InputStream in = OpenmrsClassLoader.getInstance().getResourceAsStream(RESOURCE_PATH + "template.xml");
		String cdaMessage = IOUtils.toString(in);
		CaseReport cr = Context.getService(CaseReportService.class).getCaseReportByUuid(reportForm.getReportUuid());
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("birthdate"),
		    DATE_FORMATTER.format(cr.getPatient().getBirthdate()));
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("assigningAuthorityId"),
		    reportForm.getAssigningAuthorityId());
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("assigningAuthorityName"),
		    reportForm.getAssigningAuthorityId());
		String triggerList = "";
		int index = 0;
		for (DatedUuidAndValue trigger : reportForm.getTriggers()) {
			if (index > 0) {
				triggerList += "\n\t\t\t\t\t\t";
			}
			triggerList += ("<item>" + trigger.getValue().toString() + "</item>");
			index++;
		}
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("triggerList"), triggerList);
		
		//in = OpenmrsClassLoader.getInstance().getResourceAsStream(RESOURCE_PATH + "soap-envelope-template.xml");
		//String soapEnvelope = IOUtils.toString(in);
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("dateSubmitted"), DATE_FORMATTER.format(new Date()));
		String oid1 = "2.25." + new BigInteger(reportForm.getReportUuid().getBytes());
		String oid2 = "2.25." + new BigInteger(UUID.randomUUID().toString().getBytes());
        cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("docUniqueId"), oid1);
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("subUniqueId"), oid2);
		final String uuidPlaceHolder = getPlaceHolder("randomUUID");
		while (cdaMessage.indexOf(uuidPlaceHolder) > -1) {
			cdaMessage = StringUtils.replaceOnce(cdaMessage, uuidPlaceHolder, UUID.randomUUID().toString());
		}
		
		//String document = StringUtils.replace(cdaMessage, getPlaceHolder("cdaMessage"), cdaMessage);
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("reportUuid"), reportForm.getReportUuid());
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("reportDate"),
		    DATE_FORMATTER.format(reportForm.getReportDate()));
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("sourcePatId"), reportForm.getPatientIdentifier()
		        .getValue().toString());
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("givenName"), reportForm.getGivenName());
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("familyName"), reportForm.getFamilyName());
		cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("gender"), reportForm.getGender());
		
		return cdaMessage;
	}
	
	private String getPlaceHolder(String name) {
		return "#{" + name + "}";
	}
}
