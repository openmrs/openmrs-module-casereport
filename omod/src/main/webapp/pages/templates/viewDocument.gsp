<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->
<h2>${ui.message('casereport.submitted.document')}</h2>

<br />

<div class="button-group right" id="button-group-right">
    <label class="button" onclick="javascript:window.open('http://brynlewis.org/challenge/index.htm', '_blank');">
        <i class="icon-external-link edit-action"></i> ${ui.message("casereport.cdaViewer")}
    </label>
    <label class="button" onclick="javascript:window.open('https://www.lantanagroup.com/validator/Validator', '_blank');">
        <i class="icon-external-link edit-action"></i> ${ui.message("casereport.cdaValidator")}
    </label>
</div>

<div class="button-group">
    <label class="button" ui-sref="list" title="${ui.message("general.back")}">
        <i class="icon-chevron-left edit-action"></i>
    </label>
    <label class="button" ng-click="downloadCdaDoc()" title="${ui.message("general.download")}">
        <i class="icon-download-alt edit-action"></i>
    </label>
    <label class="button" ng-click="selectCdaDoc()" title="${ui.message("casereport.selectAll")}" ng-show="false">
        <i class="icon-check edit-action"></i>
    </label>
    <label class="button" ng-click="copyCdaDoc()" title="${ui.message("casereport.copy")}">
        <i class="icon-copy edit-action"></i>
    </label>
</div>
<br />
<br />
<pre id="casereport-document-panel" ng-bind-html="cdaDocument"></pre>