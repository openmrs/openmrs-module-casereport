angular.module('caseReportService', ['ngResource', 'uicommons.common'])

    .factory('CaseReport', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/casereport/queue/:uuid", {
            uuid: '@uuid'
        },{
            query: { method:'GET' }
        });
    })

    .factory("CaseReportService", function(CaseReport, RestService) {
        return {
            getCaseReports: function(params) {
                return RestService.getAllResults(CaseReport, params);
            }
        }
    });