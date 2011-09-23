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
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAgeInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;

public class SetupPMTCTFormulaDistributionReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupPMTCTFormulaDistributionReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		//h.createRowPerPatientXlsOverview(rd, "PMTCTFormulaDistribution.xls", "PMTCTFormulaDistribution.xls_", null);
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTFormulaDistribution.xls", "PMTCTFormulaDistribution.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:4,dataset:dataSet");
	
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTFormulaDistribution.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "PMTCT Formula Package Distribution");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "PMTCT Formula Package Distribution Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "FPDlocation: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Formula Package Distribution");
		
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("FPDlocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
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
		inPMTCTProgram.setOnOrAfter(new Date());
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_COMBINED_CLINIC_PROGRAM"));
		if(pmtct != null)
		{
			programs.add(pmtct);
		}
		inPMTCTProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inPMTCTProgram, new HashMap<String,Object>());
		
		InStateCohortDefinition feedingStatus = new InStateCohortDefinition();
		List<ProgramWorkflowState> formulaStates = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow feedingStatusWorkflow = pmtct.getWorkflow(Integer.valueOf(properties.get("PMTCT_FEEDING_STATUS_WORKFLOW")));
		formulaStates.add(feedingStatusWorkflow.getState(Context.getConceptService().getConcept(Integer.parseInt(properties.get("FORMULA_STATE_ONE")))));
		formulaStates.add(feedingStatusWorkflow.getState(Context.getConceptService().getConcept(Integer.parseInt(properties.get("FORMULA_STATE_TWO")))));
		formulaStates.add(feedingStatusWorkflow.getState(Context.getConceptService().getConcept(Integer.parseInt(properties.get("FORMULA_STATE_THREE")))));
		feedingStatus.setStates(formulaStates);
		feedingStatus.setName("feeding state: Feeding state of patients");
		feedingStatus.setOnDate(new Date());
		dataSetDefinition.addFilter(feedingStatus,  new HashMap<String,Object>());
		
		Concept nextVisitConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("PMTCT_NEXT_VISIT_CONCEPT_ID")));
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName, new HashMap<String,Object>());
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("IMB_IDENTIFIER_TYPE"));
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		
		
		PatientIdentifierType pcType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("PRIMARY_CARE_IDENTIFIER_TYPE"));
		PatientIdentifier pcId = new PatientIdentifier(pcType);
		
		
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
		
		DateOfBirthShowingEstimation birthdate = new DateOfBirthShowingEstimation();
		dataSetDefinition.addColumn(birthdate, new HashMap<String,Object>());
		
		PatientAgeInMonths ageInMonths = new PatientAgeInMonths();
		dataSetDefinition.addColumn(ageInMonths, new HashMap<String,Object>());
		
		StateOfPatient feedingGroup = new StateOfPatient();
		feedingGroup.setName("FeedingGroup");
		feedingGroup.setDescription("FeedingGroup");
		feedingGroup.setPatientProgram(pmtct);
		feedingGroup.setPatienProgramWorkflow(feedingStatusWorkflow);
		dataSetDefinition.addColumn(feedingGroup, new HashMap<String,Object>());
		
		MostRecentObservation nextVisit = new MostRecentObservation();
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
		
		//dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
		
		h.replaceDataSetDefinition(dataSetDefinition);
	}
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location");
		location.setName("FPDlocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
	}
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctcombinedprogramname");
		properties.put("PMTCT_COMBINED_CLINIC_PROGRAM", pmtctProgram);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String pcIdentifierType = Context.getAdministrationService().getGlobalProperty("reports.primaryCareIdIdentifier");
		properties.put("PRIMARY_CARE_IDENTIFIER_TYPE", pcIdentifierType);
		
		String relationshipType = Context.getAdministrationService().getGlobalProperty("reports.pmtctMotherRelationship");
		properties.put("PMTCT_MOTHER_RELATIONSHIP_ID", relationshipType);
		
		String feedingStatus = Context.getAdministrationService().getGlobalProperty("reports.pmtctFeedingStatusWorkflowName");
		properties.put("PMTCT_FEEDING_STATUS_WORKFLOW", feedingStatus);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
		
		String nextVisitConcept = Context.getAdministrationService().getGlobalProperty("reports.pmtctNextVisitConcept");
		properties.put("PMTCT_NEXT_VISIT_CONCEPT_ID", nextVisitConcept);
		
		String formula1 = Context.getAdministrationService().getGlobalProperty("reports.formulaStateOne");
		properties.put("FORMULA_STATE_ONE", formula1);
		
		String formula2 = Context.getAdministrationService().getGlobalProperty("reports.formulaStateTwo");
		properties.put("FORMULA_STATE_TWO", formula2);
		
		String formula3 = Context.getAdministrationService().getGlobalProperty("reports.formulaStateThree");
		properties.put("FORMULA_STATE_THREE", formula3);
	}	
}
