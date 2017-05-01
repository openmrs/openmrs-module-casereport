/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * 
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.casereport.rest.v1_0.controller;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.dcm4chee.xds2.infoset.ihe.ProvideAndRegisterDocumentSetRequestType;
import org.openmrs.api.APIException;
import org.openmrs.module.casereport.CaseReport;
import org.openmrs.module.casereport.CaseReportConstants;
import org.openmrs.module.casereport.DocumentUtil;
import org.openmrs.module.casereport.api.CaseReportService;
import org.openmrs.module.casereport.rest.CaseReportRestConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.response.ObjectNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Document;

@Controller
@RequestMapping("/rest/" + CaseReportRestConstants.REST_NAMESPACE)
public class CaseReportController extends MainResourceController {
	
	@Autowired
	private CaseReportService service;
	
	@Autowired
	private WebServiceTemplate webServiceTemplate;
	
	/**
	 * @see MainResourceController#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return CaseReportRestConstants.REST_NAMESPACE;
	}
	
	@RequestMapping(value = "/" + CaseReportConstants.MODULE_ID + "/{uuid}/document", method = RequestMethod.GET)
	@ResponseBody
	public Object getSubmittedCDAContents(@PathVariable("uuid") String uuid) {
		
		CaseReport cr = service.getCaseReportByUuid(uuid);
		if (cr == null) {
			throw new ObjectNotFoundException();
		}
		
		SimpleObject so = new SimpleObject();
		String pnrDoc = DocumentUtil.getSubmittedDocumentContents(cr);
		Exception e = null;
		if (StringUtils.isNotBlank(pnrDoc)) {
			try {
				Object o = webServiceTemplate.getUnmarshaller().unmarshal(new StringSource(pnrDoc));
				byte[] bytes = ((JAXBElement<ProvideAndRegisterDocumentSetRequestType>) o).getValue().getDocument().get(0)
				        .getValue();
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				Document cdaDoc = db.parse(new ByteArrayInputStream(bytes));
				StringResult result = new StringResult();
				transformer.transform(new DOMSource(cdaDoc), result);
				so.add("contents", result.toString());
				
				return so;
			}
			catch (Exception ex) {
				e = ex;
			}
		}
		
		throw new APIException("casereport.error.submittedCDAdoc.fail", e);
	}
}
