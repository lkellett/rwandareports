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

public class SetupHeartFailurereportAllSites {
	protected final static Log log = LogFactory.getLog(SetupHeartFailurereportAllSites.class);
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupHeartFailurereportAllSites(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		//setUpGlobalProperties();
		
		//createLocationCohortDefinitions();
		//createCompositionCohortDefinitions();
		//createIndicators();
		ReportDefinition rd = createReportDefinition();
		h.createXlsOverview(rd, "heartfailurereporttemplateallsites.xls", "XlsheartfailurereporttemplateAllSites", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsheartfailurereporttemplateAllSites".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(PeriodIndicatorReportDefinition.class, "Heart Failure Report All Site");
		
		h.purgeDefinition(DataSetDefinition.class, "Heart Failure Report Data Set");
		//h.purgeDefinition(CohortDefinition.class, "location: Heart Failure Patients at location");
		

		h.purgeDefinition(CohortDefinition.class, "patientsEnrolledInHFProgramAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFProgramAllSites");
		h.purgeDefinition(CohortDefinition.class, "malesDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "femalesDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "maleInFHProgramCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "femaleInFHProgramCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCardFormBeforeEndDateAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFProgramBeforeEndDateAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFProgramWithouCardFormAllSites");
		/*
		h.purgeDefinition(CohortDefinition.class, "allPatientsInHF");
		h.purgeDefinition(CohortDefinition.class, "pateintsDied");
		h.purgeDefinition(CohortDefinition.class, "pateintsTransferedOut");
		h.purgeDefinition(CohortDefinition.class, "pateintsCured");
		h.purgeDefinition(CohortDefinition.class, "pateintsRefused");*/
		h.purgeDefinition(CohortDefinition.class, "echocardiographyDuringPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "echocardiographyAndHFProgramCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCreatinineCohortDefAllSites");
		h.purgeDefinition(CohortDefinition.class, "hfPatientWithoutCreatinineCompositionCohortDefAllSites");
		h.purgeDefinition(CohortDefinition.class, "cardiomyopathyDiognosisAllSites");		
		h.purgeDefinition(CohortDefinition.class, "cardiomyopathyDiognosisAnsHFProgCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "mitralStenosisDiagnosisAllSites");
		h.purgeDefinition(CohortDefinition.class, "mitralStenosisDiagnosisAnsHFProgCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "rheumaticHeartDiseaseDiagnosisAllSites");
		h.purgeDefinition(CohortDefinition.class, "rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "hypertensiveHeartDiseaseDiagnosisAllSites");
		h.purgeDefinition(CohortDefinition.class, "hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "pericardialDiseaseDiagnosisAllSites");
		h.purgeDefinition(CohortDefinition.class, "pericardialDiseaseDiagnosisAndHFProgCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "congenitalDiseaseDiagnosisAllSites");
		h.purgeDefinition(CohortDefinition.class, "congenitalDiseaseDiagnosisAndHFProgCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCreatinineAllSites");
		h.purgeDefinition(CohortDefinition.class, "hfpatientsWithCreatinineCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "postCardiacSurgeryCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsInFamilyPlanningAllSites");
		h.purgeDefinition(CohortDefinition.class, "lessThanFiftyAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientsInHFWithoutFamilyPlanningCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onLasixAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFonLasixAtEndOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onAtenololAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFonAtenololAtEndOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onCarvedilolAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onAldactoneAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFonAldactoneAtEndOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onWarfarinAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFonWarfarinAtEndOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onLisinoprilAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "onCaptoprilAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFLisinoprilOrCaptoprilCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFAtenololAndCarvedilolCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFLisinoprilAndCaptoprilCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "heartRateDiseaseDuringPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "onPenicillinAtEndOfPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites");
		h.purgeDefinition(CohortDefinition.class, "hFencounterDuringPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientNotSeenDuringPeriodCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "allPatientsWhitAccompagnateurAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientWithoutAccompagnateurPeriodCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "diedDuringPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "diedDuringPeriodCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "INRLTTwoCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "INRGTFourCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "hospitalizedDuringPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "hospitalizedDuringPeriodCompositionAllSites");
		h.purgeDefinition(CohortDefinition.class, "heightCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "heightEverCompositionCohortDefinitionAllSites");
		h.purgeDefinition(CohortDefinition.class, "encounterFormDuringDDBPeriodAllSites");
		h.purgeDefinition(CohortDefinition.class, "patientWithoutDonneDebasePeriodCompositionAllSites");
		
			
		
		h.purgeDefinition(CohortIndicator.class, "percentMaleInFHProgramIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "percentFemaleInFHProgramIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "medianAgeAllSites");	
		h.purgeDefinition(CohortIndicator.class, "patientsInHFIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "patientsEnrolledInHFIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "patientsInHFProgramWithouCardFormIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "hfEchocardiographyPercentageIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "hfPatientWithoutCreatininePercentIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "cardiomyopathyDiognosisAnsHFProgIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "mitralStenosisDiagnosisAnsHFProgIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "hfpatientsWithCreatininePercentIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "postCardiacSurgeryCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "patientsInHFWithoutFamilyPlanningIndicatorAllSites");		
		h.purgeDefinition(CohortIndicator.class, "onLasixAtEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onAtenololAtEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onCarvedilolAtEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onAldactoneAtEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onWarfarinAtEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onMitralStenosisHeartRatePeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "percentPatientWithoutAccompagnateurIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "diedDuringPeriodIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "hospitalizedDuringPeriodIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "heightEverCohortIndicatorAllSites");
		h.purgeDefinition(CohortIndicator.class, "patientWithoutDonneDebasePeriodIndicatorAllSites");

}
	
	
	private ReportDefinition createReportDefinition() {
		// Heart Failure Indicator Report
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		//rd.addParameter(new Parameter("location", "Location", Location.class));		
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
				
		rd.setName("Heart Failure Report All Sites");
		
		rd.setupDataSetDefinition();

//Patient In Heart Failure Program
		

		InProgramCohortDefinition patientsInHFProgramAllSites=getInProgramCohortDefinition("patientsInHFProgramAllSites", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
		patientsInHFProgramAllSites.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsInHFProgramAllSites.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientsInHFProgramAllSites);
		
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
		
		GenderCohortDefinition femalesDefinitionAllSites=new GenderCohortDefinition();
		femalesDefinitionAllSites.setName("femalesDefinitionAllSites");
		femalesDefinitionAllSites.setFemaleIncluded(true);
		h.replaceCohortDefinition(femalesDefinitionAllSites);
		
		GenderCohortDefinition malesDefinitionAllSites=new GenderCohortDefinition();
		malesDefinitionAllSites.setName("malesDefinitionAllSites");
		malesDefinitionAllSites.setMaleIncluded(true);		
		h.replaceCohortDefinition(malesDefinitionAllSites);
		
		CompositionCohortDefinition maleInFHProgramCompositionAllSites=new CompositionCohortDefinition();
		maleInFHProgramCompositionAllSites.setName("maleInFHProgramCompositionAllSites");
		maleInFHProgramCompositionAllSites.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleInFHProgramCompositionAllSites.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleInFHProgramCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleInFHProgramCompositionAllSites.getSearches().put("malesDefinition",new Mapped<CohortDefinition>(malesDefinitionAllSites,null));
		maleInFHProgramCompositionAllSites.setCompositionString("patientsInHFProgram AND malesDefinition");
		h.replaceCohortDefinition(maleInFHProgramCompositionAllSites);
		
		CompositionCohortDefinition femaleInFHProgramCompositionAllSites=new CompositionCohortDefinition();
		femaleInFHProgramCompositionAllSites.setName("femaleInFHProgramCompositionAllSites");
		femaleInFHProgramCompositionAllSites.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleInFHProgramCompositionAllSites.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleInFHProgramCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleInFHProgramCompositionAllSites.getSearches().put("femalesDefinition",new Mapped<CohortDefinition>(femalesDefinitionAllSites,null));
		femaleInFHProgramCompositionAllSites.setCompositionString("patientsInHFProgram AND femalesDefinition");
		h.replaceCohortDefinition(femaleInFHProgramCompositionAllSites);
		
		CohortIndicator percentMaleInFHProgramIndicatorAllSites = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(maleInFHProgramCompositionAllSites, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentMaleInFHProgramIndicatorAllSites.setName("percentMaleInFHProgramIndicatorAllSites");
		percentMaleInFHProgramIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentMaleInFHProgramIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentMaleInFHProgramIndicatorAllSites);
		
		CohortIndicator percentFemaleInFHProgramIndicatorAllSites = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(femaleInFHProgramCompositionAllSites, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentFemaleInFHProgramIndicatorAllSites.setName("percentFemaleInFHProgramIndicatorAllSites");
		percentFemaleInFHProgramIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentFemaleInFHProgramIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentFemaleInFHProgramIndicatorAllSites);
	
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
            
		CohortIndicator medianAgeAllSites = CohortIndicator.newLogicIndicator("medianAgeAllSites", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null, MedianAggregator.class, "AGE");
		medianAgeAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
		medianAgeAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(medianAgeAllSites);

//=========================================================================================
//      1.3.   Patients enrolled
//=========================================================================================
		
//Patient Enrolled in Heart Failure Program
		
		ProgramEnrollmentCohortDefinition patientsEnrolledInHFProgramAllSites=getProgramEnrollment("patientsEnrolledInHFProgramAllSites", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
		patientsEnrolledInHFProgramAllSites.addParameter(new Parameter("enrolledOnOrAfter","enrolledOnOrAfter",Date.class));
		patientsEnrolledInHFProgramAllSites.addParameter(new Parameter("enrolledOnOrBefore","enrolledOnOrBefore",Date.class));
		h.replaceCohortDefinition(patientsEnrolledInHFProgramAllSites);
		
		CohortIndicator patientsEnrolledInHFIndicatorAllSites = new CohortIndicator();
		patientsEnrolledInHFIndicatorAllSites.setName("patientsEnrolledInHFIndicatorAllSites");
		patientsEnrolledInHFIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsEnrolledInHFIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsEnrolledInHFIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(patientsEnrolledInHFProgramAllSites,ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		h.replaceDefinition(patientsEnrolledInHFIndicatorAllSites);

//=========================================================================================
//      1.4.   Total number of Patient in Heart Failure
//=========================================================================================

		CohortIndicator patientsInHFIndicatorAllSites = new CohortIndicator();
		patientsInHFIndicatorAllSites.setName("patientsInHFIndicatorAllSites");
		patientsInHFIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsInHFIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsInHFIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(patientsInHFIndicatorAllSites);
		
//===============================================================================
//  2.1. number and percent of patients without a cardiology consultation
//===============================================================================
		
	List<Form> cardCons=new ArrayList<Form>();
	String formid=Context.getAdministrationService().getGlobalProperty("cardiologyreporting.cardilogyConsultationFormId");
	cardCons.add(Context.getFormService().getForm(Integer.valueOf(formid)));
	
	EncounterCohortDefinition patientsWithCardFormBeforeEndDateAllSites =makeEncounterCohortDefinition("patientsWithCardFormBeforeEndDateAllSites", cardCons);       
	patientsWithCardFormBeforeEndDateAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(patientsWithCardFormBeforeEndDateAllSites);
	
   /* InProgramCohortDefinition patientsInHFProgramBeforeEndDate=getInProgramCohortDefinition("patientsInHFProgramBeforeEndDate", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID);
    patientsInHFProgramBeforeEndDate.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    h.replaceCohortDefinition(patientsInHFProgramBeforeEndDate);
    */
    CompositionCohortDefinition patientsInHFProgramWithouCardFormAllSites=new CompositionCohortDefinition();
    patientsInHFProgramWithouCardFormAllSites.setName("patientsInHFProgramWithouCardFormAllSites");
    patientsInHFProgramWithouCardFormAllSites.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
    patientsInHFProgramWithouCardFormAllSites.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
    patientsInHFProgramWithouCardFormAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
    patientsInHFProgramWithouCardFormAllSites.getSearches().put("patientsWithCardFormBeforeEndDate",new Mapped<CohortDefinition>(patientsWithCardFormBeforeEndDateAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    patientsInHFProgramWithouCardFormAllSites.setCompositionString("patientsInHFProgram AND (NOT patientsWithCardFormBeforeEndDate)");
	h.replaceCohortDefinition(patientsInHFProgramWithouCardFormAllSites);
	
	/*CohortIndicator patientsInHFProgramWithouCardFormIndicator = new CohortIndicator();
	patientsInHFProgramWithouCardFormIndicator.setName("patientsInHFProgramWithouCardFormIndicator");
	patientsInHFProgramWithouCardFormIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
	patientsInHFProgramWithouCardFormIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
	patientsInHFProgramWithouCardFormIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsInHFProgramWithouCardForm,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
	h.replaceDefinition(patientsInHFProgramWithouCardFormIndicator);
	*/
	CohortIndicator patientsInHFProgramWithouCardFormIndicatorAllSites = CohortIndicator.newFractionIndicator
	(null,new Mapped<CohortDefinition>(patientsInHFProgramWithouCardFormAllSites, 
			ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
			new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
			ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
			null);
	patientsInHFProgramWithouCardFormIndicatorAllSites.setName("patientsInHFProgramWithouCardFormIndicatorAllSites");
	patientsInHFProgramWithouCardFormIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	patientsInHFProgramWithouCardFormIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	h.replaceDefinition(patientsInHFProgramWithouCardFormIndicatorAllSites);
	
	
// =============================================================================== 
//   2.2.Number and percent of patients without a preliminary echocardiographic diagnosis
// ===============================================================================   
// echocardiographyDuringPeriod echocardiographyAndHFProgramComposition hfEchocardiographyPercentageIndicator

      CodedObsCohortDefinition echocardiographyDuringPeriodAllSites =makeCodedObsCohortDefinition("echocardiographyDuringPeriodAllSites",HeartFailureReportConstants.DDB_ECHOCARDIOGRAPH_RESULT, HeartFailureReportConstants.NOT_DONE, SetComparator.IN, TimeModifier.ANY);		
      h.replaceCohortDefinition(echocardiographyDuringPeriodAllSites);
       
      CompositionCohortDefinition echocardiographyAndHFProgramCompositionAllSites = new CompositionCohortDefinition();
      echocardiographyAndHFProgramCompositionAllSites.setName("echocardiographyAndHFProgramCompositionAllSites");
      echocardiographyAndHFProgramCompositionAllSites.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
      echocardiographyAndHFProgramCompositionAllSites.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
      echocardiographyAndHFProgramCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
      echocardiographyAndHFProgramCompositionAllSites.getSearches().put("echocardiographyDuringPeriod", new Mapped<CohortDefinition>(echocardiographyDuringPeriodAllSites, null));
      echocardiographyAndHFProgramCompositionAllSites.setCompositionString("(patientsInHFProgram AND echocardiographyDuringPeriod");
      h.replaceCohortDefinition(echocardiographyAndHFProgramCompositionAllSites);
      
      CohortIndicator hfEchocardiographyPercentageIndicatorAllSites = CohortIndicator.newFractionIndicator("hfEchocardiographyPercentageIndicatorAllSites", new Mapped<CohortDefinition>(echocardiographyAndHFProgramCompositionAllSites , ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
   		  new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
      hfEchocardiographyPercentageIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
      hfEchocardiographyPercentageIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
      h.replaceDefinition(hfEchocardiographyPercentageIndicatorAllSites);

      
// ===============================================================================                         
//                 2.3.  Percent without a creatinine in the last 6 months
// ===============================================================================   

      NumericObsCohortDefinition patientsWithCreatinineCohortDefAllSites=makeNumericObsCohortDefinition("patientsWithCreatinineCohortDefAllSites", HeartFailureReportConstants.SERUM_CREATININE, 0.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
      patientsWithCreatinineCohortDefAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
      patientsWithCreatinineCohortDefAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
      h.replaceCohortDefinition(patientsWithCreatinineCohortDefAllSites);
                              
      CompositionCohortDefinition hfPatientWithoutCreatinineCompositionCohortDefAllSites = new CompositionCohortDefinition();
      hfPatientWithoutCreatinineCompositionCohortDefAllSites.setName("hfPatientWithoutCreatinineCompositionCohortDefAllSites");
      hfPatientWithoutCreatinineCompositionCohortDefAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
      hfPatientWithoutCreatinineCompositionCohortDefAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
      hfPatientWithoutCreatinineCompositionCohortDefAllSites.getSearches().put("patientsWithCreatinineCohortDef", new Mapped<CohortDefinition>(patientsWithCreatinineCohortDefAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
      hfPatientWithoutCreatinineCompositionCohortDefAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
      hfPatientWithoutCreatinineCompositionCohortDefAllSites.setCompositionString("patientsInHFProgram AND (NOT patientsWithCreatinineCohortDef)");
      h.replaceCohortDefinition(hfPatientWithoutCreatinineCompositionCohortDefAllSites);
      
      CohortIndicator hfPatientWithoutCreatininePercentIndicatorAllSites = CohortIndicator.newFractionIndicator("hfPatientWithoutCreatininePercentIndicatorAllSites", new Mapped<CohortDefinition>(hfPatientWithoutCreatinineCompositionCohortDefAllSites , ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${endDate}")), 
       		  new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
      hfPatientWithoutCreatininePercentIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
      hfPatientWithoutCreatininePercentIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
      h.replaceDefinition(hfPatientWithoutCreatininePercentIndicatorAllSites);

//===============================================================================                 
//   PATIENTS WHO WITH HEART FAILURE DIAGNOSIS IN THE LAST MONTH
// ===============================================================================                 
    
//===============================================================================                 
//     2.4. Patient with Cardiomyopathy
// ===============================================================================                 
 
	CodedObsCohortDefinition cardiomyopathyDiognosisAllSites =makeCodedObsCohortDefinition("cardiomyopathyDiognosisAllSites", HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.CARDIOMYOPATHY, SetComparator.IN, TimeModifier.ANY);	
	cardiomyopathyDiognosisAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(cardiomyopathyDiognosisAllSites);
	
	 CompositionCohortDefinition cardiomyopathyDiognosisAnsHFProgCompositionAllSites = new CompositionCohortDefinition();
	 cardiomyopathyDiognosisAnsHFProgCompositionAllSites.setName("cardiomyopathyDiognosisAnsHFProgCompositionAllSites");
	 cardiomyopathyDiognosisAnsHFProgCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 cardiomyopathyDiognosisAnsHFProgCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	 cardiomyopathyDiognosisAnsHFProgCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	 cardiomyopathyDiognosisAnsHFProgCompositionAllSites.getSearches().put("cardiomyopathyDiognosis", new Mapped<CohortDefinition>(cardiomyopathyDiognosisAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	 cardiomyopathyDiognosisAnsHFProgCompositionAllSites.setCompositionString("(patientsInHFProgram AND cardiomyopathyDiognosis");
	 h.replaceCohortDefinition(cardiomyopathyDiognosisAnsHFProgCompositionAllSites);
 
	 CohortIndicator cardiomyopathyDiognosisAnsHFProgIndicatorAllSites = new CohortIndicator();
	 cardiomyopathyDiognosisAnsHFProgIndicatorAllSites.setName("cardiomyopathyDiognosisAnsHFProgIndicatorAllSites");
	 cardiomyopathyDiognosisAnsHFProgIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	 cardiomyopathyDiognosisAnsHFProgIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
     cardiomyopathyDiognosisAnsHFProgIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
     h.replaceDefinition(cardiomyopathyDiognosisAnsHFProgIndicatorAllSites);
      
      
//===============================================================================                 
//               2.5.  PATIENTS WHICH HAVE HAD  PURE MITRAL STENOSIS
//===============================================================================  

    CodedObsCohortDefinition mitralStenosisDiagnosisAllSites =makeCodedObsCohortDefinition("mitralStenosisDiagnosisAllSites",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.MITRAL_STENOSIS, SetComparator.IN, TimeModifier.ANY);                
    mitralStenosisDiagnosisAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    h.replaceCohortDefinition(mitralStenosisDiagnosisAllSites);
    
    CompositionCohortDefinition mitralStenosisDiagnosisAnsHFProgCompositionAllSites = new CompositionCohortDefinition();
    mitralStenosisDiagnosisAnsHFProgCompositionAllSites.setName("mitralStenosisDiagnosisAnsHFProgCompositionAllSites");
    mitralStenosisDiagnosisAnsHFProgCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    mitralStenosisDiagnosisAnsHFProgCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    mitralStenosisDiagnosisAnsHFProgCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    mitralStenosisDiagnosisAnsHFProgCompositionAllSites.getSearches().put("mitralStenosisDiagnosis", new Mapped<CohortDefinition>(mitralStenosisDiagnosisAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    mitralStenosisDiagnosisAnsHFProgCompositionAllSites.setCompositionString("(patientsInHFProgram AND mitralStenosisDiagnosis");
	 h.replaceCohortDefinition(mitralStenosisDiagnosisAnsHFProgCompositionAllSites);

	 CohortIndicator mitralStenosisDiagnosisAnsHFProgIndicatorAllSites = new CohortIndicator();
	 mitralStenosisDiagnosisAnsHFProgIndicatorAllSites.setName("mitralStenosisDiagnosisAnsHFProgIndicatorAllSites");
	 mitralStenosisDiagnosisAnsHFProgIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	 mitralStenosisDiagnosisAnsHFProgIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	 mitralStenosisDiagnosisAnsHFProgIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
    h.replaceDefinition(mitralStenosisDiagnosisAnsHFProgIndicatorAllSites);
     
      
  //===============================================================================                 
  //      2.6.  PATIENTS WHICH HAVE HAD A RHEUMATIC HEART DISEASE
  //===============================================================================  

    CodedObsCohortDefinition rheumaticHeartDiseaseDiagnosisAllSites =makeCodedObsCohortDefinition("rheumaticHeartDiseaseDiagnosisAllSites",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.RHEUMATIC_HEART_DISEASE, SetComparator.IN, TimeModifier.ANY);
    rheumaticHeartDiseaseDiagnosisAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(rheumaticHeartDiseaseDiagnosisAllSites);
	
    CompositionCohortDefinition rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites = new CompositionCohortDefinition();
    rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites.setName("rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites");
    rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("rheumaticHeartDiseaseDiagnosis", new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites.setCompositionString("(patientsInHFProgram AND rheumaticHeartDiseaseDiagnosis");
	 h.replaceCohortDefinition(rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites);

	 CohortIndicator rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites = new CohortIndicator();
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.setName("rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites");
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	 rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
    h.replaceDefinition(rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites);
     
//===============================================================================                 
//    2.7.  PATIENTS WHO HAVE HAD A HYPERTENSIVE HEART DISEASE
//===============================================================================                                
    
                   
	 
	   CodedObsCohortDefinition hypertensiveHeartDiseaseDiagnosisAllSites =makeCodedObsCohortDefinition("hypertensiveHeartDiseaseDiagnosisAllSites",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.HYPERTENSIVE_HEART_DISEASE, SetComparator.IN, TimeModifier.ANY);
	   hypertensiveHeartDiseaseDiagnosisAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	   h.replaceCohortDefinition(hypertensiveHeartDiseaseDiagnosisAllSites);
	   
	  CompositionCohortDefinition hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites = new CompositionCohortDefinition();
	  hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites.setName("hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites");
	  hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("hypertensiveHeartDiseaseDiagnosis", new Mapped<CohortDefinition>(hypertensiveHeartDiseaseDiagnosisAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	  hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites.setCompositionString("(patientsInHFProgram AND hypertensiveHeartDiseaseDiagnosis");
		 h.replaceCohortDefinition(hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites);

		 CohortIndicator hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites = new CohortIndicator();
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.setName("hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites");
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
		 hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(hypertensiveHeartDiseaseDiagnosisAndHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
	    h.replaceDefinition(hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites);
	     
		  
// ===============================================================================                 
//  2.8.  PATIENTS WHO HAVE HAD A PERICARDIAL DISEASE
// ===============================================================================   				    

	  
	    
	 CodedObsCohortDefinition pericardialDiseaseDiagnosisAllSites =makeCodedObsCohortDefinition("pericardialDiseaseDiagnosisAllSites", HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.PERICARDIAL_DISEASE, SetComparator.IN, TimeModifier.ANY);	
	 pericardialDiseaseDiagnosisAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 h.replaceCohortDefinition(pericardialDiseaseDiagnosisAllSites);

    CompositionCohortDefinition pericardialDiseaseDiagnosisAndHFProgCompositionAllSites = new CompositionCohortDefinition();
    pericardialDiseaseDiagnosisAndHFProgCompositionAllSites.setName("pericardialDiseaseDiagnosisAndHFProgCompositionAllSites");
    pericardialDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    pericardialDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
    pericardialDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
    pericardialDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("pericardialDiseaseDiagnosis", new Mapped<CohortDefinition>(pericardialDiseaseDiagnosisAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
    pericardialDiseaseDiagnosisAndHFProgCompositionAllSites.setCompositionString("(patientsInHFProgram AND pericardialDiseaseDiagnosis");
	h.replaceCohortDefinition(pericardialDiseaseDiagnosisAndHFProgCompositionAllSites);

	CohortIndicator pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites = new CohortIndicator();
	pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites.setName("pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites");
	pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(pericardialDiseaseDiagnosisAndHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
    h.replaceDefinition(pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites);
     			 
// ===============================================================================                 
//         2.9. PATIENTS WHO HAVE HAD A CONGENITAL HEART DISEASE
// ===============================================================================  

	 CodedObsCohortDefinition congenitalDiseaseDiagnosisAllSites =makeCodedObsCohortDefinition("congenitalDiseaseDiagnosisAllSites",HeartFailureReportConstants.HEART_FAILURE_DIAGNOSIS, HeartFailureReportConstants.CONGENITAL_HEART_DISEASE, SetComparator.IN, TimeModifier.ANY);
	 congenitalDiseaseDiagnosisAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 h.replaceCohortDefinition(congenitalDiseaseDiagnosisAllSites);
		  
	CompositionCohortDefinition congenitalDiseaseDiagnosisAndHFProgCompositionAllSites = new CompositionCohortDefinition();
	congenitalDiseaseDiagnosisAndHFProgCompositionAllSites.setName("congenitalDiseaseDiagnosisAndHFProgCompositionAllSites");
	congenitalDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	congenitalDiseaseDiagnosisAndHFProgCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	congenitalDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	congenitalDiseaseDiagnosisAndHFProgCompositionAllSites.getSearches().put("congenitalDiseaseDiagnosis", new Mapped<CohortDefinition>(congenitalDiseaseDiagnosisAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	congenitalDiseaseDiagnosisAndHFProgCompositionAllSites.setCompositionString("(patientsInHFProgram AND congenitalDiseaseDiagnosis");
	h.replaceCohortDefinition(congenitalDiseaseDiagnosisAndHFProgCompositionAllSites);

	 CohortIndicator congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites = new CohortIndicator();
	 congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites.setName("congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites");
	 congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	 congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	 congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(congenitalDiseaseDiagnosisAndHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
	 h.replaceDefinition(congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites);
   	 
// ========================================================================================                        
//   2.10. Parcent with creatinine > 200
// ========================================================================================  
	 
	 
	NumericObsCohortDefinition patientsWithCreatinineAllSites=makeNumericObsCohortDefinition("patientsWithCreatinineAllSites", HeartFailureReportConstants.SERUM_CREATININE, 200.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
	patientsWithCreatinineAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
    h.replaceCohortDefinition(patientsWithCreatinineAllSites);
    
	CompositionCohortDefinition hfpatientsWithCreatinineCompositionAllSites = new CompositionCohortDefinition();
	hfpatientsWithCreatinineCompositionAllSites.setName("hfpatientsWithCreatinineCompositionAllSites");
	hfpatientsWithCreatinineCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	hfpatientsWithCreatinineCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	hfpatientsWithCreatinineCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	hfpatientsWithCreatinineCompositionAllSites.getSearches().put("patientsWithCreatinine", new Mapped<CohortDefinition>(patientsWithCreatinineAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	hfpatientsWithCreatinineCompositionAllSites.setCompositionString("patientsInHFProgram AND patientsWithCreatinine");
	h.replaceCohortDefinition(hfpatientsWithCreatinineCompositionAllSites);
      
       
    CohortIndicator hfpatientsWithCreatininePercentIndicatorAllSites = CohortIndicator.newFractionIndicator("hfpatientsWithCreatininePercentIndicatorAllSites", new Mapped<CohortDefinition>(hfpatientsWithCreatinineCompositionAllSites , 
       		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
	              ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
    hfpatientsWithCreatininePercentIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
    hfpatientsWithCreatininePercentIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
    h.replaceDefinition(hfpatientsWithCreatininePercentIndicatorAllSites);
    
    
//======================================================================================================                       
//   2.11. number post-cardiac surgery
//====================================================================================================== 
   
    PatientStateCohortDefinition postCardiacSurgeryCohortDefinitionAllSites=makePatientStateCohortDefinition("postCardiacSurgeryCohortDefinitionAllSites", HeartFailureReportConstants.HEART_FAILURE_PROGRAM_UUID, HeartFailureReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_UUID, HeartFailureReportConstants.HEART_FAILURE_PROGRAM_SURGERY_STATUS_WORKFLOW_POST_OPERATIVE_STATE_UUID);
    postCardiacSurgeryCohortDefinitionAllSites.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
    postCardiacSurgeryCohortDefinitionAllSites.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
    h.replaceCohortDefinition(postCardiacSurgeryCohortDefinitionAllSites);
    
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
  
    CohortIndicator postCardiacSurgeryCohortIndicatorAllSites = new CohortIndicator();
    postCardiacSurgeryCohortIndicatorAllSites.setName("postCardiacSurgeryCohortIndicatorAllSites");
    postCardiacSurgeryCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate",Date.class));
    postCardiacSurgeryCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
    postCardiacSurgeryCohortIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinitionAllSites,ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}")));
    h.replaceDefinition(postCardiacSurgeryCohortIndicatorAllSites);
 

//================================================                        
//   3.4. in the subgroup (female < age 50), percent not on family planning
// ================================================  
  
    NumericObsCohortDefinition patientsInFamilyPlanningAllSites =makeNumericObsCohortDefinition("patientsInFamilyPlanningAllSites",HeartFailureReportConstants.PATIENT_USING_FAMILY_PLANNING,1.0, RangeComparator.EQUAL, TimeModifier.LAST);       
	patientsInFamilyPlanningAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	h.replaceCohortDefinition(patientsInFamilyPlanningAllSites);
	
	AgeCohortDefinition lessThanFiftyAllSites = new AgeCohortDefinition(null, 50, null); 
	 lessThanFiftyAllSites.setName("lessThanFiftyAllSites");
	 h.replaceCohortDefinition(lessThanFiftyAllSites);
	 
	 CompositionCohortDefinition patientsInHFWithoutFamilyPlanningCompositionCohortAllSites = new CompositionCohortDefinition();
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.setName("patientsInHFWithoutFamilyPlanningCompositionCohortAllSites");
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.getSearches().put("patientsInFamilyPlanning", new Mapped<CohortDefinition>(patientsInFamilyPlanningAllSites,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.getSearches().put("femalesDefinition", new Mapped<CohortDefinition>(femalesDefinitionAllSites,null));
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.getSearches().put("lessThanFifty", new Mapped<CohortDefinition>(lessThanFiftyAllSites,null));
	 patientsInHFWithoutFamilyPlanningCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND femalesDefinition AND lessThanFifty AND (NOT patientsInFamilyPlanning)");
	 h.replaceCohortDefinition(patientsInHFWithoutFamilyPlanningCompositionCohortAllSites);
	 
	CohortIndicator patientsInHFWithoutFamilyPlanningIndicatorAllSites = CohortIndicator.newFractionIndicator("patientsInHFWithoutFamilyPlanningIndicatorAllSites", new Mapped<CohortDefinition>(patientsInHFWithoutFamilyPlanningCompositionCohortAllSites , 
	  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
	  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
	patientsInHFWithoutFamilyPlanningIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
	patientsInHFWithoutFamilyPlanningIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	h.replaceDefinition(patientsInHFWithoutFamilyPlanningIndicatorAllSites);
	
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
   DrugsActiveCohortDefinition onLasixAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
   onLasixAtEndOfPeriodAllSites.setName("onLasixAtEndOfPeriodAllSites");
   onLasixAtEndOfPeriodAllSites.setDrugs(onFurosemideRegimen);
   onLasixAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
   h.replaceCohortDefinition(onLasixAtEndOfPeriodAllSites);

   CompositionCohortDefinition hFonLasixAtEndOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.setName("hFonLasixAtEndOfPeriodCompositionCohortAllSites");
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.getSearches().put("onLasixAtEndOfPeriod", new Mapped<CohortDefinition>(onLasixAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
   hFonLasixAtEndOfPeriodCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND onLasixAtEndOfPeriod");
   h.replaceCohortDefinition(hFonLasixAtEndOfPeriodCompositionCohortAllSites);
   
   CohortIndicator onLasixAtEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onLasixAtEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFonLasixAtEndOfPeriodCompositionCohortAllSites , 
     		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
	              ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
    onLasixAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
    onLasixAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
    h.replaceDefinition(onLasixAtEndOfPeriodCohortIndicatorAllSites);


// ================================================                        
//  3.6. Patients on Atenolol COHORT
// ================================================ 
    
DrugsActiveCohortDefinition onAtenololAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
onAtenololAtEndOfPeriodAllSites.setName("onAtenololAtEndOfPeriodAllSites");
onAtenololAtEndOfPeriodAllSites.setDrugs(onAtenololRegimen);
onAtenololAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onAtenololAtEndOfPeriodAllSites);

CompositionCohortDefinition hFonAtenololAtEndOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.setName("hFonAtenololAtEndOfPeriodCompositionCohortAllSites");
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.getSearches().put("onAtenololAtEndOfPeriod", new Mapped<CohortDefinition>(onAtenololAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonAtenololAtEndOfPeriodCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND onAtenololAtEndOfPeriod");
h.replaceCohortDefinition(hFonAtenololAtEndOfPeriodCompositionCohortAllSites);

CohortIndicator onAtenololAtEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onAtenololAtEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFonAtenololAtEndOfPeriodCompositionCohortAllSites , 
  		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
             ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onAtenololAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onAtenololAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onAtenololAtEndOfPeriodCohortIndicatorAllSites);

// ================================================                        
//  3.7. Patients Carvedilol COHORT
// ================================================   

DrugsActiveCohortDefinition onCarvedilolAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
onCarvedilolAtEndOfPeriodAllSites.setName("onCarvedilolAtEndOfPeriodAllSites");
onCarvedilolAtEndOfPeriodAllSites.setDrugs(onCarvedilolRegimen);
onCarvedilolAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onCarvedilolAtEndOfPeriodAllSites);

CompositionCohortDefinition hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.setName("hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites");
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.getSearches().put("onCarvedilolAtEndOfPeriod", new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND onCarvedilolAtEndOfPeriod");
h.replaceCohortDefinition(hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites);

CohortIndicator onCarvedilolAtEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onCarvedilolAtEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFonCarvedilolAtEndOfPeriodCompositionCohortAllSites , 
  		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
             ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onCarvedilolAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onCarvedilolAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onCarvedilolAtEndOfPeriodCohortIndicatorAllSites);
           
// ================================================                        
//  3.8. Patients Aldactone COHORT
// ================================================   


DrugsActiveCohortDefinition onAldactoneAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
onAldactoneAtEndOfPeriodAllSites.setName("onAldactoneAtEndOfPeriodAllSites");
onAldactoneAtEndOfPeriodAllSites.setDrugs(onAldactoneRegimen);
onAldactoneAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onAldactoneAtEndOfPeriodAllSites);

CompositionCohortDefinition hFonAldactoneAtEndOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.setName("hFonAldactoneAtEndOfPeriodCompositionCohortAllSites");
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.getSearches().put("onAldactoneAtEndOfPeriod", new Mapped<CohortDefinition>(onAldactoneAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonAldactoneAtEndOfPeriodCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND onAldactoneAtEndOfPeriod");
h.replaceCohortDefinition(hFonAldactoneAtEndOfPeriodCompositionCohortAllSites);

CohortIndicator onAldactoneAtEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onAldactoneAtEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFonAldactoneAtEndOfPeriodCompositionCohortAllSites , 
  		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
             ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onAldactoneAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onAldactoneAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onAldactoneAtEndOfPeriodCohortIndicatorAllSites);               
                      
  
                      
//================================================                        
//  3.9. Patients Lisinopril or Captopril COHORT
//================================================   	                                                

                                        
 /*Lisinopril*/
  DrugsActiveCohortDefinition onLisinoprilAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
  onLisinoprilAtEndOfPeriodAllSites.setName("onLisinoprilAtEndOfPeriodAllSites");
  onLisinoprilAtEndOfPeriodAllSites.setDrugs(onLisinoprilRegimen);
  onLisinoprilAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
  h.replaceCohortDefinition(onLisinoprilAtEndOfPeriodAllSites);
 
                               
/* Captopril*/
    DrugsActiveCohortDefinition onCaptoprilAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
    onCaptoprilAtEndOfPeriodAllSites.setName("onCaptoprilAtEndOfPeriodAllSites");
    onCaptoprilAtEndOfPeriodAllSites.setDrugs(onCaptoprilRegimen);
    onCaptoprilAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
    h.replaceCohortDefinition(onCaptoprilAtEndOfPeriodAllSites);
     
CompositionCohortDefinition hFLisinoprilOrCaptoprilCompositionCohortAllSites = new CompositionCohortDefinition();
hFLisinoprilOrCaptoprilCompositionCohortAllSites.setName("hFLisinoprilOrCaptoprilCompositionCohortAllSites");
hFLisinoprilOrCaptoprilCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFLisinoprilOrCaptoprilCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFLisinoprilOrCaptoprilCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFLisinoprilOrCaptoprilCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFLisinoprilOrCaptoprilCompositionCohortAllSites.getSearches().put("onLisinoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onLisinoprilAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilOrCaptoprilCompositionCohortAllSites.getSearches().put("onCaptoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onCaptoprilAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilOrCaptoprilCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND (onLisinoprilAtEndOfPeriod OR onCaptoprilAtEndOfPeriod)");
h.replaceCohortDefinition(hFLisinoprilOrCaptoprilCompositionCohortAllSites);

CohortIndicator onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFLisinoprilOrCaptoprilCompositionCohortAllSites , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites);
                        
                     
                             
//================================================                        
//  3.10. Patients Atenolol and Carvedilol COHORT
//================================================   	                                                


CompositionCohortDefinition hFAtenololAndCarvedilolCompositionCohortAllSites = new CompositionCohortDefinition();
hFAtenololAndCarvedilolCompositionCohortAllSites.setName("hFAtenololAndCarvedilolCompositionCohortAllSites");
hFAtenololAndCarvedilolCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFAtenololAndCarvedilolCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFAtenololAndCarvedilolCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFAtenololAndCarvedilolCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFAtenololAndCarvedilolCompositionCohortAllSites.getSearches().put("onAtenololAtEndOfPeriod", new Mapped<CohortDefinition>(onAtenololAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFAtenololAndCarvedilolCompositionCohortAllSites.getSearches().put("onCarvedilolAtEndOfPeriod", new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFAtenololAndCarvedilolCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND (onAtenololAtEndOfPeriod AND onCarvedilolAtEndOfPeriod)");
h.replaceCohortDefinition(hFAtenololAndCarvedilolCompositionCohortAllSites);

CohortIndicator onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFAtenololAndCarvedilolCompositionCohortAllSites , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites);
     
          

//================================================                        
//  3.11. Patients Lisinopril and Captopril COHORT
//================================================   	                                                


CompositionCohortDefinition hFLisinoprilAndCaptoprilCompositionCohortAllSites = new CompositionCohortDefinition();
hFLisinoprilAndCaptoprilCompositionCohortAllSites.setName("hFLisinoprilAndCaptoprilCompositionCohortAllSites");
hFLisinoprilAndCaptoprilCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFLisinoprilAndCaptoprilCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFLisinoprilAndCaptoprilCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFLisinoprilAndCaptoprilCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFLisinoprilAndCaptoprilCompositionCohortAllSites.getSearches().put("onLisinoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onLisinoprilAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilAndCaptoprilCompositionCohortAllSites.getSearches().put("onCaptoprilAtEndOfPeriod", new Mapped<CohortDefinition>(onCaptoprilAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFLisinoprilAndCaptoprilCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND (onLisinoprilAtEndOfPeriod AND onCaptoprilAtEndOfPeriod)");
h.replaceCohortDefinition(hFLisinoprilAndCaptoprilCompositionCohortAllSites);

CohortIndicator onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFLisinoprilAndCaptoprilCompositionCohortAllSites , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites);
     
//================================================                        
//3.12. Patients on wafarin COHORT
//================================================ 


DrugsActiveCohortDefinition onWarfarinAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
onWarfarinAtEndOfPeriodAllSites.setName("onWarfarinAtEndOfPeriodAllSites");
onWarfarinAtEndOfPeriodAllSites.setDrugs(onWarfarinRegimen);
onWarfarinAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onWarfarinAtEndOfPeriodAllSites);

CompositionCohortDefinition hFonWarfarinAtEndOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.setName("hFonWarfarinAtEndOfPeriodCompositionCohortAllSites");
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.getSearches().put("onWarfarinAtEndOfPeriod", new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFonWarfarinAtEndOfPeriodCompositionCohortAllSites.setCompositionString("patientsInHFProgram AND onWarfarinAtEndOfPeriod");
h.replaceCohortDefinition(hFonWarfarinAtEndOfPeriodCompositionCohortAllSites);

CohortIndicator onWarfarinAtEndOfPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onWarfarinAtEndOfPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFonWarfarinAtEndOfPeriodCompositionCohortAllSites , 
		  ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites, 
         ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onWarfarinAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onWarfarinAtEndOfPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onWarfarinAtEndOfPeriodCohortIndicatorAllSites);


// ===================================================================================                       
//   3.1. in the subgroup of cardiomyopathy percent with heart rate >60 not  carvedilol
// =================================================================================== 


 NumericObsCohortDefinition heartRateDiseaseDuringPeriodAllSites=makeNumericObsCohortDefinition("heartRateDiseaseDuringPeriodAllSites", HeartFailureReportConstants.PULSE, 60.0, RangeComparator.GREATER_THAN, TimeModifier.LAST);
 h.replaceCohortDefinition(heartRateDiseaseDuringPeriodAllSites);
 
CompositionCohortDefinition hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.setName("hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites");
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.getSearches().put("cardiomyopathyDiognosisAnsHFProgComposition", new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));  
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.getSearches().put("heartRateDiseaseDuringPeriod", new Mapped<CohortDefinition>(heartRateDiseaseDuringPeriodAllSites,null));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.getSearches().put("onCarvedilolAtEndOfPeriod", new Mapped<CohortDefinition>(onCarvedilolAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites.setCompositionString("cardiomyopathyDiognosisAnsHFProgComposition AND (heartRateDiseaseDuringPeriod AND (NOT onCarvedilolAtEndOfPeriod))");
h.replaceCohortDefinition(hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites);

CohortIndicator onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFCardiomyopathyWithHeartRateOfPeriodCompositionCohortAllSites , 
	   ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(cardiomyopathyDiognosisAnsHFProgCompositionAllSites, 
     ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites);


//===================================================================================                       
//   3.2. in the subgroup of mitral stenosis percent with heart rate >60 not Atenolol
// ===================================================================================  

 CompositionCohortDefinition hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.setName("hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites");
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.getSearches().put("mitralStenosisDiagnosisAnsHFProgComposition", new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));  
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.getSearches().put("heartRateDiseaseDuringPeriod", new Mapped<CohortDefinition>(heartRateDiseaseDuringPeriodAllSites,null));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.getSearches().put("onAtenololAtEndOfPeriod", new Mapped<CohortDefinition>(onAtenololAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
 hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites.setCompositionString("mitralStenosisDiagnosisAnsHFProgComposition AND  (heartRateDiseaseDuringPeriod AND (NOT onAtenololAtEndOfPeriod))");
 h.replaceCohortDefinition(hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites);
 
CohortIndicator onMitralStenosisHeartRatePeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onMitralStenosisHeartRatePeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFMitralStenosisWithHeartRateOfPeriodCompositionCohortAllSites , 
	   ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(mitralStenosisDiagnosisAnsHFProgCompositionAllSites, 
        ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onMitralStenosisHeartRatePeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onMitralStenosisHeartRatePeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onMitralStenosisHeartRatePeriodCohortIndicatorAllSites);

// ===================================================================================                       
//   3.3. in the subgroup of rheumatic heart disease percent not penicillin
// ===================================================================================  


DrugsActiveCohortDefinition onPenicillinAtEndOfPeriodAllSites = new DrugsActiveCohortDefinition();          
onPenicillinAtEndOfPeriodAllSites.setName("onPenicillinAtEndOfPeriodAllSites");
onPenicillinAtEndOfPeriodAllSites.setDrugs(onPenicillinRegimen);
onPenicillinAtEndOfPeriodAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
h.replaceCohortDefinition(onPenicillinAtEndOfPeriodAllSites);

CompositionCohortDefinition hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites = new CompositionCohortDefinition();
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.setName("hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites");
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.getSearches().put("rheumaticHeartDiseaseDiagnosisAndHFProgComposition", new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.getSearches().put("onPenicillinAtEndOfPeriod", new Mapped<CohortDefinition>(onPenicillinAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites.setCompositionString("rheumaticHeartDiseaseDiagnosisAndHFProgComposition AND (NOT onPenicillinAtEndOfPeriod)");
h.replaceCohortDefinition(hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites);

CohortIndicator onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites", new Mapped<CohortDefinition>(hFRheumaticHeartDiseaseOfPeriodCompositionCohortAllSites , 
           ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(rheumaticHeartDiseaseDiagnosisAndHFProgCompositionAllSites, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites);


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
EncounterCohortDefinition hFencounterDuringPeriodAllSites =makeEncounterCohortDefinition(encounterTypes); 
hFencounterDuringPeriodAllSites.setName("hFencounterDuringPeriodAllSites");
hFencounterDuringPeriodAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hFencounterDuringPeriodAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(hFencounterDuringPeriodAllSites);

CompositionCohortDefinition patientNotSeenDuringPeriodCompositionAllSites = new CompositionCohortDefinition();
patientNotSeenDuringPeriodCompositionAllSites.setName("patientNotSeenDuringPeriodCompositionAllSites");
patientNotSeenDuringPeriodCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));                       
patientNotSeenDuringPeriodCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
patientNotSeenDuringPeriodCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
patientNotSeenDuringPeriodCompositionAllSites.getSearches().put("hFencounterDuringPeriod", new Mapped<CohortDefinition>(hFencounterDuringPeriodAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
patientNotSeenDuringPeriodCompositionAllSites.setCompositionString("patientsInHFProgram AND (NOT hFencounterDuringPeriod)");
h.replaceCohortDefinition(patientNotSeenDuringPeriodCompositionAllSites);

CohortIndicator percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites = CohortIndicator.newFractionIndicator("percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites", new Mapped<CohortDefinition>(patientNotSeenDuringPeriodCompositionAllSites,
        ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites,
        ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);      
 percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
 percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
 h.replaceDefinition(percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites);


 // ====================================================================            
 //  4.2. Percent and number of patients not seen in the last 3 months
 // ====================================================================

 CohortIndicator percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites = CohortIndicator.newFractionIndicator("percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites", new Mapped<CohortDefinition>(patientNotSeenDuringPeriodCompositionAllSites,
	        ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m},onOrBefore=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites,
	        ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);      
 percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
 percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
	 h.replaceDefinition(percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites);


//======================================================================	
//	      4.3.    number of patients without an accompagnateur
//======================================================================
	
 SqlCohortDefinition allPatientsWhitAccompagnateurAllSites = new SqlCohortDefinition("SELECT DISTINCT person_b FROM relationship WHERE relationship='1' and date_created<= :endDate and voided=0");
 allPatientsWhitAccompagnateurAllSites.setName("allPatientsWhitAccompagnateurAllSites");
 allPatientsWhitAccompagnateurAllSites.addParameter(new Parameter("endDate","endDate",Date.class));  
 h.replaceCohortDefinition(allPatientsWhitAccompagnateurAllSites);
 
 CompositionCohortDefinition patientWithoutAccompagnateurPeriodCompositionAllSites = new CompositionCohortDefinition();
 patientWithoutAccompagnateurPeriodCompositionAllSites.setName("patientWithoutAccompagnateurPeriodCompositionAllSites");
 patientWithoutAccompagnateurPeriodCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 patientWithoutAccompagnateurPeriodCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 patientWithoutAccompagnateurPeriodCompositionAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
 patientWithoutAccompagnateurPeriodCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
 patientWithoutAccompagnateurPeriodCompositionAllSites.getSearches().put("allPatientsWhitAccompagnateur", new Mapped<CohortDefinition>(allPatientsWhitAccompagnateurAllSites, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
 patientWithoutAccompagnateurPeriodCompositionAllSites.setCompositionString("(patientsInHFProgram AND (NOT allPatientsWhitAccompagnateur)");        
 h.replaceCohortDefinition(patientWithoutAccompagnateurPeriodCompositionAllSites);

 CohortIndicator percentPatientWithoutAccompagnateurIndicatorAllSites = CohortIndicator.newFractionIndicator("percentPatientWithoutAccompagnateurIndicatorAllSites", new Mapped<CohortDefinition>(patientWithoutAccompagnateurPeriodCompositionAllSites,
         ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},endDate=${endDate}")), new Mapped<CohortDefinition>(patientsInHFProgramAllSites,
         ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), null);       
 percentPatientWithoutAccompagnateurIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
 percentPatientWithoutAccompagnateurIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
 h.replaceDefinition(percentPatientWithoutAccompagnateurIndicatorAllSites);

//==================================================
//      5.1. PATIENTS WHO DIED DURING PERIOD 
// ===================================================              

CodedObsCohortDefinition diedDuringPeriodAllSites =makeCodedObsCohortDefinition("diedDuringPeriodAllSites",HeartFailureReportConstants.REASON_FOR_EXITING_CARE, HeartFailureReportConstants.PATIENT_DIED, SetComparator.IN, TimeModifier.LAST);                
diedDuringPeriodAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
diedDuringPeriodAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(diedDuringPeriodAllSites);

CompositionCohortDefinition diedDuringPeriodCompositionAllSites = new CompositionCohortDefinition();
diedDuringPeriodCompositionAllSites.setName("diedDuringPeriodCompositionAllSites");
diedDuringPeriodCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
diedDuringPeriodCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
diedDuringPeriodCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
diedDuringPeriodCompositionAllSites.getSearches().put("diedDuringPeriod", new Mapped<CohortDefinition>(diedDuringPeriodAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
diedDuringPeriodCompositionAllSites.setCompositionString("(patientsInHFProgram AND diedDuringPeriod");

CohortIndicator diedDuringPeriodIndicatorAllSites = new CohortIndicator();
diedDuringPeriodIndicatorAllSites.setName("diedDuringPeriodIndicatorAllSites");
diedDuringPeriodIndicatorAllSites.addParameter(new Parameter("startDate", "startDate",Date.class));
diedDuringPeriodIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
diedDuringPeriodIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(diedDuringPeriodCompositionAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
h.replaceDefinition(diedDuringPeriodIndicatorAllSites);
    


// ===============================================================================                        
//   5.2.  Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR < 2
// ===============================================================================  


NumericObsCohortDefinition INRLTTwoCohortDefinitionAllSites=makeNumericObsCohortDefinition("INRLTTwoCohortDefinitionAllSites", HeartFailureReportConstants.INTERNATIONAL_NORMALIZED_RATIO,2.0, RangeComparator.LESS_THAN, TimeModifier.LAST);
INRLTTwoCohortDefinitionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRLTTwoCohortDefinitionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(INRLTTwoCohortDefinitionAllSites);

CompositionCohortDefinition INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites = new CompositionCohortDefinition();
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.setName("INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites");
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));          		
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.getSearches().put("onWarfarinAtEndOfPeriod", new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.getSearches().put("postCardiacSurgeryCohortDefinition", new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinitionAllSites, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.getSearches().put("INRLTTwoCohortDefinition", new Mapped<CohortDefinition>(INRLTTwoCohortDefinitionAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites.setCompositionString("INRLTTwoCohortDefinition AND postCardiacSurgeryCohortDefinition AND onWarfarinAtEndOfPeriod");
h.replaceCohortDefinition(INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites);

CohortIndicator percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites", new Mapped<CohortDefinition>(INRALTTwondPostCardiacSugeryCompositionCohortDefinitionAllSites,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinitionAllSites,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}")), null);       
percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites);

// ===============================================================================                        
//   5.3. Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR > 4
// ===============================================================================  

NumericObsCohortDefinition INRGTFourCohortDefinitionAllSites=makeNumericObsCohortDefinition("INRGTFourCohortDefinitionAllSites", HeartFailureReportConstants.INTERNATIONAL_NORMALIZED_RATIO,4.0, RangeComparator.GREATER_EQUAL, TimeModifier.LAST);
INRGTFourCohortDefinitionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRGTFourCohortDefinitionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
h.replaceCohortDefinition(INRGTFourCohortDefinitionAllSites);

CompositionCohortDefinition INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites = new CompositionCohortDefinition();
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.setName("INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites");
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("startedOnOrAfter", "startedOnOrAfter", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("startedOnOrBefore", "startedOnOrBefore", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));          		
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.getSearches().put("onWarfarinAtEndOfPeriod", new Mapped<CohortDefinition>(onWarfarinAtEndOfPeriodAllSites,ParameterizableUtil.createParameterMappings("asOfDate=${asOfDate}")));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.getSearches().put("postCardiacSurgeryCohortDefinition", new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinitionAllSites, ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startedOnOrAfter},startedOnOrBefore=${startedOnOrBefore}")));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.getSearches().put("INRGTFourCohortDefinition", new Mapped<CohortDefinition>(INRGTFourCohortDefinitionAllSites, 
		ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites.setCompositionString("INRGTFourCohortDefinition AND postCardiacSurgeryCohortDefinition AND onWarfarinAtEndOfPeriod");
h.replaceCohortDefinition(INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites);

CohortIndicator percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites = CohortIndicator.newFractionIndicator("percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites", new Mapped<CohortDefinition>(INRGTFourAndPostCardiacSugeryCompositionCohortDefinitionAllSites,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},asOfDate=${endDate}")), new Mapped<CohortDefinition>(postCardiacSurgeryCohortDefinitionAllSites,
      ParameterizableUtil.createParameterMappings("startedOnOrAfter=${startDate},startedOnOrBefore=${endDate}")), null);       
percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate", Date.class));
percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
h.replaceDefinition(percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites);



//===============================================================================                 
//5.4. PATIENTS WHO HAD A HOSPITALIZATION IN THE PAST MONTH
//===============================================================================                 

 CodedObsCohortDefinition hospitalizedDuringPeriodAllSites =makeCodedObsCohortDefinition("hospitalizedDuringPeriodAllSites",HeartFailureReportConstants.DISPOSITION, HeartFailureReportConstants.ADMIT_TO_HOSPITAL, SetComparator.IN, TimeModifier.LAST);                
 hospitalizedDuringPeriodAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
 hospitalizedDuringPeriodAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 h.replaceCohortDefinition(hospitalizedDuringPeriodAllSites);


CompositionCohortDefinition hospitalizedDuringPeriodCompositionAllSites = new CompositionCohortDefinition();
hospitalizedDuringPeriodCompositionAllSites.setName("hospitalizedDuringPeriodCompositionAllSites");
hospitalizedDuringPeriodCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
hospitalizedDuringPeriodCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
hospitalizedDuringPeriodCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hospitalizedDuringPeriodCompositionAllSites.getSearches().put("hospitalizedDuringPeriod", new Mapped<CohortDefinition>(hospitalizedDuringPeriodAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
hospitalizedDuringPeriodCompositionAllSites.setCompositionString("(patientsInHFProgram AND hospitalizedDuringPeriod");
h.replaceCohortDefinition(hospitalizedDuringPeriodCompositionAllSites);

 CohortIndicator hospitalizedDuringPeriodIndicatorAllSites = new CohortIndicator();
 hospitalizedDuringPeriodIndicatorAllSites.setName("hospitalizedDuringPeriodIndicatorAllSites");
 hospitalizedDuringPeriodIndicatorAllSites.addParameter(new Parameter("startDate", "startDate",Date.class));
 hospitalizedDuringPeriodIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
 hospitalizedDuringPeriodIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(hospitalizedDuringPeriodCompositionAllSites, 
          ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
h.replaceDefinition(hospitalizedDuringPeriodIndicatorAllSites);

//===============================================================================                   
//
//   6.1.  number of patients without a height ever
//
// ===============================================================================  
        NumericObsCohortDefinition heightCohortDefinitionAllSites=makeNumericObsCohortDefinition("heightCohortDefinitionAllSites", HeartFailureReportConstants.HEIGHT_CM,0.0, RangeComparator.GREATER_EQUAL, TimeModifier.ANY);
        heightCohortDefinitionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        h.replaceCohortDefinition(heightCohortDefinitionAllSites);
        
        CompositionCohortDefinition heightEverCompositionCohortDefinitionAllSites = new CompositionCohortDefinition();
        heightEverCompositionCohortDefinitionAllSites.setName("heightEverCompositionCohortDefinitionAllSites");
        heightEverCompositionCohortDefinitionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        heightEverCompositionCohortDefinitionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        heightEverCompositionCohortDefinitionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
        heightEverCompositionCohortDefinitionAllSites.getSearches().put("heightCohortDefinition", new Mapped<CohortDefinition>(heightCohortDefinitionAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
        heightEverCompositionCohortDefinitionAllSites.setCompositionString("(patientsInHFProgram AND (NOT heightCohortDefinition)");
        h.replaceCohortDefinition(heightEverCompositionCohortDefinitionAllSites);     
        
        CohortIndicator heightEverCohortIndicatorAllSites = new CohortIndicator();
        heightEverCohortIndicatorAllSites.setName("heightEverCohortIndicatorAllSites");
        heightEverCohortIndicatorAllSites.addParameter(new Parameter("startDate", "startDate",Date.class));
        heightEverCohortIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
        heightEverCohortIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(heightEverCompositionCohortDefinitionAllSites, 
                 ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
        h.replaceDefinition(heightEverCohortIndicatorAllSites);
                
// ===============================================================================                 
//  37. NUMBER OF  PATIENTS WITHOUT A DONNE DE BASE 
// =============================================================================== 

  List<Form> donnDeBase=new ArrayList<Form>();
  String donnDeBaseFormid=Context.getAdministrationService().getGlobalProperty("cardiologyreporting.hFDonneDeBaseFormId");
  donnDeBase.add(Context.getFormService().getForm(Integer.valueOf(donnDeBaseFormid)));
	  
  EncounterCohortDefinition encounterFormDuringDDBPeriodAllSites =makeEncounterCohortDefinition("encounterFormDuringDDBPeriodAllSites",donnDeBase);       
  encounterFormDuringDDBPeriodAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
  h.replaceCohortDefinition(encounterFormDuringDDBPeriodAllSites);
   
 CompositionCohortDefinition patientWithoutDonneDebasePeriodCompositionAllSites = new CompositionCohortDefinition();
 patientWithoutDonneDebasePeriodCompositionAllSites.setName("patientWithoutDonneDebasePeriodCompositionAllSites");
 patientWithoutDonneDebasePeriodCompositionAllSites.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));                        
 patientWithoutDonneDebasePeriodCompositionAllSites.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
 patientWithoutDonneDebasePeriodCompositionAllSites.getSearches().put("patientsInHFProgram", new Mapped<CohortDefinition>(patientsInHFProgramAllSites,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
 patientWithoutDonneDebasePeriodCompositionAllSites.getSearches().put("encounterFormDuringDDBPeriod", new Mapped<CohortDefinition>(encounterFormDuringDDBPeriodAllSites, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore}")));
 patientWithoutDonneDebasePeriodCompositionAllSites.setCompositionString("(patientsInHFProgram AND (NOT encounterFormDuringDDBPeriod)");  
 h.replaceCohortDefinition(patientWithoutDonneDebasePeriodCompositionAllSites);   	
    	 
 CohortIndicator patientWithoutDonneDebasePeriodIndicatorAllSites = new CohortIndicator();
 patientWithoutDonneDebasePeriodIndicatorAllSites.setName("patientWithoutDonneDebasePeriodIndicatorAllSites");
 patientWithoutDonneDebasePeriodIndicatorAllSites.addParameter(new Parameter("startDate", "startDate",Date.class));
 patientWithoutDonneDebasePeriodIndicatorAllSites.addParameter(new Parameter("endDate", "endDate", Date.class));
 patientWithoutDonneDebasePeriodIndicatorAllSites.setCohortDefinition(new Mapped<CohortDefinition>(patientWithoutDonneDebasePeriodCompositionAllSites, 
 ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"))); 
 h.replaceDefinition(patientWithoutDonneDebasePeriodIndicatorAllSites);

//Add global filters to the report
        
		rd.addIndicator("1.1.m", "% of Male", percentMaleInFHProgramIndicatorAllSites);
		rd.addIndicator("1.1.f", "% of Female", percentFemaleInFHProgramIndicatorAllSites);
		rd.addIndicator("1.2", "Median Age", medianAgeAllSites);
		rd.addIndicator("1.3", "Number of new patients enrolled in reporting period", patientsEnrolledInHFIndicatorAllSites);
		rd.addIndicator("1.4", "Total number of Patients", patientsInHFIndicatorAllSites);
		
		rd.addIndicator("2.1", "Number and percent of patients without a cardiology consultation", patientsInHFProgramWithouCardFormIndicatorAllSites);
		rd.addIndicator("2.2", "Number and percent of patients without a preliminary echocardiographic diagnosis", hfEchocardiographyPercentageIndicatorAllSites);
		rd.addIndicator("2.3", "Percent without a creatinine in the last 6 months", hfPatientWithoutCreatininePercentIndicatorAllSites);
		rd.addIndicator("2.4", "Number of patients with Cardiomyopathy", cardiomyopathyDiognosisAnsHFProgIndicatorAllSites);
		rd.addIndicator("2.5", "Number of patients with pure mitral stenosis", mitralStenosisDiagnosisAnsHFProgIndicatorAllSites);
		rd.addIndicator("2.6", "Number of patients with other rheumatic heart disease", rheumaticHeartDiseaseDiagnosisAndHFProgIndicatorAllSites);
		rd.addIndicator("2.7", "Number of patients with hypertensive heart disease", hypertensiveHeartDiseaseDiagnosisAndHFProgIndicatorAllSites);
		rd.addIndicator("2.8", "Number of patients with pericardial disease", pericardialDiseaseDiagnosisAndHFProgIndicatorAllSites);
		rd.addIndicator("2.9", "Number of patients with congenital heart disease", congenitalDiseaseDiagnosisAndHFProgIndicatorAllSites);
		rd.addIndicator("2.10", "Percent of patients with creatinine > 200", hfpatientsWithCreatininePercentIndicatorAllSites);
		rd.addIndicator("2.11", "Number of patients in post-cardiac surgery", postCardiacSurgeryCohortIndicatorAllSites);	
	
		rd.addIndicator("3.1", "In the subgroup (cardiomyopathy): percent of Patients with heart rate > 60 at last visit not on carvedilol", onCardiomyopathyHeartRatePeriodCohortIndicatorAllSites);
		rd.addIndicator("3.2", "in the subgroup (mitral stenosis): percent of Patients with heart rate > 60 at last visit not on atenolol", onMitralStenosisHeartRatePeriodCohortIndicatorAllSites);
		rd.addIndicator("3.3", "in the subgroup (rheumatic heart disease): percent not on penicillin",onRheumaticNotOnPenicillinPeriodCohortIndicatorAllSites);		
		rd.addIndicator("3.4", "In the subgroup (female < age 50), percent of patients not on family planning", patientsInHFWithoutFamilyPlanningIndicatorAllSites);
		rd.addIndicator("3.5", "Percent of Patients on lasix", onLasixAtEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.6", "Percent of Patients on atenolol", onAtenololAtEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.7", "Percent of Patients on carvedilol", onCarvedilolAtEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.8", "Percent of Patients on aldactone", onAldactoneAtEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.9", "Percent of Patients on lisinopril or captopril", onLisinoprilOrCaptoprilEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.10", "Percent of Patients on atenolol and carvedilol", onAtenololAndCarvedilolEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.11", "Percent of Patients on lisinopril and captopril", onLisinoprilAndCaptoprilEndOfPeriodCohortIndicatorAllSites);
		rd.addIndicator("3.12", "Percent and number of Patients on warfarin", onWarfarinAtEndOfPeriodCohortIndicatorAllSites);
		
		rd.addIndicator("4.1","Percent and number of patients not seen in the last 6 months",percentageOfpatientNotSeenInLastSixMonthPeriodIndicatorAllSites);
		rd.addIndicator("4.2","Percent and number of patients not seen in the last 3 months",percentageOfpatientNotSeenInLastThreeMonthPeriodIndicatorAllSites);
		rd.addIndicator("4.3","Number of patients without an accompagnateur", percentPatientWithoutAccompagnateurIndicatorAllSites);
		
		rd.addIndicator("5.1", "Number of people who have ever been in the heart failure program who died in report window", diedDuringPeriodIndicatorAllSites);
		rd.addIndicator("5.2", "Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR < 2", percentINRALTTwoPostCardiacSugeryCohortIndicatorAllSites);
		rd.addIndicator("5.3", "Percent of patients in the subgroup (post-cardiac surgery) and on warfarin with INR > 4", percentINRGTTFourPostCardiacSugeryCohortIndicatorAllSites);
		rd.addIndicator("5.4", "Number of hospitalizations in reporting window", hospitalizedDuringPeriodIndicatorAllSites);
		
		rd.addIndicator("6.1", "number of patients without a height ever", heightEverCohortIndicatorAllSites);
		rd.addIndicator("6.2", "number of patients without a donne de base (intake form)", patientWithoutDonneDebasePeriodIndicatorAllSites);

		//rd.setBaseCohortDefinition(h.cohortDefinition("location: Heart Failure Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
	    h.replaceReportDefinition(rd);
		
		return rd;
	}
	
//Methods defining definitions		

private void createLocationCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
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
