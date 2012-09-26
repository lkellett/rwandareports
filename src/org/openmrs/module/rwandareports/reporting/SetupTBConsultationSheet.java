package org.openmrs.module.rwandareports.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rwandareports.customcalculator.HIVAdultAlerts;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupTBConsultationSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties
	private Program tbProgram;
	
	private EncounterType flowsheetAdult;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "TBConsultationSheetV2.xls",
		    "TBConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("TBConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("TB-Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("TB-Consultation Sheet");
		
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		
		//reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class));
		//This is waiting on changes to the reporting framework to allow for filtering of the state parameter
		//so the user is only presented with the treatment group options
		Properties stateProperties = new Properties();
		stateProperties.setProperty("Program", tbProgram.getName());
		stateProperties.setProperty("Workflow", Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.TB_TREATMENT_GROUP_WORKFLOW));
		reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class, stateProperties));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		dataSetDefinition.addParameter(new Parameter("state", "State", ProgramWorkflowState.class));
		
		dataSetDefinition.addFilter(Cohorts.createInCurrentStateParameterized("in state", "states"),
		    ParameterizableUtil.createParameterMappings("states=${state},onDate=${now}"));
		
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("adultTB: In Program", tbProgram),
		    ParameterizableUtil.createParameterMappings("onDate=${now}"));
		
		//Add Columns
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentWeight("RecentWeight", "@ddMMMyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentTbTest("RecentTB", "@ddMMMyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentCD4("CD4Test", "@ddMMMyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentViralLoad("ViralLoad", "@ddMMMyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getCurrentARTOrders("Regimen", "@ddMMMyy", new DrugNameFilter()),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getCurrentTBOrders("TB Treatment", "@ddMMMyy", new DrugNameFilter()),
		    new HashMap<String, Object>());
		
		//Calculation definitions
		MostRecentObservation mostRecentHeight = RowPerPatientColumns.getMostRecentHeight("RecentHeight", null);
		
		AllObservationValues weight = RowPerPatientColumns.getAllWeightValues("weightObs", "ddMMMyy",
		    new LastThreeObsFilter(), new ObservationFilter());
		
		AllObservationValues cd4Test = RowPerPatientColumns.getAllCD4Values("CD4Test", "ddMMMyy", new LastThreeObsFilter(),
		    new ObservationFilter());
		
		ObservationInMostRecentEncounterOfType io = RowPerPatientColumns.getIOInMostRecentEncounterOfType("IO",
		    flowsheetAdult);
		
		ObservationInMostRecentEncounterOfType sideEffect = RowPerPatientColumns.getSideEffectInMostRecentEncounterOfType(
		    "SideEffects", flowsheetAdult);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(mostRecentHeight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.setCalculator(new HIVAdultAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("state", "${state}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setUpProperties() {
		tbProgram = gp.getProgram(GlobalPropertiesManagement.TB_PROGRAM);
		
		flowsheetAdult = gp.getEncounterType(GlobalPropertiesManagement.ADULT_FLOWSHEET_ENCOUNTER);
	}
	
}
