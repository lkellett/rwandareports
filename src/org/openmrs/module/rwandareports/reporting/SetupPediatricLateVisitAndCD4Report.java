package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiffInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupPediatricLateVisitAndCD4Report {
	
	protected final static Log log = LogFactory.getLog(SetupAdultLateVisitAndCD4Report.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program hivProgram;
	
	private ProgramWorkflow treatmentGroup;
	
	private ProgramWorkflow treatmentStatus;
	
	private ProgramWorkflowState onART;
	
	private ProgramWorkflowState following;
	
	private List<EncounterType> clinicalEnountersIncLab;
	
	private List<EncounterType> clinicalEncoutersExcLab;
	
	private Concept cd4;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediatricLateVisitAndCD4Template.xls",
		    "XlsPediatricLateVisitAndCD4Template", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:8,dataset:PediatricARTLateVisit|sheet:2,row:8,dataset:PediatricPreARTLateVisit|sheet:3,row:8,dataset:PediatricHIVLateCD4Count|sheet:4,row:8,dataset:HIVLostToFollowup");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsPediatricLateVisitAndCD4Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Pediatric HIV Monthly Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Pediatric HIV Monthly Report");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		//====================================================================
		//           Patients Dataset definitions
		//====================================================================
		
		// Create ART late visit dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition1 = new RowPerPatientDataSetDefinition();
		dataSetDefinition1.setName("Pediatric ART dataSetDefinition");
		
		// Create Pre-ART late visit dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
		dataSetDefinition2.setName("Pediatric Pre-ART dataSetDefinition");
		
		//Create HIV late CD4 count dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
		dataSetDefinition3.setName("Pediatric HIV late CD4 dataSetDefinition");
		
		//Create HIV lost to follow-up dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
		dataSetDefinition4.setName("Pediatric HIV lost to follow-up dataSetDefinition");
		
		// HIV program Cohort definition
		InProgramCohortDefinition pediatricHivProgramCohort = Cohorts.createInProgramParameterizableByDate(
		    "pediatricHivProgramCohort", hivProgram);
		
		dataSetDefinition1.addFilter(pediatricHivProgramCohort,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition2.addFilter(pediatricHivProgramCohort,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition3.addFilter(pediatricHivProgramCohort,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition4.addFilter(pediatricHivProgramCohort,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		//==================================================================
		//                 1. Pediatric ART late visit
		//==================================================================		
		
		// ON ANTIRETROVIRALS state cohort definition.
		InStateCohortDefinition onARTStatusCohort = Cohorts.createInProgramStateParameterizableByDate("onARTStatusCohort",
		    onART);
		
		dataSetDefinition1.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
		dataSetDefinition1.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition2.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition3.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition4.addFilter(patientsNotVoided, new HashMap<String, Object>());
		
		//Patients with any Clinical Encounter(Lab Test included) in last year
		EncounterCohortDefinition patientsWithClinicalEncounters = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncounters", "onOrAfter", clinicalEnountersIncLab);
		dataSetDefinition1.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition2.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition3.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		
		// Patients without Any clinical Encounter(Test lab excluded) in last three months.
		EncounterCohortDefinition patientsWithClinicalEncountersWithoutLabTest = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncounters", "onOrAfter", clinicalEncoutersExcLab);
		
		CompositionCohortDefinition patientsWithoutClinicalEncounters = new CompositionCohortDefinition();
		patientsWithoutClinicalEncounters.setName("patientsWithoutClinicalEncounters");
		patientsWithoutClinicalEncounters.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithoutClinicalEncounters.getSearches().put(
		    "patientsWithClinicalEncountersWithoutLabTest",
		    new Mapped<CohortDefinition>(patientsWithClinicalEncountersWithoutLabTest, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithoutClinicalEncounters.setCompositionString("NOT patientsWithClinicalEncountersWithoutLabTest");
		
		dataSetDefinition1.addFilter(patientsWithoutClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m}"));
		
		//==================================================================
		//                 2. Pre-ART late visit
		//==================================================================
		
		// Following state cohort definition.
		
		InStateCohortDefinition followingStatusCohort = Cohorts.createInProgramStateParameterizableByDate(
		    "followingStatusCohort", following);
		
		dataSetDefinition2
		        .addFilter(followingStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		// Patients without Any clinical Encounter(Test lab excluded) in last six months.
		dataSetDefinition2.addFilter(patientsWithoutClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
		//==================================================================
		//                 3. HIV late CD4 count
		//==================================================================
		
		NumericObsCohortDefinition cd4CohortDefinition = Cohorts.createNumericObsCohortDefinition("cd4CohortDefinition",
		    "onOrAfter", cd4, new Double(0), null, TimeModifier.LAST);
		
		CompositionCohortDefinition patientsWithouthCD4RecordComposition = new CompositionCohortDefinition();
		patientsWithouthCD4RecordComposition.setName("patientsWithouthCD4RecordComposition");
		patientsWithouthCD4RecordComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithouthCD4RecordComposition.getSearches().put(
		    "cd4CohortDefinition",
		    new Mapped<CohortDefinition>(cd4CohortDefinition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithouthCD4RecordComposition.setCompositionString("NOT cd4CohortDefinition");
		
		dataSetDefinition3.addFilter(patientsWithouthCD4RecordComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
		//==================================================================
		//                 4. HIV lost to follow-up
		//==================================================================
		
		//Patients with no encounters of any kind in the past year
		
		InverseCohortDefinition patientsWithoutEncountersInPastYear = new InverseCohortDefinition(
		        patientsWithClinicalEncounters);
		patientsWithoutEncountersInPastYear.setName("patientsWithoutEncountersInPastYear");
		
		dataSetDefinition4.addFilter(patientsWithoutEncountersInPastYear,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
		dataSetDefinition1.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(imbType, new HashMap<String, Object>());
		
		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("First Name");
		dataSetDefinition1.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(givenName, new HashMap<String, Object>());
		
		PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("Last Name");
		dataSetDefinition1.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(familyName, new HashMap<String, Object>());
		
		PatientProperty gender = RowPerPatientColumns.getGender("Sex");
		dataSetDefinition1.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(gender, new HashMap<String, Object>());
		
		DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
		dataSetDefinition1.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(birthdate, new HashMap<String, Object>());
		
		StateOfPatient txGroup = RowPerPatientColumns.getStateOfPatient("Group", hivProgram, treatmentGroup,
		    new GroupStateFilter());
		dataSetDefinition1.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(txGroup, new HashMap<String, Object>());
		
		StateOfPatient stOfPatient = RowPerPatientColumns.getStateOfPatient("Treatment", hivProgram, treatmentStatus,
		    new TreatmentStateFilter());
		dataSetDefinition1.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(stOfPatient, new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType = RowPerPatientColumns.getRecentEncounterType("Last visit type",
		    clinicalEncoutersExcLab, new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(lastEncounterType, new HashMap<String, Object>());
		
		DateDiffInMonths lateVisitInMonth = RowPerPatientColumns.getDifferenceInMonthsSinceLastEncounter(
		    "Late visit in months", clinicalEncoutersExcLab);
		lateVisitInMonth.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		MostRecentObservation returnVisitDate = RowPerPatientColumns.getMostRecentReturnVisitDate(
		    "Date of missed appointment", null);
		dataSetDefinition1.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(returnVisitDate, new HashMap<String, Object>());
		
		MostRecentObservation cd4Count = RowPerPatientColumns.getMostRecentCD4("Most recent CD4", null);
		dataSetDefinition1.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Count, new HashMap<String, Object>());
		
		MostRecentObservation cd4Percentage = RowPerPatientColumns.getMostRecentCD4Percentage("Most recent CD4 Percentage",
		    null);
		dataSetDefinition1.addColumn(cd4Percentage, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(cd4Percentage, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Percentage, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Percentage, new HashMap<String, Object>());
		
		DateDiffInMonths lateCD4InMonths = RowPerPatientColumns.getDifferenceInMonthsSinceLastObservation(
		    "Late CD4 in months", cd4);
		lateCD4InMonths.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("Accompagnateur");
		dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(accompagnateur, new HashMap<String, Object>());
		
		PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
		dataSetDefinition1.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(address1, new HashMap<String, Object>());
		
		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
		
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("PediatricARTLateVisit", dataSetDefinition1, mappings);
		reportDefinition.addDataSetDefinition("PediatricPreARTLateVisit", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("PediatricHIVLateCD4Count", dataSetDefinition3, mappings);
		reportDefinition.addDataSetDefinition("HIVLostToFollowup", dataSetDefinition4, mappings);
	}
	
	private void setupProperties() {
		hivProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		onART = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		clinicalEnountersIncLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
		
		clinicalEncoutersExcLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST);
		
		following = gp.getProgramWorkflowState(GlobalPropertiesManagement.FOLLOWING_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		treatmentGroup = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		treatmentStatus = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
	}
}
