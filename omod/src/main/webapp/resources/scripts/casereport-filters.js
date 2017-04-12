/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("casereport.filters", [])

    .filter('pagination', function () {
        return function (caseReports, $scope) {
            $scope.start = ($scope.currentPage - 1) * $scope.itemsPerPage;
            $scope.end = $scope.start + $scope.itemsPerPage;
            if($scope.end > $scope.effectiveCount){
                $scope.end = $scope.effectiveCount;
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
    });