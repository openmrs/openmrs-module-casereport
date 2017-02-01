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

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.APIException;
import org.openmrs.module.casereport.api.CaseReportSubmittedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/***
 * An instance of this class listens for event fired when a case report is submitted so that it can
 * generate and submit a CDA message to the HIE
 */
@Component
public class HealthInfoExchangeListener implements ApplicationListener<CaseReportSubmittedEvent> {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private static final String OpenHIM_URL = "https://openshr-preprod.jembi.org:5000/openmrs/ms/xdsrepository";
	
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
			
			String document = CdaDocumentGenerator.getInstance().generate(form);
			
			HttpURLConnection httpConnection = (HttpURLConnection) new URL(OpenHIM_URL).openConnection();
			httpConnection.setRequestMethod("POST");
			httpConnection.setDoOutput(true);
			httpConnection.setRequestProperty("Accept", "application/soap+xml");
			httpConnection.setConnectTimeout(60000);
			httpConnection.setUseCaches(false);
			httpConnection.setRequestProperty("Content-length", String.valueOf(document.length()));
			httpConnection
			        .setRequestProperty(
			            "Content-Type",
			            "multipart/related; boundary=MIMEBoundaryurn_uuid_DCD262C64C22DB97351256303951323; type=\"application/xop+xml\"; start=\"<0.urn:uuid:DCD262C64C22DB97351256303951324@apache.org>\"; start-info=\"application/soap+xml\";");
			OutputStream writer = httpConnection.getOutputStream();
			writer.write(document.getBytes());
			writer.flush();
			writer.close();
			
			int statusCode = httpConnection.getResponseCode();
			log.info("STATUS:" + statusCode);
			log.info("MESSAGE:" + httpConnection.getResponseMessage());
			log.warn("RESPONSE:" + IOUtils.toString(httpConnection.getInputStream()));
			if (statusCode != 200) {
				log.warn("Http Error:" + statusCode);
				if (httpConnection.getErrorStream() != null) {
					log.warn("Error response from server:" + IOUtils.toString(httpConnection.getErrorStream()));
				}
				throw new APIException();
			} else {
				String response = IOUtils.toString(httpConnection.getInputStream());
				if (response.indexOf("Error") > -1) {
					log.warn("Error message from server:" + response);
					throw new APIException(response);
				}
			}
		}
		catch (Exception e) {
			throw new APIException("An error occurred while submitting the cda message for the case report", e);
		}
	}
}
