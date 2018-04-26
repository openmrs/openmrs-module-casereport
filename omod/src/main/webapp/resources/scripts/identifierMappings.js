/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("casereports.identifierMappings", [
        "uicommons.common.error",
        "uicommons.filters",
        "systemSettingService",
        "patientIdentifierTypeService"
    ])

    .controller("IdentifierMappingsController", ["$scope", "SystemSettingService", "SystemSetting",
        "PatientIdentifierTypeService",

        function ($scope, SystemSettingService, SystemSetting, PatientIdentifierTypeService) {

            $scope.GpName = 'casereport.identifierTypeMappings';
            $scope.comma = ',';
            $scope.colon = ':';
            $scope.idTypes;
            $scope.idMappingsGp;
            $scope.idMappings = {};

            function loadIdentifierTypes(){
                PatientIdentifierTypeService.getPatientIdentifierTypes().then(function(results){
                    $scope.idTypes = results;
                    loadIdMappings();
                });
            }

            function loadIdMappings(){
                var params = {q: $scope.GpName, v: "custom:(property,value,uuid)"};
                SystemSettingService.getSystemSettings(params).then(function(results){
                    var uuidAndOidMap = {};
                    if(results.length == 1){
                        $scope.idMappingsGp = results[0];
                        var gpValue = $scope.idMappingsGp.value;
                        if(gpValue && gpValue.trim().length > 0){
                            var mappings = gpValue.split($scope.comma);
                            for(var i in mappings){
                                var uuidAndOid = mappings[i].split($scope.colon);
                                uuidAndOidMap[uuidAndOid[0].trim()] = uuidAndOid[1].trim();
                            }
                        }
                    }
                    
                    for(var i in $scope.idTypes){
                        var uuid = $scope.idTypes[i].uuid;
                        $scope.idMappings[uuid] = uuidAndOidMap[uuid];
                    }
                    
                });
            }

            $scope.getIdTypeByUuid = function(uuid){
                for(var i in $scope.idTypes){
                    if(uuid == $scope.idTypes[i].uuid){
                        return $scope.idTypes[i];
                    }
                }
                return null;
            }

            $scope.save = function(){
                var gpValue = "";
                var count = 0;
                for(var uuid in $scope.idMappings){
                    var oid = $scope.idMappings[uuid];
                    if(oid && oid.trim().length > 0){
                        var mapping = count == 0 ? uuid+$scope.colon+oid : $scope.comma+uuid+$scope.colon+oid;
                        gpValue+=mapping;
                        count++;
                    }
                }
                $scope.idMappingsGp.value = gpValue;
                var gp = {}
                SystemSetting.save($scope.idMappingsGp).$promise.then(function(){
                    emr.successMessage('casereport.save.success');
                });
            }

            loadIdentifierTypes();

        }

    ])