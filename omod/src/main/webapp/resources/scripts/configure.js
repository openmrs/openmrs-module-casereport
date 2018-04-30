/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("casereports.configure", [
        "ngSanitize",
        "systemSettingService",
        "patientIdentifierTypeService",
        "providerService",
        "uicommons.filters",
        "uicommons.common.error"
    ])

    .controller("ConfigController", ["$scope", "SystemSettingService", "SystemSetting",
        "PatientIdentifierTypeService", "ProviderService",

        function ($scope, SystemSettingService, SystemSetting, PatientIdentifierTypeService, ProviderService) {
            $scope.settings;
            $scope.identifierTypes;
            $scope.providers;

            $scope.optionsSettings = [
                'casereport.autoSubmitProviderUuid',
                'casereport.healthCareFacilityTypeDisplayName',
                'casereport.practiceSettingDisplayName',
                'casereport.openHIMClientId',
                'casereport.openHIMClientPassword'
            ];

            $scope.confidentialityCodes = [
                {label:'Normal', value:'N'},
                {label:'Restricted', value:'R'},
                {label:'Very Restricted', value:'V'}
            ];

            var params = {q: "casereport", v: "custom:(property,value,description,uuid)"};

            var settingPropertyMap = {
                'casereport.autoSubmitProviderUuid': 'Auto Submit Provider',
                'casereport.openHIMClientId': 'OpenHIM Client Id',
                'casereport.openHIMClientPassword': 'OpenHIM Client Password',
                'casereport.openHIMUrl': 'OpenHIM URL'
            };

            var settingDescrMap = {
                'casereport.autoSubmitProviderUuid': 'The provider to set as the submitter of automatically ' +
                    'submitted case reports, must be for a provider account that is either linked to a person ' +
                    'record or has a name with at least 2 name fields specified ',

                'casereport.confidentialityCode': 'The code specifying the level of confidentiality of the CDA document'
            };

            var hiddenProps = ['casereport.identifierTypeMappings'];

            PatientIdentifierTypeService.getPatientIdentifierTypes().then(function(results){
                $scope.identifierTypes = results;
            });

            ProviderService.getProviders().then(function(results){
                $scope.providers = results;
            });

            SystemSettingService.getSystemSettings(params).then(function(results){
                var ret = [];
                for(var i in results){
                    var r = results[i];console.log(hiddenProps.indexOf(r.property));
                    if('casereport.mandatory' == r.property || 'casereport.started' == r.property
                        || hiddenProps.indexOf(r.property) > -1) {
                        continue;
                    }
                    ret.push(r);
                }
                $scope.settings = ret;
            });

            $scope.save = function() {
                var savedCount = 0;
                for(var i in $scope.settings){
                    SystemSetting.save($scope.settings[i]).$promise.then(function(){
                        savedCount++;
                        if(savedCount == $scope.settings.length){
                            emr.successMessage('casereport.save.success');
                        }
                    });
                }
            }

            $scope.isRequired = function(setting){
                return !_.contains($scope.optionsSettings, setting.property);
            }

            $scope.printProperty = function(setting) {
                if(settingPropertyMap[setting.property]){
                    return settingPropertyMap[setting.property];
                }

                var ret =  setting.property.substr(setting.property.indexOf('.') + 1);
                //capitalize the first letter
                ret = ret.charAt(0).toUpperCase() + ret.substring(1);
                //split on carmel case i.e 'IdentifierUuid' gets transformed to 'Identifier Uuid'
                return  ret.replace(/([a-z](?=[A-Z]))/g, '$1 ');
            }

            $scope.printDescription = function(setting) {
                if(!settingDescrMap[setting.property]){
                    return setting.description;
                }

                return settingDescrMap[setting.property];
            }
        }

    ]);