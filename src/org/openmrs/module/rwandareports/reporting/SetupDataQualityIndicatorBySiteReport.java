package org.openmrs.module.rwandareports.reporting;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.definition.DrugsActiveCohortDefinition;
import org.openmrs.module.rwandareports.renderer.DataQualityReportWebRenderer;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GetDate;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;

public class SetupDataQualityIndicatorBySiteReport {
	
	Helper h = new Helper();
	protected final Log log = LogFactory.getLog(getClass());
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	
	//properties
	private Program pmtct;	
	private Program pmtctCombinedClinicInfant;	
	private Program pmtctCombinedClinicMother;
	private Program pediHIV;	
	private Program adultHIV;	
	private Program tb;	
	private Program heartFailure;	
    private ProgramWorkflow treatmentStatus;	
	private ProgramWorkflowState adultOnART;	
	private ProgramWorkflowState pediOnART;
	private ProgramWorkflowState PMTCTOnART;
	private List<Concept> artDrugsconcepts;
	private List<Concept> tbDrugsconcepts;
	private List<Concept> tbFirstLineDrugsConcepts;
	private List<Concept> tbSecondLineDrugsConcepts;	
	private PatientIdentifierType imb;
	private PatientIdentifierType primaryCare;
	
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		createReportDefinition();
	}
	
	public void delete() {
		
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Data Quality Web Renderer".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		
		h.purgeReportDefinition("Data Quality Report");
	}
	
	private ReportDefinition createReportDefinition() throws IOException {
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));
		/*rd.addParameter(new Parameter("startDate", "Start Date", Date.class));*/
		//rd.addParameter(new Parameter("endDate", "Date", Date.class));
		
		rd.setName("Data Quality Report");
		
		rd.setupDataSetDefinition();
		
		
		rd.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createIndicators(rd);
		h.saveReportDefinition(rd);
		createCustomWebRenderer(rd,  "Data Quality Web Renderer");
		
		return rd;
	}
	
	private void createIndicators(PeriodIndicatorReportDefinition reportDefinition) {
		
		//====================================================================
		// 1. Any patients who are in Pediatric HIV program or in the Adult HIV program AND on ART whose accompagnateur is not listed in EMR (or who are incorrectly identified as status 'on antiretrovirals')
		//====================================================================
		
		
		
		List<Program> hivPrograms=new ArrayList<Program>();
		hivPrograms.add(pediHIV);
		hivPrograms.add(adultHIV);		
		InProgramCohortDefinition inHIVprogram=Cohorts.createInProgramParameterizableByDate("DQ: inHIVProgram", hivPrograms, "onDate");
		
		List<ProgramWorkflowState> OnARTstates=new ArrayList<ProgramWorkflowState>();
		OnARTstates.add(adultOnART);
		OnARTstates.add(pediOnART);
		InStateCohortDefinition onARTStatusCohort=Cohorts.createInCurrentState("onARTStatus", OnARTstates);
		
		
		SqlCohortDefinition patientsWithAcc=Cohorts.createPatientsWithAccompagnateur("DQ: Patient with accompagnateur", "endDate");
		
		CompositionCohortDefinition patientsInHIVAndOnARTWithoutAccomp = new CompositionCohortDefinition();
		patientsInHIVAndOnARTWithoutAccomp.setName("DQ: In HIV on ART without Accompagnateur");
		patientsInHIVAndOnARTWithoutAccomp.getSearches().put("1",new Mapped(inHIVprogram, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		patientsInHIVAndOnARTWithoutAccomp.getSearches().put("2",new Mapped(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		patientsInHIVAndOnARTWithoutAccomp.getSearches().put("3",new Mapped(patientsWithAcc, ParameterizableUtil.createParameterMappings("endDate=${now}")));
		patientsInHIVAndOnARTWithoutAccomp.setCompositionString("1 AND 2 AND (NOT 3)");		
		
		
		CohortIndicator patientsInHIVOnARTWithoutAccompIndicator = Indicators.newCountIndicator(
			    "hivOnARTWithoutAccompDQ: Number of patients in HIV program on ART and without Accompagnateur", patientsInHIVAndOnARTWithoutAccomp,
			    null);
		
		//======================================================================================
		// 2. Patients enrolled in PMTCT Pregnancy for more than 8 months
		//======================================================================================
		
		
		
		SqlCohortDefinition patientsInPMTCTTooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+pmtct.getName()+"' and pp.date_enrolled<'"+GetDate.getCalendarMonthDate(-8)+"' and pp.voided=false and pp.date_completed is null");
		CohortIndicator patientsInPMTCTTooLongIndicator = Indicators.newCountIndicator(
			    "PMTCTDQ: Number of patients in PMTCT program", patientsInPMTCTTooLong,
			    null);
		

		//======================================================================================
		// 3. Patients enrolled in Combined Clinic Mother for more than 19 months
		//======================================================================================
		
		
		SqlCohortDefinition patientsInPMTCTCCMTooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+pmtctCombinedClinicMother.getName()+"' and pp.date_enrolled<'"+GetDate.getCalendarMonthDate(-19)+"' and pp.voided=false and pp.date_completed is null");
		CohortIndicator patientsInPMTCTCCMTooLongIndicator = Indicators.newCountIndicator(
			    "PMTCTCCMDQ: Number of patients in Combined Clinic Mother program", patientsInPMTCTCCMTooLong,
			    null);		
		
		

		//======================================================================================
		// 4. Patients enrolled in Combined Clinic Infant for more than 19 months
		//======================================================================================
		
		
		SqlCohortDefinition patientsInPMTCTCCITooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+pmtctCombinedClinicInfant.getName()+"' and pp.date_enrolled<'"+GetDate.getCalendarMonthDate(-19)+"' and pp.voided=false and pp.date_completed is null");
		CohortIndicator patientsInPMTCTCCITooLongIndicator = Indicators.newCountIndicator(
			    "PMTCTCCIDQ: Number of patients in Combined Clinic Infant program", patientsInPMTCTCCITooLong,
			    null);			
		
		//======================================================================================
		// 5. In PMTCT-pregnancy or PMTCT Combine Clinic - mother while a 'male' patient
		//======================================================================================
		
		
		List<Program> PMTCTPrograms=new ArrayList<Program>();
		PMTCTPrograms.add(pmtct);
		PMTCTPrograms.add(pmtctCombinedClinicMother);		
		InProgramCohortDefinition inPMTCTPrograms=Cohorts.createInProgramParameterizableByDate("DQ: inHIVProgram", PMTCTPrograms, "onDate");
		
		
		GenderCohortDefinition males=Cohorts.createMaleCohortDefinition("Males patients");
		
		CompositionCohortDefinition malesInPMTCTAndPMTCTCCM = new CompositionCohortDefinition();
		malesInPMTCTAndPMTCTCCM.setName("DQ: Male in PMTCT and PMTCT-combined clinic mother");
		malesInPMTCTAndPMTCTCCM.getSearches().put("1",new Mapped(inPMTCTPrograms, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		malesInPMTCTAndPMTCTCCM.getSearches().put("2",new Mapped(males, null ));
		malesInPMTCTAndPMTCTCCM.setCompositionString("1 AND 2");		
		
		
		
		CohortIndicator malesInPMTCTAndPMTCTCCMIndicator = Indicators.newCountIndicator(
			    "PMTCTCCIDQ: Number of Male patients in PMTCT-Pregnancy and Combined Clinic Mother programs", malesInPMTCTAndPMTCTCCM,
			    null);			
		
		
		//======================================================================================
		// 6. Patients with current ARV regimen with incorrect treatment status (not "on ART)
		//======================================================================================
		//List<Drug> artDrugs=new ArrayList<Drug>();
		String stringOfIdsOfConcepts=null;
		
		for(Concept concept:artDrugsconcepts){
			stringOfIdsOfConcepts=stringOfIdsOfConcepts+","+concept.getId();
			/*List<Drug> drugs=Context.getConceptService().getDrugsByConcept(concept);			
			for(Drug drug:drugs){
				artDrugs.add(drug);
				log.info("Drug: "+drug.getName());
			}*/			
		}
		 
		List<ProgramWorkflowState> OnARTstatesAllPrograms=new ArrayList<ProgramWorkflowState>();
		OnARTstatesAllPrograms.add(adultOnART);
		OnARTstatesAllPrograms.add(pediOnART);
		OnARTstatesAllPrograms.add(PMTCTOnART);
		InStateCohortDefinition onARTStatusAllProgramsCohort=Cohorts.createInCurrentState("onARTStatus", OnARTstatesAllPrograms);
		
		
		SqlCohortDefinition onARTDrugs=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.concept_id in ("+stringOfIdsOfConcepts+") and o.discontinued=0 and auto_expire_date is null and o.voided=0");
		
					
		CompositionCohortDefinition onARTDrugsNotOnARTStatus = new CompositionCohortDefinition();
		onARTDrugsNotOnARTStatus.setName("DQ: patients On ART Drugs Not On ART Status");
		onARTDrugsNotOnARTStatus.getSearches().put("1",new Mapped(onARTDrugs, null));
		onARTDrugsNotOnARTStatus.getSearches().put("2",new Mapped(onARTStatusAllProgramsCohort, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		onARTDrugsNotOnARTStatus.setCompositionString("1 AND (NOT 2)");			
		
		CohortIndicator patientsOnARTRegimenNotOnARTStatus = Indicators.newCountIndicator("Patients with current ARV regimen with incorrect treatment status", onARTDrugsNotOnARTStatus,null);		
		
		
		//======================================================================================
		// 7. Patients with treatment status 'On Antiretrovirals' without an ARV regimen
		//======================================================================================
		
		CompositionCohortDefinition onARTStatusNotOnARTDrugs = new CompositionCohortDefinition();
		onARTStatusNotOnARTDrugs.setName("DQ: patients On ART Status Not On ART Drugs");
		onARTStatusNotOnARTDrugs.getSearches().put("1",new Mapped(onARTDrugs, null));
		onARTStatusNotOnARTDrugs.getSearches().put("2",new Mapped(onARTStatusAllProgramsCohort, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		onARTStatusNotOnARTDrugs.setCompositionString("2 AND (NOT 1)");		
		
		
		CohortIndicator patientsOnARTStatusNotOnARTRegimen = Indicators.newCountIndicator("Patients with treatment status 'On Antiretrovirals' without an ARV regimen", onARTStatusNotOnARTDrugs,null);		
		
		//======================================================================================
		// 8. Patients with current TB regimen not currently in TB program (excluding patients in HF program)
		//======================================================================================
		
		String stringOfIdsOfTbDrugsConcepts=null;
		log.info("##############################: Size of Array Of TB Drug concepts:"+tbDrugsconcepts.size());
		for(Concept concept:tbDrugsconcepts){
			stringOfIdsOfTbDrugsConcepts=stringOfIdsOfTbDrugsConcepts+","+concept.getId();	
			log.info("Drug concept id: "+ concept.getId());
		}
		 
		SqlCohortDefinition onTBDrugs=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.concept_id in ("+stringOfIdsOfTbDrugsConcepts+") and o.discontinued=0 and (auto_expire_date is null or auto_expire_date > :now) and o.voided=0");
		onTBDrugs.addParameter(new Parameter("now","now",Date.class));
		
		List<Program> tbPrograms=new ArrayList<Program>();
		tbPrograms.add(tb);
			
		InProgramCohortDefinition inTBprogram=Cohorts.createInProgramParameterizableByDate("DQ: inTBprogram", tbPrograms, "onDate");
		
		List<Program> hfPrograms=new ArrayList<Program>();
		hfPrograms.add(heartFailure);
		
		InProgramCohortDefinition inHFprogram=Cohorts.createInProgramParameterizableByDate("DQ: inHFprogram", hfPrograms, "onDate");
		
		
		
		
		CompositionCohortDefinition onTBDrugsNotInTBProgHFExcluded = new CompositionCohortDefinition();
		onTBDrugsNotInTBProgHFExcluded.setName("DQ: patients On TB Drugs Not In TB program and HF program excluded");
		onTBDrugsNotInTBProgHFExcluded.getSearches().put("1",new Mapped(onTBDrugs, ParameterizableUtil.createParameterMappings("now=${now}")));
		onTBDrugsNotInTBProgHFExcluded.getSearches().put("2",new Mapped(inTBprogram, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		onTBDrugsNotInTBProgHFExcluded.getSearches().put("3",new Mapped(inHFprogram, ParameterizableUtil.createParameterMappings("onDate=${now}")));
		onTBDrugsNotInTBProgHFExcluded.setCompositionString("NOT (2 OR 3) AND 1");			
		
		CohortIndicator patientsOnTBRegimenNotInTBProgramHFExcluded = Indicators.newCountIndicator("Patients with current TB regimen who are not in TB Program excluding Heart Failure Program", onTBDrugsNotInTBProgHFExcluded,null);		
		
		//======================================================================================
		//  10. Active patients with no IMB or PHC ID
		//======================================================================================
		
		
		String imbPCIdentifiersID=imb.getId()+","+primaryCare.getId();
		List<String> parameterNames=new ArrayList<String>();
		parameterNames.add("onOrAfter");
		parameterNames.add("onOrBefore");
		EncounterCohortDefinition anyEncounter=Cohorts.createEncounterParameterizedByDate("DQ: any encounter", parameterNames);
		
		SqlCohortDefinition patientsWithIMBOrPCIdentifer=new SqlCohortDefinition("select distinct patient_id from patient_identifier where identifier_type in ("+imbPCIdentifiersID+") and voided=0");
		

		CompositionCohortDefinition patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow = new CompositionCohortDefinition();
		patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.setName("DQ: patients without IMB or Primary Care Identifier ids but with any encounter in last year from now");
		patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.getSearches().put("1",new Mapped(anyEncounter, ParameterizableUtil.createParameterMappings("onOrAfter=${now-12m},onOrBefore=${now}")));
		patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.getSearches().put("2",new Mapped(patientsWithIMBOrPCIdentifer, null));
		patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.setCompositionString("1 AND (NOT 2)");
		
		CohortIndicator patientsWithIMBOrPCIdentiferanyEncounterLastYearFromNowIndicator = Indicators.newCountIndicator("patients without IMB or Primary Care Identifier ids but with any encounter in last year from now", patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow,null);		
		


		//======================================================================================
		// 12. On initial TB treatment for longer than 8 months
		//======================================================================================
		
		SqlCohortDefinition patientsInTBTooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+tb.getName()+"' and pp.date_enrolled<'"+GetDate.getCalendarMonthDate(-8)+"' and pp.voided=false and pp.date_completed is null");
		
		String tbFirstLineDrugsConceptIds=null;
		
		for(Concept concept:tbFirstLineDrugsConcepts){
			
			tbFirstLineDrugsConceptIds=tbFirstLineDrugsConceptIds+","+concept.getId();
			
		}		
		
		SqlCohortDefinition onTBFirstLineDrugs=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.concept_id in ("+tbFirstLineDrugsConceptIds+") and o.discontinued=0 and (auto_expire_date is null or auto_expire_date > :now) and o.voided=0");
		
        String tbFirstSecondDrugsConceptIds=null;
		
		for(Concept concept:tbSecondLineDrugsConcepts){
			
			tbFirstSecondDrugsConceptIds=tbFirstSecondDrugsConceptIds+","+concept.getId();
			
		}
		
		SqlCohortDefinition onTBSecondLineDrugs=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.concept_id in ("+tbFirstSecondDrugsConceptIds+") and o.discontinued=0 and (auto_expire_date is null or auto_expire_date > :now) and o.voided=0");
		
		
		CompositionCohortDefinition patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen = new CompositionCohortDefinition();
		patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen.setName("DQ: patients In TB Program Too long on First Line Regimen and Not on Second Line regimen");
		patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen.getSearches().put("1",new Mapped(patientsInTBTooLong, null));
		patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen.getSearches().put("2",new Mapped(onTBFirstLineDrugs, null));
		patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen.getSearches().put("3",new Mapped(onTBSecondLineDrugs, null));
		patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen.setCompositionString("1 AND 2 AND (NOT 3)");
		
		
		
		CohortIndicator patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimenIndicator = Indicators.newCountIndicator(
			    "PMTCTDQ: Number patients In TB Program Too long on First Line Regimen and Not on Second Line regimen", patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimen,
			    null);
		//======================================================================================
		//  13. Patients over 100 years old
		//======================================================================================
		
		AgeCohortDefinition patientsOver100Yearsold=new AgeCohortDefinition(100,null,null);
		
		CohortIndicator patientsOver100YearsoldIndicator = Indicators.newCountIndicator(
			    "PMTCTDQ: Number patients Over 100 years old", patientsOver100Yearsold,
			    null);

		
		//======================================================================================
		// 14. Patients with a visit in last 12 months who do not have a correctly structured address
		//======================================================================================
		
		
		SqlCohortDefinition patientsWithNoStructuredAddress=new SqlCohortDefinition("select distinct(p.patient_id) from patient p,person_address pa where p.patient_id=pa.person_id and p.voided=0 and (pa.state_province is null or pa.county_district is null or pa.city_village is null or pa.neighborhood_cell is null or pa.address1 is null)");
		
		CompositionCohortDefinition patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow = new CompositionCohortDefinition();
		patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.setName("DQ: patients With No Structured Address and with any encounter in last year from now");
		patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.getSearches().put("1",new Mapped(anyEncounter, ParameterizableUtil.createParameterMappings("onOrAfter=${now-12m},onOrBefore=${now}")));
		patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.getSearches().put("2",new Mapped(patientsWithNoStructuredAddress, null));
		patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.setCompositionString("1 AND 2");
		
		CohortIndicator patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNowIndicator = Indicators.newCountIndicator("Number of patients With No Structured Address and with any encounter in last year from now", patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow,null);		
		
		
		//======================================================================================
		//  Add global filters to the report
		//======================================================================================
		
		reportDefinition.addIndicator("1", "patients who are in Pediatric and Adult HIV program AND on ART whose accompagnateur is not listed in EMR", patientsInHIVOnARTWithoutAccompIndicator);		
		reportDefinition.addIndicator("2", "Patients enrolled in PMTCT Pregnancy for more than 8 months", patientsInPMTCTTooLongIndicator);
		reportDefinition.addIndicator("3", "Patients enrolled in Combined Clinic Mother for more than 19 months", patientsInPMTCTCCMTooLongIndicator);
		reportDefinition.addIndicator("4", "Patients enrolled in Combined Clinic Infant for more than 19 months", patientsInPMTCTCCITooLongIndicator);
		reportDefinition.addIndicator("5", "Patients in PMTCT-pregnancy or PMTCT Combine Clinic - mother while a 'male' patient", malesInPMTCTAndPMTCTCCMIndicator);
		reportDefinition.addIndicator("6","Patients with current ARV regimen with incorrect treatment status(not 'On ART')",patientsOnARTRegimenNotOnARTStatus);
		reportDefinition.addIndicator("7","Patients with treatment status 'On Antiretrovirals' without an ARV regimen",patientsOnARTStatusNotOnARTRegimen);
		reportDefinition.addIndicator("8","Patients with current TB regimen not currently in TB program (excluding patients in HF program)",patientsOnTBRegimenNotInTBProgramHFExcluded);
		reportDefinition.addIndicator("10","Active patients with no IMB or PHC ID",patientsWithIMBOrPCIdentiferanyEncounterLastYearFromNowIndicator);
		reportDefinition.addIndicator("12","On initial TB treatment for longer than 8 months",patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimenIndicator);
		reportDefinition.addIndicator("13","Patients over 100 years old",patientsOver100YearsoldIndicator);
		reportDefinition.addIndicator("14","Patients with a visit in last 12 months who do not have a correctly structured address",patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNowIndicator);

	}
	
	private void setUpProperties() {
		pmtct=gp.getProgram(GlobalPropertiesManagement.PMTCT);
		pmtctCombinedClinicInfant=gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);	
		pmtctCombinedClinicMother=gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		pediHIV=gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);	
	    adultHIV=gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);	
		tb=gp.getProgram(GlobalPropertiesManagement.TB_PROGRAM);
		heartFailure=gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM);
		adultOnART = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediOnART=gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		PMTCTOnART=gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT);
		
		artDrugsconcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.ART_DRUGS_SET);
		//tbDrugsconcepts=Context.getConceptService().getConceptsByConceptSet(gp.getConcept(GlobalPropertiesManagement.TB_TREATMENT_DRUGS));
		tbDrugsconcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.TB_TREATMENT_DRUGS);
		tbFirstLineDrugsConcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.TB_FIRST_LINE_DRUG_SET);
		tbSecondLineDrugsConcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.TB_SECOND_LINE_DRUG_SET);
		
		imb=gp.getPatientIdentifier(GlobalPropertiesManagement.IMB_IDENTIFIER);
		primaryCare=gp.getPatientIdentifier(GlobalPropertiesManagement.PC_IDENTIFIER);
	}
	
	private void createCustomWebRenderer(ReportDefinition rd, String name) throws IOException {
    	final ReportDesign design = new ReportDesign();
    	design.setName(name);
    	design.setReportDefinition(rd);
    	design.setRendererType(DataQualityReportWebRenderer.class);
    	
    	ReportService rs = Context.getService(ReportService.class);
    	rs.saveReportDesign(design);
    }
	
}
