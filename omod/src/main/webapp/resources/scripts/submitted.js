/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("submittedCaseReports", [ "caseReportService", "ui.router", "uicommons.filters", "uicommons.common.error", "ui.bootstrap"])

    .config([ "$stateProvider", "$urlRouterProvider", function($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.otherwise("/list");

        $stateProvider
            .state('list', {
                url: "/list",
                templateUrl: "templates/submittedList.page",
                controller: "SubmittedCaseReportsController"
            })
    }])

    .controller("SubmittedCaseReportsController", ["$scope", "CaseReportService",
        function ($scope, CaseReportService) {
            $scope.caseReports = [];
            var customRep = 'custom:(resolutionDate,uuid,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportForm:(triggers))';

            CaseReportService.getSubmittedCaseReports({v: customRep}).then(function(results) {
                $scope.caseReports = results;
            });
        }
    ]);