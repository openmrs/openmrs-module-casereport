<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: "/" + OPENMRS_CONTEXT_PATH + "/index.htm" },
        { label: "${ ui.message('casereport.label')}" , link: '${ui.pageLink("casereport", "caseReports")}'},
        {label: "${ ui.message("casereport.submittedCaseReports.label")}" }
    ];
</script>

<h2>${ ui.message('casereport.submittedCaseReports.label')}</h2>

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
    <tr ng-repeat="cr in caseReports | orderBy:'resolutionDate':true" ng-click="" title="${ui.message("casereport.clickToViewDocument")}">
        <td valign="top">{{ cr.resolutionDate | serverDate }}</td>
        <td class="casereport-identifier-column" valign="top">{{ cr.patient.patientIdentifier.identifier }}</td>
        <td valign="top">{{ cr.patient.person.personName.display }}</td>
        <td valign="top">{{ cr.patient.person.gender }}</td>
        <td valign="top">{{ cr.reportForm.triggers | omrs.display }}</td>
    </tr>
    </tbody>
</table>