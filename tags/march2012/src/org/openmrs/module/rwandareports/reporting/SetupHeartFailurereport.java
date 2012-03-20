package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.datasource.PersonDataSource;
import org.openmrs.logic.rule.AgeRule;
import org.openmrs.logic.token.TokenService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PatientStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.aggregation.MedianAggregator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.definition.DrugsActiveCohortDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupHeartFailurereport {
	
	protected final static Log log = LogFactory.getLog(SetupHeartFailurereport.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program heartFailure;
	
	private ProgramWorkflowState postOp;
	
	private List<Form> cardiologyForm;
	
	private List<Form> cardiologyDDB;
	
	private Concept echocardiographyResult;
	
	private Concept notDone;
	
	private Concept serumCreatinine;
	
	private Concept heartFailureDiagnosis;
	
	private Concept cardiomyopathy;
	
	private Concept miralStenosis;
	
	private Concept rhuematicHeartDisease;
	
	private Concept hypertensiveDisease;
	
	private Concept pericardialDisease;
	
	private Concept congenitalHeartFailure;
	
	private Concept patientsUsingFamilyPlanning;
	
	private Concept pulse;
	
	private Concept reasonForExitingCare;
	
	private Concept patientDied;
	
	private Concept internationalNormalizedRatio;
	
	private Concept disposition;
	
	private Concept admitToHospital;
	
	private Concept height;
	
	private List<Drug> furosemide;
	
	private List<Drug> atenolol;
	
	private List<Drug> carvedilol;
	
	private List<Drug> aldactone;
	
	private List<Drug> lisinopril;
	
	private List<Drug> captopril;
	
	private List<Drug> warfarin;
	
	private List<Drug> penicillin;
	
	List<EncounterType> encounterTypes;
	
	private List<String> onOrAfterOnOrBeforeParamterNames = new ArrayList<String>();
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "heartfailurereporttemplate.xls",
		    "Xlsheartfailurereporttemplate", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:Heart Failure Report Location");
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Xlsheartfailurereporttemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Heart Failure Report");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		rd.setName("Heart Failure Report");
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
		        createDataSetDefinition());
		ldsd.setName("Heart Failure Report Location");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		rd.addDataSetDefinition(ldsd,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		h.saveReportDefinition(rd);
		
		return rd;
		
	}
	
	private DataSetDefinition createDataSetDefinition() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Heart Failure Report Indicators");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		//Patient In Heart Failure Program
		InProgramCohortDefinition patientsInHFProgram = Cohorts.createInProgramParameterizableByDate("patientsInHFProgram",
		    heartFailure);
		
		//============================================================================
		//  1.1.m & 1.1.f % male and female
		//============================================================================
		
		//Gender Cohort definitions
		GenderCohortDefinition femalesDefinition = Cohorts.createFemaleCohortDefinition("femalesDefinition");
		
		GenderCohortDefinition malesDefinition = Cohorts.createMaleCohortDefinition("malesDefinition");
		
		CompositionCohortDefinition maleInFHProgramComposition = new CompositionCohortDefinition();
		maleInFHProgramComposition.setName("maleInFHProgramComposition");
		maleInFHProgramComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		maleInFHProgramComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		maleInFHProgramComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleInFHProgramComposition.getSearches().put("malesDefinition", new Mapped<CohortDefinition>(malesDefinition, null));
		maleInFHProgramComposition.setCompositionString("patientsInHFProgram AND malesDefinition");
		
		CompositionCohortDefinition femaleInFHProgramComposition = new CompositionCohortDefinition();
		femaleInFHProgramComposition.setName("femaleInFHProgramComposition");
		femaleInFHProgramComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		femaleInFHProgramComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		femaleInFHProgramComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleInFHProgramComposition.getSearches().put("femalesDefinition",
		    new Mapped<CohortDefinition>(femalesDefinition, null));
		femaleInFHProgramComposition.setCompositionString("patientsInHFProgram AND femalesDefinition");
		
		CohortIndicator percentMaleInFHProgramIndicator = Indicators
		        .newFractionIndicator("percentMaleInFHProgramIndicator", maleInFHProgramComposition,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		CohortIndicator percentFemaleInFHProgramIndicator = Indicators
		        .newFractionIndicator("percentFemaleInFHProgramIndicator", femaleInFHProgramComposition,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//=========================================================================================
		//      1.2.   median age
		//=========================================================================================
		LogicService ls = Context.getLogicService();
		TokenService ts = Context.getService(TokenService.class);
		
		try {
			ls.getRule("AGE");
		}
		catch (Exception ex) {
			AgeRule ageRule = new AgeRule();
			
			//ls.addRule("AGE", ageRule);
			ts.registerToken("AGE", new PersonDataSource(), "");
		}
		
		CohortIndicator medianAge = Indicators.newLogicIndicator("medianAge", patientsInHFProgram,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    MedianAggregator.class, "AGE");
		
		//=========================================================================================
		//      1.3.   Patients enrolled
		//=========================================================================================
		
		//Patient Enrolled in Heart Failure Program
		
		ProgramEnrollmentCohortDefinition patientsEnrolledInHFProgram = Cohorts
		        .createProgramEnrollmentParameterizedByStartEndDate("patientsEnrolledInHFProgram", heartFailure);
		
		CohortIndicator patientsEnrolledInHFIndicator = Indicators.newCohortIndicator("patientsEnrolledInHFIndicator",
		    patientsEnrolledInHFProgram,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		
		//=========================================================================================
		//      1.4.   Total number of Patient in Heart Failure
		//=========================================================================================
		
		CohortIndicator patientsInHFIndicator = Indicators.newCohortIndicator("patientsInHFIndicator", patientsInHFProgram,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===============================================================================
		//  2.1. number and percent of patients without a cardiology consultation
		//===============================================================================
		
		EncounterCohortDefinition patientsWithCardFormBeforeEndDate = Cohorts.createEncounterBasedOnForms(
		    "patientsWithCardFormBeforeEndDate", "onOrBefore", cardiologyForm);
		
		CompositionCohortDefinition patientsInHFProgramWithouCardForm = new CompositionCohortDefinition();
		patientsInHFProgramWithouCardForm.setName("patientsInHFProgramWithouCardForm");
		patientsInHFProgramWithouCardForm.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsInHFProgramWithouCardForm.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsInHFProgramWithouCardForm.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsInHFProgramWithouCardForm.getSearches().put(
		    "patientsWithCardFormBeforeEndDate",
		    new Mapped<CohortDefinition>(patientsWithCardFormBeforeEndDate, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		patientsInHFProgramWithouCardForm
		        .setCompositionString("patientsInHFProgram AND (NOT patientsWithCardFormBeforeEndDate)");
		
		CohortIndicator patientsInHFProgramWithouCardFormIndicator = Indicators
		        .newFractionIndicator("patientsInHFProgramWithouCardFormIndicator", patientsInHFProgramWithouCardForm,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// =============================================================================== 
		//   2.2.Number and percent of patients without a preliminary echocardiographic diagnosis
		// ===============================================================================   
		// echocardiographyDuringPeriod echocardiographyAndHFProgramComposition hfEchocardiographyPercentageIndicator
		
		CodedObsCohortDefinition echocardiographyDuringPeriod = Cohorts.createCodedObsCohortDefinition(
		    "echocardiographyDuringPeriod", echocardiographyResult, notDone, SetComparator.IN, TimeModifier.ANY);
		
		CompositionCohortDefinition echocardiographyAndHFProgramComposition = new CompositionCohortDefinition();
		echocardiographyAndHFProgramComposition.setName("echocardiographyAndHFProgramComposition");
		echocardiographyAndHFProgramComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		echocardiographyAndHFProgramComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		echocardiographyAndHFProgramComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		echocardiographyAndHFProgramComposition.getSearches().put("echocardiographyDuringPeriod",
		    new Mapped<CohortDefinition>(echocardiographyDuringPeriod, null));
		echocardiographyAndHFProgramComposition
		        .setCompositionString("(patientsInHFProgram AND echocardiographyDuringPeriod");
		
		CohortIndicator hfEchocardiographyPercentageIndicator = Indicators
		        .newFractionIndicator("hfEchocardiographyPercentageIndicator", echocardiographyAndHFProgramComposition,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ===============================================================================                         
		//                 2.3.  Percent without a creatinine in the last 6 months
		// ===============================================================================   
		
		NumericObsCohortDefinition patientsWithCreatinineCohortDef = Cohorts.createNumericObsCohortDefinition(
		    "patientsWithCreatinineCohortDef", serumCreatinine, 0.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
		
		CompositionCohortDefinition hfPatientWithoutCreatinineCompositionCohortDef = new CompositionCohortDefinition();
		hfPatientWithoutCreatinineCompositionCohortDef.setName("hfPatientWithoutCreatinineCompositionCohortDef");
		hfPatientWithoutCreatinineCompositionCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hfPatientWithoutCreatinineCompositionCohortDef.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hfPatientWithoutCreatinineCompositionCohortDef.getSearches().put(
		    "patientsWithCreatinineCohortDef",
		    new Mapped<CohortDefinition>(patientsWithCreatinineCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hfPatientWithoutCreatinineCompositionCohortDef.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hfPatientWithoutCreatinineCompositionCohortDef
		        .setCompositionString("patientsInHFProgram AND (NOT patientsWithCreatinineCohortDef)");
		
		CohortIndicator hfPatientWithoutCreatininePercentIndicator = Indicators
		        .newFractionIndicator("hfPatientWithoutCreatininePercentIndicator",
		            hfPatientWithoutCreatinineCompositionCohortDef,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===============================================================================                 
		//   PATIENTS WHO WITH HEART FAILURE DIAGNOSIS IN THE LAST MONTH
		// ===============================================================================                 
		
		//===============================================================================                 
		//     2.4. Patient with Cardiomyopathy
		// ===============================================================================                 
		
		CodedObsCohortDefinition cardiomyopathyDiognosis = Cohorts.createCodedObsCohortDefinition("cardiomyopathyDiognosis",
		    "onOrBefore", heartFailureDiagnosis, cardiomyopathy, SetComparator.IN, TimeModifier.ANY);
		
		CompositionCohortDefinition cardiomyopathyDiognosisAnsHFProgComposition = new CompositionCohortDefinition();
		cardiomyopathyDiognosisAnsHFProgComposition.setName("cardiomyopathyDiognosisAnsHFProgComposition");
		cardiomyopathyDiognosisAnsHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		cardiomyopathyDiognosisAnsHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		cardiomyopathyDiognosisAnsHFProgComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		cardiomyopathyDiognosisAnsHFProgComposition.getSearches().put(
		    "cardiomyopathyDiognosis",
		    new Mapped<CohortDefinition>(cardiomyopathyDiognosis, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		cardiomyopathyDiognosisAnsHFProgComposition.setCompositionString("(patientsInHFProgram AND cardiomyopathyDiognosis");
		
		CohortIndicator cardiomyopathyDiognosisAnsHFProgIndicator = Indicators.newCohortIndicator(
		    "cardiomyopathyDiognosisAnsHFProgIndicator", cardiomyopathyDiognosisAnsHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===============================================================================                 
		//               2.5.  PATIENTS WHICH HAVE HAD  PURE MITRAL STENOSIS
		//===============================================================================  
		
		CodedObsCohortDefinition mitralStenosisDiagnosis = Cohorts.createCodedObsCohortDefinition("mitralStenosisDiagnosis",
		    "onOrBefore", heartFailureDiagnosis, miralStenosis, SetComparator.IN, TimeModifier.ANY);
		
		CompositionCohortDefinition mitralStenosisDiagnosisAnsHFProgComposition = new CompositionCohortDefinition();
		mitralStenosisDiagnosisAnsHFProgComposition.setName("mitralStenosisDiagnosisAnsHFProgComposition");
		mitralStenosisDiagnosisAnsHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		mitralStenosisDiagnosisAnsHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		mitralStenosisDiagnosisAnsHFProgComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		mitralStenosisDiagnosisAnsHFProgComposition.getSearches().put(
		    "mitralStenosisDiagnosis",
		    new Mapped<CohortDefinition>(mitralStenosisDiagnosis, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		mitralStenosisDiagnosisAnsHFProgComposition.setCompositionString("(patientsInHFProgram AND mitralStenosisDiagnosis");
		
		CohortIndicator mitralStenosisDiagnosisAnsHFProgIndicator = Indicators.newCohortIndicator(
		    "mitralStenosisDiagnosisAnsHFProgIndicator", mitralStenosisDiagnosisAnsHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===============================================================================                 
		//      2.6.  PATIENTS WHICH HAVE HAD A RHEUMATIC HEART DISEASE
		//===============================================================================  
		
		CodedObsCohortDefinition rheumaticHeartDiseaseDiagnosis = Cohorts.createCodedObsCohortDefinition(
		    "rheumaticHeartDiseaseDiagnosis", "onOrBefore", heartFailureDiagnosis, rhuematicHeartDisease, SetComparator.IN,
		    TimeModifier.ANY);
		
		CompositionCohortDefinition rheumaticHeartDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
		rheumaticHeartDiseaseDiagnosisAndHFProgComposition.setName("rheumaticHeartDiseaseDiagnosisAndHFProgComposition");
		rheumaticHeartDiseaseDiagnosisAndHFProgComposition
		        .addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		rheumaticHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		rheumaticHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		rheumaticHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "rheumaticHeartDiseaseDiagnosis",
		    new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosis, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		rheumaticHeartDiseaseDiagnosisAndHFProgComposition
		        .setCompositionString("(patientsInHFProgram AND rheumaticHeartDiseaseDiagnosis");
		
		CohortIndicator rheumaticHeartDiseaseDiagnosisAndHFProgIndicator = Indicators.newCohortIndicator(
		    "rheumaticHeartDiseaseDiagnosisAndHFProgIndicator", rheumaticHeartDiseaseDiagnosisAndHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===============================================================================                 
		//    2.7.  PATIENTS WHO HAVE HAD A HYPERTENSIVE HEART DISEASE
		//===============================================================================                                
		
		CodedObsCohortDefinition hypertensiveHeartDiseaseDiagnosis = Cohorts.createCodedObsCohortDefinition(
		    "hypertensiveHeartDiseaseDiagnosis", "onOrBefore", heartFailureDiagnosis, hypertensiveDisease, SetComparator.IN,
		    TimeModifier.ANY);
		
		CompositionCohortDefinition hypertensiveHeartDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
		hypertensiveHeartDiseaseDiagnosisAndHFProgComposition
		        .setName("hypertensiveHeartDiseaseDiagnosisAndHFProgComposition");
		hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore",
		        Date.class));
		hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter",
		        Date.class));
		hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "hypertensiveHeartDiseaseDiagnosis",
		    new Mapped<CohortDefinition>(hypertensiveHeartDiseaseDiagnosis, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		hypertensiveHeartDiseaseDiagnosisAndHFProgComposition
		        .setCompositionString("(patientsInHFProgram AND hypertensiveHeartDiseaseDiagnosis");
		
		CohortIndicator hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator = Indicators.newCohortIndicator(
		    "hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator", hypertensiveHeartDiseaseDiagnosisAndHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ===============================================================================                 
		//  2.8.  PATIENTS WHO HAVE HAD A PERICARDIAL DISEASE
		// ===============================================================================   				    
		
		CodedObsCohortDefinition pericardialDiseaseDiagnosis = Cohorts.createCodedObsCohortDefinition(
		    "pericardialDiseaseDiagnosis", "onOrBefore", heartFailureDiagnosis, pericardialDisease, SetComparator.IN,
		    TimeModifier.ANY);
		
		CompositionCohortDefinition pericardialDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
		pericardialDiseaseDiagnosisAndHFProgComposition.setName("pericardialDiseaseDiagnosisAndHFProgComposition");
		pericardialDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		pericardialDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		pericardialDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		pericardialDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "pericardialDiseaseDiagnosis",
		    new Mapped<CohortDefinition>(pericardialDiseaseDiagnosis, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		pericardialDiseaseDiagnosisAndHFProgComposition
		        .setCompositionString("(patientsInHFProgram AND pericardialDiseaseDiagnosis");
		
		CohortIndicator pericardialDiseaseDiagnosisAndHFProgIndicator = Indicators.newCohortIndicator(
		    "pericardialDiseaseDiagnosisAndHFProgIndicator", pericardialDiseaseDiagnosisAndHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ===============================================================================                 
		//         2.9. PATIENTS WHO HAVE HAD A CONGENITAL HEART DISEASE
		// ===============================================================================  
		
		CodedObsCohortDefinition congenitalDiseaseDiagnosis = Cohorts.createCodedObsCohortDefinition(
		    "congenitalDiseaseDiagnosis", "onOrBefore", heartFailureDiagnosis, congenitalHeartFailure, SetComparator.IN,
		    TimeModifier.ANY);
		
		CompositionCohortDefinition congenitalDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
		congenitalDiseaseDiagnosisAndHFProgComposition.setName("congenitalDiseaseDiagnosisAndHFProgComposition");
		congenitalDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		congenitalDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		congenitalDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		congenitalDiseaseDiagnosisAndHFProgComposition.getSearches().put(
		    "congenitalDiseaseDiagnosis",
		    new Mapped<CohortDefinition>(congenitalDiseaseDiagnosis, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		congenitalDiseaseDiagnosisAndHFProgComposition
		        .setCompositionString("(patientsInHFProgram AND congenitalDiseaseDiagnosis");
		
		CohortIndicator congenitalDiseaseDiagnosisAndHFProgIndicator = Indicators.newCohortIndicator(
		    "congenitalDiseaseDiagnosisAndHFProgIndicator", congenitalDiseaseDiagnosisAndHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ========================================================================================                        
		//   2.10. Parcent with creatinine > 200
		// ========================================================================================  
		
		NumericObsCohortDefinition patientsWithCreatinine = Cohorts.createNumericObsCohortDefinition(
		    "patientsWithCreatinine", "onOrBefore", serumCreatinine, 200.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
		
		CompositionCohortDefinition hfpatientsWithCreatinineComposition = new CompositionCohortDefinition();
		hfpatientsWithCreatinineComposition.setName("hfpatientsWithCreatinineComposition");
		hfpatientsWithCreatinineComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hfpatientsWithCreatinineComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hfpatientsWithCreatinineComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hfpatientsWithCreatinineComposition.getSearches().put(
		    "patientsWithCreatinine",
		    new Mapped<CohortDefinition>(patientsWithCreatinine, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		hfpatientsWithCreatinineComposition.setCompositionString("patientsInHFProgram AND patientsWithCreatinine");
		
		CohortIndicator hfpatientsWithCreatininePercentIndicator = Indicators
		        .newFractionIndicator("hfpatientsWithCreatininePercentIndicator", hfpatientsWithCreatinineComposition,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//======================================================================================================                       
		//   2.11. number post-cardiac surgery
		//====================================================================================================== 
		
		PatientStateCohortDefinition postCardiacSurgeryCohortDefinition = Cohorts.createPatientStateCohortDefinition(
		    "postCardiacSurgeryCohortDefinition", postOp);
		postCardiacSurgeryCohortDefinition.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
		postCardiacSurgeryCohortDefinition.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
		
		CohortIndicator postCardiacSurgeryCohortIndicator = Indicators.newCountIndicator("postCardiacSurgeryCohortIndicator",
		    postCardiacSurgeryCohortDefinition,
		    ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		//================================================                        
		//   3.4. in the subgroup (female < age 50), percent not on family planning
		// ================================================  
		
		NumericObsCohortDefinition patientsInFamilyPlanning = Cohorts.createNumericObsCohortDefinition(
		    "patientsInFamilyPlanning", "onOrBefore", patientsUsingFamilyPlanning, 1.0, RangeComparator.EQUAL,
		    TimeModifier.LAST);
		
		AgeCohortDefinition lessThanFifty = new AgeCohortDefinition(null, 50, null);
		lessThanFifty.setName("lessThanFifty");
		
		CompositionCohortDefinition patientsInHFWithoutFamilyPlanningCompositionCohort = new CompositionCohortDefinition();
		patientsInHFWithoutFamilyPlanningCompositionCohort.setName("patientsInHFWithoutFamilyPlanningCompositionCohort");
		patientsInHFWithoutFamilyPlanningCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsInHFWithoutFamilyPlanningCompositionCohort
		        .addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put(
		    "patientsInFamilyPlanning",
		    new Mapped<CohortDefinition>(patientsInFamilyPlanning, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put("femalesDefinition",
		    new Mapped<CohortDefinition>(femalesDefinition, null));
		patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put("lessThanFifty",
		    new Mapped<CohortDefinition>(lessThanFifty, null));
		patientsInHFWithoutFamilyPlanningCompositionCohort
		        .setCompositionString("patientsInHFProgram AND femalesDefinition AND lessThanFifty AND (NOT patientsInFamilyPlanning)");
		
		CohortIndicator patientsInHFWithoutFamilyPlanningIndicator = Indicators
		        .newFractionIndicator("patientsInHFWithoutFamilyPlanningIndicator",
		            patientsInHFWithoutFamilyPlanningCompositionCohort,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ================================================                        
		//  3.5. Patients LASIX COHORT
		// ================================================  
		DrugsActiveCohortDefinition onLasixAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onLasixAtEndOfPeriod",
		    "asOfDate", furosemide);
		
		CompositionCohortDefinition hFonLasixAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFonLasixAtEndOfPeriodCompositionCohort.setName("hFonLasixAtEndOfPeriodCompositionCohort");
		hFonLasixAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFonLasixAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFonLasixAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFonLasixAtEndOfPeriodCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFonLasixAtEndOfPeriodCompositionCohort.getSearches().put(
		    "onLasixAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onLasixAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFonLasixAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onLasixAtEndOfPeriod");
		
		CohortIndicator onLasixAtEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onLasixAtEndOfPeriodCohortIndicator", hFonLasixAtEndOfPeriodCompositionCohort,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ================================================                        
		//  3.6. Patients on Atenolol COHORT
		// ================================================ 
		
		DrugsActiveCohortDefinition onAtenololAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onAtenololAtEndOfPeriod",
		    "asOfDate", atenolol);
		
		CompositionCohortDefinition hFonAtenololAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFonAtenololAtEndOfPeriodCompositionCohort.setName("hFonAtenololAtEndOfPeriodCompositionCohort");
		hFonAtenololAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFonAtenololAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFonAtenololAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFonAtenololAtEndOfPeriodCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFonAtenololAtEndOfPeriodCompositionCohort.getSearches().put(
		    "onAtenololAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onAtenololAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFonAtenololAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onAtenololAtEndOfPeriod");
		
		CohortIndicator onAtenololAtEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onAtenololAtEndOfPeriodCohortIndicator", hFonAtenololAtEndOfPeriodCompositionCohort,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ================================================                        
		//  3.7. Patients Carvedilol COHORT
		// ================================================   
		
		DrugsActiveCohortDefinition onCarvedilolAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onCarvedilolAtEndOfPeriod",
		    "asOfDate", carvedilol);
		
		CompositionCohortDefinition hFonCarvedilolAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFonCarvedilolAtEndOfPeriodCompositionCohort.setName("hFonCarvedilolAtEndOfPeriodCompositionCohort");
		hFonCarvedilolAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFonCarvedilolAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFonCarvedilolAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFonCarvedilolAtEndOfPeriodCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFonCarvedilolAtEndOfPeriodCompositionCohort.getSearches().put(
		    "onCarvedilolAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFonCarvedilolAtEndOfPeriodCompositionCohort
		        .setCompositionString("patientsInHFProgram AND onCarvedilolAtEndOfPeriod");
		
		CohortIndicator onCarvedilolAtEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onCarvedilolAtEndOfPeriodCohortIndicator",
		            hFonCarvedilolAtEndOfPeriodCompositionCohort, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ================================================                        
		//  3.8. Patients Aldactone COHORT
		// ================================================   
		
		DrugsActiveCohortDefinition onAldactoneAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onAldactoneAtEndOfPeriod",
		    "asOfDate", aldactone);
		
		CompositionCohortDefinition hFonAldactoneAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFonAldactoneAtEndOfPeriodCompositionCohort.setName("hFonAldactoneAtEndOfPeriodCompositionCohort");
		hFonAldactoneAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFonAldactoneAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFonAldactoneAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFonAldactoneAtEndOfPeriodCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFonAldactoneAtEndOfPeriodCompositionCohort.getSearches().put(
		    "onAldactoneAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onAldactoneAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFonAldactoneAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onAldactoneAtEndOfPeriod");
		
		CohortIndicator onAldactoneAtEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onAldactoneAtEndOfPeriodCohortIndicator",
		            hFonAldactoneAtEndOfPeriodCompositionCohort, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//================================================                        
		//  3.9. Patients Lisinopril or Captopril COHORT
		//================================================   	                                                
		
		/*Lisinopril*/
		DrugsActiveCohortDefinition onLisinoprilAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onLisinoprilAtEndOfPeriod",
		    "asOfDate", lisinopril);
		
		/* Captopril*/
		DrugsActiveCohortDefinition onCaptoprilAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onCaptoprilAtEndOfPeriod",
		    "asOfDate", captopril);
		
		CompositionCohortDefinition hFLisinoprilOrCaptoprilCompositionCohort = new CompositionCohortDefinition();
		hFLisinoprilOrCaptoprilCompositionCohort.setName("hFLisinoprilOrCaptoprilCompositionCohort");
		hFLisinoprilOrCaptoprilCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFLisinoprilOrCaptoprilCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFLisinoprilOrCaptoprilCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFLisinoprilOrCaptoprilCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFLisinoprilOrCaptoprilCompositionCohort.getSearches().put(
		    "onLisinoprilAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onLisinoprilAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFLisinoprilOrCaptoprilCompositionCohort.getSearches().put(
		    "onCaptoprilAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onCaptoprilAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFLisinoprilOrCaptoprilCompositionCohort
		        .setCompositionString("patientsInHFProgram AND (onLisinoprilAtEndOfPeriod OR onCaptoprilAtEndOfPeriod)");
		
		CohortIndicator onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator",
		            hFLisinoprilOrCaptoprilCompositionCohort, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		//================================================                        
		//  3.10. Patients Atenolol and Carvedilol COHORT
		//================================================   	                                                
		
		CompositionCohortDefinition hFAtenololAndCarvedilolCompositionCohort = new CompositionCohortDefinition();
		hFAtenololAndCarvedilolCompositionCohort.setName("hFAtenololAndCarvedilolCompositionCohort");
		hFAtenololAndCarvedilolCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFAtenololAndCarvedilolCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFAtenololAndCarvedilolCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFAtenololAndCarvedilolCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFAtenololAndCarvedilolCompositionCohort.getSearches().put(
		    "onAtenololAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onAtenololAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFAtenololAndCarvedilolCompositionCohort.getSearches().put(
		    "onCarvedilolAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFAtenololAndCarvedilolCompositionCohort
		        .setCompositionString("patientsInHFProgram AND (onAtenololAtEndOfPeriod AND onCarvedilolAtEndOfPeriod)");
		
		CohortIndicator onAtenololAndCarvedilolEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onAtenololAndCarvedilolEndOfPeriodCohortIndicator",
		            hFAtenololAndCarvedilolCompositionCohort, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		onAtenololAndCarvedilolEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		onAtenololAndCarvedilolEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		//================================================                        
		//  3.11. Patients Lisinopril and Captopril COHORT
		//================================================   	                                                
		
		CompositionCohortDefinition hFLisinoprilAndCaptoprilCompositionCohort = new CompositionCohortDefinition();
		hFLisinoprilAndCaptoprilCompositionCohort.setName("hFLisinoprilAndCaptoprilCompositionCohort");
		hFLisinoprilAndCaptoprilCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFLisinoprilAndCaptoprilCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFLisinoprilAndCaptoprilCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFLisinoprilAndCaptoprilCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFLisinoprilAndCaptoprilCompositionCohort.getSearches().put(
		    "onLisinoprilAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onLisinoprilAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFLisinoprilAndCaptoprilCompositionCohort.getSearches().put(
		    "onCaptoprilAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onCaptoprilAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFLisinoprilAndCaptoprilCompositionCohort
		        .setCompositionString("patientsInHFProgram AND (onLisinoprilAtEndOfPeriod AND onCaptoprilAtEndOfPeriod)");
		
		CohortIndicator onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator",
		            hFLisinoprilAndCaptoprilCompositionCohort, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//================================================                        
		//3.12. Patients on wafarin COHORT
		//================================================ 
		
		DrugsActiveCohortDefinition onWarfarinAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onWarfarinAtEndOfPeriod",
		    "asOfDate", warfarin);
		
		CompositionCohortDefinition hFonWarfarinAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFonWarfarinAtEndOfPeriodCompositionCohort.setName("hFonWarfarinAtEndOfPeriodCompositionCohort");
		hFonWarfarinAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFonWarfarinAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFonWarfarinAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFonWarfarinAtEndOfPeriodCompositionCohort.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFonWarfarinAtEndOfPeriodCompositionCohort.getSearches().put(
		    "onWarfarinAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFonWarfarinAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onWarfarinAtEndOfPeriod");
		
		CohortIndicator onWarfarinAtEndOfPeriodCohortIndicator = Indicators
		        .newFractionIndicator("onWarfarinAtEndOfPeriodCohortIndicator", hFonWarfarinAtEndOfPeriodCompositionCohort,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		onWarfarinAtEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		onWarfarinAtEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		// ===================================================================================                       
		//   3.1. in the subgroup of cardiomyopathy percent with heart rate >60 not  carvedilol
		// =================================================================================== 
		
		NumericObsCohortDefinition heartRateDiseaseDuringPeriod = Cohorts.createNumericObsCohortDefinition(
		    "heartRateDiseaseDuringPeriod", pulse, 60.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
		
		CompositionCohortDefinition hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort
		        .setName("hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort");
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter",
		        Date.class));
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore",
		        Date.class));
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort
		        .addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.getSearches().put(
		    "cardiomyopathyDiognosisAnsHFProgComposition",
		    new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgComposition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.getSearches().put("heartRateDiseaseDuringPeriod",
		    new Mapped<CohortDefinition>(heartRateDiseaseDuringPeriod, null));
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.getSearches().put(
		    "onCarvedilolAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort
		        .setCompositionString("cardiomyopathyDiognosisAnsHFProgComposition AND (heartRateDiseaseDuringPeriod AND (NOT onCarvedilolAtEndOfPeriod))");
		
		CohortIndicator onCardiomyopathyHeartRatePeriodCohortIndicator = Indicators.newFractionIndicator(
		    "onCardiomyopathyHeartRatePeriodCohortIndicator", hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		    cardiomyopathyDiognosisAnsHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===================================================================================                       
		//   3.2. in the subgroup of mitral stenosis percent with heart rate >60 not Atenolol
		// ===================================================================================  
		
		CompositionCohortDefinition hFMitralStenosisWithHeartRateOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort
		        .setName("hFMitralStenosisWithHeartRateOfPeriodCompositionCohort");
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter",
		        Date.class));
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore",
		        Date.class));
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort
		        .addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.getSearches().put(
		    "mitralStenosisDiagnosisAnsHFProgComposition",
		    new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgComposition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.getSearches().put("heartRateDiseaseDuringPeriod",
		    new Mapped<CohortDefinition>(heartRateDiseaseDuringPeriod, null));
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.getSearches().put(
		    "onAtenololAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onAtenololAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFMitralStenosisWithHeartRateOfPeriodCompositionCohort
		        .setCompositionString("mitralStenosisDiagnosisAnsHFProgComposition AND  (heartRateDiseaseDuringPeriod AND (NOT onAtenololAtEndOfPeriod))");
		
		CohortIndicator onMitralStenosisHeartRatePeriodCohortIndicator = CohortIndicator.newFractionIndicator(
		    "onMitralStenosisHeartRatePeriodCohortIndicator",
		    new Mapped<CohortDefinition>(hFMitralStenosisWithHeartRateOfPeriodCompositionCohort, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")),
		    new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgComposition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);
		onMitralStenosisHeartRatePeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		onMitralStenosisHeartRatePeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		// ===================================================================================                       
		//   3.3. in the subgroup of rheumatic heart disease percent not penicillin
		// ===================================================================================  
		
		DrugsActiveCohortDefinition onPenicillinAtEndOfPeriod = Cohorts.createDrugsActiveCohort("onPenicillinAtEndOfPeriod",
		    "asOfDate", penicillin);
		
		CompositionCohortDefinition hFRheumaticHeartDiseaseOfPeriodCompositionCohort = new CompositionCohortDefinition();
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort.setName("hFRheumaticHeartDiseaseOfPeriodCompositionCohort");
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort.getSearches().put(
		    "rheumaticHeartDiseaseDiagnosisAndHFProgComposition",
		    new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgComposition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort.getSearches().put(
		    "onPenicillinAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onPenicillinAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		hFRheumaticHeartDiseaseOfPeriodCompositionCohort
		        .setCompositionString("rheumaticHeartDiseaseDiagnosisAndHFProgComposition AND (NOT onPenicillinAtEndOfPeriod)");
		
		CohortIndicator onRheumaticNotOnPenicillinPeriodCohortIndicator = Indicators.newFractionIndicator(
		    "onRheumaticNotOnPenicillinPeriodCohortIndicator", hFRheumaticHeartDiseaseOfPeriodCompositionCohort,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		    rheumaticHeartDiseaseDiagnosisAndHFProgComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ====================================================================            
		//  4.1. Percent and number of patients not seen in the last 6 months
		// ====================================================================
		
		EncounterCohortDefinition hFencounterDuringPeriod = Cohorts.createEncounterParameterizedByDate(
		    "encounterFormDuringPeriod", onOrAfterOnOrBeforeParamterNames, encounterTypes);
		
		CompositionCohortDefinition patientNotSeenDuringPeriodComposition = new CompositionCohortDefinition();
		patientNotSeenDuringPeriodComposition.setName("patientNotSeenDuringPeriodComposition");
		patientNotSeenDuringPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientNotSeenDuringPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientNotSeenDuringPeriodComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		patientNotSeenDuringPeriodComposition.getSearches().put(
		    "hFencounterDuringPeriod",
		    new Mapped<CohortDefinition>(hFencounterDuringPeriod, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		patientNotSeenDuringPeriodComposition.setCompositionString("patientsInHFProgram AND (NOT hFencounterDuringPeriod)");
		
		CohortIndicator percentageOfpatientNotSeenInLastSixMonthPeriodIndicator = Indicators
		        .newFractionIndicator("percentageOfpatientNotSeenInLastSixMonthPeriodIndicator",
		            patientNotSeenDuringPeriodComposition,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ====================================================================            
		//  4.2. Percent and number of patients not seen in the last 3 months
		// ====================================================================
		
		CohortIndicator percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator = Indicators
		        .newFractionIndicator("percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator",
		            patientNotSeenDuringPeriodComposition,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}"),
		            patientsInHFProgram,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//======================================================================	
		//	      4.3.    number of patients without an accompagnateur
		//======================================================================
		
		SqlCohortDefinition allPatientsWhitAccompagnateur = Cohorts.createPatientsWithAccompagnateur(
		    "allPatientsWhitAccompagnateur", "endDate");
		
		CompositionCohortDefinition patientWithoutAccompagnateurPeriodComposition = new CompositionCohortDefinition();
		patientWithoutAccompagnateurPeriodComposition.setName("patientWithoutAccompagnateurPeriodComposition");
		patientWithoutAccompagnateurPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientWithoutAccompagnateurPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWithoutAccompagnateurPeriodComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientWithoutAccompagnateurPeriodComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		patientWithoutAccompagnateurPeriodComposition.getSearches().put(
		    "allPatientsWhitAccompagnateur",
		    new Mapped<CohortDefinition>(allPatientsWhitAccompagnateur, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")));
		patientWithoutAccompagnateurPeriodComposition
		        .setCompositionString("(patientsInHFProgram AND (NOT allPatientsWhitAccompagnateur)");
		
		CohortIndicator percentPatientWithoutAccompagnateurIndicator = Indicators
		        .newFractionIndicator("percentPatientWithoutAccompagnateurIndicator",
		            patientWithoutAccompagnateurPeriodComposition, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},endDate=${endDate}"),
		            patientsInHFProgram, ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//==================================================
		//      5.1. PATIENTS WHO DIED DURING PERIOD 
		// ===================================================              
		
		CodedObsCohortDefinition diedDuringPeriod = Cohorts.createCodedObsCohortDefinition("diedDuringPeriod",
		    onOrAfterOnOrBeforeParamterNames, reasonForExitingCare, patientDied, SetComparator.IN, TimeModifier.LAST);
		
		CompositionCohortDefinition diedDuringPeriodComposition = new CompositionCohortDefinition();
		diedDuringPeriodComposition.setName("diedDuringPeriodComposition");
		diedDuringPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		diedDuringPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		diedDuringPeriodComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		diedDuringPeriodComposition.getSearches().put(
		    "diedDuringPeriod",
		    new Mapped<CohortDefinition>(diedDuringPeriod, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		diedDuringPeriodComposition.setCompositionString("(patientsInHFProgram AND diedDuringPeriod");
		
		CohortIndicator diedDuringPeriodIndicator = Indicators.newCohortIndicator("diedDuringPeriodIndicator",
		    diedDuringPeriodComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ===============================================================================                        
		//   5.2.  Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR < 2
		// ===============================================================================  
		
		NumericObsCohortDefinition INRLTTwoCohortDefinition = Cohorts.createNumericObsCohortDefinition(
		    "INRLTTwoCohortDefinition", onOrAfterOnOrBeforeParamterNames, internationalNormalizedRatio, 2.0,
		    RangeComparator.LESS_THAN, TimeModifier.LAST);
		
		CompositionCohortDefinition INRALTTwondPostCardiacSugeryCompositionCohortDefinition = new CompositionCohortDefinition();
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition
		        .setName("INRALTTwondPostCardiacSugeryCompositionCohortDefinition");
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrAfter",
		        "startedOnOrAfter", Date.class));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrBefore",
		        "startedOnOrBefore", Date.class));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter",
		        Date.class));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore",
		        Date.class));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("asOfDate", "asOfDate",
		        Date.class));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.getSearches().put(
		    "onWarfarinAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition
		        .getSearches()
		        .put(
		            "postCardiacSurgeryCohortDefinition",
		            new Mapped<CohortDefinition>(
		                    postCardiacSurgeryCohortDefinition,
		                    ParameterizableUtil
		                            .createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition.getSearches().put(
		    "INRLTTwoCohortDefinition",
		    new Mapped<CohortDefinition>(INRLTTwoCohortDefinition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		INRALTTwondPostCardiacSugeryCompositionCohortDefinition
		        .setCompositionString("INRLTTwoCohortDefinition AND postCardiacSurgeryCohortDefinition AND onWarfarinAtEndOfPeriod");
		
		CohortIndicator percentINRALTTwoPostCardiacSugeryCohortIndicator = Indicators
		        .newFractionIndicator(
		            "percentINRALTTwoPostCardiacSugeryCohortIndicator",
		            INRALTTwondPostCardiacSugeryCompositionCohortDefinition,
		            ParameterizableUtil
		                    .createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            postCardiacSurgeryCohortDefinition, ParameterizableUtil
		                    .createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		// ===============================================================================                        
		//   5.3. Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR > 4
		// ===============================================================================  
		
		NumericObsCohortDefinition INRGTFourCohortDefinition = Cohorts.createNumericObsCohortDefinition(
		    "INRGTFourCohortDefinition", onOrAfterOnOrBeforeParamterNames, internationalNormalizedRatio, 4.0,
		    RangeComparator.GREATER_EQUAL, TimeModifier.LAST);
		
		CompositionCohortDefinition INRGTFourAndPostCardiacSugeryCompositionCohortDefinition = new CompositionCohortDefinition();
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition
		        .setName("INRGTFourAndPostCardiacSugeryCompositionCohortDefinition");
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrAfter",
		        "startedOnOrAfter", Date.class));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrBefore",
		        "startedOnOrBefore", Date.class));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter",
		        Date.class));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore",
		        Date.class));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("asOfDate", "asOfDate",
		        Date.class));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.getSearches().put(
		    "onWarfarinAtEndOfPeriod",
		    new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriod, ParameterizableUtil
		            .createParameterMappings("asOfDate=${asOfDate}")));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition
		        .getSearches()
		        .put(
		            "postCardiacSurgeryCohortDefinition",
		            new Mapped<CohortDefinition>(
		                    postCardiacSurgeryCohortDefinition,
		                    ParameterizableUtil
		                            .createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.getSearches().put(
		    "INRGTFourCohortDefinition",
		    new Mapped<CohortDefinition>(INRGTFourCohortDefinition, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		INRGTFourAndPostCardiacSugeryCompositionCohortDefinition
		        .setCompositionString("INRGTFourCohortDefinition AND postCardiacSurgeryCohortDefinition AND onWarfarinAtEndOfPeriod");
		
		CohortIndicator percentINRGTTFourPostCardiacSugeryCohortIndicator = Indicators
		        .newFractionIndicator(
		            "percentINRGTTFourPostCardiacSugeryCohortIndicator",
		            INRGTFourAndPostCardiacSugeryCompositionCohortDefinition,
		            ParameterizableUtil
		                    .createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}"),
		            postCardiacSurgeryCohortDefinition, ParameterizableUtil
		                    .createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}"));
		
		//===============================================================================                 
		//5.4. PATIENTS WHO HAD A HOSPITALIZATION IN THE PAST MONTH
		//===============================================================================                 
		
		CodedObsCohortDefinition hospitalizedDuringPeriod = Cohorts.createCodedObsCohortDefinition(
		    "hospitalizedDuringPeriod", onOrAfterOnOrBeforeParamterNames, disposition, admitToHospital, SetComparator.IN,
		    TimeModifier.LAST);
		
		CompositionCohortDefinition hospitalizedDuringPeriodComposition = new CompositionCohortDefinition();
		hospitalizedDuringPeriodComposition.setName("hospitalizedDuringPeriodComposition");
		hospitalizedDuringPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		hospitalizedDuringPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		hospitalizedDuringPeriodComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hospitalizedDuringPeriodComposition.getSearches().put(
		    "hospitalizedDuringPeriod",
		    new Mapped<CohortDefinition>(hospitalizedDuringPeriod, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		hospitalizedDuringPeriodComposition.setCompositionString("(patientsInHFProgram AND hospitalizedDuringPeriod");
		
		CohortIndicator hospitalizedDuringPeriodIndicator = Indicators.newCohortIndicator("hospitalizedDuringPeriodIndicator",
		    hospitalizedDuringPeriodComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//===============================================================================                   
		//
		//   6.1.  number of patients without a height ever
		//
		// ===============================================================================  
		NumericObsCohortDefinition heightCohortDefinition = Cohorts.createNumericObsCohortDefinition(
		    "heightCohortDefinition", "onOrBefore", height, 0.0, RangeComparator.GREATER_EQUAL, TimeModifier.ANY);
		
		CompositionCohortDefinition heightEverCompositionCohortDefinition = new CompositionCohortDefinition();
		heightEverCompositionCohortDefinition.setName("heightEverCompositionCohortDefinition");
		heightEverCompositionCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		heightEverCompositionCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		heightEverCompositionCohortDefinition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		heightEverCompositionCohortDefinition.getSearches().put(
		    "heightCohortDefinition",
		    new Mapped<CohortDefinition>(heightCohortDefinition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		heightEverCompositionCohortDefinition.setCompositionString("(patientsInHFProgram AND (NOT heightCohortDefinition)");
		
		CohortIndicator heightEverCohortIndicator = Indicators.newCohortIndicator("heightEverCohortIndicator",
		    heightEverCompositionCohortDefinition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ===============================================================================                 
		//  37. NUMBER OF  PATIENTS WITHOUT A DONNE DE BASE 
		// =============================================================================== 
		
		EncounterCohortDefinition encounterFormDuringDDBPeriod = Cohorts.createEncounterBasedOnForms(
		    "encounterFormDuringDDBPeriod", "onOrBefore", cardiologyDDB);
		
		CompositionCohortDefinition patientWithoutDonneDebasePeriodComposition = new CompositionCohortDefinition();
		patientWithoutDonneDebasePeriodComposition.setName("patientWithoutDonneDebasePeriodComposition");
		patientWithoutDonneDebasePeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientWithoutDonneDebasePeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWithoutDonneDebasePeriodComposition.getSearches().put(
		    "patientsInHFProgram",
		    new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		patientWithoutDonneDebasePeriodComposition.getSearches().put(
		    "encounterFormDuringDDBPeriod",
		    new Mapped<CohortDefinition>(encounterFormDuringDDBPeriod, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore}")));
		patientWithoutDonneDebasePeriodComposition
		        .setCompositionString("(patientsInHFProgram AND (NOT encounterFormDuringDDBPeriod)");
		
		CohortIndicator patientWithoutDonneDebasePeriodIndicator = Indicators.newCohortIndicator(
		    "patientWithoutDonneDebasePeriodIndicator", patientWithoutDonneDebasePeriodComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//Add global filters to the report
		dsd.addColumn(
		    "1.1.m",
		    "% of Male",
		    new Mapped(percentMaleInFHProgramIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "1.1.f",
		    "% of Female",
		    new Mapped(percentFemaleInFHProgramIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("1.2", "Median Age",
		    new Mapped(medianAge, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")),
		    "");
		dsd.addColumn(
		    "1.3",
		    "Number of new patients enrolled in reporting period",
		    new Mapped(patientsEnrolledInHFIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "1.4",
		    "Total number of Patients",
		    new Mapped(patientsInHFIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "2.1",
		    "Number and percent of patients without a cardiology consultation",
		    new Mapped(patientsInHFProgramWithouCardFormIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.2",
		    "Number and percent of patients without a preliminary echocardiographic diagnosis",
		    new Mapped(hfEchocardiographyPercentageIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.3",
		    "Percent without a creatinine in the last 6 months",
		    new Mapped(hfPatientWithoutCreatininePercentIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("2.4", "Number of patients with Cardiomyopathy", new Mapped(cardiomyopathyDiognosisAnsHFProgIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.5",
		    "Number of patients with pure mitral stenosis",
		    new Mapped(mitralStenosisDiagnosisAnsHFProgIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.6",
		    "Number of patients with other rheumatic heart disease",
		    new Mapped(rheumaticHeartDiseaseDiagnosisAndHFProgIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.7",
		    "Number of patients with hypertensive heart disease",
		    new Mapped(hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.8",
		    "Number of patients with pericardial disease",
		    new Mapped(pericardialDiseaseDiagnosisAndHFProgIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.9",
		    "Number of patients with congenital heart disease",
		    new Mapped(congenitalDiseaseDiagnosisAndHFProgIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "2.10",
		    "Percent of patients with creatinine > 200",
		    new Mapped(hfpatientsWithCreatininePercentIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("2.11", "Number of patients in post-cardiac surgery", new Mapped(postCardiacSurgeryCohortIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "3.1",
		    "In the subgroup (cardiomyopathy): percent of Patients with heart rate > 60 at last visit not on carvedilol",
		    new Mapped(onCardiomyopathyHeartRatePeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.2",
		    "in the subgroup (mitral stenosis): percent of Patients with heart rate > 60 at last visit not on atenolol",
		    new Mapped(onMitralStenosisHeartRatePeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.3",
		    "in the subgroup (rheumatic heart disease): percent not on penicillin",
		    new Mapped(onRheumaticNotOnPenicillinPeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.4",
		    "In the subgroup (female < age 50), percent of patients not on family planning",
		    new Mapped(patientsInHFWithoutFamilyPlanningIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("3.5", "Percent of Patients on lasix", new Mapped(onLasixAtEndOfPeriodCohortIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("3.6", "Percent of Patients on atenolol", new Mapped(onAtenololAtEndOfPeriodCohortIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("3.7", "Percent of Patients on carvedilol", new Mapped(onCarvedilolAtEndOfPeriodCohortIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("3.8", "Percent of Patients on aldactone", new Mapped(onAldactoneAtEndOfPeriodCohortIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.9",
		    "Percent of Patients on lisinopril or captopril",
		    new Mapped(onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.10",
		    "Percent of Patients on atenolol and carvedilol",
		    new Mapped(onAtenololAndCarvedilolEndOfPeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.11",
		    "Percent of Patients on lisinopril and captopril",
		    new Mapped(onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "3.12",
		    "Percent and number of Patients on warfarin",
		    new Mapped(onWarfarinAtEndOfPeriodCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "4.1",
		    "Percent and number of patients not seen in the last 6 months",
		    new Mapped(percentageOfpatientNotSeenInLastSixMonthPeriodIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "4.2",
		    "Percent and number of patients not seen in the last 3 months",
		    new Mapped(percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "4.3",
		    "Number of patients without an accompagnateur",
		    new Mapped(percentPatientWithoutAccompagnateurIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "5.1",
		    "Number of people who have ever been in the heart failure program who died in report window",
		    new Mapped(diedDuringPeriodIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "5.2",
		    "Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR < 2",
		    new Mapped(percentINRALTTwoPostCardiacSugeryCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "5.3",
		    "Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR > 4",
		    new Mapped(percentINRGTTFourPostCardiacSugeryCohortIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn("5.4", "Number of hospitalizations in reporting window", new Mapped(hospitalizedDuringPeriodIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn("6.1", "number of patients without a height ever", new Mapped(heightEverCohortIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		dsd.addColumn(
		    "6.2",
		    "number of patients without a donne de base (intake form)",
		    new Mapped(patientWithoutDonneDebasePeriodIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		return dsd;
	}
	
	private void setupProperties() {
		heartFailure = gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM);
		
		postOp = gp.getProgramWorkflowState(GlobalPropertiesManagement.HEART_FAILURE_POST_OPERATIVE_STATE,
		    GlobalPropertiesManagement.HEART_FAILURE_SURGERY_STATUS, GlobalPropertiesManagement.HEART_FAILURE_PROGRAM);
		
		cardiologyForm = gp.getFormList(GlobalPropertiesManagement.CARDIOLOGY_CONSULT_FORM);
		cardiologyDDB = gp.getFormList(GlobalPropertiesManagement.CARDIOLOGY_DDB);
		
		encounterTypes = gp.getEncounterTypeList(GlobalPropertiesManagement.CARDIOLOGY_ENCTOUNTER_TYPES);
		
		echocardiographyResult = gp.getConcept(GlobalPropertiesManagement.DDB_ECHOCARDIOGRAPH_RESULT);
		notDone = gp.getConcept(GlobalPropertiesManagement.NOT_DONE);
		serumCreatinine = gp.getConcept(GlobalPropertiesManagement.SERUM_CREATININE);
		heartFailureDiagnosis = gp.getConcept(GlobalPropertiesManagement.HEART_FAILURE_DIAGNOSIS);
		cardiomyopathy = gp.getConcept(GlobalPropertiesManagement.CARDIOMYOPATHY);
		miralStenosis = gp.getConcept(GlobalPropertiesManagement.MITRAL_STENOSIS);
		rhuematicHeartDisease = gp.getConcept(GlobalPropertiesManagement.RHUEMATIC_HEART_DISEASE);
		hypertensiveDisease = gp.getConcept(GlobalPropertiesManagement.HYPERTENSIVE_HEART_DISEASE);
		pericardialDisease = gp.getConcept(GlobalPropertiesManagement.PERICARDIAL_DISEASE);
		congenitalHeartFailure = gp.getConcept(GlobalPropertiesManagement.CONGENITAL_HEART_FAILURE);
		patientsUsingFamilyPlanning = gp.getConcept(GlobalPropertiesManagement.PATIENTS_USING_FAMILY_PLANNING);
		pulse = gp.getConcept(GlobalPropertiesManagement.PULSE);
		reasonForExitingCare = gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);
		patientDied = gp.getConcept(GlobalPropertiesManagement.PATIENT_DIED);
		internationalNormalizedRatio = gp.getConcept(GlobalPropertiesManagement.INTERNATIONAL_NORMALIZED_RATIO);
		disposition = gp.getConcept(GlobalPropertiesManagement.DISPOSITION);
		admitToHospital = gp.getConcept(GlobalPropertiesManagement.ADMIT_TO_HOSPITAL);
		height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		
		furosemide = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.FUROSEMIDE));
		atenolol = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.ATENOLOL));
		carvedilol = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.CARVEDILOL));
		aldactone = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.ALDACTONE));
		lisinopril = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.LISINOPRIL));
		captopril = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.CAPTOPRIL));
		warfarin = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.WARFARIN));
		penicillin = gp.getDrugs(gp.getConcept(GlobalPropertiesManagement.PENICILLIN));
		
		onOrAfterOnOrBeforeParamterNames.add("onOrAfter");
		onOrAfterOnOrBeforeParamterNames.add("onOrBefore");
	}
	
}
