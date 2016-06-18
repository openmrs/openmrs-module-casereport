/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("manageCaseReports", [ "caseReportService", "ui.router", "uicommons.filters", "uicommons.common.error"])

    .config([ "$stateProvider", "$urlRouterProvider", function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/list");

        $stateProvider
            .state('list', {
                url: "/list",
                templateUrl: "templates/list.page",
                controller: "ViewCaseReportsController"
            })
            .state('reportForm', {
                url: "/reportForm",
                templateUrl: "templates/reportForm.page",
                controller: "SubmitCaseReportController",
                params: {
                    uuid: null,
                    status: null
                },
                resolve: {
                    caseReport: function($stateParams, CaseReport) {
                        var requestParams = { uuid: $stateParams.uuid, v: "full" };
                        if ($stateParams.status != 'DRAFT') {
                            requestParams.generateForm = true;
                        }

                        return CaseReport.get(requestParams);
                    }
                }
            })
    }])

    .controller("ViewCaseReportsController", [ "$scope", "StatusChange", "CaseReportService",
        function($scope, StatusChange, CaseReportService) {
            var customRep = 'custom:(uuid,status,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportTriggers:(display,auditInfo))';

            function loadCaseReports() {
                CaseReportService.getCaseReports({v: customRep}).then(function(results) {
                    $scope.caseReports = results;
                });
            }

            $scope.dismiss = function(caseReport){
                StatusChange.save({
                    uuid: caseReport.uuid,
                    action: "DISMISS"
                }).$promise.then(function() {
                    loadCaseReports();
                    emr.successMessage("casereport.dismissed");
                });
            }

            loadCaseReports();
    }])

    .controller("SubmitCaseReportController", [ "$scope", "$state", "StatusChange", "caseReport",
        function($scope, $state, StatusChange, caseReport) {
            $scope.caseReport = caseReport;

            $scope.submitCaseReport = function() {
                StatusChange.save({
                    uuid: caseReport.uuid,
                    action: "SUBMIT"
                }).$promise.then(function() {
                    $state.go("list");
                    emr.successMessage("casereport.submitted");
                });
            }
    }]);