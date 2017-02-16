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

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.GlobalPropertyListener;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.addressing.client.ActionCallback;
import org.springframework.ws.soap.addressing.core.EndpointReference;

/***
 * An instance of this class listens for event fired when a case report is submitted so that it can
 * generate and submit a CDA message to the HIE
 */
public class HealthInfoExchangeListener implements ApplicationListener<CaseReportSubmittedEvent>, GlobalPropertyListener {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	private WebServiceTemplate webServiceTemplate;
	
	private static WebServiceMessageCallback messageCallback;
	
	private static String documentRepositoryUrl;
	
	private WebServiceMessageCallback getActionCallBack() throws URISyntaxException {
		if (messageCallback == null) {
			ActionCallback callback = new ActionCallback(WebConstants.PROV_REG_DOC_ACTION);
			callback.setReplyTo(new EndpointReference(new URI(XDSConstants.WS_ADDRESSING_ANONYMOUS)));
			messageCallback = callback;
		}
		return messageCallback;
	}
	
	private static String getRepositoryUrl() {
		if (documentRepositoryUrl == null) {
			documentRepositoryUrl = Context.getAdministrationService().getGlobalProperty(WebConstants.GP_CR_DEST_URL);
		}
		return documentRepositoryUrl;
	}
	
	/**
	 * @see ApplicationListener#onApplicationEvent(ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(CaseReportSubmittedEvent event) {
		try {
			CaseReport caseReport = (CaseReport) event.getSource();
			CaseReportForm form = new ObjectMapper().readValue(caseReport.getReportForm(), CaseReportForm.class);
			form.setReportUuid(caseReport.getUuid());
			form.setReportDate(caseReport.getDateCreated());
			ProvideAndRegisterDocumentSetRequestType docRequest = ProvideAndRegisterDocumentSetRequestGenerator
			        .getInstance().generate(form);
			QName qName = new QName(DocumentConstants.XDS_TX_NAMESPACE_URI, DocumentConstants.XDS_ROOT_ELEMENT);
			JAXBElement rootElement = new JAXBElement(qName, docRequest.getClass(), docRequest);
			if (log.isDebugEnabled()) {
				log.debug("Sending Case report document.....");
			}
			
			Object response = webServiceTemplate.marshalSendAndReceive(getRepositoryUrl(), rootElement, getActionCallBack());
			if (log.isDebugEnabled()) {
				log.debug("Case report document successfully sent!");
				log.debug(response);
			}
		}
		catch (Exception e) {
			throw new APIException("An error occurred while submitting the cda message for the case report", e);
		}
	}
	
	/**
	 * @see GlobalPropertyListener#globalPropertyChanged(GlobalProperty)
	 */
	@Override
	public void globalPropertyChanged(GlobalProperty globalProperty) {
		documentRepositoryUrl = null;
	}
	
	/**
	 * @see GlobalPropertyListener#globalPropertyDeleted(String)
	 */
	@Override
	public void globalPropertyDeleted(String s) {
		documentRepositoryUrl = null;
	}
	
	/**
	 * @see GlobalPropertyListener#supportsPropertyName(String)
	 */
	@Override
	public boolean supportsPropertyName(String propertyName) {
		return WebConstants.GP_CR_DEST_URL.equals(propertyName);
	}
}
