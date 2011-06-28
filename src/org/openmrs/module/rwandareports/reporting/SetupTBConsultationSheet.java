package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfProgramEnrolment;
import org.openmrs.module.rowperpatientreports.patientdata.definition.IsEnrolledInProgram;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculators.TBAlerts;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;
import org.openmrs.module.rwandareports.filter.PosOrNegFilter;

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
		h.createRowPerPatientXlsOverview(rd, "TBConsultationSheet.xls", "TBConsultationSheet.xls_", null);
//		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "TBConsultationSheetV2.xls", "TBConsultationSheetV2.xls_", null);
//		
//		Properties props = new Properties();
//		props.put("repeatSheet1Row6", "dataSet");
//	
//		design.setProperties(props);
//		h.saveReportDesign(design);
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
		reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class));
		//This is waiting on changes to the reporting framework to allow for filtering of the state parameter
		//so the user is only presented with the treatment group options
		Properties stateProperties = new Properties();
		stateProperties.setProperty("Program", properties.get("TB_PROGRAM"));
		stateProperties.setProperty("Workflow", properties.get("TREATMENT_GROUP"));
		//reportDefinition.addParameter(new Parameter("state", "Group", ProgramWorkflowState.class, stateProperties));
		
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
		tbGroup.setName("tb group");
		dataSetDefinition.addFilter(tbGroup, ParameterizableUtil.createParameterMappings("states=${state}"));
		
		InProgramCohortDefinition inTBProgram = new InProgramCohortDefinition();
		inTBProgram.setOnDate(Calendar.getInstance().getTime());
		inTBProgram.setName("tb: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program tb = Context.getProgramWorkflowService().getProgram(Integer.parseInt(properties.get("TB_PROGRAM_ID")));
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
		
		PatientProperty gender = new PatientProperty("gender");
		dataSetDefinition.addColumn(gender, new HashMap<String,Object>());
		
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
		
		IsEnrolledInProgram hivEnrolment = new IsEnrolledInProgram();
		hivEnrolment.setProgramId(new Integer(properties.get("ADULT_HIV_PROGRAM_ID")));
		hivEnrolment.setFilter(new PosOrNegFilter());
		hivEnrolment.setName("HIVStatus");
		dataSetDefinition.addColumn(hivEnrolment, new HashMap<String,Object>());
		
		Concept heightConcept = Context.getConceptService().getConcept(new Integer(properties.get("HEIGHT_CONCEPT")));
		
		MostRecentObservation mostRecentHeight = new MostRecentObservation();
		mostRecentHeight.setConcept(heightConcept);
		mostRecentHeight.setName("RecentHeight");
		
		MostRecentObservation cd4Test = new MostRecentObservation();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		cd4Test.setDateFormat("@ddMMMyy");
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
		DateOfProgramEnrolment tbStart = new DateOfProgramEnrolment();
		tbStart.setName("tbStart");
		tbStart.setProgramId(new Integer(properties.get("TB_PROGRAM_ID")));
		tbStart.setDateFormat("ddMMMyy");
		dataSetDefinition.addColumn(tbStart, new HashMap<String, Object>());
		
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
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(mostRecentHeight, new HashMap<String, Object>());
		alert.setCalculator(new TBAlerts());
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
		
		String tbId = Context.getAdministrationService().getGlobalProperty("tb.programid");
		properties.put("TB_PROGRAM_ID", tbId);
		
		String hivId = Context.getAdministrationService().getGlobalProperty("hiv.programid.adult");
		properties.put("ADULT_HIV_PROGRAM_ID", hivId);
		
		String tb = Context.getAdministrationService().getGlobalProperty("reports.tbprogramname");
		properties.put("TB_PROGRAM", tb);
		
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
	}	
	
}
