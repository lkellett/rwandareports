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
.tabletd{
font-weight: bold;
background-color: #EEE;
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
<td>Categories</td>
<td>Report Name</td>
<td>Run</td>
<td colspan="2"><center>Action</center></td>
</tr>
<tr>
<td rowspan="2" class="tabletd">PIH Reports</td>
<td>PIH Quarterly Cross Site Indicator Report	
</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossSiteIndicator.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossSiteIndicator.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PIH Quarterly Cross for individual Site Indicator Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossRegionIndicator.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossRegionIndicator.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="3" class="tabletd">Registers</td>
<td>Adult HIV Art Register report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adulthivartregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adulthivartregister.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Pedi HIV Art Register report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pedihivartregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pedihivartregister.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>TRAC Mother-Infant Pair Follow-up Register report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctregister.form" onclick=msgrem(this)>Remove</a></td>	 
</tr>
<tr>
<td class="tabletd">Adult HIV Program</td>
<td>Adult HIV Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adultHIVConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultHIVConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>	 
</tr>
<tr>
<td rowspan="4" class="tabletd">PMTCT Reports</td>
<td>Combined HFCSP Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_combinedHSCSPConsultation.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_combinedHSCSPConsultation.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Food Distribution</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFoodDistributionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFoodDistributionSheet.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Formula Distribution</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFormulaDistributionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFormulaDistributionSheet.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Pregnancy Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctPregnancyConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctPregnancyConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td rowspan="2" class="tabletd">Patient Follow-up</td>
<td>Adult Late Visit And CD4</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adultLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a></td>	
</tr><tr>
<td>Pediatric Late Visit And CD4</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pediatricLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pediatricLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td class="tabletd">Primary Care</td>
<td>Primary Care Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_rwandaPrimaryCareReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_rwandaPrimaryCareReport.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td rowspan="2" class="tabletd">Heart Failure</td>
<td>Heart Failure Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureReport.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Heart Failure Report for all sites</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureReportAllSites.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureReportAllSites.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td rowspan="3" class="tabletd">Data Quality</td>
<td>Missing CD4 Report by site</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4Report.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4Report.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Missing CD4 Report for all sites</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4AllSiteReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4AllSiteReport.form" onclick=msgrem(this)>Remove</a></td>	
</tr>
<tr>
<td>Data Quality per site</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_dataQualityReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_dataQualityReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
</table>
<%@ include file="/WEB-INF/template/footer.jsp"%>


