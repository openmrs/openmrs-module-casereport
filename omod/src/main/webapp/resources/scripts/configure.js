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
        "caseReportService",
        "systemSettingService",
        "ui.router",
        "uicommons.filters",
        "uicommons.common.error"
    ])

    .config([ "$stateProvider", "$urlRouterProvider", function($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.otherwise("/view");

        $stateProvider
            .state('view', {
                url: "/view",
                templateUrl: "templates/viewConfig.page",
                controller: "ConfigController"
            })
    }])

    .controller("ConfigController", ["$scope", "CaseReportService", "SystemSettingService",

        function ($scope, CaseReportService, SystemSettingService) {
            $scope.settings;
            var params = {q: "casereport", v: "custom:(property,value,description,uuid)"};

            SystemSettingService.getSystemSettings(params).then(function(results){
                var ret = [];
                for(var i in results){
                    var r = results[i];
                    if('casereport.mandatory' == r.property || 'casereport.started' == r.property) {
                        continue;
                    }
                    ret.push(results[i]);
                }
                $scope.settings = ret;
            });

            $scope.getDisplay = function(setting) {
                var ret =  setting.substr(setting.indexOf('.') + 1);
                ret = ret.charAt(0).toUpperCase() + ret.substring(1);
                //hack to make sure we split between OpenHIM and the word after it
                var openHimIndex = ret.indexOf('OpenHIM');
                if(openHimIndex == 0){
                    ret = ret.replace('OpenHIM', '');
                }
                ret = ret.replace(/([a-z](?=[A-Z]))/g, '$1 ');
                if(openHimIndex == 0){
                    ret = 'OpenHIM '+ret;
                }
                ret = ret.replace(/uuid$/i, '');

                return ret;
            }
        }

    ]);