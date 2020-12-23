<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<script type="text/javascript">
    emr.loadMessages(["casereport.item.save.success"]);
</script>

<h2>${ ui.message("casereport.caseReportQueueItemForm.title")}</h2>

<form class="simple-form-ui" name="caseReportQueueItemForm" novalidate ng-submit="saveNewQueueItem()">
    <p>
        <label>${ui.message("general.patient")}</label>
    </p>
    <p>
        <div class="casereport-margin-left">{{ patient | omrsDisplay }}</div>
    </p>
    <p>
        <label for="casereport-triggers">
            ${ ui.message("casereport.trigger") }
            <span>
            (${ ui.message("emr.formValidation.messages.requiredField.label") })
            </span>
        </label>
    </p>
    <p>
        <div class="casereport-margin-left">
            <select id="casereport-triggers" name="trigger" ng-model="trigger" required>
                <option value="">${ui.message("casereport.selectTrigger")}</option>
                <option ng-repeat="t in triggers" value="{{ t.name }}" ng-disabled="hasItemWithTrigger(t)">
                    {{ t.name }}
                </option>
            </select>
        </div>
    </p>

    <br />
    <p>
        <button type="submit" class="right confirm" ng-disabled="caseReportQueueItemForm.\$invalid">
            ${ui.message("general.save")}
        </button>
        <button type="button" class="cancel" ui-sref="list">${ui.message("general.cancel")}</button>
    </p>
</form>