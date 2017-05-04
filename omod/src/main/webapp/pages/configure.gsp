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
    ui.includeJavascript("uicommons", "angular-sanitize.min.js")
    ui.includeJavascript("uicommons", "filters/display.js")
    ui.includeJavascript("uicommons", "services/systemSettingService.js")
    ui.includeJavascript("uicommons", "services/providerService.js")
    ui.includeJavascript("uicommons", "services/patientIdentifierTypeService.js")
    ui.includeJavascript("casereport", "configure.js");

    ui.includeCss("casereport", "casereport.css");
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: "/" + OPENMRS_CONTEXT_PATH + "/index.htm" },
        { label: "${ ui.message('casereport.label')}" , link: '${ui.pageLink("casereport", "caseReports")}'},
        {label: "${ ui.message("casereport.configure.label")}" }
    ];
    emr.loadMessages(["casereport.save.success"]);
</script>

<div id="casereports-configure" ng-app="casereports.configure" ng-controller="ConfigController">
    <form class="simple-form-ui" name="configForm" novalidate ng-submit="save()">
        <p>
            <button ng-disabled="configForm.\$invalid" type="submit" class="confirm right">
                ${ui.message('general.save')}
            </button>
        </p>
        <span class="casereport-red">* </span>${ui.message("casereport.indicates.requiredField")}
        <br />
        <br />
        <table class="casereport-form-table">
            <tr ng-repeat="setting in settings track by \$index">
                <td class="casereport-text-left" valign="top">
                    {{ printProperty(setting) }}
                    <span class="casereport-red" ng-show="isRequired(setting)">*</span>
                    <br />
                    <span class="casereport-small-faint" ng-switch="setting.property">
                        <span ng-switch-when="casereport.healthCareFacilityTypeCode"
                            ng-bind-html="setting.description">
                        </span>
                        <span ng-switch-when="casereport.practiceSettingCode"
                              ng-bind-html="setting.description">
                        </span>
                        <span ng-switch-default>
                            {{ printDescription(setting) }}
                        </span>
                    </span>
                </td>
                <td class="casereport-text-left" valign="top">
                    <div ng-switch="setting.property">
                        <select ng-switch-when="casereport.autoSubmitProviderUuid"
                                name="{{ setting.property }}"
                                ng-model="settings[\$index].value" ng-required="isRequired(setting)">
                            <option value=""></option>
                            <option ng-repeat="p in providers" value="{{ p.uuid }}"
                                    ng-selected="p.uuid == setting.value">
                                {{ p | omrs.display }}
                            </option>
                        </select>
                        <select ng-switch-when="casereport.confidentialityCode"
                                name="{{ setting.property }}"
                                ng-model="settings[\$index].value"
                                ng-required="isRequired(setting)">
                            <option value="" ng-disabled="true"></option>
                            <option ng-repeat="conf in confidentialityCodes" value="{{ conf.value }}"
                                    ng-selected="conf.value == setting.value">
                                {{ conf.label }}
                            </option>
                        </select>
                        <select ng-switch-when="casereport.identifierTypeUuid"
                                name="{{ setting.property }}"
                                ng-model="settings[\$index].value"
                                ng-required="isRequired(setting)">
                            <option value="" ng-disabled="true"></option>
                            <option ng-repeat="iType in identifierTypes" value="{{ iType.uuid }}"
                                    ng-selected="iType.uuid == setting.value">
                                {{ iType | omrs.display }}
                            </option>
                        </select>
                        <input ng-switch-default name="{{ setting.property }}" value="{{ setting.value }}"
                               ng-model="settings[\$index].value" size="43" ng-required="isRequired(setting)" />
                    </div>
                </td>
            </tr>
        </table>
        <br />
        <p>
            <button ng-disabled="configForm.\$invalid" type="submit" class="confirm right">
                ${ui.message('general.save')}
            </button>
        </p>
    </form>
</div>

