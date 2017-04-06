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

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.marc.everest.datatypes.NullFlavor;
import org.marc.everest.datatypes.TS;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.context.Context;

/**
 * Contains utility methods that are used by document generators
 */
public class DocumentUtil {
	
	//The count of decimal numbers that can be represented with 128 bits, i.e. 2 power 128
	private static final BigInteger DECIMAL_REP_COUNT = BigInteger.ONE.shiftLeft(128);
	
	//Formatter used to print dates in text sections that are human readable
	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy h a zzz");
	
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
	 * @see #createTS(Date, Integer)
	 */
	public static TS createTS(Date date) {
		return createTS(date, null);
	}
	
	/**
	 * Creates a TS object for the specified date with the specified precision.
	 *
	 * @param date the date object
	 * @param precision the date precision level as specified in the TS class
	 * @return the TS object
	 */
	public static TS createTS(Date date, Integer precision) {
		if (date == null) {
			TS retVal = new TS();
			retVal.setNullFlavor(NullFlavor.NoInformation);
			return retVal;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return (precision == null) ? new TS(calendar) : new TS(calendar, precision);
	}
	
	/**
	 * Reformats the specified serialized date string to a human readable format, assumes the string
	 * had previously been formatted with CaseReportConstants.DATE_FORMATTER
	 * 
	 * @param dateString the date string to format
	 * @return a human readable date
	 * @throws ParseException
	 */
	public static String getDisplayDate(String dateString) throws ParseException {
		Date date = CaseReportConstants.DATE_FORMATTER.parse(dateString);
		return DATE_FORMATTER.format(date);
	}
	
	/**
	 * Gets the value of the organisation oid global property
	 *
	 * @return the organisation's OID
	 */
	public static String getOrganisationOID() {
		return getGlobalProperty(DocumentConstants.GP_ORG_ID);
	}
	
	/**
	 * Gets the value of the confidentiality code global property
	 *
	 * @return the confidentiality code
	 */
	public static String getConfidentialityCode() {
		return getGlobalProperty(DocumentConstants.GP_CONFIDENTIALITY_CODE);
	}
	
	/**
	 * Gets the value of the id format format global property
	 * 
	 * @return the id format
	 */
	public static String getIdFormat() {
		return getGlobalProperty(DocumentConstants.GP_ID_FORMAT);
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
	
	/**
	 * Gets the PersonName of the provider with the specified identifier
	 * 
	 * @param providerIdentifier the provider identifier to match against
	 * @return the PersonName of the provider
	 */
	public static PersonName getPersonNameForProvider(String providerIdentifier) {
		Provider provider = Context.getProviderService().getProviderByIdentifier(providerIdentifier);
		PersonName personName = provider.getPerson().getPersonName();
		if (personName == null) {
			String[] names = StringUtils.split(provider.getName().trim());
			String firstName = names[0];
			String middleName = null;
			String familyName;
			if (names.length > 2) {
				middleName = names[1];
				familyName = names[2];
			} else {
				familyName = names[1];
			}
			personName = new PersonName(firstName, middleName, familyName);
		}
		return personName;
	}
	
	/**
	 * Converts the specified uuid to its decimal representation
	 * 
	 * @param uuid the uuid to convert
	 * @return a string representation of the decimal number
	 */
	public static String convertToDecimal(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		BigInteger bi = new BigInteger(bb.array());
		//Get the unsigned representation for -ve numbers
		if (bi.compareTo(BigInteger.ZERO) < 0) {
			bi = DECIMAL_REP_COUNT.add(bi);
		}
		return bi.toString();
	}
}
