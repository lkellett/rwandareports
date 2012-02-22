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
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PatientStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
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
	private List<Program> DMPrograms=new ArrayList<Program>();
	private int DMEncounterTypeId;
	private EncounterType DMEncounterType;
	private EncounterType adultInitialVisit;
	private Form DDBform;
	private List<Form> DDBforms=new ArrayList<Form>();
	private List<EncounterType> patientsSeenEncounterTypes=new ArrayList<EncounterType>();	
	private List<String> onOrAfterOnOrBefore=new ArrayList<String>();
	private List<String> onOrBefOnOrAf=new ArrayList<String>();
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
	private String lisinoprilCaptopril;
	private String splitedDiabetesConceptSet;
	private List<Concept> diabetesDrugConcepts=new ArrayList<Concept>();
	private Concept metformin;
	private Concept glibenclimide;	
	private ProgramWorkflowState diedState;
	private Concept admitToHospital;
	private Concept locOfHosp;
	
	
	private List<Drug> onAceInhibitorsDrugs=new ArrayList<Drug>();
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = new ReportDefinition();
    	rd.addParameter(new Parameter("endDate", "End Date", Date.class));
    	
    	Properties properties = new Properties();
    	properties.setProperty("hierarchyFields", "countyDistrict:District");
    	rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
    	
    	rd.setName("Diabetes Quarterly and Monthly Indicator Report");
    	
    	rd.addDataSetDefinition(createDataSet(),
    	    ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}"));
    	
    	h.saveReportDefinition(rd);
		
    	ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd,
		    "DM_Quarterly_Monthly_Indicator_Report.xls", "Diabetes Quarterly and Monthly Indicator Report (Excel)", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:Data Set");
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Diabetes Quarterly and Monthly Indicator Report (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Diabetes Quarterly and Monthly Indicator Report");
		
	}
	
	public LocationHierachyIndicatorDataSetDefinition createDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(createBaseDataSet());
		ldsd.setName("Data Set");
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Cohort Data Set");
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		
	/*	SqlEncounterQuery encq=new SqlEncounterQuery();
		encq.setQuery("select encounter_id from encounter where encounter_type=42");
		
		EncounterQueryResult s =new EncounterQueryResult(encq,new EvaluationContext());
		*/
		
		
// A2: Total # of patients seen in the last month/quarter
		
		EncounterCohortDefinition patientSeen=Cohorts.createEncounterParameterizedByDate("Patients seen", onOrAfterOnOrBefore, patientsSeenEncounterTypes);
		
		EncounterCohortDefinition patientWithDDB=Cohorts.createEncounterBasedOnForms("patientWithDDB", onOrAfterOnOrBefore, DDBforms);
				
		CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
		patientsSeenComposition.setName("patientsSeenComposition");
		patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenComposition.getSearches().put("1",new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.getSearches().put("2",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));		
		patientsSeenComposition.setCompositionString("1 OR 2");
		
		CohortIndicator patientsSeenQuarterIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator", patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthOneIndicator = Indicators.newCountIndicator("patientsSeenMonthOneIndicator", patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthTwoIndicator = Indicators.newCountIndicator("patientsSeenMonthTwoIndicator", patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m},onOrBefore=${endDate-1m}"));
		CohortIndicator patientsSeenMonthThreeIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator", patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate-2m}"));
		
		
