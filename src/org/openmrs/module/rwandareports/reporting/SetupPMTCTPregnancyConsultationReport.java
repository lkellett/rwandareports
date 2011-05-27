package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
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
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfProgramEnrolment;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstRecordedObservationWithCodedConceptAnswer;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAgeInMonths;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.customcalculators.BreastFeedingOrFormula;
import org.openmrs.module.rwandareports.customcalculators.DecisionDate;
import org.openmrs.module.rwandareports.customcalculators.GestationalAge;
import org.openmrs.module.rwandareports.customcalculators.PMTCTInfantDBSDue;
import org.openmrs.module.rwandareports.customcalculators.StartOfARTForThisPMTCT;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.comparator.PMTCTDataSetRowComparator;
import org.openmrs.module.rwandareports.filter.DiscordantCoupleFilter;
import org.openmrs.module.rwandareports.filter.RemoveTimestampFilter;

public class SetupPMTCTPregnancyConsultationReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupPMTCTPregnancyConsultationReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		h.createRowPerPatientXlsOverview(rd, "PMTCTPregnancyConsultationSheet.xls", "PMTCTPregnancyConsultationSheet.xls_", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTPregnancyConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "PMTCT Pregnancy consultation");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "PMTCT Pregnancy consultation Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "location: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Pregnancy consultation");
		
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
		dataSetDefinition.setComparator(new PMTCTDataSetRowComparator());
		
		InProgramCohortDefinition inPMTCTProgram = new InProgramCohortDefinition();
		inPMTCTProgram.setName("pmtct: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_PROGRAM"));
		inPMTCTProgram.setOnDate(Calendar.getInstance().getTime());
		if(pmtct != null)
		{
			programs.add(pmtct);
		}
		inPMTCTProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inPMTCTProgram, new HashMap<String,Object>());
		
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
		
		FirstRecordedObservationWithCodedConceptAnswer diagnosisDate = new FirstRecordedObservationWithCodedConceptAnswer();
		diagnosisDate.setName("hivDiagnosis");
		diagnosisDate.setDescription("hivDiagnosis");
		Concept question = Context.getConceptService().getConcept(Integer.valueOf(properties.get("HIV_TEST_CONCEPT")));
		Concept answer = Context.getConceptService().getConcept(Integer.valueOf(properties.get("POSITIVE_HIV_RESULT_CONCEPT")));
		diagnosisDate.setAnswerRequired(answer);
		diagnosisDate.setQuestion(question);
		dataSetDefinition.addColumn(diagnosisDate, new HashMap<String, Object>());
		
		MostRecentObservation ddr = new MostRecentObservation();
		Concept ddrConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("DDR_CONCEPT")));
		ddr.setConcept(ddrConcept);
		ddr.setName("ddr");
		ddr.setDescription("ddr");
		dataSetDefinition.addColumn(ddr, new HashMap<String, Object>());
		
		MostRecentObservation dpa = new MostRecentObservation();
		Concept dpaConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("DPA_CONCEPT")));
		dpa.setConcept(dpaConcept);
		dpa.setName("dpa");
		dpa.setDescription("dpa");
		dpa.setFilter(new RemoveTimestampFilter());
		dataSetDefinition.addColumn(dpa, new HashMap<String, Object>());
		
		MostRecentObservation cd4Test = new MostRecentObservation();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
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
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		
		CurrentOrdersRestrictedByConceptSet artDrugs = new CurrentOrdersRestrictedByConceptSet();
		artDrugs.addParameter(new Parameter("onDate", "OnDate", ProgramWorkflowState.class));
		artDrugs.setName("Regimen");
		artDrugs.setDescription("Regimen");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		dataSetDefinition.addColumn(artDrugs, new HashMap<String,Object>());
		
		Concept nextVisitConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("PMTCT_NEXT_VISIT_CONCEPT_ID")));
		MostRecentObservation nextVisit = new MostRecentObservation();
		nextVisit.setConcept(nextVisitConcept);
		nextVisit.setName("nextVisit");
		nextVisit.setDescription("nextVisit");
		dataSetDefinition.addColumn(nextVisit, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions gestationalAge = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		gestationalAge.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		gestationalAge.setName("gestationalAge");
		gestationalAge.setDescription("gestationalAge");
		gestationalAge.setCalculator(new GestationalAge());
		dataSetDefinition.addColumn(gestationalAge, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions decisionDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		decisionDate.addPatientDataToBeEvaluated(artDrugs, new HashMap<String, Object>());
		decisionDate.setName("decisionDate");
		decisionDate.setDescription("decisionDate");
		decisionDate.setCalculator(new DecisionDate());
		dataSetDefinition.addColumn(decisionDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bOrF = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bOrF.setName("bOrF");
		bOrF.addPatientDataToBeEvaluated(decisionDate, new HashMap<String, Object>());
		bOrF.setCalculator(new BreastFeedingOrFormula());
		dataSetDefinition.addColumn(bOrF, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
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
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_PROGRAM", pmtctProgram);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String pcIdentifierType = Context.getAdministrationService().getGlobalProperty("reports.primaryCareIdIdentifier");
		properties.put("PRIMARY_CARE_IDENTIFIER_TYPE", pcIdentifierType);
		
		String hivConcept = Context.getAdministrationService().getGlobalProperty("reports.hivTestConcept");
		properties.put("HIV_TEST_CONCEPT", hivConcept);
		
		String positiveHivConcept = Context.getAdministrationService().getGlobalProperty("reports.positiveHivTestConcept");
		properties.put("POSITIVE_HIV_RESULT_CONCEPT", positiveHivConcept);
		
		String ddrConcept = Context.getAdministrationService().getGlobalProperty("reports.ddrConcept");
		properties.put("DDR_CONCEPT", ddrConcept);
		
		String dpaConcept = Context.getAdministrationService().getGlobalProperty("reports.dpaConcept");
		properties.put("DPA_CONCEPT", dpaConcept);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String nextVisitConcept = Context.getAdministrationService().getGlobalProperty("reports.pmtctNextVisitConcept");
		properties.put("PMTCT_NEXT_VISIT_CONCEPT_ID", nextVisitConcept);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
	}	
	
}
