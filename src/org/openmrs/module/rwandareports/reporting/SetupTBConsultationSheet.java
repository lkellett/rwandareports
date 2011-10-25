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
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculators.HIVAdultAlerts;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;

public class SetupTBConsultationSheet {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupTBConsultationSheet(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
//		h.createRowPerPatientXlsOverview(rd, "TBConsultationSheet.xls", "TBConsultationSheet.xls_", null);
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "TBConsultationSheetV2.xls", "TBConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
	
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("TBConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "TB Consultation Sheet");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "TB Consultation Sheet Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "TBLocation: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("TB Consultation Sheet");
		
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		//reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class));
		//This is waiting on changes to the reporting framework to allow for filtering of the state parameter
		//so the user is only presented with the treatment group options
		Properties stateProperties = new Properties();
		stateProperties.setProperty("Program", properties.get("TB_PROGRAM"));
		stateProperties.setProperty("Workflow", properties.get("TREATMENT_GROUP"));
		reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class, stateProperties));
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("TBLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
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
		
		InStateCohortDefinition tbGroup = new InStateCohortDefinition();
		tbGroup.addParameter(new Parameter("states", "Group", ProgramWorkflowState.class));
		tbGroup.addParameter(new Parameter("onDate", "onDate", Date.class));
		tbGroup.setName("tb group");
		//tbGroup.setOnOrAfter(null);
		dataSetDefinition.addFilter(tbGroup, ParameterizableUtil.createParameterMappings("states=${state},onDate=${now}"));
		
		InProgramCohortDefinition inTBProgram = new InProgramCohortDefinition();
		inTBProgram.setOnDate(Calendar.getInstance().getTime());
		inTBProgram.setName("adultTB: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program tb = Context.getProgramWorkflowService().getProgramByName(properties.get("TB_PROGRAM"));
		if(tb != null)
		{
			programs.add(tb);
		}
		inTBProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inTBProgram, new HashMap<String,Object>());
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName, new HashMap<String,Object>());
		
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
		
		PatientProperty age = new PatientProperty("age");
		dataSetDefinition.addColumn(age, new HashMap<String,Object>());
		
		AllObservationValues weight = new AllObservationValues();
		Concept weightConcept = Context.getConceptService().getConcept(new Integer(properties.get("WEIGHT_CONCEPT")));
		weight.setConcept(weightConcept);
		weight.setName("weightObs");
		weight.setFilter(new LastThreeObsFilter());
		weight.setDateFormat("ddMMMyy");
		weight.setOutputFilter(new ObservationFilter());
		
		MostRecentObservation mostRecentWeight = new MostRecentObservation();
		mostRecentWeight.setConcept(weightConcept);
		mostRecentWeight.setName("RecentWeight");
		mostRecentWeight.setDateFormat("@ddMMMyy");
		dataSetDefinition.addColumn(mostRecentWeight, new HashMap<String,Object>());
		
		Concept heightConcept = Context.getConceptService().getConcept(new Integer(properties.get("HEIGHT_CONCEPT")));
		
		MostRecentObservation mostRecentHeight = new MostRecentObservation();
		mostRecentHeight.setConcept(heightConcept);
		mostRecentHeight.setName("RecentHeight");
		
		AllObservationValues cd4Test = new AllObservationValues();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		cd4Test.setFilter(new LastThreeObsFilter());
		cd4Test.setDateFormat("ddMMMyy");
		cd4Test.setOutputFilter(new ObservationFilter());
		
		MostRecentObservation cd4Result = new MostRecentObservation();
		cd4Result.setConcept(cd4Concept);
		cd4Result.setName("CD4Test");
		cd4Result.setDateFormat("@ddMMMyy");
		cd4Result.setIncludeNull(false);
		dataSetDefinition.addColumn(cd4Result, new HashMap<String,Object>());
		
		Concept viralConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("VIRAL_LOAD_CONCEPT")));
		MostRecentObservation viralLoad = new MostRecentObservation();
		viralLoad.setConcept(viralConcept);
		viralLoad.setName("ViralLoad");
		viralLoad.setDateFormat("@ddMMMyy");
		viralLoad.setIncludeNull(false);
		dataSetDefinition.addColumn(viralLoad, new HashMap<String,Object>());
		
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
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		
		CurrentOrdersRestrictedByConceptSet artDrugs = new CurrentOrdersRestrictedByConceptSet();
		artDrugs.setDescription("Regimen");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		artDrugs.setDateFormat("@ddMMMyy");
		artDrugs.setDrugFilter(new DrugNameFilter());
		artDrugs.setName("Regimen");
		dataSetDefinition.addColumn(artDrugs, new HashMap<String, Object>());
		
		CurrentOrdersRestrictedByConceptSet tbTreatment = new CurrentOrdersRestrictedByConceptSet();
		tbTreatment.setName("TB Treatment");
		tbTreatment.setDescription("TB Treatment");
		tbTreatment.setDrugFilter(new DrugNameFilter());
		tbTreatment.setDateFormat("@ddMMMyy");
		Concept tbDrugConcept = Context.getConceptService().getConcept(new Integer(properties.get("TB_TREATMENT_CONCEPT")));
		tbTreatment.setDrugConceptSetConcept(tbDrugConcept);
		dataSetDefinition.addColumn(tbTreatment, new HashMap<String,Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(mostRecentHeight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.setCalculator(new HIVAdultAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("state", "${state}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("TBLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String tb = Context.getAdministrationService().getGlobalProperty("reports.tbprogramname");
		properties.put("TB_PROGRAM", tb);
		
		String adultHIVId = Context.getAdministrationService().getGlobalProperty("hiv.programid.adult");
		properties.put("ADULT_HIV_PROGRAM_ID", adultHIVId);
		
		String adultHIV = Context.getAdministrationService().getGlobalProperty("reports.hivprogramname");
		properties.put("ADULT_HIV_PROGRAM", adultHIV);
		
		String treatmentgroup = Context.getAdministrationService().getGlobalProperty("reports.tbworkflowgroup");
		properties.put("TREATMENT_GROUP", treatmentgroup);
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String pcIdentifierType = Context.getAdministrationService().getGlobalProperty("reports.primaryCareIdIdentifier");
		properties.put("PRIMARY_CARE_IDENTIFIER_TYPE", pcIdentifierType);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
		
		String tbTreatmentConcept = Context.getAdministrationService().getGlobalProperty("reports.tbTreatmentConcept");
		properties.put("TB_TREATMENT_CONCEPT", tbTreatmentConcept);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String heightConcept = Context.getAdministrationService().getGlobalProperty("reports.heightConcept");
		properties.put("HEIGHT_CONCEPT", heightConcept);
		
		String tbTestConcept = Context.getAdministrationService().getGlobalProperty("reports.tbTestConcept");
		properties.put("TB_TEST_CONCEPT", tbTestConcept);
		
		String ioConcept = Context.getAdministrationService().getGlobalProperty("reports.ioConcept");
		properties.put("IO_CONCEPT", ioConcept);
		
		String flowsheetEncounter = Context.getAdministrationService().getGlobalProperty("reports.adultflowsheetencounter");
		properties.put("FLOWSHEET_ENCOUNTER", flowsheetEncounter);
		
		String sideEffectConcept = Context.getAdministrationService().getGlobalProperty("reports.sideEffectConcept");
		properties.put("SIDE_EFFECT_CONCEPT", sideEffectConcept);
		
		String viralLoadConcept = Context.getAdministrationService().getGlobalProperty("reports.viralLoadConcept");
		properties.put("VIRAL_LOAD_CONCEPT", viralLoadConcept);
		
	}	
	
}
