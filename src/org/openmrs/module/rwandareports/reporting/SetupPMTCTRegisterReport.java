package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfMostRecentObservationHavingCodedAnswer;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfProgramEnrolment;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ReasonForExitingProgram;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.customcalculators.ArtTakenDuringBreastFeeding;
import org.openmrs.module.rwandareports.customcalculators.StartOfARTForThisPMTCT;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition2;
import org.openmrs.module.rwandareports.dataset.PMTCTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DiscordantCoupleFilter;

public class SetupPMTCTRegisterReport {
	
	Helper h = new Helper();
	
	boolean pedi = false;
	
	private HashMap<String, String> properties;
	
	public SetupPMTCTRegisterReport(Helper helper, boolean pedi) {
		h = helper;
		
		this.pedi = pedi;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		h.createRowPerPatientXlsOverview(rd, "TRACMotherInfantRegister.xls", "TRACMotherInfantRegister.xls_", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("TRACMotherInfantRegister.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		
		h.purgeDefinition(ReportDefinition.class, "PMTCT Register");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition2.class, "PMTCT Register Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "location: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		
		reportDefinition.setName("PMTCT Register");
	
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("location: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PMTCTRegisterDataSetDefinition dataSetDefinition = new PMTCTRegisterDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		
		InProgramCohortDefinition inPMTCTCombinedProgram = new InProgramCohortDefinition();
		inPMTCTCombinedProgram.setName("pmtct: In PMTCT Combined Infant Program");
		List<Program> pmtctPrograms = new ArrayList<Program>();
		Program pmtctCombined = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_COMBINED_CLINIC_PROGRAM"));
		if(pmtctCombined != null)
		{
			pmtctPrograms.add(pmtctCombined);
		}
		inPMTCTCombinedProgram.setPrograms(pmtctPrograms);
		inPMTCTCombinedProgram.setOnOrBefore(new Date());
		dataSetDefinition.addFilter(inPMTCTCombinedProgram);
		
		DateOfProgramEnrolment infantRegistration = new DateOfProgramEnrolment();
		infantRegistration.setName("infantRegistration");
		infantRegistration.setDescription("infantDescription");
		infantRegistration.setProgramId(pmtctCombined.getId());
		dataSetDefinition.addColumn(infantRegistration);
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName);
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName);
		
		PatientProperty birthdate = new PatientProperty("birthdate");
		dataSetDefinition.addColumn(birthdate);
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("IMB_IDENTIFIER_TYPE"));
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbType.setPatientIdentifierTypeId(imbId.getId());
		
		PatientIdentifierType tracType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("TRACNET_IDENTIFIER_TYPE"));
		PatientIdentifier tracId = new PatientIdentifier(tracType);
		tracType.setPatientIdentifierTypeId(tracId.getId());
		
		MultiplePatientDataDefinitions id = new MultiplePatientDataDefinitions();
		id.setName("id");
		id.addPatientDataDefinition(imbId, new HashMap<String,Object>());
		id.addPatientDataDefinition(tracId, new HashMap<String,Object>());
		
		RetrievePersonByRelationship mother = new RetrievePersonByRelationship();
		mother.setRelationshipTypeId(Integer.valueOf(properties.get("PMTCT_MOTHER_RELATIONSHIP_ID")));
		mother.setRetrievePersonAorB("A");
		
		EvaluateDefinitionForOtherPersonData motherId = new EvaluateDefinitionForOtherPersonData();
		motherId.setPersonData(mother, new HashMap<String,Object>());
		motherId.setDefinition(id, new HashMap<String,Object>());
		motherId.setName("MotherId");
		motherId.setDescription("MotherId");
		dataSetDefinition.addColumn(motherId);
		
		PatientProperty gender = new PatientProperty("gender");
		dataSetDefinition.addColumn(gender);
		
