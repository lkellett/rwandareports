package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
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
import org.openmrs.module.rwandareports.customcalculator.AsthmaClassificationAlerts;
import org.openmrs.module.rwandareports.customcalculator.BMI;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.HIVAdultAlerts;
import org.openmrs.module.rwandareports.filter.DrugDosageFrequencyFilter;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.LastTwoObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupAsthmaConsultationSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program asthmaProgram;
	
	private EncounterType flowsheetAsthmas;
	
	private int asthmaDDBFormId ;
	
	private Concept returnVisitDate;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "AsthmaConsultationSheet.xls",
		    "AsthmaConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("AsthmaConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Asthma Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Asthma Consultation Sheet");
				
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Asthma Consultation Dataset");
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		//dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+asthmaProgram.getName(), asthmaProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
		
		dataSetDefinition.addFilter(getMondayToSundayPatientReturnVisit(), null);
		
		
		//Add Columns
		
	
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("return visit date", null), new HashMap<String, Object>());
		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPeakFlow("Last peak flow", "@ddMMMyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getCurrentAsthmaOrders("Regimen", "@ddMMMyy", new DrugDosageFrequencyFilter()),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"), new HashMap<String, Object>());
		
		
		AllObservationValues asthmaClassification = RowPerPatientColumns.getAllAsthmaClassificationValues("asthmaClassification", null, new LastTwoObsFilter(),
		    null);
		
		
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(asthmaClassification, new HashMap<String, Object>());
		alert.setCalculator(new AsthmaClassificationAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		
	
		
		/*dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentWeight("RecentWeight", "@ddMMMyy"),
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
		    flowsheetAsthmas);
		
		ObservationInMostRecentEncounterOfType sideEffect = RowPerPatientColumns.getSideEffectInMostRecentEncounterOfType(
		    "SideEffects", flowsheetAsthmas);
		
		AllObservationValues allCD4 = RowPerPatientColumns.getAllCD4Values("allCD4Obs", "ddMMMyy",
		    null, null);
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = RowPerPatientColumns.getDrugOrderForStartOfART("StartART", "dd-MMM-yyyy");
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(mostRecentHeight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.setCalculator(new HIVAdultAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bmi = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bmi.setName("bmi");
		bmi.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		bmi.addPatientDataToBeEvaluated(mostRecentHeight, new HashMap<String, Object>());
		bmi.setCalculator(new BMI());
		dataSetDefinition.addColumn(bmi, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions cd4Decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		cd4Decline.setName("cd4Decline");
		cd4Decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		cd4Decline.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		DeclineHighestCD4 declineCD4 = new DeclineHighestCD4();
		declineCD4.setInitiationArt("StartART");
		cd4Decline.setCalculator(declineCD4);
		dataSetDefinition.addColumn(cd4Decline, new HashMap<String, Object>());
		*/
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
		
		flowsheetAsthmas = gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
		
		asthmaDDBFormId=gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB).getFormId();
		
		returnVisitDate=gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
	}
	
	
	private SqlCohortDefinition getMondayToSundayPatientReturnVisit() {
	    SqlCohortDefinition cohortquery=new SqlCohortDefinition();
	    cohortquery.setQuery("select o.person_id from obs o,(select * from (select * from encounter where (form_id="+asthmaDDBFormId+" or encounter_type="+flowsheetAsthmas.getEncounterTypeId()+") order by encounter_datetime desc) as ordred_enc group by ordred_enc.patient_id) as last_enc where o.encounter_id=last_enc.encounter_id and last_enc.voided=0 and o.voided=0 and o.concept_id="+returnVisitDate.getConceptId()+" and o.value_datetime>=(select DATE_FORMAT(CURDATE()+(- (select IF(DAYOFWEEK(CURDATE())=1,6,DAYOFWEEK(CURDATE())-2) as sun)),'%Y-%m-%d')) and o.value_datetime<=(select DATE_FORMAT(CURDATE()+(- (select IF(DAYOFWEEK(CURDATE())=1,6,DAYOFWEEK(CURDATE())-2) as sun)+6),'%Y-%m-%d')) order by o.value_datetime");
	    return cohortquery;
    }
	
	
	
}
