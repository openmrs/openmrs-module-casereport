<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<script type="text/javascript">
    emr.loadMessages([
        "casereport.submitted",
        "casereport.dismissed"
    ]);
</script>

<h2>${ ui.message('casereport.report.form.title')}</h2>

<form class="simple-form-ui" name="caseReportForm" novalidate ng-submit="submitCaseReport()">
    <table class="casereport-form-table" cellpadding="0" cellspacing="0">
        <tr>
            <th valign="top">${ui.message("casereport.givenName")}</th>
            <td valign="top">{{ caseReport.reportForm.givenName }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("casereport.middleName")}</th>
            <td valign="top">{{ caseReport.reportForm.middleName }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("PersonName.familyName")}</th>
            <td valign="top">{{ caseReport.reportForm.familyName }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("Person.gender")}</th>
            <td valign="top">{{ caseReport.reportForm.gender }}</td>
        </tr>
        <tr>
            <th valign="top">${ui.message("casereport.triggers")}</th>
            <td valign="top">{{ caseReport.reportForm.reportTriggers | omrs.display }}</td>
        </tr>
    </table>

    <br />
    <br />
    <p>
        <button type="submit" class="confirm right">${ui.message('general.submit')}</button>
        <button type="button" class="cancel" ui-sref="list">${ui.message('general.cancel')}</button>
    </p>
</form>