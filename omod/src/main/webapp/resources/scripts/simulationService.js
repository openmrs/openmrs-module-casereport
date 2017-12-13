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