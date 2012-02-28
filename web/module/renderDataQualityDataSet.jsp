<%@ include file="/WEB-INF/view/module/reporting/include.jsp"%>
<%@ include file="/WEB-INF/view/module/reporting/includeScripts.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<script type="text/javascript" src="reporting/web/resources/scripts/jquery/jquery-1.3.2.min.js"></script>        
<style type="text/css">
   .alt { background-color: #EEE; }
   .hover { background-color: #DED; }
   .althover { background-color: #EFE; }        
</style>

<script type="text/javascript">
$(document).ready(function(){
   $('.display tr:even').addClass('alt');
   $('.display tr:even').hover(
           function(){$(this).addClass('hover')},
           function(){$(this).removeClass('hover')}
   );    
   $('.display tr:odd').hover(
           function(){$(this).addClass('althover')},
           function(){$(this).removeClass('althover')}
   );
});
</script>


<!-- Start break table scripts-->

<style type="text/css">
       #page { margin: 0px; } 
       #cohortHeader { padding-left: 5px; background-color: #003366; height: 2em; border-bottom: 1px solid black;
       vertical-align:middle; width: 100%; 
       line-height:2em; font-size: 1.5em; font-weight: bold; color: white; }         
       .cohortResultsColumn { width: 100%; height: auto; text-align: left; margin: 0px; padding-left: 5px; padding-top: 5px;border-collapse: collapse;border: 1px solid black; } 
       #accordion { width: 100%; } 
       table { width: 100%; } 
       .profileImage { width: 75px; height: auto; }
       #cohort-details-table_wrapper { width: 75%; } 
       #cohort-details-table { border: 0px; } 
</style>

<script type="text/javascript" charset="utf-8">
$(document).ready(function() {
       $('.dataset-preview-table').dataTable( {
               "bPaginate": true,
               //"sPaginationType": "full_numbers",
               "iDisplayLength": 5,
               "bLengthChange": false,
               "bFilter": true,
               "bSort": true,
               "bInfo": true,
               "bAutoWidth": true,
               "oLanguage": {
                       "oPaginate": {
                               "sPrevious": "<spring:message code="general.previous" javaScriptEscape="true"/>",
                               "sNext": "<spring:message code="general.next" javaScriptEscape="true"/>",
                       },
                       "sInfo": "<spring:message code="SearchResults.viewing" javaScriptEscape="false"/> _START_ - _END_ <spring:message code="SearchResults.of" javaScriptEscape="false"/> _TOTAL_ ",
                       "sSearch": "<spring:message code="general.search" javaScriptEscape="false"/>"
               }                
               
               //"sDom": '<"top"i>rt<"bottom"flp<"clear">'
       } );        

       $("#show-columns").click(function(event){ 
               // eventually will show/hide appropriate columns based on selected dataset definition
       });

} );
</script>
<script type="text/javascript">
$(function() {

       $('#cohort-breakdown-table').dataTable( {
             //  "iDisplayLength": 5,
               "bPaginate": false,
               "bLengthChange": false,
               "bFilter": false,
               "bSort": false,
               "bInfo": false,
               "bAutoWidth": false
       } );
       
       $("#accordion").accordion();
       $('#cohort-tabs').tabs();
       $('#cohort-tabs').show();        

       $('#cohort-details-table').dataTable(
                        {
                               "iDisplayLength": 5,                                 
                               "bPaginate": true,
                               "bLengthChange": false,
                               "bFilter": true,
                               "bSort": false,
                               "bInfo": true,
                               "bAutoWidth": true
                               } 
       );

});
</script>

<style>
       input,select { font-size: medium; } 
       legend { font-size: large; } 
       label { font-size: medium; font-weight: bold; } 
       .desc { display: block; }
       .divDimension {width: 100%; height: 20%;overflow: auto;bgcolor:#E0ECF8 }
</style>



<!-- End break table scripts -->

<%--
 This page assumes a ReportData object in the session as the attribute '__openmrs_report_data'
--%>

<c:set var="__openmrs_hide_report_link" value="true" />
<c:set var="dataSetMaps" value="${__openmrs_report_data.dataSets}" />
<c:set var="mapDataSet" value="${dataSetMaps['defaultDataSet'].data}"/> 
<openmrs:portlet url="currentReportHeader" moduleId="reporting" parameters="showDiscardButton=true"/>

<div id="page" style="display:block;">
   <div id="container">
       <div id="portal">
       <c:forEach var="cohort" items="${dQRList}" varStatus="cohortNum">
       <c:set var="patientsNumInOneDataSet" value="${fn:length(cohort.dataSet.rows)}" />
       <c:set var="patientsNumInAllDataSets" value="${patientsNumInAllDataSets + patientsNumInOneDataSet}" />                
       </c:forEach>
       <br /><h3>There are ${patientsNumInAllDataSets} total data problems</h3> <br /><br />
        <c:forEach var="cohortResults" items="${dQRList}" varStatus="loopTimes">
           <div class="cohortResultsColumn">                
               <div class="divDimension">        
                   <h3>${cohortResults.selectedColumn.name}: ${cohortResults.selectedColumn.label} (${fn:length(cohortResults.dataSet.rows)} <spring:message code="Patient.header"/>)</h3>
                   
                   <c:if test="${!empty cohortResults.dataSet}">
                       <!-- <table class="dataset-preview-table" class="display"> -->
                       <table class="display">
                           <thead>
                                                       
                               <tr>
                                   <c:forEach var="column" items="${cohortResults.dataSet.metaData.columns}" varStatus="varStatus">                
                                       <c:if test="${column.label!='patientId'}">
                                       <th>
                                   <!-- <br /><br /> -->
                                           ${column.label}
                                       </th>
                                       </c:if>
                                   </c:forEach>
                               </tr>
                           </thead>
                           <tbody>                        
                                   <c:forEach var="dataSetRow" items="${cohortResults.dataSet.rows}" varStatus="varStatus">
                                       <c:set var="patId" value="${dataSetRow.columnValuesByKey['patientId']}"/>
                                       <tr>
                                           <c:forEach var="column" items="${cohortResults.dataSet.metaData.columns}" varStatus="varStatus">
                                               <c:if test="${column.label!='patientId'}">
                                               <td>
                                                   <c:if test="${!empty patId}"><a href="${pageContext.request.contextPath}/patientDashboard.form?patientId=${patId}" target="_blank"></c:if>
                                                       ${dataSetRow.columnValues[column]}
                                                   <c:if test="${!empty patId}"></a></c:if>
                                               </td>
                                               </c:if>
                                           </c:forEach>                                        
                                       </tr>
                                   </c:forEach>
                           </tbody>
                           <tfoot>
                           </tfoot>
                       </table>
                   </c:if>
               </div>    
                               
           </div><!-- column -->
           <br clear="both"/>
           </c:forEach>
       
       </div><!-- portal -->
   </div><!-- container -->
</div><!-- page -->


<%@ include file="/WEB-INF/template/footer.jsp"%>