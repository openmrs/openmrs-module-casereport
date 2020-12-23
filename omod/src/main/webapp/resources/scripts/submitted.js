/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

angular.module("casereports.submitted", [
        "ngSanitize",
        "caseReportService",
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
                templateUrl: "templates/submittedList.page",
                controller: "SubmittedCaseReportsController"
            })
            .state('document', {
                url: "/document/:uuid",
                templateUrl: "templates/viewDocument.page",
                controller: "ViewDocumentController",
                params: {
                    uuid: null
                },
                resolve: {
                    submittedDocument: function($stateParams, CaseReportService){
                        return CaseReportService.getSubmittedDocument($stateParams.uuid).then(function(response){
                            return response;
                        });
                    }
                }
            })
            
    }])

    .controller("SubmittedCaseReportsController", ["$scope", "$state", "ngDialog", "CaseReportService",

        function ($scope, $state, ngDialog, CaseReportService) {
            $scope.caseReports = [];
            $scope.searchText = null;
            $scope.currentPage = 1;
            $scope.itemsPerPage = 10;
            $scope.start = 0;
            $scope.end = 0;

            var customRep = 'custom:(resolutionDate,uuid,patient:(patientIdentifier:(identifier),' +
                'person:(gender,age,personName:(display))),reportForm)';

            CaseReportService.getSubmittedCaseReports({v: customRep}).then(function(results) {
                $scope.caseReports = results;
                $scope.effectiveCount = $scope.caseReports.length;
            });

            $scope.showSubmittedDocument = function(caseReport){
                $state.go('document', {
                    uuid: caseReport.uuid
                });
            }
        }

    ])

    .controller("ViewDocumentController", ["$scope", "submittedDocument",

        function($scope, submittedDocument){

            $scope.cdaContainerId = 'casereport-document-panel';
            $scope.cda = submittedDocument;
           
            if (submittedDocument.contents.includes("ClinicalDocument")) {            	
            	$scope.cdaDocument = beautifyXml(submittedDocument);
           	 	//Append the root closing tag since it's discarded from the last token when beautifying the doc
            	$scope.cdaDocument += ("<span class='casereport-element'>&lt;/ClinicalDocument&gt;</span>\n");
			}
			else {
			    document.getElementById("button-group-right").style.display = "none";
				$scope.cdaDocument = JSON.stringify(JSON.parse(submittedDocument.contents), null, 2);
			}
			
            $scope.downloadCdaDoc = function(){
                var blob = new Blob([ $scope.cda.contents], { type : 'application/xml;charset=utf-8' });
                (blob, "cda.xml");
            }

            $scope.selectCdaDoc = function(){
                if (document.selection) { // IE
                    var range = document.body.createTextRange();
                    range.moveToElementText(document.getElementById($scope.cdaContainerId ));
                    range.select();
                } else if (window.getSelection) {
                    var range = document.createRange();
                    range.selectNode(document.getElementById($scope.cdaContainerId ));
                    window.getSelection().removeAllRanges();
                    window.getSelection().addRange(range);
                }
            }


            $scope.copyCdaDoc = function(){
                $scope.selectCdaDoc();
                try {
                    var copied = document.execCommand('copy');
                    if (!copied) {
                        alert('Failed to copy to the document contents to the clipboard!');
                    }
                } catch (err) {
                    alert("Ooops! Looks like your browser doesn't support this feature, please try another browser");
                }
            }
            
            

            function beautifyXml(xmlDoc){
                //Getting fancy with some cool decoration of the xml doc
                var cda = "";
                var tokens = xmlDoc.contents.split(" ");
                for(var i in tokens){
                    var token = tokens[i];
                    if(token.indexOf('<') == 0) {
                        //This is a token that starts with a tag
                        var tagName = token.substring(1);
                        if(token.indexOf('</') == -1 && token.indexOf('>') == -1){
                            //Start tag with attributes e.g <ClinicalDocument
                            cda += ("<span class='casereport-element'>&lt;" + tagName + "</span> ");
                        }else {
                            var tagAndText = token.split(">");
                            var tagName = tagAndText[0].substring(1);
                            if(token.indexOf('</') == 0){
                                //This is a closing tag e.g </name>
                                tagName = tagName.substring(1);
                                cda += ("<span class='casereport-element'>&lt;/" + tagName + "&gt;</span>\n");
                            }else {
                                //Token is a start tag with no attributes, e.g <name>, <name>Super, <name>Super</name>
                                var startTag = "<" + tagName + ">";
                                var startTagIndex = token.indexOf(startTag);
                                var encodedStartTag = "<span class='casereport-element'>&lt;" + tagName + "&gt;</span>";
                                var endTagIndex = token.indexOf("</" + tagName + ">");
                                if(endTagIndex == -1 ){
                                    //Token start tag followed by text e.g <name>Super
                                    cda += (encodedStartTag + tagAndText[1] + " ");
                                }else {
                                    //Token contains text inside a tag e.g <given>Wilhelmine</given>
                                    var tagContent = token.substring(startTagIndex + startTag.length, endTagIndex);
                                    var encodedEndTag = "<span class='casereport-element'>&lt;/" + tagName + "&gt;</span>\n";
                                    cda += (encodedStartTag + tagContent + encodedEndTag);
                                }
                            }
                        }
                    }else if(token.indexOf('="') > 0) {
                        //This is an attribute e.g code="UV", code="UV">, code="UV"/>
                        var closingTag = "";
                        var closingTagReplace = "";
                        if(token.endsWith('>\n') || token.endsWith('/>\n')){
                            closingTagReplace += "<span class='casereport-element'>";
                            if(token.endsWith('/>\n')){
                                closingTag = "/>\n";
                                closingTagReplace += "/>\n";
                            }else{
                                closingTag = ">\n";
                                closingTagReplace += ">\n";
                            }
                            closingTagReplace += "</span>";
                        }

                        var attribAndValue = token.split("=");
                        var attribute = "<span class='casereport-attribute'>"+attribAndValue[0]+"</span>";
                        var value = "="+attribAndValue[1].replace(closingTag, '');

                        cda += (attribute + value + closingTagReplace+" ");

                    }else if(token.indexOf('</') > 0 && token.endsWith('>\n')) {
                        //Token ends with text and a closing tag e.g Super</given>
                        var textAndClosingTag = token.split("</");
                        var closingTag = textAndClosingTag[1];
                        var tagName = closingTag.substring(0, closingTag.indexOf('>\n'));
                        var encodedEndTag = "<span class='casereport-element'>&lt;/" + tagName + "&gt;</span>\n";

                        cda += (textAndClosingTag[0] + encodedEndTag+" ");
                    }else {
                        cda += (token+" ");
                    }
                }

                return cda;
            }

        }
        
    ])

    .filter('mainFilter', function ($filter) {

        return function (caseReports, $scope) {

            var matches = [];
            if(!$scope.searchText){
                matches = caseReports;
            }else {
                matches = $filter('searchReportsByPatient')(caseReports, $scope.searchText);
            }

            $scope.effectiveCount = matches.length;
            //apply paging so that we only see a single page of results
            return $filter('pagination')(matches, $scope);
        }

    });