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
    //ui.includeJavascript("uicommons", "angular-common-error.js")
    //ui.includeJavascript("uicommons", "angular-sanitize.min.js")
    //ui.includeJavascript("uicommons", "filters/display.js")
    ui.includeJavascript("uicommons", "services/systemSettingService.js")
    //ui.includeJavascript("uicommons", "services/providerService.js")
    //ui.includeJavascript("uicommons", "services/patientIdentifierTypeService.js")
    ui.includeJavascript("uicommons", "moment.js")
    ui.includeJavascript("casereport", "simulator.js")
    ui.includeJavascript("casereport", "simulator-dataset-short.js")
    ui.includeJavascript("casereport", "simulationService.js")

    ui.includeCss("casereport", "simulator.css");
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

        <p ng-repeat="event in dataset.timeline track by \$index">
            <input id="{{ \$index }}" type="radio" name="event" ng-model="eventIndex" value="{{ \$index }}" />
            <label for="{{ \$index }}">{{ \$index }}. {{ event.date }} - {{ event.event }}</label>
        </p>

        <br />
        <p>
            <button type="submit">Run</button>
        </p>

    </form>

</div>