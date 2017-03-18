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

import java.util.UUID;

import org.junit.Test;

public class DocumentUtilTest {
	
	@Test
	public void convertToDecimalString_shouldReturnTheStringifiedDecimalFormOfTheSpecifiedUuid() {
		assertEquals("165886298145228458464681453875973269261",
		    convertToDecimal(UUID.fromString("7ccc89f5-1904-4141-b5e3-bf0d8bb3270d")));
		
		//Should be the unsigned representation for negative numbers 
		String uuid = "e2687878-fb18-4dda-85c4-eb451bbb765e";
		assertEquals("300947969394920668599875792303032071774", convertToDecimal(UUID.fromString(uuid)));
	}
}
