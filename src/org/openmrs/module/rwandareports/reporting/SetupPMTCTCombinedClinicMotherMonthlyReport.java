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

public class SetupPMTCTCombinedClinicMotherMonthlyReport {
	
	protected final static Log log = LogFactory.getLog(SetupPMTCTCombinedClinicMotherMonthlyReport.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program pmtctCombinedClinicMotherProgram;
	
	private Concept nextVisitConcept;
	
	private EncounterType adultFlowVisit;
	
	private List<EncounterType> clinicalEnountersIncLab;
	
	private Concept cd4;
	
	private Concept height;
	
	private Concept weight;
	
	private Concept viralLoad;
	
	private List<EncounterType> clinicalEncoutersExcLab;
		
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTCCMotherTemplate.xls",
		    "PMTCTCCMotherTemplate", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:8,dataset:LateVisit|sheet:2,row:8,dataset:LateCD4Count|sheet:3,row:8,dataset:LostToFollowup|sheet:4,row:8,dataset:LowBMI|sheet:5,row:8,dataset:DecliningInCD4ByMoreThan50|sheet:6,row:8,dataset:ViralLoadGreaterThan20InTheLast3Months");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTCCMotherTemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PMTCT Combined Clinic Mother Monthly Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Combined Clinic Mother Monthly Report");
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
		
		// Create late visit dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition1 = new RowPerPatientDataSetDefinition();
		dataSetDefinition1.setName("Late visit");
		
		//Create late CD4 count dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
		dataSetDefinition2.setName("late CD4");
		
		//Create lost to follow-up dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
		dataSetDefinition3.setName("lost to follow-up");
		
		
		//Patients with BMI below 18.5 dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
		dataSetDefinition4.setName("BMI below 18.5 ");
		
		//Patients whose cd4 has declined more than 50 in the last month for ART patients
		RowPerPatientDataSetDefinition dataSetDefinition5 = new RowPerPatientDataSetDefinition();
		dataSetDefinition5.setName("declining in CD4 more than 50");
		
		//Patients whose viral loads are greater than 20 in the last 3 months
		RowPerPatientDataSetDefinition dataSetDefinition6 = new RowPerPatientDataSetDefinition();
		dataSetDefinition6.setName("Viral Load greater than 20 in the last three months");
		
