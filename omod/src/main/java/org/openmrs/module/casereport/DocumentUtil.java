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
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.marc.everest.datatypes.NullFlavor;
import org.marc.everest.datatypes.TS;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

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
	 * Gets the value of the Organization OID global property
	 *
	 * @return the Organization's OID
	 */
	public static String getOrganizationOID() {
		return getGlobalProperty(DocumentConstants.GP_ORG_ID);
	}
	
	/**
	 * Gets the value of the Organization extension global property
	 *
	 * @return the Organization's extension
	 */
	public static String getOrganizationExtension() {
		return getGlobalProperty(DocumentConstants.GP_ORG_EXT);
	}
	
	/**
	 * Gets the value of the Organization name global property
	 *
	 * @return the Organization's name
	 */
	public static String getOrganizationName() {
		return getGlobalProperty(DocumentConstants.GP_ORG_NAME);
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
	 * Gets the value of the patient id format format global property
	 * 
	 * @return the patient id format
	 */
	public static String getPatientIdFormat() {
		return getGlobalProperty(DocumentConstants.GP_PATIENT_ID_FORMAT);
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
	public static PersonName getPersonNameForProvider(String uuid) {
		Provider provider = Context.getProviderService().getProviderByUuid(uuid);
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
	
	public static File getSubmittedCaseReportFile(CaseReport caseReport) {
		Date date = caseReport.getResolutionDate();
		if (date == null) {
			throw new APIException("A case report must have a resolution date to get its document file");
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String year = new Integer(cal.get(Calendar.YEAR)).toString();
		String month = new Integer(cal.get(Calendar.MONTH) + 1).toString();
		String day = new Integer(cal.get(Calendar.DAY_OF_MONTH)).toString();
		String appDataDir = OpenmrsUtil.getApplicationDataDirectory();
		File dir = Paths.get(appDataDir, CaseReportConstants.MODULE_ID, year, month, day).toFile();
		
		return new File(dir, caseReport.getUuid() + DocumentConstants.DOC_FILE_EXT);
	}
	
	/**
	 * Retrieves the contents of the saved document for the specified submitted case report
	 * 
	 * @param caseReport the submitted case report object
	 * @return the contents of the saved document for the case report
	 */
	public static String getSubmittedDocumentContents(CaseReport caseReport) {
		if (!caseReport.isSubmitted()) {
			throw new APIException(caseReport + " is not submitted");
		}
		
		File docFile = getSubmittedCaseReportFile(caseReport);
		try {
			return FileUtils.readFileToString(docFile, DocumentConstants.ENCODING);
		}
		catch (IOException e) {
			throw new APIException(e);
		}
	}
	
	/**
	 * Creates and returns a reference term representation of the value of the specified global
	 * property name
	 * 
	 * @param globalPropertyName the name of the global property to match
	 * @return a ReferenceTerm object
	 */
	public static ReferenceTerm getReferenceTerm(String globalPropertyName) {
		String mapping = getGlobalProperty(globalPropertyName);
		if (StringUtils.isBlank(mapping)) {
			throw new APIException(globalPropertyName + " global property value is required");
		}
		
		String[] fields = StringUtils.split(mapping, ":");
		if (fields.length != 4) {
			throw new APIException("Invalid value for the " + globalPropertyName + " global property");
		}
		
		return new ReferenceTerm(fields[0], fields[1], fields[2], fields[3]);
	}
	
	/**
	 * Gets the HIE identifier mapped to the patient identifier type with the specified uuid
	 *
	 * @return the OID of mapped HIE identifier
	 */
	public static String getMappedHieIdentifier(String idTypeUuid) {
		String mappingsStr = Context.getAdministrationService().getGlobalProperty(DocumentConstants.GP_ID_MAPPINGS);
		if (StringUtils.isBlank(mappingsStr)) {
			throw new APIException(DocumentConstants.GP_ID_MAPPINGS + " global property value needs to be set");
		}
		
		String[] mappings = StringUtils.split(mappingsStr, CaseReportConstants.CHAR_COMMA);
		for (String mapping : mappings) {
			String[] localIdAndHieId = StringUtils.split(mapping, CaseReportConstants.CHAR_COLON);
			if (idTypeUuid.equalsIgnoreCase(localIdAndHieId[0].trim())) {
				return localIdAndHieId[1].trim();
			}
		}
		
		throw new APIException("No HIE identifier mapped to identifier type with uuid: " + idTypeUuid);
	}
	
	public static String getCaseReportFormat() {
		return getGlobalProperty(DocumentConstants.GP_CASEREPORT_FORMAT);		
	}

	
}
