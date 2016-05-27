<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-ui/angular-ui-router.min.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-common-error.js")
    ui.includeJavascript("uicommons", "filters/display.js")
    ui.includeJavascript("casereport", "caseReportService.js")
    ui.includeJavascript("casereport", "casereports.js")

    ui.includeCss("casereport", "casereports.css")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: "/" + OPENMRS_CONTEXT_PATH + "/index.htm" },
        {label: "${ ui.message("casereport.app.label")}" }
    ];
</script>

<h2>${ ui.message('casereport.manageCaseReports.label')}</h2>

<div id="manage-casereports">
    <ui-view/>
</div>

<script type="text/javascript">
    angular.bootstrap("#manage-casereports", [ "manageCaseReports" ])
</script>