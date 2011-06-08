package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import javax.xml.crypto.KeySelector.Purpose;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.logic.LogicService;
import org.openmrs.logic.rule.AgeRule;
import org.openmrs.module.rwandareports.definition.DrugsActiveCohortDefinition;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
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
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.aggregation.MedianAggregator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.HeartFailureReportConstants;

public class SetupHeartFailurereport {
	protected final static Log log = LogFactory.getLog(SetupHeartFailurereport.class);
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupHeartFailurereport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		//setUpGlobalProperties();
		
		createLocationCohortDefinitions();
		//createCompositionCohortDefinitions();
		//createIndicators();
		ReportDefinition rd = createReportDefinition();
		h.createXlsOverview(rd, "heartfailurereporttemplate.xls", "Xlsheartfailurereporttemplate", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Xlsheartfailurereporttemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(PeriodIndicatorReportDefinition.class, "Heart Failure Report");
		
		h.purgeDefinition(DataSetDefinition.class, "Heart Failure Report Data Set");
		h.purgeDefinition(CohortDefinition.class, "location: Heart Failure Patients at location");
		

		h.purgeDefinition(CohortDefinition.class, "patientsEnrolledInHFProgram");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFProgram");
		h.purgeDefinition(CohortDefinition.class, "malesDefinition");
		h.purgeDefinition(CohortDefinition.class, "femalesDefinition");
		h.purgeDefinition(CohortDefinition.class, "maleInFHProgramComposition");
		h.purgeDefinition(CohortDefinition.class, "femaleInFHProgramComposition");
		h.purgeDefinition(CohortDefinition.class, "patientsEnrolledInHFIndicator");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCardFormBeforeEndDate");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFProgramBeforeEndDate");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFProgramWithouCardForm");
		/*
		h.purgeDefinition(CohortDefinition.class, "allPatientsInHF");
		h.purgeDefinition(CohortDefinition.class, "pateintsDied");
		h.purgeDefinition(CohortDefinition.class, "pateintsTransferedOut");
		h.purgeDefinition(CohortDefinition.class, "pateintsCured");
		h.purgeDefinition(CohortDefinition.class, "pateintsRefused");*/
		h.purgeDefinition(CohortDefinition.class, "echocardiographyDuringPeriod");
		h.purgeDefinition(CohortDefinition.class, "echocardiographyAndHFProgramComposition");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCreatinineCohortDef");
		h.purgeDefinition(CohortDefinition.class, "hfPatientWithoutCreatinineCompositionCohortDef");
		h.purgeDefinition(CohortDefinition.class, "cardiomyopathyDiognosis");		
		h.purgeDefinition(CohortDefinition.class, "cardiomyopathyDiognosisAnsHFProgComposition");
		h.purgeDefinition(CohortDefinition.class, "mitralStenosisDiagnosis");
		h.purgeDefinition(CohortDefinition.class, "mitralStenosisDiagnosisAnsHFProgComposition");
		h.purgeDefinition(CohortDefinition.class, "rheumaticHeartDiseaseDiagnosis");
		h.purgeDefinition(CohortDefinition.class, "rheumaticHeartDiseaseDiagnosisAndHFProgComposition");
		h.purgeDefinition(CohortDefinition.class, "hypertensiveHeartDiseaseDiagnosis");
		h.purgeDefinition(CohortDefinition.class, "hypertensiveHeartDiseaseDiagnosisAndHFProgComposition");
		h.purgeDefinition(CohortDefinition.class, "pericardialDiseaseDiagnosis");
		h.purgeDefinition(CohortDefinition.class, "pericardialDiseaseDiagnosisAndHFProgComposition");
		h.purgeDefinition(CohortDefinition.class, "congenitalDiseaseDiagnosis");
		h.purgeDefinition(CohortDefinition.class, "congenitalDiseaseDiagnosisAndHFProgComposition");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCreatinine");
		h.purgeDefinition(CohortDefinition.class, "hfpatientsWithCreatinineComposition");
		h.purgeDefinition(CohortDefinition.class, "postCardiacSurgeryCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "patientsInFamilyPlanning");
		h.purgeDefinition(CohortDefinition.class, "lessThanFifty");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFWithoutFamilyPlanningCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onLasixAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFonLasixAtEndOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onAtenololAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFonAtenololAtEndOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onCarvedilolAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFonCarvedilolAtEndOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onAldactoneAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFonAldactoneAtEndOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onWarfarinAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFonWarfarinAtEndOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onLisinoprilAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "onCaptoprilAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFLisinoprilOrCaptoprilCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "hFAtenololAndCarvedilolCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "hFLisinoprilAndCaptoprilCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "heartRateDiseaseDuringPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "hFMitralStenosisWithHeartRateOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "onPenicillinAtEndOfPeriod");
		h.purgeDefinition(CohortDefinition.class, "hFRheumaticHeartDiseaseOfPeriodCompositionCohort");
		h.purgeDefinition(CohortDefinition.class, "hFencounterDuringPeriod");
		h.purgeDefinition(CohortDefinition.class, "patientNotSeenDuringPeriodComposition");
		h.purgeDefinition(CohortDefinition.class, "allPatientsWhitAccompagnateur");
		h.purgeDefinition(CohortDefinition.class, "patientWithoutAccompagnateurPeriodComposition");
		h.purgeDefinition(CohortDefinition.class, "diedDuringPeriod");
		h.purgeDefinition(CohortDefinition.class, "diedDuringPeriodComposition");
		h.purgeDefinition(CohortDefinition.class, "INRLTTwoCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "INRALTTwondPostCardiacSugeryCompositionCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "INRGTFourCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "INRGTFourAndPostCardiacSugeryCompositionCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "hospitalizedDuringPeriod");
		h.purgeDefinition(CohortDefinition.class, "hospitalizedDuringPeriodComposition");
		h.purgeDefinition(CohortDefinition.class, "heightCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "heightEverCompositionCohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "encounterFormDuringDDBPeriod");
		h.purgeDefinition(CohortDefinition.class, "patientWithoutDonneDebasePeriodComposition");
		
			
		
		h.purgeDefinition(CohortIndicator.class, "percentMaleInFHProgramIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentFemaleInFHProgramIndicator");
		h.purgeDefinition(CohortIndicator.class, "medianAge");	
		h.purgeDefinition(CohortIndicator.class, "patientsInHFIndicator");	
		h.purgeDefinition(CohortIndicator.class, "patientsInHFProgramWithouCardFormIndicator");
		h.purgeDefinition(CohortIndicator.class, "hfEchocardiographyPercentageIndicator");
		h.purgeDefinition(CohortIndicator.class, "hfPatientWithoutCreatininePercentIndicator");
		h.purgeDefinition(CohortIndicator.class, "cardiomyopathyDiognosisAnsHFProgIndicator");
		h.purgeDefinition(CohortIndicator.class, "mitralStenosisDiagnosisAnsHFProgIndicator");
		h.purgeDefinition(CohortIndicator.class, "rheumaticHeartDiseaseDiagnosisAndHFProgIndicator");
		h.purgeDefinition(CohortIndicator.class, "hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator");
		h.purgeDefinition(CohortIndicator.class, "pericardialDiseaseDiagnosisAndHFProgIndicator");
		h.purgeDefinition(CohortIndicator.class, "congenitalDiseaseDiagnosisAndHFProgIndicator");
		h.purgeDefinition(CohortIndicator.class, "hfpatientsWithCreatininePercentIndicator");
		h.purgeDefinition(CohortIndicator.class, "postCardiacSurgeryCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsInHFWithoutFamilyPlanningIndicator");		
		h.purgeDefinition(CohortIndicator.class, "onLasixAtEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onAtenololAtEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onCarvedilolAtEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onAldactoneAtEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onWarfarinAtEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onAtenololAndCarvedilolEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onCardiomyopathyHeartRatePeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onMitralStenosisHeartRatePeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "onRheumaticNotOnPenicillinPeriodCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentageOfpatientNotSeenInLastSixMonthPeriodIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentPatientWithoutAccompagnateurIndicator");
		h.purgeDefinition(CohortIndicator.class, "diedDuringPeriodIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentINRALTTwoPostCardiacSugeryCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentINRGTTFourPostCardiacSugeryCohortIndicator");
		h.purgeDefinition(CohortIndicator.class, "hospitalizedDuringPeriodIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientWithoutDonneDebasePeriodIndicator");

}
	
	
	private ReportDefinition createReportDefinition() {
		// Heart Failure Indicator Report
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));		
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
				
		rd.setName("Heart Failure Report");
		
		rd.setupDataSetDefinition();

//Patient In Heart Failure Program
		

		InProgramCohortDefinition patientsInHFProgram=getInProgramCohortDefinition("patientsInHFProgram", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
		patientsInHFProgram.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsInHFProgram.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientsInHFProgram);
		
		/*InProgramCohortDefinition allPatientsInHF=getInProgramCohortDefinition("allPatientsInHF", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
		allPatientsInHF.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		allPatientsInHF.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(allPatientsInHF);
		
		CodedObsCohortDefinition pateintsDied =makeCodedObsCohortDefinition("pateintsDied",HeartFailureReportConstants.REASON_FOR_EXITING_CARE, HeartFailureReportConstants.PATIENT_DIED, SetComparator.IN, TimeModifier.LAST);                
		pateintsDied.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		h.replaceCohortDefinition(pateintsDied);
		
		CodedObsCohortDefinition pateintsTransferedOut =makeCodedObsCohortDefinition("pateintsTransferedOut",HeartFailureReportConstants.REASON_FOR_EXITING_CARE, HeartFailureReportConstants.PATIENT_TRANSFERRED_OUT, SetComparator.IN, TimeModifier.LAST);                
		pateintsTransferedOut.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		h.replaceCohortDefinition(pateintsTransferedOut);
		
		CodedObsCohortDefinition pateintsCured =makeCodedObsCohortDefinition("pateintsCured",HeartFailureReportConstants.REASON_FOR_EXITING_CARE, HeartFailureReportConstants.PATIENT_CURED, SetComparator.IN, TimeModifier.LAST);                
		pateintsCured.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		h.replaceCohortDefinition(pateintsCured);
		
		CodedObsCohortDefinition pateintsRefused =makeCodedObsCohortDefinition("pateintsRefused",HeartFailureReportConstants.REASON_FOR_EXITING_CARE, HeartFailureReportConstants.PATIENT_REFUSED, SetComparator.IN, TimeModifier.LAST);                
		pateintsRefused.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		h.replaceCohortDefinition(pateintsRefused);
		
		CompositionCohortDefinition patientsInHFProgram=new CompositionCohortDefinition();
		patientsInHFProgram.setName("patientsInHFProgram");
		patientsInHFProgram.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsInHFProgram.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsInHFProgram.getSearches().put("allPatientsInHF", new Mapped<CohortDefinition>(allPatientsInHF,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsInHFProgram.getSearches().put("pateintsDied", new Mapped<CohortDefinition>(pateintsDied,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
		patientsInHFProgram.getSearches().put("pateintsTransferedOut", new Mapped<CohortDefinition>(pateintsTransferedOut,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
		patientsInHFProgram.getSearches().put("pateintsCured", new Mapped<CohortDefinition>(pateintsCured,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
		patientsInHFProgram.getSearches().put("pateintsRefused", new Mapped<CohortDefinition>(pateintsRefused,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
		patientsInHFProgram.setCompositionString("allPatientsInHF AND (NOT(pateintsDied OR pateintsTransferedOut OR pateintsCured OR pateintsRefused))");
		h.replaceCohortDefinition(patientsInHFProgram);*/
		
//============================================================================
//  1.1.m & 1.1.f % male and female
//============================================================================

//Gender Cohort definitions
		
