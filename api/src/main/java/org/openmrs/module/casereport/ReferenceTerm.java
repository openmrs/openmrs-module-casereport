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

public class ReferenceTerm {
	
	private String codeSystem;
	
	private String codeSystemName;
	
	private String code;
	
	private String name;
	
	public ReferenceTerm(String codeSystem, String codeSystemName, String code, String name) {
		this.codeSystem = codeSystem;
		this.codeSystemName = codeSystemName;
		this.code = code;
		this.name = name;
	}
	
	public String getCodeSystem() {
		return codeSystem;
	}
	
	public String getCodeSystemName() {
		return codeSystemName;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getName() {
		return name;
	}
	
}