		MostRecentObservation birthWeight = new MostRecentObservation();
		Concept birthWeightConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("BIRTH_WEIGHT_CONCEPT_ID")));
		birthWeight.setName("birthWeight");
		birthWeight.setDescription("birthWeight");
		birthWeight.setConcept(birthWeightConcept);
		dataSetDefinition.addColumn(birthWeight);
		
		MostRecentObservation infantFeedingMethod = new MostRecentObservation();
		Concept infantFeedingMethodConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("INFANT_FEEDING_METHOD_CONCEPT_ID")));
		infantFeedingMethod.setName("infantFeedingMethod");
		infantFeedingMethod.setDescription("infantFeedingMethod");
		infantFeedingMethod.setConcept(infantFeedingMethodConcept);
		dataSetDefinition.addColumn(infantFeedingMethod);
		
		AllObservationValues hiv = new AllObservationValues();
		Concept hivConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("HIV_TEST_CONCEPT")));
		hiv.setConcept(hivConcept);
		hiv.setName("HIVTest");
		
		EvaluateDefinitionForOtherPersonData motherHIVTest = new EvaluateDefinitionForOtherPersonData();
		motherHIVTest.setPersonData(mother, new HashMap<String,Object>());
		motherHIVTest.setDefinition(hiv, new HashMap<String,Object>());
		motherHIVTest.setName("motherHIV");
		motherHIVTest.setDescription("motherHIV");
		dataSetDefinition.addColumn(motherHIVTest);
		
		AllDrugOrdersRestrictedByConcept ctx = new AllDrugOrdersRestrictedByConcept();
		ctx.setName("CTX treatment");
		ctx.setDescription("CTX treatment");
		Concept ctxConcept = Context.getConceptService().getConcept(new Integer(properties.get("CTX_TREATMENT_CONCEPT")));
		ctx.setConcept(ctxConcept);
		dataSetDefinition.addColumn(ctx);
		
		AllObservationValues testType = new AllObservationValues();
		Concept testTypeConcept = Context.getConceptService().getConcept(new Integer(properties.get("HIV_TEST_TYPE_CONCEPT")));
		testType.setName("HivTestTypes");
		testType.setDescription("HivTestTypes");
		testType.setConcept(testTypeConcept);
		dataSetDefinition.addColumn(testType);
		
		AllObservationValues testResults = new AllObservationValues();
		testResults.setName("testResults");
		testResults.setDescription("testResults");
		testResults.setConcept(hivConcept);
		dataSetDefinition.addColumn(testResults);
		
		AllObservationValues testDate = new AllObservationValues();
		Concept testDateConcept = Context.getConceptService().getConcept(new Integer(properties.get("HIV_TEST_DATE_CONCEPT")));
		testDate.setName("testDates");
		testDate.setDescription("testDates");
		testDate.setConcept(testDateConcept);
		dataSetDefinition.addColumn(testDate);
		
		Concept breastFedStatusConcept = Context.getConceptService().getConcept(new Integer(properties.get("BREAST_FED_CONCEPT")));
		Concept weanedConcept = Context.getConceptService().getConcept(new Integer(properties.get("WEANED_CONCEPT")));
		DateOfMostRecentObservationHavingCodedAnswer dateWeaned = new DateOfMostRecentObservationHavingCodedAnswer();
		dateWeaned.setName("dateWeaned");
		dateWeaned.setDescription("dateWeaned");
		dateWeaned.addConcept(breastFedStatusConcept);
		dateWeaned.addAnswer(weanedConcept);
		dataSetDefinition.addColumn(dateWeaned);
		
		ReasonForExitingProgram exitingPMTCTCombined = new ReasonForExitingProgram();
		ProgramWorkflow treatmentWF = pmtctCombined.getWorkflowByName(properties.get("TREATMENT_STATUS"));
		exitingPMTCTCombined.setPatientProgram(pmtctCombined);
		exitingPMTCTCombined.setReasonWorkflow(treatmentWF);
		exitingPMTCTCombined.setName("ReasonForExit");
		exitingPMTCTCombined.setDescription("ReasonForExit");
		dataSetDefinition.addColumn(exitingPMTCTCombined);
		
		DateOfWorkflowStateChange preArt = new DateOfWorkflowStateChange();
		Concept artConcept = Context.getConceptService().getConcept(new Integer(properties.get("PRE_ART_CONCEPT")));
		preArt.setConcept(artConcept);
		preArt.setName("preArt");
		preArt.setDescription("preArt");
		dataSetDefinition.addColumn(preArt);
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
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
		dataSetDefinition.addColumn(motherStartArt);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions arvDuringBreastFeeding = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		arvDuringBreastFeeding.setName("arvDuringBreastFeeding");
		arvDuringBreastFeeding.setDescription("arvDuringBreastFeeding");
		arvDuringBreastFeeding.addPatientDataToBeEvaluated(birthdate, new HashMap<String,Object>());
		arvDuringBreastFeeding.addPatientDataToBeEvaluated(dateWeaned, new HashMap<String,Object>());
		arvDuringBreastFeeding.addPatientDataToBeEvaluated(motherStartARV, new HashMap<String,Object>());
		arvDuringBreastFeeding.setCalculator(new ArtTakenDuringBreastFeeding());
		dataSetDefinition.addColumn(arvDuringBreastFeeding);
		
		StateOfPatient discordant = new StateOfPatient();
		discordant.setName("DiscordantCouple");
		discordant.setDescription("DiscordantCouple");
		if(pmtctProg != null)
		{
			discordant.setPatientProgram(pmtctProg);
			discordant.setPatienProgramWorkflow(pmtctProg.getWorkflowByName(properties.get("PMTCT_RELATIONSHIP_STATUS_WORKFLOW")));
		}
		discordant.setFilter(new DiscordantCoupleFilter());
		
		EvaluateDefinitionForOtherPersonData motherDiscordant = new EvaluateDefinitionForOtherPersonData();
		motherDiscordant.setPersonData(mother, new HashMap<String,Object>());
		motherDiscordant.setDefinition(discordant, new HashMap<String,Object>());
		motherDiscordant.setName("motherDiscordant");
		motherDiscordant.setDescription("motherDiscordant");
		dataSetDefinition.addColumn(motherDiscordant);

		AllObservationValues cd4Test = new AllObservationValues();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
	
		EvaluateDefinitionForOtherPersonData motherCD4 = new EvaluateDefinitionForOtherPersonData();
		motherCD4.setPersonData(mother, new HashMap<String,Object>());
		motherCD4.setDefinition(cd4Test, new HashMap<String,Object>());
		motherCD4.setName("motherCD4");
		motherCD4.setDescription("motherCD4");
		dataSetDefinition.addColumn(motherCD4);
		
		
		
		
