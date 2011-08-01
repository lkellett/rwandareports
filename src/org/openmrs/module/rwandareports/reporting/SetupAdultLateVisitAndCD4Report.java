package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
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
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiffInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientDateOfBirth;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ReturnVisitDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.LateVisitAndCD4ReportConstant;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;

public class SetupAdultLateVisitAndCD4Report {
	protected final static Log log = LogFactory.getLog(SetupAdultLateVisitAndCD4Report.class);
	
	Helper h = new Helper();
	
	//private HashMap<String, String> properties;
	
	public SetupAdultLateVisitAndCD4Report(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		//setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		//h.createRowPerPatientXlsOverview(rd, "AdultLateVisitAndCD4Template.xls", "XlsAdultLateVisitAndCD4Template", null);
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "AdultLateVisitAndCD4Template.xls", "XlsAdultLateVisitAndCD4Template", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8,dataset:AdultARTLateVisit|sheet:2,row:8,dataset:AdultPreARTLateVisit|sheet:3,row:8,dataset:AdultHIVLateCD4Count|sheet:4,row:8,dataset:HIVLostToFollowup|sheet5,row:8,dataset:PreARTBelow350CD4|sheet6,row:8,dataset:HIVLowBMI");
	
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
		h.purgeDefinition(ReportDefinition.class, "Adult HIV Monthly Report");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult Late Visit And CD4 Data Set");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult ART dataSetDefinition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult Pre-ART dataSetDefinition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult HIV late CD4 dataSetDefinition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult HIV lost to follow-up dataSetDefinition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult HIV lost to follow-up dataSetDefinition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult Pre-ART patients with CD4 below 350 dataSetDefinition");
		
		
		h.purgeDefinition(CohortDefinition.class, "location: HIV Adult Patients at location");
		h.purgeDefinition(CohortDefinition.class, "adultHivProgramCohort");		
		h.purgeDefinition(CohortDefinition.class, "onARTStatusCohort");
		h.purgeDefinition(CohortDefinition.class, "patientsWithoutEncountersInPastYear");
		h.purgeDefinition(CohortDefinition.class, "cd4CohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "patientsWithouthCD4RecordComposition");
		h.purgeDefinition(CohortDefinition.class, "followingStatusCohort");
		h.purgeDefinition(CohortDefinition.class, "patientsNotVoided");
		h.purgeDefinition(CohortDefinition.class, "patientsWithAnyEncounterNotVoided");
		h.purgeDefinition(CohortDefinition.class, "patientsWithClinicalEncounters");
		h.purgeDefinition(CohortDefinition.class, "patientsWithAnyEncounterInOneYear");
		h.purgeDefinition(CohortDefinition.class, "patientsWithClinicalEncountersWithoutLabTest");
		h.purgeDefinition(CohortDefinition.class, "patientsWithoutClinicalEncounters");
		h.purgeDefinition(CohortDefinition.class, "lastDC4below350");
		h.purgeDefinition(CohortDefinition.class, "deciningInCD4MoreThan50");
		
		    
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Adult HIV Monthly Report");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("location: HIV Adult Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		//====================================================================
		//           Patients Dataset definition
		//====================================================================
		
		
		// Create Adult ART late visit dataset definition 
		PatientDataSetDefinition dataSetDefinition1 = new PatientDataSetDefinition();
		dataSetDefinition1.setName("Adult ART dataSetDefinition");
		
		// Create Adult Pre-ART late visit dataset definition 
		PatientDataSetDefinition dataSetDefinition2 = new PatientDataSetDefinition();
		dataSetDefinition2.setName("Adult Pre-ART dataSetDefinition");
	
		//Create Adult HIV late CD4 count dataset definition
		PatientDataSetDefinition dataSetDefinition3 = new PatientDataSetDefinition();
		dataSetDefinition3.setName("Adult HIV late CD4 dataSetDefinition");
		
		//Create HIV lost to follow-up dataset definition
		PatientDataSetDefinition dataSetDefinition4 = new PatientDataSetDefinition();
		dataSetDefinition4.setName("Adult HIV lost to follow-up dataSetDefinition");
		

		//Create Adult Pre-ART patients with CD4 below 350 dataset definition
		PatientDataSetDefinition dataSetDefinition5 = new PatientDataSetDefinition();
		dataSetDefinition5.setName("Adult Pre-ART patients with CD4 below 350 dataSetDefinition");
		
		//Patients with BMI below 18.5 dataset definition
		PatientDataSetDefinition dataSetDefinition6 = new PatientDataSetDefinition();
		dataSetDefinition6.setName("Patients with BMI below 18.5 dataSetDefinition");
		
		//PatientDataSetDefinition dataSetDefinition7 = new PatientDataSetDefinition();
		//dataSetDefinition7.setName("Patients declining in CD4 more than 50 dataSetDefinition");
		
		
		
		//Adult HIV program Cohort definition
		InProgramCohortDefinition adultHivProgramCohort = new InProgramCohortDefinition();
		adultHivProgramCohort.setName("adultHivProgramCohort");
		adultHivProgramCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<Program> programs = new ArrayList<Program>();
		Program hadultHivProgram = Context.getProgramWorkflowService().getProgram(Integer.parseInt(Context.getAdministrationService().getGlobalProperty("hiv.programid.adult")));
		programs.add(hadultHivProgram);
		adultHivProgramCohort.setPrograms(programs);
		
		dataSetDefinition1.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition2.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition3.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition4.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition5.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition6.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		h.replaceCohortDefinition(adultHivProgramCohort);
		
		//==================================================================
		//                 1. Adult ART late visit
		//==================================================================		
		
		
		// ON ANTIRETROVIRALS state cohort definition.
		
		InStateCohortDefinition onARTStatusCohort = new InStateCohortDefinition();
		onARTStatusCohort.setName("onARTStatusCohort");
		onARTStatusCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow txStatus = hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.ADULT_TREATMENT_STATUS_ID);
		
				
		ProgramWorkflowState onART = null;
		if(txStatus != null)
		{
			onART = txStatus.getState(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.ON_ANTIRETROVIRALS_UUID));
					