		//PMTCT Combined clinic program Cohort definition
		InProgramCohortDefinition pmtctCombinedClinicMotherProgramCohort = Cohorts.createInProgramParameterizableByDate(
		    "adultHivProgramCohort", pmtctCombinedClinicMotherProgram);
		dataSetDefinition1.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition2.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition3.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition4.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition5.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition6.addFilter(pmtctCombinedClinicMotherProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		//==================================================================
		//                 1. Late visit
		//==================================================================		
		
	
		
		SqlCohortDefinition latevisit=new SqlCohortDefinition("select o.person_id from obs o, (select * from (select * from encounter where encounter_type="+adultFlowVisit.getEncounterTypeId()+" and voided=0 order by encounter_datetime desc) as e group by patient_id) as last_encounters where last_encounters.encounter_id=o.encounter_id and last_encounters.encounter_datetime<o.value_datetime and o.voided=0 and o.concept_id="+nextVisitConcept.getConceptId()+" and DATEDIFF(:endDate,o.value_datetime)>7 ;");
		latevisit.addParameter(new Parameter("endDate","endDate",Date.class));
		
		dataSetDefinition1.addFilter(latevisit, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
			
		//==================================================================
		//                 2. Late CD4 count
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
		
		dataSetDefinition2.addFilter(patientsWithouthCD4RecordComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
		//==================================================================
		//                 3. Lost to follow-up
		//==================================================================
		
		//Patients with no encounters of any kind in the past year
		
		EncounterCohortDefinition patientsWithClinicalEncounters = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncounters", "onOrAfter", clinicalEnountersIncLab);
		
		InverseCohortDefinition patientsWithoutEncountersInPastYear = new InverseCohortDefinition(
		        patientsWithClinicalEncounters);
		patientsWithoutEncountersInPastYear.setName("patientsWithoutEncountersInPastYear");
		
		dataSetDefinition3.addFilter(patientsWithoutEncountersInPastYear,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		
		
		//==================================================================
		//                 4. BMI below 18.5
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
		dataSetDefinition4.addFilter(patientWithLowBMI, new HashMap<String, Object>());
		
		//==================================================================
		//                5 . CD4 decline of more than 50
		//==================================================================
		
		//Patients Declining in CD4 by more than 50
		SqlCohortDefinition deciningInCD4MoreThan50 = Cohorts.createPatientsWithDecline("deciningInCD4MoreThan50", cd4, 50);
		dataSetDefinition5.addFilter(deciningInCD4MoreThan50,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate}"));
		
		//==================================================================
		//                6 . Patients who have a viral load >20
		//==================================================================
		
		SqlCohortDefinition viralLoadGreaterThan20InLast3Months = new SqlCohortDefinition(
		        "select person_id from (select o.person_id,o.obs_datetime,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"
		                + viralLoad.getUuid()
		                + "' and o.value_numeric>20 and o.voided=0 and o.obs_datetime> :beforeDate and o.obs_datetime<= :onDate order by o.obs_datetime desc) as vload group by person_id");
		viralLoadGreaterThan20InLast3Months.setName("viralLoadGreaterThan20InLast3Months");
		viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("beforeDate", "beforeDate", Date.class));
		viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("onDate", "onDate", Date.class));
		viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("location", "location", Location.class));
		dataSetDefinition6.addFilter(viralLoadGreaterThan20InLast3Months,
		    ParameterizableUtil.createParameterMappings("beforeDate=${endDate-3m},onDate=${endDate}"));
		
		
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
		
		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("First Name");
		dataSetDefinition1.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(givenName, new HashMap<String, Object>());
		
		PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("Last Name");
		dataSetDefinition1.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(familyName, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(familyName, new HashMap<String, Object>());
		
		PatientProperty gender = RowPerPatientColumns.getGender("Sex");
		dataSetDefinition1.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(gender, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(gender, new HashMap<String, Object>());
		
		DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
		dataSetDefinition1.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(birthdate, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(birthdate, new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType = RowPerPatientColumns.getRecentEncounterType("Last visit type",
		    clinicalEncoutersExcLab, new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(lastEncounterType, new HashMap<String, Object>());
		
		DateDiff lateVisitInMonth = RowPerPatientColumns.getDifferenceSinceLastEncounter(
		    "Late visit in months", clinicalEncoutersExcLab, DateDiffType.MONTHS);
		lateVisitInMonth.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(lateVisitInMonth, new HashMap<String, Object>());
		
		MostRecentObservation returnVisitDate = RowPerPatientColumns.getMostRecentReturnVisitDate(
		    "Date of missed appointment", null);
		dataSetDefinition1.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(returnVisitDate, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(returnVisitDate, new HashMap<String, Object>());
		
		MostRecentObservation cd4Count = RowPerPatientColumns.getMostRecentCD4("Most recent CD4", null);
		dataSetDefinition1.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(cd4Count, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(cd4Count, new HashMap<String, Object>());
		
		DateDiff lateCD4InMonths = RowPerPatientColumns.getDifferenceSinceLastObservation(
		    "Late CD4 in months", cd4, DateDiffType.MONTHS);
		lateCD4InMonths.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateCD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(lateCD4InMonths, new HashMap<String, Object>());
		
		PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("Accompagnateur");
		dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(accompagnateur, new HashMap<String, Object>());
		
		PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
		dataSetDefinition1.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition6.addColumn(address1, new HashMap<String, Object>());
		
		MostRecentObservation viralLoad = RowPerPatientColumns.getMostRecentViralLoad("Most recent viralLoad", null);
		dataSetDefinition6.addColumn(viralLoad, new HashMap<String, Object>());
		
		MostRecentObservation weight = RowPerPatientColumns.getMostRecentWeight("Weight", "dd-mmm-yyyy");
		dataSetDefinition4.addColumn(weight, new HashMap<String, Object>());
		
		MostRecentObservation height = RowPerPatientColumns.getMostRecentHeight("Height", "dd-mmm-yyyy");
		dataSetDefinition4.addColumn(height, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bmi = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bmi.setName("BMI");
		bmi.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		bmi.addPatientDataToBeEvaluated(height, new HashMap<String, Object>());
		BMICalculation bmiCalc = new BMICalculation();
		bmiCalc.setHeightName(height.getName());
		bmiCalc.setWeightName(weight.getName());
		bmi.setCalculator(bmiCalc);
		dataSetDefinition4.addColumn(bmi, new HashMap<String, Object>());
		
		AllObservationValues allCD4 = RowPerPatientColumns.getAllCD4Values("allCD4Obs", "dd-mmm-yyyy", null, null);
		CustomCalculationBasedOnMultiplePatientDataDefinitions decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		decline.setName("Decline");
		decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		decline.setCalculator(new DifferenceBetweenLastTwoObs());
		dataSetDefinition5.addColumn(decline, new HashMap<String, Object>());
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = RowPerPatientColumns.getDrugOrderForStartOfART("StartART", "dd-MMM-yyyy");
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions cd4Decline = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		cd4Decline.setName("cd4Decline");
		cd4Decline.addPatientDataToBeEvaluated(allCD4, new HashMap<String, Object>());
		cd4Decline.addPatientDataToBeEvaluated(startArt, new HashMap<String, Object>());
		DeclineHighestCD4 declineCD4 = new DeclineHighestCD4();
		declineCD4.setInitiationArt("StartART");
		declineCD4.setShortDisplay(true);
		cd4Decline.setCalculator(declineCD4);
		
		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition5.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition6.addParameter(new Parameter("location", "Location", Location.class));
		
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition5.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition6.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("LateVisit", dataSetDefinition1, mappings);
		reportDefinition.addDataSetDefinition("LateCD4Count", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("LostToFollowup", dataSetDefinition3, mappings);
		reportDefinition.addDataSetDefinition("LowBMI", dataSetDefinition4, mappings);
		reportDefinition.addDataSetDefinition("DecliningInCD4ByMoreThan50", dataSetDefinition5, mappings);
		reportDefinition.addDataSetDefinition("ViralLoadGreaterThan20InTheLast3Months", dataSetDefinition6, mappings);
	}
	
	private void setupProperties() {
		pmtctCombinedClinicMotherProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		
		nextVisitConcept = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
		
		adultFlowVisit = (gp.getForm(GlobalPropertiesManagement.ADULT_FLOW_VISIT)).getEncounterType();
		
		clinicalEnountersIncLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES);
		
		height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		
		viralLoad = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
		
		clinicalEncoutersExcLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST);		
		
	}
}
