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

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.support.interceptor.ClientInterceptorAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpUrlConnection;

/**
 * An Interceptor that adds authentication credentials to the request using Basic Authentication
 * scheme, If the respective global properties for the username and password are not set, the
 * assumption is that they are not required therefore authentication credentials won't be added.
 */
public class AuthenticatingInterceptor extends ClientInterceptorAdapter {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see org.springframework.ws.client.support.interceptor.ClientInterceptor#handleRequest(MessageContext)
	 */
	@Override
	public boolean handleRequest(MessageContext messageContext) throws WebServiceClientException {
		
		TransportContext context = TransportContextHolder.getTransportContext();
		HttpUrlConnection connection = (HttpUrlConnection) context.getConnection();
		AdministrationService as = Context.getAdministrationService();
		String username = as.getGlobalProperty(DocumentConstants.GP_OPENHIM_CLIENT_ID);
		String password = as.getGlobalProperty(DocumentConstants.GP_OPENHIM_CLIENT_PASSWORD);
		
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			try {
				String token = Base64.encodeBase64String((username + ":" + password).getBytes());
				connection.addRequestHeader("Authorization", "Basic " + token);
			}
			catch (IOException e) {
				throw new WebServiceIOException("Failed to set the authorization header when sending the case report", e);
			}
		}
		
		return super.handleRequest(messageContext);
	}
}
