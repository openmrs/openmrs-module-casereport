<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<div id="tasks">
    <a class="button app big" ui-sref="queue">
        <div class="task">
            <i class="icon-item"></i>
            ${ ui.message("casereport.caseReportQueue.label") }
        </div>
    </a>
    <a class="button app big" ui-sref="submitted">
        <div class="task">
            <i class="icon-item"></i>
            ${ ui.message("casereport.submittedCaseReports.label") }
        </div>
    </a>
</div>