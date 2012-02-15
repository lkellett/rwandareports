package org.openmrs.module.rwandareports.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rwandareports.customcalculator.Alerts;
import org.openmrs.module.rwandareports.customcalculator.BreastFeedingOrFormula;
import org.openmrs.module.rwandareports.customcalculator.DDR;
import org.openmrs.module.rwandareports.customcalculator.DPA;
import org.openmrs.module.rwandareports.customcalculator.DecisionDate;
import org.openmrs.module.rwandareports.customcalculator.GestationalAge;
import org.openmrs.module.rwandareports.dataset.comparator.PMTCTDataSetRowComparator;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;
import org.openmrs.module.rwandareports.filter.RemoveDecimalFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupPMTCTPregnancyConsultationReport {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties
	private Program pmtct;
	
	private Concept hivTest;
	
	private Concept hivPositiveAnswer;
	
	private Concept ddrConcept;
	
	private Concept dpaConcept;
	
	private EncounterType flowsheetEncounter;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTPregnancyConsultationSheetV2.xls",
		    "PMTCTPregnancyConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:5,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTPregnancyConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PMTCT Pregnancy consultation");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Pregnancy consultation");
		
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		dataSetDefinition.setComparator(new PMTCTDataSetRowComparator());
		
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("pmtct: In Program", pmtct),
		    ParameterizableUtil.createParameterMappings("onDate=${now}"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstRecordedObservationWithCodedConceptAnswer("hivDiagnosis",
		    hivTest, hivPositiveAnswer, "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");
		
		MostRecentObservation ddr = RowPerPatientColumns.getMostRecent("ddr", ddrConcept, null, dateFilter);
		
		MostRecentObservation dpa = RowPerPatientColumns.getMostRecent("dpa", dpaConcept, null, dateFilter);
		
		MostRecentObservation cd4Test = RowPerPatientColumns.getMostRecentCD4("CD4Test", "dd-MMM-yyyy",
		    new RemoveDecimalFilter());
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Sector", false, true, true, true),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Cell", false, false, true, false),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Umudugudu", false, false, false, true),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("District", true, false, false, false),
		    new HashMap<String, Object>());
		
		CurrentOrdersRestrictedByConceptSet artDrugs = RowPerPatientColumns.getCurrentARTOrders("Regimen", "dd-MMM-yyyy",
		    new DrugNameFilter());
		dataSetDefinition.addColumn(artDrugs, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, dateFilter),
		    new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions gestationalAge = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		gestationalAge.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		gestationalAge.setName("gestationalAge");
		gestationalAge.setDescription("gestationalAge");
		gestationalAge.setCalculator(new GestationalAge());
		dataSetDefinition.addColumn(gestationalAge, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions decisionDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		decisionDate.addPatientDataToBeEvaluated(artDrugs, new HashMap<String, Object>());
		decisionDate.setName("decisionDate");
		decisionDate.setDescription("decisionDate");
		decisionDate.setCalculator(new DecisionDate());
		dataSetDefinition.addColumn(decisionDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions ddrDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		ddrDate.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		ddrDate.addPatientDataToBeEvaluated(dpa, new HashMap<String, Object>());
		ddrDate.setName("ddrCalc");
		ddrDate.setDescription("ddrCalc");
		ddrDate.setCalculator(new DDR());
		dataSetDefinition.addColumn(ddrDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions dpaDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		dpaDate.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		dpaDate.addPatientDataToBeEvaluated(dpa, new HashMap<String, Object>());
		dpaDate.setName("dpaCalc");
		dpaDate.setDescription("dpaCalc");
		dpaDate.setCalculator(new DPA());
		dataSetDefinition.addColumn(dpaDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bOrF = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bOrF.setName("bOrF");
		bOrF.addPatientDataToBeEvaluated(decisionDate, new HashMap<String, Object>());
		bOrF.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		bOrF.setCalculator(new BreastFeedingOrFormula());
		dataSetDefinition.addColumn(bOrF, new HashMap<String, Object>());
		
		AllObservationValues weight = RowPerPatientColumns.getAllWeightValues("weightObs", "ddMMMyy",
		    new LastThreeObsFilter(), new ObservationFilter());
		
		ObservationInMostRecentEncounterOfType io = RowPerPatientColumns.getIOInMostRecentEncounterOfType("IO",
		    flowsheetEncounter);
		
		ObservationInMostRecentEncounterOfType sideEffect = RowPerPatientColumns.getSideEffectInMostRecentEncounterOfType(
		    "SideEffects", flowsheetEncounter);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(gestationalAge, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.setCalculator(new Alerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setUpProperties() {
		
		pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT);
		
		hivTest = gp.getConcept(GlobalPropertiesManagement.HIV_TEST);
		
		hivPositiveAnswer = gp.getConcept(GlobalPropertiesManagement.POSITIVE_HIV_TEST_ANSWER);
		
		ddrConcept = gp.getConcept(GlobalPropertiesManagement.DDR);
		
		dpaConcept = gp.getConcept(GlobalPropertiesManagement.DPA);
		
		flowsheetEncounter = gp.getEncounterType(GlobalPropertiesManagement.ADULT_FLOWSHEET_ENCOUNTER);
	}
	
}