		GenderCohortDefinition femalesDefinition=new GenderCohortDefinition();
		femalesDefinition.setName("femalesDefinition");
		femalesDefinition.setFemaleIncluded(true);
		h.replaceCohortDefinition(femalesDefinition);
		
		GenderCohortDefinition malesDefinition=new GenderCohortDefinition();
		malesDefinition.setName("malesDefinition");
		malesDefinition.setMaleIncluded(true);		
		h.replaceCohortDefinition(malesDefinition);
		
		CompositionCohortDefinition maleInFHProgramComposition=new CompositionCohortDefinition();
		maleInFHProgramComposition.setName("maleInFHProgramComposition");
		maleInFHProgramComposition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleInFHProgramComposition.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleInFHProgramComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleInFHProgramComposition.getSearches().put("malesDefinition",new Mapped<CohortDefinition>(malesDefinition,null));
		maleInFHProgramComposition.setCompositionString("patientsInHFProgram AND malesDefinition");
		h.replaceCohortDefinition(maleInFHProgramComposition);
		
		CompositionCohortDefinition femaleInFHProgramComposition=new CompositionCohortDefinition();
		femaleInFHProgramComposition.setName("femaleInFHProgramComposition");
		femaleInFHProgramComposition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleInFHProgramComposition.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleInFHProgramComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleInFHProgramComposition.getSearches().put("femalesDefinition",new Mapped<CohortDefinition>(femalesDefinition,null));
		femaleInFHProgramComposition.setCompositionString("patientsInHFProgram AND femalesDefinition");
		h.replaceCohortDefinition(femaleInFHProgramComposition);
		
		CohortIndicator percentMaleInFHProgramIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(maleInFHProgramComposition, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsInHFProgram, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentMaleInFHProgramIndicator.setName("percentMaleInFHProgramIndicator");
		percentMaleInFHProgramIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentMaleInFHProgramIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentMaleInFHProgramIndicator);
		
		CohortIndicator percentFemaleInFHProgramIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(femaleInFHProgramComposition, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsInHFProgram, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentFemaleInFHProgramIndicator.setName("percentFemaleInFHProgramIndicator");
		percentFemaleInFHProgramIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentFemaleInFHProgramIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentFemaleInFHProgramIndicator);
	
//=========================================================================================
//      1.2.   median age
//=========================================================================================
       LogicService ls = Context.getLogicService();
		
		try {
			ls.getRule("AGE");
		} catch (Exception ex){
			AgeRule ageRule = new AgeRule();
			ls.addRule("AGE", ageRule);
		}
            
