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
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
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
import org.openmrs.module.rwandareports.customcalculator.BMI;
import org.openmrs.module.rwandareports.customcalculator.BMICalculation;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.DifferenceBetweenLastTwoObs;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupAdultLateVisitAndCD4Report {
	
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
	
	private Concept height;
	
	private Concept weight;
	
	private Concept viralLoad;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "AdultLateVisitAndCD4Template.xls",
		    "XlsAdultLateVisitAndCD4Template", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:8,dataset:AdultARTLateVisit|sheet:2,row:8,dataset:AdultPreARTLateVisit|sheet:3,row:8,dataset:AdultHIVLateCD4Count|sheet:4,row:8,dataset:HIVLostToFollowup|sheet:5,row:8,dataset:PreARTBelow350CD4|sheet:6,row:8,dataset:HIVLowBMI|sheet:7,row:8,dataset:DecliningInCD4ByMoreThan50|sheet:8,row:8,dataset:ViralLoadGreaterThan20InTheLast3Months|sheet:9,row:8,dataset:DecliningInCD4By50Percent");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsAdultLateVisitAndCD4Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Adult HIV Monthly Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Adult HIV Monthly Report");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		//====================================================================
		//           Patients Dataset definitions
		//====================================================================
		
		// Create Adult ART late visit dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition1 = new RowPerPatientDataSetDefinition();
		dataSetDefinition1.setName("Adult ART dataSetDefinition");
		
		// Create Adult Pre-ART late visit dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
		dataSetDefinition2.setName("Adult Pre-ART dataSetDefinition");
		
		//Create Adult HIV late CD4 count dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
		dataSetDefinition3.setName("Adult HIV late CD4 dataSetDefinition");
		
		//Create HIV lost to follow-up dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
		dataSetDefinition4.setName("Adult HIV lost to follow-up dataSetDefinition");
		
		//Create Adult Pre-ART patients with CD4 below 350 dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition5 = new RowPerPatientDataSetDefinition();
		dataSetDefinition5.setName("Adult Pre-ART patients with CD4 below 350 dataSetDefinition");
		
		//Patients with BMI below 18.5 dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition6 = new RowPerPatientDataSetDefinition();
		dataSetDefinition6.setName("Patients with BMI below 18.5 dataSetDefinition");
		
		//Patients whose cd4 has declined more than 50 in the last month for ART patients
		RowPerPatientDataSetDefinition dataSetDefinition7 = new RowPerPatientDataSetDefinition();
		dataSetDefinition7.setName("Patients declining in CD4 more than 50 dataSetDefinition");
		
		//Patients whose viral loads are greater than 20 in the last 3 months
		RowPerPatientDataSetDefinition dataSetDefinition8 = new RowPerPatientDataSetDefinition();
		dataSetDefinition8.setName("Patients with Viral Load greater than 20 in the last three months");
		
		//50% decline from highest CD4 count from baseline CD4 after ART initiation 
		RowPerPatientDataSetDefinition dataSetDefinition9 = new RowPerPatientDataSetDefinition();
		dataSetDefinition9.setName("Patients with CD4 count decline of more than 50%");
		
		//Adult HIV program Cohort definition
		InProgramCohortDefinition adultHivProgramCohort = Cohorts.createInProgramParameterizableByDate(
		    "adultHivProgramCohort", hivProgram);
		dataSetDefinition1.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition2.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition3.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition4.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition5.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition6.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition7.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition8.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition9.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		//==================================================================
		//                 1. Adult ART late visit
		//==================================================================		
		
		// ON ANTIRETROVIRALS state cohort definition.
		InStateCohortDefinition onARTStatusCohort = Cohorts.createInProgramStateParameterizableByDate("onARTStatusCohort",
		    onART);
		
		dataSetDefinition1.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition7.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition8.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition9.addFilter(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
		dataSetDefinition1.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition2.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition3.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition4.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition5.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition6.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition7.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition8.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition9.addFilter(patientsNotVoided, new HashMap<String, Object>());
		
		//Patients with any Clinical Encounter(Lab Test included) in last year
		EncounterCohortDefinition patientsWithClinicalEncounters = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncounters", "onOrAfter", clinicalEnountersIncLab);
		dataSetDefinition1.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition2.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition3.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition5.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition6.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition7.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition8.addFilter(patientsWithClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition9.addFilter(patientsWithClinicalEncounters,
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
		//                 2. Adult Pre-ART late visit
		//==================================================================
		
		// Following state cohort definition.
		
		InStateCohortDefinition followingStatusCohort = Cohorts.createInProgramStateParameterizableByDate(
		    "followingStatusCohort", following);
		
		dataSetDefinition2
		        .addFilter(followingStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition5
		        .addFilter(followingStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		// Patients without Any clinical Encounter(Test lab excluded) in last six months.
		dataSetDefinition2.addFilter(patientsWithoutClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
		//==================================================================
		//                 3. Adult HIV late CD4 count
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
		//                 5. Pre-ART patients with CD4 below 350
		//==================================================================
		
		//Patients with CD4 below 350
		NumericObsCohortDefinition lastDC4below350 = Cohorts.createNumericObsCohortDefinition("lastDC4below350", "onOrBefore", cd4, 350.0,
		    RangeComparator.LESS_THAN, TimeModifier.LAST);
		dataSetDefinition5.addFilter(lastDC4below350, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
		
		
		//==================================================================
		//                 6. Patients with BMI less than 18.5
		//==================================================================
		
		//Patients with BMI less than 18.5
		SqlCohortDefinition patientWithLowBMI = new SqlCohortDefinition();
		patientWithLowBMI.setName("patientWithLowBMI");
		patientWithLowBMI
		        .setQuery("select w.person_id from (select * from (select o.person_id,o.value_numeric from obs o,concept c where o.voided=0 and o.value_numeric is not null and o.concept_id= c.concept_id and c.uuid='"
		                + height.getUuid()
		                + "' order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h,(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.voided=0 and o.value_numeric is not null and o.concept_id= c.concept_id and c.uuid='"
		                + weight.getUuid()
		                + "' order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w,(select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location) loc where loc.patient_id=w.person_id and w.person_id=h.person_id and ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)<18.5");
		patientWithLowBMI.addParameter(new Parameter("location", "location", Location.class));
		dataSetDefinition6.addFilter(patientWithLowBMI, new HashMap<String, Object>());
		
		//==================================================================
		//                7 . Patients Declining in CD4 by more than 50
		//==================================================================
		
		//Patients Declining in CD4 by more than 50
		SqlCohortDefinition deciningInCD4MoreThan50 = Cohorts.createPatientsWithDecline("deciningInCD4MoreThan50", cd4, 50);
		dataSetDefinition7.addFilter(deciningInCD4MoreThan50,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		
		//==================================================================
		//                8 . Patients with Viral Load >20 in the last three months
		//==================================================================
		
		SqlCohortDefinition viralLoadGreaterThan20InLast3Months = new SqlCohortDefinition(
		        "select person_id from (select o.person_id,o.obs_datetime,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"
		                + viralLoad.getUuid()
		                + "' and o.value_numeric>20 and o.voided=0 and o.obs_datetime> :beforeDate and o.obs_datetime<= :onDate order by o.obs_datetime desc) as vload group by person_id");
		viralLoadGreaterThan20InLast3Months.setName("viralLoadGreaterThan20InLast3Months");
		viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("beforeDate", "beforeDate", Date.class));
		viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("onDate", "onDate", Date.class));
		viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("location", "location", Location.class));
		dataSetDefinition8.addFilter(viralLoadGreaterThan20InLast3Months,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate-3m},onDate=${endDate}"));
		
		//==================================================================
		//                9 . Patients with 50% decline from highest CD4 count from baseline CD4 after ART initiation 
		//==================================================================
		SqlCohortDefinition cd4declineOfMoreThan50Percent = Cohorts.createPatientsWithDeclineFromBaseline("cd4decline", cd4, onART);
		dataSetDefinition9.addFilter(cd4declineOfMoreThan50Percent,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
		dataSetDefinition1.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(imbType, new HashMap<String, Object>());
		
		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("First Name");
		dataSetDefinition1.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(givenName, new HashMap<String, Object>());
		
		PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("Last Name");
		dataSetDefinition1.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(familyName, new HashMap<String, Object>());
		
		PatientProperty gender = RowPerPatientColumns.getGender("Sex");
		dataSetDefinition1.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(gender, new HashMap<String, Object>());
		
		DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
		dataSetDefinition1.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(birthdate, new HashMap<String, Object>());
		
		StateOfPatient txGroup = RowPerPatientColumns.getStateOfPatient("Group", hivProgram, treatmentGroup,
		    new GroupStateFilter());
		dataSetDefinition1.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(txGroup, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(txGroup, new HashMap<String, Object>());
		
		StateOfPatient stOfPatient = RowPerPatientColumns.getStateOfPatient("Treatment", hivProgram, treatmentStatus,
		    new TreatmentStateFilter());
		dataSetDefinition1.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(stOfPatient, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(stOfPatient, new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType = RowPerPatientColumns.getRecentEncounterType("Last visit type",
		    clinicalEncoutersExcLab, new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(lastEncounterType, new HashMap<String, Object>());
		
		DateDiff lateVisitInMonth = RowPerPatientColumns.getDifferenceSinceLastEncounter(
		    "Late visit in months", clinicalEncoutersExcLab, DateDiffType.MONTHS);
		lateVisitInMonth.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition5.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition6.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		
		MostRecentObservation returnVisitDate = RowPerPatientColumns.getMostRecentReturnVisitDate(
		    "Date of missed appointment", null);
		dataSetDefinition1.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(returnVisitDate, new HashMap<String, Object>());
		
		MostRecentObservation cd4Count = RowPerPatientColumns.getMostRecentCD4("Most recent CD4", null);
		dataSetDefinition1.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(cd4Count, new HashMap<String, Object>());
		
		DateDiff lateCD4InMonths = RowPerPatientColumns.getDifferenceSinceLastObservation(
		    "Late CD4 in months", cd4, DateDiffType.MONTHS);
		lateCD4InMonths.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition5.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition6.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		
		PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("Accompagnateur");
		dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(accompagnateur, new HashMap<String, Object>());
		
		PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
		dataSetDefinition1.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition7.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition8.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition9.addColumn(address1, new HashMap<String, Object>());
		
		MostRecentObservation viralLoad = RowPerPatientColumns.getMostRecentViralLoad("Most recent viralLoad", null);
		dataSetDefinition8.addColumn(viralLoad, new HashMap<String, Object>());
		
		MostRecentObservation weight = RowPerPatientColumns.getMostRecentWeight("Weight", "dd-mmm-yyyy");
		dataSetDefinition6.addColumn(weight, new HashMap<String, Object>());
		
		MostRecentObservation height = RowPerPatientColumns.getMostRecentHeight("Height", "dd-mmm-yyyy");
		dataSetDefinition6.addColumn(height, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bmi = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bmi.setName("BMI");
		bmi.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		bmi.addPatientDataToBeEvaluated(height, new HashMap<String, Object>());
		BMICalculation bmiCalc = new BMICalculation();
		bmiCalc.setHeightName(height.getName());
		bmiCalc.setWeightName(weight.getName());
		bmi.setCalculator(bmiCalc);
		dataSetDefinition6.addColumn(bmi, new HashMap<String, Object>());
		
		AllObservationValues allCD4 = RowPerPatientColumns.getAllCD4Values("allCD4Obs", "dd-mmm-yyyy", null, null);
		CustomCalculationBasedOnMultiplePatientDataDefinitions decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		decline.setName("Decline");
		decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		decline.setCalculator(new DifferenceBetweenLastTwoObs());
		dataSetDefinition7.addColumn(decline, new HashMap<String, Object>());
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = RowPerPatientColumns.getDrugOrderForStartOfART("StartART", "dd-MMM-yyyy");
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions cd4Decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		cd4Decline.setName("cd4Decline");
		cd4Decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		cd4Decline.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		DeclineHighestCD4 declineCD4 = new DeclineHighestCD4();
		declineCD4.setInitiationArt("StartART");
		declineCD4.setShortDisplay(true);
		cd4Decline.setCalculator(declineCD4);
		dataSetDefinition9.addColumn(cd4Decline, new HashMap<String, Object>());
		
		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition5.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition6.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition7.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition8.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition9.addParameter(new Parameter("location", "Location", Location.class));
		
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition5.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition6.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition7.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition8.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition9.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("AdultARTLateVisit", dataSetDefinition1, mappings);
		reportDefinition.addDataSetDefinition("AdultPreARTLateVisit", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("AdultHIVLateCD4Count", dataSetDefinition3, mappings);
		reportDefinition.addDataSetDefinition("HIVLostToFollowup", dataSetDefinition4, mappings);
		reportDefinition.addDataSetDefinition("PreARTBelow350CD4", dataSetDefinition5, mappings);
		reportDefinition.addDataSetDefinition("HIVLowBMI", dataSetDefinition6, mappings);
		reportDefinition.addDataSetDefinition("DecliningInCD4ByMoreThan50", dataSetDefinition7, mappings);
		reportDefinition.addDataSetDefinition("ViralLoadGreaterThan20InTheLast3Months", dataSetDefinition8, mappings);
		reportDefinition.addDataSetDefinition("DecliningInCD4By50Percent", dataSetDefinition9, mappings);
	}
	
	private void setupProperties() {
		hivProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		onART = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		clinicalEnountersIncLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
		
		clinicalEncoutersExcLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST);
		
		following = gp.getProgramWorkflowState(GlobalPropertiesManagement.FOLLOWING_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		
		treatmentGroup = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		treatmentStatus = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		
		viralLoad = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
	}
}
