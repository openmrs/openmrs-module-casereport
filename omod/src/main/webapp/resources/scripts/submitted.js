/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("casereports.submitted", [
        "caseReportService",
        "casereport.filters",
        "ui.router",
        "uicommons.filters",
        "uicommons.common.error",
        "ui.bootstrap"
    ])

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
            $scope.searchText = null;
            $scope.currentPage = 1;
            $scope.itemsPerPage = 10;
            $scope.start = 0;
            $scope.end = 0;

            var customRep = 'custom:(resolutionDate,uuid,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportForm)';

            CaseReportService.getSubmittedCaseReports({v: customRep}).then(function(results) {
                $scope.caseReports = results;
                $scope.effectiveCount = $scope.caseReports.length;
            });
        }

    ])

    .filter('mainFilter', function ($filter) {

        return function (caseReports, $scope) {

            var matches = [];
            if(!$scope.searchText){
                matches = caseReports;
            }else {
                matches = $filter('searchReportsByPatient')(caseReports, $scope.searchText);
            }

            $scope.effectiveCount = matches.length;
            //apply paging so that we only see a single page of results
            return $filter('pagination')(matches, $scope);
        }

    });