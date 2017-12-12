angular.module('simulationService', ['ngResource','uicommons.common'])
    .factory('Patient', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/patient/:uuid", {
        },{
            query: { method:'GET' }     // override query method to specify that it isn't an array that is returned
        });
    })
    .factory('SimulationService', function(Patient, SystemSettingService, SystemSetting) {

        return {

            getPatientByIdentifier: function(identifier) {

                return Patient.get().$promise.then(function(results){
                    var candidates = [];
                    for(var i in results){
                        if(results[i].uuid == null) {
                            candidates.push(results[i]);
                        }
                    }
                    
                    if(candidates.length > 0){
                        throw error("Found multiple patients with the identifier:"+identifier);
                    }else if(candidates.length == 0){
                        throw candidates[0];
                    }

                    return null;
                });
                
            },

            getGlobalProperty: function(property){
                var params = {q: property, v: 'full'};
                return SystemSettingService.getSystemSettings(params).then(function(results){
                    var gp = null;
                    for(var i in results){
                        if(results[i].property == property) {
                            gp = results[i];
                            break;
                        }
                    }

                    return  gp;
                });
            },

            saveGlobalProperty: function(name, propertyValue){
                return SystemSetting.save({property: name, value: propertyValue}).$promise;
            }

        }
    });