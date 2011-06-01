package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfProgramEnrolment;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultipleConceptObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationPresentInMostRecentOrder;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAgeInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.customcalculators.PMTCTInfantDBSDue;
import org.openmrs.module.rwandareports.customcalculators.StartOfARTForThisPMTCT;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DiscordantCoupleFilter;
import org.openmrs.module.rwandareports.filter.PMTCTDbsTestOrderedFilter;

public class SetupCombinedHFCSPConsultationReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupCombinedHFCSPConsultationReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		h.createRowPerPatientXlsOverview(rd, "PMTCTCombinedClinicConsultationSheet.xls", "PMTCTCombinedClinicConsultationSheet.xls_", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTCombinedClinicConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Combined HFCSP consultation");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "Combined HFCSP consultation Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "PMTCTCombinedLocation: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Combined HFCSP consultation");
		
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		//reportDefinition.addParameter(new Parameter("state", "Feeding Group", ProgramWorkflowState.class,  properties.get("PMTCT_COMBINED_CLINIC_PROGRAM")));
		reportDefinition.addParameter(new Parameter("date", "Week starting on", Date.class));
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("PMTCTCombinedLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		dataSetDefinition.addParameter(new Parameter("state", "State", ProgramWorkflowState.class));
		dataSetDefinition.addParameter(new Parameter("date", "Date", Date.class));
		
		InStateCohortDefinition feedingStatus = new InStateCohortDefinition();
		feedingStatus.addParameter(new Parameter("states", "States", ProgramWorkflowState.class));
		feedingStatus.setName("feeding state: Feeding state of patients");
		dataSetDefinition.addFilter(feedingStatus, ParameterizableUtil.createParameterMappings("states=${state}"));
		
		InProgramCohortDefinition inPMTCTProgram = new InProgramCohortDefinition();
		inPMTCTProgram.setName("pmtct: Combined Clinic In Program");
		List<Program> programs = new ArrayList<Program>();
		Program pmtctCombined = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_COMBINED_CLINIC_PROGRAM"));
		if(pmtctCombined != null)
		{
			programs.add(pmtctCombined);
		}
		inPMTCTProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inPMTCTProgram, new HashMap<String,Object>());
		
		Concept nextVisitConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("PMTCT_NEXT_VISIT_CONCEPT_ID")));
		
		DateObsCohortDefinition dueThatWeek = new DateObsCohortDefinition();
		dueThatWeek.setOperator1(RangeComparator.GREATER_EQUAL);
		dueThatWeek.setOperator2(RangeComparator.LESS_EQUAL);
		dueThatWeek.setTimeModifier(TimeModifier.ANY);
		dueThatWeek.addParameter(new Parameter("value1", "value1", Date.class));
		dueThatWeek.addParameter(new Parameter("value2", "value2", Date.class));
		dueThatWeek.setName("patients due that week");
		dueThatWeek.setGroupingConcept(nextVisitConcept);
		dataSetDefinition.addFilter(dueThatWeek, ParameterizableUtil.createParameterMappings("value1=${date},value2=${date+7d}"));
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName, new HashMap<String,Object>());
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("IMB_IDENTIFIER_TYPE"));
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbType.setPatientIdentifierTypeId(imbId.getId());
		
		PatientIdentifierType pcType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("PRIMARY_CARE_IDENTIFIER_TYPE"));
		PatientIdentifier pcId = new PatientIdentifier(pcType);
		pcType.setPatientIdentifierTypeId(pcId.getId());
		
		MultiplePatientDataDefinitions infantId = new MultiplePatientDataDefinitions();
		infantId.setName("InfantId");
		infantId.addPatientDataDefinition(imbId, new HashMap<String,Object>());
		infantId.addPatientDataDefinition(pcId, new HashMap<String,Object>());
		dataSetDefinition.addColumn(infantId, new HashMap<String,Object>());
		
		RetrievePersonByRelationship mother = new RetrievePersonByRelationship();
		mother.setRelationshipTypeId(Integer.valueOf(properties.get("PMTCT_MOTHER_RELATIONSHIP_ID")));
		mother.setRetrievePersonAorB("A");
		
		PatientRelationship motherName = new PatientRelationship();
		motherName.setName("MotherName");
		motherName.setRelationshipTypeId(Integer.valueOf(properties.get("PMTCT_MOTHER_RELATIONSHIP_ID")));
		motherName.setRetrievePersonAorB("A");
		dataSetDefinition.addColumn(motherName, new HashMap<String,Object>());
		
		EvaluateDefinitionForOtherPersonData motherId = new EvaluateDefinitionForOtherPersonData();
		motherId.setPersonData(mother, new HashMap<String,Object>());
		motherId.setDefinition(infantId, new HashMap<String,Object>());
		motherId.setName("MotherId");
		motherId.setDescription("MotherId");
		dataSetDefinition.addColumn(motherId, new HashMap<String,Object>());
		
		PatientProperty birthdate = new PatientProperty("birthdate");
		dataSetDefinition.addColumn(birthdate, new HashMap<String,Object>());
		
		PatientAgeInMonths ageInMonths = new PatientAgeInMonths();
		ageInMonths.addParameter(new Parameter("onDate", "onDate", ProgramWorkflowState.class));
		dataSetDefinition.addColumn(ageInMonths, ParameterizableUtil.createParameterMappings("onDate=${date}"));
		
		StateOfPatient feedingGroup = new StateOfPatient();
		feedingGroup.setName("FeedingGroup");
		feedingGroup.setDescription("FeedingGroup");
		feedingGroup.setPatientProgram(pmtctCombined);
		feedingGroup.setPatienProgramWorkflow(pmtctCombined.getWorkflowByName(properties.get("PMTCT_FEEDING_STATUS_WORKFLOW")));
		dataSetDefinition.addColumn(feedingGroup, new HashMap<String,Object>());
		
		PatientRelationship accomp = new PatientRelationship();
		accomp.setName("AccompName");
		accomp.setRelationshipTypeId(Integer.valueOf(properties.get("ACCOMPAGNATUER_RELATIONSHIP_ID")));
		accomp.setRetrievePersonAorB("A");
		dataSetDefinition.addColumn(accomp, new HashMap<String,Object>());
		
		PatientAddress address = new PatientAddress();
		address.setName("Address");
		address.setIncludeCountry(false);
		address.setIncludeProvince(false);
		dataSetDefinition.addColumn(address, new HashMap<String,Object>());
		
		MostRecentObservation dbsTest = new MostRecentObservation();
		Concept dbsConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("DBS_CONCEPT_ID")));
		dbsTest.setConcept(dbsConcept);
		dbsTest.setName("DBSTest");
		dataSetDefinition.addColumn(dbsTest, new HashMap<String,Object>());
		
		StateOfPatient discordant = new StateOfPatient();
		discordant.setName("DiscordantCouple");
		discordant.setDescription("DiscordantCouple");
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_CLINIC_PROGRAM"));
		if(pmtct != null)
		{
			discordant.setPatientProgram(pmtct);
			discordant.setPatienProgramWorkflow(pmtct.getWorkflowByName(properties.get("PMTCT_RELATIONSHIP_STATUS_WORKFLOW")));
		}
		discordant.setFilter(new DiscordantCoupleFilter());
		
		EvaluateDefinitionForOtherPersonData motherDiscordant = new EvaluateDefinitionForOtherPersonData();
		motherDiscordant.setPersonData(mother, new HashMap<String,Object>());
		motherDiscordant.setDefinition(discordant, new HashMap<String,Object>());
		motherDiscordant.setName("motherDiscordant");
		motherDiscordant.setDescription("motherDiscordant");
		dataSetDefinition.addColumn(motherDiscordant, new HashMap<String,Object>());
		
		MostRecentObservation cd4Test = new MostRecentObservation();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		
		MostRecentObservation viralLoad = new MostRecentObservation();
		Concept viralLoadConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("VIRAL_LOAD_CONCEPT")));
		viralLoad.setConcept(viralLoadConcept);
		viralLoad.setName("ViralLoadTest");
	
		EvaluateDefinitionForOtherPersonData motherCD4 = new EvaluateDefinitionForOtherPersonData();
		motherCD4.setPersonData(mother, new HashMap<String,Object>());
		motherCD4.setDefinition(cd4Test, new HashMap<String,Object>());
		motherCD4.setName("motherCD4");
		motherCD4.setDescription("motherCD4");
		dataSetDefinition.addColumn(motherCD4, new HashMap<String,Object>());
		
		EvaluateDefinitionForOtherPersonData motherViralLoad = new EvaluateDefinitionForOtherPersonData();
		motherViralLoad.setPersonData(mother, new HashMap<String,Object>());
		motherViralLoad.setDefinition(viralLoad, new HashMap<String,Object>());
		motherViralLoad.setName("motherViralLoad");
		motherViralLoad.setDescription("motherViralLoad");
		dataSetDefinition.addColumn(motherViralLoad, new HashMap<String,Object>());
		
		MostRecentObservation hiv = new MostRecentObservation();
		Concept hivConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("HIV_TEST_CONCEPT")));
		hiv.setConcept(hivConcept);
		hiv.setName("HIVTest");
		
		EvaluateDefinitionForOtherPersonData motherHIVTest = new EvaluateDefinitionForOtherPersonData();
		motherHIVTest.setPersonData(mother, new HashMap<String,Object>());
		motherHIVTest.setDefinition(hiv, new HashMap<String,Object>());
		motherHIVTest.setName("motherHIV");
		motherHIVTest.setDescription("motherHIV");
		dataSetDefinition.addColumn(motherHIVTest, new HashMap<String,Object>());
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		
		CurrentOrdersRestrictedByConceptSet artDrugs = new CurrentOrdersRestrictedByConceptSet();
		artDrugs.addParameter(new Parameter("onDate", "OnDate", ProgramWorkflowState.class));
		artDrugs.setName("Regimen");
		artDrugs.setDescription("Regimen");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		
		EvaluateDefinitionForOtherPersonData motherARV = new EvaluateDefinitionForOtherPersonData();
		motherARV.setPersonData(mother, new HashMap<String,Object>());
		motherARV.setDefinition(artDrugs, ParameterizableUtil.createParameterMappings("onDate=${date}"));
		motherARV.setName("motherART");
		motherARV.setDescription("motherART");
		dataSetDefinition.addColumn(motherARV, new HashMap<String,Object>());
		
		MostRecentEncounterOfType encounters = new MostRecentEncounterOfType();
		EncounterType exposedEnc = Context.getEncounterService().getEncounterType(new Integer(properties.get("EXPOSED_ENCOUNTER_TYPE")));
		encounters.setName("LastPMTCTEncounter");
		encounters.setDescription("LastPMTCTEncounter");
		encounters.addEncounterType(exposedEnc);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions infantHivTestDue = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		infantHivTestDue.addPatientDataToBeEvaluated(encounters, new HashMap<String,Object>());
		infantHivTestDue.setCalculator(new PMTCTInfantDBSDue());
		infantHivTestDue.setName("infantTestDue");
		infantHivTestDue.setDescription("infantTestDue");
		dataSetDefinition.addColumn(infantHivTestDue, new HashMap<String,Object>());
		
		AllDrugOrdersRestrictedByConceptSet allArtDrugs = new AllDrugOrdersRestrictedByConceptSet();
		allArtDrugs.setName("ArtDrugs");
		allArtDrugs.setDescription("ArtDrugs");
		allArtDrugs.setDrugConceptSetConcept(artDrugsSet);
		
		EvaluateDefinitionForOtherPersonData motherStartARV = new EvaluateDefinitionForOtherPersonData();
		motherStartARV.setPersonData(mother, new HashMap<String,Object>());
		motherStartARV.setDefinition(allArtDrugs, new HashMap<String,Object>());
		motherStartARV.setName("ArtDrugs");
		motherStartARV.setDescription("ArtDrugs");
		
		DateOfProgramEnrolment motherRegistration = new DateOfProgramEnrolment();
		Program pmtctProg = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_PROGRAM"));
		motherRegistration.setName("motherRegistration");
		motherRegistration.setDescription("motherRegistration");
		motherRegistration.setProgramId(pmtctProg.getId());
		
		EvaluateDefinitionForOtherPersonData motherReg = new EvaluateDefinitionForOtherPersonData();
		motherReg.setPersonData(mother, new HashMap<String,Object>());
		motherReg.setDefinition(motherRegistration, new HashMap<String,Object>());
		motherReg.setName("motherRegistration");
		motherReg.setDescription("motherRegistration");
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions motherStartArt = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		motherStartArt.setName("motherStartArt");
		motherStartArt.setDescription("motherStartArt");
		motherStartArt.addPatientDataToBeEvaluated(motherReg, new HashMap<String,Object>());
		motherStartArt.addPatientDataToBeEvaluated(motherStartARV, new HashMap<String,Object>());
		motherStartArt.setCalculator(new StartOfARTForThisPMTCT());
		dataSetDefinition.addColumn(motherStartArt, new HashMap<String,Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("state", "${state}");
		mappings.put("date", "${date}");
		
		reportDefinition.addDataSetDefinition("Register", dataSetDefinition, mappings);
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("PMTCTCombinedLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctcombinedprogramname");
		properties.put("PMTCT_COMBINED_CLINIC_PROGRAM", pmtctProgram);
		
		String pmtctPro = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_CLINIC_PROGRAM", pmtctPro);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String pcIdentifierType = Context.getAdministrationService().getGlobalProperty("reports.primaryCareIdIdentifier");
		properties.put("PRIMARY_CARE_IDENTIFIER_TYPE", pcIdentifierType);
		
		String relationshipType = Context.getAdministrationService().getGlobalProperty("reports.pmtctMotherRelationship");
		properties.put("PMTCT_MOTHER_RELATIONSHIP_ID", relationshipType);
		
		String disCordRelationshipType = Context.getAdministrationService().getGlobalProperty("reports.pmtctRelationshipStatusWorkflowName");
		properties.put("PMTCT_RELATIONSHIP_STATUS_WORKFLOW", disCordRelationshipType);
		
		String feedingStatus = Context.getAdministrationService().getGlobalProperty("reports.pmtctFeedingStatusWorkflowName");
		properties.put("PMTCT_FEEDING_STATUS_WORKFLOW", feedingStatus);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
		
		String nextVisitConcept = Context.getAdministrationService().getGlobalProperty("reports.pmtctNextVisitConcept");
		properties.put("PMTCT_NEXT_VISIT_CONCEPT_ID", nextVisitConcept);
		
		String dbsConcept = Context.getAdministrationService().getGlobalProperty("reports.dbsConcept");
		properties.put("DBS_CONCEPT_ID", dbsConcept);
		
		String dbsInitialConcept = Context.getAdministrationService().getGlobalProperty("reports.dbsInitialConcept");
		properties.put("DBS_INITIAL_CONCEPT_ID", dbsInitialConcept);
		
		String dbsConfirmConcept = Context.getAdministrationService().getGlobalProperty("reports.dbsConfirmationConcept");
		properties.put("DBS_CONFIRM_CONCEPT_ID", dbsConfirmConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String viralLoadConcept = Context.getAdministrationService().getGlobalProperty("reports.viralLoadConcept");
		properties.put("VIRAL_LOAD_CONCEPT", viralLoadConcept);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String exposedType = Context.getAdministrationService().getGlobalProperty("reports.ExposedInfantEncounterType");
		properties.put("EXPOSED_ENCOUNTER_TYPE", exposedType);
		
		String hivConcept = Context.getAdministrationService().getGlobalProperty("reports.hivTestConcept");
		properties.put("HIV_TEST_CONCEPT", hivConcept);
		
		String pmtct = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_PROGRAM", pmtct);
	}	
	
}
