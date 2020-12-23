<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<%
    ui.decorateWith("appui", "standardEmrPage", [ title: ui.message("casereport.label") ])

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-common-error.js")
    ui.includeJavascript("uicommons", "filters/display.js")
    ui.includeJavascript("uicommons", "services/systemSettingService.js")
    ui.includeJavascript("uicommons", "services/patientIdentifierTypeService.js")
    ui.includeJavascript("casereport", "identifierMappings.js")
    ui.includeCss("casereport", "casereport.css")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: "/" + OPENMRS_CONTEXT_PATH + "/index.htm" },
        { label: "${ ui.message('casereport.label')}" , link: '${ui.pageLink("casereport", "caseReports")}'},
        {label: "${ ui.message("casereport.identifierMappings.label")}" }
    ];
    emr.loadMessages(["casereport.save.success"]);
</script>

<h2>${ ui.message("casereport.identifierMappings.label") }</h2>

<div ng-app="casereports.identifierMappings" ng-controller="IdentifierMappingsController">
    <form class="simple-form-ui" novalidate ng-submit="save()">
        <table id="casereport-idMapping-table">
            <tr>
                <th>${ ui.message("PatientIdentifier.type") }</th>
                <th>${ ui.message("casereport.universalIdentifier") }</th>
            </tr>
            <tr ng-repeat="(key, value) in idMappings">
                <td style="width: 65%">{{ getIdTypeByUuid(key) | omrsDisplay }}</td>
                <td>
                    <input style="float: right" ng-model="idMappings[key]" value="{{ value }}" size="35" />
                </td>
            </tr>
        </table>
        <br />
        <p class="casereport-small-faint">
            <i>
                Each listed local identifier type should be mapped to its own identifier domain in OpenEMPI,
                each value must match the Universal Identifier field of the mapped identifier domain in OpenEMPI.
            </i>
        </p>
        <br />
        <div class="casereport-info">
            <i class="icon-info-sign" style="font-size: medium"></i> NOTE: You might have to create new identifier domains in OpenEMPI that map to your local ones.
        </div>
        <br />
        <p>
            <button type="submit" class="confirm right">
                ${ui.message('general.save')}
            </button>
        </p>
    </form>
</div>