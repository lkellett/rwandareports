<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<h2>Register Reports for IMB Rwanda</h2>

<h3>Quarterly Cross Site Indicator Reports</h3>
PIH Quarterly Cross Site Indicator Report: 
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossSiteIndicator.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossSiteIndicator.form">Remove</a>
<br></br>
PIH Quarterly Cross for individual Site Indicator Report: 
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossRegionIndicator.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossRegionIndicator.form">Remove</a>
<hr></hr>	
<h3>Register Reports</h3>
Adult HIV Art Register report:	
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_adulthivartregister.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_adulthivartregister.form">Remove</a>
<br></br>
Pedi HIV Art Register report:	
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pedihivartregister.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pedihivartregister.form">Remove</a>
<hr></hr>
<h3>PMTCT Reports</h3>
Combined HFCSP Consultation Sheet:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_combinedHSCSPConsultation">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_combinedHSCSPConsultation">Remove</a>
<br></br>	
Food Distribution:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFoodDistribution">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFoodDistribution">Remove</a>	
<hr></hr>
<h3>Late Visit And CD4</h3>
Adult Late Visit And CD4:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_adultLatevisitAndCD4.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultLatevisitAndCD4.form">Remove</a>	
<br></br>	
Pediatric Late Visit And CD4:
<hr></hr>
Missing CD4 Report:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4Report.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4Report.form">Remove</a>
<br></br>
Missing CD4 All Sites Report:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4AllSiteReport.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4AllSiteReport.form">Remove</a>		
<hr></hr>

<%@ include file="/WEB-INF/template/footer.jsp"%>


