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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Document;

/**
 * Generates a CDA document
 */
public final class CdaDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
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
	 * @should generate a CDA document
	 */
	public String generate(CaseReportForm reportForm) throws Exception {
		//cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("triggerList"), triggerList);
		//cdaMessage = StringUtils.replace(cdaMessage, getPlaceHolder("dateSubmitted"), DATE_FORMATTER.format(new Date()));
		//String oid1 = "2.25." + new BigInteger(reportForm.getReportUuid().getBytes());
		//String oid2 = "2.25." + new BigInteger(UUID.randomUUID().toString().getBytes())
		ClinicalDocument cdaDocument = new ClinicalDocument();
		cdaDocument.setRealmCode(new SET<CS<BindingRealm>>(new CS<BindingRealm>(
		        BindingRealm.UniversalRealmOrContextUsedInEveryInstance)));
		cdaDocument.setTypeId(DocumentConstants.TYPE_ID_ROOT, DocumentConstants.TEXT_EXTENSION);
		cdaDocument.setTemplateId(Arrays.asList(new II(DocumentConstants.TEMPLATE_ID_ROOT)));
		cdaDocument.setId(reportForm.getReportUuid());
		cdaDocument.setCode(CdaUtil.createLoincCE(DocumentConstants.DOCUMENT_CODE, DocumentConstants.TEXT_DOCUMENT_NAME));
		cdaDocument.setTitle(DocumentConstants.TEXT_TITLE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(reportForm.getReportDate());
		cdaDocument.setEffectiveTime(calendar);
		cdaDocument.setConfidentialityCode(x_BasicConfidentialityKind.Normal);
		cdaDocument.setLanguageCode(DocumentConstants.LANGUAGE_CODE);
		cdaDocument.getRecordTarget().add(CdaUtil.createRecordTarget(reportForm));
		cdaDocument.getAuthor().add(CdaUtil.createAuthor(reportForm));
		cdaDocument.setCustodian(CdaUtil.createCustodian(reportForm));
		cdaDocument.setComponent(CdaUtil.createRootComponent(reportForm));
		
		XmlIts1Formatter fmtr = new XmlIts1Formatter();
		//This instructs the XML ITS1 Formatter we want to use CDA datatypes
		fmtr.getGraphAides().add(new DatatypeFormatter(R1FormatterCompatibilityMode.ClinicalDocumentArchitecture));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fmtr.graph(out, cdaDocument);
		
		//Use a Transformer for output the cda in a pretty format
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream inputStream = new ByteArrayInputStream(out.toByteArray());
		Document doc = builder.parse(inputStream);
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		
		StreamResult result = new StreamResult(new ByteArrayOutputStream());
		transformer.transform(new DOMSource(doc), result);
		System.out.println(result.getOutputStream().toString());
		return result.getOutputStream().toString();
	}
}
