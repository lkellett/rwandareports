package org.openmrs.module.rwandareports.reporting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
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
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.HIVPediAlerts;
import org.openmrs.module.rwandareports.customcalculator.NextCD4;
import org.openmrs.module.rwandareports.filter.DrugDosageFrequencyFilter;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.InformedStateFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.RemoveDecimalFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupPediHIVConsultationSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties
	private Program pediProgram;
	
	private ProgramWorkflow informed;
	
	private ProgramWorkflow counsellingWorkflow;
	
	private List<EncounterType> pediEncounters;
	
	private EncounterType pediFlowsheet;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediHIVConsultationSheetV2.xls",
		    "PediHIVConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
		
		ReportDesign design2 = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediPreArtHIVConsultationSheet.xls",
		    "PediPreArtHIVConsultationSheet.xls_", null);
		
		Properties props2 = new Properties();
		props2.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
		
		design2.setProperties(props2);
		h.saveReportDesign(design2);
		
		ReportDesign design3 = h.createRowPerPatientXlsOverviewReportDesign(rd, "BactrimSheet.xls", "Bactrim.xls_", null);
		
		Properties props3 = new Properties();
		props3.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
		
		design3.setProperties(props3);
		h.saveReportDesign(design3);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PediHIVConsultationSheet.xls_".equals(rd.getName())
			        || "PediPreArtHIVConsultationSheet.xls_".equals(rd.getName()) || "Bactrim.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Pedi HIV Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Pedi HIV Consultation Sheet");
		
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		
		//reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class));
		//This is waiting on changes to the reporting framework to allow for filtering of the state parameter
		//so the user is only presented with the treatment group options
		Properties stateProperties = new Properties();
		stateProperties.setProperty("Program", pediProgram.getName());
		stateProperties.setProperty("Workflow", Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW));
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
		
		dataSetDefinition.addFilter(Cohorts.createInCurrentStateParameterized("hiv group", "states"),
		    ParameterizableUtil.createParameterMappings("states=${state},onDate=${now}"));
		
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("pediHIV: In Program", pediProgram),
		    ParameterizableUtil.createParameterMappings("onDate=${now}"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		PatientProperty age = RowPerPatientColumns.getAge("age");
		dataSetDefinition.addColumn(age, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentTbTest("RecentTB", "@ddMMMyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("DOB", "dd-MMM-yyyy", "yyyy"),
		    new HashMap<String, Object>());
		
		MostRecentObservation cd4Test = RowPerPatientColumns.getMostRecentCD4("CD4Test", "dd-MMM-yyyy",
		    new RemoveDecimalFilter());
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
		MostRecentObservation cd4Percent = RowPerPatientColumns.getMostRecentCD4Percentage("CD4Percent", "dd-MMM-yyyy");
		dataSetDefinition.addColumn(cd4Percent,
		    new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions nextCD4 = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		nextCD4.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		nextCD4.setName("nextCD4");
		nextCD4.setCalculator(new NextCD4());
		dataSetDefinition.addColumn(nextCD4, new HashMap<String, Object>());
		
		MostRecentObservation weight = RowPerPatientColumns.getMostRecentWeight("weight", "dd-MMM-yyyy",
		    new RemoveDecimalFilter());
		dataSetDefinition.addColumn(weight, new HashMap<String, Object>());
		
		MostRecentObservation height = RowPerPatientColumns.getMostRecentHeight("height", "dd-MMM-yyyy",
		    new RemoveDecimalFilter());
		dataSetDefinition.addColumn(height, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getRecentEncounterType("lastEncounter", pediEncounters, "dd-MMM-yyyy", null),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Sector", false, true, false, false),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Cell", false, false, true, false),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentViralLoad("viralLoad", "dd-MMM-yyyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getCurrentTBOrders("TB Treatment", "dd-MMM-yyyy", new DrugNameFilter()),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getStateOfPatient("informed", pediProgram, informed, new InformedStateFilter()),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getStateOfPatient("counselling", pediProgram, counsellingWorkflow, null),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getCurrentARTOrders("Regimen", "dd-MMM-yyyy", new DrugDosageFrequencyFilter()),
		    new HashMap<String, Object>());
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = RowPerPatientColumns.getDrugOrderForStartOfART("StartART", "dd-MMM-yyyy");
		dataSetDefinition.addColumn(startArt,
		    new HashMap<String, Object>());
		
		AllObservationValues allWeights = RowPerPatientColumns.getAllWeightValues("weightObs", "ddMMMyy",
		    new LastThreeObsFilter(), null);
		
		AllObservationValues allCD4 = RowPerPatientColumns.getAllCD4Values("allCD4Obs", "ddMMMyy",
		    null, null);
		
		ObservationInMostRecentEncounterOfType io = RowPerPatientColumns.getIOInMostRecentEncounterOfType("IO",
		    pediFlowsheet);
		
		ObservationInMostRecentEncounterOfType sideEffect = RowPerPatientColumns.getSideEffectInMostRecentEncounterOfType(
		    "SideEffects", pediFlowsheet);
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(allWeights, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(age, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(height, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(cd4Percent, new HashMap<String, Object>());
		alert.setCalculator(new HIVPediAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions cd4Decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		cd4Decline.setName("cd4Decline");
		cd4Decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		cd4Decline.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		DeclineHighestCD4 declineCD4 = new DeclineHighestCD4();
		declineCD4.setInitiationArt("StartART");
		cd4Decline.setCalculator(declineCD4);
		dataSetDefinition.addColumn(cd4Decline, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("state", "${state}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setUpProperties() {
		pediProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		informed = gp.getProgramWorkflow(GlobalPropertiesManagement.INFORMED_STATUS,
		    GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		pediEncounters = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
		
		counsellingWorkflow = gp.getProgramWorkflow(GlobalPropertiesManagement.COUNSELLING_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		pediFlowsheet = gp.getEncounterType(GlobalPropertiesManagement.PEDI_FLOWSHEET_ENCOUNTER);
	}
	
}
