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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

@Component
public class DefaultDataSource implements DataSource {
	
	public static final String CURRENT_HIV_WHO_STAGE = "currentHivWhoStage";
	
	private ObjectMapper mapper = new ObjectMapper();
	
	/**
	 * @see DataSource#getData(Patient)
	 * @param patient
	 */
	@Override
	public ObjectNode getData(Patient patient) {
		Map<String, UuidAndValue> data = new HashMap<>();
		Obs mostRecentWHOStageObs = CaseReportUtil.getMostRecentWHOStage(patient);
		if (mostRecentWHOStageObs != null) {
			data.put(
			    CURRENT_HIV_WHO_STAGE,
			    new UuidAndValue(mostRecentWHOStageObs.getUuid(),
			            mostRecentWHOStageObs.getValueAsString(Context.getLocale())));
		}
		
		return mapper.convertValue(data, ObjectNode.class);
	}
}
