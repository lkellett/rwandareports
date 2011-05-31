<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/rwandareports/jquery.js" />
<!-- <script type="text/javascript">
	var $j = jQuery.noConflict(); 
</script> -->
<script type="text/javascript">
function msgreg(){
document.getElementById('msg').innerHTML="<div id='openmrs_msg'>Registering...</div>";
exit();
}
function msgrem(){
	document.getElementById('msg').innerHTML="<div id='openmrs_msg'>Removing...</div>";
	exit();
	}
</script>
<style>
table.reports{
border-collapse: collapse;
border: 1px solid blue;
width: 100%;
} 
td{
border-collapse: collapse;
border: 1px solid blue;
}
.tableheaders{
font-weight: bold;
background-color: #B0C4DE;
}

.alt { background-color: #B0C4DE; }
.altodd { background-color: #EEE; }
.hover { background-color: #DED; }
.althover { background-color: #EFE; }        
</style>
<script type="text/javascript">
$(document).ready(function(){
	$('tr:even').addClass('alt');
	$('tr:even').hover(
			function(){$(this).addClass('hover')},
			function(){$(this).removeClass('hover')}
	);	
	$('tr:odd').addClass('altodd');
	$('tr:odd').hover(
			function(){$(this).addClass('althover')},
			function(){$(this).removeClass('althover')}
	);
});
</script>
<div id="msg"></div>
<h2>Register Reports for IMB Rwanda</h2>
<table class="reports" style="width:100%;">
<tr class="tableheaders">
<td>No</td>
<td>Report Name</td>
<td>Run</td>
<td colspan="2"><center>Action</center></td>
</tr>
<tr>
<td>1.</td>
<td>PIH Quarterly Cross Site Indicator Report	
</td>
<td>All sites</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossSiteIndicator.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossSiteIndicator.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>2.</td>
<td>PIH Quarterly Cross for individual Site Indicator Report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossRegionIndicator.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossRegionIndicator.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>3.</td>
<td>Adult HIV Art Register report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adulthivartregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adulthivartregister.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>4.</td>
<td>Pedi HIV Art Register report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pedihivartregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pedihivartregister.form" onclick=msgrem(this)>Remove</a></td>	
</tr><tr>
<td>5.</td>
<td>TRAC Mother-Infant Pair Follow-up Register report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctregister.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>6.</td>
<td>PMTCT Reports-Combined HFCSP Consultation Sheet</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_combinedHSCSPConsultation.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_combinedHSCSPConsultation.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>7.</td>
<td>PMTCT Reports-Food Distribution</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFoodDistributionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFoodDistributionSheet.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>8.</td>
<td>PMTCT Reports-Formula Distribution</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFormulaDistributionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFormulaDistributionSheet.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>9.</td>
<td>PMTCT Reports-Pregnancy Consultation Sheet</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctPregnancyConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctPregnancyConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>10.</td>
<td>Adult Late Visit And CD4</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adultLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a></td>	
</tr><tr>
<td>11.</td>
<td>Pediatric Late Visit And CD4</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pediatricLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pediatricLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a></td>	
</tr><tr>
<td>12.</td>
<td>Primary Care Report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_rwandaPrimaryCareReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_rwandaPrimaryCareReport.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>13.</td>
<td>Heart Failure Report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureReport.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>14.</td>
<td>Heart Failure Report</td>
<td>All sites</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureReportAllSites.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureReportAllSites.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>15.</td>
<td>Missing CD4 Report</td>
<td>By site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4Report.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4Report.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>16.</td>
<td>Missing CD4 Report</td>
<td>All sites</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4AllSiteReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4AllSiteReport.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
</table>
<%@ include file="/WEB-INF/template/footer.jsp"%>


