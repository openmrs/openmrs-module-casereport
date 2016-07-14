<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<div class="dialog-header">
    <h3>${ui.message("casereport.dismiss.title")}</h3>
</div>
<div class="dialog-content">
    <h4>{{ '${ui.message("casereport.dismiss.confirm")}'.replace('{0}', caseReport.patient.person.personName.display) }}</h4>
    <br/>
    <div>
        <button class="confirm right" ng-click="confirm('')">${ ui.message("uicommons.confirm") }</button>
        <button class="cancel" ng-click="closeThisDialog()">${ ui.message("general.cancel") }</button>
    </div>
</div>