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
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientDateOfBirth;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
//import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
//import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentObservation;
//import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentObservationDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ReturnVisitDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiffInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rwandareports.LateVisitAndCD4ReportConstant;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;

public class SetupPediatricLateVisitAndCD4Report {
	protected final static Log log = LogFactory.getLog(SetupPediatricLateVisitAndCD4Report.class);
	
	Helper h = new Helper();
	
	//private HashMap<String, String> properties;
	
	public SetupPediatricLateVisitAndCD4Report(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		//setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		//h.createRowPerPatientXlsOverview(rd, "PediatricLateVisitAndCD4Template.xls", "XlsPediatricLateVisitAndCD4Template", null);
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediatricLateVisitAndCD4Template.xls", "XlsPediatricLateVisitAndCD4Template", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8,dataset:PediatricARTLateVisit|sheet:2,row:8,dataset:PediatricPreARTLateVisit|sheet:3,row:8,dataset:PediatricHIVLateCD4Count|sheet:4,row:8,dataset:PediatricHIVLostToFollowup");
	
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsPediatricLateVisitAndCD4Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Pediatric HIV Monthly Report");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Pediatric Late Visit And CD4 Data Set");
		
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Pedi ART late visit dataset definition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Pedi Pre-ART late visit dataset definition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Pedi HIV late CD4 count dataset definition");
		h.purgeDefinition(PatientDataSetDefinition.class, "Pedi HIV lost to follow-up dataset definition");
		          
		h.purgeDefinition(CohortDefinition.class, "location: HIV Pedi Patients at location");
		h.purgeDefinition(CohortDefinition.class, "pediHivProgramCohort");
		h.purgeDefinition(CohortDefinition.class, "pediOnARTStatusCohort");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsNotVoided");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithAnyEncounterNotVoided");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithClinicalEncounters");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithAnyEncounterInOneYear");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithClinicalEncountersWithoutLabTest");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithoutClinicalEncounters");
		h.purgeDefinition(CohortDefinition.class, "pediFollowingStatusCohort");
		h.purgeDefinition(CohortDefinition.class, "pediCD4CohortDefinition");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithouthCD4RecordComposition");
		h.purgeDefinition(CohortDefinition.class, "pediPatientsWithoutEncountersInPastYear");
		h.purgeDefinition(CohortDefinition.class, "");
		
		      	
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Pediatric HIV Monthly Report");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("location: HIV Pedi Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		//====================================================================
		//           Patients Dataset definition
		//====================================================================
		
		
		// Create Pedi ART late visit dataset definition 
		PatientDataSetDefinition pediatricARTLateVisitDataSet = new PatientDataSetDefinition();
		pediatricARTLateVisitDataSet.setName("Pedi ART late visit dataset definition");
		
		// Create Pedi Pre-ART late visit dataset definition 
		PatientDataSetDefinition pediatricPreARTLateVisitDataSet = new PatientDataSetDefinition();
		pediatricPreARTLateVisitDataSet.setName("Pedi Pre-ART late visit dataset definition");
		
		//Create Pedi HIV late CD4 count dataset definition
		PatientDataSetDefinition pediatricHIVLateCD4CountDataSet = new PatientDataSetDefinition();
		pediatricHIVLateCD4CountDataSet.setName("Pedi HIV late CD4 count dataset definition");
		
		//Create Pedi HIV lost to follow-up dataset definition
		PatientDataSetDefinition pediatricHIVLostToFollowupDatSet = new PatientDataSetDefinition();
		pediatricHIVLostToFollowupDatSet.setName("Pedi HIV lost to follow-up dataset definition");
		
		
		
		//Pedi HIV program Cohort definition
		InProgramCohortDefinition pediHivProgramCohort = new InProgramCohortDefinition();
		pediHivProgramCohort.setName("pediHivProgramCohort");
		pediHivProgramCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<Program> programs = new ArrayList<Program>();
		Program pediHivProgram = Context.getProgramWorkflowService().getProgram(Integer.parseInt(Context.getAdministrationService().getGlobalProperty("hiv.programid.pediatric")));
		programs.add(pediHivProgram);
		pediHivProgramCohort.setPrograms(programs);
		h.replaceCohortDefinition(pediHivProgramCohort);
		
		pediatricARTLateVisitDataSet.addFilter(pediHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		pediatricPreARTLateVisitDataSet.addFilter(pediHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		pediatricHIVLateCD4CountDataSet.addFilter(pediHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		pediatricHIVLostToFollowupDatSet.addFilter(pediHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		/*
		SqlCohortDefinition patientDied=new SqlCohortDefinition("SELECT DISTINCT person_id FROM obs o WHERE o.concept_id='1811'");
		patientDied.addParameter(new Parameter("onDate","On Date",Date.class));
		InverseCohortDefinition patientAlive=new InverseCohortDefinition(patientDied);
		dataSetDefinition1.addFilter(patientAlive,new HashMap<String, Object>());
		dataSetDefinition2.addFilter(patientAlive,new HashMap<String, Object>());
		dataSetDefinition3.addFilter(patientAlive,new HashMap<String, Object>());
		dataSetDefinition4.addFilter(patientAlive,new HashMap<String, Object>());
		*/
		//==================================================================
		//                 1. Pediatric ART late visit
		//==================================================================
		
		
		
		// ON ANTIRETROVIRALS state cohort definition.
			
		InStateCohortDefinition pediOnARTStatusCohort = new InStateCohortDefinition();
		pediOnARTStatusCohort.setName("pediOnARTStatusCohort");
		pediOnARTStatusCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow txStatus = pediHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_STATUS_ID);
		
		ProgramWorkflowState onART = null;
		if(txStatus != null)
		{
			onART = txStatus.getState(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.ON_ANTIRETROVIRALS_UUID));
			if(onART != null)
			{
				states.add(onART);
				pediOnARTStatusCohort.setStates(states);
				pediatricARTLateVisitDataSet.addFilter(pediOnARTStatusCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
				
			}
		}
		h.replaceCohortDefinition(pediOnARTStatusCohort);
		
		
		
				
		SqlCohortDefinition pediPatientsNotVoided = new SqlCohortDefinition("select distinct p.patient_id from patient p where p.voided=0");
		pediPatientsNotVoided.setName("pediPatientsNotVoided");
		h.replaceCohortDefinition(pediPatientsNotVoided);
		pediatricARTLateVisitDataSet.addFilter(pediPatientsNotVoided,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addFilter(pediPatientsNotVoided,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addFilter(pediPatientsNotVoided,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addFilter(pediPatientsNotVoided,new HashMap<String, Object>());
		
		SqlCohortDefinition pediPatientsWithAnyEncounterNotVoided = new SqlCohortDefinition("select distinct e.patient_id from encounter e where e.voided=0");
		pediPatientsWithAnyEncounterNotVoided.setName("pediPatientsWithAnyEncounterNotVoided");
		h.replaceCohortDefinition(pediPatientsWithAnyEncounterNotVoided);
		
		String clinicalEncTypesIds=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestIncl");
		String[] clinicalEncTypesIdsList=clinicalEncTypesIds.split(",");
		List<EncounterType> clinicalEncounterTypes=new ArrayList<EncounterType>();
		for(String id:clinicalEncTypesIdsList){			
			if(Context.getEncounterService().getEncounterType(Integer.parseInt(id))!=null)
			clinicalEncounterTypes.add(Context.getEncounterService().getEncounterType(Integer.parseInt(id)));
		}
		if(clinicalEncounterTypes==null||clinicalEncounterTypes.size()==0)
			throw new RuntimeException("Are you sure the global property ClinicalencounterTypeIds.labTestIncl is set correctly?");
		
		
		EncounterCohortDefinition pediPatientsWithClinicalEncounters=new EncounterCohortDefinition();
		pediPatientsWithClinicalEncounters.setName("pediPatientsWithClinicalEncounters");
		pediPatientsWithClinicalEncounters.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		pediPatientsWithClinicalEncounters.setEncounterTypeList(clinicalEncounterTypes);
		h.replaceCohortDefinition(pediPatientsWithClinicalEncounters);
		
		CompositionCohortDefinition pediPatientsWithAnyEncounterInOneYear=new CompositionCohortDefinition();
		pediPatientsWithAnyEncounterInOneYear.setName("pediPatientsWithAnyEncounterInOneYear");
		pediPatientsWithAnyEncounterInOneYear.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		pediPatientsWithAnyEncounterInOneYear.getSearches().put("pediPatientsWithAnyEncounterNotVoided", new Mapped<CohortDefinition>(pediPatientsWithAnyEncounterNotVoided,null));
		pediPatientsWithAnyEncounterInOneYear.getSearches().put("patientsWithAnyEncounter", new Mapped<CohortDefinition>(pediPatientsWithClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		pediPatientsWithAnyEncounterInOneYear.setCompositionString("pediPatientsWithAnyEncounterNotVoided AND patientsWithAnyEncounter");
		h.replaceCohortDefinition(pediPatientsWithAnyEncounterInOneYear);
		
		pediatricARTLateVisitDataSet.addFilter(pediPatientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		pediatricPreARTLateVisitDataSet.addFilter(pediPatientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		pediatricHIVLateCD4CountDataSet.addFilter(pediPatientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
	
		
		
		// Patients without Any clinical Encounter(Test lab excluded) in last three months.
		
		String clinicalEncTypesIdsWithoutLabTest=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestExcl");
		String[] clinicalEncTypesIdsWithoutLabTestList=clinicalEncTypesIdsWithoutLabTest.split(",");
		List<EncounterType> clinicalEncounterTypesWithoutLabTest=new ArrayList<EncounterType>();
		for(String id:clinicalEncTypesIdsWithoutLabTestList){			
			if(Context.getEncounterService().getEncounterType(Integer.parseInt(id))!=null)
			clinicalEncounterTypesWithoutLabTest.add(Context.getEncounterService().getEncounterType(Integer.parseInt(id)));
		}
		
		EncounterCohortDefinition pediPatientsWithClinicalEncountersWithoutLabTest=new EncounterCohortDefinition();
		pediPatientsWithClinicalEncountersWithoutLabTest.setName("pediPatientsWithClinicalEncountersWithoutLabTest");
		pediPatientsWithClinicalEncountersWithoutLabTest.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		pediPatientsWithClinicalEncountersWithoutLabTest.setEncounterTypeList(clinicalEncounterTypesWithoutLabTest);
		h.replaceCohortDefinition(pediPatientsWithClinicalEncountersWithoutLabTest);
		
		CompositionCohortDefinition pediPatientsWithoutClinicalEncounters=new CompositionCohortDefinition();
		pediPatientsWithoutClinicalEncounters.setName("pediPatientsWithoutClinicalEncounters");
		pediPatientsWithoutClinicalEncounters.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		pediPatientsWithoutClinicalEncounters.getSearches().put("pediPatientsWithClinicalEncountersWithoutLabTest", new Mapped<CohortDefinition>(pediPatientsWithClinicalEncountersWithoutLabTest,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		pediPatientsWithoutClinicalEncounters.setCompositionString("NOT pediPatientsWithClinicalEncountersWithoutLabTest");
		h.replaceCohortDefinition(pediPatientsWithoutClinicalEncounters);
		
		
		pediatricARTLateVisitDataSet.addFilter(pediPatientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m}"));
				
		
		//==================================================================
		//                 2. Pediatric Pre-ART late visit
		//==================================================================
		
		// Following state cohort definition.
		
		
		InStateCohortDefinition pediFollowingStatusCohort = new InStateCohortDefinition();
		pediFollowingStatusCohort.setName("pediFollowingStatusCohort");
		pediFollowingStatusCohort.addParameter(new Parameter("onDate","On date",Date.class));
		List<ProgramWorkflowState> followingstates = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow followingtxStatus = pediHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_STATUS_ID);
		
		ProgramWorkflowState following = null;
		if(followingtxStatus != null)
		{
			following = followingtxStatus.getState(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.FOLLOWING_UUID));
			if(following != null)
			{
				followingstates.add(following);
				pediFollowingStatusCohort.setStates(followingstates);
				pediatricPreARTLateVisitDataSet.addFilter(pediFollowingStatusCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
			}
		}		
		h.replaceCohortDefinition(pediFollowingStatusCohort);
		// Patients without Any clinical Encounter(Test lab excluded) in last six months.
		pediatricPreARTLateVisitDataSet.addFilter(pediPatientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
			
			

		//==================================================================
		//                 3. Pediatric HIV late CD4 count
		//==================================================================
		
		NumericObsCohortDefinition pediCD4CohortDefinition=new NumericObsCohortDefinition();
		pediCD4CohortDefinition.setName("pediCD4CohortDefinition");
		pediCD4CohortDefinition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		pediCD4CohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		pediCD4CohortDefinition.setTimeModifier(TimeModifier.ANY);
		pediCD4CohortDefinition.setOperator1(null);
		h.replaceCohortDefinition(pediCD4CohortDefinition);
		
		CompositionCohortDefinition pediPatientsWithouthCD4RecordComposition=new CompositionCohortDefinition();
		pediPatientsWithouthCD4RecordComposition.setName("pediPatientsWithouthCD4RecordComposition");
		pediPatientsWithouthCD4RecordComposition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		pediPatientsWithouthCD4RecordComposition.getSearches().put("cd4CohortDefinition", new Mapped<CohortDefinition>(pediCD4CohortDefinition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		pediPatientsWithouthCD4RecordComposition.setCompositionString("NOT cd4CohortDefinition");
		h.replaceCohortDefinition(pediPatientsWithouthCD4RecordComposition);
		
		pediatricHIVLateCD4CountDataSet.addFilter(pediPatientsWithouthCD4RecordComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		

		//==================================================================
		//                 4. Pediatric HIV lost to follow-up
		//==================================================================
		
		//Patients with no encounters of any kind in the past year
		
		
		//SqlCohortDefinition patientsWithoutEncountersInPastYear = new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_datetime < '"+sdf1.format(dateOneYearAgo)+"' and p.patient_id not in (select patient_id from encounter where encounter_datetime >= '"+sdf1.format(dateOneYearAgo)+"' and voided = 0)");
		//SqlCohortDefinition patientsWithEncountersInPastYear = new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_datetime >= '"+sdf1.format(dateOneYearAgo)+"'");
		InverseCohortDefinition pediPatientsWithoutEncountersInPastYear=new InverseCohortDefinition(pediPatientsWithClinicalEncounters);
		pediPatientsWithoutEncountersInPastYear.setName("pediPatientsWithoutEncountersInPastYear");
		h.replaceCohortDefinition(pediPatientsWithoutEncountersInPastYear);
		
		pediatricHIVLostToFollowupDatSet.addFilter(pediPatientsWithoutEncountersInPastYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));		
		
					
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		
		
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbId.setName("IMB ID");
		imbId.setDescription("IMB ID");
		pediatricARTLateVisitDataSet.addColumn(imbId,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(imbId,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(imbId,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(imbId,new HashMap<String, Object>());
		
		PatientProperty givenName = new PatientProperty("givenName");
		givenName.setName("First Name");
		givenName.setDescription("First Name");
		pediatricARTLateVisitDataSet.addColumn(givenName,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(givenName,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(givenName,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(givenName,new HashMap<String, Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		familyName.setName("Last Name");
		familyName.setDescription("Last Name");
		pediatricARTLateVisitDataSet.addColumn(familyName,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(familyName,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(familyName,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(familyName,new HashMap<String, Object>());
		
		PatientProperty gender = new PatientProperty("gender");
		gender.setName("Sex");
		gender.setDescription("Sex");
		pediatricARTLateVisitDataSet.addColumn(gender,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(gender,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(gender,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(gender,new HashMap<String, Object>());
		
		PatientDateOfBirth birthdate = new PatientDateOfBirth();
		birthdate.setName("Date of Birth");
		birthdate.setDescription("Date of Birth");
		pediatricARTLateVisitDataSet.addColumn(birthdate,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(birthdate,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(birthdate,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(birthdate,new HashMap<String, Object>());
		
		
		StateOfPatient txGroup=new StateOfPatient();
		txGroup.setPatientProgram(pediHivProgram);
		txGroup.setPatienProgramWorkflow(pediHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_GROUP_ID));
		txGroup.setName("Group");
		txGroup.setDescription("Group");
		txGroup.setFilter(new GroupStateFilter());
		pediatricARTLateVisitDataSet.addColumn(txGroup,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(txGroup,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(txGroup,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(txGroup,new HashMap<String, Object>());
				
		StateOfPatient stOfPatient=new StateOfPatient();
		stOfPatient.setPatientProgram(pediHivProgram);
		stOfPatient.setPatienProgramWorkflow(pediHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_STATUS_ID));
		stOfPatient.setName("Treatment");
		stOfPatient.setDescription("Treatment");
		stOfPatient.setFilter(new TreatmentStateFilter());
		pediatricARTLateVisitDataSet.addColumn(stOfPatient,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(stOfPatient,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(stOfPatient,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(stOfPatient,new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType=new RecentEncounterType();
		lastEncounterType.setName("Last visit type");
		lastEncounterType.setDescription("Last visit type");
		lastEncounterType.setFilter(new LastEncounterFilter());
		pediatricARTLateVisitDataSet.addColumn(lastEncounterType,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(lastEncounterType,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(lastEncounterType,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(lastEncounterType,new HashMap<String, Object>());
		
		
		DateDiffInMonths lateVisitInMonth=new DateDiffInMonths();
		lateVisitInMonth.setName("Late visit in months");
		lateVisitInMonth.setDescription("Late visit type");
		lateVisitInMonth.setEncounterTypes(clinicalEncounterTypesWithoutLabTest);
		lateVisitInMonth.addParameter(new Parameter("endDate","endDate",Date.class));
		pediatricARTLateVisitDataSet.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		pediatricPreARTLateVisitDataSet.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		pediatricHIVLateCD4CountDataSet.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		pediatricHIVLostToFollowupDatSet.addColumn(lateVisitInMonth,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		
		ReturnVisitDate returnVisitDate=new ReturnVisitDate();
		returnVisitDate.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.RETURN_VISIT_DATE_UUID));
		returnVisitDate.setName("Date of missed appointment");
		returnVisitDate.setDescription("Date of missed appointment");
		pediatricARTLateVisitDataSet.addColumn(returnVisitDate,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(returnVisitDate,new HashMap<String, Object>());
		
		
		MostRecentObservation cd4Count=new MostRecentObservation();
		cd4Count.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		cd4Count.setName("Most recent CD4");
		cd4Count.setDescription("Most recent CD4");
		pediatricARTLateVisitDataSet.addColumn(cd4Count,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(cd4Count,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(cd4Count,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(cd4Count,new HashMap<String, Object>());
		
		DateDiffInMonths lateCD4InMonths=new DateDiffInMonths();
		lateCD4InMonths.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		lateCD4InMonths.setName("Late CD4 in months");
		lateCD4InMonths.setDescription("Late CD4 in months");
		lateCD4InMonths.addParameter(new Parameter("endDate","endDate",Date.class));
		pediatricARTLateVisitDataSet.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		pediatricPreARTLateVisitDataSet.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		pediatricHIVLateCD4CountDataSet.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		pediatricHIVLostToFollowupDatSet.addColumn(lateCD4InMonths,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		
		PatientRelationship accompagnateur=new PatientRelationship();
		accompagnateur.setRelationshipTypeId(LateVisitAndCD4ReportConstant.RELATIONSHIP_TYPE_ID);
		accompagnateur.setName("Accompagnateur");
		accompagnateur.setDescription("Accompagnateur");
		pediatricARTLateVisitDataSet.addColumn(accompagnateur,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(accompagnateur,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(accompagnateur,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(accompagnateur,new HashMap<String, Object>());
		
		PatientAddress address1 = new PatientAddress();
		address1.setName("Address");
		address1.setIncludeCountry(false);
		address1.setIncludeProvince(false);
		pediatricARTLateVisitDataSet.addColumn(address1,new HashMap<String, Object>());
		pediatricPreARTLateVisitDataSet.addColumn(address1,new HashMap<String, Object>());
		pediatricHIVLateCD4CountDataSet.addColumn(address1,new HashMap<String, Object>());
		pediatricHIVLostToFollowupDatSet.addColumn(address1,new HashMap<String, Object>());	
		
		pediatricARTLateVisitDataSet.addParameter(new Parameter("location", "Location", Location.class));
		pediatricPreARTLateVisitDataSet.addParameter(new Parameter("location", "Location", Location.class));
		pediatricHIVLateCD4CountDataSet.addParameter(new Parameter("location", "Location", Location.class));
		pediatricHIVLostToFollowupDatSet.addParameter(new Parameter("location", "Location", Location.class));
		
		
		pediatricARTLateVisitDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		pediatricPreARTLateVisitDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		pediatricHIVLateCD4CountDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		pediatricHIVLostToFollowupDatSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		h.replaceDefinition(pediatricARTLateVisitDataSet);
		h.replaceDefinition(pediatricPreARTLateVisitDataSet);
		h.replaceDefinition(pediatricHIVLateCD4CountDataSet);
		h.replaceDefinition(pediatricHIVLostToFollowupDatSet);
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("PediatricARTLateVisit", pediatricARTLateVisitDataSet, mappings);
		reportDefinition.addDataSetDefinition("PediatricPreARTLateVisit", pediatricPreARTLateVisitDataSet, mappings);
		reportDefinition.addDataSetDefinition("PediatricHIVLateCD4Count", pediatricHIVLateCD4CountDataSet, mappings);
		reportDefinition.addDataSetDefinition("PediatricHIVLostToFollowup", pediatricHIVLostToFollowupDatSet, mappings);
		
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location");
		location.setName("location: HIV Pedi Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}	
}
