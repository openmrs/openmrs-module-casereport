<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<%
    ui.decorateWith("appui", "standardEmrPage", [ title: ui.message("casereport.submittedCaseReports.label") ])

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("casereport", "submittedCaseReports.js");

    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeCss("casereport", "casereports.css")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: "/" + OPENMRS_CONTEXT_PATH + "/index.htm" },
        { label: "${ ui.message('casereport.label')}" , link: '${ui.pageLink("casereport", "caseReports")}'},
        {label: "${ ui.message("casereport.submittedCaseReports.label")}" }
    ];
    casereport_setUuidDocumentMap(${reportUuidDocumentMap});
</script>

<h2>${ ui.message('casereport.submittedCaseReports.label')}</h2>

<div ng-app="submittedCaseReports" ng-controller="SubmittedCaseReportsController">
    <table id="casereport-submitted">
    <thead>
        <tr>
            <th>${ui.message('casereport.date')}</th>
            <th>${ui.message('Patient.identifier')}</th>
            <th class="casereport-name-column">${ui.message('general.name')}</th>
            <th>${ui.message('Patient.gender')}</th>
            <th class="casereport-trigger-column">${ui.message('casereport.triggers')}</th>
        </tr>
    </thead>
    <tbody>
        <% caseReports.each {  %>
        <tr ng-click="displayDocument('${it.uuid}')" title="${ui.message("casereport.clickToViewFhirDocument")}">
            <td valign="top">${ui.formatDatePretty(it.dateChanged)}</td>
            <td class="casereport-identifier-column" valign="top">${it.patient.patientIdentifier.identifier}</td>
            <td valign="top">${ui.format(it.patient)}</td>
            <td valign="top">${it.patient.person.gender}</td>
            <td valign="top">${reportUuidSubmittedTriggersMap[it.uuid]}</td>
        </tr>
        <% } %>
    </tbody>
    </table>
</div>

<% if ( caseReports.size() > 0 ) { %>
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#casereport-submitted",
          options: [
                  bFilter: true,
                  bJQueryUI: true,
                  bLengthChange: false,
                  iDisplayLength: 15,
                  sPaginationType: '\'full_numbers\'',
                  bSort: false,
                  sDom: '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
          ]
]) }
<% } %>

