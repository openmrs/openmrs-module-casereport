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

/**
 * A subclass of UuidAndValue that has date field associated to the domain object, the value of the
 * date field should be the formatted string version.
 */
public class DatedUuidAndValue extends UuidAndValue {
	
	private String date;
	
	public DatedUuidAndValue() {
	}
	
	public DatedUuidAndValue(String uuid, Object value, String date) {
		super(uuid, value);
		this.date = date;
	}
	
	public String getDate() {
		return date;
	}
	
	public void setDate(String date) {
		this.date = date;
	}
}
