/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("manageCaseReportQueue", [
        "caseReportService",
        "personService",
        "casereport.filters",
        "ui.router",
        "ngDialog",
        "uicommons.filters",
        "uicommons.common.error",
        "ui.bootstrap"
    ])

    .config([ "$stateProvider", "$urlRouterProvider", function($stateProvider, $urlRouterProvider) {

        $urlRouterProvider.otherwise("/list");

        $stateProvider
            .state('list', {
                url: "/list",
                templateUrl: "templates/queueList.page",
                controller: "ViewQueueController"
            })
            .state('queueItemForm', {
                url: "/queueItemForm/:patientUuid",
                templateUrl: function(){
                    return "templates/queueItemForm.page";
                },
                controller: "QueueItemFormController",
                params:{
                    patientUuid: null
                },
                resolve: {
                    patient: function($stateParams, Person) {
                        return Person.get({uuid: $stateParams.patientUuid, v: "custom:(display,uuid)"});
                    },

                    triggers: function(CaseReportService) {
                        return CaseReportService.getTriggers();
                    },

                    existingQueueItem: function($stateParams, CaseReportService) {
                        var rep = "custom:(reportTriggers:(name))";
                        return CaseReportService.getExistingQueueItem($stateParams.patientUuid, rep);
                    }
                }
            })
            .state('reportForm', {
                url: "/reportForm/:uuid/:patientUuid",
                templateUrl: "templates/reportForm.page",
                controller: "ReportFormController",
                params: {
                    uuid: null,
                    patientUuid: null
                },
                resolve: {
                    caseReport: function($stateParams, CaseReport) {
                        return CaseReport.get({ uuid: $stateParams.uuid, v: "full" });
                    },
                    previousCaseReports: function($stateParams, CaseReportService){
                        var customRep = 'custom:(resolutionDate,reportForm)';
                        var params = {
                            patient: $stateParams.patientUuid,
                            v: customRep
                        };

                        return CaseReportService.getSubmittedCaseReports(params).then(function(results){
                            return results;
                        });
                    }
                }
            })
    }])

    .controller("ViewQueueController", [ "$scope", "orderByFilter", "ngDialog", "StatusChange", "CaseReportService",

        function($scope, orderBy, ngDialog, StatusChange, CaseReportService) {
            $scope.caseReports = [];
            $scope.patientSearchText = null;
            $scope.triggerSearchText = null;
            $scope.propertyName = 'dateCreated';
            $scope.reverse = false;
            $scope.currentPage = 1;
            $scope.itemsPerPage = 10;
            $scope.start = 0;
            $scope.end = 0;

            var customRep = 'custom:(dateCreated,uuid,status,patient:(patientIdentifier:(identifier),' +
                'person:(uuid,gender,age,personName:(display))),reportTriggers:(display,auditInfo))';

            function loadCaseReports() {
                CaseReportService.getCaseReports({v: customRep}).then(function(results) {
                    $scope.caseReports = results;
                    $scope.effectiveCount = $scope.caseReports.length;
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
        }
    ])

    .controller("QueueItemFormController", ["$scope", "$state", "CaseReport", "patient",
        "triggers", "existingQueueItem",

        function ($scope, $state, CaseReport, patient, triggers, existingQueueItem) {
            $scope.patient = patient;
            $scope.triggers = triggers;
            $scope.existingQueueItem = existingQueueItem;
            $scope.trigger;

            $scope.saveNewQueueItem = function(){
                var newItem = {
                    patient: $scope.patient.uuid,
                    reportTriggers: [
                        {"name": $scope.trigger}
                    ]
                }

                CaseReport.save(newItem).$promise.then(function() {
                    $state.go("list");
                    emr.successMessage("casereport.save.success");
                });
            }

            $scope.hasItemWithTrigger = function(trigger){
                if($scope.existingQueueItem) {
                    for (var i in $scope.existingQueueItem.reportTriggers) {
                        if (trigger.name == $scope.existingQueueItem.reportTriggers[i].name) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

    ])

    .controller("ReportFormController", [ "$scope", "$state", "$filter", "orderByFilter",
        "ngDialog", "CaseReport", "StatusChange", "caseReport", "previousCaseReports",

        function($scope, $state, $filter, orderBy, ngDialog, CaseReport, StatusChange, caseReport, previousCaseReports) {
            $scope.caseReport = caseReport;
            $scope.previousCaseReports = previousCaseReports;
            $scope.showPreviousReports = false;

            $scope.updateFormTitle = function(personName){
                var text = emr.message('casereport.report.form.title').replace("{0}", personName);
                jq("#casereport-reportTitle").text(text);
            }

            $scope.formatDate = function(date){
                return $filter('serverDate')(date, 'dd-MMM-yyyy');
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
                var id = ngDialog.open({
                    showClose: false,
                    closeByEscape: false,
                    closeByDocument: false,
                    template: "casereport-template-processing"
                });

                StatusChange.save({
                    uuid: caseReport.uuid,
                    action: "SUBMIT",
                    reportForm: $scope.caseReport.reportForm
                }).$promise.then(function() {
                    $state.go("list");
                    emr.successMessage("casereport.submitted");
                }, function(error) {
                    emr.errorMessage("casereport.seeLogs");
                }).finally(function(){
                    ngDialog.close(id);
                });
            }
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

            $scope.effectiveCount = matches.length;
            //apply paging so that we only see a single page of results
            return $filter('pagination')(matches, $scope);
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