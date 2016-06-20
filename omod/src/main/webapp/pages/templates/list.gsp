<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<h2>${ ui.message('casereport.manageCaseReports.label')}</h2>

<input class="right" placeholder="${ui.message('casereport.searchByTriggers')}" />
<input ng-model="search.patient.person.personName.display"
       placeholder="${ui.message('general.searchByPatient')}" />
<br />
<br />
<table id="casereport-reports">
    <thead>
        <tr>
            <th>${ui.message('Patient.identifier')}</th>
            <th class="casereport-name-column">${ui.message('general.name')}</th>
            <th>${ui.message('Patient.gender')}</th>
            <th>${ui.message('Person.age')}</th>
            <th>${ui.message('casereport.triggers')}</th>
            <th>${ui.message('casereport.actions')}</th>
        </tr>
    </thead>
    <tbody>
    <tr ng-repeat="caseReport in caseReports | filter:search">
        <td valign="top">{{caseReport.patient.patientIdentifier.identifier}}</td>
        <td valign="top">{{caseReport.patient.person.personName.display}}
            <span ng-show="{{caseReport.status == 'DRAFT'}}" class="casereport-draft-lozenge">
                ${ui.message("casereport.draft")}
            </span>
        </td>
        <td valign="top">{{caseReport.patient.person.gender}}</td>
        <td valign="top">{{caseReport.patient.person.age}}</td>
        <td valign="top">
            <table class="casereport-form-table" cellpadding="0" cellspacing="0">
                <tr ng-class="{'casereport-border-bottom' : caseReport.reportTriggers.length > 1 && !\$last}"
                    ng-repeat="trigger in caseReport.reportTriggers">
                    <td ng-class="{'casereport-focus-element' : \$parent.\$odd}">
                        {{trigger | omrs.display}}
                        <span class="casereport-small-faint">{{trigger.auditInfo.dateCreated | serverDate}}</span>
                    </td>
                </tr>
            </table>
        </td>
        <td valign="top">
            <a ui-sref="reportForm({uuid: caseReport.uuid, status: caseReport.status})">
                <i class="icon-external-link edit-action" title="${ui.message("casereport.submit")}"></i>
            </a>
            <a ng-click="dismiss(caseReport)">
                <i class="icon-thumbs-down delete-action" title="${ui.message("casereport.dismiss")}"></i>
            </a>
        </td>
    </tr>
    </tbody>
</table>