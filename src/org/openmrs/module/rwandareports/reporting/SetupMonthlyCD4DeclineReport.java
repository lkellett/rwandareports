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
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff.DateDiffType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.DifferenceBetweenLastTwoObs;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupMonthlyCD4DeclineReport {
	
	protected final static Log log = LogFactory.getLog(SetupMonthlyCD4DeclineReport.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program hivProgram;
	
	private Program pediProgram;
	
	private Program pmtctCCMother;
	
	private ProgramWorkflow treatmentGroup;
	
	private ProgramWorkflow treatmentStatus;
	
	private ProgramWorkflow treatmentGroupPedi;
	
	private ProgramWorkflowState onART;
	
	private ProgramWorkflowState onARTPedi;
	
	private List<EncounterType> clinicalEnountersIncLab;
	
	private List<EncounterType> clinicalEncoutersExcLab;
	
	private Concept cd4;
	
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinitionDecline("Monthly Pedi Art Decline");
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediCD4DeclineTemplate.xls",
		    "XlsPediLateVisitAndCD4DeclineTemplate", null);
		
		ReportDefinition rdp = createReportDefinitionDecline("Monthly PMTCT CC Mother Art Decline");
		ReportDesign designp = h.createRowPerPatientXlsOverviewReportDesign(rdp, "CCMotherCD4DeclineTemplate.xls",
		    "XlsCCMotherLateVisitAndCD4DeclineTemplate", null);
		
		ReportDefinition artDecline = createReportDefinitionDecline("Monthly Adult Art Decline");
		ReportDesign designa = h.createRowPerPatientXlsOverviewReportDesign(artDecline, "AdultLateVisitAndCD4DeclineTemplate.xls",
		    "XlsAdultLateVisitAndCD4DeclineTemplate", null);
		
		createDataSetDefinition(rd, rdp, artDecline);
		
		h.saveReportDefinition(rd);
		h.saveReportDefinition(rdp);
		h.saveReportDefinition(artDecline);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,dataset:dataSet|sheet:1,row:9,dataset:decline50PercPedi");
		
		design.setProperties(props);
		h.saveReportDesign(design);
		
		Properties propsp = new Properties();
		propsp.put(
		    "repeatingSections",
		    "sheet:1,dataset:dataSet|sheet:1,row:9,dataset:decline50CC");
		
		designp.setProperties(propsp);
		h.saveReportDesign(designp);
		
		Properties propsa = new Properties();
		propsa.put(
		    "repeatingSections",
		    "sheet:1,dataset:dataSet|sheet:1,row:9,dataset:decline50Perc|sheet:2,dataset:dataSet|sheet:2,row:9,dataset:decline50");
		
		designa.setProperties(propsa);
		h.saveReportDesign(designa);
		
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsCCMotherLateVisitAndCD4DeclineTemplate".equals(rd.getName()) || "XlsPediLateVisitAndCD4DeclineTemplate".equals(rd.getName()) || "XlsAdultLateVisitAndCD4DeclineTemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Monthly Pedi Art Decline");
		h.purgeReportDefinition("Monthly PMTCT CC Mother Art Decline");
		h.purgeReportDefinition("Monthly Adult Art Decline");
	}
	
	private ReportDefinition createReportDefinitionDecline(String name) {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName(name);
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		reportDefinition.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition pedi, ReportDefinition cc, ReportDefinition adult) {
		//====================================================================
		//           Patients Dataset definitions
		//====================================================================
		
		
		//Patients whose cd4 has declined more than 50 in the last month for ART patients
		RowPerPatientDataSetDefinition dataSetDefinitionA1 = new RowPerPatientDataSetDefinition();
		dataSetDefinitionA1.setName("decline50Perc");
		
		//50% decline from highest CD4 count from baseline CD4 after ART initiation 
		RowPerPatientDataSetDefinition dataSetDefinitionA2 = new RowPerPatientDataSetDefinition();
		dataSetDefinitionA2.setName("decline50");
		
		//Create 50% decline in CD4 since ART initiation dataset definition
		RowPerPatientDataSetDefinition dataSetDefinitionP = new RowPerPatientDataSetDefinition();
		dataSetDefinitionP.setName("decline50PercPedi");
		
		//Patients whose cd4 has declined more than 50 in the last month for ART patients
		RowPerPatientDataSetDefinition dataSetDefinitionCC = new RowPerPatientDataSetDefinition();
		dataSetDefinitionCC.setName("decline50CC");
		
		//==================================================================
		//                 Program Enrollment Filters
		//==================================================================
		
		//Adult HIV program Cohort definition
		InProgramCohortDefinition adultHivProgramCohort = Cohorts.createInProgramParameterizableByDate(
		    "adultHivProgramCohort", hivProgram);
		dataSetDefinitionA1.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinitionA2.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		InProgramCohortDefinition pediatricHivProgramCohort = Cohorts.createInProgramParameterizableByDate(
		    "pediatricHivProgramCohort", hivProgram);
		
		dataSetDefinitionP.addFilter(pediatricHivProgramCohort,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		InProgramCohortDefinition pmtctCombinedClinicMotherProgramCohort = Cohorts.createInProgramParameterizableByDate(
		    "adultHivProgramCohort", pmtctCCMother);
		dataSetDefinitionCC.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		//==================================================================
		//               Encounter Filters
		//==================================================================
		
		//Patients with any Clinical Encounter(Lab Test included) in last year
		EncounterCohortDefinition patientsWithClinicalEncounters = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncounters", "onOrAfter", clinicalEnountersIncLab);
		dataSetDefinitionA1.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinitionA2.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinitionP.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinitionCC.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		
		//==================================================================
		//                 Art Status filters
		//==================================================================
		
		InStateCohortDefinition onARTStatusCohort = Cohorts.createInProgramStateParameterizableByDate("onARTStatusCohort",
		    onART);
		
		dataSetDefinitionA1.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinitionA2.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		InStateCohortDefinition onARTPediStatusCohort = Cohorts.createInProgramStateParameterizableByDate("onARTPediStatusCohort",
		    onARTPedi);
		dataSetDefinitionP.addFilter(onARTPediStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		
		//==================================================================
		//                1 . Patients Declining in CD4 by more than 50
		//==================================================================
		
		//Patients Declining in CD4 by more than 50
		SqlCohortDefinition deciningInCD4MoreThan50 = Cohorts.createPatientsWithDecline("deciningInCD4MoreThan50", cd4, 50);
		dataSetDefinitionA2.addFilter(deciningInCD4MoreThan50,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		dataSetDefinitionCC.addFilter(deciningInCD4MoreThan50,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		
		
		//==================================================================
		//                2 . Patients with 50% decline from highest CD4 count from baseline CD4 after ART initiation 
		//==================================================================
		SqlCohortDefinition cd4declineOfMoreThan50Percent = Cohorts.createPatientsWithDeclineFromBaseline("cd4decline", cd4, onART);
		dataSetDefinitionA1.addFilter(cd4declineOfMoreThan50Percent,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		
		SqlCohortDefinition cd4declineOfMoreThan50PercentPedi = Cohorts.createPatientsWithDeclineFromBaseline("cd4decline", cd4, onARTPedi);
		dataSetDefinitionP.addFilter(cd4declineOfMoreThan50PercentPedi,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
		dataSetDefinitionA1.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(imbType, new HashMap<String, Object>());
		
		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("First Name");
		dataSetDefinitionA1.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(givenName, new HashMap<String, Object>());
		
		PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("Last Name");
		dataSetDefinitionA1.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(familyName, new HashMap<String, Object>());
		
		PatientProperty gender = RowPerPatientColumns.getGender("Sex");
		dataSetDefinitionA1.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(gender, new HashMap<String, Object>());
		
		DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
		dataSetDefinitionA1.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(birthdate, new HashMap<String, Object>());
		
		StateOfPatient txGroup = RowPerPatientColumns.getStateOfPatient("Group", hivProgram, treatmentGroup,
		    new GroupStateFilter());
		dataSetDefinitionA1.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(txGroup, new HashMap<String, Object>());
		
		StateOfPatient txGroupPedi = RowPerPatientColumns.getStateOfPatient("Group", pediProgram, treatmentGroupPedi,
		    new GroupStateFilter());
		dataSetDefinitionP.addColumn(txGroupPedi, new HashMap<String, Object>());
		
		StateOfPatient stOfPatient = RowPerPatientColumns.getStateOfPatient("Treatment", hivProgram, treatmentStatus,
		    new TreatmentStateFilter());
		dataSetDefinitionA1.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(stOfPatient, new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType = RowPerPatientColumns.getRecentEncounterType("Last visit type",
		    clinicalEncoutersExcLab, new LastEncounterFilter());
		dataSetDefinitionA1.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(lastEncounterType, new HashMap<String, Object>());
		
		DateDiff lateVisitInMonth = RowPerPatientColumns.getDifferenceSinceLastEncounter(
		    "Late visit in months", clinicalEncoutersExcLab, DateDiffType.MONTHS);
		lateVisitInMonth.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinitionA1.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinitionA2.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinitionP.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinitionCC.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		
		MostRecentObservation returnVisitDate = RowPerPatientColumns.getMostRecentReturnVisitDate(
		    "Date of missed appointment", null);
		dataSetDefinitionA1.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(returnVisitDate, new HashMap<String, Object>());
		
		MostRecentObservation cd4Count = RowPerPatientColumns.getMostRecentCD4("Most recent CD4", null);
		dataSetDefinitionA1.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(cd4Count, new HashMap<String, Object>());
		
		DateDiff lateCD4InMonths = RowPerPatientColumns.getDifferenceSinceLastObservation(
		    "Late CD4 in months", cd4, DateDiffType.MONTHS);
		lateCD4InMonths.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinitionA1.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinitionA2.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinitionP.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinitionCC.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		
		PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("Accompagnateur");
		dataSetDefinitionA1.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(accompagnateur, new HashMap<String, Object>());
		
		PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
		dataSetDefinitionA1.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinitionA2.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(address1, new HashMap<String, Object>());
		
		AllObservationValues allCD4 = RowPerPatientColumns.getAllCD4Values("allCD4Obs", "dd-mmm-yyyy", null, null);
		CustomCalculationBasedOnMultiplePatientDataDefinitions decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		decline.setName("Decline");
		decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		decline.setCalculator(new DifferenceBetweenLastTwoObs());
		dataSetDefinitionA2.addColumn(decline, new HashMap<String, Object>());
		dataSetDefinitionCC.addColumn(decline, new HashMap<String, Object>());
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = RowPerPatientColumns.getDrugOrderForStartOfART("StartART", "dd-MMM-yyyy");
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions cd4Decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		cd4Decline.setName("cd4Decline");
		cd4Decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		cd4Decline.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		DeclineHighestCD4 declineCD4 = new DeclineHighestCD4();
		declineCD4.setInitiationArt("StartART");
		declineCD4.setShortDisplay(true);
		cd4Decline.setCalculator(declineCD4);
		dataSetDefinitionA1.addColumn(cd4Decline, new HashMap<String, Object>());
		dataSetDefinitionP.addColumn(cd4Decline, new HashMap<String, Object>());
		
		dataSetDefinitionA1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinitionA2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinitionP.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinitionCC.addParameter(new Parameter("location", "Location", Location.class));
		
		dataSetDefinitionA1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinitionA2.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinitionP.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinitionCC.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		LocationHierachyIndicatorDataSetDefinition ldsdPedi = new LocationHierachyIndicatorDataSetDefinition();
		ldsdPedi.setName("ARTPediDecline");
		ldsdPedi.addBaseDefinition(dataSetDefinitionP);
		ldsdPedi.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsdPedi.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsdPedi.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		pedi.addDataSetDefinition("dataSet", ldsdPedi, mappings);
		
		LocationHierachyIndicatorDataSetDefinition ldsdCC = new LocationHierachyIndicatorDataSetDefinition();
		ldsdCC.setName("ARTCCDecline");
		ldsdCC.addBaseDefinition(dataSetDefinitionCC);
		ldsdCC.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsdCC.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsdCC.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		cc.addDataSetDefinition("dataSet", ldsdCC, mappings);
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition();
		ldsd.setName("ARTDecline");
		ldsd.addBaseDefinition(dataSetDefinitionA1);
		ldsd.addBaseDefinition(dataSetDefinitionA2);
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		adult.addDataSetDefinition("dataSet", ldsd, mappings);
	}
	
	private void setupProperties() {
		hivProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		pediProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		onART = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		onARTPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		pmtctCCMother = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		
		clinicalEnountersIncLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
		
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		
		treatmentGroup = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		treatmentStatus = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		treatmentGroupPedi = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
	}
}
