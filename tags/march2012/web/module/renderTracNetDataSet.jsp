<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>
<%@ include file="/WEB-INF/view/module/reporting/include.jsp"%>
<%@ include file="/WEB-INF/view/module/reporting/includeScripts.jsp"%>

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />

<!--  <openmrs:require privilege="View TRACNet Reporting" otherwise="/login.htm" redirect="/module/@MODULE_ID@/tracnetreportingForm.list" /> -->

<openmrs:htmlInclude file="/scripts/calendar/calendar.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/jquery-1.3.2.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/styles/ui.all.css" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/styles/demos.css" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui.core.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui.accordion.js" />
<openmrs:htmlInclude file="/moduleResources/@MODULE_ID@/scripts/ui.tabs.js" />


	<script type="text/javascript">

	$(function() {
		$("#accordion1").accordion({collapsible: true, autoHeight: false});
	});

	$(function() {
		$("#accordion2").accordion({collapsible: true, autoHeight: false});
	});

	$(function() {
		$("#tabs").tabs({collapsible: true});
	});
	
</script>
<%--
  This page assumes a ReportData object in the session as the attribute '__openmrs_report_data'
--%>

<c:set var="__openmrs_hide_report_link" value="true" />
<c:set var="dataSetMaps" value="${__openmrs_report_data.dataSets}" />
<c:set var="mapDataSet" value="${dataSetMaps['defaultDataSet'].data}"/>

<openmrs:portlet url="currentReportHeader" moduleId="reporting" parameters="showDiscardButton=true"/>  


<div class="alltables" style="margin-left: 70px">	

