/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("casereport.simulator.boot", [])

    .controller("BootController", ["$scope",

        function($scope){
            window.setTimeout(function(){
                
                angular.bootstrap("#casereport-simulator", ["casereport.simulator"]);
                $("#casereport-simulator-boot").hide();
                $("#casereport-simulator").show();

            }, 30);
        }

    ]);

angular.module("casereport.simulator", ["simulationService", "systemSettingService"])

    .factory('Patient', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/patient/:uuid", {
        },{
            query: { method:'GET' }
        });
    })

    .controller("SimulatorController", ["$scope", "SimulationService", "Patient",

        function($scope, SimulationService, Patient){
            $scope.eventIndex = null;
            $scope.identifierType = null;
            $scope.dataset = dataset;

            $scope.run = function(){
                //alert($scope.eventIndex);
            }

            $scope.buildPatient = function(patientData){
                var birthDateStr = moment().add(patientData.birthdate, 'days').format('YYYY-MM-DD');

                var person = {
                    birthdate: birthDateStr,
                    gender: patientData.gender,
                    names: [
                        {
                            givenName: patientData.givenName,
                            middleName: patientData.middleName,
                            familyName: patientData.familyName
                        }
                    ]
                }

                var identifier =  {
                    identifier: patientData.identifier,
                    identifierType: $scope.identifierType
                }

                return {
                    person: person,
                    identifiers: [identifier]
                }

            }

            $scope.createPatients = function(){
                SimulationService.getGlobalProperty('casereport.simulator.patientsCreated').then(function(value){
                    if(!value){
                        SimulationService.getGlobalProperty('casereport.identifierTypeUuid').then(function(gp){
                            if(gp) {
                                $scope.identifierType = gp.value;
                                createPatientsInternal();
                            }
                        });
                    }
                });
            }

            $scope.createObs = function() {

            }

            function createPatientsInternal(){
                var savedCount = 0;
                var patients = $scope.dataset.patients;
                for(var i in patients){
                    var patient = $scope.buildPatient(patients[i]);
                    Patient.save(patient).$promise.then(function(){
                        savedCount++;
                        if(savedCount == patients.length){
                            SimulationService.saveGlobalProperty('casereport.simulatorPatientsCreated', 'true').then(function(){
                                emr.successMessage('Created patients successfully');
                            });
                        }
                    });
                }

            }

        }

    ]);