// A3: Total # of new patients enrolled in the last month/quarter
		
		ProgramEnrollmentCohortDefinition patientEnrolledInDM=Cohorts.createProgramEnrollmentParameterizedByStartEndDate("Enrolled In DM", DMProgram);
		
		CohortIndicator patientEnrolledInDMQuarterIndicator = Indicators.newCountIndicator("patientEnrolledInDMQuarterIndicator", patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		CohortIndicator patientEnrolledInDMMonthOneIndicator = Indicators.newCountIndicator("patientEnrolledInDMQuarterIndicator", patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		CohortIndicator patientEnrolledInDMMonthTwooIndicator = Indicators.newCountIndicator("patientEnrolledInDMQuarterIndicator", patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-2m},enrolledOnOrBefore=${endDate-1m}"));
		CohortIndicator patientEnrolledInDMMonthThreeIndicator = Indicators.newCountIndicator("patientEnrolledInDMQuarterIndicator", patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate-2m}"));
		
		//B1: Pediatric:  Of the new patients enrolled in the last quarter, % ≤15 years old at intake
		
	//	AgeCohortDefinition patientsUnderFifteen=Cohorts.createUnder15AgeCohort("Under 15 years old");
		
		SqlCohortDefinition patientsUnderFifteenAtEnrollementDate=new SqlCohortDefinition("select distinct pp.patient_id from person p,patient_program pp where p.person_id=pp.patient_id and DATEDIFF(pp.date_enrolled,p.birthdate)<=5475 and pp.program_id= "+DMProgram.getId()+" and p.voided=0 and pp.voided=0");
		
		
		CompositionCohortDefinition patientsUnderFifteenComposition = new CompositionCohortDefinition();
		patientsUnderFifteenComposition.setName("patientsUnderFifteenComposition");
		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsUnderFifteenComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsUnderFifteenComposition.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsUnderFifteenComposition.getSearches().put("2",new Mapped<CohortDefinition>(patientsUnderFifteenAtEnrollementDate,null));
		patientsUnderFifteenComposition.setCompositionString("1 AND 2");
		
		CohortIndicator patientsUnderFifteenIndicator=Indicators.newFractionIndicator("patientsUnderFifteen", patientsUnderFifteenComposition, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsUnderFifteenCountIndicator=Indicators.newCountIndicator("patientsUnderFifteenCountIndicator", patientsUnderFifteenComposition, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		
		
		//B2: Gender: Of the new patients enrolled in the last quarter, % male
		
		GenderCohortDefinition malePatients=Cohorts.createMaleCohortDefinition("Male patients");
		
		CompositionCohortDefinition malePatientsEnrolledInDM = new CompositionCohortDefinition();
		malePatientsEnrolledInDM.setName("malePatientsEnrolledIn");
		malePatientsEnrolledInDM.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		malePatientsEnrolledInDM.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		malePatientsEnrolledInDM.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsEnrolledInDM.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsEnrolledInDM.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		malePatientsEnrolledInDM.getSearches().put("2",new Mapped<CohortDefinition>(malePatients, null));
		malePatientsEnrolledInDM.setCompositionString("1 AND 2");
		
        CohortIndicator malePatientsEnrolledInDMIndicator=Indicators.newFractionIndicator("malePatientsEnrolledInDMIndicator", malePatientsEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator malePatientsEnrolledInDMCountIndicator=Indicators.newCountIndicator("malePatientsEnrolledInDMCountIndicator", malePatientsEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		
		//B3: Of the new patients enrolled in the last month/quarter, % with HbA1c done at intake
		
		//// collection for HbA1c not provided at intake, modification id needed on DDB form.
		
		
		
		SqlCohortDefinition HbA1cAtIntake=new SqlCohortDefinition("select distinct o.person_id from encounter e, obs o where e.encounter_id=o.encounter_id and e.form_id="+DDBform.getId()+" and o.concept_id="+hbA1c.getId()+" and o.voided=0 and e.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		HbA1cAtIntake.setName("HbA1cAtIntake");
		HbA1cAtIntake.addParameter(new Parameter("start","start",Date.class));
		HbA1cAtIntake.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsEnrolledAndHaveHbAc1AtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndHaveHbAc1AtIntake.setName("patientsEnrolledAndHaveHbAc1AtIntake");
		patientsEnrolledAndHaveHbAc1AtIntake.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsEnrolledAndHaveHbAc1AtIntake.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsEnrolledAndHaveHbAc1AtIntake.addParameter(new Parameter("start", "start", Date.class));
		patientsEnrolledAndHaveHbAc1AtIntake.addParameter(new Parameter("end", "end", Date.class));
		patientsEnrolledAndHaveHbAc1AtIntake.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsEnrolledAndHaveHbAc1AtIntake.getSearches().put("2",new Mapped<CohortDefinition>(HbA1cAtIntake, ParameterizableUtil.createParameterMappings("start=${start},end=${end}")));
		patientsEnrolledAndHaveHbAc1AtIntake.setCompositionString("1 AND 2");

        CohortIndicator patientsEnrolledAndHaveHbAc1AtIntakeIndicator=Indicators.newFractionIndicator("patientsEnrolledAndHaveglucoseAtIntakeIndicator", patientsEnrolledAndHaveHbAc1AtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndHaveHbAc1AtIntakeCountIndicator=Indicators.newCountIndicator("patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndHaveHbAc1AtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsEnrolledAndHaveHbAc1AtIntakeMonthIndicator=Indicators.newFractionIndicator("patientsEnrolledAndHaveglucoseAtIntakeIndicator", patientsEnrolledAndHaveHbAc1AtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndHaveHbAc1AtIntakeCountMonthIndicator=Indicators.newCountIndicator("patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndHaveHbAc1AtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		
		
		
		
		//B4: Of the new patients enrolled in the last month/quarter, % with Glucose done at intake
		
		SqlCohortDefinition glucoseAtIntake=new SqlCohortDefinition("select distinct o.person_id from encounter e, obs o where e.encounter_id=o.encounter_id and e.form_id="+DDBform.getId()+" and o.concept_id="+glucose.getId()+" and o.voided=0 and e.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		glucoseAtIntake.setName("glucoseAtIntake");
		glucoseAtIntake.addParameter(new Parameter("start","start",Date.class));
		glucoseAtIntake.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsEnrolledAndHaveglucoseAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndHaveglucoseAtIntake.setName("patientsEnrolledAndHaveglucoseAtIntake");
		patientsEnrolledAndHaveglucoseAtIntake.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsEnrolledAndHaveglucoseAtIntake.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsEnrolledAndHaveglucoseAtIntake.addParameter(new Parameter("start", "start", Date.class));
		patientsEnrolledAndHaveglucoseAtIntake.addParameter(new Parameter("end", "end", Date.class));
		patientsEnrolledAndHaveglucoseAtIntake.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsEnrolledAndHaveglucoseAtIntake.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsEnrolledAndHaveglucoseAtIntake.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsEnrolledAndHaveglucoseAtIntake.getSearches().put("2",new Mapped<CohortDefinition>(glucoseAtIntake, ParameterizableUtil.createParameterMappings("start=${start},end=${end}")));
		patientsEnrolledAndHaveglucoseAtIntake.setCompositionString("1 AND 2");

        CohortIndicator patientsEnrolledAndHaveglucoseAtIntakeIndicator=Indicators.newFractionIndicator("patientsEnrolledAndHaveglucoseAtIntakeIndicator", patientsEnrolledAndHaveglucoseAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndHaveglucoseAtIntakeCountIndicator=Indicators.newCountIndicator("patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndHaveglucoseAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsEnrolledAndHaveglucoseAtIntakeMonthIndicator=Indicators.newFractionIndicator("patientsEnrolledAndHaveglucoseAtIntakeIndicator", patientsEnrolledAndHaveglucoseAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndHaveglucoseAtIntakeCountMonthIndicator=Indicators.newCountIndicator("patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndHaveglucoseAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		
        //B5: Of the new patients enrolled in the last month/quarter, % with BMI recorded at intake
		
		SqlCohortDefinition BMIAtIntake=new SqlCohortDefinition("select distinct o.person_id from encounter e, obs o where e.encounter_id=o.encounter_id and e.form_id="+DDBform.getId()+" and (o.concept_id="+weight.getId()+" or o.concept_id="+height.getId()+") and o.voided=0 and e.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		BMIAtIntake.setName("BMIAtIntake");
		BMIAtIntake.addParameter(new Parameter("start","start",Date.class));
		BMIAtIntake.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsEnrolledAndBMIRecordedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndBMIRecordedAtIntake.setName("patientsEnrolledAndBMIRecordedAtIntake");
		patientsEnrolledAndBMIRecordedAtIntake.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsEnrolledAndBMIRecordedAtIntake.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsEnrolledAndBMIRecordedAtIntake.addParameter(new Parameter("start", "start", Date.class));
		patientsEnrolledAndBMIRecordedAtIntake.addParameter(new Parameter("end", "end", Date.class));
		patientsEnrolledAndBMIRecordedAtIntake.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsEnrolledAndBMIRecordedAtIntake.getSearches().put("2",new Mapped<CohortDefinition>(BMIAtIntake, ParameterizableUtil.createParameterMappings("start=${start},end=${end}")));
		patientsEnrolledAndBMIRecordedAtIntake.setCompositionString("1 AND 2");

        CohortIndicator patientsEnrolledAndBMIRecordedAtIntakeIndicator=Indicators.newFractionIndicator("patientsEnrolledAndHaveglucoseAtIntakeIndicator", patientsEnrolledAndBMIRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndBMIRecordedAtIntakeCountIndicator=Indicators.newCountIndicator("patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndBMIRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsEnrolledAndBMIRecordedAtIntakeMonthIndicator=Indicators.newFractionIndicator("patientsEnrolledAndHaveglucoseAtIntakeIndicator", patientsEnrolledAndBMIRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndBMIRecordedAtIntakeCountMonthIndicator=Indicators.newCountIndicator("patientsEnrolledAndHaveglucoseAtIntakeCountIndicator", patientsEnrolledAndBMIRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		
        //B6: Of the new patients enrolled in the last month/quarter, % with BP recorded at intake
		
		SqlCohortDefinition BPAtIntake=new SqlCohortDefinition("select distinct o.person_id from encounter e, obs o where e.encounter_id=o.encounter_id and e.form_id="+DDBform.getId()+" and (o.concept_id="+diastolicBP.getId()+" or o.concept_id="+systolicBP.getId()+") and o.voided=0 and e.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		BPAtIntake.setName("BPAtIntake");
		BPAtIntake.addParameter(new Parameter("start","start",Date.class));
		BPAtIntake.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsEnrolledAndBPRecordedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndBPRecordedAtIntake.setName("patientsEnrolledAndBPRecordedAtIntake");
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("start", "start", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("end", "end", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsEnrolledAndBPRecordedAtIntake.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsEnrolledAndBPRecordedAtIntake.getSearches().put("2",new Mapped<CohortDefinition>(BPAtIntake, ParameterizableUtil.createParameterMappings("start=${start},end=${end}")));
		patientsEnrolledAndBPRecordedAtIntake.setCompositionString("1 AND 2");

        CohortIndicator patientsEnrolledAndBPRecordedAtIntakeIndicator=Indicators.newFractionIndicator("patientsEnrolledAndBPRecordedAtIntakeIndicator", patientsEnrolledAndBPRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndBPRecordedAtIntakeCountIndicator=Indicators.newCountIndicator("patientsEnrolledAndBPRecordedAtIntakeCountIndicator", patientsEnrolledAndBPRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsEnrolledAndBPRecordedAtIntakeMonthIndicator=Indicators.newFractionIndicator("patientsEnrolledAndBPRecordedAtIntakeIndicator", patientsEnrolledAndBPRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndBPRecordedAtIntakeCountMonthIndicator=Indicators.newCountIndicator("patientsEnrolledAndBPRecordedAtIntakeCountIndicator", patientsEnrolledAndBPRecordedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		
		
  //B7: Of the new patients enrolled in the last month/quarter, % with Neuropathy status recorded at intake
		
		SqlCohortDefinition NeuropathyAtIntake=new SqlCohortDefinition("select distinct o.person_id from encounter e, obs o where e.encounter_id=o.encounter_id and e.form_id="+DDBform.getId()+" and (o.concept_id="+sensationInLeftFoot.getId()+" or o.concept_id="+sensationInRightFoot.getId()+") and o.voided=0 and e.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		NeuropathyAtIntake.setName("NeuropathyAtIntake");
		NeuropathyAtIntake.addParameter(new Parameter("start","start",Date.class));
		NeuropathyAtIntake.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsEnrolledAndNeuropathyCheckedAtIntake = new CompositionCohortDefinition();
		patientsEnrolledAndNeuropathyCheckedAtIntake.setName("patientsEnrolledAndNeuropathyCheckedAtIntake");
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("start", "start", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("end", "end", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsEnrolledAndNeuropathyCheckedAtIntake.getSearches().put("1",new Mapped<CohortDefinition>(patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsEnrolledAndNeuropathyCheckedAtIntake.getSearches().put("2",new Mapped<CohortDefinition>(NeuropathyAtIntake, ParameterizableUtil.createParameterMappings("start=${start},end=${end}")));
		patientsEnrolledAndNeuropathyCheckedAtIntake.setCompositionString("1 AND 2");

        CohortIndicator patientsEnrolledAndNeuropathyCheckedAtIntakeIndicator=Indicators.newFractionIndicator("patientsEnrolledAndNeuropathyCheckedAtIntakeIndicator", patientsEnrolledAndNeuropathyCheckedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndNeuropathyCheckedAtIntakeCountIndicator=Indicators.newCountIndicator("patientsEnrolledAndNeuropathyCheckedAtIntakeCountIndicator", patientsEnrolledAndNeuropathyCheckedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-3m},end=${endDate},enrolledOnOrAfter=${endDate-3m},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsEnrolledAndNeuropathyCheckedAtIntakeMonthIndicator=Indicators.newFractionIndicator("patientsEnrolledAndNeuropathyCheckedAtIntakeIndicator", patientsEnrolledAndNeuropathyCheckedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"), patientEnrolledInDM, ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsEnrolledAndNeuropathyCheckedAtIntakeCountMonthIndicator=Indicators.newCountIndicator("patientsEnrolledAndNeuropathyCheckedAtIntakeCountIndicator", patientsEnrolledAndNeuropathyCheckedAtIntake, ParameterizableUtil.createParameterMappings("start=${endDate-1m},end=${endDate},enrolledOnOrAfter=${endDate-1m},enrolledOnOrBefore=${endDate}"));
		
		
		
		
		//C1: Of total patients seen in the last quarter and are on ace inhibitors, % who had Creatinine tested in the last 6 months
		
		SqlCohortDefinition onAceInhibitor=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.uuid in "+lisinoprilCaptopril+" and o.discontinued=0 and o.voided=0");
		
		
		SqlCohortDefinition testedForCreatinine=new SqlCohortDefinition("select distinct person_id from obs where concept_id="+creatinine.getId()+" and obs_datetime<= :end and obs_datetime>= :start and voided=0 and value_numeric is NOT NULL");
		testedForCreatinine.setName("patientsTestedForHbA1c");
		testedForCreatinine.addParameter(new Parameter("start","start",Date.class));
		testedForCreatinine.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsSeenOnAceInhibitorsAndTestedForCreatinine = new CompositionCohortDefinition();
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.setName("patientsSeenOnAceInhibitorsAndTestedForCreatinine");
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.addParameter(new Parameter("start","start",Date.class));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.addParameter(new Parameter("end","end",Date.class));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.getSearches().put("1",new Mapped<CohortDefinition>(onAceInhibitor, null));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.getSearches().put("2",new Mapped<CohortDefinition>(testedForCreatinine, ParameterizableUtil.createParameterMappings("end=${end},start=${start}")));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.getSearches().put("3",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenOnAceInhibitorsAndTestedForCreatinine.setCompositionString("1 AND 2 AND 3");
		
		CohortIndicator patientsSeenOnAceInhibitorsAndTestedForCreatinineIndicator=Indicators.newFractionIndicator("patientsSeenOnAceInhibitorsAndTestedForCreatinineIndicator", patientsSeenOnAceInhibitorsAndTestedForCreatinine,ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-6m},onOrBefore=${endDate},onOrAfter=${endDate-3m}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"));
		CohortIndicator patientsSeenOnAceInhibitorsAndTestedForCreatinineCountIndicator=Indicators.newCountIndicator("patientsSeenOnAceInhibitorsAndTestedForCreatinineCountIndicator", patientsSeenOnAceInhibitorsAndTestedForCreatinine,ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-6m},onOrBefore=${endDate},onOrAfter=${endDate-3m}"));

		
		//C2: Of total patients seen in the last quarter, % who had HbA1c tested in the last 6 months
		
		
		SqlCohortDefinition patientsTestedForHbA1c=new SqlCohortDefinition("select distinct person_id from obs where concept_id="+hbA1c.getId()+" and obs_datetime<= :end and obs_datetime>= :start and voided=0 and value_numeric is NOT NULL");
		patientsTestedForHbA1c.setName("patientsTestedForHbA1c");
		patientsTestedForHbA1c.addParameter(new Parameter("start","start",Date.class));
		patientsTestedForHbA1c.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition patientsSeenAndTestedForHbA1c = new CompositionCohortDefinition();
		patientsSeenAndTestedForHbA1c.setName("patientsSeenAndTestedForHbA1c");
		patientsSeenAndTestedForHbA1c.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndTestedForHbA1c.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndTestedForHbA1c.addParameter(new Parameter("start","start",Date.class));
		patientsSeenAndTestedForHbA1c.addParameter(new Parameter("end","end",Date.class));
		patientsSeenAndTestedForHbA1c.getSearches().put("1",new Mapped<CohortDefinition>(patientsTestedForHbA1c, ParameterizableUtil.createParameterMappings("end=${end},start=${start}")));
		patientsSeenAndTestedForHbA1c.getSearches().put("2",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndTestedForHbA1c.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndTestedForHbA1cIndicator=Indicators.newFractionIndicator("patientsSeenAndTestedForHbA1cIndicator", patientsSeenAndTestedForHbA1c,ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-6m},onOrBefore=${endDate},onOrAfter=${endDate-3m}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"));
		CohortIndicator patientsSeenAndTestedForHbA1cCountIndicator=Indicators.newCountIndicator("patientsSeenAndTestedForHbA1cCountIndicator", patientsSeenAndTestedForHbA1c,ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-6m},onOrBefore=${endDate},onOrAfter=${endDate-3m}"));

		//C3: Of all the patients ever registered (with Diabetes DDB), % ever tested for HbA1c
		
		EncounterCohortDefinition everRegisteredWithDDB=new EncounterCohortDefinition();
		everRegisteredWithDDB.setName("EverRegistered with DDB");
		everRegisteredWithDDB.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		everRegisteredWithDDB.setFormList(DDBforms);
			//Cohorts.createEncounterBasedOnForms("EverRegistered with DDB", "onOrBefore", DDBforms);
		
		
		SqlCohortDefinition everTestedForHbA1c=new SqlCohortDefinition("select distinct person_id from obs where concept_id="+hbA1c.getId()+" and voided=0 and value_numeric is NOT NULL");
		everTestedForHbA1c.setName("everTestedForHbA1c");
		
		
		
		CompositionCohortDefinition everRegisteredWithDDBAndTestForHbA1c = new CompositionCohortDefinition();
		everRegisteredWithDDBAndTestForHbA1c.setName("everRegisteredWithDDBAndTestForHbA1c");
		everRegisteredWithDDBAndTestForHbA1c.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		everRegisteredWithDDBAndTestForHbA1c.getSearches().put("1",new Mapped<CohortDefinition>(everTestedForHbA1c, null));
		everRegisteredWithDDBAndTestForHbA1c.getSearches().put("2",new Mapped<CohortDefinition>(everRegisteredWithDDB, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
		everRegisteredWithDDBAndTestForHbA1c.setCompositionString("1 AND 2");
		
		
		CohortIndicator everRegisteredWithDDBAndTestForHbA1cIndicator=Indicators.newFractionIndicator("everRegisteredWithDDBAndTestForHbA1cIndicator", everRegisteredWithDDBAndTestForHbA1c, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"), everRegisteredWithDDB, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
		CohortIndicator everRegisteredWithDDBAndTestForHbA1cIndicatorNumerator=Indicators.newCountIndicator("everRegisteredWithDDBAndTestForHbA1cIndicatorNumerator", everRegisteredWithDDBAndTestForHbA1c, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
		CohortIndicator everRegisteredWithDDBAndTestForHbA1cIndicatorDenominator=Indicators.newCountIndicator("everRegisteredWithDDBAndTestForHbA1cIndicatorDenominator", everRegisteredWithDDB, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
		
		
		
		//C4: Of total patients seen in the last quarter, % with BMI recorded at last visit

		SqlCohortDefinition BMIAtLastVist=new SqlCohortDefinition("select distinct height.patient_id from (select lastenc.encounter_id,lastenc.patient_id from obs o,(select * from (select * from encounter e where e.encounter_type="+DMEncounterType.getId()+" or e.form_id="+DDBform.getId()+" order by e.encounter_datetime desc) as lastencbypatient group by lastencbypatient.patient_id) lastenc where o.encounter_id=lastenc.encounter_id and o.concept_id="+height.getId()+" and o.voided=0) as height,(select lastenc.encounter_id,lastenc.patient_id from obs o,(select * from (select * from encounter e where e.encounter_type="+DMEncounterType.getId()+" or e.form_id="+DDBform.getId()+" order by e.encounter_datetime desc) as lastencbypatient group by lastencbypatient.patient_id) lastenc where o.encounter_id=lastenc.encounter_id and o.concept_id="+weight.getId()+" and o.voided=0) as weight where weight.encounter_id=height.encounter_id");
		
		CompositionCohortDefinition patientsSeenAndBMIAtLastVist = new CompositionCohortDefinition();
		patientsSeenAndBMIAtLastVist.setName("patientsSeenAndBMIAtLastVist");
		patientsSeenAndBMIAtLastVist.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndBMIAtLastVist.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndBMIAtLastVist.getSearches().put("1",new Mapped<CohortDefinition>(BMIAtLastVist, null));
		patientsSeenAndBMIAtLastVist.getSearches().put("2",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndBMIAtLastVist.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndBMIAtLastVistIndicator=Indicators.newFractionIndicator("patientsSeenAndBMIAtLastVistIndicator", patientsSeenAndBMIAtLastVist,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"));
		CohortIndicator patientsSeenAndBMIAtLastVistCountIndicator=Indicators.newCountIndicator("patientsSeenAndBMIAtLastVistCountIndicator", patientsSeenAndBMIAtLastVist,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"));

		
		//C5: Of total patients seen in the last quarter, % with BP recorded at last visit
		
		SqlCohortDefinition BPAtLastVist=new SqlCohortDefinition("select o.person_id from obs o,(select * from (select * from encounter e where (e.encounter_type="+DMEncounterType.getId()+" or e.form_id="+DDBform.getId()+") and e.voided=0 order by e.encounter_datetime desc) as lastencbypatient group by lastencbypatient.patient_id) as lastenc where lastenc.encounter_id=o.encounter_id and (o.concept_id= "+diastolicBP.getId()+" or o.concept_id= "+systolicBP.getId()+") and o.voided=0 group by o.person_id");
		
		CompositionCohortDefinition patientsSeenAndBPAtLastVist = new CompositionCohortDefinition();
		patientsSeenAndBPAtLastVist.setName("patientsSeenAndBPAtLastVist");
		patientsSeenAndBPAtLastVist.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndBPAtLastVist.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndBPAtLastVist.getSearches().put("1",new Mapped<CohortDefinition>(BPAtLastVist, null));
		patientsSeenAndBPAtLastVist.getSearches().put("2",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndBPAtLastVist.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenAndBPAtLastVistIndicator=Indicators.newFractionIndicator("patientsSeenAndBPAtLastVistIndicator", patientsSeenAndBPAtLastVist,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"));
		CohortIndicator patientsSeenAndBPAtLastVistCountIndicator=Indicators.newCountIndicator("patientsSeenAndBPAtLastVistCountIndicator", patientsSeenAndBPAtLastVist,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m}"));

		
		
		//C6: Of total patients seen in the last year, % with Neuropathy checked in the last year

		SqlCohortDefinition neuropathyChecked=new SqlCohortDefinition("select distinct o.person_id from encounter e, obs o where e.encounter_id=o.encounter_id and (e.form_id="+DDBform.getId()+" or e.encounter_type="+DMEncounterType.getId()+") and (o.concept_id="+sensationInLeftFoot.getId()+" or o.concept_id="+sensationInRightFoot.getId()+") and o.voided=0 and e.voided=0 and o.obs_datetime>= :start and o.obs_datetime<= :end and o.value_numeric is NOT NULL");
		neuropathyChecked.setName("neuropathyChecked");
		neuropathyChecked.addParameter(new Parameter("start","start",Date.class));
		neuropathyChecked.addParameter(new Parameter("end","end",Date.class));
		
		
		
		CompositionCohortDefinition patientsSeenAndNeuropathyChecked = new CompositionCohortDefinition();
		patientsSeenAndNeuropathyChecked.setName("patientsSeenAndNeuropathyChecked");
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("start", "start", Date.class));
		patientsSeenAndNeuropathyChecked.addParameter(new Parameter("end", "end", Date.class));
		patientsSeenAndNeuropathyChecked.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndNeuropathyChecked.getSearches().put("2",new Mapped<CohortDefinition>(neuropathyChecked, ParameterizableUtil.createParameterMappings("start=${start},end=${end}")));
		patientsSeenAndNeuropathyChecked.setCompositionString("1 AND 2");
			
        CohortIndicator patientsSeenAndNeuropathyCheckedIndicator=Indicators.newFractionIndicator("patientsSeenAndNeuropathyCheckedIndicator", patientsSeenAndNeuropathyChecked, ParameterizableUtil.createParameterMappings("start=${endDate-12m},end=${endDate},onOrAfter=${endDate-12m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndNeuropathyCheckedCountIndicator=Indicators.newCountIndicator("patientsSeenAndNeuropathyCheckedCountIndicator", patientsSeenAndNeuropathyChecked, ParameterizableUtil.createParameterMappings("start=${endDate-12m},end=${endDate},onOrAfter=${endDate-12m},onOrBefore=${endDate}"));

		CohortIndicator patientsSeenInOneYearCountIndicator=Indicators.newCountIndicator("patientsSeenInOneYearCountIndicator", patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));

		
		
		//D1: Of total patients seen in the last month/quarter, % with no regimen documented
		
		SqlCohortDefinition patientOnRegimen=new SqlCohortDefinition("select distinct patient_id from orders where concept_id in ("+splitedDiabetesConceptSet+") and voided=0 and discontinued=0");
		
		CompositionCohortDefinition patientsSeenAndNotOnAnyDMRegimen = new CompositionCohortDefinition();
		patientsSeenAndNotOnAnyDMRegimen.setName("patientsSeenAndNotOnAnyDMRegimen");
		patientsSeenAndNotOnAnyDMRegimen.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndNotOnAnyDMRegimen.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndNotOnAnyDMRegimen.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndNotOnAnyDMRegimen.getSearches().put("2",new Mapped<CohortDefinition>(patientOnRegimen, null));
		patientsSeenAndNotOnAnyDMRegimen.setCompositionString("1 AND (NOT 2)");
			
       	CohortIndicator patientsSeenAndNotOnAnyDMRegimenQuarterIndicator=Indicators.newFractionIndicator("patientsSeenAndNotOnAnyDMRegimenQuarterIndicator", patientsSeenAndNotOnAnyDMRegimen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndNotOnAnyDMRegimenCountQuarterIndicator=Indicators.newCountIndicator("patientsSeenAndNotOnAnyDMRegimenCountQuarterIndicator", patientsSeenAndNotOnAnyDMRegimen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
        CohortIndicator patientsSeenAndNotOnAnyDMRegimenMonthIndicator=Indicators.newFractionIndicator("patientsSeenAndNotOnAnyDMRegimenMonthIndicator", patientsSeenAndNotOnAnyDMRegimen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator=Indicators.newCountIndicator("patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator", patientsSeenAndNotOnAnyDMRegimen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m},onOrBefore=${endDate}"));
		
		
        //D2: Of total patients seen in the last quarter, % on any type of insulin at last visit
		
		SqlCohortDefinition patientOnInsulin=new SqlCohortDefinition("select o.patient_id from orders o,(select * from (select * from encounter e where (e.encounter_type=42 or e.form_id=83) and e.voided=0 order by e.encounter_datetime desc) as lastencbypatient group by lastencbypatient.patient_id) as lastenc where lastenc.patient_id>=o.patient_id and lastenc.encounter_datetime>=o.start_date and (o.concept_id= "+insulin7030.getId()+" or o.concept_id= "+insulinLente.getId()+" or o.concept_id= "+insulinRapide.getId()+") and o.discontinued=0 and o.voided=0 group by o.patient_id;");
		
		CompositionCohortDefinition patientsSeenAndOnInsulin = new CompositionCohortDefinition();
		patientsSeenAndOnInsulin.setName("patientsSeenAndOnInsulin");
		patientsSeenAndOnInsulin.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulin.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulin.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulin.getSearches().put("2",new Mapped<CohortDefinition>(patientOnInsulin, null));
		patientsSeenAndOnInsulin.setCompositionString("1 AND 2");
			
       	CohortIndicator patientsSeenAndOnInsulinQuarterIndicator=Indicators.newFractionIndicator("patientsSeenAndOnInsulinQuarterIndicator", patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndOnInsulinCountQuarterIndicator=Indicators.newCountIndicator("patientsSeenAndOnInsulinCountQuarterIndicator", patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		
		
		//D3: Of total patients seen in the last quarter and on any type of insulin at last visit, % who are on metformin

		SqlCohortDefinition onMetformin=new SqlCohortDefinition("select distinct patient_id from orders where concept_id = "+metformin.getId()+" and voided=0 and discontinued=0");
		
		CompositionCohortDefinition patientsSeenAndOnInsulinAndOnMetformin = new CompositionCohortDefinition();
		patientsSeenAndOnInsulinAndOnMetformin.setName("patientsSeenAndOnInsulinAndOnMetformin");
		patientsSeenAndOnInsulinAndOnMetformin.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulinAndOnMetformin.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulinAndOnMetformin.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulinAndOnMetformin.getSearches().put("2",new Mapped<CohortDefinition>(onMetformin, null));
		patientsSeenAndOnInsulinAndOnMetformin.setCompositionString("1 AND 2");
		
	    CohortIndicator patientsSeenAndOnInsulinAndOnMetforminQuarterIndicator=Indicators.newFractionIndicator("patientsSeenAndOnInsulinAndOnMetforminQuarterIndicator", patientsSeenAndOnInsulinAndOnMetformin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"), patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndOnInsulinAndOnMetforminCountQuarterIndicator=Indicators.newCountIndicator("patientsSeenAndOnInsulinAndOnMetforminCountQuarterIndicator", patientsSeenAndOnInsulinAndOnMetformin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		//D4: Of total patients seen in the last quarter and on any type of insulin at last visit, % who are on mixed insulin
		
        SqlCohortDefinition onInsulinMixte=new SqlCohortDefinition("select distinct patient_id from orders where concept_id = "+insulin7030.getId()+" and voided=0 and discontinued=0");
		
		CompositionCohortDefinition patientsSeenAndOnInsulinAndOnInsulinMixte = new CompositionCohortDefinition();
		patientsSeenAndOnInsulinAndOnInsulinMixte.setName("patientsSeenAndOnInsulinAndOnMetformin");
		patientsSeenAndOnInsulinAndOnInsulinMixte.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulinAndOnInsulinMixte.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulinAndOnInsulinMixte.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulinAndOnInsulinMixte.getSearches().put("2",new Mapped<CohortDefinition>(onInsulinMixte, null));
		patientsSeenAndOnInsulinAndOnInsulinMixte.setCompositionString("1 AND 2");
		
	    CohortIndicator patientsSeenAndOnInsulinAndOnInsulinMixteQuarterIndicator=Indicators.newFractionIndicator("patientsSeenAndOnInsulinAndOnInsulinMixteQuarterIndicator", patientsSeenAndOnInsulinAndOnInsulinMixte, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"), patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndOnInsulinAndOnInsulinMixteCountQuarterIndicator=Indicators.newCountIndicator("patientsSeenAndOnInsulinAndOnInsulinMixteCountQuarterIndicator", patientsSeenAndOnInsulinAndOnInsulinMixte, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		
		//D5: Of total patients seen in the last quarter and on any type of insulin at last visit, % who have accompanateurs
		
		SqlCohortDefinition patientsWhitAccompagnateur = Cohorts.createPatientsWithAccompagnateur(
			    "allPatientsWhitAccompagnateur", "endDate");
		
		CompositionCohortDefinition patientsSeenAndOnInsulinAndWhitAccompagnateur = new CompositionCohortDefinition();
		patientsSeenAndOnInsulinAndWhitAccompagnateur.setName("patientsSeenAndOnInsulinAndWhitAccompagnateur");
		patientsSeenAndOnInsulinAndWhitAccompagnateur.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.getSearches().put("2",new Mapped<CohortDefinition>(patientsWhitAccompagnateur, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		patientsSeenAndOnInsulinAndWhitAccompagnateur.setCompositionString("1 AND 2");
		
	    CohortIndicator patientsSeenAndOnInsulinAndWhitAccompagnateurQuarterIndicator=Indicators.newFractionIndicator("patientsSeenAndOnInsulinAndWhitAccompagnateurQuarterIndicator", patientsSeenAndOnInsulinAndWhitAccompagnateur, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate},endDate=${endDate}"), patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndOnInsulinAndWhitAccompagnateurCountQuarterIndicator=Indicators.newCountIndicator("patientsSeenAndOnInsulinAndWhitAccompagnateurCountQuarterIndicator", patientsSeenAndOnInsulinAndWhitAccompagnateur, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate},endDate=${endDate}"));
		
		
		
		//D6: Of total patients seen in the last quarter, % on oral medications only (metformin and/or glibenclimide)
		
		SqlCohortDefinition onMetforminOrGlibenclimide=new SqlCohortDefinition("select distinct patient_id from orders where (concept_id = "+glibenclimide.getId()+" or concept_id = "+metformin.getId()+") and voided=0 and discontinued=0");
		
		CompositionCohortDefinition patientsSeenAndOnMetforminOrGlibenclimide = new CompositionCohortDefinition();
		patientsSeenAndOnMetforminOrGlibenclimide.setName("patientsSeenAndOnMetforminOrGlibenclimide");
		patientsSeenAndOnMetforminOrGlibenclimide.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenAndOnMetforminOrGlibenclimide.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenAndOnMetforminOrGlibenclimide.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenAndOnMetforminOrGlibenclimide.getSearches().put("2",new Mapped<CohortDefinition>(onMetforminOrGlibenclimide, null));
		patientsSeenAndOnMetforminOrGlibenclimide.setCompositionString("1 AND 2");
			
       	CohortIndicator patientsSeenAndOnMetforminOrGlibenclimideQuarterIndicator=Indicators.newFractionIndicator("patientsSeenAndOnMetforminOrGlibenclimideQuarterIndicator", patientsSeenAndOnMetforminOrGlibenclimide, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenAndOnMetforminOrGlibenclimideCountQuarterIndicator=Indicators.newCountIndicator("patientsSeenAndOnMetforminOrGlibenclimideCountQuarterIndicator", patientsSeenAndOnMetforminOrGlibenclimide, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"));
		
		//E1: Of total active patients, % who died
		
		PatientStateCohortDefinition patientDied=Cohorts.createPatientStateCohortDefinition("Died patient", diedState);
		
		CompositionCohortDefinition activeAndDiedPatients = new CompositionCohortDefinition();
		activeAndDiedPatients.setName("activeAndDiedPatients");
		activeAndDiedPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndDiedPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndDiedPatients.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndDiedPatients.getSearches().put("2",new Mapped<CohortDefinition>(patientDied, null));
		activeAndDiedPatients.setCompositionString("1 AND 2");
		
	    CohortIndicator activeAndDiedPatientsQuarterIndicator=Indicators.newFractionIndicator("activeAndDiedPatientsQuarterIndicator", activeAndDiedPatients, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		CohortIndicator activeAndDiedPatientsCountQuarterIndicator=Indicators.newCountIndicator("activeAndDiedPatientsCountQuarterIndicator", activeAndDiedPatients, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		//E2: Of total active patients, % with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)
		
		SqlCohortDefinition patientHospitalized=new SqlCohortDefinition("select distinct o.person_id from obs o,encounter e where o.encounter_id=e.encounter_id and o.concept_id="+locOfHosp.getId()+" and e.encounter_type="+DMEncounterType.getId()+" and o.obs_datetime>= :start and o.obs_datetime<= :end and o.voided=0 and e.voided=0");
		patientHospitalized.addParameter(new Parameter("start","start",Date.class));
		patientHospitalized.addParameter(new Parameter("end","end",Date.class));
		
		CompositionCohortDefinition activeAndHospitalizedPatients = new CompositionCohortDefinition();
		activeAndHospitalizedPatients.setName("activeAndHospitalizedPatients");
		activeAndHospitalizedPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("end", "end", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("start", "start", Date.class));		
		activeAndHospitalizedPatients.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndHospitalizedPatients.getSearches().put("2",new Mapped<CohortDefinition>(patientHospitalized, ParameterizableUtil.createParameterMappings("end=${end},start=${start}")));
		activeAndHospitalizedPatients.setCompositionString("1 AND 2");
		
	    CohortIndicator activeAndHospitalizedPatientsQuarterIndicator=Indicators.newFractionIndicator("activeAndHospitalizedPatientsQuarterIndicator", activeAndHospitalizedPatients, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-3m},onOrAfter=${endDate-12m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		CohortIndicator activeAndHospitalizedPatientsCountQuarterIndicator=Indicators.newCountIndicator("activeAndHospitalizedPatientsCountQuarterIndicator", activeAndHospitalizedPatients, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-3m},onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		//E3: Of total active patients, % with no visit 14 weeks or more past last visit date
		
		SqlCohortDefinition	withDiabetesVisit=new SqlCohortDefinition("select patient_id from encounter where encounter_type="+DMEncounterType.getId()+" and encounter_datetime>= :start and encounter_datetime<= :end and voided=0");
		withDiabetesVisit.addParameter(new Parameter("end", "end", Date.class));
		withDiabetesVisit.addParameter(new Parameter("start", "start", Date.class));
		
		CompositionCohortDefinition activeAndNotwithDiabetesVisitInFourteenWeeksPatients = new CompositionCohortDefinition();
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.setName("activeAndNotwithDiabetesVisitInFourteenWeeksPatients");
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("end", "end", Date.class));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("start", "start", Date.class));		
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put("2",new Mapped<CohortDefinition>(withDiabetesVisit, ParameterizableUtil.createParameterMappings("end=${end},start=${start}")));
		activeAndNotwithDiabetesVisitInFourteenWeeksPatients.setCompositionString("1 AND (NOT 2)");
		
	    CohortIndicator activeAndNotwithDiabetesVisitInFourteenWeeksPatientsQuarterIndicator=Indicators.newFractionIndicator("activeAndNotwithDiabetesVisitInFourteenWeeksPatientsQuarterIndicator", activeAndNotwithDiabetesVisitInFourteenWeeksPatients, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-14w},onOrAfter=${endDate-12m},onOrBefore=${endDate}"), patientsSeenComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		CohortIndicator activeAndNotwithDiabetesVisitInFourteenWeeksPatientsCountQuarterIndicator=Indicators.newCountIndicator("activeAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicator", activeAndNotwithDiabetesVisitInFourteenWeeksPatients, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-14w},onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		//E4: Of total active patients on insulin at last visit, % with no visit 14 weeks or more past last visit date
		
		
		CompositionCohortDefinition activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients = new CompositionCohortDefinition();
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.setName("activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients");
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("end", "end", Date.class));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.addParameter(new Parameter("start", "start", Date.class));		
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put("1",new Mapped<CohortDefinition>(patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.getSearches().put("2",new Mapped<CohortDefinition>(withDiabetesVisit, ParameterizableUtil.createParameterMappings("end=${end},start=${start}")));
		activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients.setCompositionString("1 AND (NOT 2)");
		
	    CohortIndicator activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsQuarterIndicator=Indicators.newFractionIndicator("activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsQuarterIndicator", activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-14w},onOrAfter=${endDate-12m},onOrBefore=${endDate}"), patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		CohortIndicator activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicatorrs=Indicators.newCountIndicator("activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicator", activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatients, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-14w},onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		CohortIndicator activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsDenominatoCountQuarterIndicatorrs=Indicators.newCountIndicator("activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsDenominatorCountQuarterIndicator", patientsSeenAndOnInsulin, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));
		
		//E5: Of patients who have had HbA1c tested in the last quarter, % with last HbA1c <8 

		SqlCohortDefinition patientsWithLastHbA1cLessThanEight=new SqlCohortDefinition("select lasthba1cvalue.person_id from (select * from (select * from obs where concept_id="+hbA1c.getId()+" and voided=0 order by obs_datetime desc) as lasthba1c group by lasthba1c.person_id) as lasthba1cvalue where lasthba1cvalue.value_numeric<8");
		
		CompositionCohortDefinition patientsTestedForHbA1cWithLastHbA1cLessThanEight = new CompositionCohortDefinition();
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.setName("patientsTestedForHbA1cWithLastHbA1cLessThanEight");
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.addParameter(new Parameter("end", "end", Date.class));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.addParameter(new Parameter("start", "start", Date.class));		
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.getSearches().put("1",new Mapped<CohortDefinition>(patientsTestedForHbA1c, ParameterizableUtil.createParameterMappings("end=${end},start=${start}")));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.getSearches().put("2",new Mapped<CohortDefinition>(patientsWithLastHbA1cLessThanEight, null));
		patientsTestedForHbA1cWithLastHbA1cLessThanEight.setCompositionString("1 AND 2");
		
	    CohortIndicator patientsTestedForHbA1cWithLastHbA1cLessThanEightQuarterIndicator=Indicators.newFractionIndicator("patientsTestedForHbA1cWithLastHbA1cLessThanEightQuarterIndicator", patientsTestedForHbA1cWithLastHbA1cLessThanEight, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-3m}"), patientsTestedForHbA1c, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-3m}"));
		
		CohortIndicator patientsTestedForHbA1cWithLastHbA1cLessThanEightNumeratorCountQuarterIndicatorrs=Indicators.newCountIndicator("patientsTestedForHbA1cWithLastHbA1cLessThanEightNumeratorCountQuarterIndicator", patientsTestedForHbA1cWithLastHbA1cLessThanEight, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-3m}"));
		
		CohortIndicator patientsTestedForHbA1cWithLastHbA1cLessThanEightDenominatoCountQuarterIndicatorrs=Indicators.newCountIndicator("patientsTestedForHbA1cWithLastHbA1cLessThanEightDenominatorCountQuarterIndicator", patientsTestedForHbA1c, ParameterizableUtil.createParameterMappings("end=${endDate},start=${endDate-3m}"));
		
		
		//Adding columns to data set definition
		
		
		dsd.addColumn("A2Q", "Total # of patients seen in the last quarter",
		    new Mapped(patientsSeenQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM1", "Total # of patients seen in the last month one",
		    new Mapped(patientsSeenMonthOneIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM2", "Total # of patients seen in the last month two",
		    new Mapped(patientsSeenMonthTwoIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM3", "Total # of patients seen in the last month three",
		    new Mapped(patientsSeenMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3Q", "Total # of new patients enrolled in the last quarter",
	    new Mapped(patientEnrolledInDMQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3QM1", "Total # of new patients enrolled in the month one",
	    new Mapped(patientEnrolledInDMMonthOneIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3QM2", "Total # of new patients enrolled in the month two",
	    new Mapped(patientEnrolledInDMMonthTwooIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A3QM3", "Total # of new patients enrolled in the month three",
	    new Mapped(patientEnrolledInDMMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B1", "Pediatric: Of the new patients enrolled in the last quarter, % ≤15 years old at intake",
		    new Mapped(patientsUnderFifteenIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B1N", "Pediatric: Of the new patients enrolled in the last quarter, number ≤15 years old at intake",
		    new Mapped(patientsUnderFifteenCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B2", "Gender: Of the new patients enrolled in the last quarter, % male",
		    new Mapped(malePatientsEnrolledInDMIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B2N", "Gender: Of the new patients enrolled in the last quarter, number male",
		    new Mapped(malePatientsEnrolledInDMCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B3Q", "New patients enrolled in the last quarter, % with HbA1c done at intake",
		new Mapped(patientsEnrolledAndHaveHbAc1AtIntakeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B3NQ", "New patients enrolled in the last quarter, Number with HbA1c done at intake",
		new Mapped(patientsEnrolledAndHaveHbAc1AtIntakeCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B3M", "New patients enrolled in the last month, % with HbA1c done at intake",
		new Mapped(patientsEnrolledAndHaveHbAc1AtIntakeMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B3NM", "New patients enrolled in the last month, Number with HbA1c done at intake",
		new Mapped(patientsEnrolledAndHaveHbAc1AtIntakeCountMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");		
		dsd.addColumn("B4Q", "New patients enrolled in the last quarter, % with Glucose done at intake",
		new Mapped(patientsEnrolledAndHaveglucoseAtIntakeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B4NQ", "New patients enrolled in the last quarter, Number with Glucose done at intake",
		new Mapped(patientsEnrolledAndHaveglucoseAtIntakeCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B4M", "New patients enrolled in the last month, % with Glucose done at intake",
		new Mapped(patientsEnrolledAndHaveglucoseAtIntakeMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B4NM", "New patients enrolled in the last month, Number with Glucose done at intake",
		new Mapped(patientsEnrolledAndHaveglucoseAtIntakeCountMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");		
		dsd.addColumn("B5Q", "New patients enrolled in the last quarter, % with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBMIRecordedAtIntakeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B5NQ", "New patients enrolled in the last quarter, Number with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBMIRecordedAtIntakeCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B5M", "New patients enrolled in the last month, % with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBMIRecordedAtIntakeMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B5NM", "Of the new patients enrolled in the last month, Number with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBMIRecordedAtIntakeCountMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B6Q", "New patients enrolled in the last quarter, % with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBPRecordedAtIntakeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B6NQ", "New patients enrolled in the last quarter, Number with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBPRecordedAtIntakeCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B6M", "New patients enrolled in the last month, % with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBPRecordedAtIntakeMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B6NM", "new patients enrolled in the last month, Number with BMI recorded at intake",
		new Mapped(patientsEnrolledAndBPRecordedAtIntakeCountMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B7Q", "new patients enrolled in the last quarter, % with Neuropathy checked at intake",
		new Mapped(patientsEnrolledAndNeuropathyCheckedAtIntakeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B7NQ", "new patients enrolled in the last quarter, Number with Neuropathy checked at intake",
		new Mapped(patientsEnrolledAndNeuropathyCheckedAtIntakeCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B7M", "new patients enrolled in the last month, % with Neuropathy checked at intake",
		new Mapped(patientsEnrolledAndNeuropathyCheckedAtIntakeMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("B7NM", "new patients enrolled in the last month, Number with Neuropathy checked at intake",
		new Mapped(patientsEnrolledAndNeuropathyCheckedAtIntakeCountMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");		
		dsd.addColumn("C1Q", "total patients seen in the last quarter and are on ace inhibitors, % who had Creatinine tested in the last 6 months",
		new Mapped(patientsSeenOnAceInhibitorsAndTestedForCreatinineIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C1NQ", "total patients seen in the last quarter and are on ace inhibitors, Numerator who had Creatinine tested in the last 6 months",
		new Mapped(patientsSeenOnAceInhibitorsAndTestedForCreatinineCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C2Q", "total patients seen in the last quarter, % who had HbA1c tested in the last 6 months",
		new Mapped(patientsSeenAndTestedForHbA1cIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C2NQ", "total patients seen in the last quarter, Numeric who had HbA1c tested in the last 6 months",
		new Mapped(patientsSeenAndTestedForHbA1cCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C3Q", "all the patients ever registered (with Diabetes DDB), % ever tested for HbA1c",
		new Mapped(everRegisteredWithDDBAndTestForHbA1cIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C3NQ", "all the patients ever registered (with Diabetes DDB), Number Numerator ever tested for HbA1c",
		new Mapped(everRegisteredWithDDBAndTestForHbA1cIndicatorNumerator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C3DQ", "all the patients ever registered (with Diabetes DDB), Number Denominator ever tested for HbA1c",
		new Mapped(everRegisteredWithDDBAndTestForHbA1cIndicatorDenominator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C4Q", "total patients seen in the last quarter, % with BMI recorded at last visit",
		new Mapped(patientsSeenAndBMIAtLastVistIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C4NQ", "total patients seen in the last quarter, number with BMI recorded at last visit",
		new Mapped(patientsSeenAndBMIAtLastVistCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");		
		dsd.addColumn("C5Q", "total patients seen in the last quarter, % with BP recorded at last visit",
		new Mapped(patientsSeenAndBPAtLastVistIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C5NQ", "total patients seen in the last quarter, number with BP recorded at last visit",
		new Mapped(patientsSeenAndBPAtLastVistCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");			
		dsd.addColumn("C6Y", "total patients seen in the last year, % with Neuropathy checked in the last year",
		new Mapped(patientsSeenAndNeuropathyCheckedIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C6NY", "Of total patients seen in the last year, number with Neuropathy checked in the last year",
		new Mapped(patientsSeenAndNeuropathyCheckedCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("C6DY", "total patients seen in the last year",
		new Mapped(patientsSeenInOneYearCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D1Q", "Total patients seen in the last quarter, % with no regimen documented",
		new Mapped(patientsSeenAndNotOnAnyDMRegimenQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D1NQ", "Total patients seen in the last month/quarter,number with no regimen documented",
		new Mapped(patientsSeenAndNotOnAnyDMRegimenCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D1M", "Total patients seen in the last month, % with no regimen documented",
		new Mapped(patientsSeenAndNotOnAnyDMRegimenMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D1NM", "Total patients seen in the last month, number with no regimen documented",
		new Mapped(patientsSeenAndNotOnAnyDMRegimenCountMonthIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D2Q", "Total patients seen in the last quarter, % on any type of insulin at last visit",
		new Mapped(patientsSeenAndOnInsulinQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D2NQ", "Total patients seen in the last quarter, Number on any type of insulin at last visit",
		new Mapped(patientsSeenAndOnInsulinCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D3Q", "Total patients seen in the last quarter and on any type of insulin at last visit, % who are on metformin",
		new Mapped(patientsSeenAndOnInsulinAndOnMetforminQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D3NQ", "Total patients seen in the last quarter and on any type of insulin at last visit, Number who are on metformin",
		new Mapped(patientsSeenAndOnInsulinAndOnMetforminCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D4Q", "Total patients seen in the last quarter and on any type of insulin at last visit, % who are on mixed insulin",
		new Mapped(patientsSeenAndOnInsulinAndOnInsulinMixteQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D4NQ", "Total patients seen in the last quarter and on any type of insulin at last visit, Number who are on mixed insulin",
		new Mapped(patientsSeenAndOnInsulinAndOnInsulinMixteCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D5Q", "Total patients seen in the last quarter and on any type of insulin at last visit, % who have accompanateurs",
		new Mapped(patientsSeenAndOnInsulinAndWhitAccompagnateurQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D5NQ", "Total patients seen in the last quarter and on any type of insulin at last visit, number who have accompanateurs",
		new Mapped(patientsSeenAndOnInsulinAndWhitAccompagnateurCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D6Q", "Total patients seen in the last quarter, % on oral medications only (metformin and/or glibenclimide)",
		new Mapped(patientsSeenAndOnMetforminOrGlibenclimideQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("D6NQ", "Total patients seen in the last quarter, number on oral medications only (metformin and/or glibenclimide)",
		new Mapped(patientsSeenAndOnMetforminOrGlibenclimideCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E1Q", "Total active patients, % who died",
		new Mapped(activeAndDiedPatientsQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E1NQ", "Total active patients, number who died",
		new Mapped(activeAndDiedPatientsCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), ""); 		
		dsd.addColumn("E2Q", "Total active patients, % with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)",
		new Mapped(activeAndHospitalizedPatientsQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E2NQ", "Total active patients, number with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)",
		new Mapped(activeAndHospitalizedPatientsCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), ""); 		
		dsd.addColumn("E3Q", "Total active patients, % with no visit 14 weeks or more past last visit date",
		new Mapped(activeAndNotwithDiabetesVisitInFourteenWeeksPatientsQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E3NQ", "Total active patients, number with no visit 14 weeks or more past last visit date",
		new Mapped(activeAndNotwithDiabetesVisitInFourteenWeeksPatientsCountQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), ""); 		
		dsd.addColumn("E4Q", "Total active patients on insulin at last visit, % with no visit 14 weeks or more past last visit date",
		new Mapped(activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E4NQ", "Total active patients on insulin at last visit, Numerator with no visit 14 weeks or more past last visit date",
		new Mapped(activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsNumeratorCountQuarterIndicatorrs, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E4DQ", "Total active patients on insulin at last visit, Denominator with no visit 14 weeks or more past last visit date",
		new Mapped(activeAndOnInsulinAndNotwithDiabetesVisitInFourteenWeeksPatientsDenominatoCountQuarterIndicatorrs, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E5Q", "Patients who have had HbA1c tested in the last quarter, % with last HbA1c <8 ",
		new Mapped(patientsTestedForHbA1cWithLastHbA1cLessThanEightQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E5NQ", "Patients who have had HbA1c tested in the last quarter, Numerator with last HbA1c <8 ",
		new Mapped(patientsTestedForHbA1cWithLastHbA1cLessThanEightNumeratorCountQuarterIndicatorrs, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("E5DQ", "Patients who have had HbA1c tested in the last quarter, Denominator with last HbA1c <8 ",
		new Mapped(patientsTestedForHbA1cWithLastHbA1cLessThanEightDenominatoCountQuarterIndicatorrs, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
	}
	
	private void setUpProperties() {
		DMProgram=gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
		DMPrograms.add(DMProgram);
		DMEncounterTypeId=Integer.parseInt(Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.DIABETES_VISIT));
		DMEncounterType=gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT);
		adultInitialVisit=gp.getEncounterType(GlobalPropertiesManagement.ADULT_INITIAL_VISIT);
		DDBform=gp.getForm(GlobalPropertiesManagement.DIABETES_DDB_FORM);
		DDBforms.add(DDBform);
		patientsSeenEncounterTypes.add(DMEncounterType);
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		glucose=gp.getConcept(GlobalPropertiesManagement.GLUCOSE);
		weight=gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		height=gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		diastolicBP=gp.getConcept(GlobalPropertiesManagement.DIASTOLIC_BLOOD_PRESSURE);
		systolicBP=gp.getConcept(GlobalPropertiesManagement.SYSTOLIC_BLOOD_PRESSURE);
		hbA1c=gp.getConcept(GlobalPropertiesManagement.HBA1C);
		onOrBefOnOrAf.add("onOrBef");
		onOrBefOnOrAf.add("onOrAf");		
		sensationInRightFoot=gp.getConcept(GlobalPropertiesManagement.SENSATION_IN_RIGHT_FOOT);
		sensationInLeftFoot=gp.getConcept(GlobalPropertiesManagement.SENSATION_IN_LEFT_FOOT);
		onAceInhibitorsDrugs.addAll(gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.LISINOPRIL)));
		onAceInhibitorsDrugs.addAll(gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.CAPTOPRIL)));
		lisinoprilCaptopril="('"+gp.getConcept(GlobalPropertiesManagement.LISINOPRIL).getUuid()+"',"+"'"+gp.getConcept(GlobalPropertiesManagement.CAPTOPRIL).getUuid()+"')";
		creatinine=gp.getConcept(GlobalPropertiesManagement.SERUM_CREATININE);
		insulin7030=gp.getConcept(GlobalPropertiesManagement.INSULIN_70_30);
		insulinLente=gp.getConcept(GlobalPropertiesManagement.INSULIN_LENTE);
		insulinRapide=gp.getConcept(GlobalPropertiesManagement.INSULIN_RAPIDE);
		diabetesDrugConcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.DIABETES_TREATMENT_DRUG_SET);
		for (Concept conc : diabetesDrugConcepts) {
			if(splitedDiabetesConceptSet!=null)
				splitedDiabetesConceptSet=splitedDiabetesConceptSet+","+conc.getId();
			else
				splitedDiabetesConceptSet=conc.getId().toString();
		}
		metformin=gp.getConcept(GlobalPropertiesManagement.METFORMIN_DRUG);
		glibenclimide=gp.getConcept(GlobalPropertiesManagement.GLIBENCLAMIDE_DRUG);
		diedState=DMProgram.getWorkflow(28).getState("PATIENT DIED");
		admitToHospital=gp.getConcept(GlobalPropertiesManagement.HOSPITAL_ADMITTANCE);
		locOfHosp=gp.getConcept(GlobalPropertiesManagement.LOCATION_OF_HOSPITALIZATION);
	}
}
