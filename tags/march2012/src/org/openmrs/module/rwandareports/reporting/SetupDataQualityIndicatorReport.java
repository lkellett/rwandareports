package org.openmrs.module.rwandareports.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.renderer.DataQualityReportWebRenderer;
import org.openmrs.module.rwandareports.renderer.DataQualityWebRendererForSites;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GetDate;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;

public class SetupDataQualityIndicatorReport {
	
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
	private Program dmprogram;	
	private Program nutritionpro;
	private Program chronicrespiratory;	
	private Program hypertention;	
	private Program epilepsy;
	private ProgramWorkflowState adultOnART;	
	private ProgramWorkflowState pediOnART;
	private ProgramWorkflowState PMTCTOnART;
	private ProgramWorkflowState diedinAdult;
	private ProgramWorkflowState diedinAdultgroup;
	private ProgramWorkflowState diedInPedi;
	private ProgramWorkflowState diedInTb;
	private ProgramWorkflowState diedInTbgroup;
	private ProgramWorkflowState diedInNutri;
	private ProgramWorkflowState diedInPmtct;
	private ProgramWorkflowState diedInPmtctgroup;
	private ProgramWorkflowState diedInHf;
	private ProgramWorkflowState diedInDiab;
	private ProgramWorkflowState diedInChr;
	private ProgramWorkflowState diedInHyp;
	private ProgramWorkflowState diedInEpil;
	private List<Concept> tbFirstLineDrugsConcepts;
	private List<Concept> tbSecondLineDrugsConcepts;	
	private Concept reasonForExitingCare;
    private Concept transferOut;
    private Concept height;
	private Concept weight;
    private List<String> onOrAfterOnOrBeforeParamterNames = new ArrayList<String>();
   
	public void setup() throws Exception {
		
		setUpProperties();
		
		createReportDefinition();
		createReportDefinitionAllSites();
	}
	
	public void delete() {
		
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
		if ("DataQualityWebRenderer".equals(rd.getName())||"DataWebRenderer".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		
		h.purgeReportDefinition("Data Quality Report By Site");
		h.purgeReportDefinition("Data Quality Report For All Sites");
	}
	
	// DQ Report by Site
	private ReportDefinition createReportDefinition() throws IOException {
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));
		
		rd.setName("Data Quality Report By Site");
		
		
		rd.setupDataSetDefinition();
		
		
		rd.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createIndicatorsForReports(rd);
		h.saveReportDefinition(rd);
		createCustomWebRenderer(rd, "DataQualityWebRenderer");
		
