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
    ui.includeJavascript("uicommons", "filters/serverDate.js")
    ui.includeJavascript("uicommons", "services/systemSettingService.js")
    ui.includeJavascript("uicommons", "services/obsService.js")
    ui.includeJavascript("uicommons", "services/personService.js")
    ui.includeJavascript("uicommons", "moment.js")
    ui.includeJavascript("casereport", "simulator.js")
    ui.includeJavascript("casereport", "simulator-dataset-short.js")
    ui.includeJavascript("casereport", "simulationService.js")
    ui.includeJavascript("casereport", "lib/bootstrap/ui-bootstrap-tpls-2.2.0.min.js")

    ui.includeCss("casereport", "simulator.css")
    ui.includeCss("casereport", "lib/bootstrap/bootstrap.min.css")
    ui.includeCss("casereport", "casereport.css")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: "/" + OPENMRS_CONTEXT_PATH + "/index.htm" },
        { label: "${ ui.message('casereport.label')}" , link: '${ui.pageLink("casereport", "caseReports")}'},
        { label: "Simulator" }
    ];
</script>

<h2>Simulator</h2>

<div id="casereport-simulator-boot" ng-app="casereport.simulator.boot" ng-controller="BootController">
    Loading simulation data<span id="casereport-blinker">.....</span>
</div>

<div id="casereport-simulator" ng-controller="SimulatorController">

    <form class="simple-form-ui" name="simulatorForm" novalidate ng-submit="run()">

        <p>
            <button ng-disabled="patientsCreated()" type="button" ng-click="createPatients()">Create Patients</button>
        </p>

        <table id="casereport-simulator-table">
            <thead>
                <tr>
                    <th></th>
                    <th style="text-align: center">Events</th>
                </tr>
            </thead>
            <tbody>
                <tr ng-repeat="event in dataset.timeline | pagination:this track by \$index">
                    <td style="width:50px !important;" valign="middle">
                        <input id="{{ \$index }}" type="radio" name="eventIndex" ng-model="\$parent.eventIndex"
                            ng-value="{{ \$index }}" ng-hide="\$index <= getEndEventIndex()" />
                    </td>
                    <td>
                        <label for="{{ \$index }}" ng-hide="\$index <= getEndEventIndex()">
                            {{ displayEvent(event) }}
                        </label>
                    </td>
                </tr>
            </tbody>
        </table>
        <br>
        ${ ui.includeFragment("casereport", "pagination") }

        <br>
        <p>
            <button type="submit">Run</button>
        </p>

    </form>

</div>