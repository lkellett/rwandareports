package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rwandareports.customcalculators.DrugOrderDateManipulation;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupHIVResearchDataQualitySheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Concept cd4;
	
	private Concept weight;
	
	private ProgramWorkflowState onArt;
	
	private ProgramWorkflowState onArtPedi;
	
	List<ProgramWorkflowState> pws = new ArrayList<ProgramWorkflowState>();
	
	List<EncounterType> transferEncounter = new ArrayList<EncounterType>();
	
	private Concept art;
	
	private Program pmtct;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "HIVResearchDataQuality.xls",
		    "HIVResearchDataQuality", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:dataSet|sheet:1,row:6,dataset:BaselineCD4PatientDataSet|sheet:1,row:11,dataset:BaselineWeightPatientDataSet|sheet:1,row:16,dataset:TransferPatientsPatientDataSet");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("HIVResearchDataQuality".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("HIV Research Data Quality");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("HIV Research Data Quality");
		
		createDataSetDefinition(reportDefinition);
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		reportDefinition.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName("BaselineCD4");
		
		PatientDataSetDefinition dataSetDefinition2 = new PatientDataSetDefinition();
		dataSetDefinition2.setName("BaselineWeight");
		
		PatientDataSetDefinition dataSetDefinition3 = new PatientDataSetDefinition();
		dataSetDefinition3.setName("TransferPatients");
		
		CompositionCohortDefinition onAllArt = new CompositionCohortDefinition();
		onAllArt.setName("onArt");
		onAllArt.getSearches().put(
		    "adult",
		    new Mapped<CohortDefinition>(Cohorts.createPatientStateCohortDefinition("onArt", onArt), new HashMap<String, Object>()));
		onAllArt.getSearches().put(
		    "pedi",
		    new Mapped<CohortDefinition>(Cohorts.createPatientStateCohortDefinition("onArt", onArtPedi), new HashMap<String, Object>()));
		onAllArt.setCompositionString("adult or pedi");
	
		//Add Filters		
		dataSetDefinition.addFilter(onAllArt,
			new HashMap<String, Object>());
		dataSetDefinition2.addFilter(onAllArt,
			new HashMap<String, Object>());
		
		//No baseline CD4
		dataSetDefinition.addFilter(Cohorts.createPatientsWithoutBaseLineObservation(cd4, pws, 180, 42), new HashMap<String, Object>());
		//No baseline weight
		dataSetDefinition2.addFilter(Cohorts.createPatientsWithoutBaseLineObservation(weight, pws, 180, 42), new HashMap<String, Object>());
		
		//Transfer in Patients
		EncounterCohortDefinition transferEncounter = new EncounterCohortDefinition();
		transferEncounter.setEncounterTypeList(this.transferEncounter);
		
		CompositionCohortDefinition allTransfer = new CompositionCohortDefinition();
		allTransfer.setName("allTransfer");
		allTransfer.getSearches().put("adult", new Mapped<CohortDefinition>(Cohorts.createPatientsWithStatePredatingProgramEnrolment(onArt), new HashMap<String, Object>()));
		allTransfer.getSearches().put("pedi", new Mapped<CohortDefinition>(Cohorts.createPatientsWithStatePredatingProgramEnrolment(onArtPedi), new HashMap<String, Object>()));
		allTransfer.getSearches().put("transfer", new Mapped<CohortDefinition>(transferEncounter, new HashMap<String, Object>()));
		allTransfer.setCompositionString("(adult or pedi) and not transfer");
		
		dataSetDefinition3.addFilter(allTransfer, new HashMap<String, Object>());
		
		//Add Columns
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		//dataSetDefinition.addColumn(RowPerPatientColumns.getHealthCenter("healthCenter"), new HashMap<String, Object>());
		//dataSetDefinition2.addColumn(RowPerPatientColumns.getHealthCenter("healthCenter"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getTreatmentGroupOfAllHIVPatientIncludingCompleted("Group", null), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getTreatmentGroupOfAllHIVPatientIncludingCompleted("Group", null), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getTreatmentGroupOfAllHIVPatientIncludingCompleted("Group", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfWorkflowStateChange("artWorkflowStart", art), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getDateOfWorkflowStateChange("artWorkflowStart", art), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getDateOfWorkflowStateChange("artWorkflowStart", art), new HashMap<String, Object>());
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = RowPerPatientColumns.getDrugOrderForStartOfART("StartART", "dd-MMM-yyyy");
		dataSetDefinition.addColumn(startArt,
		    new HashMap<String, Object>());
		dataSetDefinition2.addColumn(startArt,
		    new HashMap<String, Object>());
		dataSetDefinition3.addColumn(startArt,
		    new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions minDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		minDate.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		minDate.setName("MinDate");
		minDate.setCalculator(new DrugOrderDateManipulation("StartART", -180, Calendar.DAY_OF_MONTH, "dd-MMM-yyyy"));
		dataSetDefinition.addColumn(minDate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(minDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions maxDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		maxDate.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		maxDate.setName("MaxDate");
		maxDate.setCalculator(new DrugOrderDateManipulation("StartART", 45, Calendar.DAY_OF_MONTH, "dd-MMM-yyyy"));
		dataSetDefinition.addColumn(maxDate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(maxDate, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfAllHIVEnrolment("hivEnrolment", "dd-MMM-yyyy"), new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getDateOfAllHIVEnrolment("hivEnrolment", "dd-MMM-yyyy"), new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getDateOfAllHIVEnrolment("hivEnrolment", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition3.addColumn(RowPerPatientColumns.getDateOfProgramEnrolment("pmtctEnrolment", pmtct, "dd-MMM-yyyy"), new HashMap<String, Object>());

		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(dataSetDefinition);
		ldsd.addBaseDefinition(dataSetDefinition2);
		ldsd.addBaseDefinition(dataSetDefinition3);
		ldsd.setName("Location Data Set");
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		reportDefinition.addDataSetDefinition("dataSet", ldsd, ParameterizableUtil.createParameterMappings("location=${location}"));
	}
	
	private void setupProperties() {
		
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		
		onArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
	
		onArtPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		pws.add(onArt);
		pws.add(onArtPedi);
		
		EncounterType transferEnc = gp.getEncounterType(GlobalPropertiesManagement.TRANSFER_ENCOUNTER);
		transferEncounter.add(transferEnc);
		
		art = gp.getConcept(GlobalPropertiesManagement.ON_ART_TREATMENT_STATUS_CONCEPT);
		
		pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT);
	}
}
