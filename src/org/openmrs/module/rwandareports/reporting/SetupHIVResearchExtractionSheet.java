package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomEvaluatorDefinition;
import org.openmrs.module.rwandareports.customevaluator.ARTStartDate;
import org.openmrs.module.rwandareports.customevaluator.CD4AtAnniversary;
import org.openmrs.module.rwandareports.customevaluator.HIVResearchExtractionExclusions;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupHIVResearchExtractionSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	
	private ProgramWorkflowState onArt;
	
	private ProgramWorkflowState onArtPedi;
	
	private List<ProgramWorkflow> pw = new ArrayList<ProgramWorkflow>();
	
	private List<ProgramWorkflowState> pws = new ArrayList<ProgramWorkflowState>();

	private Program pmtct;
	
	private Program tb;
	
	private List<EncounterType> clinicalEnountersIncLab;
	
	private Concept reasonForExitCare;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "HIVResearchDataExtraction.xls",
		    "HIVResearchDataExtraction", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:6,dataset:PatientDataSet");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("HIVResearchDataExtraction".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Research-Extraction Data for HIV Research");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Research-Extraction Data for HIV Research");
		
		createDataSetDefinition(reportDefinition);
		
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("PatientDataSet");
		
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
		
		//Transfer in Patients
		CompositionCohortDefinition allTransfer = new CompositionCohortDefinition();
		allTransfer.setName("allTransfer");
		allTransfer.getSearches().put("adult", new Mapped<CohortDefinition>(Cohorts.createPatientsWithStatePredatingProgramEnrolment(onArt), new HashMap<String, Object>()));
		allTransfer.getSearches().put("pedi", new Mapped<CohortDefinition>(Cohorts.createPatientsWithStatePredatingProgramEnrolment(onArtPedi), new HashMap<String, Object>()));
		allTransfer.setCompositionString("(adult or pedi) ");
		
		InverseCohortDefinition patientNotTransferred = new InverseCohortDefinition(
			allTransfer);

		
		dataSetDefinition.addFilter(patientNotTransferred, new HashMap<String, Object>());
		//For testing as it is a smaller dataset and runs quicker
		//dataSetDefinition.addFilter(allTransfer, new HashMap<String, Object>());
		
		//Add Columns
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
	
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Sector", false, true, false, false), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Cell", false, false, true, false), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Umudugudu", false, false, false, true), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("DOB", "dd-MMM-yyyy", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getTreatmentGroupOfAllHIVPatientIncludingCompleted("Group", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getHealthCenter("healthCenter"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getTreamentStatusOfAllHIVPatientIncludingCompleted("artWorkflowStart"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfAllHIVEnrolment("hivEnrolment", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfAllHIVCompletion("hivCompletion", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfAllProgramEnrolment("pmtctEnrolment", pmtct, "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfAllProgramEnrolment("TBEnrolment", tb, "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getBaselineCD4("BaselineCD4", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentCD4("CD4", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getRecentEncounterType("Encounter", clinicalEnountersIncLab, "dd-MMM-yyyy", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecent("exitFromCare", reasonForExitCare, "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFullHistoryOfProgramWorkflowStates("stateHistory", pw, "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		CustomEvaluatorDefinition exclude = new CustomEvaluatorDefinition();
		exclude.setName("exclude");
		exclude.setEvaluator(new HIVResearchExtractionExclusions());
		dataSetDefinition.addColumn(exclude, new HashMap<String, Object>());
		
		CustomEvaluatorDefinition artStart = new CustomEvaluatorDefinition();
		artStart.setName("artStart");
		artStart.setEvaluator(new ARTStartDate());
		dataSetDefinition.addColumn(artStart, new HashMap<String, Object>());
		
		CustomEvaluatorDefinition cd4OneYear = new CustomEvaluatorDefinition();
		cd4OneYear.setName("cd4OneYear");
		CD4AtAnniversary cd41 = new CD4AtAnniversary();
		cd41.setAnniversary(1);
		cd4OneYear.setEvaluator(cd41);
		dataSetDefinition.addColumn(cd4OneYear, new HashMap<String, Object>());
		
		CustomEvaluatorDefinition cd4ThreeYear = new CustomEvaluatorDefinition();
		cd4ThreeYear.setName("cd4ThreeYear");
		CD4AtAnniversary cd43 = new CD4AtAnniversary();
		cd43.setAnniversary(3);
		cd4ThreeYear.setEvaluator(cd43);
		dataSetDefinition.addColumn(cd4ThreeYear, new HashMap<String, Object>());
		
		CustomEvaluatorDefinition cd4FiveYear = new CustomEvaluatorDefinition();
		cd4FiveYear.setName("cd4FiveYear");
		CD4AtAnniversary cd45 = new CD4AtAnniversary();
		cd45.setAnniversary(5);
		cd4FiveYear.setEvaluator(cd45);
		dataSetDefinition.addColumn(cd4FiveYear, new HashMap<String, Object>());


		
		reportDefinition.setBaseCohortDefinition(onAllArt, new HashMap<String, Object>());
		
		reportDefinition.addDataSetDefinition("PatientDataSet", dataSetDefinition, new HashMap<String, Object>());
	}
	
	private void setupProperties() {
		
		onArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
	
		onArtPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflow treatAdult = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pw.add(treatAdult);
		ProgramWorkflow treatPedi = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		pw.add(treatPedi);
		ProgramWorkflow treatPMTCT = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		pw.add(treatPMTCT);
		
		pws.add(onArt);
		pws.add(onArtPedi);
		
		pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		
		tb = gp.getProgram(GlobalPropertiesManagement.TB_PROGRAM);
		
		clinicalEnountersIncLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
		
		reasonForExitCare = gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);
	}
}
