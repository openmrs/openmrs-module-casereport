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

import java.io.File;

import javax.xml.bind.JAXBElement;
import javax.xml.transform.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.dcm4chee.xds2.common.XDSConstants;
import org.dcm4chee.xds2.infoset.ihe.ObjectFactory;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.dcm4chee.xds2.infoset.rim.RegistryError;
import org.dcm4chee.xds2.infoset.rim.RegistryResponseType;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;

/***
 * An instance of this class listens for event fired when a case report is submitted so that it can
 * generate and submit a CDA message to the HIE
 */
@Component
public class HealthInfoExchangeListener implements ApplicationListener<CaseReportSubmittedEvent> {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private ObjectFactory objectFactory = new ObjectFactory();
	
	@Autowired
	private WebServiceTemplate webServiceTemplate;
	
	@Autowired
	private WebServiceMessageCallback messageCallback;
	
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
			ProvideAndRegisterDocumentSetRequestType docRequest = new ProvideAndRegisterDocGenerator(form).generate();
			JAXBElement rootElement = objectFactory.createProvideAndRegisterDocumentSetRequest(docRequest);
			
			if (log.isDebugEnabled()) {
				log.debug("Saving Case report document to the file system.....");
			}
			
			File docFile = DocumentUtil.getSubmittedCaseReportFile(caseReport);
			Result out = new StringResult();
			webServiceTemplate.getMarshaller().marshal(rootElement, out);
			FileUtils.writeStringToFile(docFile, out.toString(), DocumentConstants.ENCODING);
			
			if (log.isDebugEnabled()) {
				log.debug("Case report document successfully saved to the file system");
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Sending Case report document.....");
			}
			
			String url = Context.getAdministrationService().getGlobalProperty(DocumentConstants.GP_OPENHIM_URL);
			Object response = webServiceTemplate.marshalSendAndReceive(url, rootElement, messageCallback);
			String lf = SystemUtils.LINE_SEPARATOR;
			RegistryResponseType regResp = ((JAXBElement<RegistryResponseType>) response).getValue();
			if (!XDSConstants.XDS_B_STATUS_SUCCESS.equals(regResp.getStatus())) {
				StringBuffer sb = new StringBuffer();
				if (regResp.getRegistryErrorList() != null && regResp.getRegistryErrorList().getRegistryError() != null) {
					for (RegistryError re : regResp.getRegistryErrorList().getRegistryError()) {
						sb.append("Severity: "
						        + (StringUtils.isNotBlank(re.getSeverity()) ? re.getSeverity().substring(
						            re.getSeverity().lastIndexOf(":") + 1) : "?") + ", Code: "
						        + (StringUtils.isNotBlank(re.getErrorCode()) ? re.getErrorCode() : "?") + ", Message: "
						        + (StringUtils.isNotBlank(re.getCodeContext()) ? re.getCodeContext() : "?") + lf);
					}
				}
				
				throw new APIException(sb.toString());
			}
			
			if (log.isDebugEnabled()) {
				log.debug("Case report document successfully sent");
			}
		}
		catch (Exception e) {
			
			log.warn("An error occurred while submitting a case report document to the HIE");
			
			APIException rethrow;
			if (e instanceof APIException) {
				rethrow = (APIException) e;
			} else {
				rethrow = new APIException(e);
			}
			
			throw rethrow;
		}
	}
}
