package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AgeAtDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedAfterDateRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueBeforeDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;

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
		
		h.purgeDefinition(CohortDefinition.class, "pmtct: In Program");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Combined HFCSP consultation");
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("pmtct: In Program"), new HashMap<String,Object>());
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		
		PersonAttributeCohortDefinition location = new PersonAttributeCohortDefinition();
		PersonAttributeType healthCenterType = Context.getPersonService().getPersonAttributeTypeByName("Health Center");
		location.setAttributeType(healthCenterType);
		
		List<Location> locations = new ArrayList<Location>();
		Location currentLocationObj = Context.getLocationService().getLocation(properties.get("CURRENT_LOCATION"));
		locations.add(currentLocationObj);
		location.setValueLocations(locations);
		dataSetDefinition.addFilter(location);
		
		InStateCohortDefinition onARTCohort = new InStateCohortDefinition();
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		Program hiv = Context.getProgramWorkflowService().getProgramByName(properties.get("HIV_PROGRAM"));
		Program pediHiv = Context.getProgramWorkflowService().getProgramByName("PEDIATRIC HIV PROGRAM");
		
		if(hiv != null)
		{
			ProgramWorkflow txStatus = hiv.getWorkflowByName(properties.get("HIV_WORKFLOW_STATUS"));
			if(txStatus != null)
			{
				ProgramWorkflowState onART = txStatus.getState(properties.get("HIV_ON_ART_STATE"));
				if(onART != null)
				{
					states.add(onART);
				}
			}
		}
		if(pediHiv != null)
		{
			ProgramWorkflow pediTxStatus = pediHiv.getWorkflowByName("TX STATUS");
			if(pediTxStatus != null)
			{
				ProgramWorkflowState onART = pediTxStatus.getState("ON ANTIRETROVIRALS");
				if(onART != null)
				{
					states.add(onART);
				}
			}
		}
		
		
		if(states.size() > 0)
		{
			onARTCohort.setStates(states);
			dataSetDefinition.addFilter(onARTCohort);
		}
		
		DateOfWorkflowStateChange startDate = new DateOfWorkflowStateChange();
		Concept artConcept = Context.getConceptService().getConcept(new Integer(1577));
		startDate.setConcept(artConcept);
		startDate.setName("Commencement of ART");
		startDate.setDescription("Commencement of ART");
		dataSetDefinition.addColumn(startDate);
		
		FirstDrugOrderStartedRestrictedByConceptSet startDateDrugs = new FirstDrugOrderStartedRestrictedByConceptSet();
		startDateDrugs.setName("Start ART Regimen");
		startDateDrugs.setDescription("Start ART Regimen");
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		startDateDrugs.setDrugConceptSetConcept(artDrugsSet);
		dataSetDefinition.addColumn(startDateDrugs);
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		dataSetDefinition.addColumn(imbId);
		
		PatientIdentifierType tracNetType = Context.getPatientService().getPatientIdentifierTypeByName("TRACnet ID");
		PatientIdentifier tracNetId = new PatientIdentifier(tracNetType);
		tracNetId.setName("TracNetID");
		tracNetId.setDescription("TracNetId");
		dataSetDefinition.addColumn(tracNetId);
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName);
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName);
		
		PatientProperty gender = new PatientProperty("gender");
		dataSetDefinition.addColumn(gender);
		
		PatientProperty birthdate = new PatientProperty("birthdate");
		dataSetDefinition.addColumn(birthdate);
		
		AgeAtDateOfOtherDefinition ageAtStart = new AgeAtDateOfOtherDefinition();
		ageAtStart.setDateOfPatientData(startDate);
		dataSetDefinition.addColumn(ageAtStart);
		
		ObsValueAfterDateOfOtherDefinition weightAtStart = new ObsValueAfterDateOfOtherDefinition();
		Concept weight = Context.getConceptService().getConcept(new Integer(properties.get("WEIGHT_CONCEPT")));
		weightAtStart.setConcept(weight);
		weightAtStart.setDateOfPatientData(startDate);
		dataSetDefinition.addColumn(weightAtStart);
		
		ObsValueAfterDateOfOtherDefinition stageAtStart = new ObsValueAfterDateOfOtherDefinition();
		Concept stage = Context.getConceptService().getConcept(new Integer(properties.get("STAGE_CONCEPT")));
		stageAtStart.setConcept(stage);
		stageAtStart.setDateOfPatientData(startDate);
		stageAtStart.setName("Initial stage");
		stageAtStart.setDescription("Initial stage");
		dataSetDefinition.addColumn(stageAtStart);
		
		ObsValueBeforeDateOfOtherDefinition cd4CountAtStartBefore = new ObsValueBeforeDateOfOtherDefinition();
		Concept cd4 = Context.getConceptService().getConcept(new Integer(properties.get("CD4_CONCEPT")));
		cd4CountAtStartBefore.setConcept(cd4);
		cd4CountAtStartBefore.setDateOfPatientData(startDate);
		cd4CountAtStartBefore.setName("Initial CD4 count");
		cd4CountAtStartBefore.setDescription("Initial CD4 count");
		
		ObsValueAfterDateOfOtherDefinition cd4CountAtStartAfter = new ObsValueAfterDateOfOtherDefinition();
		cd4CountAtStartAfter.setConcept(cd4);
		cd4CountAtStartAfter.setDateOfPatientData(startDate);
		cd4CountAtStartAfter.setName("Initial CD4 count");
		cd4CountAtStartAfter.setDescription("Initial CD4 count");
		
		MultiplePatientDataDefinitions cd4CountAtStart = new MultiplePatientDataDefinitions();
		cd4CountAtStart.setName("Initial CD4 count");
		cd4CountAtStart.setDescription("Initial CD4 count");
		cd4CountAtStart.addPatientDataDefinition(cd4CountAtStartBefore);
		cd4CountAtStart.addPatientDataDefinition(cd4CountAtStartAfter);
		dataSetDefinition.addColumn(cd4CountAtStart);
		
		ObsValueBeforeDateOfOtherDefinition cd4PercentAtStart = new ObsValueBeforeDateOfOtherDefinition();
		Concept cd4percentage = Context.getConceptService().getConcept(new Integer(properties.get("CD4_PERCENTAGE_CONCEPT")));
		cd4PercentAtStart.setConcept(cd4percentage);
		cd4PercentAtStart.setDateOfPatientData(startDate);
		dataSetDefinition.addColumn(cd4PercentAtStart);
		
		AllDrugOrdersRestrictedByConcept ctx = new AllDrugOrdersRestrictedByConcept();
		ctx.setName("CTX treatment");
		ctx.setDescription("CTX treatment");
		Concept ctxConcept = Context.getConceptService().getConcept(new Integer(properties.get("CTX_TREATMENT_CONCEPT")));
		ctx.setConcept(ctxConcept);
		dataSetDefinition.addColumn(ctx);
		
		AllDrugOrdersRestrictedByConcept tbTreatment = new AllDrugOrdersRestrictedByConcept();
		tbTreatment.setName("TB Treatment");
		tbTreatment.setDescription("TB Treatment");
		Concept tbDrugConcept = Context.getConceptService().getConcept(new Integer(properties.get("TB_TREATMENT_CONCEPT")));
		tbTreatment.setConcept(tbDrugConcept);
		dataSetDefinition.addColumn(tbTreatment);
		
		AllObservationValues deliveryDate = new AllObservationValues();
		Concept delivery = Context.getConceptService().getConcept(new Integer(properties.get("PREGNANCY_DELIVERY_DATE_CONCEPT")));
		deliveryDate.setConcept(delivery);
		dataSetDefinition.addColumn(deliveryDate);
		
		FirstDrugOrderStartedAfterDateRestrictedByConceptSet artDrugsInitial = new FirstDrugOrderStartedAfterDateRestrictedByConceptSet();
		artDrugsInitial.setName("Initial Regimen");
		artDrugsInitial.setDescription("Initial Regimen");
		artDrugsInitial.setDrugConceptSetConcept(artDrugsSet);
		artDrugsInitial.setDateOfPatientData(startDate);
		dataSetDefinition.addColumn(artDrugsInitial);
		
		AllDrugOrdersRestrictedByConceptSet firstLineChanges = new AllDrugOrdersRestrictedByConceptSet();
		firstLineChanges.setName("First Line Changes");
		firstLineChanges.setDescription("First Line Changes");
		Concept firstLineDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_FIRST_LINE_ART_DRUGS_CONCEPT")));
		firstLineChanges.setDrugConceptSetConcept(firstLineDrugsSet);
		dataSetDefinition.addColumn(firstLineChanges);
		
		AllDrugOrdersRestrictedByConceptSet secondLineChanges = new AllDrugOrdersRestrictedByConceptSet();
		secondLineChanges.setName("Second Line Changes");
		secondLineChanges.setDescription("Second Line Changes");
		Concept secondLineDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_SECOND_LINE_ART_DRUGS_CONCEPT")));
		secondLineChanges.setDrugConceptSetConcept(secondLineDrugsSet);
		dataSetDefinition.addColumn(secondLineChanges);
		
		AllDrugOrdersRestrictedByConceptSet artDrugs = new AllDrugOrdersRestrictedByConceptSet();
		artDrugs.setName("ART Drugs");
		artDrugs.setDescription("ART Drugs");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		dataSetDefinition.addColumn(artDrugs);
		
		AllObservationValues cd4Ongoing = new AllObservationValues();
		cd4Ongoing.setConcept(cd4);
		dataSetDefinition.addColumn(cd4Ongoing);
		
		AllObservationValues whoStage = new AllObservationValues();
		whoStage.setConcept(stage);
		dataSetDefinition.addColumn(whoStage);
		
		AllObservationValues tbStatus = new AllObservationValues();
		Concept tb = Context.getConceptService().getConcept(new Integer(properties.get("TB_TEST_CONCEPT")));
		tbStatus.setConcept(tb);
		dataSetDefinition.addColumn(tbStatus);
		
		//dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		//mappings.put("location", "${location}");
		
		reportDefinition.addDataSetDefinition("Register", dataSetDefinition, mappings);
		
		//h.replaceDataSetDefinition(dataSetDefinition);
	}
	
	
	
	private void createCohortDefinitions() {
		
		InProgramCohortDefinition inPMTCTProgram = new InProgramCohortDefinition();
		inPMTCTProgram.setName("pmtct: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_PROGRAM"));
		if(pmtct != null)
		{
			programs.add(pmtct);
		}
		inPMTCTProgram.setPrograms(programs);
		h.replaceCohortDefinition(inPMTCTProgram);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_PROGRAM", pmtctProgram);
		
		String workflowStatus = Context.getAdministrationService().getGlobalProperty("reports.pmtctGaveBirthStatus");
		properties.put("PMTCT_GAVE_BIRTH_STATUS", workflowStatus);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
	}
	
}
