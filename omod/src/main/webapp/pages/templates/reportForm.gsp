<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<script type="text/javascript">
    emr.loadMessages(["casereport.submitted"]);
    emr.loadMessages(["casereport.report.form.title"]);
</script>

<h2 id="casereport-reportTitle" />

<form class="simple-form-ui" name="caseReportForm" novalidate ng-submit="submitCaseReport()">
    {{ updateFormTitle(caseReport.patient.person.display) }}
    <table class="casereport-form-table" cellpadding="0" cellspacing="0">
        <tr>
            <th valign="top">${ui.message("general.name")}</th>
            <td valign="top">{{ caseReport.patient.person.display }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("Patient.identifier")}</th>
            <td valign="top">{{ caseReport.reportForm.patientIdentifier }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("Person.gender")}</th>
            <td valign="top">{{ caseReport.reportForm.gender }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("casereport.birthdate")}</th>
            <td valign="top">
                <span ng-show="caseReport.reportForm.dead" class="right">${ui.message("casereport.deathdate")}:
                {{ formatDate(caseReport.reportForm.deathdate) }}
                </span> {{ formatDate(caseReport.reportForm.birthdate) }}
            </td>
        </tr>
        <tr ng-show="caseReport.reportForm.dead && caseReport.reportForm.causeOfDeath">
            <th valign="top">${ui.message("casereport.causeOfDeath")}</th>
            <td valign="top">{{ formatDate(caseReport.reportForm.causeOfDeath) }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("casereport.triggers")}</th>
            <td valign="top">{{ getObjectKeys(caseReport.reportForm.triggerAndDateCreatedMap) | omrs.display }}</td>
        </tr>
        <tr ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndViralLoadMap) > 0
                    || getMapSize(caseReport.reportForm.mostRecentDateAndCd4CountMap) > 0
                    || getMapSize(caseReport.reportForm.mostRecentDateAndHivTestMap) > 0">
            <th valign="top">${ui.message("casereport.data")}</th>
            <td valign="top">
                <br />
                <table id="casereport-data-table" cellpadding="0" cellspacing="0">
                    <tr ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndViralLoadMap) > 0">
                        <th valign="top">${ui.message("casereport.viralLoad")}</th>
                        <td valign="top" ng-repeat="key in getObjectKeys(caseReport.reportForm.mostRecentDateAndViralLoadMap)">
                            {{ caseReport.reportForm.mostRecentDateAndViralLoadMap[key] }} <span class="casereport-small-faint">({{ key | serverDate}})</span>
                        </td>
                        <td ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndViralLoadMap) < 3"></td>
                        <td ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndViralLoadMap) == 1"></td>
                    </tr>
                    <tr ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndCd4CountMap) > 0">
                        <th valign="top">${ui.message("casereport.cd4Count")}</th>
                        <td valign="top" ng-repeat="key in getObjectKeys(caseReport.reportForm.mostRecentDateAndCd4CountMap)">
                            {{ caseReport.reportForm.mostRecentDateAndCd4CountMap[key] }} <span class="casereport-small-faint">({{ key | serverDate}})</span>
                        </td>
                        <td ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndCd4CountMap) < 3"></td>
                        <td ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndCd4CountMap) == 1"></td>
                    </tr>
                    <tr ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndHivTestMap) > 0">
                        <th valign="top">${ui.message("casereport.hivTest")}</th>
                        <td valign="top" ng-repeat="key in getObjectKeys(caseReport.reportForm.mostRecentDateAndHivTestMap)">
                            {{ caseReport.reportForm.mostRecentDateAndHivTestMap[key] }} <span class="casereport-small-faint">({{ key | serverDate}})</span>
                        </td>
                        <td ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndHivTestMap) < 3"></td>
                        <td ng-show="getMapSize(caseReport.reportForm.mostRecentDateAndHivTestMap) == 1"></td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr ng-show="caseReport.reportForm.mostRecentHivWhoStage">
            <th valign="top">${ui.message("casereport.whoClassification")}</th>
            <td valign="top">{{ caseReport.reportForm.mostRecentHivWhoStage }}</td>
        </tr>
        <tr ng-show="caseReport.reportForm.currentHivMedications.length > 0">
            <th valign="top">${ui.message("casereport.arvs")}</th>
            <td valign="top">{{ caseReport.reportForm.currentHivMedications | omrs.display }}</td>
        </tr>
        <tr ng-show="caseReport.reportForm.mostRecentArvStopReason">
            <th valign="top">${ui.message("casereport.reasonArvsStopped")}</th>
            <td valign="top">{{ caseReport.reportForm.mostRecentArvStopReason }}</td>
        </tr>
        <tr ng-show="caseReport.reportForm.lastVisitDate">
            <th valign="top">${ui.message("casereport.lastVisit")}</th>
            <td valign="top">{{ caseReport.reportForm.lastVisitDate | serverDate}}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("casereport.signature")}</th>
            <td valign="top">${ui.format(context.authenticatedUser.person)}</td>
        </tr>
    </table>

    <br />
    <br />
    <p>
        <button type="submit" class="confirm right">${ui.message('general.submit')}</button>
        <button type="button" class="cancel" ui-sref="list">${ui.message('general.cancel')}</button>
    </p>
</form>