<div id="displayIndicators" class="demo"><!-- Tabs that holds the tabs start -->
		<div id="tabs">
			<ul>
				<li><a href="#tabs-1"><spring:message code="rwandareports.tracnetreport.category.preartvariablefortracnet" /></a></li>
				<li><a href="#tabs-2"><spring:message code="rwandareports.tracnetreport.category.artvariables" /></a></li>
			</ul>
			
			<!-- ************************************************************ --> 
			
			<!-- Tab-1 for ART Variables Start -->
			<div id="tabs-1"><!-- accordion-1 Div Start -->
				<div id="accordion1Div" class="demo"><!-- accordion-1 start -->
					<div id="accordion1"><!-- indicatorsPreArtDataElement start -->
		<!-- PEP DATA ELEMENTS -->
	<h3><a href="#section1"><spring:message code="rwandareports.tracnetreport.category.pepdataelem"/></a></h3>
	<div>
	<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivOccupationExposure" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivRapeAssault" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivOtherNoneOccupationExposure" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivOccupationExposurePep" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivRapeAssaultPep" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivOtherNoneOccupationExposurePep" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivOccupExpo3MonthAfterPep" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivRapeAssault3MonthAfterPep" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.newAtRiskHivOtherNoneOccupExpo3MonthAfterPep" />
		</td>
		<c:forEach var="counter" begin="1" end="1" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
		
		</table>
		</div>
		<!-- ANTENATAL DATA ELEMENTS -->
	<h3><a href="#section2"><spring:message code="rwandareports.tracnetreport.category.antenataldataelem"/></a></h3>
	<div>
	<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenUnknownHivFirstAntenatal" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenKnownHivPosFirstAntenatal" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenUnknownHivTested" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosReturnRes" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosTestedCd4" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantHivPosEligibleArvs1" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.negativeWomenReturnRes" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantHivPos" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantHivPosAztProphyAt28Weeks" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 10 -->
	<tr>
		<td>10</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantHivPosTripleTheraProphy" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.10"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 11 -->
	<tr>
		<td bgcolor="#E0ECF8">11</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantHivPosEligibleArvs2" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.11"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 12 -->
	<tr>
		<td>12</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenTestedForRpr" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.12"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 13 -->
	<tr>
		<td bgcolor="#E0ECF8">13</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantTestedPosForRpr" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.13"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 14 -->
	<tr>
		<td>14</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantPartnersTestedForHiv" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.14"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 15 -->
	<tr>
		<td bgcolor="#E0ECF8">15</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.hivNegPregnantPartnersTestedHivPos" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.15"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 16-->
	<tr>
		<td>16</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.discordantCouples1" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td>
			<c:set var="key" value="${counter}.16"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 17 -->
	<tr>
		<td bgcolor="#E0ECF8">17</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.partnersTestedHivPos" />
		</td>
		<c:forEach var="counter" begin="2" end="2" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.17"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
		</table></div>
		<!-- MATERNITY DATA ELEMENTS -->
	<h3><a href="#section3"><spring:message code="rwandareports.tracnetreport.category.maternitydataelem"/></a></h3>
	<div>
		<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.expectedDeliveriesFacilityThisMonth" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.occuringDeliveriesFacilityThisMonth" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.expectedDeliveriesAmongHivPosWomen" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosGivingBirthAtFacility" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.reportedHivPosGivingBirthAtHome" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosAzt3tcNvpDuringLabor" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenReceivingAzt3tcAfterDelivery" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenUnknownHivStatusTestedDuringLabor1" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenUnknownHivStatusTestedPosDuringLabor2" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 10 -->
	<tr>
		<td>10</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantReceivedCompleteCourseThisMonth" />
		</td>
		<c:forEach var="counter" begin="3" end="3" step="1">
		<td>
			<c:set var="key" value="${counter}.10"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	</table>
	</div>
	<!-- HIV INFANT EXPOSED DATA ELEMENTS -->
	<h3><a href="#section4"><spring:message code="rwandareports.tracnetreport.category.hivexposedinfantfollowup"/></a></h3>
	<div>
		<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosBreastFeeding" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosUsingFormula" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersEnrolledPmtct" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantBornToHivNegMothersInCoupleDiscordantEnrolledInPMTCT" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTestedAt6Weeks" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTestedPosAt6Weeks" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersAged6WeeksThisMonth" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersAged6WeeksThisMonthonCOtrimo" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTestedAt9Months" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 10 -->
	<tr>
		<td>10</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTestedPosAt9Months" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.10"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 11 -->
		<tr>
		<td bgcolor="#E0ECF8">11</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersAged9MonthsThisMonth" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.11"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 12 -->
	<tr>
	    <td>12</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTestedAt18Months" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.12"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 13 -->
		<tr>
		<td bgcolor="#E0ECF8">13</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTestedPosAt18Months" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.13"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 14 -->
	<tr>
	    <td>14</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersAgedAt18MonthsThisMonth" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.14"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 15 -->
	<tr>
		<td bgcolor="#E0ECF8">15</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersLostFollowup" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.15"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 16-->
	<tr>
	    <td>16</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersScreenedTbThisMonth" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.16"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 17 -->
	<tr>
		<td bgcolor="#E0ECF8">17</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.reportedDeadInfantHivPosMothers" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.17"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 18 -->
	<tr>
	    <td>18</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersMalnourished" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td>
			<c:set var="key" value="${counter}.18"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 19 -->
	<tr>
		<td bgcolor="#E0ECF8">19</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.infantHivPosMothersTherapFood" />
		</td>
		<c:forEach var="counter" begin="4" end="4" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.19"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	</table>    
	</div>
	
	<!-- FAMILY PLANNING DATA ELEMENTS -->
	<h3><a href="#section5"><spring:message code="rwandareports.tracnetreport.category.familyplandataelem"/></a></h3>
	<div>
	<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosExpectedFpAtFacility" />
		</td>
		<c:forEach var="counter" begin="5" end="5" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosSeenInFp" />
		</td>
		<c:forEach var="counter" begin="5" end="5" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosPartnersSeenInFp" />
		</td>
		<c:forEach var="counter" begin="5" end="5" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosReceivingModernContraceptive" />
		</td>
		<c:forEach var="counter" begin="5" end="5" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.womenHivPosRefferedForFp" />
		</td>
		<c:forEach var="counter" begin="5" end="5" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
		</table>
		</div>
						
					</div><!-- accordion-1 end -->
				</div><!-- accordion-1 Div end -->
			</div><!-- Tab-1 for ART Variables End --> 
			
			<!-- ************************************************************ -->
			
			<!-- Tab-2 for Prevention Tracnet Data Start -->
			<div id="tabs-2"><!-- accordion-2 Div start -->
				<div id="accordion2Div" class="demo"><!-- accordion-2 start -->
					<div id="accordion2">
					
	<!-- PRE-ART DATA ELEMENTS -->      
	<h3><a href="#section6"><spring:message code="rwandareports.tracnetreport.category.preartdataelement"/></a></h3>
	<div>
		<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newPedsUnderEighteenMonthsInHivCare" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newPedsUnderFiveInHivCare" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newFemaleUnderFifteenInHivCare" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newMaleUnderFifteenInHivCare" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newFemaleMoreThanFifteenInHivCare" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newMaleMoreThanFifteenInHivCare" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.pedUnderEighteenMonthsEverInHiv" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.pedsUnderFiveEverInHiv" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.femaleMoreThanFifteenEverInHiv" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 10 -->
	<tr>
		<td>10</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.femalePedsUnderFifteenEverInHiv" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.10"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 11 -->
		<tr>
		<td bgcolor="#E0ECF8">11</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.maleMoreThanFifteenEverInHiv" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.11"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 12 -->
	<tr>
	    <td>12</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.malePedsUnderFifteenEverInHiv" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.12"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 13 -->
		<tr>
		<td bgcolor="#E0ECF8">13</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.patientsOnCotrimoProphylaxis" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.13"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 14 -->
	<tr>
	    <td>14</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.patientsActiveTbAtEnrolThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.14"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 15 -->
	<tr>
		<td bgcolor="#E0ECF8">15</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.patientsTbPositiveAtEnrolThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.15"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 16-->
	<tr>
	    <td>16</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newEnrolledPedsStartTbTreatThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.16"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 17 -->
	<tr>
		<td bgcolor="#E0ECF8">17</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.newEnrolledAdultsStartTbTreatThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.17"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 18 -->
	<tr>
	    <td>18</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.PatientsInPreARVDiedThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.18"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 19 -->
	<tr>
		<td bgcolor="#E0ECF8">19</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.PatientsInPreARVTransferredInThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.19"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 20 -->
	<tr>
	    <td>20</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.PatientsInPreARVTransferredOutThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td>
			<c:set var="key" value="${counter}.20"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 21 -->
	<tr>
		<td bgcolor="#E0ECF8">21</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.preart.PatientsInPreARVTLostToFollowUpThisMonth" />
		</td>
		<c:forEach var="counter" begin="6" end="6" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.21"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	</table>    
	</div>
	<!-- ART DATA ELEMENTS -->   
	<h3><a href="#section7"><spring:message code="rwandareports.tracnetreport.category.artdataelement"/></a></h3>
	<div>
		<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.pedsUnderEighteenMonthsCurrentOnArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.pedsUnderFiveCurrentOnArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.femalePedsUnderFifteenCurrentOnArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.malePedsUnderFifteenCurrentOnArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.pedsOnFirstLineReg" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.pedsOnSecondLineReg" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.femaleMoreThanFifteenCurrentOnArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.maleMoreThanFifteenCurrentOnArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.adultOnFirstLineReg" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 10 -->
	<tr>
		<td>10</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.adultOnSecondLineReg" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.10"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 11 -->
		<tr>
		<td bgcolor="#E0ECF8">11</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsUnderEighteenMonthStartArvThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.11"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 12 -->
	<tr>
	    <td>12</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsUnderFiveStartArvThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.12"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 13 -->
		<tr>
		<td bgcolor="#E0ECF8">13</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newFemalePedsUnderFifteenStartArvThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.13"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 14 -->
	 <tr>
	    <td>14</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newMalePedsUnderFifteenStartArvThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.14"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 15 -->
	<tr>
		<td bgcolor="#E0ECF8">15</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsWhoStageFourThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.15"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 16-->
	<tr>
	    <td>16</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsWhoStageThreeThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.16"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 17 -->
	<tr>
		<td bgcolor="#E0ECF8">17</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsWhoStageTwoThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.17"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 18 -->
	<tr>
	    <td>18</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsWhoStageOneThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.18"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 19 -->
	<tr>
		<td bgcolor="#E0ECF8">19</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newPedsUndefinedWhoStageThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.19"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 20 -->
	<tr>
	    <td>20</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newFemaleAdultStartiArvThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.20"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 21 -->
	<tr>
		<td bgcolor="#E0ECF8">21</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newMaleAdultStartiArvThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.21"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 22 -->
	<tr>
	    <td>22</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newAdultWhoStageFourThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.22"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 23 -->
	<tr>
		<td bgcolor="#E0ECF8">23</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newAdultWhoStageThreeThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.23"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 24 -->
	<tr>
	    <td>24</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newAdultWhoStageTwoThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.24"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 25 -->
	<tr>
		<td bgcolor="#E0ECF8">25</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newAdultWhoStageOneThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.25"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 26 -->      
	<tr>
	    <td>26</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.newAdultUndefinedWhoStageThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.26"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 27 -->
	<tr>
		<td bgcolor="#E0ECF8">27</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvPedsFifteenInterruptTreatThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.27"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 28 -->      
	<tr>
	    <td>28</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvAdultFifteenInterruptTreatThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.28"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 29 -->
	<tr>
		<td bgcolor="#E0ECF8">29</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvPedsDiedThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.29"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 30 -->      
	<tr>
	    <td>30</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvAdultDiedThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.30"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 31 -->
	<tr>
		<td bgcolor="#E0ECF8">31</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvPedsLostFollowupMoreThreeMonths" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.31"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr><!-- indicator 32 -->      
	<tr>
	    <td>32</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvAdultLostFollowupMoreThreeMonths" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.32"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 33 -->
	<tr>
		<td bgcolor="#E0ECF8">33</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.maleOnTreatTwelveAfterInitArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.33"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr><!-- indicator 34 -->      
	<tr>
	    <td>34</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.femaleOnTreatTwelveAfterInitArv" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.34"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 35 -->
	<tr>
		<td bgcolor="#E0ECF8">35</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvPedsTransferredOutThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.35"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr><!-- indicator 36 -->      
	<tr>
	    <td>36</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvAdultTransferredOutThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.36"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 37 -->
	<tr>
		<td bgcolor="#E0ECF8">37</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvPedsTransferredInThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.37"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 38 -->
	<tr>
	    <td>38</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.art.arvAdultTransferreInThisMonth" />
		</td>
		<c:forEach var="counter" begin="7" end="7" step="1">
		<td>
			<c:set var="key" value="${counter}.38"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	</table>    
	</div>
	
	<!-- STI OPPORTUNISTIC INFECTIONS -->
	<h3><a href="#section8"><spring:message code="rwandareports.tracnetreport.category.stiopportandothers"/></a></h3>
	<div>
	<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.clientsCounceledForStiThisMonth" />
		</td>
		<c:forEach var="counter" begin="8" end="8" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.stiDiagnosedThisMonth" />
		</td>
		<c:forEach var="counter" begin="8" end="8" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.opportInfectTreatedExcludeTbThisMonth" />
		</td>
		<c:forEach var="counter" begin="8" end="8" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	</table>
		</div>
	
	<!-- MALNUTRITION DATA ELEMENTS -->
	<h3><a href="#section9"><spring:message code="rwandareports.tracnetreport.category.nutritiondataelem"/></a></h3>
	<div>
	<table>
		<!-- indicator 1 -->
		<tr>
		<td bgcolor="#E0ECF8">1</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pedsUnderFiveSevereMalnutrThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.1"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 2 -->
	<tr>
	    <td>2</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pedsUnderFiveSevereMalnutrTheurapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td>
			<c:set var="key" value="${counter}.2"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	
	<!-- indicator 3 -->
		<tr>
		<td bgcolor="#E0ECF8">3</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pedsUnderFifteenSevMalnutrTherapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.3"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 4 -->
	<tr>
	    <td>4</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.adultSevereMalnutrTherapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td>
			<c:set var="key" value="${counter}.4"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 5 -->
	<tr>
		<td bgcolor="#E0ECF8">5</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pregnantMalnutrTherapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.5"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 6-->
	<tr>
	    <td>6</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.lactatingMalnutrTherapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td>
			<c:set var="key" value="${counter}.6"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 7 -->
	<tr>
		<td bgcolor="#E0ECF8">7</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pedsUnderFiveWithSevMalnutrThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.7"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 8 -->
	<tr>
	    <td>8</td>
		<td class="categories"><spring:message code="rwandareports.tracnetreport.indicator.pedsTherapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td>
			<c:set var="key" value="${counter}.8"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
	<!-- indicator 9 -->
	<tr>
		<td bgcolor="#E0ECF8">9</td>
		<td bgcolor="#E0ECF8" class="categories"><spring:message code="rwandareports.tracnetreport.indicator.adultTherapThisMonth" />
		</td>
		<c:forEach var="counter" begin="9" end="9" step="1">
		<td bgcolor="#E0ECF8">
			<c:set var="key" value="${counter}.9"/>
			<c:url var="url"
				value="/module/reporting/dashboard/viewCohortDataSet.form?savedDataSetKey=defaultDataSet&savedColumnKey=${key}" />
			<a style="text-decoration: underline" href="${url}">
				${mapDataSet.columnValuesByKey[key].value} </a>
		</td>
		</c:forEach>
	</tr>
		
		</table>
		</div>
						
					</div><!-- accordion-2 end -->
				</div><!-- accordion-2 Div end -->
			</div><!-- Tab-2 for Prevention Tracnet Data End --> 
			
			<!-- ************************************************************ -->
			
		</div><!-- CategTabs that holds the tabs end -->
	</div><!--  TabDiv that holds the tabs --> 

<br />
<br />

</div><!-- End alltables -->
<%@ include file="/WEB-INF/template/footer.jsp"%>