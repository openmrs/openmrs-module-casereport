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
        { label: "${ ui.message('casereport.manageCaseReports.label')}" , link: '${ui.pageLink("casereport", "manageCaseReports")}'},
        {label: "${ ui.message("casereport.submittedCaseReports.label")}" }
    ];
    casereport_setUuidDocumentMap(${reportUuidDocumentMap});
</script>

<h2>${ ui.message('casereport.submittedCaseReports.label')}</h2>

<div ng-app="submittedCaseReports" ng-controller="SubmittedCaseReportsController">

    <table id="casereport-submitted">
    <thead>
        <tr>
            <th>${ui.message('Patient.identifier')}</th>
            <th>${ui.message('general.name')}</th>
            <th>${ui.message('Patient.gender')}</th>
            <th>${ui.message('casereport.triggers')}</th>
        </tr>
    </thead>
    <tbody>
        <% caseReports.each {  %>
        <tr ng-click="displayDocument('${it.uuid}')" title="${ui.message("casereport.clickToViewFhirDocument")}">
            <td valign="top">${it.patient.patientIdentifier.identifier}</td>
            <td valign="top">${ui.format(it.patient)}</td>
            <td valign="top">${it.patient.person.gender}</td>
            <td valign="top">${reportUuidSubmittedTriggersMap[it.uuid]}</td>
        </tr>
        <% } %>
    </tbody>
    </table>
</div>

<script type="text/ng-template" id="templateId">
<div id="casereport-document-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ui.message('casereport.submitted.document')}</h3>
    </div>
    <div class="dialog-content">
        <div id="casereport-submitted-document"></div>
        <br />
        <div>
            <button class="confirm">${ ui.message("casereport.close") }</button>
        </div>
    </div>
</div>
</script>

