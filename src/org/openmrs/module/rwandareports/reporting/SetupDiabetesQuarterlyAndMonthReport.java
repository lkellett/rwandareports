package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PatientStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupDiabetesQuarterlyAndMonthReport {
	
	public Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties
	private Program DMProgram;
	
	private List<Program> DMPrograms = new ArrayList<Program>();
	
	private int DMEncounterTypeId;
	
	private EncounterType DMEncounterType;
	
	private Form DDBform;
	
	private Form DiabetesFlowVisit;
	
	private List<Form> DDBforms = new ArrayList<Form>();
	
	private List<EncounterType> patientsSeenEncounterTypes = new ArrayList<EncounterType>();
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private List<String> onOrBefOnOrAf = new ArrayList<String>();
	
	private Concept glucose;
	
	private Concept weight;
	
	private Concept height;
	
	private Concept diastolicBP;
	
	private Concept systolicBP;
	
	private Concept hbA1c;
	
	private Concept sensationInRightFoot;
	
	private Concept sensationInLeftFoot;
	
	private Concept creatinine;
	
	private Concept insulin7030;
	
	private Concept insulinLente;
	
	private Concept insulinRapide;
	
	private Concept lisinopril;
	
	private Concept captopril;
	
	private List<Concept> lisinoprilCaptopril = new ArrayList<Concept>();
	
	private List<Concept> diabetesDrugConcepts = new ArrayList<Concept>();
	
	private Concept metformin;
	
	private Concept glibenclimide;
	
	private ProgramWorkflowState diedState;
	
	private Concept admitToHospital;
	
	private Concept locOfHosp;
	
	private List<Drug> onAceInhibitorsDrugs = new ArrayList<Drug>();
	
	List<Concept> neuropathyConcepts = new ArrayList<Concept>();
	
	List<Concept> insulinConcepts = new ArrayList<Concept>();
	
	List<Concept> metforminAndGlibenclimideConcepts = new ArrayList<Concept>();
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		//Monthly report set-up
		ReportDefinition monthlyRd = new ReportDefinition();
		monthlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		monthlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		monthlyRd.setName("Diabetes Monthly Indicator Report");
		
		monthlyRd.addDataSetDefinition(createMonthlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: Start
		
		ReportDefinition quarterlyRd = new ReportDefinition();
		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		quarterlyRd.setName("Diabetes Quarterly Indicator Report");
		
		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: End
		
		ProgramEnrollmentCohortDefinition patientEnrolledInDM = new ProgramEnrollmentCohortDefinition();
		patientEnrolledInDM.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientEnrolledInDM.setPrograms(DMPrograms);
		
		monthlyRd.setBaseCohortDefinition(patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		quarterlyRd.setBaseCohortDefinition(patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		h.saveReportDefinition(monthlyRd);
		h.saveReportDefinition(quarterlyRd);
		
		ReportDesign monthlyDesign = h.createRowPerPatientXlsOverviewReportDesign(monthlyRd,
		    "DM_Monthly_Indicator_Report.xls", "Diabetes Monthly Indicator Report (Excel)", null);
		Properties monthlyProps = new Properties();
		monthlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Monthly Data Set");
		
		monthlyDesign.setProperties(monthlyProps);
		h.saveReportDesign(monthlyDesign);
		
		ReportDesign quarterlyDesign = h.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
		    "DM_Quarterly_Indicator_Report.xls", "Diabetes Quarterly Indicator Report (Excel)", null);
		Properties quarterlyProps = new Properties();
		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
		
		quarterlyDesign.setProperties(quarterlyProps);
		h.saveReportDesign(quarterlyDesign);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Diabetes Monthly Indicator Report (Excel)".equals(rd.getName())
			        || "Diabetes Quarterly Indicator Report (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Diabetes Quarterly Indicator Report");
		h.purgeReportDefinition("Diabetes Monthly Indicator Report");
		
	}
	
	// Create Monthly Location Data set
	public LocationHierachyIndicatorDataSetDefinition createMonthlyLocationDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
		        createMonthlyEncounterBaseDataSet());
		ldsd.addBaseDefinition(createMonthlyBaseDataSet());
		ldsd.setName("Encounter Monthly Data Set");
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private EncounterIndicatorDataSetDefinition createMonthlyEncounterBaseDataSet() {
		
		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
		
		eidsd.setName("eidsd");
		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createMonthlyIndicators(eidsd);
		return eidsd;
	}
	
	private void createMonthlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
		
		SqlEncounterQuery patientVisitsToDMClinic = new SqlEncounterQuery();
		
		patientVisitsToDMClinic
		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (encounter_type="
		                + DMEncounterTypeId
		                + " or form_id="
		                + DDBform.getId()
		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id)");
		patientVisitsToDMClinic.setName("patientVisitsToDMClinic");
		patientVisitsToDMClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToDMClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator patientVisitsToDMClinicMonthIndicator = new EncounterIndicator();
		patientVisitsToDMClinicMonthIndicator.setName("patientVisitsToDMClinicMonthIndicator");
		patientVisitsToDMClinicMonthIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToDMClinic,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${endDate-1m+1d}")));
		
		dsd.addColumn(patientVisitsToDMClinicMonthIndicator);
		
	}
	
	//Create Quarterly Encounter Data set
	
	public LocationHierachyIndicatorDataSetDefinition createQuarterlyLocationDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
		        createEncounterQuarterlyBaseDataSet());
		ldsd.addBaseDefinition(createQuarterlyBaseDataSet());
		ldsd.setName("Encounter Quarterly Data Set");
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private EncounterIndicatorDataSetDefinition createEncounterQuarterlyBaseDataSet() {
		
		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
		
		eidsd.setName("eidsd");
		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createQuarterlyIndicators(eidsd);
		return eidsd;
	}
	
	private void createQuarterlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
		//=======================================================================
		//  A1: Total # of patient visits to DM clinic in the last quarter
		//==================================================================
		SqlEncounterQuery patientVisitsToDMClinic = new SqlEncounterQuery();
		
		patientVisitsToDMClinic.setQuery("select distinct e.encounter_id from encounter e where (e.encounter_type="
		        + DMEncounterTypeId + " or e.form_id=" + DDBform.getId()
		        + ") and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.voided=0");
		patientVisitsToDMClinic.setName("patientVisitsToDMClinic");
		patientVisitsToDMClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToDMClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator patientVisitsToDMClinicQuarterIndicator = new EncounterIndicator();
		patientVisitsToDMClinicQuarterIndicator.setName("patientVisitsToDMClinicQuarterIndicator");
		patientVisitsToDMClinicQuarterIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToDMClinic,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${endDate-3m+1d}")));
		
		dsd.addColumn(patientVisitsToDMClinicQuarterIndicator);
		
	}
	
	// create monthly cohort Data set
	
	private CohortIndicatorDataSetDefinition createMonthlyBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Monthly Cohort Data Set");
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createMonthlyIndicators(dsd);
		return dsd;
	}
	
	// create quarterly cohort Data set
	private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Quarterly Cohort Data Set");
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		createQuarterlyIndicators(dsd);
		return dsd;
	}
	
	private void createQuarterlyIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		//=======================================================================
		//  A2: Total # of patients seen in the last month/quarter
		//==================================================================
		
		EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
		    onOrAfterOnOrBefore, patientsSeenEncounterTypes);
		
		EncounterCohortDefinition patientWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
		    onOrAfterOnOrBefore, DDBforms);
		
		CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
		patientsSeenComposition.setName("patientsSeenComposition");
		patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenComposition.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.setCompositionString("1 OR 2");
		
		CohortIndicator patientsSeenQuarterIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthOneIndicator = Indicators.newCountIndicator("patientsSeenMonthOneIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthTwoIndicator = Indicators.newCountIndicator("patientsSeenMonthTwoIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
		CohortIndicator patientsSeenMonthThreeIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));
		
		//=======================================================================
		// A3: Total # of new patients enrolled in the last month/quarter
		//==================================================================
		
		CompositionCohortDefinition patientEnrolledInDM = Cohorts.createEnrolledInProgramDuringPeriod("Enrolled In DM",
		    DMProgram);
		
		CohortIndicator patientEnrolledInDMQuarterIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInDMQuarterIndicator", patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
		CohortIndicator patientEnrolledInDMMonthOneIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInDMQuarterIndicator", patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
		CohortIndicator patientEnrolledInDMMonthTwooIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInDMQuarterIndicator", patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-2m+1d},endDate=${endDate-1m+1d}"));
		CohortIndicator patientEnrolledInDMMonthThreeIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInDMQuarterIndicator", patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate-2m+1d}"));
		
		//=======================================================================
		// A4: Total # of new patients with RDV in the last month/quarter
		//==================================================================
		
		CohortIndicator patientRDVQuarterIndicator = Indicators.newCountIndicator("patientRDVQuarterIndicator", patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientRDVMonthOneIndicator = Indicators.newCountIndicator("patientRDVMonthOneIndicator",
		    patientSeen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientRDVMonthTwooIndicator = Indicators.newCountIndicator("patientRDVMonthTwooIndicator",
		    patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
		CohortIndicator patientRDVMonthThreeIndicator = Indicators.newCountIndicator("patientRDVMonthThreeIndicator",
		    patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));
		
		//=======================================================================
		//B1: Pediatric:  Of the new patients enrolled in the last quarter, % ≤15 years old at intake
		//==================================================================
		
		SqlCohortDefinition patientsUnderFifteenAtEnrollementDate = Cohorts.createUnder15AtEnrollmentCohort(
		    "patientsUnder15AtEnrollment", DMProgram);
		
		CompositionCohortDefinition patientsUnderFifteenComposition = new CompositionCohortDefinition();
		patientsUnderFifteenComposition.setName("patientsUnderFifteenComposition");
		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsUnderFifteenComposition.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil
		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
		patientsUnderFifteenComposition.getSearches().put("2",
		    new Mapped<CohortDefinition>(patientsUnderFifteenAtEnrollementDate, null));
		patientsUnderFifteenComposition.setCompositionString("1 AND 2");
		
		CohortIndicator patientsUnderFifteenCountIndicator = Indicators.newCountIndicator(
		    "patientsUnderFifteenCountIndicator", patientsUnderFifteenComposition,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate}"));
		
		//=======================================================================
		//B2: Gender: Of the new patients enrolled in the last quarter, % male
		//==================================================================
		
		GenderCohortDefinition malePatients = Cohorts.createMaleCohortDefinition("Male patients");
		
		CompositionCohortDefinition malePatientsEnrolledInDM = new CompositionCohortDefinition();
		malePatientsEnrolledInDM.setName("malePatientsEnrolledIn");
		malePatientsEnrolledInDM.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		malePatientsEnrolledInDM.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		malePatientsEnrolledInDM.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsEnrolledInDM.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsEnrolledInDM.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil
		            .createParameterMappings("startDate=${enrolledOnOrAfter},endDate=${enrolledOnOrBefore}")));
		malePatientsEnrolledInDM.getSearches().put("2", new Mapped<CohortDefinition>(malePatients, null));
		malePatientsEnrolledInDM.setCompositionString("1 AND 2");
		
		CohortIndicator malePatientsEnrolledInDMCountIndicator = Indicators.newCountIndicator(
		    "malePatientsEnrolledInDMCountIndicator", malePatientsEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate}"));
		
		//=======================================================================
		//B3: Of the new patients enrolled in the last month/quarter, % with HbA1c done at intake
		//==================================================================
		
		//// collection for HbA1c not provided at intake, modification id needed on DDB form.
		
		//		CompositionCohortDefinition patientsEnrolledAndHaveHbAc1AtIntake = Cohorts
		//		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveHbAc1AtIntake",
		//		            DMProgram, DDBform, hbA1c);
		//		
		//		CohortIndicator patientsEnrolledAndHaveHbAc1AtIntakeCountIndicator = Indicators.newCountIndicator(
		//		    "patientsEnrolledAndHaveHbAc1AtIntakeCountIndicator", patientsEnrolledAndHaveHbAc1AtIntake,
		//		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
		
		//=======================================================================
		//B4: Of the new patients enrolled in the last month/quarter, % with Glucose done at intake
		//==================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveglucoseAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveglucoseAtIntake",
		            DMProgram, DDBform, glucose);
		
		CohortIndicator patientsEnrolledAndHaveglucoseAtIntakeCountIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndHaveglucoseAtIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
		
		//=======================================================================
		//B5: Of the new patients enrolled in the last month/quarter, % with Height Weight recorded at intake
		//==================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveWeightAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveWeightAtIntake",
		            DMProgram, DDBform, weight);
		CompositionCohortDefinition patientsEnrolledAndHaveHeightAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveHeightAtIntake",
		            DMProgram, DDBform, height);
		
		CompositionCohortDefinition heightWeightIntake = new CompositionCohortDefinition();
		heightWeightIntake.setName("patientsEnrolledAndHeightWeightRecordedAtIntake");
		heightWeightIntake.addParameter(new Parameter("startDate", "start", Date.class));
		heightWeightIntake.addParameter(new Parameter("endDate", "end", Date.class));
		heightWeightIntake.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveWeightAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		heightWeightIntake.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveHeightAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		heightWeightIntake.setCompositionString("1 AND 2");
		
		CohortIndicator patientsEnrolledAndHeightWeightRecordedAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndHeightWeightRecordedAtIntakeCountIndicator", heightWeightIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
		
		//=======================================================================
		//B6: Of the new patients enrolled in the last month/quarter, % with BP recorded at intake
		//==================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveDiastolicAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveDiastolicAtIntake",
		            DMProgram, DDBform, diastolicBP);
		CompositionCohortDefinition patientsEnrolledAndHaveSystolicAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveSystolicAtIntake",
		            DMProgram, DDBform, systolicBP);
		
		CompositionCohortDefinition patientsEnrolledAndBPRecordedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndBPRecordedAtIntake.setName("patientsEnrolledAndBPRecordedAtIntake");
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("startDate", "start", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("endDate", "end", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveDiastolicAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndBPRecordedAtIntake.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveSystolicAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndBPRecordedAtIntake.setCompositionString("1 AND 2");
		
		CohortIndicator patientsEnrolledAndBPRecordedAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndBPRecordedAtIntakeCountIndicator", patientsEnrolledAndBPRecordedAtIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m},endDate=${endDate}"));
		
		//=======================================================================
		//B7: Of the new patients enrolled in the last month/quarter, % with Neuropathy status recorded at intake
		//==================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveSensationLeftFootAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveDiastolicAtIntake",
		            DMProgram, DDBform, sensationInLeftFoot);
		CompositionCohortDefinition patientsEnrolledAndHaveSensationRightFootAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveSystolicAtIntake",
		            DMProgram, DDBform, sensationInRightFoot);
		
		CompositionCohortDefinition patientsEnrolledAndNeuropathyCheckedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndNeuropathyCheckedAtIntake.setName("patientsEnrolledAndNeuropathyCheckedAtIntake");
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("startDate", "start", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("endDate", "end", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveSensationLeftFootAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndNeuropathyCheckedAtIntake.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveSensationRightFootAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndNeuropathyCheckedAtIntake.setCompositionString("1 AND 2");
		
		CohortIndicator patientsEnrolledAndNeuropathyCheckedAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndNeuropathyCheckedAtIntakeCountIndicator", patientsEnrolledAndNeuropathyCheckedAtIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-3m+1d},endDate=${endDate}"));
		
		//=======================================================================
		//C1: Of total patients seen in the last quarter and are on ace inhibitors, % who had Creatinine tested in the last 6 months
		//==================================================================
		
		SqlCohortDefinition onAceInhibitor = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate("onAceInhibitor",
		    lisinoprilCaptopril);
		
		NumericObsCohortDefinition testedForCreatinine = Cohorts.createNumericObsCohortDefinition(
		    "patientsTestedForCreatinine", onOrAfterOnOrBefore, creatinine, 0, null, TimeModifier.LAST);
		
		CompositionCohortDefinition patientsSeenOnAceInhibitorsAndTestedForCreatinine = new CompositionCohortDefinition();
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.setName("patientsSeenOnAceInhibitorsAndTestedForCreatinine");
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine
		        .addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(onAceInhibitor, ParameterizableUtil
		            .createParameterMappings("endDate=${onOrBefore}")));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(testedForCreatinine, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-3m}")));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.getSearches().put(
		    "3",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.setCompositionString("1 AND 2 AND 3");
		
		CohortIndicator patientsSeenOnAceInhibitorsAndTestedForCreatinineCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenOnAceInhibitorsAndTestedForCreatinineCountIndicator",
		    patientsSeenOnAceInhibitorsAndTestedForCreatinine,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		CompositionCohortDefinition patientsSeenOnAceInhibitors = new CompositionCohortDefinition();
		patientsSeenOnAceInhibitors.setName("patientsSeenOnAceInhibitors");
		patientsSeenOnAceInhibitors.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenOnAceInhibitors.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenOnAceInhibitors.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(onAceInhibitor, ParameterizableUtil
		            .createParameterMappings("endDate=${onOrBefore}")));
		patientsSeenOnAceInhibitors.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenOnAceInhibitors.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenOnAceInhibitorsCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenOnAceInhibitorsCountIndicator", patientsSeenOnAceInhibitors,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		//=======================================================================
		//C2: Of total patients seen in the last quarter, % who had HbA1c tested in the last 6 months
		//==================================================================
		
		NumericObsCohortDefinition patientsTestedForHbA1c = Cohorts.createNumericObsCohortDefinition(
		    "patientsTestedForHbA1c", onOrAfterOnOrBefore, hbA1c, 0, null, TimeModifier.LAST);
		
		CompositionCohortDefinition patientsSeenAndTestedForHbA1c = new CompositionCohortDefinition();
		patientsSeenAndTestedForHbA1c.setName("patientsSeenAndTestedForHbA1c");
		patientsSeenAndTestedForHbA1c.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndTestedForHbA1c.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndTestedForHbA1c.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsTestedForHbA1c, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-3m}")));
		patientsSeenAndTestedForHbA1c.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndTestedForHbA1c.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndTestedForHbA1cCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndTestedForHbA1cCountIndicator", patientsSeenAndTestedForHbA1c,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		//=======================================================================
		//C3: Of all the patients ever registered (with Diabetes DDB), % ever tested for HbA1c
		//==================================================================
		
		EncounterCohortDefinition everRegisteredWithDDB = Cohorts.createEncounterBasedOnForms("EverRegistered with DDB",
		    "onOrBefore", DDBforms);
		
		NumericObsCohortDefinition everTestedForHbA1c = Cohorts.createNumericObsCohortDefinition("everTestedForHbA1c",
		    hbA1c, 0, null, TimeModifier.ANY);
		
		CompositionCohortDefinition everRegisteredWithDDBAndTestForHbA1c = new CompositionCohortDefinition();
		everRegisteredWithDDBAndTestForHbA1c.setName("everRegisteredWithDDBAndTestForHbA1c");
		everRegisteredWithDDBAndTestForHbA1c.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		everRegisteredWithDDBAndTestForHbA1c.getSearches().put("1", new Mapped<CohortDefinition>(everTestedForHbA1c, null));
		everRegisteredWithDDBAndTestForHbA1c.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(everRegisteredWithDDB, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		everRegisteredWithDDBAndTestForHbA1c.setCompositionString("1 AND 2");
		
		CohortIndicator everRegisteredWithDDBAndTestForHbA1cIndicatorNumerator = Indicators.newCountIndicator(
		    "everRegisteredWithDDBAndTestForHbA1cIndicatorNumerator", everRegisteredWithDDBAndTestForHbA1c,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
		
		CohortIndicator everRegisteredWithDDBAndTestForHbA1cIndicatorDenominator = Indicators.newCountIndicator(
		    "everRegisteredWithDDBAndTestForHbA1cIndicatorDenominator", everRegisteredWithDDB,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
		
		//=======================================================================
		//C4: Of total patients seen in the last quarter, % with height weight recorded at last visit
		//==================================================================
		
		SqlCohortDefinition heightAtLastVist = Cohorts.getPatientsWithObservationAtLastVisit("heightAtLastVist", height,
		    DMEncounterType);
		SqlCohortDefinition weightAtLastVist = Cohorts.getPatientsWithObservationAtLastVisit("weightAtLastVist", weight,
		    DMEncounterType);
		
		CompositionCohortDefinition patientsSeenAndBMIAtLastVist = new CompositionCohortDefinition();
		patientsSeenAndBMIAtLastVist.setName("patientsSeenAndBMIAtLastVist");
		patientsSeenAndBMIAtLastVist.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndBMIAtLastVist.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndBMIAtLastVist.getSearches().put("1", new Mapped<CohortDefinition>(heightAtLastVist, null));
		patientsSeenAndBMIAtLastVist.getSearches().put("2", new Mapped<CohortDefinition>(weightAtLastVist, null));
		
		patientsSeenAndBMIAtLastVist.getSearches().put(
		    "3",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndBMIAtLastVist.setCompositionString("1 AND 2 AND 3");
		
		CohortIndicator patientsSeenAndBMIAtLastVistCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndBMIAtLastVistCountIndicator", patientsSeenAndBMIAtLastVist,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		//=======================================================================
		//C5: Of total patients seen in the last quarter, % with BP recorded at last visit
		//==================================================================
		
		SqlCohortDefinition systolicBPAtLastVist = Cohorts.getPatientsWithObservationAtLastVisit("systolicBPAtLastVist",
		    systolicBP, DMEncounterType);
		SqlCohortDefinition diastolicBPAtLastVist = Cohorts.getPatientsWithObservationAtLastVisit("systolicBPAtLastVist",
		    diastolicBP, DMEncounterType);
		
		CompositionCohortDefinition patientsSeenAndBPAtLastVist = new CompositionCohortDefinition();
		patientsSeenAndBPAtLastVist.setName("patientsSeenAndBPAtLastVist");
		patientsSeenAndBPAtLastVist.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndBPAtLastVist.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndBPAtLastVist.getSearches().put("1", new Mapped<CohortDefinition>(systolicBPAtLastVist, null));
		patientsSeenAndBPAtLastVist.getSearches().put("2", new Mapped<CohortDefinition>(diastolicBPAtLastVist, null));
		
		patientsSeenAndBPAtLastVist.getSearches().put(
		    "3",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndBPAtLastVist.setCompositionString("(1 OR 2) AND 3");
		
		CohortIndicator patientsSeenAndBPAtLastVistCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndBPAtLastVistCountIndicator", patientsSeenAndBPAtLastVist,
		    ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		//=======================================================================
		//C6: Of total patients seen in the last year, % with Neuropathy checked in the last year
		//==================================================================
		
		SqlCohortDefinition neuropathyChecked = Cohorts.getPatientsWithObservationsBetweenStartDateAndEndDate(
		    "neuropathyChecked", neuropathyConcepts);
		
		CompositionCohortDefinition patientsSeenAndNeuropathyChecked = new CompositionCohortDefinition();
		patientsSeenAndNeuropathyChecked.setName("patientsSeenAndNeuropathyChecked");
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("start", "start", Date.class));
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("end", "end", Date.class));
		patientsSeenAndNeuropathyChecked.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndNeuropathyChecked.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(neuropathyChecked, ParameterizableUtil
		            .createParameterMappings("start=${start},end=${end}")));
		patientsSeenAndNeuropathyChecked.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndNeuropathyCheckedCountIndicator = Indicators
		        .newCountIndicator(
		            "patientsSeenAndNeuropathyCheckedCountIndicator",
		            patientsSeenAndNeuropathyChecked,
		            ParameterizableUtil
		                    .createParameterMappings("start=${endDate-12m+1d},end=${endDate},onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenInOneYearCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenInOneYearCountIndicator", patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		//=======================================================================
		//D1: Of total patients seen in the last month/quarter, % with no regimen documented
		//==================================================================
		
		SqlCohortDefinition patientOnRegimen = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate("patientsOnRegime",
		    diabetesDrugConcepts);
		
		CompositionCohortDefinition patientsSeenAndNotOnAnyDMRegimen = new CompositionCohortDefinition();
		patientsSeenAndNotOnAnyDMRegimen.setName("patientsSeenAndNotOnAnyDMRegimen");
		patientsSeenAndNotOnAnyDMRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndNotOnAnyDMRegimen.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndNotOnAnyDMRegimen.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndNotOnAnyDMRegimen.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientOnRegimen, ParameterizableUtil
		            .createParameterMappings("endDate=${onOrBefore}")));
		patientsSeenAndNotOnAnyDMRegimen.setCompositionString("1 AND (NOT 2)");
		
		CohortIndicator patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator", patientsSeenAndNotOnAnyDMRegimen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		//=======================================================================
		//D2: Of total patients seen in the last quarter, % on any type of insulin at last visit
		//==================================================================
		
		SqlCohortDefinition patientOnInsulinAtLastVist = Cohorts.getPatientsOnRegimenAtLastVisit(
		    "patientOnInsulinAtLastVist", insulinConcepts, DMEncounterType);
		
		CompositionCohortDefinition patientsSeenAndOnInsulin = new CompositionCohortDefinition();
		patientsSeenAndOnInsulin.setName("patientsSeenAndOnInsulin");
		patientsSeenAndOnInsulin.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulin.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulin.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulin.getSearches().put("2", new Mapped<CohortDefinition>(patientOnInsulinAtLastVist, null));
		patientsSeenAndOnInsulin.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndOnInsulinCountQuarterIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndOnInsulinCountQuarterIndicator", patientsSeenAndOnInsulin,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//D3: Of total patients seen in the last quarter and on any type of insulin at last visit, % who are on metformin
		//==================================================================
		
		SqlCohortDefinition onMetformin = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate("onMetformin", metformin);
		
		CompositionCohortDefinition patientsSeenAndOnInsulinAndOnMetformin = new CompositionCohortDefinition();
		patientsSeenAndOnInsulinAndOnMetformin.setName("patientsSeenAndOnInsulinAndOnMetformin");
		patientsSeenAndOnInsulinAndOnMetformin.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulinAndOnMetformin.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulinAndOnMetformin.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenAndOnInsulinAndOnMetformin.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulinAndOnMetformin.getSearches().put("2",
		    new Mapped<CohortDefinition>(onMetformin, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		patientsSeenAndOnInsulinAndOnMetformin.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndOnInsulinAndOnMetforminCountQuarterIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndOnInsulinAndOnMetforminCountQuarterIndicator", patientsSeenAndOnInsulinAndOnMetformin,
		    ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//D4: Of total patients seen in the last quarter and on any type of insulin at last visit, % who are on mixed insulin
		//==================================================================
		
		SqlCohortDefinition onInsulinMixte = Cohorts
		        .getPatientsOnCurrentRegimenBasedOnEndDate("onInsulinMixte", insulin7030);
		
		CompositionCohortDefinition patientsSeenAndOnInsulinAndOnInsulinMixte = new CompositionCohortDefinition();
		patientsSeenAndOnInsulinAndOnInsulinMixte.setName("patientsSeenAndOnInsulinAndOnMetformin");
		patientsSeenAndOnInsulinAndOnInsulinMixte.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulinAndOnInsulinMixte.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulinAndOnInsulinMixte.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenAndOnInsulinAndOnInsulinMixte.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulinAndOnInsulinMixte.getSearches().put("2",
		    new Mapped<CohortDefinition>(onInsulinMixte, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		patientsSeenAndOnInsulinAndOnInsulinMixte.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndOnInsulinAndOnInsulinMixteCountQuarterIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndOnInsulinAndOnInsulinMixteCountQuarterIndicator", patientsSeenAndOnInsulinAndOnInsulinMixte,
		    ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//D5: Of total patients seen in the last quarter and on any type of insulin at last visit, % who have accompanateurs
		//==================================================================		
		
		SqlCohortDefinition patientsWhitAccompagnateur = Cohorts.createPatientsWithAccompagnateur(
		    "allPatientsWhitAccompagnateur", "endDate");
		
		CompositionCohortDefinition patientsSeenAndOnInsulinAndWhitAccompagnateur = new CompositionCohortDefinition();
		patientsSeenAndOnInsulinAndWhitAccompagnateur.setName("patientsSeenAndOnInsulinAndWhitAccompagnateur");
		patientsSeenAndOnInsulinAndWhitAccompagnateur.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsWhitAccompagnateur, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndOnInsulinAndWhitAccompagnateurCountQuarterIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndOnInsulinAndWhitAccompagnateurCountQuarterIndicator",
		    patientsSeenAndOnInsulinAndWhitAccompagnateur, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate},endDate=${endDate}"));
		
		//=======================================================================
		//D6: Of total patients seen in the last quarter, % on oral medications only (metformin and/or glibenclimide)
		//==================================================================		
		
		SqlCohortDefinition onMetforminOrGlibenclimide = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
		    "onMetforminOrGlibenclimide", metforminAndGlibenclimideConcepts);
		
		CompositionCohortDefinition patientsSeenAndOnMetforminOrGlibenclimide = new CompositionCohortDefinition();
		patientsSeenAndOnMetforminOrGlibenclimide.setName("patientsSeenAndOnMetforminOrGlibenclimide");
		patientsSeenAndOnMetforminOrGlibenclimide.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnMetforminOrGlibenclimide.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnMetforminOrGlibenclimide.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenAndOnMetforminOrGlibenclimide.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnMetforminOrGlibenclimide.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(onMetforminOrGlibenclimide, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")));
		patientsSeenAndOnMetforminOrGlibenclimide.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndOnMetforminOrGlibenclimideCountQuarterIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndOnMetforminOrGlibenclimideCountQuarterIndicator", patientsSeenAndOnMetforminOrGlibenclimide,
		    ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//E1: Of total active patients, % who died
		//==================================================================		
		
		PatientStateCohortDefinition patientDied = Cohorts.createPatientStateCohortDefinition("Died patient", diedState);
		
		CompositionCohortDefinition activeAndDiedPatients = new CompositionCohortDefinition();
		activeAndDiedPatients.setName("activeAndDiedPatients");
		activeAndDiedPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndDiedPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndDiedPatients.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndDiedPatients.getSearches().put("2", new Mapped<CohortDefinition>(patientDied, null));
		activeAndDiedPatients.setCompositionString("1 AND 2");
		
		CohortIndicator activeAndDiedPatientsCountQuarterIndicator = Indicators.newCountIndicator(
		    "activeAndDiedPatientsCountQuarterIndicator", activeAndDiedPatients,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//E2: Of total active patients, % with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)
		//==================================================================
		
		SqlCohortDefinition patientHospitalized = Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate(
		    "patientHospitalized", DMEncounterType, locOfHosp);
		
		CompositionCohortDefinition activeAndHospitalizedPatients = new CompositionCohortDefinition();
		activeAndHospitalizedPatients.setName("activeAndHospitalizedPatients");
		activeAndHospitalizedPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeAndHospitalizedPatients.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndHospitalizedPatients.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientHospitalized, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		activeAndHospitalizedPatients.setCompositionString("1 AND 2");
		
		CohortIndicator activeAndHospitalizedPatientsCountQuarterIndicator = Indicators
		        .newCountIndicator(
		            "activeAndHospitalizedPatientsCountQuarterIndicator",
		            activeAndHospitalizedPatients,
		            ParameterizableUtil
		                    .createParameterMappings("endDate=${endDate},startDate=${endDate-3m+1d},onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//E3: Of total active patients, % with no visit 14 weeks or more past last visit date
		//==================================================================		
		
		EncounterCohortDefinition withDiabetesVisit = Cohorts.createEncounterParameterizedByDate("withDiabetesVisit",
		    onOrAfterOnOrBefore, DMEncounterType);
		
		CompositionCohortDefinition activeAndNotwithDiabetesVisitInFourteenWeeksPatients = new CompositionCohortDefinition();
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.setName("activeAndNotwithDiabetesVisitInFourteenWeeksPatients");
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients
		        .addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore",
		        Date.class));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(withDiabetesVisit, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter+12m-14w}")));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.setCompositionString("1 AND (NOT 2)");
		
		CohortIndicator activeAndNotwithDiabetesVisitInFourteenWeeksPatientsCountQuarterIndicator = Indicators
		        .newCountIndicator("activeAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicator",
		            activeAndNotwithDiabetesVisitInFourteenWeeksPatients,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//E4: Of total active patients on insulin at last visit, % with no visit 14 weeks or more past last visit date		
		//==================================================================
		
		CompositionCohortDefinition activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients = new CompositionCohortDefinition();
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients
		        .setName("activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients");
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrAfter",
		        "onOrAfter", Date.class));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrBefore",
		        "onOrBefore", Date.class));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(withDiabetesVisit, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter+12m-14w}")));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.setCompositionString("1 AND (NOT 2)");
		
		CohortIndicator activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicatorrs = Indicators
		        .newCountIndicator(
		            "activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicator",
		            activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		CohortIndicator activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsDenominatoCountQuarterIndicatorrs = Indicators
		        .newCountIndicator(
		            "activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsDenominatorCountQuarterIndicator",
		            patientsSeenAndOnInsulin,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//=======================================================================
		//E5: Of patients who have had HbA1c tested in the last quarter, % with last HbA1c <8 
		//==================================================================
		
		NumericObsCohortDefinition patientsWithLastHbA1cLessThanEight = Cohorts.createNumericObsCohortDefinition(
		    "patientsWithLastHbA1cLessThanEight", hbA1c, 8.0, RangeComparator.LESS_THAN, TimeModifier.LAST);
		
		CompositionCohortDefinition patientsTestedForHbA1cWithLastHbA1cLessThanEight = new CompositionCohortDefinition();
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.setName("patientsTestedForHbA1cWithLastHbA1cLessThanEight");
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsTestedForHbA1c, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.getSearches().put("2",
		    new Mapped<CohortDefinition>(patientsWithLastHbA1cLessThanEight, null));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.setCompositionString("1 AND 2");
		
		CohortIndicator patientsTestedForHbA1cWithLastHbA1cLessThanEightNumeratorCountQuarterIndicatorrs = Indicators
		        .newCountIndicator("patientsTestedForHbA1cWithLastHbA1cLessThanEightNumeratorCountQuarterIndicator",
		            patientsTestedForHbA1cWithLastHbA1cLessThanEight,
		            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		CohortIndicator patientsTestedForHbA1cWithLastHbA1cLessThanEightDenominatoCountQuarterIndicatorrs = Indicators
		        .newCountIndicator("patientsTestedForHbA1cWithLastHbA1cLessThanEightDenominatorCountQuarterIndicator",
		            patientsTestedForHbA1c,
		            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d}"));
		
		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn("A2Q", "Total # of patients seen in the last quarter", new Mapped(patientsSeenQuarterIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM1", "Total # of patients seen in the last month one", new Mapped(patientsSeenMonthOneIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM2", "Total # of patients seen in the last month two", new Mapped(patientsSeenMonthTwoIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM3", "Total # of patients seen in the last month three", new Mapped(
		        patientsSeenMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3Q", "Total # of new patients enrolled in the last quarter", new Mapped(
		        patientEnrolledInDMQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3QM1", "Total # of new patients enrolled in the month one", new Mapped(
		        patientEnrolledInDMMonthOneIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3QM2", "Total # of new patients enrolled in the month two", new Mapped(
		        patientEnrolledInDMMonthTwooIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		dsd.addColumn("A3QM3", "Total # of new patients enrolled in the month three", new Mapped(
		        patientEnrolledInDMMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		
		dsd.addColumn("A4Q", "Total # of new patients with RDV in the last quarter", new Mapped(patientRDVQuarterIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM1", "Total # of new patients with RDV in the month one", new Mapped(patientRDVMonthOneIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM2", "Total # of new patients with RDV in the month two", new Mapped(patientRDVMonthTwooIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM3", "Total # of new patients with RDV in the month three", new Mapped(
		        patientRDVMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "B1N",
		    "Pediatric: Of the new patients enrolled in the last quarter, number ≤15 years old at intake",
		    new Mapped(patientsUnderFifteenCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		
		dsd.addColumn("B2N", "Gender: Of the new patients enrolled in the last quarter, number male", new Mapped(
		        malePatientsEnrolledInDMCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		//		dsd.addColumn("B3NQ", "New patients enrolled in the last quarter, Number with HbA1c done at intake",
		//		new Mapped(patientsEnrolledAndHaveHbAc1AtIntakeCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "B4NQ",
		    "New patients enrolled in the last quarter, Number with Glucose done at intake",
		    new Mapped(patientsEnrolledAndHaveglucoseAtIntakeCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "B5NQ",
		    "New patients enrolled in the last quarter, Number with Height Weight Recorded recorded at intake",
		    new Mapped(patientsEnrolledAndHeightWeightRecordedAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "B6NQ",
		    "New patients enrolled in the last quarter, With BP recorded at intake",
		    new Mapped(patientsEnrolledAndBPRecordedAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "B7NQ",
		    "new patients enrolled in the last quarter, Number with Neuropathy checked at intake",
		    new Mapped(patientsEnrolledAndNeuropathyCheckedAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C1NQ",
		    "total patients seen in the last quarter and are on ace inhibitors, Numerator who had Creatinine tested in the last 6 months",
		    new Mapped(patientsSeenOnAceInhibitorsAndTestedForCreatinineCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C1DQ",
		    "total patients seen in the last quarter and are on ace inhibitors",
		    new Mapped(patientsSeenOnAceInhibitorsCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C2NQ",
		    "total patients seen in the last quarter, Numeric who had HbA1c tested in the last 6 months",
		    new Mapped(patientsSeenAndTestedForHbA1cCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C3NQ",
		    "all the patients ever registered (with Diabetes DDB), Number Numerator ever tested for HbA1c",
		    new Mapped(everRegisteredWithDDBAndTestForHbA1cIndicatorNumerator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "C3DQ",
		    "all the patients ever registered (with Diabetes DDB), Number Denominator ever tested for HbA1c",
		    new Mapped(everRegisteredWithDDBAndTestForHbA1cIndicatorDenominator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C4NQ",
		    "total patients seen in the last quarter, number with BMI recorded at last visit",
		    new Mapped(patientsSeenAndBMIAtLastVistCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C5NQ",
		    "total patients seen in the last quarter, number with BP recorded at last visit",
		    new Mapped(patientsSeenAndBPAtLastVistCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "C6NY",
		    "Of total patients seen in the last year, number with Neuropathy checked in the last year",
		    new Mapped(patientsSeenAndNeuropathyCheckedCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C6DY", "total patients seen in the last year", new Mapped(patientsSeenInOneYearCountIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "D1NQ",
		    "Total patients seen in the last month/quarter,number with no regimen documented",
		    new Mapped(patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "D2NQ",
		    "Total patients seen in the last quarter, Number on any type of insulin at last visit",
		    new Mapped(patientsSeenAndOnInsulinCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "D3NQ",
		    "Total patients seen in the last quarter and on any type of insulin at last visit, Number who are on metformin",
		    new Mapped(patientsSeenAndOnInsulinAndOnMetforminCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "D4NQ",
		    "Total patients seen in the last quarter and on any type of insulin at last visit, Number who are on mixed insulin",
		    new Mapped(patientsSeenAndOnInsulinAndOnInsulinMixteCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "D5NQ",
		    "Total patients seen in the last quarter and on any type of insulin at last visit, number who have accompanateurs",
		    new Mapped(patientsSeenAndOnInsulinAndWhitAccompagnateurCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "D6NQ",
		    "Total patients seen in the last quarter, number on oral medications only (metformin and/or glibenclimide)",
		    new Mapped(patientsSeenAndOnMetforminOrGlibenclimideCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "E1NQ",
		    "Total active patients, number who died",
		    new Mapped(activeAndDiedPatientsCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "E2NQ",
		    "Total active patients, number with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)",
		    new Mapped(activeAndHospitalizedPatientsCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "E3NQ",
		    "Total active patients, number with no visit 14 weeks or more past last visit date",
		    new Mapped(activeAndNotwithDiabetesVisitInFourteenWeeksPatientsCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn("E4NQ",
		    "Total active patients on insulin at last visit, Numerator with no visit 14 weeks or more past last visit date",
		    new Mapped(activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicatorrs,
		            ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "E4DQ",
		    "Total active patients on insulin at last visit, Denominator with no visit 14 weeks or more past last visit date",
		    new Mapped(activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsDenominatoCountQuarterIndicatorrs,
		            ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "E5NQ",
		    "Patients who have had HbA1c tested in the last quarter, Numerator with last HbA1c <8 ",
		    new Mapped(patientsTestedForHbA1cWithLastHbA1cLessThanEightNumeratorCountQuarterIndicatorrs, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E5DQ", "Patients who have had HbA1c tested in the last quarter, Denominator with last HbA1c <8 ",
		    new Mapped(patientsTestedForHbA1cWithLastHbA1cLessThanEightDenominatoCountQuarterIndicatorrs,
		            ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
	}
	
	private void createMonthlyIndicators(CohortIndicatorDataSetDefinition dsd) {
		//=======================================================
		// A2: Total # of patients seen in the last month/quarter
		//=======================================================
		EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
		    onOrAfterOnOrBefore, patientsSeenEncounterTypes);
		
		EncounterCohortDefinition patientWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
		    onOrAfterOnOrBefore, DDBforms);
		
		CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
		patientsSeenComposition.setName("patientsSeenComposition");
		patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenComposition.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.setCompositionString("1 OR 2");
		
		CohortIndicator patientsSeenMonthOneIndicator = Indicators.newCountIndicator("patientsSeenMonthOneIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		
		//=================================================================
		// A3: Total # of new patients enrolled in the last month/quarter
		//=================================================================
		
		CompositionCohortDefinition patientEnrolledInDM = Cohorts.createEnrolledInProgramDuringPeriod("Enrolled In DM",
		    DMProgram);
		
		CohortIndicator patientEnrolledInDMMonthOneIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInDMQuarterIndicator", patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
		
		//==============================================================
		// A4: Total # of patients with a return visit in the last month
		//==============================================================
		
		CohortIndicator patientsRDVMonthIndicator = Indicators.newCountIndicator("patientsRDVIndicator", patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		
		//B3: Of the new patients enrolled in the last month/quarter, % with HbA1c done at intake
		
		// collection for HbA1c not provided at intake, modification id needed on DDB form.		
		//		CompositionCohortDefinition patientsEnrolledAndHaveHbAc1AtIntake = Cohorts.getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveHbAc1AtIntake", DMProgram, DDBform, hbA1c);
		
		//		CohortIndicator patientsEnrolledAndHaveHbAc1AtIntakeCountMonthIndicator = Indicators
		//        .newCountIndicator(
		//            "patientsEnrolledAndHaveHbAc1AtIntakeCountMonthIndicator",
		//            patientsEnrolledAndHaveHbAc1AtIntake,
		//            ParameterizableUtil
		//                    .createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
		
		//B4: Of the new patients enrolled in the last month/quarter, % with Glucose done at intake
		
		CompositionCohortDefinition patientsEnrolledAndHaveglucoseAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveglucoseAtIntake",
		            DMProgram, DDBform, glucose);
		
		CohortIndicator patientsEnrolledAndHaveglucoseAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndHaveglucoseAtIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
		
		//=======================================================================================================
		//B5: Of the new patients enrolled in the last month/quarter, % with height and weight recorded at intake
		//=======================================================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveWeightAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveWeightAtIntake",
		            DMProgram, DDBform, weight);
		CompositionCohortDefinition patientsEnrolledAndHaveHeightAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveHeightAtIntake",
		            DMProgram, DDBform, height);
		
		CompositionCohortDefinition heightWeightIntake = new CompositionCohortDefinition();
		heightWeightIntake.setName("patientsEnrolledAndHeightWeightRecordedAtIntake");
		heightWeightIntake.addParameter(new Parameter("startDate", "start", Date.class));
		heightWeightIntake.addParameter(new Parameter("endDate", "endDate", Date.class));
		heightWeightIntake.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveWeightAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		heightWeightIntake.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveHeightAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		heightWeightIntake.setCompositionString("1 AND 2");
		
		CohortIndicator patientsEnrolledAndHeightWeightRecordedAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndHeightWeightRecordedAtIntakeCountIndicator", heightWeightIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
		
		//==========================================================================================
		//B6: Of the new patients enrolled in the last month/quarter, % with BP recorded at intake
		//==========================================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveDiastolicAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveDiastolicAtIntake",
		            DMProgram, DDBform, diastolicBP);
		CompositionCohortDefinition patientsEnrolledAndHaveSystolicAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveSystolicAtIntake",
		            DMProgram, DDBform, systolicBP);
		
		CompositionCohortDefinition patientsEnrolledAndBPRecordedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndBPRecordedAtIntake.setName("patientsEnrolledAndBPRecordedAtIntake");
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("startDate", "start", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("endDate", "end", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveDiastolicAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndBPRecordedAtIntake.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveSystolicAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndBPRecordedAtIntake.setCompositionString("1 AND 2");
		
		CohortIndicator patientsEnrolledAndBPRecordedAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndBPRecordedAtIntakeCountIndicator", patientsEnrolledAndBPRecordedAtIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m},endDate=${endDate}"));
		
		//==========================================================================================================
		//B7: Of the new patients enrolled in the last month/quarter, % with Neuropathy status recorded at intake
		//==========================================================================================================
		
		CompositionCohortDefinition patientsEnrolledAndHaveSensationLeftFootAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveDiastolicAtIntake",
		            DMProgram, DDBform, sensationInLeftFoot);
		CompositionCohortDefinition patientsEnrolledAndHaveSensationRightFootAtIntake = Cohorts
		        .getNewPatientsWithObservationAtIntakeBetweenStartAndEndDate("patientsEnrolledAndHaveSystolicAtIntake",
		            DMProgram, DDBform, sensationInRightFoot);
		
		CompositionCohortDefinition patientsEnrolledAndNeuropathyCheckedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndNeuropathyCheckedAtIntake.setName("patientsEnrolledAndNeuropathyCheckedAtIntake");
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("startDate", "start", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("endDate", "end", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveSensationLeftFootAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndNeuropathyCheckedAtIntake.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsEnrolledAndHaveSensationRightFootAtIntake, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsEnrolledAndNeuropathyCheckedAtIntake.setCompositionString("1 AND 2");
		
		CohortIndicator patientsEnrolledAndNeuropathyCheckedAtIntakeCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsEnrolledAndNeuropathyCheckedAtIntakeCountIndicator", patientsEnrolledAndNeuropathyCheckedAtIntake,
		    ParameterizableUtil.createParameterMappings("startDate=${endDate-1m+1d},endDate=${endDate}"));
		
		//==================================================================================
		//D1: Of total patients seen in the last month/quarter, % with no regimen documented
		//==================================================================================
		
		SqlCohortDefinition patientOnRegimen = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate("patientsOnRegime",
		    diabetesDrugConcepts);
		
		CompositionCohortDefinition patientsSeenAndNotOnAnyDMRegimen = new CompositionCohortDefinition();
		patientsSeenAndNotOnAnyDMRegimen.setName("patientsSeenAndNotOnAnyDMRegimen");
		patientsSeenAndNotOnAnyDMRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndNotOnAnyDMRegimen.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndNotOnAnyDMRegimen.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndNotOnAnyDMRegimen.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientOnRegimen, ParameterizableUtil
		            .createParameterMappings("endDate=${onOrBefore}")));
		patientsSeenAndNotOnAnyDMRegimen.setCompositionString("1 AND (NOT 2)");
		
		CohortIndicator patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator = Indicators.newCountIndicator(
		    "patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator", patientsSeenAndNotOnAnyDMRegimen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		
		dsd.addColumn("A2QM1", "Total # of patients seen in the last month one", new Mapped(patientsSeenMonthOneIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3QM1", "Total # of new patients enrolled in the month one", new Mapped(
		        patientEnrolledInDMMonthOneIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM1", "Total # of patients with return visit in the last month", new Mapped(
		        patientsRDVMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "B4NM",
		    "New patients enrolled in the last month, Number with Glucose done at intake",
		    new Mapped(patientsEnrolledAndHaveglucoseAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "B5NM",
		    "Of the new patients enrolled in the last month, Number with HeightWeight recorded at intake",
		    new Mapped(patientsEnrolledAndHeightWeightRecordedAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "B6NM",
		    "new patients enrolled in the last month, Number with BP recorded at intake",
		    new Mapped(patientsEnrolledAndBPRecordedAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "B7NM",
		    "new patients enrolled in the last month, Number with Neuropathy checked at intake",
		    new Mapped(patientsEnrolledAndNeuropathyCheckedAtIntakeCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn(
		    "D1NM",
		    "Total patients seen in the last month, number with no regimen documented",
		    new Mapped(patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
	}
	
	private void setUpProperties() {
		DMProgram = gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
		DMPrograms.add(DMProgram);
		DMEncounterTypeId = Integer.parseInt(Context.getAdministrationService().getGlobalProperty(
		    GlobalPropertiesManagement.DIABETES_VISIT));
		DMEncounterType = gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT);
		DDBform = gp.getForm(GlobalPropertiesManagement.DIABETES_DDB_FORM);
		DiabetesFlowVisit = gp.getForm(GlobalPropertiesManagement.DIABETES_FLOW_VISIT);
		DDBforms.add(DDBform);
		DDBforms.add(DiabetesFlowVisit);
		
		patientsSeenEncounterTypes.add(DMEncounterType);
		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		glucose = gp.getConcept(GlobalPropertiesManagement.GLUCOSE);
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		diastolicBP = gp.getConcept(GlobalPropertiesManagement.DIASTOLIC_BLOOD_PRESSURE);
		systolicBP = gp.getConcept(GlobalPropertiesManagement.SYSTOLIC_BLOOD_PRESSURE);
		hbA1c = gp.getConcept(GlobalPropertiesManagement.HBA1C);
		onOrBefOnOrAf.add("onOrBef");
		onOrBefOnOrAf.add("onOrAf");
		sensationInRightFoot = gp.getConcept(GlobalPropertiesManagement.SENSATION_IN_RIGHT_FOOT);
		sensationInLeftFoot = gp.getConcept(GlobalPropertiesManagement.SENSATION_IN_LEFT_FOOT);
		
		neuropathyConcepts.add(sensationInLeftFoot);
		neuropathyConcepts.add(sensationInRightFoot);
		
		lisinopril = gp.getConcept(GlobalPropertiesManagement.LISINOPRIL);
		captopril = gp.getConcept(GlobalPropertiesManagement.CAPTOPRIL);
		
		lisinoprilCaptopril.add(lisinopril);
		lisinoprilCaptopril.add(captopril);
		
		onAceInhibitorsDrugs.addAll(gp.getDrugs(lisinopril));
		onAceInhibitorsDrugs.addAll(gp.getDrugs(captopril));
		
		creatinine = gp.getConcept(GlobalPropertiesManagement.SERUM_CREATININE);
		insulin7030 = gp.getConcept(GlobalPropertiesManagement.INSULIN_70_30);
		insulinLente = gp.getConcept(GlobalPropertiesManagement.INSULIN_LENTE);
		insulinRapide = gp.getConcept(GlobalPropertiesManagement.INSULIN_RAPIDE);
		
		insulinConcepts.add(insulin7030);
		insulinConcepts.add(insulinLente);
		insulinConcepts.add(insulinRapide);
		
		diabetesDrugConcepts = gp.getConceptsByConceptSet(GlobalPropertiesManagement.DIABETES_TREATMENT_DRUG_SET);
		
		metformin = gp.getConcept(GlobalPropertiesManagement.METFORMIN_DRUG);
		glibenclimide = gp.getConcept(GlobalPropertiesManagement.GLIBENCLAMIDE_DRUG);
		
		metforminAndGlibenclimideConcepts.add(metformin);
		metforminAndGlibenclimideConcepts.add(glibenclimide);
		
	//	diedState = DMProgram.getWorkflow(28).getState("PATIENT DIED");
		
		diedState=DMProgram.getWorkflowByName(Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.DIABETE_TREATMENT_WORKFLOW)).getState(Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.PATIENT_DIED_STATE));
		
		admitToHospital = gp.getConcept(GlobalPropertiesManagement.HOSPITAL_ADMITTANCE);
		locOfHosp = gp.getConcept(GlobalPropertiesManagement.LOCATION_OF_HOSPITALIZATION);
	}
}
