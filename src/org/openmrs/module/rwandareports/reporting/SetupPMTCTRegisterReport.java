package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AgeAtDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfProgramEnrolment;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedAfterDateRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueBeforeDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition2;
import org.openmrs.module.rwandareports.dataset.PMTCTRegisterDataSetDefinition;

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
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_COMBINED_CLINIC_PROGRAM"));
		if(pmtct != null)
		{
			pmtctPrograms.add(pmtct);
		}
		inPMTCTCombinedProgram.setPrograms(pmtctPrograms);
		dataSetDefinition.addFilter(inPMTCTCombinedProgram);
		
		DateOfProgramEnrolment infantRegistration = new DateOfProgramEnrolment();
		infantRegistration.setName("infantRegistration");
		infantRegistration.setDescription("infantDescription");
		infantRegistration.setProgramId(pmtct.getId());
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
//		PatientProperty gender = new PatientProperty("gender");
//		dataSetDefinition.addColumn(gender);
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
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
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
		
		
		
		
		
		
		String hivProgram = Context.getAdministrationService().getGlobalProperty("reports.hivprogramname");
		properties.put("HIV_PROGRAM", hivProgram);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
		
		String workflowStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatus");
		properties.put("HIV_WORKFLOW_STATUS", workflowStatus);
		
		String onARTState = Context.getAdministrationService().getGlobalProperty("reports.hivonartstate");
		properties.put("HIV_ON_ART_STATE", onARTState);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
		
		String stageConcept = Context.getAdministrationService().getGlobalProperty("reports.stageConcept");
		properties.put("STAGE_CONCEPT", stageConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String cd4PercentageConcept = Context.getAdministrationService().getGlobalProperty("reports.cd4PercentageConcept");
		properties.put("CD4_PERCENTAGE_CONCEPT", cd4PercentageConcept);
		
		String ctxTreatmentConcept = Context.getAdministrationService().getGlobalProperty("reports.ctxTreatmentConcept");
		properties.put("CTX_TREATMENT_CONCEPT", ctxTreatmentConcept);
		
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
