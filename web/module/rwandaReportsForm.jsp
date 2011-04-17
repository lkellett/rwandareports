<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>
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

<div id="msg"></div>
<h2>Register Reports for IMB Rwanda</h2>

<h3>Quarterly Cross Site Indicator Reports</h3>
PIH Quarterly Cross Site Indicator Report: 
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossSiteIndicator.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossSiteIndicator.form" onclick=msgrem(this)>Remove</a>
<br></br>
PIH Quarterly Cross for individual Site Indicator Report: 
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossRegionIndicator.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossRegionIndicator.form" onclick=msgrem(this)>Remove</a>
<hr></hr>	
<h3>Register Reports</h3>
Adult HIV Art Register report:	
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_adulthivartregister.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_adulthivartregister.form" onclick=msgrem(this)>Remove</a>
<br></br>
Pedi HIV Art Register report:	
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pedihivartregister.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pedihivartregister.form" onclick=msgrem(this)>Remove</a>
<br></br>
TRAC Mother-Infant Pair Follow-up Register report:	
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctregister.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctregister.form" onclick=msgrem(this)>Remove</a>
<hr></hr>
<h3>PMTCT Reports</h3>
Combined HFCSP Consultation Sheet:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_combinedHSCSPConsultation.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_combinedHSCSPConsultation.form" onclick=msgrem(this)>Remove</a>
<br></br>	
Food Distribution:							   
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFoodDistributionSheet.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFoodDistributionSheet.form" onclick=msgrem(this)>Remove</a>	
<br></br>	
Formula Distribution:							   
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFormulaDistributionSheet.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFormulaDistributionSheet.form" onclick=msgrem(this)>Remove</a>	
<hr></hr>
<h3>Late Visit And CD4</h3>
Adult Late Visit And CD4:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_adultLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a>	
<br></br>	
Pediatric Late Visit And CD4:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pediatricLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pediatricLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a>	

<hr></hr>
<h3>Primary Care Report</h3>
Rwanda Primary Care Report:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_rwandaPrimaryCareReport.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_rwandaPrimaryCareReport.form" onclick=msgrem(this)>Remove</a>	
<hr></hr>
<h3>Heart Failure Report</h3>
Heart Failure Report:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureReport.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureReport.form" onclick=msgrem(this)>Remove</a>	
<hr></hr>
Missing CD4 Report:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4Report.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4Report.form" onclick=msgrem(this)>Remove</a>
<br></br>
Missing CD4 All Sites Report:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4AllSiteReport.form" onclick=msgreg(this)>(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4AllSiteReport.form" onclick=msgrem(this)>Remove</a>		
<hr></hr>

<%@ include file="/WEB-INF/template/footer.jsp"%>


