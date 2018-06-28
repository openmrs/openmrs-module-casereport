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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;

/**
 * This filter
 * 
 * @see <a href="http://www.w3.org/TR/cors/">CORS specification</a>
 */
@Controller
public class CrossOriginResourceSharingFilter implements Filter {
	
	protected Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}
	
	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
	    throws IOException, ServletException {
		
		if (log.isDebugEnabled()) {
			log.debug("In Cors filter...");
		}
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String gpValue = Context.getAdministrationService().getGlobalProperty(CaseReportWebConstants.GP_ENABLE_CORS);
		if (Boolean.valueOf(gpValue)) {
			if (log.isDebugEnabled()) {
				log.debug("Cors is enabled");
			}
			HttpServletResponse response = (HttpServletResponse) servletResponse;
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET, POST");
			response.addHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
			response.addHeader("Access-Control-Max-Age", "3600");
			
			//TODO add GP to disable this filter
			if (request.getMethod().equals("OPTIONS")) {
				
				if (log.isDebugEnabled()) {
					log.debug("Cors filter responding with Ok status");
				}
				response.setStatus(HttpServletResponse.SC_OK);
				
				return;
			}
		} else {
			if (log.isDebugEnabled()) {
				log.debug("Cors is disabled");
			}
		}
		
		// pass the request along the filter chain
		chain.doFilter(request, servletResponse);
	}
	
	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
	}
	
}
