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
import java.net.ServerSocket;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.openmrs.GlobalProperty;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsClassLoader;
import org.w3c.dom.Document;

import com.github.tomakehurst.wiremock.client.WireMock;

public class TestUtils {
	
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	
	private static final String CONTENT_TYPE = "application/soap+xml";
	
	private static final String SUCCESS_RESPONSE_XML = "success_response.xml";
	
	private static final String FAILURE_RESPONSE_XML = "failure_response.xml";
	
	public static String getResponse(boolean success) throws IOException {
		String filename = success ? SUCCESS_RESPONSE_XML : FAILURE_RESPONSE_XML;
		return IOUtils.toString(OpenmrsClassLoader.getInstance().getResourceAsStream(filename));
	}
	
	public static void createPostStub(boolean withSuccessResponse) throws IOException {
		
		final int sc = withSuccessResponse ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR;
		
		WireMock.stubFor(WireMock
		        .post(WireMock.urlEqualTo("/xdsrepository"))
		        .withHeader("Accept", WireMock.containing(CONTENT_TYPE))
		        .withBasicAuth("fake user", "fake password")
		        .willReturn(
		            WireMock.aResponse().withStatus(sc).withHeader("Content-Type", CONTENT_TYPE)
		                    .withBody(getResponse(withSuccessResponse))));
	}
	
	public static String getElement(Document doc, String path) throws XPathExpressionException {
		return xpath.compile(path).evaluate(doc);
	}
	
	public static int getCount(Document doc, String path) throws XPathExpressionException {
		return Integer.valueOf(xpath.compile("count(" + path + ")").evaluate(doc));
	}
	
	public static boolean elementExists(Document doc, String path) throws XPathExpressionException {
		return getCount(doc, path) > 0;
	}
	
	public static boolean elementContainsText(Document doc, String path, String search) throws XPathExpressionException {
		return getElement(doc, path).contains(search);
	}
	
	public static boolean attributeContainsText(Document doc, String path, String attribute, String search)
	    throws XPathExpressionException {
		return getAttribute(doc, path, attribute).contains(search);
	}
	
	public static String getAttribute(Document doc, String path, String attribute) throws XPathExpressionException {
		return xpath.compile(path + "/@" + attribute).evaluate(doc);
	}
	
	public static boolean elementHasText(Document doc, String path) throws XPathExpressionException {
		return StringUtils.isNotBlank(getElement(doc, path));
	}
	
	public static boolean attributeHasText(Document doc, String path, String attribute) throws XPathExpressionException {
		return StringUtils.isNotBlank(getAttribute(doc, path, attribute));
	}
	
	public static Integer getAvailablePort() {
		for (int i = 1024; i < 49151; i++) {
			try {
				new ServerSocket(i).close();
				return i;
			}
			catch (IOException e) {
				//Port is not available for use
			}
		}
		
		//Really! No port is available?
		throw new APIException("No available port found");
	}
	
	public static void setOpenHIMPort(Integer port) {
		AdministrationService as = Context.getAdministrationService();
		GlobalProperty gp = as.getGlobalPropertyObject(DocumentConstants.GP_OPENHIM_URL);
		gp.setPropertyValue(StringUtils.replaceOnce(gp.getPropertyValue(), "{{PORT}}", port.toString()));
		as.saveGlobalProperty(gp);
	}
	
}