//		PatientRelationship motherName = new PatientRelationship();
//		motherName.setName("MotherName");
//		motherName.setRelationshipTypeId(Integer.valueOf(properties.get("PMTCT_MOTHER_RELATIONSHIP_ID")));
//		motherName.setRetrievePersonAorB("A");
//		dataSetDefinition.addColumn(motherName, new HashMap<String,Object>());
//		
//		DateOfWorkflowStateChange startDate = new DateOfWorkflowStateChange();
//		Concept artConcept = Context.getConceptService().getConcept(new Integer(1577));
//		startDate.setConcept(artConcept);
//		startDate.setName("Commencement of ART");
//		startDate.setDescription("Commencement of ART");
//		dataSetDefinition.addColumn(startDate);
//		
//		FirstDrugOrderStartedRestrictedByConceptSet startDateDrugs = new FirstDrugOrderStartedRestrictedByConceptSet();
//		startDateDrugs.setName("Start ART Regimen");
//		startDateDrugs.setDescription("Start ART Regimen");
//		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
//		startDateDrugs.setDrugConceptSetConcept(artDrugsSet);
//		dataSetDefinition.addColumn(startDateDrugs);
//		
//		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
//		PatientIdentifier imbId = new PatientIdentifier(imbType);
//		dataSetDefinition.addColumn(imbId);
//		
//		PatientIdentifierType tracNetType = Context.getPatientService().getPatientIdentifierTypeByName("TRACnet ID");
//		PatientIdentifier tracNetId = new PatientIdentifier(tracNetType);
//		tracNetId.setName("TracNetID");
//		tracNetId.setDescription("TracNetId");
//		dataSetDefinition.addColumn(tracNetId);
//		
//		
//		
//		
//		
//		PatientProperty birthdate = new PatientProperty("birthdate");
//		dataSetDefinition.addColumn(birthdate);
//		
//		AgeAtDateOfOtherDefinition ageAtStart = new AgeAtDateOfOtherDefinition();
//		ageAtStart.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		dataSetDefinition.addColumn(ageAtStart);
//		
//		ObsValueAfterDateOfOtherDefinition weightAtStart = new ObsValueAfterDateOfOtherDefinition();
//		Concept weight = Context.getConceptService().getConcept(new Integer(properties.get("WEIGHT_CONCEPT")));
//		weightAtStart.setConcept(weight);
//		weightAtStart.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		dataSetDefinition.addColumn(weightAtStart);
//		
//		ObsValueAfterDateOfOtherDefinition stageAtStart = new ObsValueAfterDateOfOtherDefinition();
//		Concept stage = Context.getConceptService().getConcept(new Integer(properties.get("STAGE_CONCEPT")));
//		stageAtStart.setConcept(stage);
//		stageAtStart.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		stageAtStart.setName("Initial stage");
//		stageAtStart.setDescription("Initial stage");
//		dataSetDefinition.addColumn(stageAtStart);
//		
//		ObsValueBeforeDateOfOtherDefinition cd4CountAtStartBefore = new ObsValueBeforeDateOfOtherDefinition();
//		Concept cd4 = Context.getConceptService().getConcept(new Integer(properties.get("CD4_CONCEPT")));
//		cd4CountAtStartBefore.setConcept(cd4);
//		cd4CountAtStartBefore.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		cd4CountAtStartBefore.setName("Initial CD4 count");
//		cd4CountAtStartBefore.setDescription("Initial CD4 count");
//		
//		ObsValueAfterDateOfOtherDefinition cd4CountAtStartAfter = new ObsValueAfterDateOfOtherDefinition();
//		cd4CountAtStartAfter.setConcept(cd4);
//		cd4CountAtStartAfter.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		cd4CountAtStartAfter.setName("Initial CD4 count");
//		cd4CountAtStartAfter.setDescription("Initial CD4 count");
//		
//		MultiplePatientDataDefinitions cd4CountAtStart = new MultiplePatientDataDefinitions();
//		cd4CountAtStart.setName("Initial CD4 count");
//		cd4CountAtStart.setDescription("Initial CD4 count");
//		cd4CountAtStart.addPatientDataDefinition(cd4CountAtStartBefore, new HashMap<String,Object>());
//		cd4CountAtStart.addPatientDataDefinition(cd4CountAtStartAfter, new HashMap<String,Object>());
//		dataSetDefinition.addColumn(cd4CountAtStart);
//		
//		ObsValueBeforeDateOfOtherDefinition cd4PercentAtStart = new ObsValueBeforeDateOfOtherDefinition();
//		Concept cd4percentage = Context.getConceptService().getConcept(new Integer(properties.get("CD4_PERCENTAGE_CONCEPT")));
//		cd4PercentAtStart.setConcept(cd4percentage);
//		cd4PercentAtStart.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		dataSetDefinition.addColumn(cd4PercentAtStart);
//		
//		AllDrugOrdersRestrictedByConcept ctx = new AllDrugOrdersRestrictedByConcept();
//		ctx.setName("CTX treatment");
//		ctx.setDescription("CTX treatment");
//		Concept ctxConcept = Context.getConceptService().getConcept(new Integer(properties.get("CTX_TREATMENT_CONCEPT")));
//		ctx.setConcept(ctxConcept);
//		dataSetDefinition.addColumn(ctx);
//		
//		AllDrugOrdersRestrictedByConcept tbTreatment = new AllDrugOrdersRestrictedByConcept();
//		tbTreatment.setName("TB Treatment");
//		tbTreatment.setDescription("TB Treatment");
//		Concept tbDrugConcept = Context.getConceptService().getConcept(new Integer(properties.get("TB_TREATMENT_CONCEPT")));
//		tbTreatment.setConcept(tbDrugConcept);
//		dataSetDefinition.addColumn(tbTreatment);
//		
//		AllObservationValues deliveryDate = new AllObservationValues();
//		Concept delivery = Context.getConceptService().getConcept(new Integer(properties.get("PREGNANCY_DELIVERY_DATE_CONCEPT")));
//		deliveryDate.setConcept(delivery);
//		dataSetDefinition.addColumn(deliveryDate);
//		
//		FirstDrugOrderStartedAfterDateRestrictedByConceptSet artDrugsInitial = new FirstDrugOrderStartedAfterDateRestrictedByConceptSet();
//		artDrugsInitial.setName("Initial Regimen");
//		artDrugsInitial.setDescription("Initial Regimen");
//		artDrugsInitial.setDrugConceptSetConcept(artDrugsSet);
//		artDrugsInitial.setDateOfPatientData(startDate, new HashMap<String,Object>());
//		dataSetDefinition.addColumn(artDrugsInitial);
//		
//		AllDrugOrdersRestrictedByConceptSet firstLineChanges = new AllDrugOrdersRestrictedByConceptSet();
//		firstLineChanges.setName("First Line Changes");
//		firstLineChanges.setDescription("First Line Changes");
//		Concept firstLineDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_FIRST_LINE_ART_DRUGS_CONCEPT")));
//		firstLineChanges.setDrugConceptSetConcept(firstLineDrugsSet);
//		dataSetDefinition.addColumn(firstLineChanges);
//		
//		AllDrugOrdersRestrictedByConceptSet secondLineChanges = new AllDrugOrdersRestrictedByConceptSet();
//		secondLineChanges.setName("Second Line Changes");
//		secondLineChanges.setDescription("Second Line Changes");
//		Concept secondLineDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_SECOND_LINE_ART_DRUGS_CONCEPT")));
//		secondLineChanges.setDrugConceptSetConcept(secondLineDrugsSet);
//		dataSetDefinition.addColumn(secondLineChanges);
//		
//		AllDrugOrdersRestrictedByConceptSet artDrugs = new AllDrugOrdersRestrictedByConceptSet();
//		artDrugs.setName("ART Drugs");
//		artDrugs.setDescription("ART Drugs");
//		artDrugs.setDrugConceptSetConcept(artDrugsSet);
//		dataSetDefinition.addColumn(artDrugs);
//		
//		AllObservationValues cd4Ongoing = new AllObservationValues();
//		cd4Ongoing.setConcept(cd4);
//		dataSetDefinition.addColumn(cd4Ongoing);
//		
//		AllObservationValues whoStage = new AllObservationValues();
//		whoStage.setConcept(stage);
//		dataSetDefinition.addColumn(whoStage);
//		
//		AllObservationValues tbStatus = new AllObservationValues();
//		Concept tb = Context.getConceptService().getConcept(new Integer(properties.get("TB_TEST_CONCEPT")));
//		tbStatus.setConcept(tb);
//		dataSetDefinition.addColumn(tbStatus);
//		
//		//dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		//mappings.put("location", "${location}");
		
		reportDefinition.addDataSetDefinition("Register", dataSetDefinition, mappings);
		
		//h.replaceDataSetDefinition(dataSetDefinition);
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location");
		location.setName("location: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctcombinedprogramname");
		properties.put("PMTCT_COMBINED_CLINIC_PROGRAM", pmtctProgram);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String tracType = Context.getAdministrationService().getGlobalProperty("reports.tracIdentifier");
		properties.put("TRAC_IDENTIFIER_TYPE", tracType);
		
		String relationshipType = Context.getAdministrationService().getGlobalProperty("reports.pmtctMotherRelationship");
		properties.put("PMTCT_MOTHER_RELATIONSHIP_ID", relationshipType);
		
		String birthWeightConcept = Context.getAdministrationService().getGlobalProperty("reports.birthWeightConcept");
		properties.put("BIRTH_WEIGHT_CONCEPT_ID", birthWeightConcept);
		
		String infantFeedingMethodConcept = Context.getAdministrationService().getGlobalProperty("reports.infantFeedingMethodConcept");
		properties.put("INFANT_FEEDING_METHOD_CONCEPT_ID", infantFeedingMethodConcept);
		
		String hivConcept = Context.getAdministrationService().getGlobalProperty("reports.hivTestConcept");
		properties.put("HIV_TEST_CONCEPT", hivConcept);
		
		String ctxTreatmentConcept = Context.getAdministrationService().getGlobalProperty("reports.ctxTreatmentConcept");
		properties.put("CTX_TREATMENT_CONCEPT", ctxTreatmentConcept);
		
		String hivTestType = Context.getAdministrationService().getGlobalProperty("reports.infantHivTestTypeConcept");
		properties.put("HIV_TEST_TYPE_CONCEPT", hivTestType);
		
		String hivTestDate = Context.getAdministrationService().getGlobalProperty("reports.hivTestDateConcept");
		properties.put("HIV_TEST_DATE_CONCEPT", hivTestDate);
		
		String breastFedConcept = Context.getAdministrationService().getGlobalProperty("reports.breastFedStatusConcept");
		properties.put("BREAST_FED_CONCEPT", breastFedConcept);
		
		String weanedConcept = Context.getAdministrationService().getGlobalProperty("reports.weanedConcept");
		properties.put("WEANED_CONCEPT", weanedConcept);
		
		String treatmentStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatus");
		properties.put("TREATMENT_STATUS", treatmentStatus);
		
		String preArtStatusConcept = Context.getAdministrationService().getGlobalProperty("reports.preArtStatusConcept");
		properties.put("PRE_ART_CONCEPT", preArtStatusConcept);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String pmtct = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_PROGRAM", pmtct);
		
		String disCordRelationshipType = Context.getAdministrationService().getGlobalProperty("reports.pmtctRelationshipStatusWorkflowName");
		properties.put("PMTCT_RELATIONSHIP_STATUS_WORKFLOW", disCordRelationshipType);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		String hivProgram = Context.getAdministrationService().getGlobalProperty("reports.hivprogramname");
		properties.put("HIV_PROGRAM", hivProgram);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
		
		String workflowStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatus");
		properties.put("HIV_WORKFLOW_STATUS", workflowStatus);
		
		String onARTState = Context.getAdministrationService().getGlobalProperty("reports.hivonartstate");
		properties.put("HIV_ON_ART_STATE", onARTState);
		
		
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
		
		String stageConcept = Context.getAdministrationService().getGlobalProperty("reports.stageConcept");
		properties.put("STAGE_CONCEPT", stageConcept);
		
		
		
		String cd4PercentageConcept = Context.getAdministrationService().getGlobalProperty("reports.cd4PercentageConcept");
		properties.put("CD4_PERCENTAGE_CONCEPT", cd4PercentageConcept);
		
		
		
		String tbTreatmentConcept = Context.getAdministrationService().getGlobalProperty("reports.tbTreatmentConcept");
		properties.put("TB_TREATMENT_CONCEPT", tbTreatmentConcept);
		
		String pregnancyDeliveryDateConcept = Context.getAdministrationService().getGlobalProperty("reports.pregnancyDeliveryDateConcept");
		properties.put("PREGNANCY_DELIVERY_DATE_CONCEPT", pregnancyDeliveryDateConcept);
		
		String allFirstLineARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allFirstLineArtDrugsConceptSet");
		properties.put("ALL_FIRST_LINE_ART_DRUGS_CONCEPT", allFirstLineARTDrugsConcept);
		
		String allSecondLineARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allSecondLineArtDrugsConceptSet");
		properties.put("ALL_SECOND_LINE_ART_DRUGS_CONCEPT", allSecondLineARTDrugsConcept);
		
		String tbTestConcept = Context.getAdministrationService().getGlobalProperty("reports.tbTestConcept");
		properties.put("TB_TEST_CONCEPT", tbTestConcept);
	}
	
}
