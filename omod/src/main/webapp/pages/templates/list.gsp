<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<h2>${ ui.message('casereport.caseReportQueue.label')}</h2>

<input ng-model="patientSearchText" placeholder="${ui.message('casereport.searchByPatient')}" />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input ng-model="triggerSearchText" placeholder="${ui.message('casereport.filterByTriggers')}" />
<br />
<br />
<table id="casereport-reports">
    <thead>
        <tr>
            <th>
                ${ui.message('casereport.dateAdded')}
                <a ng-click="sort('dateCreated')">
                    <i ng-class="{'icon-sort edit-action' : propertyName != 'dateCreated',
                                'icon-sort-up' : propertyName == 'dateCreated' && !reverse,
                                'icon-sort-down' : propertyName == 'dateCreated' && reverse}" />
                </a>
            </th>
            <th>
                ${ui.message('Patient.identifier')}
                <a ng-click="sort('patient.patientIdentifier.identifier')">
                    <i ng-class="{'icon-sort edit-action' : propertyName != 'patient.patientIdentifier.identifier',
                                'icon-sort-up' : propertyName == 'patient.patientIdentifier.identifier' && !reverse,
                                'icon-sort-down' : propertyName == 'patient.patientIdentifier.identifier' && reverse}" />
                </a>
            </th>
            <th class="casereport-name-column">
                ${ui.message('general.name')}
                <a ng-click="sort('patient.person.personName.display')">
                    <i ng-class="{'icon-sort edit-action' : propertyName != 'patient.person.personName.display',
                                'icon-sort-up' : propertyName == 'patient.person.personName.display' && !reverse,
                                'icon-sort-down' : propertyName == 'patient.person.personName.display' && reverse}" />
                </a>
            </th>
            <th>${ui.message('Patient.gender')}
                <a ng-click="sort('patient.person.gender')">
                    <i  ng-class="{'icon-sort edit-action' : propertyName != 'patient.person.gender',
                                'icon-sort-up' : propertyName == 'patient.person.gender' && !reverse,
                                'icon-sort-down' : propertyName == 'patient.person.gender' && reverse}" />
                </a>
            </th>
            <th>${ui.message('Person.age')}
                <a ng-click="sort('patient.person.age')">
                    <i  ng-class="{'icon-sort edit-action' : propertyName != 'patient.person.age',
                                'icon-sort-up' : propertyName == 'patient.person.age' && !reverse,
                                'icon-sort-down' : propertyName == 'patient.person.age' && reverse}" />
                </a>
            </th>
            <th class="casereport-trigger-column">${ui.message('casereport.triggers')}</th>
            <th>${ui.message('casereport.actions')}</th>
        </tr>
    </thead>
    <tbody>
    <tr ng-repeat="caseReport in caseReports | searchReportsByPatient:patientSearchText | searchReportsByTrigger:triggerSearchText">
        <td valign="top">{{caseReport.dateCreated | serverDate}}</td>
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
                    ng-repeat="trigger in caseReport.reportTriggers | searchTriggers:triggerSearchText">
                    <td ng-class="{'casereport-focus-element' : \$parent.\$odd}">
                        {{trigger | omrs.display}}
                        <span class="casereport-small-faint">{{trigger.auditInfo.dateCreated | serverDate}}</span>
                    </td>
                </tr>
            </table>
        </td>
        <td valign="top">
            <a ui-sref="reportForm({uuid: caseReport.uuid, status: caseReport.status})">
                <i class="icon-circle-arrow-right edit-action" title="${ui.message("casereport.submit")}"></i>
            </a>
            <a ng-click="dismiss(caseReport)">
                <i class="icon-remove delete-action" title="${ui.message("casereport.dismiss")}"></i>
            </a>
        </td>
    </tr>
    </tbody>
</table>