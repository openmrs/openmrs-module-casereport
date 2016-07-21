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
 * An instance of this class encapsulates a minimised serializable version of a domain object it
 * contains the uuid and a serialized value of interest for the domain object, the value can be
 * anything e.g an Obs value, visit datetime, drug name etc.
 */
public class UuidAndValue {
	
	private String uuid;
	
	private Object value;
	
	public UuidAndValue() {
	}
	
	public UuidAndValue(String uuid, Object value) {
		this.uuid = uuid;
		this.value = value;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
}