		return rd;
	}
	
	// DQ Report for all sites
				private ReportDefinition createReportDefinitionAllSites() throws IOException {
					
					PeriodIndicatorReportDefinition rdsites = new PeriodIndicatorReportDefinition();
					rdsites.removeParameter(ReportingConstants.START_DATE_PARAMETER);
					rdsites.removeParameter(ReportingConstants.END_DATE_PARAMETER);
					rdsites.removeParameter(ReportingConstants.LOCATION_PARAMETER);
					rdsites.setName("Data Quality Report For All Sites");
					
					rdsites.setupDataSetDefinition();
					
					createIndicatorsForReports(rdsites);
					h.saveReportDefinition(rdsites);
					createCustomWebRendererForSites(rdsites, "DataWebRenderer");
					
					return rdsites;
				}
		
		@SuppressWarnings({ "unchecked", "rawtypes"})
		private void createIndicatorsForReports(PeriodIndicatorReportDefinition reportDefinition) {
			//======================================================================================================================================================================================================
			// 1. Any patients who are in Pediatric HIV program or in the Adult HIV program AND on ART whose accompagnateur is not listed in EMR (or who are incorrectly identified as status 'on antiretrovirals')
			//======================================================================================================================================================================================================
			
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
			
			
			
			SqlCohortDefinition patientsInPMTCTTooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+pmtct.getName()+"' and DATEDIFF(CURDATE(),pp.date_enrolled) > "+gp.EIGHT_MONTHS+" and pp.voided=false and pp.date_completed is null");
			CohortIndicator patientsInPMTCTTooLongIndicator = Indicators.newCountIndicator(
				    "PMTCTDQ: Number of patients in PMTCT program", patientsInPMTCTTooLong,       
				    null);
					                                                                                                             		            
			//======================================================================================     
			// 3. Patients enrolled in Combined Clinic Mother for more than 19 months
			//======================================================================================
			
			
			SqlCohortDefinition patientsInPMTCTCCMTooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+pmtctCombinedClinicMother.getName()+"' and DATEDIFF(CURDATE(),pp.date_enrolled) > "+gp.NINETEEN_MONTHS+" and pp.voided=false and pp.date_completed is null");
			CohortIndicator patientsInPMTCTCCMTooLongIndicator = Indicators.newCountIndicator(
				    "PMTCTCCMDQ: Number of patients in Combined Clinic Mother program", patientsInPMTCTCCMTooLong,
				    null);		
			
			

			//======================================================================================
			// 4. Patients enrolled in Combined Clinic Infant for more than 19 months
			//======================================================================================
			
			
			SqlCohortDefinition patientsInPMTCTCCITooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+pmtctCombinedClinicInfant.getName()+"' and DATEDIFF(CURDATE(),pp.date_enrolled) > "+gp.NINETEEN_MONTHS+" and pp.voided=false and pp.date_completed is null");
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
		 
			List<ProgramWorkflowState> OnARTstatesAllPrograms=new ArrayList<ProgramWorkflowState>();
			OnARTstatesAllPrograms.add(adultOnART);
			OnARTstatesAllPrograms.add(pediOnART);
			OnARTstatesAllPrograms.add(PMTCTOnART);
			InStateCohortDefinition onARTStatusAllProgramsCohort=Cohorts.createInCurrentState("onARTStatus", OnARTstatesAllPrograms);
			
			SqlCohortDefinition onARTDrugs=Cohorts.getArtDrugs("On Art Drugs ever");
						
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
			
			//===================================================================================================
			// 8. Patients with current TB regimen not currently in TB program (excluding patients in HF program)
			//===================================================================================================
		
			SqlCohortDefinition onTBDrugs=Cohorts.getTbDrugs("DQ:on Tb Drugs ever");
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
			// 9. Patients with imb invalid identifier type
			//======================================================================================
			
			
			SqlCohortDefinition imbIds=Cohorts.getIMBId("DQ:IMB IDs");
			SqlCohortDefinition pciIds=Cohorts.getPciId("DQ: PCI IDs");
			SqlCohortDefinition patswithInvalidImb=Cohorts.getInvalidIMB("DQ: patients with invalid IMB");
		
			CompositionCohortDefinition patientsWithInvalidIdsnotWIthImbOrPciIds = new CompositionCohortDefinition();
			patientsWithInvalidIdsnotWIthImbOrPciIds.setName("DQ: Invalids but no IMB or PCI IDs");
			patientsWithInvalidIdsnotWIthImbOrPciIds.getSearches().put("1",new Mapped(patswithInvalidImb, null));
			patientsWithInvalidIdsnotWIthImbOrPciIds.getSearches().put("2",new Mapped(imbIds, null));
			patientsWithInvalidIdsnotWIthImbOrPciIds.getSearches().put("3",new Mapped(pciIds, null));
			patientsWithInvalidIdsnotWIthImbOrPciIds.setCompositionString("NOT (2 OR 3) AND 1");
		
			CohortIndicator patientsWithInvalidIdInd = Indicators.newCountIndicator("patients with invalid id check digit", patientsWithInvalidIdsnotWIthImbOrPciIds,null);		
			
			//======================================================================================
			//  10. Active patients with no IMB or PHC ID
			//======================================================================================
			
			
			List<String> parameterNames=new ArrayList<String>();
			parameterNames.add("onOrAfter");
			parameterNames.add("onOrBefore");
			EncounterCohortDefinition anyEncounter=Cohorts.createEncounterParameterizedByDate("DQ: any encounter", parameterNames);
		
			CompositionCohortDefinition patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow = new CompositionCohortDefinition();
			patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.setName("DQ: patients without IMB or Primary Care Identifier ids but with any encounter in last year from now");
			patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.getSearches().put("1",new Mapped(anyEncounter, ParameterizableUtil.createParameterMappings("onOrAfter=${now-12m},onOrBefore=${now}")));
			patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.getSearches().put("2",new Mapped(imbIds, null));
			patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.getSearches().put("3",new Mapped(pciIds, null));
			patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow.setCompositionString("NOT (2 OR 3) AND 1");
			
			CohortIndicator patientsWithIMBOrPCIdentiferanyEncounterLastYearFromNowIndicator = Indicators.newCountIndicator("patients without IMB or Primary Care Identifier ids but with any encounter in last year from now", patientsWithoutIMBOrPCIdentiferWithAnyEncounterLastYearFromNow,null);		
		
			//======================================================================================
			// 11. Observations in the future (except return visit date)
			//======================================================================================
			
			SqlCohortDefinition patientsWithObsgreaterThanEnc=new SqlCohortDefinition("select o.person_id from obs o inner join encounter enc where enc.encounter_id=o.encounter_id and o.person_id=enc.patient_id and o.obs_datetime > enc.encounter_datetime and o.voided=0 and o.void_reason is null");
			
			CohortIndicator patientsWithObsgreaterThanEncIndi = Indicators.newCountIndicator("Observations in the future", patientsWithObsgreaterThanEnc, null);
			//======================================================================================
			// 12. On initial TB treatment for longer than 8 months
			//======================================================================================
			
			SqlCohortDefinition patientsInTBTooLong=new SqlCohortDefinition("select distinct patient_id from patient_program pp,program p where pp.program_id=p.program_id and p.name='"+tb.getName()+"' and pp.date_enrolled< DATE_SUB(pp.date_enrolled,INTERVAL 8 MONTH) and pp.voided=false and pp.date_completed is null");
			
			String tbFirstLineDrugsConceptIds=null;
			
			for(Concept concept:tbFirstLineDrugsConcepts){
				
				tbFirstLineDrugsConceptIds=tbFirstLineDrugsConceptIds+","+concept.getId();
				
			}		
			
			SqlCohortDefinition onTBFirstLineDrugs=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.concept_id in ("+tbFirstLineDrugsConceptIds+") and o.discontinued=0 and (auto_expire_date is null or auto_expire_date > now()) and o.voided=0");
			
	        String tbFirstSecondDrugsConceptIds=null;
			
			for(Concept concept:tbSecondLineDrugsConcepts){
				
				tbFirstSecondDrugsConceptIds=tbFirstSecondDrugsConceptIds+","+concept.getId();
				
			}
			
			SqlCohortDefinition onTBSecondLineDrugs=new SqlCohortDefinition("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id and c.concept_id in ("+tbFirstSecondDrugsConceptIds+") and o.discontinued=0 and (auto_expire_date is null or auto_expire_date > now()) and o.voided=0");
			
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
			
			SqlCohortDefinition patientsWithNoStructuredAddress=new SqlCohortDefinition("select distinct(p.patient_id) from patient p,person_address pa " +
					"where p.patient_id=pa.person_id and pa.preferred=1 and p.voided=0 and pa.voided=0 " +
					"and (pa.state_province is null or pa.county_district is null or pa.city_village is null or pa.address3 is null or pa.address1 is null " +
					"or pa.state_province='' or pa.county_district='' or pa.address3 is null or pa.address1='' )");
			
			CompositionCohortDefinition patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow = new CompositionCohortDefinition();
			patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.setName("DQ: patients With No Structured Address and with any encounter in last year from now");
			patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.getSearches().put("1",new Mapped(anyEncounter, ParameterizableUtil.createParameterMappings("onOrAfter=${now-12m},onOrBefore=${now}")));
			patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.getSearches().put("2",new Mapped(patientsWithNoStructuredAddress, null));
			patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow.setCompositionString("1 AND 2");
			
			CohortIndicator patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNowIndicator = Indicators.newCountIndicator("Number of patients With No Structured Address and with any encounter in last year from now", patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNow,null);		
		
			//======================================================================================
			// 15. Patients whose status status 'deceased' but enrolled in program
			//======================================================================================
		
           //Patients with died state in all programs
			List<ProgramWorkflowState> diedStates=new ArrayList<ProgramWorkflowState>();
			diedStates.add(diedinAdult);
			diedStates.add(diedinAdultgroup);
			diedStates.add(diedInPedi);
			diedStates.add(diedInTb);
			diedStates.add(diedInTbgroup);
			diedStates.add(diedInNutri);
			diedStates.add(diedInPmtct);
			diedStates.add(diedInPmtctgroup);
			diedStates.add(diedInHf);
			diedStates.add(diedInDiab);
			diedStates.add(diedInChr);
			diedStates.add(diedInHyp);
			diedStates.add(diedInEpil);
			InStateCohortDefinition diedStateInAllProgramsCohort=Cohorts.createInCurrentState("diedState", diedStates);
			
			// died but still active in programs
			List<Program> inAllPrograms=new ArrayList<Program>();
			inAllPrograms.add(pediHIV);
			inAllPrograms.add(adultHIV);
			inAllPrograms.add(tb);
			inAllPrograms.add(nutritionpro);
			inAllPrograms.add(pmtct);
			inAllPrograms.add(heartFailure);
			inAllPrograms.add(dmprogram);
			inAllPrograms.add(chronicrespiratory);
			inAllPrograms.add(hypertention);
			inAllPrograms.add(epilepsy);
			InProgramCohortDefinition enrolledInAllPrograms=Cohorts.createInProgramParameterizableByDate("DQ: enrolledInAllPrograms", inAllPrograms, "onDate");
			
			CompositionCohortDefinition patientExitedfromcareinPrograms = new CompositionCohortDefinition();
			patientExitedfromcareinPrograms.setName("DQ: Exited from care in All Programs ");
			patientExitedfromcareinPrograms.getSearches().put("1",new Mapped(enrolledInAllPrograms, ParameterizableUtil.createParameterMappings("onDate=${now}")));
			patientExitedfromcareinPrograms.getSearches().put("2",new Mapped(diedStateInAllProgramsCohort, ParameterizableUtil.createParameterMappings("onDate=${now}")));
			patientExitedfromcareinPrograms.setCompositionString("1 AND 2");		
			CohortIndicator patientExitedfromcareinProgramsIndicator = Indicators.newCountIndicator("Number of patients With status decease but still enrolled in their programs", patientExitedfromcareinPrograms,null);		
			
			//======================================================================================
			// 16. Patients who status is transferred out but is currently enrolled in program
			//======================================================================================
			
			CodedObsCohortDefinition patientsTransferredOut = Cohorts.createCodedObsCohortDefinition("patientsTransferredOut",
				    onOrAfterOnOrBeforeParamterNames, reasonForExitingCare, transferOut, SetComparator.IN, TimeModifier.LAST);
		
			CompositionCohortDefinition patientTransferedOutinPrograms = new CompositionCohortDefinition();
			patientTransferedOutinPrograms.setName("DQ: Transfered out in All Programs ");
			patientTransferedOutinPrograms.getSearches().put("1",new Mapped(enrolledInAllPrograms, ParameterizableUtil.createParameterMappings("onDate=${now}")));
			patientTransferedOutinPrograms.getSearches().put("2",new Mapped(patientsTransferredOut, ParameterizableUtil.createParameterMappings("onOrBefore=${now}")));
			patientTransferedOutinPrograms.setCompositionString("1 AND 2");		
			CohortIndicator patientTransferedOutinProgramsIndicator = Indicators.newCountIndicator("Number of patients Transfered out but still enrolled in their programs", patientTransferedOutinPrograms,null);		
			
			//======================================================================================
			// 17. Patients with no health center
			//======================================================================================
			
			PersonAttributeCohortDefinition pihHealthCenter = new PersonAttributeCohortDefinition();
			pihHealthCenter.setName("Patients at Health Center");
			pihHealthCenter.setAttributeType(Context.getPersonService().getPersonAttributeTypeByName("Health Center"));
			
			InverseCohortDefinition patientsWithoutHc=new InverseCohortDefinition(pihHealthCenter);
			patientsWithoutHc.setName("patientsWithoutHc");
			
			CohortIndicator patientWithnohealthCenterIndicator = Indicators.newCountIndicator("Number of patients without HC", patientsWithoutHc,null);		
		
			//======================================================================================
			// 18. Patients with no encounter
			//======================================================================================
			
			CompositionCohortDefinition patientsWithNoEncounterInProgram = new CompositionCohortDefinition();
			patientsWithNoEncounterInProgram.setName("DQ: patients with no encounter in programs");
			patientsWithNoEncounterInProgram.getSearches().put("1",new Mapped(anyEncounter, ParameterizableUtil.createParameterMappings("onOrBefore=${now}")));
			patientsWithNoEncounterInProgram.getSearches().put("2",new Mapped(enrolledInAllPrograms, ParameterizableUtil.createParameterMappings("onDate=${now}")));;
			patientsWithNoEncounterInProgram.setCompositionString("2 AND (NOT 1)");
			
			CohortIndicator patientsWithNoEncounterInProgramIndicator = Indicators.newCountIndicator("Number with no encounter", patientsWithNoEncounterInProgram,null);		

			//======================================================================================
			// 19. Patients with a BMI <12  or  >35
			//======================================================================================
			 
			 SqlCohortDefinition bmilow=new SqlCohortDefinition();
			 bmilow.setName("bmilow");
			 bmilow.setQuery("select w.person_id from " +
			 	"(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.concept_id='"+height.getId()+"' " +
			 	"order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h," +
			 	"(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.concept_id='"+weight.getId()+"' " +
			 	"order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w " +
			 	"where w.person_id=h.person_id and ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)<12.0");
				
			 SqlCohortDefinition bmihight=new SqlCohortDefinition();
			 bmihight.setName("bmihight");
			 bmihight.setQuery("select w.person_id from " +
			 	"(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.concept_id='"+height.getId()+"' " +
			 	"order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h," +
			 	"(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.concept_id='"+weight.getId()+"' " +
			 	"order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w " +
			 	"where w.person_id=h.person_id ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)>35.0");
			 
			 SqlCohortDefinition patientWithLowBMI=new SqlCohortDefinition();
			 patientWithLowBMI.setName("patientWithLowBMI");
			 patientWithLowBMI.setQuery("select w.person_id from (select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"+Context.getAdministrationService().getGlobalProperty("rwandamohreports.hivclinicalreport.HEIGHTConceptuuid")+"' order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h,(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"+Context.getAdministrationService().getGlobalProperty("rwandamohreports.hivclinicalreport.WEIGHTConceptuuid")+"' order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w where w.person_id=h.person_id and ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)<12.0");
			
			 
			 SqlCohortDefinition patientWithHighBMI=new SqlCohortDefinition();
			 patientWithHighBMI.setName("patientWithHighBMI");
			 patientWithHighBMI.setQuery("select w.person_id from (select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"+Context.getAdministrationService().getGlobalProperty("rwandamohreports.hivclinicalreport.HEIGHTConceptuuid")+"' order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h,(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"+Context.getAdministrationService().getGlobalProperty("rwandamohreports.hivclinicalreport.WEIGHTConceptuuid")+"' order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w where w.person_id=h.person_id and ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)>35.0");
			
			 CompositionCohortDefinition bmimoreorless = new CompositionCohortDefinition();
			 bmimoreorless.setName("bmimoreorless");
			 bmimoreorless.getSearches().put("1",new Mapped(patientWithLowBMI, null));
			 bmimoreorless.getSearches().put("2",new Mapped(patientWithHighBMI, null));;
			 bmimoreorless.setCompositionString("1 OR 2");
				
			
			CohortIndicator patientsWithBMIMoreThan35 = Indicators.newCountIndicator("BMI >15", patientWithLowBMI,null);		
	
			//======================================================================================
			// 20. Patients <15 in Adult HIV program or PMTCT-combined clinic mother or PMTCT pregnancy
			//======================================================================================
			
				
			SqlCohortDefinition patientsOnArtbeforeHivEnrollment=new SqlCohortDefinition();
			patientsOnArtbeforeHivEnrollment.setName("");
			patientsOnArtbeforeHivEnrollment.setQuery("SELECT pp.patient_id " +
						"FROM ( SELECT pp.patient_id a, pp.patient_program_id b, pws.program_workflow_state_id c, " +
						"group_concat(ps.patient_state_id order by ps.patient_state_id desc) d " +
						"FROM patient_program pp, program_workflow pw, program_workflow_state pws, patient_state ps " +
						"WHERE pp.program_id = pw.program_id AND pw.program_workflow_id = pws.program_workflow_id " +
						"AND pws.program_workflow_state_id = ps.state AND ps.patient_program_id = pp.patient_program_id " +
						"AND pw.concept_id="+GlobalPropertiesManagement.TREATMENT_STATUS_ID+" and pws.concept_id="+GlobalPropertiesManagement.ON_ART_TREATMENT_STATUS_ID+" AND pw.retired = 0 AND pp.voided = 0 AND ps.voided = 0 " +
						"GROUP BY pp.patient_id, pp.patient_program_id) most_recent_state, patient_program pp, program_workflow pw, program_workflow_state pws, patient_state ps " +
						"WHERE most_recent_state.d=ps.patient_state_id AND pp.program_id = pw.program_id " +
						"AND pw.program_workflow_id = pws.program_workflow_id AND pws.program_workflow_state_id = ps.state " +
						"AND ps.patient_program_id = pp.patient_program_id AND ps.start_date < pp.date_enrolled");
			
			SqlCohortDefinition patientswithouttransferInForm=new SqlCohortDefinition();
			patientswithouttransferInForm.setName("patientswithouttransferInForm");
			patientswithouttransferInForm.setQuery(" SELECT en.patient_id FROM encounter en, form f WHERE f.form_id=en.form_id AND en.encounter_type=f.encounter_type AND f.form_id=132 AND en.encounter_type=28 " );
			
			 CompositionCohortDefinition patientsWithinvaliddatesandmissingforms= new CompositionCohortDefinition();
			 patientsWithinvaliddatesandmissingforms.setName("DQ: patients with invalid dates and missing transfer in form");
			 patientsWithinvaliddatesandmissingforms.getSearches().put("1",new Mapped(patientsOnArtbeforeHivEnrollment, null));
			 patientsWithinvaliddatesandmissingforms.getSearches().put("2",new Mapped(patientswithouttransferInForm, null));;
			 patientsWithinvaliddatesandmissingforms.setCompositionString("1 AND (NOT 2)");
				
			CohortIndicator patientsOnArtbeforeHivEnrollmentIndicator = Indicators.newCountIndicator("Number of invalid dates and forms", patientsWithinvaliddatesandmissingforms,null);	
		
		// end of DQ applied to all sites	
				
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
		reportDefinition.addIndicator("9","Patients with invalid IMB ID",patientsWithInvalidIdInd);
		reportDefinition.addIndicator("10","Active patients with no IMB or PHC ID",patientsWithIMBOrPCIdentiferanyEncounterLastYearFromNowIndicator);
		reportDefinition.addIndicator("11","Observations in the future (except return visit date)",patientsWithObsgreaterThanEncIndi);
		reportDefinition.addIndicator("12","On initial TB treatment for longer than 8 months",patientsInTBTooLongOnFirstLineRegimenNotSecondLineRegimenIndicator);
		reportDefinition.addIndicator("13","Patients over 100 years old",patientsOver100YearsoldIndicator);
		reportDefinition.addIndicator("14","Patients with a visit in last 12 months who do not have a correctly structured address",patientsWithNoStructuredAddressWithAnyEncounterLastYearFromNowIndicator);
		reportDefinition.addIndicator("15","Patients whose status deceased but enrolled in program",patientExitedfromcareinProgramsIndicator);
		reportDefinition.addIndicator("16","Patients who status is transferred out but is currently enrolled in program ",patientTransferedOutinProgramsIndicator);
		reportDefinition.addIndicator("17","Patients with no health center",patientWithnohealthCenterIndicator);
		reportDefinition.addIndicator("18","Patients in a program with no encounter",patientsWithNoEncounterInProgramIndicator);
    	reportDefinition.addIndicator("19","Patients With BMI <12 or >35",patientsWithBMIMoreThan35);
		reportDefinition.addIndicator("20","Patients whose ART start date or 'on ART' workflow are before any programs began AND do not have a 'transfer in form' ",patientsOnArtbeforeHivEnrollmentIndicator);
		
	}
	
	private void setUpProperties() {
		pmtct=gp.getProgram(GlobalPropertiesManagement.PMTCT);
		pmtctCombinedClinicInfant=gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);	
		pmtctCombinedClinicMother=gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		pediHIV=gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);	
	    adultHIV=gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);	
		tb=gp.getProgram(GlobalPropertiesManagement.TB_PROGRAM);
		heartFailure=gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM);
		dmprogram=gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
		nutritionpro=gp.getProgram(GlobalPropertiesManagement.NUTRITION_PROGRAM);
		chronicrespiratory=gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
		hypertention=gp.getProgram(GlobalPropertiesManagement.HYPERTENTION_PROGRAM);
		epilepsy=gp.getProgram(GlobalPropertiesManagement.EPILEPSY_PROGRAM);
		adultOnART = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediOnART=gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		PMTCTOnART=gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT);
		tbFirstLineDrugsConcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.TB_FIRST_LINE_DRUG_SET);
		tbSecondLineDrugsConcepts=gp.getConceptsByConceptSet(GlobalPropertiesManagement.TB_SECOND_LINE_DRUG_SET);
		reasonForExitingCare=gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);
		transferOut=gp.getConcept(GlobalPropertiesManagement.TRASNFERED_OUT);
		diedinAdult=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
				GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		diedinAdultgroup=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
				GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		diedInPedi=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		diedInTb=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.TB_PROGRAM);
		diedInTbgroup=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TB_TREATMENT_GROUP_WORKFLOW, GlobalPropertiesManagement.TB_PROGRAM);
		diedInNutri=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.ASSISTANCE_STATUS_WORKFLOW, GlobalPropertiesManagement.NUTRITION_PROGRAM);
		diedInPmtct=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		diedInPmtctgroup=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.PREGNANCY_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		diedInHf=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.HEART_FAILURE_PROGRAM);
		diedInDiab=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.DIABETE_TREATMENT_WORKFLOW, GlobalPropertiesManagement.DM_PROGRAM);
		diedInChr=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.CRD_TREATMENT_WORKFLOW, GlobalPropertiesManagement.CRD_PROGRAM);
		diedInHyp=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.HYPERTENSION_PROGRAM);
		diedInEpil=gp.getProgramWorkflowState(GlobalPropertiesManagement.PATIENT_DIED_STATE,
			    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.EPILEPSY_PROGRAM);
        height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		 onOrAfterOnOrBeforeParamterNames.add("onOrAfter");
		 onOrAfterOnOrBeforeParamterNames.add("onOrBefore");
	}		
				
	private void createCustomWebRenderer(ReportDefinition rd, String name) throws IOException {
    	final ReportDesign design = new ReportDesign();
    	design.setName(name);
    	design.setReportDefinition(rd);
    	design.setRendererType(DataQualityReportWebRenderer.class);
    	
    	ReportService rs = Context.getService(ReportService.class);
    	rs.saveReportDesign(design);
    }
	
	private void createCustomWebRendererForSites(ReportDefinition rd, String name) throws IOException {
    	final ReportDesign design = new ReportDesign();
    	design.setName(name);
    	design.setReportDefinition(rd);
    	design.setRendererType(DataQualityWebRendererForSites.class);
    	
    	ReportService rs = Context.getService(ReportService.class);
    	rs.saveReportDesign(design);
    }	
	

}
