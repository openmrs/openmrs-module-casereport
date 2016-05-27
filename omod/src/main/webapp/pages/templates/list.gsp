<table>
    <thead>
    <tr>
        <th>${ui.message('general.patient')}</th>
        <th>${ui.message('casereports.status')}</th>
        <th>${ui.message('casereports.triggers')}</th>
        <th>${ui.message('casereports.actions')}</th>
    </tr>
    </thead>
    <tbody>
    <tr ng-repeat="caseReport in caseReports">
        <td valign="top">{{caseReport.patient.person | omrs.display}}</td>
        <td valign="top">{{caseReport.status}}</td>
        <td valign="top">{{caseReport.reportTriggers | omrs.display}}</td>
        <td valign="top">
            <a ui-sref="submit({caseReportUuid: caseReport.uuid})">
                <i class="icon-signout edit-action" title="${ui.message("casereports.submit")}"></i>
            </a>
            <a ng-click="dismiss(caseReport)">
                <i class="icon-thumbs-down delete-action" title="${ui.message("casereports.dismiss")}"></i>
            </a>
        </td>
    </tr>
    </tbody>
</table>