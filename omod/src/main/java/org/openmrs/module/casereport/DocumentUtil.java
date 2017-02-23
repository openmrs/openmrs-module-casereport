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

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.marc.everest.datatypes.NullFlavor;
import org.marc.everest.datatypes.TS;
import org.openmrs.api.context.Context;

/**
 * Contains utility methods that are used by document generators
 */
public class DocumentUtil {
	
	private static final HashMap<String, String> codeLocalizedStringMap = new HashMap();
	
	static {
		codeLocalizedStringMap.put(DocumentConstants.CONFIDENTIALITY_N, "Normal");
		codeLocalizedStringMap.put(DocumentConstants.CONFIDENTIALITY_R, "Restricted");
		codeLocalizedStringMap.put(DocumentConstants.CONFIDENTIALITY_V, "Very restricted");
	}
	
	//TODO Move all GPs to a Config class with a cache, it should be implemented as GlobalPropertyListener 
	// so that it can refresh its cache whenever the property values change or are deleted
	
	/**
	 * Gets the code and name mapping for confidentiality levels
	 * 
	 * @return A map of code and names
	 */
	public static HashMap<String, String> getConfidentialityCodeNameMap() {
		return codeLocalizedStringMap;
	}
	
	/**
	 * Creates a TS object for the specified date
	 * 
	 * @param date the date object
	 * @return the TS object
	 */
	public static TS createTS(Date date) {
		if (date == null) {
			TS retVal = new TS();
			retVal.setNullFlavor(NullFlavor.NoInformation);
			return retVal;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return new TS(calendar);
	}
	
	/**
	 * Gets the value of the DocumentConstants.GP_ORG_ID global property
	 *
	 * @return the organisation's OID
	 */
	public static String getOrganisationOID() {
		return getGlobalProperty(DocumentConstants.GP_ORG_ID);
	}
	
	/**
	 * Gets the value of the DocumentConstants.GP_CONFIDENTIALITY_CODE global property
	 *
	 * @return the confidentiality code
	 */
	public static String getConfidentialityCode() {
		return getGlobalProperty(DocumentConstants.GP_CONFIDENTIALITY_CODE);
	}
	
	/**
	 * Convenience method that gets the value of the specified global property name
	 * 
	 * @param propertyName the global property name
	 * @return the global property value
	 */
	private static String getGlobalProperty(String propertyName) {
		return Context.getAdministrationService().getGlobalProperty(propertyName);
	}
}
