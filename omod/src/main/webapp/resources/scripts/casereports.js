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
                    uuid: null
                },
                resolve: {
                    caseReport: function($stateParams, CaseReport) {
                        return CaseReport.get({ uuid: $stateParams.uuid, v: "full" });
                    }
                }
            })
    }])

    .controller("ViewCaseReportsController", [ "$scope", 'orderByFilter', "StatusChange", "CaseReportService",
        function($scope, orderBy, StatusChange, CaseReportService) {
            $scope.propertyName = 'dateCreated';
            $scope.reverse = true;
            var customRep = 'custom:(dateCreated,uuid,status,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportTriggers:(display,auditInfo))';

            function loadCaseReports() {
                CaseReportService.getCaseReports({v: customRep}).then(function(results) {
                    $scope.caseReports = results;
                });
            }

            $scope.sort = function(propertyName){
                $scope.reverse = ($scope.propertyName == propertyName) ? !$scope.reverse : false;
                $scope.propertyName = propertyName;
                $scope.caseReports = orderBy($scope.caseReports, $scope.propertyName, $scope.reverse);
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

    .controller("SubmitCaseReportController", [ "$scope", "$state", "$filter", "StatusChange", "caseReport",
        function($scope, $state, $filter, StatusChange, caseReport) {
            $scope.caseReport = caseReport;
            $scope.areTriggersSet = false;
            $scope.triggers;

            function getKeys(obj){
                if(obj) {
                    return Object.keys(obj);
                }
                return [];
            }

            $scope.setTriggers = function(triggerDateMap){
                if(triggerDateMap && !$scope.areTriggersSet) {
                    $scope.triggers = getKeys(triggerDateMap);
                    $scope.areTriggersSet = true;
                }
            }

            $scope.updateFormTitle = function(personName){
                jq("#casereport-reportTitle").text(emr.message('casereport.report.form.title').replace("{0}", personName));
            }

            $scope.formatDate = function(date){
                return $filter('serverDate')(date, 'dd-MMM-yyyy');
            }

            $scope.getObjectKeys = function(obj){
                if(obj) {
                    return getKeys(obj);
                }
                return [];
            }

            $scope.getMapSize = function(obj){
                return getKeys(obj).length;
            }

            $scope.remove = function(index){
                $scope.triggers.splice(index, 1);
            }

            $scope.submitCaseReport = function() {
                StatusChange.save({
                    uuid: caseReport.uuid,
                    action: "SUBMIT"
                }).$promise.then(function() {
                    $state.go("list");
                    emr.successMessage("casereport.submitted");
                });
            }
    }])

    .filter('searchReportsByPatient', function () {
        return function (caseReports, searchText) {
            if(!searchText) {
                return caseReports;
            }

            var matches = [];
            var regex = new RegExp(searchText, 'i');
            for (var i = 0; i < caseReports.length; i++) {
                var caseReport = caseReports[i];
                if (regex.test(caseReport.patient.person.personName.display)) {
                    matches.push(caseReport);
                }
            }

            return matches;
        }
    })

    .filter('searchReportsByTrigger', function () {
        return function (caseReports, searchText) {
            if(!searchText) {
                return caseReports;
            }

            var matches = [];
            var regex = new RegExp(searchText, 'i');
            for (var i = 0; i < caseReports.length; i++) {
                var caseReport = caseReports[i];
                for (var j = 0; j < caseReport.reportTriggers.length; j++) {
                    var trigger = caseReport.reportTriggers[j];
                    if (regex.test(trigger.display)) {
                        matches.push(caseReport);
                        break;
                    }
                }
            }

            return matches;
        }
    })

    .filter('searchTriggers', function () {
        return function (caseReportTriggers, searchText) {
            if(!searchText) {
                return caseReportTriggers;
            }

            var matches = [];
            var regex = new RegExp(searchText, 'i');
            for (var i = 0; i < caseReportTriggers.length; i++) {
                var trigger = caseReportTriggers[i];
                if (regex.test(trigger.display)) {
                    matches.push(trigger);
                }
            }

            return matches;
        }
    });