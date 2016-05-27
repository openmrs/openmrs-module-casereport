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

            function loadCaseReports() {
                CaseReportService.getCaseReports({v: "default"}).then(function(results) {
                    $scope.caseReports = results;
                });
            }

            loadCaseReports();

        }]
);