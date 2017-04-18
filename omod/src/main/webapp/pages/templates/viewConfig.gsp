<!--

    This Source Code Form is subject to the terms of the Mozilla Public License,
    v. 2.0. If a copy of the MPL was not distributed with this file, You can
    obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
    the terms of the Healthcare Disclaimer located at http://openmrs.org/license.

    Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
    graphic logo is a trademark of OpenMRS Inc.

-->

<form class="simple-form-ui" name="configForm" novalidate ng-submit="save()">
    <p>
        <button ng-disabled="configForm.\$invalid" type="submit" class="confirm right">
            ${ui.message('general.save')}
        </button>
    </p>
    <br />
    <br />
    <table class="casereport-form-table">
        <tr ng-repeat="setting in settings">
            <td class="casereport-text-left" valign="top">
                {{ getDisplay(setting.property) }} <br />
            <span class="casereport-small-faint">{{ setting.description }}</span>
            </td>
            <td class="casereport-text-left" valign="top">
                <input value="{{ setting.value }}" size="43" />
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