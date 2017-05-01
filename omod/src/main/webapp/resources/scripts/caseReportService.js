/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module('caseReportService', ['ngResource', 'uicommons.common'])

    .factory('CaseReport', function($resource) {

        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/casereport/casereport/:uuid", {
            uuid: '@uuid'
        },{
            query: { method:'GET' }
        });
    })

    .factory('StatusChange', function($resource) {

        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/casereport/casereport/:uuid/statuschange", {
            uuid: '@uuid'
        });

    })

    .factory('Trigger', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/casereport/trigger", {},{
            query: { method:'GET' }
        });
    })

    .factory('SubmittedDocument', function($resource) {

        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/casereport/casereport/:uuid/document", {
            uuid: '@uuid'
        },{
            query: { method:'GET', isArray:false }
        });

    })

    .config(function($httpProvider) {
        //Insert our transformer to go first so that we can remove the uuid field
        //otherwise the REST logic will freak out since StatusChange has no uuid field.
        //The only reason we initially include it is because it is needed to
        //replace :uuid in the url
        var defaultTransformer = $httpProvider.defaults.transformRequest[0];
        $httpProvider.defaults.transformRequest = [];
        $httpProvider.defaults.transformRequest.push(function (data){
            if(data){
                delete data.uuid;
            }
            return data;
        });
        $httpProvider.defaults.transformRequest.push(defaultTransformer);
    })

    .factory("CaseReportService", function(RestService, CaseReport, Trigger, SubmittedDocument) {

        return {
            getCaseReports: function(parameters) {
                return RestService.getAllResults(CaseReport, parameters);
            },

            getSubmittedCaseReports: function(parameters) {
                var params = _.clone(parameters);
                params['status'] = 'SUBMITTED';

                return RestService.getAllResults(CaseReport, params);
            },

            getTriggers: function() {
                return Trigger.get().$promise.then(function(response){
                    return response.results;
                });
            },

            getExistingQueueItem: function(patientUuid, representation){
                var params = {
                    s: "forPatient",
                    patient: patientUuid
                }
                if(representation){
                    params['v'] = representation
                }

                return this.getCaseReports(params).then(function(reports){
                    if(reports.length > 0){
                        return reports[0];
                    }

                    return null;
                });
            },

            getSubmittedDocument: function(caseReportUuid){
                return SubmittedDocument.get({uuid: caseReportUuid}).$promise;
            }
        }

    });