		CohortIndicator medianAge = CohortIndicator.newLogicIndicator("medianAge", new Mapped<CohortDefinition>(patientsInHFProgram, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null, MedianAggregator.class, "AGE");
		medianAge.addParameter(new Parameter("startDate", "startDate", Date.class));
		medianAge.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(medianAge);

//=========================================================================================
//      1.3.   Patients enrolled
//=========================================================================================
		
//Patient Enrolled in Heart Failure Program
		
		ProgramEnrollmentCohortDefinition patientsEnrolledInHFProgram=getProgramEnrollment("patientsEnrolledInHFProgram", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
		patientsEnrolledInHFProgram.addParameter(new Parameter("enrolledOnOrAfter","enrolledOnOrAfter",Date.class));
		patientsEnrolledInHFProgram.addParameter(new Parameter("enrolledOnOrBefore","enrolledOnOrBefore",Date.class));
		h.replaceCohortDefinition(patientsEnrolledInHFProgram);
		
		CohortIndicator patientsEnrolledInHFIndicator = new CohortIndicator();
		patientsEnrolledInHFIndicator.setName("patientsEnrolledInHFIndicator");
		patientsEnrolledInHFIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsEnrolledInHFIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsEnrolledInHFIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsEnrolledInHFProgram,ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		h.replaceDefinition(patientsEnrolledInHFIndicator);

//=========================================================================================
//      1.4.   Total number of Patient in Heart Failure
//=========================================================================================

		CohortIndicator patientsInHFIndicator = new CohortIndicator();
		patientsInHFIndicator.setName("patientsInHFIndicator");
		patientsInHFIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsInHFIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsInHFIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(patientsInHFIndicator);
		
//===============================================================================
//  2.1. number and percent of patients without a cardiology consultation
//===============================================================================
		
	List<Form> cardCons=new ArrayList<Form>();
	String formid=Context.getAdministrationService().getGlobalProperty("cardiologyreporting.cardilogyConsultationFormId");
	cardCons.add(Context.getFormService().getForm(Integer.valueOf(formid)));
	
	EncounterCohortDefinition patientsWithCardFormBeforeEndDate =makeEncounterCohortDefinition("patientsWithCardFormBeforeEndDate", cardCons);       
	patientsWithCardFormBeforeEndDate.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(patientsWithCardFormBeforeEndDate);
	
   /* InProgramCohortDefinition patientsInHFProgramBeforeEndDate=getInProgramCohortDefinition("patientsInHFProgramBeforeEndDate", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
    patientsInHFProgramBeforeEndDate.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    h.replaceCohortDefinition(patientsInHFProgramBeforeEndDate);
    */
    CompositionCohortDefinition patientsInHFProgramWithouCardForm=new CompositionCohortDefinition();
    patientsInHFProgramWithouCardForm.setName("patientsInHFProgramWithouCardForm");
    patientsInHFProgramWithouCardForm.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
    patientsInHFProgramWithouCardForm.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
    patientsInHFProgramWithouCardForm.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
    patientsInHFProgramWithouCardForm.getSearches().put("patientsWithCardFormBeforeEndDate",new Mapped<CohortDefinition>(patientsWithCardFormBeforeEndDate,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    patientsInHFProgramWithouCardForm.setCompositionString("patientsInHFProgram AND (NOT patientsWithCardFormBeforeEndDate)");
	h.replaceCohortDefinition(patientsInHFProgramWithouCardForm);
	
	/*CohortIndicator patientsInHFProgramWithouCardFormIndicator = new CohortIndicator();
	patientsInHFProgramWithouCardFormIndicator.setName("patientsInHFProgramWithouCardFormIndicator");
	patientsInHFProgramWithouCardFormIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	patientsInHFProgramWithouCardFormIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	patientsInHFProgramWithouCardFormIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsInHFProgramWithouCardForm,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
	h.replaceDefinition(patientsInHFProgramWithouCardFormIndicator);
	*/
	CohortIndicator patientsInHFProgramWithouCardFormIndicator = CohortIndicator.newFractionIndicator
	(null,new Mapped<CohortDefinition>(patientsInHFProgramWithouCardForm, 
			ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
			new Mapped<CohortDefinition>(patientsInHFProgram, 
			ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
			null);
	patientsInHFProgramWithouCardFormIndicator.setName("patientsInHFProgramWithouCardFormIndicator");
	patientsInHFProgramWithouCardFormIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	patientsInHFProgramWithouCardFormIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	h.replaceDefinition(patientsInHFProgramWithouCardFormIndicator);
	
	
// =============================================================================== 
//   2.2.Number and percent of patients without a preliminary echocardiographic diagnosis
// ===============================================================================   
// echocardiographyDuringPeriod echocardiographyAndHFProgramComposition hfEchocardiographyPercentageIndicator

      CodedObsCohortDefinition echocardiographyDuringPeriod =makeCodedObsCohortDefinition("echocardiographyDuringPeriod",HeartFailureReportConstants.DDB_ECHOCARDIOGRAPH_RESULT, HeartFailureReportConstants.NOT_DONE, SetComparator.IN, TimeModifier.ANY);		
      h.replaceCohortDefinition(echocardiographyDuringPeriod);
       
      CompositionCohortDefinition echocardiographyAndHFProgramComposition = new CompositionCohortDefinition();
      echocardiographyAndHFProgramComposition.setName("echocardiographyAndHFProgramComposition");
      echocardiographyAndHFProgramComposition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
      echocardiographyAndHFProgramComposition.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
      echocardiographyAndHFProgramComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
      echocardiographyAndHFProgramComposition.getSearches().put("echocardiographyDuringPeriod", new Mapped<CohortDefinition>(echocardiographyDuringPeriod, null));
      echocardiographyAndHFProgramComposition.setCompositionString("(patientsInHFProgram AND echocardiographyDuringPeriod");
      h.replaceCohortDefinition(echocardiographyAndHFProgramComposition);
      
      CohortIndicator hfEchocardiographyPercentageIndicator = CohortIndicator.newFractionIndicator("hfEchocardiographyPercentageIndicator", new Mapped<CohortDefinition>(echocardiographyAndHFProgramComposition , ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
   		  new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
      hfEchocardiographyPercentageIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
      hfEchocardiographyPercentageIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
      h.replaceDefinition(hfEchocardiographyPercentageIndicator);

      
// ===============================================================================                         
//                 2.3.  Percent without a creatinine in the last 6 months
// ===============================================================================   

      NumericObsCohortDefinition patientsWithCreatinineCohortDef=makeNumericObsCohortDefinition("patientsWithCreatinineCohortDef", HeartFailureReportConstants.SERUM_CREATININE, 0.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
      patientsWithCreatinineCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
      patientsWithCreatinineCohortDef.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
      h.replaceCohortDefinition(patientsWithCreatinineCohortDef);
                              
      CompositionCohortDefinition hfPatientWithoutCreatinineCompositionCohortDef = new CompositionCohortDefinition();
      hfPatientWithoutCreatinineCompositionCohortDef.setName("hfPatientWithoutCreatinineCompositionCohortDef");
      hfPatientWithoutCreatinineCompositionCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
      hfPatientWithoutCreatinineCompositionCohortDef.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
      hfPatientWithoutCreatinineCompositionCohortDef.getSearches().put("patientsWithCreatinineCohortDef", new Mapped<CohortDefinition>(patientsWithCreatinineCohortDef, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
      hfPatientWithoutCreatinineCompositionCohortDef.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
      hfPatientWithoutCreatinineCompositionCohortDef.setCompositionString("patientsInHFProgram AND (NOT patientsWithCreatinineCohortDef)");
      h.replaceCohortDefinition(hfPatientWithoutCreatinineCompositionCohortDef);
      
      CohortIndicator hfPatientWithoutCreatininePercentIndicator = CohortIndicator.newFractionIndicator("hfPatientWithoutCreatininePercentIndicator", new Mapped<CohortDefinition>(hfPatientWithoutCreatinineCompositionCohortDef , ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${endDate}")), 
       		  new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
      hfPatientWithoutCreatininePercentIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
      hfPatientWithoutCreatininePercentIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
      h.replaceDefinition(hfPatientWithoutCreatininePercentIndicator);

//===============================================================================                 
//   PATIENTS WHO WITH HEART FAILURE DIAGNOSIS IN THE LAST MONTH
// ===============================================================================                 
    
//===============================================================================                 
//     2.4. Patient with Cardiomyopathy
// ===============================================================================                 
 
	CodedObsCohortDefinition cardiomyopathyDiognosis =makeCodedObsCohortDefinition("cardiomyopathyDiognosis", HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.CARDIOMYOPATHY, SetComparator.IN, TimeModifier.ANY);	
	cardiomyopathyDiognosis.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(cardiomyopathyDiognosis);
	
	 CompositionCohortDefinition cardiomyopathyDiognosisAnsHFProgComposition = new CompositionCohortDefinition();
	 cardiomyopathyDiognosisAnsHFProgComposition.setName("cardiomyopathyDiognosisAnsHFProgComposition");
	 cardiomyopathyDiognosisAnsHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 cardiomyopathyDiognosisAnsHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	 cardiomyopathyDiognosisAnsHFProgComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	 cardiomyopathyDiognosisAnsHFProgComposition.getSearches().put("cardiomyopathyDiognosis", new Mapped<CohortDefinition>(cardiomyopathyDiognosis, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	 cardiomyopathyDiognosisAnsHFProgComposition.setCompositionString("(patientsInHFProgram AND cardiomyopathyDiognosis");
	 h.replaceCohortDefinition(cardiomyopathyDiognosisAnsHFProgComposition);
 
	 CohortIndicator cardiomyopathyDiognosisAnsHFProgIndicator = new CohortIndicator();
	 cardiomyopathyDiognosisAnsHFProgIndicator.setName("cardiomyopathyDiognosisAnsHFProgIndicator");
	 cardiomyopathyDiognosisAnsHFProgIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	 cardiomyopathyDiognosisAnsHFProgIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
     cardiomyopathyDiognosisAnsHFProgIndicator.setCohortDefinition(new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
     h.replaceDefinition(cardiomyopathyDiognosisAnsHFProgIndicator);
      
      
//===============================================================================                 
//               2.5.  PATIENTS WHICH HAVE HAD  PURE MITRAL STENOSIS
//===============================================================================  

    CodedObsCohortDefinition mitralStenosisDiagnosis =makeCodedObsCohortDefinition("mitralStenosisDiagnosis",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.MITRAL_STENOSIS, SetComparator.IN, TimeModifier.ANY);                
    mitralStenosisDiagnosis.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    h.replaceCohortDefinition(mitralStenosisDiagnosis);
    
    CompositionCohortDefinition mitralStenosisDiagnosisAnsHFProgComposition = new CompositionCohortDefinition();
    mitralStenosisDiagnosisAnsHFProgComposition.setName("mitralStenosisDiagnosisAnsHFProgComposition");
    mitralStenosisDiagnosisAnsHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    mitralStenosisDiagnosisAnsHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    mitralStenosisDiagnosisAnsHFProgComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    mitralStenosisDiagnosisAnsHFProgComposition.getSearches().put("mitralStenosisDiagnosis", new Mapped<CohortDefinition>(mitralStenosisDiagnosis, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    mitralStenosisDiagnosisAnsHFProgComposition.setCompositionString("(patientsInHFProgram AND mitralStenosisDiagnosis");
	 h.replaceCohortDefinition(mitralStenosisDiagnosisAnsHFProgComposition);

	 CohortIndicator mitralStenosisDiagnosisAnsHFProgIndicator = new CohortIndicator();
	 mitralStenosisDiagnosisAnsHFProgIndicator.setName("mitralStenosisDiagnosisAnsHFProgIndicator");
	 mitralStenosisDiagnosisAnsHFProgIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	 mitralStenosisDiagnosisAnsHFProgIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	 mitralStenosisDiagnosisAnsHFProgIndicator.setCohortDefinition(new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
    h.replaceDefinition(mitralStenosisDiagnosisAnsHFProgIndicator);
     
      
  //===============================================================================                 
  //      2.6.  PATIENTS WHICH HAVE HAD A RHEUMATIC HEART DISEASE
  //===============================================================================  

    CodedObsCohortDefinition rheumaticHeartDiseaseDiagnosis =makeCodedObsCohortDefinition("rheumaticHeartDiseaseDiagnosis",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.RHEUMATIC_HEART_DISEASE, SetComparator.IN, TimeModifier.ANY);
    rheumaticHeartDiseaseDiagnosis.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(rheumaticHeartDiseaseDiagnosis);
	
    CompositionCohortDefinition rheumaticHeartDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
    rheumaticHeartDiseaseDiagnosisAndHFProgComposition.setName("rheumaticHeartDiseaseDiagnosisAndHFProgComposition");
    rheumaticHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    rheumaticHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    rheumaticHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    rheumaticHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put("rheumaticHeartDiseaseDiagnosis", new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosis, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    rheumaticHeartDiseaseDiagnosisAndHFProgComposition.setCompositionString("(patientsInHFProgram AND rheumaticHeartDiseaseDiagnosis");
	 h.replaceCohortDefinition(rheumaticHeartDiseaseDiagnosisAndHFProgComposition);

	 CohortIndicator rheumaticHeartDiseaseDiagnosisAndHFProgIndicator = new CohortIndicator();
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicator.setName("rheumaticHeartDiseaseDiagnosisAndHFProgIndicator");
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicator.setCohortDefinition(new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
    h.replaceDefinition(rheumaticHeartDiseaseDiagnosisAndHFProgIndicator);
     
//===============================================================================                 
//    2.7.  PATIENTS WHO HAVE HAD A HYPERTENSIVE HEART DISEASE
//===============================================================================                                
    
                   
	 
	   CodedObsCohortDefinition hypertensiveHeartDiseaseDiagnosis =makeCodedObsCohortDefinition("hypertensiveHeartDiseaseDiagnosis",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.HYPERTENSIVE_HEART_DISEASE, SetComparator.IN, TimeModifier.ANY);
	   hypertensiveHeartDiseaseDiagnosis.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	   h.replaceCohortDefinition(hypertensiveHeartDiseaseDiagnosis);
	   
	  CompositionCohortDefinition hypertensiveHeartDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
	  hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.setName("hypertensiveHeartDiseaseDiagnosisAndHFProgComposition");
	  hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.getSearches().put("hypertensiveHeartDiseaseDiagnosis", new Mapped<CohortDefinition>(hypertensiveHeartDiseaseDiagnosis, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgComposition.setCompositionString("(patientsInHFProgram AND hypertensiveHeartDiseaseDiagnosis");
		 h.replaceCohortDefinition(hypertensiveHeartDiseaseDiagnosisAndHFProgComposition);

		 CohortIndicator hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator = new CohortIndicator();
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator.setName("hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator");
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator.setCohortDefinition(new Mapped<CohortDefinition>(hypertensiveHeartDiseaseDiagnosisAndHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
	    h.replaceDefinition(hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator);
	     
		  
// ===============================================================================                 
//  2.8.  PATIENTS WHO HAVE HAD A PERICARDIAL DISEASE
// ===============================================================================   				    

	  
	    
	 CodedObsCohortDefinition pericardialDiseaseDiagnosis =makeCodedObsCohortDefinition("pericardialDiseaseDiagnosis", HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.PERICARDIAL_DISEASE, SetComparator.IN, TimeModifier.ANY);	
	 pericardialDiseaseDiagnosis.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 h.replaceCohortDefinition(pericardialDiseaseDiagnosis);

    CompositionCohortDefinition pericardialDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
    pericardialDiseaseDiagnosisAndHFProgComposition.setName("pericardialDiseaseDiagnosisAndHFProgComposition");
    pericardialDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    pericardialDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    pericardialDiseaseDiagnosisAndHFProgComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    pericardialDiseaseDiagnosisAndHFProgComposition.getSearches().put("pericardialDiseaseDiagnosis", new Mapped<CohortDefinition>(pericardialDiseaseDiagnosis, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    pericardialDiseaseDiagnosisAndHFProgComposition.setCompositionString("(patientsInHFProgram AND pericardialDiseaseDiagnosis");
	h.replaceCohortDefinition(pericardialDiseaseDiagnosisAndHFProgComposition);

	CohortIndicator pericardialDiseaseDiagnosisAndHFProgIndicator = new CohortIndicator();
	pericardialDiseaseDiagnosisAndHFProgIndicator.setName("pericardialDiseaseDiagnosisAndHFProgIndicator");
	pericardialDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	pericardialDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	pericardialDiseaseDiagnosisAndHFProgIndicator.setCohortDefinition(new Mapped<CohortDefinition>(pericardialDiseaseDiagnosisAndHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
    h.replaceDefinition(pericardialDiseaseDiagnosisAndHFProgIndicator);
     			 
// ===============================================================================                 
//         2.9. PATIENTS WHO HAVE HAD A CONGENITAL HEART DISEASE
// ===============================================================================  

	 CodedObsCohortDefinition congenitalDiseaseDiagnosis =makeCodedObsCohortDefinition("congenitalDiseaseDiagnosis",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.CONGENITAL_HEART_DISEASE, SetComparator.IN, TimeModifier.ANY);
	 congenitalDiseaseDiagnosis.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 h.replaceCohortDefinition(congenitalDiseaseDiagnosis);
		  
	CompositionCohortDefinition congenitalDiseaseDiagnosisAndHFProgComposition = new CompositionCohortDefinition();
	congenitalDiseaseDiagnosisAndHFProgComposition.setName("congenitalDiseaseDiagnosisAndHFProgComposition");
	congenitalDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	congenitalDiseaseDiagnosisAndHFProgComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	congenitalDiseaseDiagnosisAndHFProgComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	congenitalDiseaseDiagnosisAndHFProgComposition.getSearches().put("congenitalDiseaseDiagnosis", new Mapped<CohortDefinition>(congenitalDiseaseDiagnosis, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	congenitalDiseaseDiagnosisAndHFProgComposition.setCompositionString("(patientsInHFProgram AND congenitalDiseaseDiagnosis");
	h.replaceCohortDefinition(congenitalDiseaseDiagnosisAndHFProgComposition);

	 CohortIndicator congenitalDiseaseDiagnosisAndHFProgIndicator = new CohortIndicator();
	 congenitalDiseaseDiagnosisAndHFProgIndicator.setName("congenitalDiseaseDiagnosisAndHFProgIndicator");
	 congenitalDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	 congenitalDiseaseDiagnosisAndHFProgIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	 congenitalDiseaseDiagnosisAndHFProgIndicator.setCohortDefinition(new Mapped<CohortDefinition>(congenitalDiseaseDiagnosisAndHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
	 h.replaceDefinition(congenitalDiseaseDiagnosisAndHFProgIndicator);
   	 
// ========================================================================================                        
//   2.10. Parcent with creatinine > 200
// ========================================================================================  
	 
	 
	NumericObsCohortDefinition patientsWithCreatinine=makeNumericObsCohortDefinition("patientsWithCreatinine", HeartFailureReportConstants.SERUM_CREATININE, 200.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
	patientsWithCreatinine.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    h.replaceCohortDefinition(patientsWithCreatinine);
    
	CompositionCohortDefinition hfpatientsWithCreatinineComposition = new CompositionCohortDefinition();
	hfpatientsWithCreatinineComposition.setName("hfpatientsWithCreatinineComposition");
	hfpatientsWithCreatinineComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	hfpatientsWithCreatinineComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	hfpatientsWithCreatinineComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	hfpatientsWithCreatinineComposition.getSearches().put("patientsWithCreatinine", new Mapped<CohortDefinition>(patientsWithCreatinine,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	hfpatientsWithCreatinineComposition.setCompositionString("patientsInHFProgram AND patientsWithCreatinine");
	h.replaceCohortDefinition(hfpatientsWithCreatinineComposition);
      
       
    CohortIndicator hfpatientsWithCreatininePercentIndicator = CohortIndicator.newFractionIndicator("hfpatientsWithCreatininePercentIndicator", new Mapped<CohortDefinition>(hfpatientsWithCreatinineComposition , 
       		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
	              ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
    hfpatientsWithCreatininePercentIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
    hfpatientsWithCreatininePercentIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
    h.replaceDefinition(hfpatientsWithCreatininePercentIndicator);
    
    
//======================================================================================================                       
//   2.11. number post-cardiac surgery
//====================================================================================================== 
   
    PatientStateCohortDefinition postCardiacSurgeryCohortDefinition=makePatientStateCohortDefinition("postCardiacSurgeryCohortDefinition", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID, HeartFailureReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_UUID, HeartFailureReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_POST_OPERATIVE_STATE_UUID);
    postCardiacSurgeryCohortDefinition.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
    postCardiacSurgeryCohortDefinition.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
    h.replaceCohortDefinition(postCardiacSurgeryCohortDefinition);
    
   /* PatientStateCohortDefinition completedPostCardiacSurgeryBeforePeriod=MakeCohortDefinitionUtil.makePatientStateCohortDefinition("Patients who completed POST CARDIAC SURGERY STATE before the period", CardiologyReportConstants.HEART_FAILURE_PROGRAM_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_POST_OPERATIVE_STATE_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_CONCEPT_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_CONCEPT_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_POST_OPERATIVE_STATE_CONCEPT_UUID);
    completedPostCardiacSurgeryBeforePeriod.addParameter(new Parameter("endedOnOrBefore", "endedOnOrBefore", Date.class));
   
    PatientStateCohortDefinition startedPostCardiacSurgeryAfterPeriod=MakeCohortDefinitionUtil.makePatientStateCohortDefinition("Patients who enrolled in POST CARDIAC SURGERY STATE after the period", CardiologyReportConstants.HEART_FAILURE_PROGRAM_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_POST_OPERATIVE_STATE_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_CONCEPT_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_CONCEPT_UUID, CardiologyReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_POST_OPERATIVE_STATE_CONCEPT_UUID);
    startedPostCardiacSurgeryAfterPeriod.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
*/
   /* CompositionCohortDefinition postCardiacCompositionCohort = new CompositionCohortDefinition();
    postCardiacCompositionCohort.setName("Total number of patient who have the state POST CARDIAC SURGERY Composition cohort definition during the period");
    postCardiacCompositionCohort.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
    postCardiacCompositionCohort.addParameter(new Parameter("endedOnOrBefore", "endedOnOrBefore", Date.class));
    postCardiacCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    postCardiacCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    postCardiacCompositionCohort.getSearches().put("startedPostCardiacSurgeryAfterPeriod", new Mapped<CohortDefinition>(startedPostCardiacSurgeryAfterPeriod, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter}")));
    postCardiacCompositionCohort.getSearches().put("completedPostCardiacSurgeryBeforePeriod", new Mapped<CohortDefinition>(completedPostCardiacSurgeryBeforePeriod, ParameterizableUtil.createParameterMappings("endedOnOrBefore=${endedOnOrBefore}")));
    postCardiacCompositionCohort.getSearches().put("postCardiacSurgery", new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinition, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    postCardiacCompositionCohort.setCompositionString("postCardiacSurgery AND (NOT startedPostCardiacSurgeryAfterPeriod) AND (NOT completedPostCardiacSurgeryBeforePeriod)");
*/
  
    CohortIndicator postCardiacSurgeryCohortIndicator = new CohortIndicator();
    postCardiacSurgeryCohortIndicator.setName("postCardiacSurgeryCohortIndicator");
    postCardiacSurgeryCohortIndicator.addParameter(new Parameter("startDate", "startDate",Date.class));
    postCardiacSurgeryCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
    postCardiacSurgeryCohortIndicator.setCohortDefinition(new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinition,ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}")));
    h.replaceDefinition(postCardiacSurgeryCohortIndicator);
 

//================================================                        
//   3.4. in the subgroup (female < age 50), percent not on family planning
// ================================================  
  
    NumericObsCohortDefinition patientsInFamilyPlanning =makeNumericObsCohortDefinition("patientsInFamilyPlanning",HeartFailureReportConstants.PATIENT_USING_FAMILY_PLANNING,1.0, RangeComparator.EQUAL, TimeModifier.LAST);       
	patientsInFamilyPlanning.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(patientsInFamilyPlanning);
	
	AgeCohortDefinition lessThanFifty = new AgeCohortDefinition(null, 50, null); 
	 lessThanFifty.setName("lessThanFifty");
	 h.replaceCohortDefinition(lessThanFifty);
	 
	 CompositionCohortDefinition patientsInHFWithoutFamilyPlanningCompositionCohort = new CompositionCohortDefinition();
	 patientsInHFWithoutFamilyPlanningCompositionCohort.setName("patientsInHFWithoutFamilyPlanningCompositionCohort");
	 patientsInHFWithoutFamilyPlanningCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	 patientsInHFWithoutFamilyPlanningCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	 patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put("patientsInFamilyPlanning", new Mapped<CohortDefinition>(patientsInFamilyPlanning,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	 patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put("femalesDefinition", new Mapped<CohortDefinition>(femalesDefinition,null));
	 patientsInHFWithoutFamilyPlanningCompositionCohort.getSearches().put("lessThanFifty", new Mapped<CohortDefinition>(lessThanFifty,null));
	 patientsInHFWithoutFamilyPlanningCompositionCohort.setCompositionString("patientsInHFProgram AND femalesDefinition AND lessThanFifty AND (NOT patientsInFamilyPlanning)");
	 h.replaceCohortDefinition(patientsInHFWithoutFamilyPlanningCompositionCohort);
	 
	CohortIndicator patientsInHFWithoutFamilyPlanningIndicator = CohortIndicator.newFractionIndicator("patientsInHFWithoutFamilyPlanningIndicator", new Mapped<CohortDefinition>(patientsInHFWithoutFamilyPlanningCompositionCohort , 
	  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
	  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
	patientsInHFWithoutFamilyPlanningIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	patientsInHFWithoutFamilyPlanningIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	h.replaceDefinition(patientsInHFWithoutFamilyPlanningIndicator);
	
//================================================                        
//   Different CARDIAC Drugs order
// ================================================   

   List<Drug> onFurosemideRegimen =getDrugs(HeartFailureReportConstants.FUROSEMIDE); // 99
   List<Drug> onAtenololRegimen =getDrugs(HeartFailureReportConstants.ATENOLOL);      // 3186
   List<Drug> onCarvedilolRegimen =getDrugs(HeartFailureReportConstants.CARVEDILOL); // 3185
   List<Drug> onAldactoneRegimen=getDrugs(HeartFailureReportConstants.ALDACTONE); // 3184
   List<Drug> onLisinoprilRegimen=getDrugs(HeartFailureReportConstants.LISINOPRIL); // 3183
   List<Drug> onCaptoprilRegimen=getDrugs(HeartFailureReportConstants.CAPTOPRIL); // 3185
   List<Drug> onWarfarinRegimen=getDrugs(HeartFailureReportConstants.WARFARIN); // 3185
   List<Drug> onPenicillinRegimen=getDrugs(HeartFailureReportConstants.PENICILLIN);   // 784	       

// ================================================                        
//  3.5. Patients LASIX COHORT
// ================================================  
   DrugsActiveCohortDefinition onLasixAtEndOfPeriod = new DrugsActiveCohortDefinition();          
   onLasixAtEndOfPeriod.setName("onLasixAtEndOfPeriod");
   onLasixAtEndOfPeriod.setDrugs(onFurosemideRegimen);
   onLasixAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
   h.replaceCohortDefinition(onLasixAtEndOfPeriod);

   CompositionCohortDefinition hFonLasixAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
   hFonLasixAtEndOfPeriodCompositionCohort.setName("hFonLasixAtEndOfPeriodCompositionCohort");
   hFonLasixAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
   hFonLasixAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
   hFonLasixAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
   hFonLasixAtEndOfPeriodCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
   hFonLasixAtEndOfPeriodCompositionCohort.getSearches().put("onLasixAtEndOfPeriod", new Mapped<CohortDefinition>(onLasixAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
   hFonLasixAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onLasixAtEndOfPeriod");
   h.replaceCohortDefinition(hFonLasixAtEndOfPeriodCompositionCohort);
   
   CohortIndicator onLasixAtEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onLasixAtEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFonLasixAtEndOfPeriodCompositionCohort , 
     		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
	              ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
    onLasixAtEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
    onLasixAtEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
    h.replaceDefinition(onLasixAtEndOfPeriodCohortIndicator);


// ================================================                        
//  3.6. Patients on Atenolol COHORT
// ================================================ 
    
DrugsActiveCohortDefinition onAtenololAtEndOfPeriod = new DrugsActiveCohortDefinition();          
onAtenololAtEndOfPeriod.setName("onAtenololAtEndOfPeriod");
onAtenololAtEndOfPeriod.setDrugs(onAtenololRegimen);
onAtenololAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onAtenololAtEndOfPeriod);

CompositionCohortDefinition hFonAtenololAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
hFonAtenololAtEndOfPeriodCompositionCohort.setName("hFonAtenololAtEndOfPeriodCompositionCohort");
hFonAtenololAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonAtenololAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonAtenololAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonAtenololAtEndOfPeriodCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonAtenololAtEndOfPeriodCompositionCohort.getSearches().put("onAtenololAtEndOfPeriod", new Mapped<CohortDefinition>(onAtenololAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonAtenololAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onAtenololAtEndOfPeriod");
h.replaceCohortDefinition(hFonAtenololAtEndOfPeriodCompositionCohort);

CohortIndicator onAtenololAtEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onAtenololAtEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFonAtenololAtEndOfPeriodCompositionCohort , 
  		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
             ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onAtenololAtEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onAtenololAtEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onAtenololAtEndOfPeriodCohortIndicator);

// ================================================                        
//  3.7. Patients Carvedilol COHORT
// ================================================   

DrugsActiveCohortDefinition onCarvedilolAtEndOfPeriod = new DrugsActiveCohortDefinition();          
onCarvedilolAtEndOfPeriod.setName("onCarvedilolAtEndOfPeriod");
onCarvedilolAtEndOfPeriod.setDrugs(onCarvedilolRegimen);
onCarvedilolAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onCarvedilolAtEndOfPeriod);

CompositionCohortDefinition hFonCarvedilolAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
hFonCarvedilolAtEndOfPeriodCompositionCohort.setName("hFonCarvedilolAtEndOfPeriodCompositionCohort");
hFonCarvedilolAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonCarvedilolAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonCarvedilolAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonCarvedilolAtEndOfPeriodCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonCarvedilolAtEndOfPeriodCompositionCohort.getSearches().put("onCarvedilolAtEndOfPeriod", new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonCarvedilolAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onCarvedilolAtEndOfPeriod");
h.replaceCohortDefinition(hFonCarvedilolAtEndOfPeriodCompositionCohort);

CohortIndicator onCarvedilolAtEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onCarvedilolAtEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFonCarvedilolAtEndOfPeriodCompositionCohort , 
  		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
             ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onCarvedilolAtEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onCarvedilolAtEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onCarvedilolAtEndOfPeriodCohortIndicator);
           
// ================================================                        
//  3.8. Patients Aldactone COHORT
// ================================================   


DrugsActiveCohortDefinition onAldactoneAtEndOfPeriod = new DrugsActiveCohortDefinition();          
onAldactoneAtEndOfPeriod.setName("onAldactoneAtEndOfPeriod");
onAldactoneAtEndOfPeriod.setDrugs(onAldactoneRegimen);
onAldactoneAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onAldactoneAtEndOfPeriod);

CompositionCohortDefinition hFonAldactoneAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
hFonAldactoneAtEndOfPeriodCompositionCohort.setName("hFonAldactoneAtEndOfPeriodCompositionCohort");
hFonAldactoneAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonAldactoneAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonAldactoneAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonAldactoneAtEndOfPeriodCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonAldactoneAtEndOfPeriodCompositionCohort.getSearches().put("onAldactoneAtEndOfPeriod", new Mapped<CohortDefinition>(onAldactoneAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonAldactoneAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onAldactoneAtEndOfPeriod");
h.replaceCohortDefinition(hFonAldactoneAtEndOfPeriodCompositionCohort);

CohortIndicator onAldactoneAtEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onAldactoneAtEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFonAldactoneAtEndOfPeriodCompositionCohort , 
  		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
             ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onAldactoneAtEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onAldactoneAtEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onAldactoneAtEndOfPeriodCohortIndicator);               
                      
  
                      
//================================================                        
//  3.9. Patients Lisinopril or Captopril COHORT
//================================================   	                                                

                                        
 /*Lisinopril*/
  DrugsActiveCohortDefinition onLisinoprilAtEndOfPeriod = new DrugsActiveCohortDefinition();          
  onLisinoprilAtEndOfPeriod.setName("onLisinoprilAtEndOfPeriod");
  onLisinoprilAtEndOfPeriod.setDrugs(onLisinoprilRegimen);
  onLisinoprilAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
  h.replaceCohortDefinition(onLisinoprilAtEndOfPeriod);
 
                               
/* Captopril*/
    DrugsActiveCohortDefinition onCaptoprilAtEndOfPeriod = new DrugsActiveCohortDefinition();          
    onCaptoprilAtEndOfPeriod.setName("onCaptoprilAtEndOfPeriod");
    onCaptoprilAtEndOfPeriod.setDrugs(onCaptoprilRegimen);
    onCaptoprilAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
    h.replaceCohortDefinition(onCaptoprilAtEndOfPeriod);
     
CompositionCohortDefinition hFLisinoprilOrCaptoprilCompositionCohort = new CompositionCohortDefinition();
hFLisinoprilOrCaptoprilCompositionCohort.setName("hFLisinoprilOrCaptoprilCompositionCohort");
hFLisinoprilOrCaptoprilCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFLisinoprilOrCaptoprilCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFLisinoprilOrCaptoprilCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFLisinoprilOrCaptoprilCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFLisinoprilOrCaptoprilCompositionCohort.getSearches().put("onLisinoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onLisinoprilAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilOrCaptoprilCompositionCohort.getSearches().put("onCaptoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onCaptoprilAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilOrCaptoprilCompositionCohort.setCompositionString("patientsInHFProgram AND (onLisinoprilAtEndOfPeriod OR onCaptoprilAtEndOfPeriod)");
h.replaceCohortDefinition(hFLisinoprilOrCaptoprilCompositionCohort);

CohortIndicator onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFLisinoprilOrCaptoprilCompositionCohort , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator);
                        
                     
                             
//================================================                        
//  3.10. Patients Atenolol and Carvedilol COHORT
//================================================   	                                                


CompositionCohortDefinition hFAtenololAndCarvedilolCompositionCohort = new CompositionCohortDefinition();
hFAtenololAndCarvedilolCompositionCohort.setName("hFAtenololAndCarvedilolCompositionCohort");
hFAtenololAndCarvedilolCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFAtenololAndCarvedilolCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFAtenololAndCarvedilolCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFAtenololAndCarvedilolCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFAtenololAndCarvedilolCompositionCohort.getSearches().put("onAtenololAtEndOfPeriod", new Mapped<CohortDefinition>(onAtenololAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFAtenololAndCarvedilolCompositionCohort.getSearches().put("onCarvedilolAtEndOfPeriod", new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFAtenololAndCarvedilolCompositionCohort.setCompositionString("patientsInHFProgram AND (onAtenololAtEndOfPeriod AND onCarvedilolAtEndOfPeriod)");
h.replaceCohortDefinition(hFAtenololAndCarvedilolCompositionCohort);

CohortIndicator onAtenololAndCarvedilolEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onAtenololAndCarvedilolEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFAtenololAndCarvedilolCompositionCohort , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onAtenololAndCarvedilolEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onAtenololAndCarvedilolEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onAtenololAndCarvedilolEndOfPeriodCohortIndicator);
     
          

//================================================                        
//  3.11. Patients Lisinopril and Captopril COHORT
//================================================   	                                                


CompositionCohortDefinition hFLisinoprilAndCaptoprilCompositionCohort = new CompositionCohortDefinition();
hFLisinoprilAndCaptoprilCompositionCohort.setName("hFLisinoprilAndCaptoprilCompositionCohort");
hFLisinoprilAndCaptoprilCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFLisinoprilAndCaptoprilCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFLisinoprilAndCaptoprilCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFLisinoprilAndCaptoprilCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFLisinoprilAndCaptoprilCompositionCohort.getSearches().put("onLisinoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onLisinoprilAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilAndCaptoprilCompositionCohort.getSearches().put("onCaptoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onCaptoprilAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilAndCaptoprilCompositionCohort.setCompositionString("patientsInHFProgram AND (onLisinoprilAtEndOfPeriod AND onCaptoprilAtEndOfPeriod)");
h.replaceCohortDefinition(hFLisinoprilAndCaptoprilCompositionCohort);

CohortIndicator onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFLisinoprilAndCaptoprilCompositionCohort , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator);
     
//================================================                        
//3.12. Patients on wafarin COHORT
//================================================ 


DrugsActiveCohortDefinition onWarfarinAtEndOfPeriod = new DrugsActiveCohortDefinition();          
onWarfarinAtEndOfPeriod.setName("onWarfarinAtEndOfPeriod");
onWarfarinAtEndOfPeriod.setDrugs(onWarfarinRegimen);
onWarfarinAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onWarfarinAtEndOfPeriod);

CompositionCohortDefinition hFonWarfarinAtEndOfPeriodCompositionCohort = new CompositionCohortDefinition();
hFonWarfarinAtEndOfPeriodCompositionCohort.setName("hFonWarfarinAtEndOfPeriodCompositionCohort");
hFonWarfarinAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonWarfarinAtEndOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonWarfarinAtEndOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonWarfarinAtEndOfPeriodCompositionCohort.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonWarfarinAtEndOfPeriodCompositionCohort.getSearches().put("onWarfarinAtEndOfPeriod", new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonWarfarinAtEndOfPeriodCompositionCohort.setCompositionString("patientsInHFProgram AND onWarfarinAtEndOfPeriod");
h.replaceCohortDefinition(hFonWarfarinAtEndOfPeriodCompositionCohort);

CohortIndicator onWarfarinAtEndOfPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onWarfarinAtEndOfPeriodCohortIndicator", new Mapped<CohortDefinition>(hFonWarfarinAtEndOfPeriodCompositionCohort , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram, 
         ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onWarfarinAtEndOfPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onWarfarinAtEndOfPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onWarfarinAtEndOfPeriodCohortIndicator);


// ===================================================================================                       
//   3.1. in the subgroup of cardiomyopathy percent with heart rate >60 not  carvedilol
// =================================================================================== 


 NumericObsCohortDefinition heartRateDiseaseDuringPeriod=makeNumericObsCohortDefinition("heartRateDiseaseDuringPeriod", HeartFailureReportConstants.PULSE, 60.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
 h.replaceCohortDefinition(heartRateDiseaseDuringPeriod);
 
CompositionCohortDefinition hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort = new CompositionCohortDefinition();
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.setName("hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort");
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.getSearches().put("cardiomyopathyDiognosisAnsHFProgComposition", new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));  
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.getSearches().put("heartRateDiseaseDuringPeriod", new Mapped<CohortDefinition>(heartRateDiseaseDuringPeriod,null));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.getSearches().put("onCarvedilolAtEndOfPeriod", new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort.setCompositionString("cardiomyopathyDiognosisAnsHFProgComposition AND (heartRateDiseaseDuringPeriod AND (NOT onCarvedilolAtEndOfPeriod))");
h.replaceCohortDefinition(hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort);

CohortIndicator onCardiomyopathyHeartRatePeriodCohortIndicator = CohortIndicator.newFractionIndicator("onCardiomyopathyHeartRatePeriodCohortIndicator", new Mapped<CohortDefinition>(hFCardiomyopathyWithHeartRateOfPeriodCompositionCohort , 
	   ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgComposition, 
     ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onCardiomyopathyHeartRatePeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onCardiomyopathyHeartRatePeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onCardiomyopathyHeartRatePeriodCohortIndicator);


//===================================================================================                       
//   3.2. in the subgroup of mitral stenosis percent with heart rate >60 not Atenolol
// ===================================================================================  

 CompositionCohortDefinition hFMitralStenosisWithHeartRateOfPeriodCompositionCohort = new CompositionCohortDefinition();
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.setName("hFMitralStenosisWithHeartRateOfPeriodCompositionCohort");
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.getSearches().put("mitralStenosisDiagnosisAnsHFProgComposition", new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));  
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.getSearches().put("heartRateDiseaseDuringPeriod", new Mapped<CohortDefinition>(heartRateDiseaseDuringPeriod,null));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.getSearches().put("onAtenololAtEndOfPeriod", new Mapped<CohortDefinition>(onAtenololAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohort.setCompositionString("mitralStenosisDiagnosisAnsHFProgComposition AND  (heartRateDiseaseDuringPeriod AND (NOT onAtenololAtEndOfPeriod))");
 h.replaceCohortDefinition(hFMitralStenosisWithHeartRateOfPeriodCompositionCohort);
 
CohortIndicator onMitralStenosisHeartRatePeriodCohortIndicator = CohortIndicator.newFractionIndicator("onMitralStenosisHeartRatePeriodCohortIndicator", new Mapped<CohortDefinition>(hFMitralStenosisWithHeartRateOfPeriodCompositionCohort , 
	   ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgComposition, 
        ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onMitralStenosisHeartRatePeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onMitralStenosisHeartRatePeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onMitralStenosisHeartRatePeriodCohortIndicator);

// ===================================================================================                       
//   3.3. in the subgroup of rheumatic heart disease percent not penicillin
// ===================================================================================  


DrugsActiveCohortDefinition onPenicillinAtEndOfPeriod = new DrugsActiveCohortDefinition();          
onPenicillinAtEndOfPeriod.setName("onPenicillinAtEndOfPeriod");
onPenicillinAtEndOfPeriod.setDrugs(onPenicillinRegimen);
onPenicillinAtEndOfPeriod.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onPenicillinAtEndOfPeriod);

CompositionCohortDefinition hFRheumaticHeartDiseaseOfPeriodCompositionCohort = new CompositionCohortDefinition();
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.setName("hFRheumaticHeartDiseaseOfPeriodCompositionCohort");
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.getSearches().put("rheumaticHeartDiseaseDiagnosisAndHFProgComposition", new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.getSearches().put("onPenicillinAtEndOfPeriod", new Mapped<CohortDefinition>(onPenicillinAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFRheumaticHeartDiseaseOfPeriodCompositionCohort.setCompositionString("rheumaticHeartDiseaseDiagnosisAndHFProgComposition AND (NOT onPenicillinAtEndOfPeriod)");
h.replaceCohortDefinition(hFRheumaticHeartDiseaseOfPeriodCompositionCohort);

CohortIndicator onRheumaticNotOnPenicillinPeriodCohortIndicator = CohortIndicator.newFractionIndicator("onRheumaticNotOnPenicillinPeriodCohortIndicator", new Mapped<CohortDefinition>(hFRheumaticHeartDiseaseOfPeriodCompositionCohort , 
           ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgComposition, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onRheumaticNotOnPenicillinPeriodCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
onRheumaticNotOnPenicillinPeriodCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onRheumaticNotOnPenicillinPeriodCohortIndicator);


// ====================================================================            
//  4.1. Percent and number of patients not seen in the last 6 months
// ====================================================================
List<EncounterType> encounterTypes=new  ArrayList<EncounterType>();
String[] encounterTypeIds= (Context.getAdministrationService().getGlobalProperty("cardiologyreporting.cardilogyEncounterTypes")).split(",");
if(encounterTypeIds!=null){
	EncounterType encType=null;
	for(String strId:encounterTypeIds){
		encType=Context.getEncounterService().getEncounterType(Integer.parseInt(strId));
		if(encType!=null)
		encounterTypes.add(encType);
	}
}
/*
List<Form> formList = new ArrayList<Form>();
String eTGP = Context.getAdministrationService().getGlobalProperty("cardiologyreporting.cardilogyEncounterTypes");
String[] splitedETGP=eTGP.split(",");
EncounterType et=null;
List<Form> allForms=Context.getFormService().getAllForms();
if(allForms!=null){
for(String str:splitedETGP){
et=Context.getEncounterService().getEncounterType(Integer.valueOf(str)); 
for (Form form :allForms ){
if (form.getEncounterType() != null && form.getEncounterType().equals(et)){
formList.add(form);      	  
}
}
}
}*/
EncounterCohortDefinition hFencounterDuringPeriod =makeEncounterCohortDefinition(encounterTypes); 
hFencounterDuringPeriod.setName("encounterFormDuringPeriod");
hFencounterDuringPeriod.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFencounterDuringPeriod.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(hFencounterDuringPeriod);

CompositionCohortDefinition patientNotSeenDuringPeriodComposition = new CompositionCohortDefinition();
patientNotSeenDuringPeriodComposition.setName("patientNotSeenDuringPeriodComposition");
patientNotSeenDuringPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));                       
patientNotSeenDuringPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
patientNotSeenDuringPeriodComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
patientNotSeenDuringPeriodComposition.getSearches().put("hFencounterDuringPeriod", new Mapped<CohortDefinition>(hFencounterDuringPeriod, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
patientNotSeenDuringPeriodComposition.setCompositionString("patientsInHFProgram AND (NOT hFencounterDuringPeriod)");
h.replaceCohortDefinition(patientNotSeenDuringPeriodComposition);

CohortIndicator percentageOfpatientNotSeenInLastSixMonthPeriodIndicator = CohortIndicator.newFractionIndicator("percentageOfpatientNotSeenInLastSixMonthPeriodIndicator", new Mapped<CohortDefinition>(patientNotSeenDuringPeriodComposition,
        ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram,
        ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);      
 percentageOfpatientNotSeenInLastSixMonthPeriodIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
 percentageOfpatientNotSeenInLastSixMonthPeriodIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
 h.replaceDefinition(percentageOfpatientNotSeenInLastSixMonthPeriodIndicator);


 // ====================================================================            
 //  4.2. Percent and number of patients not seen in the last 3 months
 // ====================================================================

 CohortIndicator percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator = CohortIndicator.newFractionIndicator("percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator", new Mapped<CohortDefinition>(patientNotSeenDuringPeriodComposition,
	        ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram,
	        ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);      
 percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
 percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	 h.replaceDefinition(percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator);


//======================================================================	
//	      4.3.    number of patients without an accompagnateur
//======================================================================
	
 SqlCohortDefinition allPatientsWhitAccompagnateur = new SqlCohortDefinition("SELECT DISTINCT person_b FROM relationship WHERE relationship='1' and date_created<= :endDate and voided=0");
 allPatientsWhitAccompagnateur.setName("allPatientsWhitAccompagnateur");
 allPatientsWhitAccompagnateur.addParameter(new Parameter("endDate","endDate",Date.class));  
 h.replaceCohortDefinition(allPatientsWhitAccompagnateur);
 
 CompositionCohortDefinition patientWithoutAccompagnateurPeriodComposition = new CompositionCohortDefinition();
 patientWithoutAccompagnateurPeriodComposition.setName("patientWithoutAccompagnateurPeriodComposition");
 patientWithoutAccompagnateurPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 patientWithoutAccompagnateurPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 patientWithoutAccompagnateurPeriodComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
 patientWithoutAccompagnateurPeriodComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
 patientWithoutAccompagnateurPeriodComposition.getSearches().put("allPatientsWhitAccompagnateur", new Mapped<CohortDefinition>(allPatientsWhitAccompagnateur, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
 patientWithoutAccompagnateurPeriodComposition.setCompositionString("(patientsInHFProgram AND (NOT allPatientsWhitAccompagnateur)");        
 h.replaceCohortDefinition(patientWithoutAccompagnateurPeriodComposition);

 CohortIndicator percentPatientWithoutAccompagnateurIndicator = CohortIndicator.newFractionIndicator("percentPatientWithoutAccompagnateurIndicator", new Mapped<CohortDefinition>(patientWithoutAccompagnateurPeriodComposition,
         ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},endDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgram,
         ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
 percentPatientWithoutAccompagnateurIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
 percentPatientWithoutAccompagnateurIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
 h.replaceDefinition(percentPatientWithoutAccompagnateurIndicator);

//==================================================
//      5.1. PATIENTS WHO DIED DURING PERIOD 
// ===================================================              

CodedObsCohortDefinition diedDuringPeriod =makeCodedObsCohortDefinition("diedDuringPeriod",HeartFailureReportConstants.REASON_FOR_EXITING_CARE, HeartFailureReportConstants.PATIENT_DIED, SetComparator.IN, TimeModifier.LAST);                
diedDuringPeriod.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
diedDuringPeriod.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(diedDuringPeriod);

CompositionCohortDefinition diedDuringPeriodComposition = new CompositionCohortDefinition();
diedDuringPeriodComposition.setName("diedDuringPeriodComposition");
diedDuringPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
diedDuringPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
diedDuringPeriodComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
diedDuringPeriodComposition.getSearches().put("diedDuringPeriod", new Mapped<CohortDefinition>(diedDuringPeriod, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
diedDuringPeriodComposition.setCompositionString("(patientsInHFProgram AND diedDuringPeriod");

CohortIndicator diedDuringPeriodIndicator = new CohortIndicator();
diedDuringPeriodIndicator.setName("diedDuringPeriodIndicator");
diedDuringPeriodIndicator.addParameter(new Parameter("startDate", "startDate",Date.class));
diedDuringPeriodIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
diedDuringPeriodIndicator.setCohortDefinition(new Mapped<CohortDefinition>(diedDuringPeriodComposition, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
h.replaceDefinition(diedDuringPeriodIndicator);
    


// ===============================================================================                        
//   5.2.  Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR < 2
// ===============================================================================  


NumericObsCohortDefinition INRLTTwoCohortDefinition=makeNumericObsCohortDefinition("INRLTTwoCohortDefinition", HeartFailureReportConstants.INTERNATIONAL_NORMALIZED_RATIO,2.0, RangeComparator.LESS_THAN, TimeModifier.LAST);
INRLTTwoCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRLTTwoCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(INRLTTwoCohortDefinition);

CompositionCohortDefinition INRALTTwondPostCardiacSugeryCompositionCohortDefinition = new CompositionCohortDefinition();
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.setName("INRALTTwondPostCardiacSugeryCompositionCohortDefinition");
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));          		
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.getSearches().put("onWarfarinAtEndOfPeriod", new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.getSearches().put("postCardiacSurgeryCohortDefinition", new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinition, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.getSearches().put("INRLTTwoCohortDefinition", new Mapped<CohortDefinition>(INRLTTwoCohortDefinition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
INRALTTwondPostCardiacSugeryCompositionCohortDefinition.setCompositionString("INRLTTwoCohortDefinition AND postCardiacSurgeryCohortDefinition AND onWarfarinAtEndOfPeriod");
h.replaceCohortDefinition(INRALTTwondPostCardiacSugeryCompositionCohortDefinition);

CohortIndicator percentINRALTTwoPostCardiacSugeryCohortIndicator = CohortIndicator.newFractionIndicator("percentINRALTTwoPostCardiacSugeryCohortIndicator", new Mapped<CohortDefinition>(INRALTTwondPostCardiacSugeryCompositionCohortDefinition,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinition,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}")), null);       
percentINRALTTwoPostCardiacSugeryCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
percentINRALTTwoPostCardiacSugeryCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(percentINRALTTwoPostCardiacSugeryCohortIndicator);

// ===============================================================================                        
//   5.3. Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR > 4
// ===============================================================================  

NumericObsCohortDefinition INRGTFourCohortDefinition=makeNumericObsCohortDefinition("INRGTFourCohortDefinition", HeartFailureReportConstants.INTERNATIONAL_NORMALIZED_RATIO,4.0, RangeComparator.GREATER_EQUAL, TimeModifier.LAST);
INRGTFourCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRGTFourCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(INRGTFourCohortDefinition);

CompositionCohortDefinition INRGTFourAndPostCardiacSugeryCompositionCohortDefinition = new CompositionCohortDefinition();
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.setName("INRGTFourAndPostCardiacSugeryCompositionCohortDefinition");
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));          		
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.getSearches().put("onWarfarinAtEndOfPeriod", new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriod,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.getSearches().put("postCardiacSurgeryCohortDefinition", new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinition, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.getSearches().put("INRGTFourCohortDefinition", new Mapped<CohortDefinition>(INRGTFourCohortDefinition, 
		ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinition.setCompositionString("INRGTFourCohortDefinition AND postCardiacSurgeryCohortDefinition AND onWarfarinAtEndOfPeriod");
h.replaceCohortDefinition(INRGTFourAndPostCardiacSugeryCompositionCohortDefinition);

CohortIndicator percentINRGTTFourPostCardiacSugeryCohortIndicator = CohortIndicator.newFractionIndicator("percentINRGTTFourPostCardiacSugeryCohortIndicator", new Mapped<CohortDefinition>(INRGTFourAndPostCardiacSugeryCompositionCohortDefinition,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinition,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}")), null);       
percentINRGTTFourPostCardiacSugeryCohortIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
percentINRGTTFourPostCardiacSugeryCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(percentINRGTTFourPostCardiacSugeryCohortIndicator);



//===============================================================================                 
//5.4. PATIENTS WHO HAD A HOSPITALIZATION IN THE PAST MONTH
//===============================================================================                 

 CodedObsCohortDefinition hospitalizedDuringPeriod =makeCodedObsCohortDefinition("hospitalizedDuringPeriod",HeartFailureReportConstants.DISPOSITION, HeartFailureReportConstants.ADMIT_TO_HOSPITAL, SetComparator.IN, TimeModifier.LAST);                
 hospitalizedDuringPeriod.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 hospitalizedDuringPeriod.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 h.replaceCohortDefinition(hospitalizedDuringPeriod);


CompositionCohortDefinition hospitalizedDuringPeriodComposition = new CompositionCohortDefinition();
hospitalizedDuringPeriodComposition.setName("hospitalizedDuringPeriodComposition");
hospitalizedDuringPeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hospitalizedDuringPeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hospitalizedDuringPeriodComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hospitalizedDuringPeriodComposition.getSearches().put("hospitalizedDuringPeriod", new Mapped<CohortDefinition>(hospitalizedDuringPeriod, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hospitalizedDuringPeriodComposition.setCompositionString("(patientsInHFProgram AND hospitalizedDuringPeriod");
h.replaceCohortDefinition(hospitalizedDuringPeriodComposition);

 CohortIndicator hospitalizedDuringPeriodIndicator = new CohortIndicator();
 hospitalizedDuringPeriodIndicator.setName("hospitalizedDuringPeriodIndicator");
 hospitalizedDuringPeriodIndicator.addParameter(new Parameter("startDate", "startDate",Date.class));
 hospitalizedDuringPeriodIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
 hospitalizedDuringPeriodIndicator.setCohortDefinition(new Mapped<CohortDefinition>(hospitalizedDuringPeriodComposition, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
h.replaceDefinition(hospitalizedDuringPeriodIndicator);

//===============================================================================                   
//
//   6.1.  number of patients without a height ever
//
// ===============================================================================  
        NumericObsCohortDefinition heightCohortDefinition=makeNumericObsCohortDefinition("heightCohortDefinition", HeartFailureReportConstants.HEIGHT_CM,0.0, RangeComparator.GREATER_EQUAL, TimeModifier.ANY);
        heightCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        h.replaceCohortDefinition(heightCohortDefinition);
        
        CompositionCohortDefinition heightEverCompositionCohortDefinition = new CompositionCohortDefinition();
        heightEverCompositionCohortDefinition.setName("heightEverCompositionCohortDefinition");
        heightEverCompositionCohortDefinition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        heightEverCompositionCohortDefinition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        heightEverCompositionCohortDefinition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
        heightEverCompositionCohortDefinition.getSearches().put("heightCohortDefinition", new Mapped<CohortDefinition>(heightCohortDefinition, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
        heightEverCompositionCohortDefinition.setCompositionString("(patientsInHFProgram AND (NOT heightCohortDefinition)");
        h.replaceCohortDefinition(heightEverCompositionCohortDefinition);     
        
        CohortIndicator heightEverCohortIndicator = new CohortIndicator();
        heightEverCohortIndicator.setName("heightEverCohortIndicator");
        heightEverCohortIndicator.addParameter(new Parameter("startDate", "startDate",Date.class));
        heightEverCohortIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
        heightEverCohortIndicator.setCohortDefinition(new Mapped<CohortDefinition>(heightEverCompositionCohortDefinition, 
                 ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
        h.replaceDefinition(heightEverCohortIndicator);
                
// ===============================================================================                 
//  37. NUMBER OF  PATIENTS WITHOUT A DONNE DE BASE 
// =============================================================================== 

  List<Form> donnDeBase=new ArrayList<Form>();
  String donnDeBaseFormid=Context.getAdministrationService().getGlobalProperty("cardiologyreporting.hFDonneDeBaseFormId");
  donnDeBase.add(Context.getFormService().getForm(Integer.valueOf(donnDeBaseFormid)));
	  
  EncounterCohortDefinition encounterFormDuringDDBPeriod =makeEncounterCohortDefinition("encounterFormDuringDDBPeriod",donnDeBase);       
  encounterFormDuringDDBPeriod.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
  h.replaceCohortDefinition(encounterFormDuringDDBPeriod);
   
 CompositionCohortDefinition patientWithoutDonneDebasePeriodComposition = new CompositionCohortDefinition();
 patientWithoutDonneDebasePeriodComposition.setName("patientWithoutDonneDebasePeriodComposition");
 patientWithoutDonneDebasePeriodComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));                        
 patientWithoutDonneDebasePeriodComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 patientWithoutDonneDebasePeriodComposition.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgram,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
 patientWithoutDonneDebasePeriodComposition.getSearches().put("encounterFormDuringDDBPeriod", new Mapped<CohortDefinition>(encounterFormDuringDDBPeriod, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
 patientWithoutDonneDebasePeriodComposition.setCompositionString("(patientsInHFProgram AND (NOT encounterFormDuringDDBPeriod)");  
 h.replaceCohortDefinition(patientWithoutDonneDebasePeriodComposition);   	
    	 
 CohortIndicator patientWithoutDonneDebasePeriodIndicator = new CohortIndicator();
 patientWithoutDonneDebasePeriodIndicator.setName("patientWithoutDonneDebasePeriodIndicator");
 patientWithoutDonneDebasePeriodIndicator.addParameter(new Parameter("startDate", "startDate",Date.class));
 patientWithoutDonneDebasePeriodIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
 patientWithoutDonneDebasePeriodIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientWithoutDonneDebasePeriodComposition, 
 ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"))); 
 h.replaceDefinition(patientWithoutDonneDebasePeriodIndicator);

//Add global filters to the report
        
		rd.addIndicator("1.1.m", "% of Male", percentMaleInFHProgramIndicator);
		rd.addIndicator("1.1.f", "% of Female", percentFemaleInFHProgramIndicator);
		rd.addIndicator("1.2", "Median Age", medianAge);
		rd.addIndicator("1.3", "Number of new patients enrolled in reporting period", patientsEnrolledInHFIndicator);
		rd.addIndicator("1.4", "Total number of Patients", patientsInHFIndicator);
		
		rd.addIndicator("2.1", "Number and percent of patients without a cardiology consultation", patientsInHFProgramWithouCardFormIndicator);
		rd.addIndicator("2.2", "Number and percent of patients without a preliminary echocardiographic diagnosis", hfEchocardiographyPercentageIndicator);
		rd.addIndicator("2.3", "Percent without a creatinine in the last 6 months", hfPatientWithoutCreatininePercentIndicator);
		rd.addIndicator("2.4", "Number of patients with Cardiomyopathy", cardiomyopathyDiognosisAnsHFProgIndicator);
		rd.addIndicator("2.5", "Number of patients with pure mitral stenosis", mitralStenosisDiagnosisAnsHFProgIndicator);
		rd.addIndicator("2.6", "Number of patients with other rheumatic heart disease", rheumaticHeartDiseaseDiagnosisAndHFProgIndicator);
		rd.addIndicator("2.7", "Number of patients with hypertensive heart disease", hypertensiveHeartDiseaseDiagnosisAndHFProgIndicator);
		rd.addIndicator("2.8", "Number of patients with pericardial disease", pericardialDiseaseDiagnosisAndHFProgIndicator);
		rd.addIndicator("2.9", "Number of patients with congenital heart disease", congenitalDiseaseDiagnosisAndHFProgIndicator);
		rd.addIndicator("2.10", "Percent of patients with creatinine > 200", hfpatientsWithCreatininePercentIndicator);
		rd.addIndicator("2.11", "Number of patients in post-cardiac surgery", postCardiacSurgeryCohortIndicator);	
	
		rd.addIndicator("3.1", "In the subgroup (cardiomyopathy): percent of Patients with heart rate > 60 at last visit not on carvedilol", onCardiomyopathyHeartRatePeriodCohortIndicator);
		rd.addIndicator("3.2", "in the subgroup (mitral stenosis): percent of Patients with heart rate > 60 at last visit not on atenolol", onMitralStenosisHeartRatePeriodCohortIndicator);
		rd.addIndicator("3.3", "in the subgroup (rheumatic heart disease): percent not on penicillin",onRheumaticNotOnPenicillinPeriodCohortIndicator);		
		rd.addIndicator("3.4", "In the subgroup (female < age 50), percent of patients not on family planning", patientsInHFWithoutFamilyPlanningIndicator);
		rd.addIndicator("3.5", "Percent of Patients on lasix", onLasixAtEndOfPeriodCohortIndicator);
		rd.addIndicator("3.6", "Percent of Patients on atenolol", onAtenololAtEndOfPeriodCohortIndicator);
		rd.addIndicator("3.7", "Percent of Patients on carvedilol", onCarvedilolAtEndOfPeriodCohortIndicator);
		rd.addIndicator("3.8", "Percent of Patients on aldactone", onAldactoneAtEndOfPeriodCohortIndicator);
		rd.addIndicator("3.9", "Percent of Patients on lisinopril or captopril", onLisinoprilOrCaptoprilEndOfPeriodCohortIndicator);
		rd.addIndicator("3.10", "Percent of Patients on atenolol and carvedilol", onAtenololAndCarvedilolEndOfPeriodCohortIndicator);
		rd.addIndicator("3.11", "Percent of Patients on lisinopril and captopril", onLisinoprilAndCaptoprilEndOfPeriodCohortIndicator);
		rd.addIndicator("3.12", "Percent and number of Patients on warfarin", onWarfarinAtEndOfPeriodCohortIndicator);
		
		rd.addIndicator("4.1","Percent and number of patients not seen in the last 6 months",percentageOfpatientNotSeenInLastSixMonthPeriodIndicator);
		rd.addIndicator("4.2","Percent and number of patients not seen in the last 3 months",percentageOfpatientNotSeenInLastThreeMonthPeriodIndicator);
		rd.addIndicator("4.3","Number of patients without an accompagnateur", percentPatientWithoutAccompagnateurIndicator);
		
		rd.addIndicator("5.1", "Number of people who have ever been in the heart failure program who died in report window", diedDuringPeriodIndicator);
		rd.addIndicator("5.2", "Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR < 2", percentINRALTTwoPostCardiacSugeryCohortIndicator);
		rd.addIndicator("5.3", "Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR > 4", percentINRGTTFourPostCardiacSugeryCohortIndicator);
		rd.addIndicator("5.4", "Number of hospitalizations in reporting window", hospitalizedDuringPeriodIndicator);
		
		rd.addIndicator("6.1", "number of patients without a height ever", heightEverCohortIndicator);
		rd.addIndicator("6.2", "number of patients without a donne de base (intake form)", patientWithoutDonneDebasePeriodIndicator);

		rd.setBaseCohortDefinition(h.cohortDefinition("location: Heart Failure Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
	    h.replaceReportDefinition(rd);
		
		return rd;
	}
	
//Methods defining definitions		

private void createLocationCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("location: Heart Failure Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		}		


	
private CodedObsCohortDefinition makeCodedObsCohortDefinition(String question, String value, SetComparator setComparator, TimeModifier timeModifier) {		
		CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();
		if (question != null) obsCohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(question));
		if (setComparator != null) obsCohortDefinition.setOperator(setComparator);
		if (timeModifier != null) obsCohortDefinition.setTimeModifier(timeModifier);
		Concept valueCoded = Context.getConceptService().getConceptByUuid(value);
		List<Concept> valueList = new ArrayList<Concept>();
		if (valueCoded != null) {
			valueList.add(valueCoded);
			obsCohortDefinition.setValueList(valueList);
		}		
		return obsCohortDefinition;
	}
	
private CodedObsCohortDefinition makeCodedObsCohortDefinition(String name, String question, String value, SetComparator setComparator, TimeModifier timeModifier) {		
		CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();
		obsCohortDefinition.setName(name);
		if (question != null) obsCohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(question));
		if (setComparator != null) obsCohortDefinition.setOperator(setComparator);
		if (timeModifier != null) obsCohortDefinition.setTimeModifier(timeModifier);
		Concept valueCoded = Context.getConceptService().getConceptByUuid(value);
		List<Concept> valueList = new ArrayList<Concept>();
		if (valueCoded != null) {
			valueList.add(valueCoded);
			obsCohortDefinition.setValueList(valueList);
		}		
		return obsCohortDefinition;
	}
	
	
public ProgramEnrollmentCohortDefinition getProgramEnrollment(String cohortName,String programId){
		 List<Program> programs=new ArrayList<Program>();
		ProgramEnrollmentCohortDefinition programEnrollmentCohortDefinition=new ProgramEnrollmentCohortDefinition();
		programEnrollmentCohortDefinition.setName(cohortName);
		 Program program = Context.getProgramWorkflowService().getProgramByUuid(programId);
		 if (program == null) throw new APIException("ProgramId " + programId + " does not exist"); 
		 programs.add(program);
		 programEnrollmentCohortDefinition.setPrograms(programs);	
		return programEnrollmentCohortDefinition;
		
	}
public InProgramCohortDefinition getInProgramCohortDefinition(String cohortName,String programId){
		List<Program> programs=new ArrayList<Program>();
		InProgramCohortDefinition InProgramCohortDefinition=new InProgramCohortDefinition();
		InProgramCohortDefinition.setName(cohortName);
		 Program program = Context.getProgramWorkflowService().getProgramByUuid(programId);
		 if (program == null) throw new APIException("ProgramId " + programId + " does not exist"); 
		 programs.add(program);
		 InProgramCohortDefinition.setPrograms(programs);	
		return InProgramCohortDefinition;
	} 
	
public EncounterCohortDefinition makeEncounterCohortDefinition(String name, List<Form> forms) {		
		EncounterCohortDefinition encounterCohortDefinition = new EncounterCohortDefinition();
		encounterCohortDefinition.setName(name);
		//Form form = Context.getFormService().getForm(formNumber);
		//List<Form> formList = new ArrayList<Form>();
		if (forms != null) {
			//formList.add(form);
			encounterCohortDefinition.setFormList(forms);
		}
		return encounterCohortDefinition;
	}
public EncounterCohortDefinition makeEncounterCohortDefinition(List<EncounterType> EncounterTypes) {		
	EncounterCohortDefinition encounterCohortDefinition = new EncounterCohortDefinition();
	if (EncounterTypes != null) {
		encounterCohortDefinition.setEncounterTypeList(EncounterTypes);	
	}
	return encounterCohortDefinition;
}
	
public NumericObsCohortDefinition makeNumericObsCohortDefinition(String name, String question, Double value, RangeComparator setComparator, TimeModifier timeModifier) {                
	     
	     NumericObsCohortDefinition obsCohortDefinition = new NumericObsCohortDefinition();
	             
	             obsCohortDefinition.setName(name);
	             
	             if (question != null) obsCohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(question));
	             
	             if (setComparator != null) obsCohortDefinition.setOperator1(setComparator);
	             
	             if (timeModifier != null) obsCohortDefinition.setTimeModifier(timeModifier);
	             
	            if (value != 0) {
	                     obsCohortDefinition.setValue1(value);
	             }
	                             
	             return obsCohortDefinition;
	     }
	
public PatientStateCohortDefinition makePatientStateCohortDefinition(String cohortName, String programId, String programWorkflowId, String programWorkflowStateId){
	     PatientStateCohortDefinition patientState= new PatientStateCohortDefinition();
	     patientState.setName(cohortName);
	    
	     Program program = Context.getProgramWorkflowService().getProgramByUuid(programId);
	     ProgramWorkflow programWorkflow = Context.getProgramWorkflowService().getWorkflowByUuid(programWorkflowId);    	 
	     programWorkflow.setProgram(program);
	      
	     ProgramWorkflowState programWorkflowState=Context.getProgramWorkflowService().getStateByUuid(programWorkflowStateId);
	     programWorkflowState.setProgramWorkflow(programWorkflow);
	     programWorkflow.addState(programWorkflowState);
	    
	     List<ProgramWorkflowState> programWorkFlowStateList= new ArrayList<ProgramWorkflowState>();
	     programWorkFlowStateList.add(programWorkflowState);
	     patientState.setStates(programWorkFlowStateList);    
	    
	 return patientState;

	}
	 
public List<Drug> getDrugs(String conceptUuid) {                 
	     List<Drug> drugs = null;
	     Concept drugConcept = Context.getConceptService().getConceptByUuid(conceptUuid);                
	     drugs = Context.getConceptService().getDrugsByConcept(drugConcept);                
	     if (drugs == null || drugs.isEmpty()) throw new APIException("Drug(s) " + conceptUuid + " does not exist");        
	     return drugs;
	 }
	
}
