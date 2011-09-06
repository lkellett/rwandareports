package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
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
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.customcalculators.HIVPediAlerts;
import org.openmrs.module.rwandareports.customcalculators.NextCD4;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.InformedStateFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.RemoveDecimalFilter;

public class SetupPediHIVConsultationSheet {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupPediHIVConsultationSheet(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
//		h.createRowPerPatientXlsOverview(rd, "PediHIVConsultationSheet.xls", "PediHIVConsultationSheet.xls_", null);
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediHIVConsultationSheetV2.xls", "PediHIVConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
	
		design.setProperties(props);
		h.saveReportDesign(design);
		
		ReportDesign design2 = h.createRowPerPatientXlsOverviewReportDesign(rd, "PediPreArtHIVConsultationSheet.xls", "PediPreArtHIVConsultationSheet.xls_", null);
		
		Properties props2 = new Properties();
		props2.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
	
		design2.setProperties(props2);
		h.saveReportDesign(design2);	
		
		ReportDesign design3 = h.createRowPerPatientXlsOverviewReportDesign(rd, "BactrimSheet.xls", "Bactrim.xls_", null);
		
		Properties props3 = new Properties();
		props3.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
	
		design3.setProperties(props3);
		h.saveReportDesign(design3);	
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PediHIVConsultationSheet.xls_".equals(rd.getName()) || "PediPreArtHIVConsultationSheet.xls_".equals(rd.getName()) || "Bactrim.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Pedi HIV Consultation Sheet");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "Pedi HIV Consultation Sheet Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "PediHIVLocation: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Pedi HIV Consultation Sheet");
		
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		//reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class));
		//This is waiting on changes to the reporting framework to allow for filtering of the state parameter
		//so the user is only presented with the treatment group options
		Properties stateProperties = new Properties();
		stateProperties.setProperty("Program", properties.get("PEDI_HIV_PROGRAM"));
		stateProperties.setProperty("Workflow", properties.get("TREATMENT_STATUS"));
		reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class, stateProperties));
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("PediHIVLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
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
		
		InStateCohortDefinition hivGroup = new InStateCohortDefinition();
		hivGroup.addParameter(new Parameter("states", "Group", ProgramWorkflowState.class));
		hivGroup.setName("hiv group");
		hivGroup.setOnDate(new Date());
		dataSetDefinition.addFilter(hivGroup, ParameterizableUtil.createParameterMappings("states=${state}"));
		
		InProgramCohortDefinition inHIVProgram = new InProgramCohortDefinition();
		inHIVProgram.setOnDate(Calendar.getInstance().getTime());
		inHIVProgram.setName("pediHIV: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program hiv = Context.getProgramWorkflowService().getProgram(Integer.parseInt(properties.get("PEDI_HIV_PROGRAM_ID")));
		if(hiv != null)
		{
			programs.add(hiv);
		}
		inHIVProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inHIVProgram, new HashMap<String,Object>());
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty age = new PatientProperty("age");
		dataSetDefinition.addColumn(age, new HashMap<String,Object>());
		
		MostRecentObservation mostRecentTBScreening = new MostRecentObservation();
		Concept tbTestConcept = Context.getConceptService().getConcept(new Integer(properties.get("TB_TEST_CONCEPT")));
		mostRecentTBScreening.setConcept(tbTestConcept);
		mostRecentTBScreening.setName("RecentTB");
		mostRecentTBScreening.setDateFormat("@ddMMMyy");
		dataSetDefinition.addColumn(mostRecentTBScreening, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName, new HashMap<String,Object>());
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("IMB_IDENTIFIER_TYPE"));
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		
		
		PatientIdentifierType pcType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("PRIMARY_CARE_IDENTIFIER_TYPE"));
		PatientIdentifier pcId = new PatientIdentifier(pcType);
		
		
		MultiplePatientDataDefinitions infantId = new MultiplePatientDataDefinitions();
		infantId.setName("Id");
		infantId.addPatientDataDefinition(imbId, new HashMap<String,Object>());
		infantId.addPatientDataDefinition(pcId, new HashMap<String,Object>());
		dataSetDefinition.addColumn(infantId, new HashMap<String,Object>());
		
		DateOfBirthShowingEstimation birthdate = new DateOfBirthShowingEstimation();
		birthdate.setDateFormat("dd-MMM-yyyy");
		birthdate.setEstimatedDateFormat("yyyy");
		dataSetDefinition.addColumn(birthdate, new HashMap<String,Object>());
		
		MostRecentObservation cd4Test = new MostRecentObservation();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		cd4Test.setFilter(new RemoveDecimalFilter());
		cd4Test.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
		MostRecentObservation cd4Percent = new MostRecentObservation();
		Concept cd4PercentConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_PERCENT_CONCEPT")));
		cd4Percent.setConcept(cd4PercentConcept);
		cd4Percent.setName("CD4Percent");
		cd4Percent.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(cd4Percent, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions nextCD4 = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		nextCD4.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		nextCD4.setName("nextCD4");
		nextCD4.setDescription("nextCD4");
		nextCD4.setCalculator(new NextCD4());
		dataSetDefinition.addColumn(nextCD4, new HashMap<String, Object>());
		
		MostRecentObservation weight = new MostRecentObservation();
		Concept weightConcept = Context.getConceptService().getConcept(new Integer(properties.get("WEIGHT_CONCEPT")));
		weight.setConcept(weightConcept);
		weight.setName("weight");
		weight.setDateFormat("dd-MMM-yyyy");
		weight.setFilter(new RemoveDecimalFilter());
		dataSetDefinition.addColumn(weight, new HashMap<String, Object>());
		
		MostRecentObservation height = new MostRecentObservation();
		Concept heightConcept = Context.getConceptService().getConcept(new Integer(properties.get("HEIGHT_CONCEPT")));
		height.setConcept(heightConcept);
		height.setName("height");
		height.setDateFormat("dd-MMM-yyyy");
		height.setFilter(new RemoveDecimalFilter());
		dataSetDefinition.addColumn(height, new HashMap<String, Object>());
		
		MostRecentEncounterOfType visit = new MostRecentEncounterOfType();
		EncounterType pediInitial = Context.getEncounterService().getEncounterType(new Integer(properties.get("PEDI_INITIAL_ENCOUNTER")));
		EncounterType pediRDV = Context.getEncounterService().getEncounterType(new Integer(properties.get("PEDI_RDV_ENCOUNTER")));
		EncounterType pediFlowsheet = Context.getEncounterService().getEncounterType(new Integer(properties.get("PEDI_FLOWSHEET_ENCOUNTER")));
		visit.addEncounterType(pediInitial);
		visit.addEncounterType(pediRDV);
		visit.addEncounterType(pediFlowsheet);
		visit.setName("lastVisit");
		visit.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(visit, new HashMap<String, Object>());
		
		PatientAddress address = new PatientAddress();
		address.setName("Sector");
		address.setIncludeCountry(false);
		address.setIncludeProvince(false);
		address.setIncludeDistrict(false);
		address.setIncludeCell(false);
		address.setIncludeUmudugudu(false);
		dataSetDefinition.addColumn(address, new HashMap<String,Object>());
		
		PatientAddress address2 = new PatientAddress();
		address2.setName("Cell");
		address2.setIncludeCountry(false);
		address2.setIncludeProvince(false);
		address2.setIncludeDistrict(false);
		address2.setIncludeSector(false);
		address2.setIncludeUmudugudu(false);
		dataSetDefinition.addColumn(address2, new HashMap<String,Object>());
		
		MostRecentObservation viralLoad = new MostRecentObservation();
		Concept viralLoadConcept = Context.getConceptService().getConcept(new Integer(properties.get("VIRAL_LOAD_CONCEPT")));
		viralLoad.setConcept(viralLoadConcept);
		viralLoad.setName("viralLoad");
		viralLoad.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(viralLoad, new HashMap<String, Object>());
		
		CurrentOrdersRestrictedByConceptSet tbTreatment = new CurrentOrdersRestrictedByConceptSet();
		tbTreatment.setName("TB Treatment");
		tbTreatment.setDescription("TB Treatment");
		tbTreatment.setDrugFilter(new DrugNameFilter());
		tbTreatment.setDateFormat("dd-MMM-yyyy");
		Concept tbDrugConcept = Context.getConceptService().getConcept(new Integer(properties.get("TB_TREATMENT_CONCEPT")));
		tbTreatment.setDrugConceptSetConcept(tbDrugConcept);
		dataSetDefinition.addColumn(tbTreatment, new HashMap<String,Object>());
		
		StateOfPatient informed = new StateOfPatient();
		informed.setPatientProgram(hiv);
		informed.setPatienProgramWorkflow(hiv.getWorkflowByName(properties.get("INFORMED_STATUS")));
		informed.setName("informed");
		informed.setDescription("informed");		
		informed.setFilter(new InformedStateFilter());
		dataSetDefinition.addColumn(informed, new HashMap<String,Object>());
		
		StateOfPatient counselling = new StateOfPatient();
		counselling.setPatientProgram(hiv);
		counselling.setPatienProgramWorkflow(hiv.getWorkflowByName(properties.get("COUNSELLING_GROUP")));
		counselling.setName("counselling");
		counselling.setDescription("counselling");		
		dataSetDefinition.addColumn(counselling, new HashMap<String,Object>());
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		
		CurrentOrdersRestrictedByConceptSet artDrugs = new CurrentOrdersRestrictedByConceptSet();
		artDrugs.setName("Regimen");
		artDrugs.setDescription("Regimen");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		artDrugs.setDateFormat("dd-MMM-yyyy");
		artDrugs.setDrugFilter(new DrugNameFilter());
		dataSetDefinition.addColumn(artDrugs, new HashMap<String,Object>());
		
		FirstDrugOrderStartedRestrictedByConceptSet startArt = new FirstDrugOrderStartedRestrictedByConceptSet();
		startArt.setDrugConceptSetConcept(artDrugsSet);
		startArt.setName("StartART");
		startArt.setDescription("StartART");
		startArt.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(startArt, new HashMap<String,Object>());
		
		AllObservationValues allWeights = new AllObservationValues();
		allWeights.setConcept(weightConcept);
		allWeights.setName("weightObs");
		allWeights.setFilter(new LastThreeObsFilter());
		allWeights.setDateFormat("ddMMMyy");
		
		ObservationInMostRecentEncounterOfType io = new ObservationInMostRecentEncounterOfType();
		Concept ioConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("IO_CONCEPT")));
		io.setName("IO");
		io.setObservationConcept(ioConcept);
		EncounterType flowsheetEncounter = Context.getEncounterService().getEncounterType(Integer.valueOf(properties.get("FLOWSHEET_ENCOUNTER")));
		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		encounterTypes.add(flowsheetEncounter);
		io.setEncounterTypes(encounterTypes);
		
		ObservationInMostRecentEncounterOfType sideEffect = new ObservationInMostRecentEncounterOfType();
		Concept sideEffectConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("SIDE_EFFECT_CONCEPT")));
		sideEffect.setName("SideEffects");
		sideEffect.setObservationConcept(sideEffectConcept);
		encounterTypes.add(flowsheetEncounter);
		sideEffect.setEncounterTypes(encounterTypes);
		
		PatientRelationship accomp = new PatientRelationship();
		accomp.setName("AccompName");
		accomp.setRelationshipTypeId(Integer.valueOf(properties.get("ACCOMPAGNATUER_RELATIONSHIP_ID")));
		accomp.setRetrievePersonAorB("A");
		dataSetDefinition.addColumn(accomp, new HashMap<String,Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(allWeights, new HashMap<String,Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.setCalculator(new HIVPediAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("state", "${state}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("PediHIVLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String adultHIVId = Context.getAdministrationService().getGlobalProperty("hiv.programid.pediatric");
		properties.put("PEDI_HIV_PROGRAM_ID", adultHIVId);
		
		String adultHIV = Context.getAdministrationService().getGlobalProperty("reports.pedihivprogramname");
		properties.put("PEDI_HIV_PROGRAM", adultHIV);
		
		String treatmentStatus = Context.getAdministrationService().getGlobalProperty("reports.hivtreatmentstatus");
		properties.put("TREATMENT_STATUS", treatmentStatus);
		
		String informedStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatusinformed");
		properties.put("INFORMED_STATUS", informedStatus);
		
		String counselling = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatuscounselling");
		properties.put("COUNSELLING_GROUP", counselling);
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
		
		String heightConcept = Context.getAdministrationService().getGlobalProperty("reports.heightConcept");
		properties.put("HEIGHT_CONCEPT", heightConcept);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String pcIdentifierType = Context.getAdministrationService().getGlobalProperty("reports.primaryCareIdIdentifier");
		properties.put("PRIMARY_CARE_IDENTIFIER_TYPE", pcIdentifierType);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String viralLoadConcept = Context.getAdministrationService().getGlobalProperty("reports.viralLoadConcept");
		properties.put("VIRAL_LOAD_CONCEPT", viralLoadConcept);
		
		String cd4PercentConcept = Context.getAdministrationService().getGlobalProperty("reports.cd4PercentageConcept");
		properties.put("CD4_PERCENT_CONCEPT", cd4PercentConcept);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
		
		String tbTreatmentConcept = Context.getAdministrationService().getGlobalProperty("reports.tbTreatmentConcept");
		properties.put("TB_TREATMENT_CONCEPT", tbTreatmentConcept);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String ioConcept = Context.getAdministrationService().getGlobalProperty("reports.ioConcept");
		properties.put("IO_CONCEPT", ioConcept);
		
		String flowsheetEncounter = Context.getAdministrationService().getGlobalProperty("reports.pediflowsheetencounter");
		properties.put("FLOWSHEET_ENCOUNTER", flowsheetEncounter);
		
		String sideEffectConcept = Context.getAdministrationService().getGlobalProperty("reports.sideEffectConcept");
		properties.put("SIDE_EFFECT_CONCEPT", sideEffectConcept);
		
		String tbTestConcept = Context.getAdministrationService().getGlobalProperty("reports.tbTestConcept");
		properties.put("TB_TEST_CONCEPT", tbTestConcept);
		
		String visitConcept = Context.getAdministrationService().getGlobalProperty("reports.visitconcept");
		properties.put("VISIT_CONCEPT", visitConcept);
		
		String pediInitial = Context.getAdministrationService().getGlobalProperty("reports.pediInitialEncounter");
		properties.put("PEDI_INITIAL_ENCOUNTER", pediInitial);
		
		String pediRDV = Context.getAdministrationService().getGlobalProperty("reports.pediRdvEncounter");
		properties.put("PEDI_RDV_ENCOUNTER", pediRDV);
		
		String pediFlowsheet = Context.getAdministrationService().getGlobalProperty("reports.pediFlowsheetEncounter");
		properties.put("PEDI_FLOWSHEET_ENCOUNTER", pediFlowsheet);
		
	}	
	
}
