package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
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
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculators.ArtDetails;
import org.openmrs.module.rwandareports.customcalculators.HIVAdultAlerts;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;

public class SetupAdultHIVConsultationSheet {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupAdultHIVConsultationSheet(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		h.createRowPerPatientXlsOverview(rd, "AdultHIVConsultationSheet.xls", "AdultHIVConsultationSheet.xls_", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("AdultHIVConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Adult HIV Consultation Sheet");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "Adult HIV Consultation Sheet Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "AdultHIVLocation: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Adult HIV Consultation Sheet");
		
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		reportDefinition.addParameter(new Parameter("date", "Date", Date.class));
		reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class));
		//This is waiting on changes to the reporting framework to allow for filtering of the state parameter
		//so the user is only presented with the treatment group options
		Properties stateProperties = new Properties();
		stateProperties.setProperty("Program", properties.get("ADULT_HIV_PROGRAM"));
		stateProperties.setProperty("Workflow", properties.get("TREATMENT_STATUS"));
		//reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class, stateProperties));
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("AdultHIVLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.addParameter(new Parameter("date", "Date", Date.class));
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		dataSetDefinition.addParameter(new Parameter("state", "State", ProgramWorkflowState.class));
		
		InStateCohortDefinition hivGroup = new InStateCohortDefinition();
		hivGroup.addParameter(new Parameter("states", "Group", ProgramWorkflowState.class));
		hivGroup.setName("hiv group");
		dataSetDefinition.addFilter(hivGroup, ParameterizableUtil.createParameterMappings("states=${state}"));
		
		InProgramCohortDefinition inHIVProgram = new InProgramCohortDefinition();
		inHIVProgram.addParameter(new Parameter("onDate", "Date", Date.class));
		inHIVProgram.setName("adultHIV: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program hiv = Context.getProgramWorkflowService().getProgram(Integer.parseInt(properties.get("ADULT_HIV_PROGRAM_ID")));
		if(hiv != null)
		{
			programs.add(hiv);
		}
		inHIVProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inHIVProgram, ParameterizableUtil.createParameterMappings("onDate=${date}"));
		
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
		dataSetDefinition.addColumn(weight, new HashMap<String,Object>());
		
		AllObservationValues cd4Test = new AllObservationValues();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		cd4Test.setFilter(new LastThreeObsFilter());
		cd4Test.setDateFormat("ddMMMyy");
		cd4Test.setOutputFilter(new ObservationFilter());
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
		PatientRelationship accomp = new PatientRelationship();
		accomp.setName("AccompName");
		accomp.setRelationshipTypeId(Integer.valueOf(properties.get("ACCOMPAGNATUER_RELATIONSHIP_ID")));
		accomp.setRetrievePersonAorB("A");
		dataSetDefinition.addColumn(accomp, new HashMap<String,Object>());
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		
		CurrentOrdersRestrictedByConceptSet artDrugs = new CurrentOrdersRestrictedByConceptSet();
		artDrugs.setName("Regimen");
		artDrugs.setDescription("Regimen");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		artDrugs.setDateFormat("dd-MMM-yyyy");
		artDrugs.setDrugFilter(new DrugNameFilter());
		
		FirstDrugOrderStartedRestrictedByConceptSet startDateDrugs = new FirstDrugOrderStartedRestrictedByConceptSet();
		startDateDrugs.setName("Start ART Regimen");
		startDateDrugs.setDescription("Start ART Regimen");
		startDateDrugs.setDrugConceptSetConcept(artDrugsSet);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions artDetails = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		artDetails.addPatientDataToBeEvaluated(artDrugs, new HashMap<String, Object>());
		artDetails.addPatientDataToBeEvaluated(startDateDrugs, new HashMap<String, Object>());
		artDetails.setName("artDetails");
		artDetails.setDescription("artDetails");
		artDetails.setCalculator(new ArtDetails());
		dataSetDefinition.addColumn(artDetails, new HashMap<String, Object>());
		
		CurrentOrdersRestrictedByConceptSet tbTreatment = new CurrentOrdersRestrictedByConceptSet();
		tbTreatment.setName("TB Treatment");
		tbTreatment.setDescription("TB Treatment");
		tbTreatment.setDrugFilter(new DrugNameFilter());
		tbTreatment.setDateFormat("ddMMMyy");
		Concept tbDrugConcept = Context.getConceptService().getConcept(new Integer(properties.get("TB_TREATMENT_CONCEPT")));
		tbTreatment.setDrugConceptSetConcept(tbDrugConcept);
		dataSetDefinition.addColumn(tbTreatment, new HashMap<String,Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.setCalculator(new HIVAdultAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("date", "${date}");
		mappings.put("state", "${state}");
		
		reportDefinition.addDataSetDefinition("Register", dataSetDefinition, mappings);
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("AdultHIVLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String adultHIVId = Context.getAdministrationService().getGlobalProperty("hiv.programid.adult");
		properties.put("ADULT_HIV_PROGRAM_ID", adultHIVId);
		
		String adultHIV = Context.getAdministrationService().getGlobalProperty("reports.hivprogramname");
		properties.put("ADULT_HIV_PROGRAM", adultHIV);
		
		String treatmentStatus = Context.getAdministrationService().getGlobalProperty("reports.hivtreatmentstatus");
		properties.put("TREATMENT_STATUS", treatmentStatus);
		
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
	}	
	
}
