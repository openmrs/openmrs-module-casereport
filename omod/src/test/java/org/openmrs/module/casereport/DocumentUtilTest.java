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

import static org.junit.Assert.assertEquals;
import static org.openmrs.module.casereport.DocumentUtil.convertToDecimal;

import java.io.File;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;

public class DocumentUtilTest extends BaseModuleWebContextSensitiveTest {
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	
	@Test
	public void convertToDecimalString_shouldReturnTheStringifiedDecimalFormOfTheSpecifiedUuid() {
		assertEquals("165886298145228458464681453875973269261",
		    convertToDecimal(UUID.fromString("7ccc89f5-1904-4141-b5e3-bf0d8bb3270d")));
		
		//Should be the unsigned representation for negative numbers 
		String uuid = "e2687878-fb18-4dda-85c4-eb451bbb765e";
		assertEquals("300947969394920668599875792303032071774", convertToDecimal(UUID.fromString(uuid)));
	}
	
	@Test
	public void getSubmittedCaseReportFile_shouldGetTheDocumentFileForTheSpecifiedCaseReport() throws Exception {
		
		CaseReport cr = new CaseReport();
		final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final String year = "2016";
		final String month = "11";
		final String day = "2";
		Date resolutionDate = dateFormat.parse(year + "-" + month + "-" + day);
		cr.setResolutionDate(resolutionDate);
		System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", SystemUtils.JAVA_IO_TMPDIR);
		File file = DocumentUtil.getSubmittedCaseReportFile(cr);
		String expected = Paths.get(OpenmrsUtil.getApplicationDataDirectory(), CaseReportConstants.MODULE_ID, year, month,
		    day, cr.getUuid() + DocumentConstants.DOC_FILE_EXT).toString();
		assertEquals(expected, file.getAbsolutePath());
	}
	
	@Test
	public void getMappedHieIdentifier_shouldFailIfTheGpIsNotSet() throws Exception {
		expectedException.expect(APIException.class);
		expectedException.expectMessage(CoreMatchers.equalTo(DocumentConstants.GP_ID_MAPPINGS
		        + " global property value needs to be set"));
		DocumentUtil.getMappedHieIdentifier("some-fake-uuid-81b5-01f0c0dfa53c");
	}
	
	@Test
	public void getMappedHieIdentifier_shouldFailIfThereIsNoMappedHieIdentifier() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-HIE.xml");
		final String uuid = "some-fake-uuid-81b5-01f0c0dfa53c";
		expectedException.expect(APIException.class);
		expectedException.expectMessage(CoreMatchers.equalTo("No HIE identifier mapped to identifier type with uuid: "
		        + uuid));
		DocumentUtil.getMappedHieIdentifier(uuid);
	}
	
	@Test
	public void getMappedHieIdentifier_shouldReturnTheMappedHieIdentifier() throws Exception {
		executeDataSet("moduleTestData-initial.xml");
		executeDataSet("moduleTestData-HIE.xml");
		assertEquals("2.16.840.1.113883.1.3", DocumentUtil.getMappedHieIdentifier("2f470aa8-1d73-43b7-81b5-01f0c0dfa53c"));
	}
	
}
