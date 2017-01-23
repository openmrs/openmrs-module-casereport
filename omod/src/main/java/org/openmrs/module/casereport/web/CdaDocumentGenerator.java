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
import org.marc.everest.datatypes.generic.CE;
import org.marc.everest.datatypes.generic.CS;
import org.marc.everest.datatypes.generic.SET;
import org.marc.everest.formatters.xml.datatypes.r1.DatatypeFormatter;
import org.marc.everest.formatters.xml.datatypes.r1.R1FormatterCompatibilityMode;
import org.marc.everest.formatters.xml.its1.XmlIts1Formatter;
import org.marc.everest.rmim.uv.cdar2.pocd_mt000040uv.ClinicalDocument;
import org.marc.everest.rmim.uv.cdar2.vocabulary.BindingRealm;
import org.marc.everest.rmim.uv.cdar2.vocabulary.x_BasicConfidentialityKind;
import org.openmrs.module.casereport.CaseReportForm;
import org.openmrs.module.casereport.CdaUtil;
import org.w3c.dom.Document;

/**
 * Generates a CDA document
 */
public final class CdaDocumentGenerator {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss.SSSZ");
	
	public static final String TYPE_ID_ROOT = "2.16.840.1.113883.1.3";
	
	public static final String EXTENSION = "POCD_HD000040";
	
	public static final String TEMPLATE_ID_ROOT = "1.3.6.1.4.1.19376.1.5.3.1.1.18.1.2";
	
	public static final String TITLE = "Case Based Report for HIV";
	
	//Should this be system locale or a constant?
	public static final String LANGUAGE_CODE = "en-US";
	
	public static final String CODE_SYSTEM_LOINC = "2.16.840.1.113883.6.1";
	
	public static final String DOCUMENT_CODE = "55751-2";
	
	public static final String PATIENT_ID_ROOT = "1.3.6.1.4.1.21367.2010.1.2.300";
	
	public static final String SECTION_TEMPLATE_ID_ROOT1 = "2.16.840.1.113883.10.20.1.6";
	
	public static final String SECTION_TEMPLATE_ID_ROOT2 = "1.3.6.1.4.1.19376.1.5.3.1.3.23";
	
	public static final String CODE_SYSTEM_NAME_LOINC = "LOINC";
	
	public static final String LOINC_DOCUMENT_NAME = "Public health Case report";
	
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
		cdaDocument.setTypeId(TYPE_ID_ROOT, EXTENSION);
		cdaDocument.setTemplateId(Arrays.asList(new II(TEMPLATE_ID_ROOT)));
		cdaDocument.setId(reportForm.getReportUuid());
		cdaDocument.setCode(new CE<String>(DOCUMENT_CODE, CODE_SYSTEM_LOINC, CODE_SYSTEM_NAME_LOINC, null,
		        LOINC_DOCUMENT_NAME, null));
		cdaDocument.setTitle(TITLE);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(reportForm.getReportDate());
		cdaDocument.setEffectiveTime(calendar);
		cdaDocument.setConfidentialityCode(x_BasicConfidentialityKind.Normal);
		cdaDocument.setLanguageCode(LANGUAGE_CODE);
		cdaDocument.getRecordTarget().add(CdaUtil.createRecordTarget(reportForm));
		cdaDocument.getAuthor().add(CdaUtil.createAuthor(reportForm));
		cdaDocument.setComponent(CdaUtil.createComponent(reportForm));
		
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
		
		return result.getOutputStream().toString();
	}
}