			if(onART != null)
			{
				states.add(onART);
				onARTStatusCohort.setStates(states);
				dataSetDefinition1.addFilter(onARTStatusCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
				
			}
		}
		h.replaceCohortDefinition(onARTStatusCohort);
		
		
				
		SqlCohortDefinition patientsNotVoided = new SqlCohortDefinition("select distinct p.patient_id from patient p where p.voided=0");
		patientsNotVoided.setName("patientsNotVoided");
		h.replaceCohortDefinition(patientsNotVoided);
		dataSetDefinition1.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition2.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition3.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition4.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition5.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition6.addFilter(patientsNotVoided,new HashMap<String, Object>());
		

		SqlCohortDefinition patientsWithAnyEncounterNotVoided = new SqlCohortDefinition("select distinct e.patient_id from encounter e where e.voided=0");
		patientsWithAnyEncounterNotVoided.setName("patientsWithAnyEncounterNotVoided");
		h.replaceCohortDefinition(patientsWithAnyEncounterNotVoided);
		
		String clinicalEncTypesIds=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestIncl");
		String[] clinicalEncTypesIdsList=clinicalEncTypesIds.split(",");
		List<EncounterType> clinicalEncounterTypes=new ArrayList<EncounterType>();
		for(String id:clinicalEncTypesIdsList){			
			if(Context.getEncounterService().getEncounterType(Integer.parseInt(id))!=null)
			clinicalEncounterTypes.add(Context.getEncounterService().getEncounterType(Integer.parseInt(id)));
		}
		if(clinicalEncounterTypes==null||clinicalEncounterTypes.size()==0)
			throw new RuntimeException("Are you sure the global property ClinicalencounterTypeIds.labTestIncl is set correctly?");
		
