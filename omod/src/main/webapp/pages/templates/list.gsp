<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<table>
    <thead>
        <tr>
            <th class="casereport-name-column">${ui.message('general.name')}</th>
            <th>${ui.message('Patient.identifier')}</th>
            <th>${ui.message('Patient.gender')}</th>
            <th>${ui.message('casereport.triggers')}</th>
            <th>${ui.message('casereport.actions')}</th>
        </tr>
    </thead>
    <tbody>
    <tr ng-repeat="caseReport in caseReports">
        <td valign="top">{{caseReport.patient.person.personName.display}}
            <span ng-show="{{caseReport.status == 'DRAFT'}}" class="casereport-draft-lozenge">
                ${ui.message("casereport.draft")}
            </span>
        </td>
        <td valign="top">{{caseReport.patient.patientIdentifier.identifier}}</td>
        <td valign="top">{{caseReport.patient.person.gender}}</td>
        <td valign="top">{{caseReport.reportTriggers | omrs.display}}</td>
        <td valign="top">
            <a ui-sref="submit({caseReportUuid: caseReport.uuid})">
                <i class="icon-external-link edit-action" title="${ui.message("casereport.submit")}"></i>
            </a>
            <a ng-click="dismiss(caseReport)">
                <i class="icon-thumbs-down delete-action" title="${ui.message("casereport.dismiss")}"></i>
            </a>
        </td>
    </tr>
    </tbody>
</table>