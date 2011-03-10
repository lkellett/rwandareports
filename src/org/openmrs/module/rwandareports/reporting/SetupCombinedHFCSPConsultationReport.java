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
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AgeAtDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedAfterDateRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueBeforeDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAgeInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DiscordantCoupleFilter;

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
		
		h.purgeDefinition(CohortDefinition.class, "location: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Combined HFCSP consultation");
		
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("location: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		
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
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName, new HashMap<String,Object>());
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("PMTCT_IDENTIFIER_TYPE"));
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbId.setName("InfantIMBId");
		imbType.setPatientIdentifierTypeId(imbId.getId());
		dataSetDefinition.addColumn(imbId, new HashMap<String,Object>());
		
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
		motherId.setDefinition(imbId, new HashMap<String,Object>());
		motherId.setName("MotherId");
		motherId.setDescription("MotherId");
		dataSetDefinition.addColumn(motherId, new HashMap<String,Object>());
		
		PatientProperty birthdate = new PatientProperty("birthdate");
		dataSetDefinition.addColumn(birthdate, new HashMap<String,Object>());
		
		PatientAgeInMonths ageInMonths = new PatientAgeInMonths();
		dataSetDefinition.addColumn(ageInMonths, new HashMap<String,Object>());
		
		StateOfPatient feedingGroup = new StateOfPatient();
		feedingGroup.setName("FeedingGroup");
		feedingGroup.setDescription("FeedingGroup");
		feedingGroup.setPatientProgram(pmtctCombined);
		feedingGroup.setPatienProgramWorkflow(pmtctCombined.getWorkflowByName(properties.get("PMTCT_FEEDING_STATUS_WORKFLOW")));
		dataSetDefinition.addColumn(feedingGroup, new HashMap<String,Object>());
		
		MostRecentObservation nextVisit = new MostRecentObservation();
		Concept nextVisitConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("PMTCT_NEXT_VISIT_CONCEPT_ID")));
		nextVisit.setConcept(nextVisitConcept);
		nextVisit.setName("NextVisit");
		dataSetDefinition.addColumn(nextVisit, new HashMap<String,Object>());
		
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
		discordant.setName("FeedingGroup");
		discordant.setDescription("FeedingGroup");
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
		cd4Test.setName("DBSTest");
	
		EvaluateDefinitionForOtherPersonData motherCD4 = new EvaluateDefinitionForOtherPersonData();
		motherCD4.setPersonData(mother, new HashMap<String,Object>());
		motherCD4.setDefinition(cd4Test, new HashMap<String,Object>());
		motherCD4.setName("motherCD4");
		motherCD4.setDescription("motherCD4");
		dataSetDefinition.addColumn(motherCD4, new HashMap<String,Object>());
		//dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		//mappings.put("location", "${location}");
		
		reportDefinition.addDataSetDefinition("Register", dataSetDefinition, mappings);
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
		
		String pmtctCombinedProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctcombinedprogramname");
		properties.put("PMTCT_COMBINED_CLINIC_PROGRAM", pmtctCombinedProgram);
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_CLINIC_PROGRAM", pmtctProgram);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.pmtctIdIdentifier");
		properties.put("PMTCT_IDENTIFIER_TYPE", identifierType);
		
		String relationshipType = Context.getAdministrationService().getGlobalProperty("reports.pmtctMotherRelationship");
		properties.put("PMTCT_MOTHER_RELATIONSHIP_ID", relationshipType);
		
		String feedingStatus = Context.getAdministrationService().getGlobalProperty("reports.pmtctFeedingStatusWorkflowName");
		properties.put("PMTCT_FEEDING_STATUS_WORKFLOW", feedingStatus);
		
		String relationshipStatus = Context.getAdministrationService().getGlobalProperty("reports.pmtctRelationshipStatusWorkflowName");
		properties.put("PMTCT_RELATIONSHIP_STATUS_WORKFLOW", relationshipStatus);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
		
		String nextVisitConcept = Context.getAdministrationService().getGlobalProperty("reports.pmtctNextVisitConcept");
		properties.put("PMTCT_NEXT_VISIT_CONCEPT_ID", nextVisitConcept);
		
		String dbsConcept = Context.getAdministrationService().getGlobalProperty("reports.dbsConcept");
		properties.put("DBS_CONCEPT_ID", dbsConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
	}	
	
}
