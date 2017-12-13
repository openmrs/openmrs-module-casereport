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

angular.module("casereport.simulator", ["uicommons.filters", "simulationService", "systemSettingService"])

    .factory('Patient', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/patient/:uuid", {
        },{
            query: { method:'GET' }
        });
    })

    .run(function($rootScope, SimulationService){
        SimulationService.getGlobalProperty('casereport.simulatorPatientsCreated').then(function(value){
            if(!value){
                SimulationService.getGlobalProperty('casereport.identifierTypeUuid').then(function(gp){
                    if(gp) {
                        $rootScope.identifierType = gp.value;
                    }
                });
            }else {
                $rootScope.patientsCreated = true;
            }
        });
    })

    .controller("SimulatorController", ["$scope", "$filter", "SimulationService", "Patient", "$rootScope",

        function($scope, $filter, SimulationService, Patient, $rootScope){
            $scope.eventIndex = null;
            $scope.dataset = dataset;

            $scope.run = function(){
                alert($scope.eventIndex);
            }

            $scope.displayEvent = function(event){
                var patient = getPatientById(event.identifier);
                var name = patient.givenName+" "+patient.middleName+" "+patient.familyName;
                var date = $scope.formatDate(convertToDate(event.date, 'dd-MMM-yyyy HH:mm'));
                return event.event+" for "+name+" on "+date;
            }

            function getPatientById(id){
                for (var i in dataset.patients){
                    var patient = $scope.dataset.patients[i];
                    if(id == patient.identifier){
                        return patient;
                    }
                }
                
                throw Error("No Patient found with id: "+id);
            }

            $scope.formatDate = function(date, format){
                if(!format){
                    format = 'dd-MMM-yyyy';
                }
                return $filter('serverDate')(date, format);
            }

            function convertToDate(offSetInDays){
                return moment().add(offSetInDays, 'days').format('YYYY-MM-DD')
            }

            $scope.patientsCreated = function(){
                return $rootScope.patientsCreated;
            }

            $scope.buildPatient = function(patientData){
                var birthDateStr = convertToDate(patientData.birthdate);

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
                    identifierType: $rootScope.identifierType
                }

                return {
                    person: person,
                    identifiers: [identifier]
                }

            }

            $scope.createPatients = function(){
                var savedCount = 0;
                var patients = $scope.dataset.patients;
                $rootScope.patientsCreated = true;
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

            $scope.createObs = function() {

            }

        }

    ]);
