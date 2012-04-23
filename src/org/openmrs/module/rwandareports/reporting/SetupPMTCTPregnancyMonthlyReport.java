package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
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
import org.openmrs.module.reporting.indicator.CohortIndicator;
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
import org.openmrs.module.rwandareports.customcalculator.BMICalculation;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.DifferenceBetweenLastTwoObs;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupPMTCTPregnancyMonthlyReport {
	
	protected final static Log log = LogFactory.getLog(SetupPMTCTPregnancyMonthlyReport.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program pmtctProgram;
	private Concept cd4;
    private Concept height;
	private Concept weight;
	private Concept viralLoad;
	private List<EncounterType>adultHivFlowsheetEncounter;
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTPregMonthlyTemplate.xls",
		    "XlsPMTCTPregMonthlyTemplate.xls_", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:9,dataset:PatientsMissedRdv|sheet:2,row:9,dataset:PatientsWithLastCd4|sheet:3,row:9,dataset:PmtctWithlowBMI|sheet:4,row:9,dataset:ViralLoadAbov20");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsPMTCTPregMonthlyTemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PMTCT Pregnancy Monthly Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Pregnancy Monthly Report");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		
		// in PMTCT Program  dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition1 = new RowPerPatientDataSetDefinition();
		dataSetDefinition1.setName("Patients in PMTCT Who have missed their visit by more than a week dataSetDefinition");
		RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
		dataSetDefinition2.setName("Patients in PMTCT With CD4 dataSetDefinition");
		RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
		dataSetDefinition3.setName("Patients in PMTCT With BMI below 18.5 dataSetDefinition");
		RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
		dataSetDefinition4.setName("Patients in PMTCT With Viral load > 20 in the last 3 months dataSetDefinition");
	

		InProgramCohortDefinition patientsInpmtctProgram = Cohorts.createInProgramParameterizableByDate(
		    "patientsInpmtctProgram", pmtctProgram);
	    dataSetDefinition1.addFilter(patientsInpmtctProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
    	dataSetDefinition2.addFilter(patientsInpmtctProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition3.addFilter(patientsInpmtctProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition4.addFilter(patientsInpmtctProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
		dataSetDefinition1.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition2.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition3.addFilter(patientsNotVoided, new HashMap<String, Object>());
		dataSetDefinition4.addFilter(patientsNotVoided, new HashMap<String, Object>());
		
		//==================================================================
		//  1. Patients who have missed their visit by more than a week in pmtct program
		//==================================================================

		SqlCohortDefinition missedRdvbymorethanaWeek=new SqlCohortDefinition("" +
				"SELECT DISTINCT pp.patient_id FROM patient_program pp " +
				"INNER JOIN " +
				"(SELECT enc.patient_id patient, MAX(enc.encounter_datetime) max_date FROM encounter enc " +
				"INNER JOIN encounter_type et ON enc.encounter_type = et.encounter_type_id " +
				"INNER JOIN patient p ON enc.patient_id = p.patient_id " +
				"WHERE et.encounter_type_id = 25 " +
				"GROUP BY enc.encounter_datetime) sel ON sel.patient = pp.patient_id " +
				"INNER JOIN patient p ON pp.patient_id = p.patient_id " +
				"INNER JOIN person pe ON p.patient_id = pe.person_id " +
				"INNER JOIN obs ON pp.patient_id = obs.person_id " +
				"INNER JOIN encounter enc ON p.patient_id = enc.patient_id " +
				"INNER JOIN form f ON enc.form_id = f.form_id " +
				"WHERE pe.dead = 0 AND p.voided = 0 AND enc.voided=0 AND pp.program_id = 6 AND f.form_id = 106 AND obs.concept_id = 5096 " +
				"AND obs.value_datetime > enc.encounter_datetime AND DATEDIFF(:endDate, obs.value_datetime) > 7");
		missedRdvbymorethanaWeek.setName("missedRdvbymorethanaWeek");
		missedRdvbymorethanaWeek.addParameter(new Parameter("endDate", "endDate", Date.class));
		 
		 dataSetDefinition1.addFilter(missedRdvbymorethanaWeek,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		//==================================================================
		//  2. Patients in PMTCT program with No CD4 in the last 6 months
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
		//   3. Patients with BMI less than 18.5
		//==================================================================
				
				SqlCohortDefinition patientWithLowBMI = new SqlCohortDefinition();
				patientWithLowBMI.setName("patientWithLowBMI");
				patientWithLowBMI
				        .setQuery("select w.person_id from (select * from (select o.person_id,o.value_numeric from obs o,concept c where o.voided=0 and o.value_numeric is not null and o.concept_id= c.concept_id and c.uuid='"
				                + height.getUuid()
				                + "' order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h,(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.voided=0 and o.value_numeric is not null and o.concept_id= c.concept_id and c.uuid='"
				                + weight.getUuid()
				                + "' order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w,(select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location) loc where loc.patient_id=w.person_id and w.person_id=h.person_id and ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)<18.5");
				patientWithLowBMI.addParameter(new Parameter("location", "location", Location.class));
				dataSetDefinition3.addFilter(patientWithLowBMI, new HashMap<String, Object>());
				
	  //==================================================================
	  //   4 . Patients with Viral Load >20 in the last three months
	 //==================================================================
				
				SqlCohortDefinition viralLoadGreaterThan20InLast3Months = new SqlCohortDefinition(
				        "select person_id from (select o.person_id,o.obs_datetime,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"
				                + viralLoad.getUuid()
				                + "' and o.value_numeric>20 and o.voided=0 and o.obs_datetime> :beforeDate and o.obs_datetime<= :onDate order by o.obs_datetime desc) as vload group by person_id");
				viralLoadGreaterThan20InLast3Months.setName("viralLoadGreaterThan20InLast3Months");
				viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("beforeDate", "beforeDate", Date.class));
				viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("onDate", "onDate", Date.class));
				viralLoadGreaterThan20InLast3Months.addParameter(new Parameter("location", "location", Location.class));
				dataSetDefinition4.addFilter(viralLoadGreaterThan20InLast3Months,ParameterizableUtil.createParameterMappings("beforeDate=${endDate-3m},onDate=${endDate}"));
				
	
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
		dataSetDefinition1.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(imbType, new HashMap<String, Object>());
		
		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("familyName");
		dataSetDefinition1.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(givenName, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(givenName, new HashMap<String, Object>());
		
		PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("givenName");
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
	
		MostRecentObservation cd4Countdate = RowPerPatientColumns.getMostRecentCD4("Most recent CD4", "@ddMMMyy");
		dataSetDefinition2.addColumn(cd4Countdate, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Countdate, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Countdate, new HashMap<String, Object>());
		
		DateDiff CD4InMonths = RowPerPatientColumns.getDifferenceSinceLastObservation("Months since last CD4", cd4, DateDiffType.MONTHS);
		CD4InMonths.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition2.addColumn(CD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(CD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(CD4InMonths, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		MostRecentObservation weight = RowPerPatientColumns.getMostRecentWeight("Weight", "@ddMMMyy");
		dataSetDefinition3.addColumn(weight, new HashMap<String, Object>());
		
		MostRecentObservation height = RowPerPatientColumns.getMostRecentHeight("Height", "@ddMMMyy");
		dataSetDefinition3.addColumn(height, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bmi = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bmi.setName("BMI");
		bmi.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		bmi.addPatientDataToBeEvaluated(height, new HashMap<String, Object>());
		BMICalculation bmiCalc = new BMICalculation();
		bmiCalc.setHeightName(height.getName());
		bmiCalc.setWeightName(weight.getName());
		bmi.setCalculator(bmiCalc);
		dataSetDefinition3.addColumn(bmi, new HashMap<String, Object>());
		
		MostRecentObservation viralLoad = RowPerPatientColumns.getMostRecentViralLoad("Last viralLoad", null);
		dataSetDefinition4.addColumn(viralLoad, new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType = RowPerPatientColumns.getRecentEncounterType("Last visit type",
			adultHivFlowsheetEncounter, new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType, new HashMap<String, Object>());
		
		DateDiff lateVisitInMonth = RowPerPatientColumns.getDifferenceSinceLastEncounter(
		    "Late visit in months", adultHivFlowsheetEncounter, DateDiffType.MONTHS);
		lateVisitInMonth.addParameter(new Parameter("endDate", "endDate", Date.class));
		dataSetDefinition1.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		MostRecentObservation returnVisitDate = RowPerPatientColumns.getMostRecentReturnVisitDate(
		    "Date of missed appointment", null);
		dataSetDefinition1.addColumn(returnVisitDate, new HashMap<String, Object>());
		
		PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
		dataSetDefinition1.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(address1, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(address1, new HashMap<String, Object>());
		
		PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("AccompName");
		dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(accompagnateur, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(accompagnateur, new HashMap<String, Object>());
		
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
		
		reportDefinition.addDataSetDefinition("PatientsMissedRdv", dataSetDefinition1,mappings);
		reportDefinition.addDataSetDefinition("PatientsWithLastCd4", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("PmtctWithlowBMI", dataSetDefinition3, mappings);
		reportDefinition.addDataSetDefinition("ViralLoadAbov20", dataSetDefinition4, mappings);
		
	}
	
	private void setupProperties() {
		pmtctProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
        height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		viralLoad = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
		adultHivFlowsheetEncounter = gp.getEncounterTypeList(GlobalPropertiesManagement.ADULT_HIV_FLOWSHEET_ENCOUNTER);
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");	
	}
}
