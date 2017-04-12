<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<div id="casereport-pagination">
    <ul class="right" uib-pagination
        total-items="effectiveCount"
        ng-model="currentPage"
        items-per-page="itemsPerPage"
        max-size="10"
        boundary-link-numbers="true"
        previous-text="${ui.message('casereport.previous')}"
        next-text="${ui.message('casereport.next')}"
        force-ellipses="true"
        rotate="true" />
    <span id="casereport-showing" class="left">
        ${ui.message("casereport.showingLabel").replace('{0}', '{{effectiveCount > 0 ? (start + 1) : start}}')
                .replace('{1}', '{{end}}').replace('{2}', '{{effectiveCount}}')}
    </span>
</div>