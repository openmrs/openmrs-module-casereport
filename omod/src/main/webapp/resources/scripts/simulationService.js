/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module('simulationService', ['ngResource','uicommons.common'])
    .factory('Patient', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/patient/:uuid", {
        },{
            query: { method:'GET' }
        });
    })
    .factory('SimulationService', function(Patient, SystemSettingService, SystemSetting) {

        return {

            getPatientByIdentifier: function(id) {
                var params = {s: "patientByIdentifier", identifier: id, v: "custom:(uuid,patientIdentifier:(identifier))"};
                return Patient.get(params).$promise;
            },

            saveGlobalProperty: function(name, propertyValue){
                return SystemSetting.save({property: name, value: propertyValue}).$promise;
            }

        }
    });