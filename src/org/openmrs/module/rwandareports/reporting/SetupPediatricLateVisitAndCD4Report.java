package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
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
		h.createRowPerPatientXlsOverview(rd, "PediatricLateVisitAndCD4Template.xls", "XlsPediatricLateVisitAndCD4Template", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsPediatricLateVisitAndCD4Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Pediatric Late Visit And CD4");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Pediatric Late Visit And CD4 Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "location: HIV Pedi Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Pediatric Late Visit And CD4");
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
		
		
		// Create Adult ART late visit dataset definition 
		PatientDataSetDefinition dataSetDefinition1 = new PatientDataSetDefinition();
		dataSetDefinition1.setName(reportDefinition.getName() + " Data Set");
		
		// Create Adult Pre-ART late visit dataset definition 
		PatientDataSetDefinition dataSetDefinition2 = new PatientDataSetDefinition();
		dataSetDefinition2.setName(reportDefinition.getName() + " Data Set");
		
		//Create Adult HIV late CD4 count dataset definition
		PatientDataSetDefinition dataSetDefinition3 = new PatientDataSetDefinition();
		dataSetDefinition3.setName(reportDefinition.getName() + " Data Set");
		
		//Create HIV lost to follow-up dataset definition
		PatientDataSetDefinition dataSetDefinition4 = new PatientDataSetDefinition();
		dataSetDefinition4.setName(reportDefinition.getName() + " Data Set");
		
		
		
		//Adult HIV program Cohort definition
		InProgramCohortDefinition adultHivProgramCohort = new InProgramCohortDefinition();
		adultHivProgramCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<Program> programs = new ArrayList<Program>();
		Program hadultHivProgram = Context.getProgramWorkflowService().getProgram(Integer.parseInt(Context.getAdministrationService().getGlobalProperty("hiv.programid.pediatric")));
		programs.add(hadultHivProgram);
		adultHivProgramCohort.setPrograms(programs);
		
		dataSetDefinition1.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition2.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition3.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		dataSetDefinition4.addFilter(adultHivProgramCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
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
			
		InStateCohortDefinition onARTStatusCohort = new InStateCohortDefinition();
		onARTStatusCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow txStatus = hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_STATUS_ID);
		
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
		
		
		
				
		SqlCohortDefinition patientsNotVoided = new SqlCohortDefinition("select distinct p.patient_id from patient p where p.voided=0");
		dataSetDefinition1.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition2.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition3.addFilter(patientsNotVoided,new HashMap<String, Object>());
		dataSetDefinition4.addFilter(patientsNotVoided,new HashMap<String, Object>());
		
		
		SqlCohortDefinition patientsWithAnyEncounterNotVoided = new SqlCohortDefinition("select distinct e.patient_id from encounter e where e.voided=0");
		
		String clinicalEncTypesIds=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestIncl");
		String[] clinicalEncTypesIdsList=clinicalEncTypesIds.split(",");
		List<EncounterType> clinicalEncounterTypes=new ArrayList<EncounterType>();
		for(String id:clinicalEncTypesIdsList){			
			if(Context.getEncounterService().getEncounterType(Integer.parseInt(id))!=null)
			clinicalEncounterTypes.add(Context.getEncounterService().getEncounterType(Integer.parseInt(id)));
		}
		
		EncounterCohortDefinition patientsWithClinicalEncounters=new EncounterCohortDefinition();
		patientsWithClinicalEncounters.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithClinicalEncounters.setEncounterTypeList(clinicalEncounterTypes);
		
		CompositionCohortDefinition patientsWithAnyEncounterInOneYear=new CompositionCohortDefinition();
		patientsWithAnyEncounterInOneYear.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithAnyEncounterInOneYear.getSearches().put("patientsWithAnyEncounterNotVoided", new Mapped<CohortDefinition>(patientsWithAnyEncounterNotVoided,null));
		patientsWithAnyEncounterInOneYear.getSearches().put("patientsWithAnyEncounter", new Mapped<CohortDefinition>(patientsWithClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithAnyEncounterInOneYear.setCompositionString("patientsWithAnyEncounterNotVoided AND patientsWithAnyEncounter");
		
		
		dataSetDefinition1.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition2.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		dataSetDefinition3.addFilter(patientsWithAnyEncounterInOneYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
	
		
		
		// Patients without Any clinical Encounter(Test lab excluded) in last three months.
		
		String clinicalEncTypesIdsWithoutLabTest=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestExcl");
		String[] clinicalEncTypesIdsWithoutLabTestList=clinicalEncTypesIdsWithoutLabTest.split(",");
		List<EncounterType> clinicalEncounterTypesWithoutLabTest=new ArrayList<EncounterType>();
		for(String id:clinicalEncTypesIdsWithoutLabTestList){			
			if(Context.getEncounterService().getEncounterType(Integer.parseInt(id))!=null)
			clinicalEncounterTypesWithoutLabTest.add(Context.getEncounterService().getEncounterType(Integer.parseInt(id)));
		}
		
		EncounterCohortDefinition patientsWithClinicalEncountersWithoutLabTest=new EncounterCohortDefinition();
		patientsWithClinicalEncountersWithoutLabTest.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithClinicalEncountersWithoutLabTest.setEncounterTypeList(clinicalEncounterTypesWithoutLabTest);
		
		CompositionCohortDefinition patientsWithoutClinicalEncounters=new CompositionCohortDefinition();
		patientsWithoutClinicalEncounters.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithoutClinicalEncounters.getSearches().put("patientsWithClinicalEncountersWithoutLabTest", new Mapped<CohortDefinition>(patientsWithClinicalEncountersWithoutLabTest,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithoutClinicalEncounters.setCompositionString("NOT patientsWithClinicalEncountersWithoutLabTest");
		dataSetDefinition1.addFilter(patientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m}"));
				
		
		//==================================================================
		//                 2. Pediatric Pre-ART late visit
		//==================================================================
		
		// Following state cohort definition.
		
		
		InStateCohortDefinition followingStatusCohort = new InStateCohortDefinition();
		followingStatusCohort.addParameter(new Parameter("onDate","On date",Date.class));
		List<ProgramWorkflowState> followingstates = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow followingtxStatus = hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_STATUS_ID);
		
		ProgramWorkflowState following = null;
		if(txStatus != null)
		{
			following = followingtxStatus.getState(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.FOLLOWING_UUID));
			if(following != null)
			{
				followingstates.add(following);
				followingStatusCohort.setStates(followingstates);
				dataSetDefinition2.addFilter(followingStatusCohort,ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
			}
		}		
		
		// Patients without Any clinical Encounter(Test lab excluded) in last six months.
		dataSetDefinition2.addFilter(patientsWithoutClinicalEncounters,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		
			
			

		//==================================================================
		//                 3. Pediatric HIV late CD4 count
		//==================================================================
		
		NumericObsCohortDefinition cd4CohortDefinition=new NumericObsCohortDefinition();
		cd4CohortDefinition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		cd4CohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		cd4CohortDefinition.setTimeModifier(TimeModifier.ANY);
		cd4CohortDefinition.setOperator1(null);
		
		CompositionCohortDefinition patientsWithouthCD4RecordComposition=new CompositionCohortDefinition();
		patientsWithouthCD4RecordComposition.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithouthCD4RecordComposition.getSearches().put("cd4CohortDefinition", new Mapped<CohortDefinition>(cd4CohortDefinition,ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithouthCD4RecordComposition.setCompositionString("NOT cd4CohortDefinition");
		
		dataSetDefinition3.addFilter(patientsWithouthCD4RecordComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m}"));
		

		//==================================================================
		//                 4. Pediatric HIV lost to follow-up
		//==================================================================
		
		//Patients with no encounters of any kind in the past year
		
		
		//SqlCohortDefinition patientsWithoutEncountersInPastYear = new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_datetime < '"+sdf1.format(dateOneYearAgo)+"' and p.patient_id not in (select patient_id from encounter where encounter_datetime >= '"+sdf1.format(dateOneYearAgo)+"' and voided = 0)");
		//SqlCohortDefinition patientsWithEncountersInPastYear = new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_datetime >= '"+sdf1.format(dateOneYearAgo)+"'");
		InverseCohortDefinition patientsWithoutEncountersInPastYear=new InverseCohortDefinition(patientsWithClinicalEncounters);
		dataSetDefinition4.addFilter(patientsWithoutEncountersInPastYear,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));		
		
		
		
		
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
		
		PatientProperty givenName = new PatientProperty("givenName");
		givenName.setName("First Name");
		givenName.setDescription("First Name");
		dataSetDefinition1.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(givenName,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(givenName,new HashMap<String, Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		familyName.setName("Last Name");
		familyName.setDescription("Last Name");
		dataSetDefinition1.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(familyName,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(familyName,new HashMap<String, Object>());
		
		PatientProperty gender = new PatientProperty("gender");
		gender.setName("Sex");
		gender.setDescription("Sex");
		dataSetDefinition1.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(gender,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(gender,new HashMap<String, Object>());
		
		PatientDateOfBirth birthdate = new PatientDateOfBirth();
		birthdate.setName("Date of Birth");
		birthdate.setDescription("Date of Birth");
		dataSetDefinition1.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(birthdate,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(birthdate,new HashMap<String, Object>());
		
		
		StateOfPatient txGroup=new StateOfPatient();
		txGroup.setPatientProgram(hadultHivProgram);
		txGroup.setPatienProgramWorkflow(hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_GROUP_ID));
		txGroup.setName("Group");
		txGroup.setDescription("Group");
		txGroup.setFilter(new GroupStateFilter());
		dataSetDefinition1.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(txGroup,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(txGroup,new HashMap<String, Object>());
				
		StateOfPatient stOfPatient=new StateOfPatient();
		stOfPatient.setPatientProgram(hadultHivProgram);
		stOfPatient.setPatienProgramWorkflow(hadultHivProgram.getWorkflow(LateVisitAndCD4ReportConstant.PEDI_TREATMENT_STATUS_ID));
		stOfPatient.setName("Treatment");
		stOfPatient.setDescription("Treatment");
		stOfPatient.setFilter(new TreatmentStateFilter());
		dataSetDefinition1.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(stOfPatient,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(stOfPatient,new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType=new RecentEncounterType();
		lastEncounterType.setName("Last visit type");
		lastEncounterType.setDescription("Last visit type");
		lastEncounterType.setFilter(new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(lastEncounterType,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(lastEncounterType,new HashMap<String, Object>());
		
		
		ReturnVisitDate returnVisitDate=new ReturnVisitDate();
		returnVisitDate.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.RETURN_VISIT_DATE_UUID));
		returnVisitDate.setName("Date of missed appointment");
		returnVisitDate.setDescription("Date of missed appointment");
		dataSetDefinition1.addColumn(returnVisitDate,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(returnVisitDate,new HashMap<String, Object>());
		
		
		MostRecentObservation cd4Count=new MostRecentObservation();
		cd4Count.setConcept(Context.getConceptService().getConceptByUuid(LateVisitAndCD4ReportConstant.CD4_COUNT_UUID));
		cd4Count.setName("Most recent CD4");
		cd4Count.setDescription("Most recent CD4");
		dataSetDefinition1.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(cd4Count,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(cd4Count,new HashMap<String, Object>());
		
		PatientRelationship accompagnateur=new PatientRelationship();
		accompagnateur.setRelationshipTypeId(LateVisitAndCD4ReportConstant.RELATIONSHIP_TYPE_ID);
		accompagnateur.setName("Accompagnateur");
		accompagnateur.setDescription("Accompagnateur");
		dataSetDefinition1.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition2.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition3.addColumn(accompagnateur,new HashMap<String, Object>());
		dataSetDefinition4.addColumn(accompagnateur,new HashMap<String, Object>());
		
				
		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
		
		
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Location.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Location.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Location.class));
		dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Location.class));
		
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("PediatricARTLateVisit", dataSetDefinition1, mappings);
		reportDefinition.addDataSetDefinition("PediatricPreARTLateVisit", dataSetDefinition2, mappings);
		reportDefinition.addDataSetDefinition("PediatricHIVLateCD4Count", dataSetDefinition3, mappings);
		reportDefinition.addDataSetDefinition("PediatricHIVLostToFollowup", dataSetDefinition4, mappings);
		
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("location: HIV Pedi Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}	
}
