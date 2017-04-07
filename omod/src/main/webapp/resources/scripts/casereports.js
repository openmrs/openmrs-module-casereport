/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("manageCaseReports", [ "caseReportService", "personService", "ui.router", "ngDialog", "uicommons.filters", "uicommons.common.error", "ui.bootstrap"])

    .config([ "$stateProvider", "$urlRouterProvider", function($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.otherwise("/home");

        $stateProvider
            .state('home', {
                url: "/home",
                templateUrl: "templates/home.page",
                controller: "HomeController"
            })
            .state('queue', {
                url: "/queue",
                templateUrl: "templates/queue.page",
                controller: "ViewQueueController"
            })
            .state('queueItemForm', {
                url: "/queueItemForm/:patientUuid",
                templateUrl: function($stateParams){
                    return "templates/queueItemForm.page?patient="+$stateParams.patientUuid;
                },
                controller: "QueueItemFormController",
                params:{
                    patientUuid: null
                },
                resolve: {
                    patient: function($stateParams, Person) {
                       return Person.get({uuid: $stateParams.patientUuid, v: "custom:(display,uuid)"});
                    }
                }
            })
            .state('reportForm', {
                url: "/reportForm/:uuid",
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
            .state('submitted', {
                url: "/submitted",
                templateUrl: "templates/submitted.page",
                controller: "ViewSubmittedCaseReportsController"
            })
    }])

    .controller("HomeController", ["$rootScope",
        function ($rootScope) {
            $rootScope.$on('$stateChangeSuccess', function(){
                emr.updateBreadcrumbs();
            });
        }
    ])

    .controller("ViewQueueController", [ "$scope", "orderByFilter", "ngDialog", "StatusChange", "CaseReportService",
        function($scope, orderBy, ngDialog, StatusChange, CaseReportService) {
            $scope.caseReports = [];
            $scope.patientSearchText = null;
            $scope.triggerSearchText = null;
            $scope.propertyName = 'dateCreated';
            $scope.reverse = true;
            $scope.currentPage = 1;
            $scope.itemsPerPage = 10;
            $scope.start = 0;
            $scope.end = 0;

            var customRep = 'custom:(dateCreated,uuid,status,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportTriggers:(display,auditInfo))';

            function loadCaseReports() {
                CaseReportService.getCaseReports({v: customRep}).then(function(results) {
                    $scope.caseReports = results;
                    $scope.effectiveCaseReportCount = $scope.caseReports.length;
                });
            }

            $scope.sort = function(propertyName){
                $scope.reverse = ($scope.propertyName == propertyName) ? !$scope.reverse : false;
                $scope.propertyName = propertyName;
                $scope.caseReports = orderBy($scope.caseReports, $scope.propertyName, $scope.reverse);
            }

            $scope.openNewItemForm = function(){
                emr.navigateTo({
                    provider: "coreapps",
                    page: "findpatient/findPatient",
                    query: {
                        app: "casereport.newItemForm"
                    }
                });
            }

            $scope.dismiss = function(caseReport){
                ngDialog.openConfirm({
                    showClose: false,
                    closeByEscape: true,
                    closeByDocument: true,
                    template:"templates/dismissCaseReportDialog.page",
                    controller: function($scope) {
                        $scope.caseReport = caseReport;
                    }
                }).then(function() {
                    StatusChange.save({
                        uuid: caseReport.uuid,
                        action: "DISMISS"
                    }).$promise.then(function() {
                        loadCaseReports();
                        emr.successMessage("casereport.dismissed");
                    });
                });
            }

            loadCaseReports();
    }])

    .controller("QueueItemFormController", ["$scope", "$state", "CaseReport", "patient",
        function ($scope, $state, CaseReport, patient) {
            $scope.patient = patient;
            $scope.trigger;

            $scope.saveNewQueueItem = function(){
                var newItem = {
                    patient: $scope.patient.uuid,
                    reportTriggers: [
                        {"name": $scope.trigger}
                    ]
                }

                CaseReport.save(newItem).$promise.then(function() {
                    $state.go("queue");
                    emr.successMessage("casereport.save.success");
                });
            }
        }
    ])

    .controller("SubmitCaseReportController", [ "$scope", "$state", "$filter", "orderByFilter", "CaseReport", "StatusChange", "caseReport",
        function($scope, $state, $filter, orderBy, CaseReport, StatusChange, caseReport) {
            $scope.caseReport = caseReport;
            $scope.previousReportDetails = [];
            $scope.showPreviousReports = false;

            function getKeys(obj){
                if(obj) {
                    return Object.keys(obj);
                }
                return [];
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

            $scope.getValues = function(list){
                var values = [];
                for(var i in list){
                    values.push(list[i].value);
                }
                return values;
            }

            $scope.remove = function(index){
                $scope.caseReport.reportForm.triggers.splice(index, 1);
            }

            $scope.submitCaseReport = function() {
                StatusChange.save({
                    uuid: caseReport.uuid,
                    action: "SUBMIT",
                    reportForm: $scope.caseReport.reportForm
                }).$promise.then(function() {
                    $state.go("queue");
                    emr.successMessage("casereport.submitted");
                }, function(error) {
                    emr.errorMessage("casereport.seeLogs");
                });
            }

            $scope.togglePreviousReports = function(){
                if($scope.getMapSize($scope.previousReportDetails) == 0) {
                    var prevReportCount = $scope.getMapSize($scope.caseReport.reportForm.previousReportUuidTriggersMap);
                    _.map($scope.caseReport.reportForm.previousReportUuidTriggersMap, function (triggers, reportUuid) {
                        CaseReport.get({uuid: reportUuid, v: "full"}).$promise.then(function (cr) {
                            var item = {};
                            //We're using dateChanged as the date the report was submitted
                            item.datechanged = cr.auditInfo.dateChanged;
                            item.triggers = triggers;
                            $scope.previousReportDetails.push(item);
                            //When all the previous report details are fetched
                            if ($scope.previousReportDetails.length == prevReportCount) {
                                $scope.previousReportDetails = orderBy($scope.previousReportDetails, 'datechanged', true);
                                $scope.showPreviousReports = true;
                            }
                        });
                    });
                } else{
                    $scope.showPreviousReports = !$scope.showPreviousReports;
                }
            }
    }])

    .controller("ViewSubmittedCaseReportsController", ["$scope", "CaseReportService",
        function ($scope, CaseReportService) {
            $scope.caseReports = [];
            var customRep = 'custom:(dateChanged,uuid,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportForm:(triggers:(display)))';

            CaseReportService.getSubmittedCaseReports({s: 'default', v: customRep}).then(function(results) {
                $scope.caseReports = results;
            });
        }
    ])

    .filter('mainFilter', function ($filter) {
        return function (caseReports, $scope) {

            var matches = [];
            if(!$scope.patientSearchText && !$scope.triggerSearchText){
                matches = caseReports;
            }else {
                if (!$scope.triggerSearchText) {
                    matches = $filter('searchReportsByPatient')(caseReports, $scope.patientSearchText);
                } else {
                    matches = $filter('searchReportsByTrigger')(caseReports, $scope.triggerSearchText);
                }
            }

            $scope.effectiveCaseReportCount = matches.length;
            //apply paging so that we only see a single page of results
            return $filter('pagination')(matches, $scope);
        }
    })

    .filter('pagination', function () {
        return function (caseReports, $scope) {
            $scope.start = ($scope.currentPage - 1) * $scope.itemsPerPage;
            $scope.end = $scope.start + $scope.itemsPerPage;
            if($scope.end > $scope.effectiveCaseReportCount){
                $scope.end = $scope.effectiveCaseReportCount;
            }

            return caseReports.slice($scope.start, $scope.end);
        }
    })

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