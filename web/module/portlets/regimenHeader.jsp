<%@ include file="/WEB-INF/template/include.jsp" %>

<script type="text/javascript">

	jQuery(document).ready(function() {
		
		jQuery('#availableCycles').click(function(){ 
			jQuery('#treatmentPlanDialog').dialog('open');
		});

		jQuery('#treatmentPlanDialog').dialog({
			position: 'middle',
			autoOpen: false,
			modal: true,
			title: '<spring:message code="rwandareports.printTreatmentAdminPlan" javaScriptEscape="true"/>',
			width: '50%',
			zIndex: 100,
			buttons: { 'Print': function() { printTreatmentPlan(); },
					   '<spring:message code="general.cancel"/>': function() { jQuery(this).dialog("close"); }
			}
		});	
	});
	
	function printTreatmentPlan()
	{	
		jQuery('#treatmentPlan').submit();
		jQuery('#treatmentPlanDialog').dialog("close");
	}
</script>
	
<div id="availableCycles">
	<c:if test="${!empty model.regimens}">
		<input type="button" id="printPlan"  value="<spring:message code="rwandareports.printTreatmentAdminPlan" />"/> 
	</c:if>	
</div>
					
<div id="treatmentPlanDialog">	
	<div class="box">
		<form id="treatmentPlan" name="treatmentPlan" method="post" action="${pageContext.request.contextPath}/module/rwandaReports/printReport.form">
			<input type="hidden" name="patientId" value="${model.patient.patientId}">
			<input type="hidden" name="report" value="Chemotherapy Treatment Administration Plan">
			<input type="hidden" name="parameters" value="patientId,regimenId">
			<input type="hidden" name="returnPage" value="/module/pihrwanda/apps/oncology/patientDashboard.form?patientId=${model.patient.patientId}"/>	
			<select name="regimenId" id="regimenId">
				<c:forEach items="${model.regimens}" var="drugGroup">
					<option value="${drugGroup.id}"><spring:message code="orderextension.regimen.currentCycleNumber" /> <c:out value="${drugGroup.cycleNumber}"/> <spring:message code="general.of" /> <c:out value="${drugGroup.orderSet.name}"/> <spring:message code="general.dateStart"/>: <openmrs:formatDate date="${drugGroup.firstDrugOrderStartDate}" type="medium" /></option>
				</c:forEach>								
			</select>
		</form>
	</div>
</div>
