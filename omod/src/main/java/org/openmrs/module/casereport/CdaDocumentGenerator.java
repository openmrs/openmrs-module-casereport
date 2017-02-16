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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.formatters.xml.datatypes.r1.DatatypeFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.R1FormatterCompatibilityMode;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.vocabulary.BindingRealm;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_BasicConfidentialityKind;

/**
 * Generates a CDA document
 */
public final class CdaDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
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
	 * @should generate a CDA document
	 */
	public byte[] generate(CaseReportForm reportForm) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Generating case report CDA document...");
		}
		ClinicalDocument cdaDocument = new ClinicalDocument();
		cdaDocument.setRealmCode(new SET(new CS(BindingRealm.UniversalRealmOrContextUsedInEveryInstance)));
		cdaDocument.setTypeId(DocumentConstants.TYPE_ID_ROOT, DocumentConstants.TEXT_EXTENSION);
		cdaDocument.setTemplateId(Arrays.asList(new II(DocumentConstants.TEMPLATE_ID_ROOT)));
		cdaDocument.setId(reportForm.getReportUuid());
		cdaDocument.setCode(CdaGeneratorUtil.createLoincCE(DocumentConstants.LOINC_CODE_CR,
		    DocumentConstants.TEXT_DOCUMENT_NAME));
		cdaDocument.setTitle(DocumentConstants.TEXT_TITLE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(reportForm.getReportDate());
		cdaDocument.setEffectiveTime(calendar);
		cdaDocument.setConfidentialityCode(x_BasicConfidentialityKind.Normal);
		cdaDocument.setLanguageCode(DocumentConstants.LANGUAGE_CODE);
		cdaDocument.getRecordTarget().add(CdaGeneratorUtil.createRecordTarget(reportForm));
		cdaDocument.getAuthor().add(CdaGeneratorUtil.createAuthor(reportForm));
		cdaDocument.setCustodian(CdaGeneratorUtil.createCustodian(reportForm));
		cdaDocument.setComponent(CdaGeneratorUtil.createRootComponent(reportForm));
		
		XmlIts1Formatter fmtr = new XmlIts1Formatter();
		//This instructs the XML ITS1 Formatter we want to use CDA datatypes
		fmtr.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.ClinicalDocumentArchitecture));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fmtr.graph(out, cdaDocument);
		return out.toByteArray();
	}
}
