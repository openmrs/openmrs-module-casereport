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

import java.io.Serializable;

public class Trigger implements Serializable {
	
	/*
	 * TODO In future nstances of this class need to be persistent and should extend BaseOpenmrsMetadata,
	 * at that point we should get rid of equals and hashcode methods
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	public Trigger(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof Trigger)) {
			return false;
		}
		
		Trigger other = (Trigger) o;
		
		return (name == null) ? other.name == null : name.equalsIgnoreCase(other.name);
		
	}
	
	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : super.hashCode();
	}
}
