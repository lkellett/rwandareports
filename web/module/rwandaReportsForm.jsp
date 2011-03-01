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
HIV Art Register report:	
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_hivartregister.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_hivartregister.form">Remove</a>
<hr></hr>
<h3>PMTCT Reports</h3>
Combined HFCSP Consultation Sheet:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_combinedHSCSPConsultation.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_combinedHSCSPConsultation.form">Remove</a>
<br></br>	
Food Distribution:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFoodDistribution.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFoodDistribution.form">Remove</a>	
<hr></hr>
<h3>Late Visit And CD4</h3>
Adult Late Visit And CD4:
	<a href="${pageContext.request.contextPath}/module/rwandareports/register_adultLatevisitAndCD4.form">(Re) register</a>
	<a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultLatevisitAndCD4.form">Remove</a>	
<br></br>	
Pediatric Late Visit And CD4:
<hr></hr>

<%@ include file="/WEB-INF/template/footer.jsp"%>