		EncounterCohortDefinition patientsWithClinicalEncounters=new EncounterCohortDefinition();
		patientsWithClinicalEncounters.setName("patientsWithClinicalEncounters");
		patientsWithClinicalEncounters.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithClinicalEncounters.setEncounterTypeList(clinicalEncounterTypes);
		h.replaceCohortDefinition(patientsWithClinicalEncounters);
		
		CompositionCohortDefinition patientsWithAnyEncounterInOneYear=new CompositionCohortDefinition();
		patientsWithAnyEncounterInOneYear.setName("patientsWithAnyEncounterInOneYear");
		patientsWithAnyEncounterInOneYear.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithAnyEncounterInOneYear.getSearches().put("patientsWithAnyEncounterNotVoided", new Mapped<CohortDefinition>(patientsWithAnyEncounterNotVoided,null));
		patientsWithAnyEncounterInOneYear.getSearches().put("patientsWithAnyEncounter", new Mapped<CohortDefinition>(patientsWithClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithAnyEncounterInOneYear.setCompositionString("patientsWithAnyEncounterNotVoided AND patientsWithAnyEncounter");
		h.replaceCohortDefinition(patientsWithAnyEncounterInOneYear);
		
		dataSetDefinition1.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition2.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition3.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition5.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition6.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		
		
		
		// Patients without Any clinical Encounter(Test lab excluded) in last three months.
		
		String clinicalEncTypesIdsWithoutLabTest=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestExcl");
		String[] clinicalEncTypesIdsWithoutLabTestList=clinicalEncTypesIdsWithoutLabTest.split(",");
		List<EncounterType> clinicalEncounterTypesWithoutLabTest=new ArrayList<EncounterType>();
		for(String id:clinicalEncTypesIdsWithoutLabTestList){			
			if(Context.getEncounterService().getEncounterType(Integer.parseInt(id))!=null)
			clinicalEncounterTypesWithoutLabTest.add(Context.getEncounterService().getEncounterType(Integer.parseInt(id)));
		}
		
		EncounterCohortDefinition patientsWithClinicalEncountersWithoutLabTest=new EncounterCohortDefinition();
		patientsWithClinicalEncountersWithoutLabTest.setName("patientsWithClinicalEncountersWithoutLabTest");
		patientsWithClinicalEncountersWithoutLabTest.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithClinicalEncountersWithoutLabTest.setEncounterTypeList(clinicalEncounterTypesWithoutLabTest);
		h.replaceCohortDefinition(patientsWithClinicalEncountersWithoutLabTest);
		
		CompositionCohortDefinition patientsWithoutClinicalEncounters=new CompositionCohortDefinition();
		patientsWithoutClinicalEncounters.setName("patientsWithoutClinicalEncounters");
		patientsWithoutClinicalEncounters.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithoutClinicalEncounters.getSearches().put("patientsWithClinicalEncountersWithoutLabTest", new Mapped<CohortDefinition>(patientsWithClinicalEncountersWithoutLabTest,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithoutClinicalEncounters.setCompositionString("NOT patientsWithClinicalEncountersWithoutLabTest");
		h.replaceCohortDefinition(patientsWithoutClinicalEncounters);		
		
		dataSetDefinition1.addFilter(patientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m}"));
		
		//==================================================================
		//                 2. Adult Pre-ART late visit
		//==================================================================
		
		// Following state cohort definition.

		
		
		InStateCohortDefinition followingStatusCohort = new InStateCohortDefinition();
		followingStatusCohort.setName("followingStatusCohort");
		followingStatusCohort.addParameter(new Parameter("onDate","On date",Date.class));
		List<ProgramWorkflowState> followingstates = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow followingtxStatus = hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.ADULT_TREATMENT_STATUS_ID);
		
			
		ProgramWorkflowState following = null;
		if(followingtxStatus != null)
		{
			following = followingtxStatus.getState(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.FOLLOWING_UUID));
					
			if(following != null)
			{
				followingstates.add(following);
				followingStatusCohort.setStates(followingstates);
				dataSetDefinition2.addFilter(followingStatusCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
				dataSetDefinition5.addFilter(followingStatusCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
				
			}
		}	
		h.replaceCohortDefinition(followingStatusCohort);
		
		
		
		// Patients without Any clinical Encounter(Test lab excluded) in last six months.
		dataSetDefinition2.addFilter(patientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		dataSetDefinition5.addFilter(patientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
			
			

		//==================================================================
		//                 3. Adult HIV late CD4 count
		//==================================================================
		
		NumericObsCohortDefinition cd4CohortDefinition=new NumericObsCohortDefinition();
		cd4CohortDefinition.setName("cd4CohortDefinition");
		cd4CohortDefinition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));		
		cd4CohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		cd4CohortDefinition.setTimeModifier(TimeModifier.ANY);
		cd4CohortDefinition.setOperator1(null);
		h.replaceCohortDefinition(cd4CohortDefinition);
		
		CompositionCohortDefinition patientsWithouthCD4RecordComposition=new CompositionCohortDefinition();
		patientsWithouthCD4RecordComposition.setName("patientsWithouthCD4RecordComposition");
		patientsWithouthCD4RecordComposition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithouthCD4RecordComposition.getSearches().put("cd4CohortDefinition", new Mapped<CohortDefinition>(cd4CohortDefinition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithouthCD4RecordComposition.setCompositionString("NOT cd4CohortDefinition");
		h.replaceCohortDefinition(patientsWithouthCD4RecordComposition);
		dataSetDefinition3.addFilter(patientsWithouthCD4RecordComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		

		//==================================================================
		//                 4. HIV lost to follow-up
		//==================================================================
		
		//Patients with no encounters of any kind in the past year
		
		InverseCohortDefinition patientsWithoutEncountersInPastYear=new InverseCohortDefinition(patientsWithClinicalEncounters);
		patientsWithoutEncountersInPastYear.setName("patientsWithoutEncountersInPastYear");
		h.replaceCohortDefinition(patientsWithoutEncountersInPastYear);
		
		dataSetDefinition4.addFilter(patientsWithoutEncountersInPastYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));		
		
		//==================================================================
		//                 5. Pre-ART patients with CD4 below 350
		//==================================================================
		
		//Patients with CD4 below 350
		
		NumericObsCohortDefinition lastDC4below350=makeNumericObsCohortDefinition(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID, 350.0,RangeComparator.LESS_THAN, TimeModifier.LAST);
		lastDC4below350.setName("lastDC4below350");
        h.replaceCohortDefinition(lastDC4below350);		
		dataSetDefinition5.addFilter(lastDC4below350,new HashMap<String, Object>());
		
		//==================================================================
		//                 6. Patients with BMI less than 18.5
		//==================================================================
		
		//Patients with BMI less than 18.5
		
		
		SqlCohortDefinition patientWithLowBMI=new SqlCohortDefinition();
		patientWithLowBMI.setName("patientWithLowBMI");
		patientWithLowBMI.setQuery("select w.person_id from (select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"+LateVisitAndCD4ReportConstant.HEIGHT_UUID+"' order by o.obs_datetime desc) as lastheight group by lastheight.person_id) h,(select * from (select o.person_id,o.value_numeric from obs o,concept c where o.concept_id= c.concept_id and c.uuid='"+LateVisitAndCD4ReportConstant.WEIGHT_UUID+"' order by o.obs_datetime desc) as lastweight group by lastweight.person_id) w where w.person_id=h.person_id and ROUND(((w.value_numeric*10000)/(h.value_numeric*h.value_numeric)),2)<18.5");
		h.replaceCohortDefinition(patientWithLowBMI);
		dataSetDefinition6.addFilter(patientWithLowBMI,new HashMap<String, Object>());
		
		
		//==================================================================
		//                 . Patients Declining in CD4 by more than 50
		//==================================================================
		
		//Patients Declining in CD4 by more than 50
		
		/*DiffNumericObsCohortDefinition deciningInCD4MoreThan50=new DiffNumericObsCohortDefinition();
		deciningInCD4MoreThan50.setName("deciningInCD4MoreThan50");
		deciningInCD4MoreThan50.setConcept(Context.getConceptService().getConcept(5497));
		h.replaceCohortDefinition(deciningInCD4MoreThan50);*/
		//dataSetDefinition7.addFilter(deciningInCD4MoreThan50,new HashMap<String, Object>());
		
		
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		
		
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbId.setName("IMB ID");
		imbId.setDescription("IMB ID");
		dataSetDefinition1.addColumn(imbId,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(imbId,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(imbId,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(imbId,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(imbId,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(imbId,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(imbId,new HashMap<String, Object>());
		
		
		PatientProperty givenName = new PatientProperty("givenName");
		givenName.setName("First Name");
		givenName.setDescription("First Name");
		dataSetDefinition1.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(givenName,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(givenName,new HashMap<String, Object>());
		
		
		PatientProperty familyName = new PatientProperty("familyName");
		familyName.setName("Last Name");
		familyName.setDescription("Last Name");
		dataSetDefinition1.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(familyName,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(familyName,new HashMap<String, Object>());
		
		PatientProperty gender = new PatientProperty("gender");
		gender.setName("Sex");
		gender.setDescription("Sex");
		dataSetDefinition1.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(gender,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(gender,new HashMap<String, Object>());
		
		PatientDateOfBirth birthdate = new PatientDateOfBirth();
		birthdate.setName("Date of Birth");
		birthdate.setDescription("Date of Birth");
		dataSetDefinition1.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(birthdate,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(birthdate,new HashMap<String, Object>());
		
		
		StateOfPatient txGroup=new StateOfPatient();
		txGroup.setPatientProgram(hadultHivProgram);
		txGroup.setPatienProgramWorkflow(hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.ADULT_TREATMENT_GROUP_ID));
		txGroup.setName("Group");
		txGroup.setDescription("Group");
		txGroup.setFilter(new GroupStateFilter());
		dataSetDefinition1.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(txGroup,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(txGroup,new HashMap<String, Object>());
				
		StateOfPatient stOfPatient=new StateOfPatient();
		stOfPatient.setPatientProgram(hadultHivProgram);
		stOfPatient.setPatienProgramWorkflow(hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.ADULT_TREATMENT_STATUS_ID));
		stOfPatient.setName("Treatment");
		stOfPatient.setDescription("Treatment");
		stOfPatient.setFilter(new TreatmentStateFilter());
		dataSetDefinition1.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(stOfPatient,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(stOfPatient,new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType=new RecentEncounterType();
		lastEncounterType.setName("Last visit type");
		lastEncounterType.setDescription("Last visit type");
		lastEncounterType.setEncounterTypes(clinicalEncounterTypesWithoutLabTest);
		lastEncounterType.setFilter(new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(lastEncounterType,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(lastEncounterType,new HashMap<String, Object>());
		
		DateDiffInMonths lateVisitInMonth=new DateDiffInMonths();
		lateVisitInMonth.setName("Late visit in months");
		lateVisitInMonth.setDescription("Late visit type");
		lateVisitInMonth.setEncounterTypes(clinicalEncounterTypesWithoutLabTest);
		lateVisitInMonth.addParameter(new Parameter("endDate","endDate",Date.class));
		dataSetDefinition1.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition5.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition6.addColumn(lateVisitInMonth,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(lateVisitInMonth,new HashMap<String, Object>());
		
		
		ReturnVisitDate returnVisitDate=new ReturnVisitDate();
		returnVisitDate.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.RETURN_VISIT_DATE_UUID));
		returnVisitDate.setName("Date of missed appointment");
		returnVisitDate.setDescription("Date of missed appointment");
		dataSetDefinition1.addColumn(returnVisitDate,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(returnVisitDate,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(returnVisitDate,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(returnVisitDate,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(returnVisitDate,new HashMap<String, Object>());
		
		MostRecentObservation cd4Count=new MostRecentObservation();
		cd4Count.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		cd4Count.setName("Most recent CD4");
		cd4Count.setDescription("Most recent CD4");
		dataSetDefinition1.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(cd4Count,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(cd4Count,new HashMap<String, Object>());
		
		DateDiffInMonths lateCD4InMonths=new DateDiffInMonths();
		lateCD4InMonths.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		lateCD4InMonths.setName("Late CD4 in months");
		lateCD4InMonths.setDescription("Late CD4 in months");
		lateCD4InMonths.addParameter(new Parameter("endDate","endDate",Date.class));
		dataSetDefinition1.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition2.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition3.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition4.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition5.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		dataSetDefinition6.addColumn(lateCD4InMonths,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(lateCD4InMonths,new HashMap<String, Object>());
		
				
		
		PatientRelationship accompagnateur=new PatientRelationship();
		accompagnateur.setRelationshipTypeId(LateVisitAndCD4ReportConstant.RELATIONSHIP_TYPE_ID);
		accompagnateur.setName("Accompagnateur");
		accompagnateur.setDescription("Accompagnateur");
		dataSetDefinition1.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition5.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(accompagnateur,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(accompagnateur,new HashMap<String, Object>());
		
		PatientAddress address1 = new PatientAddress();
		address1.setName("Address");
		address1.setDescription("Address");
		address1.setIncludeCountry(false);
		address1.setIncludeProvince(false);
		dataSetDefinition1.addColumn(address1,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(address1,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(address1,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(address1,new HashMap<String, Object>());

		dataSetDefinition5.addColumn(address1,new HashMap<String, Object>());
		dataSetDefinition6.addColumn(address1,new HashMap<String, Object>());
		//dataSetDefinition7.addColumn(address1,new HashMap<String, Object>());

		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition5.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition6.addParameter(new Parameter("location", "Location", Location.class));
		//dataSetDefinition7.addParameter(new Parameter("location", "Location", Location.class));
		
		
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition5.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition6.addParameter(new Parameter("endDate", "End Date", Date.class));
		//dataSetDefinition7.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		h.replaceDefinition(dataSetDefinition1);
		h.replaceDefinition(dataSetDefinition2);
		h.replaceDefinition(dataSetDefinition3);
		h.replaceDefinition(dataSetDefinition4);
		h.replaceDefinition(dataSetDefinition5);
		h.replaceDefinition(dataSetDefinition6);
		//h.replaceDefinition(dataSetDefinition7);
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		
		reportDefinition.addDataSetDefinition("AdultARTLateVisit", dataSetDefinition1, mappings);
		reportDefinition.addDataSetDefinition("AdultPreARTLateVisit", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("AdultHIVLateCD4Count", dataSetDefinition3, mappings);
		reportDefinition.addDataSetDefinition("HIVLostToFollowup", dataSetDefinition4, mappings);
		reportDefinition.addDataSetDefinition("PreARTBelow350CD4", dataSetDefinition5, mappings);
		reportDefinition.addDataSetDefinition("HIVLowBMI", dataSetDefinition6, mappings);
		//reportDefinition.addDataSetDefinition("DecliningInCD4ByMoreThan50", dataSetDefinition7, mappings);
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location");
		location.setName("location: HIV Adult Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	private NumericObsCohortDefinition makeNumericObsCohortDefinition(String question, Double value, RangeComparator setComparator, TimeModifier timeModifier) {                
	     
	     NumericObsCohortDefinition obsCohortDefinition = new NumericObsCohortDefinition();
	           	             
	             if (question != null) obsCohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(question));
	             
	             if (setComparator != null) obsCohortDefinition.setOperator1(setComparator);
	             
	             if (timeModifier != null) obsCohortDefinition.setTimeModifier(timeModifier);
	             
	            if (value != 0) {
	                     obsCohortDefinition.setValue1(value);
	             }
	                             
	             return obsCohortDefinition;
	     }
}
