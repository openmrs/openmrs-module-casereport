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

angular.module("casereport.simulator", [
        "uicommons.filters",
        "simulationService",
        "systemSettingService",
        "obsService"
    ])

    .factory('Patient', function($resource) {
        return $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/patient/:uuid", {
        },{
            query: { method:'GET' }
        });
    })

    .run(function($rootScope, SystemSettingService){
        $rootScope.endEventIndex = -1;
        var params1 = {q: 'casereport.simulatorPatientsCreated', v: 'full'};
        SystemSettingService.getSystemSettings(params1).then(function(results1){
            if(!results1[0]){
                var params2 = {q: 'casereport.identifierTypeUuid', v: 'full'};
                SystemSettingService.getSystemSettings(params2).then(function(results2){
                    if(results2[0]) {
                        $rootScope.identifierType = results2[0].value;
                    }
                });
            }else {
                $rootScope.patientsCreated = true;
                var params3 = {q: 'casereport.simulatorEndEventIndex', v: 'full'};
                SystemSettingService.getSystemSettings(params3).then(function(results3){
                    if(results3[0]) {
                        $rootScope.endEventIndex = results3[0].value;
                    }
                });
            }
        });

    })

    .controller("SimulatorController", ["$rootScope", "$scope", "$filter", "SimulationService", "Patient", "Obs",

        function($rootScope, $scope, $filter, SimulationService, Patient, Obs){
            $scope.eventIndex = -1;
            $scope.dataset = dataset;
            $scope.artStartUuid = '1255AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
            $scope.startDrugsUuid = '1256AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
            $scope.idPatientUuidMap = {};

            $scope.getEndEventIndex = function(){
                return $rootScope.endEventIndex;
            }

            $scope.run = function(){
                if($scope.eventIndex < 0){
                    return;
                }
                var start = 0;
                var end = $scope.eventIndex+1;
                var events = $scope.dataset.timeline.slice(start, end);
                var count = 0;
                for(var i in events){
                    var identifier = events[i].identifier;
                    SimulationService.getPatientByIdentifier(identifier).then(function(response){
                        var results = response.results;
                        if(results.length == 0){
                            throw Error("No patient found with the identifier: "+identifier);
                        }else if(results.length > 1){
                            throw Error("Found multiple patients with the identifier: "+identifier);
                        }

                        count++;
                        var patient = results[0];
                        $scope.idPatientUuidMap[patient.patientIdentifier.identifier] = patient.uuid;
                        if(count == events.length){
                            createObservations(events);
                        }
                    });
                }
            }

            $scope.displayEvent = function(event){
                var patient = getPatientById(event.identifier);
                var name = patient.givenName+" "+patient.middleName+" "+patient.familyName;
                var date = $scope.formatDate(convertToDate(event.date), 'dd-MMM-yyyy');
                return getEventLabel(event)+" for "+name+" on "+date;
            }

            function getEventLabel(event){
                switch(event.event){
                    case 'artStartDate': {
                        return "Start ART";
                    }
                    case 'cd4Count': {
                        return "CD4 Count of "+event.value;
                    }
                    case 'viralLoad': {
                        return "Viral Load of "+event.value;
                    }
                }
            }

            function getPatientById(id){
                for (var i in $scope.dataset.patients){
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
                return moment().add(offSetInDays, 'days').format('YYYY-MM-DD');
            }

            $scope.patientsCreated = function(){
                return $rootScope.patientsCreated;
            }

            $scope.buildPatient = function(patientData){
                var birthDateStr = $scope.formatDate(convertToDate(patientData.birthdate), 'yyyy-MM-dd');

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

            function createObservations(observations){
                var savedCount = 0;
                for(var i in observations){
                    var obsData = observations[i];
                    var obs = buildObs(obsData, $scope.idPatientUuidMap[obsData.identifier]);
                    Obs.save(obs).$promise.then(function(){
                        savedCount++;
                        if(savedCount == observations.length){
                            SimulationService.saveGlobalProperty('casereport.simulatorEndEventIndex', $scope.eventIndex+"").then(function(){
                                $rootScope.endEventIndex = $scope.eventIndex;
                                $scope.eventIndex = -1;
                                emr.successMessage('Created events successfully');
                            });
                        }
                    });
                }
            }

            function buildObs(obsData, patientUuid) {
                var obsDate = $scope.formatDate(convertToDate(obsData.date), 'yyyy-MM-dd');
                var questionConcept = getObsConcept(obsData);
                var obsValue = obsData.value;
                if(questionConcept == $scope.artStartUuid){
                    obsValue = $scope.startDrugsUuid;
                }

                return {
                    person : patientUuid,
                    concept: questionConcept,
                    value: obsValue,
                    obsDatetime: obsDate
                }
            }

            function getObsConcept(obsData){
                switch(obsData.event){
                    case 'artStartDate': {
                        return $scope.artStartUuid;
                    }
                    case 'cd4Count': {
                        return '5497AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
                    }
                    case 'viralLoad': {
                        return '856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA';
                    }
                }
                
                throw Error("Unknown concept for event "+$scope.displayEvent(obsData));;
            }

        }

    ]);
