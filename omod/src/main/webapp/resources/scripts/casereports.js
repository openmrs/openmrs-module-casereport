angular.module("manageCaseReports", [ "caseReportService", "ui.router", "uicommons.filters", "uicommons.common.error"])

    .config([ "$stateProvider", "$urlRouterProvider", function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/list");

        $stateProvider
            .state('list', {
                url: "/list",
                templateUrl: "templates/list.page",
                controller: "ViewCaseReportsController"
            })
    }])

    .controller("ViewCaseReportsController", [ "$scope", "$state", "CaseReport", "CaseReportService",
        function($scope, $state, CaseReport, CaseReportService) {
            var customRep = 'custom:(uuid,status,patient:(patientIdentifier:(identifier),person:(gender,personName:(display))),' +
                'reportTriggers:(display))';

            function loadCaseReports() {
                CaseReportService.getCaseReports({v: customRep}).then(function(results) {
                    $scope.caseReports = results;
                });
            }

            loadCaseReports();
    }]);