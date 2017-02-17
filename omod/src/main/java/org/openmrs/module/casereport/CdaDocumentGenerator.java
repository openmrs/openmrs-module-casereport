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

import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.marc.everest.datatypes.II;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.vocabulary.BindingRealm;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_BasicConfidentialityKind;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportService;

/**
 * Generates a CDA document
 */
public final class CdaDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private CaseReportForm form;
	
	/**
	 * @param form the CaseReportForm from which to generate a ClinicalDocument
	 */
	public CdaDocumentGenerator(CaseReportForm form) {
		this.form = form;
	}
	
	/**
	 * Generates and returns a ClinicalDocument object from its backing CaseReportForm object
	 * 
	 * @return ClinicalDocument object
	 * @should generate a ClinicalDocument object
	 */
	public ClinicalDocument generate() throws Exception {
		if (log.isDebugEnabled()) {
			CaseReportService crs = Context.getService(CaseReportService.class);
			CaseReport cr = crs.getCaseReportByUuid(form.getReportUuid());
			log.debug("Generating ClinicalDocument for: " + cr);
		}
		
		ClinicalDocument cdaDocument = new ClinicalDocument();
		cdaDocument.setRealmCode(new SET(new CS(BindingRealm.UniversalRealmOrContextUsedInEveryInstance)));
		cdaDocument.setTypeId(DocumentConstants.TYPE_ID_ROOT, DocumentConstants.TEXT_EXTENSION);
		cdaDocument.setTemplateId(Arrays.asList(new II(DocumentConstants.TEMPLATE_ID_ROOT)));
		cdaDocument.setId(form.getReportUuid());
		cdaDocument.setCode(CdaGeneratorUtil.createLoincCE(DocumentConstants.LOINC_CODE_CR,
		    DocumentConstants.TEXT_DOCUMENT_NAME));
		cdaDocument.setTitle(DocumentConstants.TEXT_TITLE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(form.getReportDate());
		cdaDocument.setEffectiveTime(calendar);
		cdaDocument.setConfidentialityCode(x_BasicConfidentialityKind.Normal);
		cdaDocument.setLanguageCode(DocumentConstants.LANGUAGE_CODE);
		cdaDocument.getRecordTarget().add(CdaGeneratorUtil.createRecordTarget(form));
		cdaDocument.getAuthor().add(CdaGeneratorUtil.createAuthor(form));
		cdaDocument.setCustodian(CdaGeneratorUtil.createCustodian(form));
		cdaDocument.setComponent(CdaGeneratorUtil.createRootComponent(form));
		
		return cdaDocument;
	}
}
